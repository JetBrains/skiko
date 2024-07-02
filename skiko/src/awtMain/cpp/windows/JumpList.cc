#if SK_BUILD_FOR_WIN

#include <windows.h>
#include <wrl/client.h>
#include <shobjidl.h>
#include <propkey.h>
#include <propvarutil.h>

#include <optional>

#include "jni_helpers.h"
#include "exceptions_handler.h"

template <typename T>
using ComPtr = Microsoft::WRL::ComPtr<T>;

#define THROW_IF_FAILED(action, message, ret)                                                       \
    do {                                                                                            \
        HRESULT hr { action };                                                                      \
        if (FAILED(hr)) {                                                                           \
            throwJavaRuntimeExceptionByErrorCodeWithContext(env, __FUNCTION__, DWORD(hr), message); \
            return ret;                                                                             \
        }                                                                                           \
    } while ((void)0, 0)

class CoInitializeWrapper {
    HRESULT _hr;
public:
    CoInitializeWrapper(COINIT flags) {
        _hr = CoInitializeEx(NULL, flags);
    }
    ~CoInitializeWrapper() {
        if (SUCCEEDED(_hr)) {
            CoUninitialize();
        }
    }
    operator HRESULT() {
        return _hr;
    }
};

class PropVariantWrapper {
    PROPVARIANT _propvar;
    HRESULT _hr;
public:
    PropVariantWrapper(PROPVARIANT propvar) : _propvar(propvar), _hr(S_OK) { }
    PropVariantWrapper(PCWSTR pszValue) {
        _hr = InitPropVariantFromString(pszValue, &_propvar);
    }
    ~PropVariantWrapper() {
        if (SUCCEEDED(_hr)) {
            PropVariantClear(&_propvar);
        }
    }
    operator HRESULT() {
        return _hr;
    }
    operator PROPVARIANT() {
        return _propvar;
    }
};

void createShellLink(JNIEnv* env, PCWSTR pszArguments, PCWSTR pszTitle, IShellLinkW **ppsl) {
    ComPtr<IShellLinkW> psl;
    THROW_IF_FAILED(
        CoCreateInstance(CLSID_ShellLink, NULL, CLSCTX_INPROC_SERVER, IID_PPV_ARGS(&psl)),
        "Failed to create a shell link.",
    );

    WCHAR szAppPath[1024];
    if (0 == GetModuleFileNameW(NULL, szAppPath, ARRAYSIZE(szAppPath))) {
        THROW_IF_FAILED(HRESULT_FROM_WIN32(GetLastError()), "Failed to get current module's path.",);
    }

    THROW_IF_FAILED(psl->SetPath(szAppPath), "Failed to set shell link's path.",);
    THROW_IF_FAILED(psl->SetArguments(pszArguments), "Failed to set shell link's arguments.",);
    THROW_IF_FAILED(
        psl->SetDescription((pszTitle + std::wstring(L" (") + pszArguments + L")").c_str()),
        "Failed to set shell link's description.",
    );

    ComPtr<IPropertyStore> pps;
    THROW_IF_FAILED(psl->QueryInterface(IID_PPV_ARGS(&pps)), "Failed to cast shell link to IPropertyStore.",);

    PropVariantWrapper propvar(pszTitle);
    THROW_IF_FAILED(propvar, "Failed to create a PropVariant.",);

    THROW_IF_FAILED(pps->SetValue(PKEY_Title, propvar), "Failed to set shell link's title.",);
    THROW_IF_FAILED(pps->Commit(), "Failed to commit shell link's title.",);

    THROW_IF_FAILED(psl->QueryInterface(IID_PPV_ARGS(ppsl)), "Failed to return the shell link.",);
}

std::optional<std::pair<std::wstring, std::wstring>> getShellLinkTitleAndArguments(IShellLinkW *psl) {
    WCHAR szTitle[INFOTIPSIZE], szArguments[INFOTIPSIZE];
    ComPtr<IPropertyStore> propStore;
    if (SUCCEEDED(psl->QueryInterface(IID_PPV_ARGS(&propStore)))) {
        PROPVARIANT propvar;
        if (SUCCEEDED(propStore->GetValue(PKEY_Title, &propvar))) {
            PropVariantWrapper propvarWrapper(propvar);
            if (SUCCEEDED(PropVariantToString(propvar, szTitle, ARRAYSIZE(szTitle)))) {
                if (SUCCEEDED(psl->GetArguments(szArguments, ARRAYSIZE(szArguments)))) {
                    return std::make_pair<std::wstring, std::wstring>(szTitle, szArguments);
                }
            }
        }
    }
    return std::nullopt;
}

namespace org::jetbrains::skiko::windows::JumpListItem {
    jclass cls;
    jmethodID init;
    jmethodID getTitle;
    jmethodID getArguments;

    void ensure(JNIEnv* env) {
        static jclass _cls = cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/windows/JumpListItem"));
        static jmethodID _init = init = env->GetMethodID(_cls, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
        static jmethodID _getTitle = getTitle = env->GetMethodID(_cls, "getTitle", "()Ljava/lang/String;");
        static jmethodID _getArguments = getArguments = env->GetMethodID(_cls, "getArguments", "()Ljava/lang/String;");
    }
}

jobject createJumpListItemJObject(JNIEnv* env, const std::wstring& title, const std::wstring& arguments) {
    org::jetbrains::skiko::windows::JumpListItem::ensure(env);
    return env->NewObject(
        org::jetbrains::skiko::windows::JumpListItem::cls, org::jetbrains::skiko::windows::JumpListItem::init,
        env->NewString(reinterpret_cast<const jchar*>(title.c_str()), title.size()),
        env->NewString(reinterpret_cast<const jchar*>(arguments.c_str()), arguments.size())
    );
}

std::pair<std::wstring, std::wstring> getJumpListItemJObjectTitleAndArguments(JNIEnv* env, jobject obj) {
    org::jetbrains::skiko::windows::JumpListItem::ensure(env);
    std::wstring title = toStdString(env, (jstring) env->CallObjectMethod(obj, org::jetbrains::skiko::windows::JumpListItem::getTitle));
    std::wstring arguments = toStdString(env, (jstring) env->CallObjectMethod(obj, org::jetbrains::skiko::windows::JumpListItem::getArguments));
    return std::make_pair(title, arguments);
}

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1init(JNIEnv* env, jobject obj) {
        ComPtr<ICustomDestinationList> pcdl;
        THROW_IF_FAILED(
            CoCreateInstance(CLSID_DestinationList, NULL, CLSCTX_INPROC_SERVER, IID_PPV_ARGS(&pcdl)),
            "Failed to create CLSID_DestinationList.", 0L
        );

        UINT uMaxSlots;
        ComPtr<IObjectArray> poaRemoved;
        THROW_IF_FAILED(
            pcdl->BeginList(&uMaxSlots, IID_PPV_ARGS(&poaRemoved)),
            "Failed to BeginList.", 0L
        );

        return toJavaPointer(pcdl.Detach());
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1dispose(
        JNIEnv* env, jobject obj, jlong ptr)
    {
        ComPtr<ICustomDestinationList> pcdl;
        pcdl.Attach(fromJavaPointer<ICustomDestinationList*>(ptr));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1addUserTask(
        JNIEnv* env, jobject obj, jlong ptr, jstring taskName, jstring taskExecArg)
    {
        CoInitializeWrapper initialize(COINIT_MULTITHREADED);
        THROW_IF_FAILED(initialize, "Failed to initialize COM apartment.",);

        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJavaRuntimeExceptionByErrorCodeWithContext(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return;
        }

        std::wstring strTaskName = toStdString(env, taskName);
        std::wstring strTaskExecArg = toStdString(env, taskExecArg);

        ComPtr<IObjectCollection> poc;
        THROW_IF_FAILED(
            CoCreateInstance(CLSID_EnumerableObjectCollection, NULL, CLSCTX_INPROC, IID_PPV_ARGS(&poc)),
            "Failed to create an instance of an object collection.",
        );

        ComPtr<IShellLinkW> psl;
        createShellLink(env, strTaskExecArg.c_str(), strTaskName.c_str(), &psl);

        THROW_IF_FAILED(poc->AddObject(psl.Get()), "Failed to add the shell link to the collection.",);

        ComPtr<IObjectArray> poa;
        THROW_IF_FAILED(poc->QueryInterface(IID_PPV_ARGS(&poa)), "Failed to cast the collection to an array.",);

        THROW_IF_FAILED(pcdl->AddUserTasks(poa.Get()), "Failed to add user tasks.",);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1addCategory(
        JNIEnv* env, jobject obj, jlong ptr, jstring category, jobjectArray itemsArray)
    {
        CoInitializeWrapper initialize(COINIT_MULTITHREADED);
        THROW_IF_FAILED(initialize, "Failed to initialize COM apartment.",);

        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJavaRuntimeExceptionByErrorCodeWithContext(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return;
        }

        jsize itemsArraySize = env->GetArrayLength(itemsArray);
        if (0 == itemsArraySize) {
            return;
        }

        ComPtr<IObjectCollection> poc;
        THROW_IF_FAILED(
            CoCreateInstance(CLSID_EnumerableObjectCollection, NULL, CLSCTX_INPROC, IID_PPV_ARGS(&poc)),
            "Failed to create an instance of EnumerableObjectCollection.",
        );

        for (jsize i = 0; i < itemsArraySize; i++) {
            auto [title, arguments] = getJumpListItemJObjectTitleAndArguments(env, env->GetObjectArrayElement(itemsArray, i));

            ComPtr<IShellLinkW> psl;
            createShellLink(env, arguments.c_str(), title.c_str(), &psl);

            THROW_IF_FAILED(poc->AddObject(psl.Get()), "Failed to add an item to the category.",);
        }

        ComPtr<IObjectArray> poa;
        THROW_IF_FAILED(poc->QueryInterface(IID_PPV_ARGS(&poa)), "Failed to cast the collection to an array.",);

        std::wstring strCategory = toStdString(env, category);

        THROW_IF_FAILED(pcdl->AppendCategory(strCategory.c_str(), poa.Get()), "Failed to append category.",);
    }

    JNIEXPORT jobjectArray JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1getRemovedItems(
        JNIEnv* env, jobject obj, jlong ptr)
    {
        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJavaRuntimeExceptionByErrorCodeWithContext(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return NULL;
        }

        ComPtr<IObjectArray> poaRemoved;
        THROW_IF_FAILED(
            pcdl->GetRemovedDestinations(IID_PPV_ARGS(&poaRemoved)),
            "Failed to get removed destinations.", NULL
        );

        UINT cItems;
        THROW_IF_FAILED(poaRemoved->GetCount(&cItems), "Failed to get removed destinations count.", NULL);

        org::jetbrains::skiko::windows::JumpListItem::ensure(env);
        jobjectArray itemsArray = env->NewObjectArray(cItems, org::jetbrains::skiko::windows::JumpListItem::cls, NULL);

        for (UINT i = 0; i < cItems; i++) {
            ComPtr<IShellLinkW> pslRemoved;
            if (SUCCEEDED(poaRemoved->GetAt(i, IID_PPV_ARGS(&pslRemoved)))) {
                auto optional = getShellLinkTitleAndArguments(pslRemoved.Get());
                if (optional.has_value()) {
                    auto [title, arguments] = optional.value();
                    env->SetObjectArrayElement(itemsArray, i, createJumpListItemJObject(env, title, arguments));
                }
            }
        }

        return itemsArray;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1commit(
        JNIEnv* env, jobject obj, jlong ptr)
    {
        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJavaRuntimeExceptionByErrorCodeWithContext(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return;
        }
        THROW_IF_FAILED(pcdl->CommitList(), "Failed to CommitList.",);
    }
}

#endif
