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

namespace org::jetbrains::skiko::windows {
    namespace JumpListInteropItem {
        jclass cls;
        jmethodID init;
        jmethodID getTitle;
        jmethodID getArguments;
        jmethodID getDescription;
        jmethodID getIconPath;
        jmethodID getIconNum;
    }

    void ensure(JNIEnv* env) {
        static jclass _cls = JumpListInteropItem::cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/windows/JumpListBuilder$JumpListInteropItem"));
        static jmethodID _init = JumpListInteropItem::init = env->GetMethodID(_cls, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
        static jmethodID _getTitle = JumpListInteropItem::getTitle = env->GetMethodID(_cls, "getTitle", "()Ljava/lang/String;");
        static jmethodID _getArguments = JumpListInteropItem::getArguments = env->GetMethodID(_cls, "getArguments", "()Ljava/lang/String;");
        static jmethodID _getDescription = JumpListInteropItem::getDescription = env->GetMethodID(_cls, "getDescription", "()Ljava/lang/String;");
        static jmethodID _getIconPath = JumpListInteropItem::getIconPath = env->GetMethodID(_cls, "getIconPath", "()Ljava/lang/String;");
        static jmethodID _getIconNum = JumpListInteropItem::getIconNum = env->GetMethodID(_cls, "getIconNum", "()I");
    }
}

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

void createShellLink(
    JNIEnv* env,
    const std::wstring& title,
    const std::wstring& arguments,
    const std::optional<std::wstring>& description,
    const std::optional<std::pair<std::wstring, int>>& icon,
    IShellLinkW **ppsl)
{
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
    THROW_IF_FAILED(psl->SetArguments(arguments.c_str()), "Failed to set shell link's arguments.",);

    if (description.has_value()) {
        THROW_IF_FAILED(psl->SetDescription(description.value().c_str()), "Failed to set shell link's description.",);
    }

    if (icon.has_value()) {
        const auto& [iconPath, iconNum] = icon.value();
        THROW_IF_FAILED(psl->SetIconLocation(iconPath.c_str(), iconNum), "Failed to set shell link's icon.",);
    }

    ComPtr<IPropertyStore> pps;
    THROW_IF_FAILED(psl->QueryInterface(IID_PPV_ARGS(&pps)), "Failed to cast shell link to IPropertyStore.",);

    PropVariantWrapper propvar(title.c_str());
    THROW_IF_FAILED(propvar, "Failed to create a PropVariant.",);

    THROW_IF_FAILED(pps->SetValue(PKEY_Title, propvar), "Failed to set shell link's title.",);
    THROW_IF_FAILED(pps->Commit(), "Failed to commit shell link's title.",);

    THROW_IF_FAILED(psl->QueryInterface(IID_PPV_ARGS(ppsl)), "Failed to return the shell link.",);
}

void createShellLinkFromInteropObject(JNIEnv* env, jobject obj, IShellLinkW **ppsl) {
    org::jetbrains::skiko::windows::ensure(env);

    std::wstring title = toStdString(env, (jstring) env->CallObjectMethod(obj, org::jetbrains::skiko::windows::JumpListInteropItem::getTitle));
    std::wstring arguments = toStdString(env, (jstring) env->CallObjectMethod(obj, org::jetbrains::skiko::windows::JumpListInteropItem::getArguments));

    jstring jDescription = (jstring) env->CallObjectMethod(obj, org::jetbrains::skiko::windows::JumpListInteropItem::getDescription);
    jstring jIconPath = (jstring) env->CallObjectMethod(obj, org::jetbrains::skiko::windows::JumpListInteropItem::getIconPath);
    int jIconNum = (int) env->CallIntMethod(obj, org::jetbrains::skiko::windows::JumpListInteropItem::getIconNum);

    std::optional<std::wstring> description =
        env->IsSameObject(jDescription, NULL) ? std::nullopt : std::make_optional(toStdString(env, jDescription));
    std::optional<std::pair<std::wstring, int>> icon =
        env->IsSameObject(jIconPath, NULL) ? std::nullopt : std::make_optional(std::make_pair(toStdString(env, jIconPath), jIconNum));

    return createShellLink(env, title, arguments, description, icon, ppsl);
}

jobject createJumpListInteropItemObject(JNIEnv* env, IShellLinkW *psl)
{
    org::jetbrains::skiko::windows::ensure(env);

    ComPtr<IPropertyStore> propStore;
    if (SUCCEEDED(psl->QueryInterface(IID_PPV_ARGS(&propStore)))) {
        PROPVARIANT propvar;
        if (SUCCEEDED(propStore->GetValue(PKEY_Title, &propvar))) {
            PropVariantWrapper propvarWrapper(propvar);
            WCHAR szTitle[512];
            if (SUCCEEDED(PropVariantToString(propvar, szTitle, ARRAYSIZE(szTitle)))) {
                WCHAR szArguments[1024];
                if (SUCCEEDED(psl->GetArguments(szArguments, ARRAYSIZE(szArguments)))) {
                    WCHAR szIconPath[512];
                    int numIcon;
                    if (SUCCEEDED(psl->GetIconLocation(szIconPath, ARRAYSIZE(szIconPath), &numIcon))) {
                        WCHAR szDescription[1024];
                        if (SUCCEEDED(psl->GetDescription(szDescription, ARRAYSIZE(szDescription)))) {
                            return env->NewObject(
                                org::jetbrains::skiko::windows::JumpListInteropItem::cls,
                                org::jetbrains::skiko::windows::JumpListInteropItem::init,
                                env->NewString(reinterpret_cast<const jchar*>(szTitle), wcsnlen_s(szTitle, ARRAYSIZE(szTitle))),
                                env->NewString(reinterpret_cast<const jchar*>(szArguments), wcsnlen_s(szArguments, ARRAYSIZE(szArguments))),
                                env->NewString(reinterpret_cast<const jchar*>(szDescription), wcsnlen_s(szDescription, ARRAYSIZE(szDescription))),
                                env->NewString(reinterpret_cast<const jchar*>(szIconPath), wcsnlen_s(szIconPath, ARRAYSIZE(szIconPath))),
                                numIcon
                            );
                        }
                    }
                }
            }
        }
    }

    return NULL;
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
        JNIEnv* env, jobject obj, jlong ptr, jobject task)
    {
        CoInitializeWrapper initialize(COINIT_MULTITHREADED);
        THROW_IF_FAILED(initialize, "Failed to initialize COM apartment.",);

        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJavaRuntimeExceptionByErrorCodeWithContext(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return;
        }

        ComPtr<IObjectCollection> poc;
        THROW_IF_FAILED(
            CoCreateInstance(CLSID_EnumerableObjectCollection, NULL, CLSCTX_INPROC, IID_PPV_ARGS(&poc)),
            "Failed to create an instance of EnumerableObjectCollection.",
        );

        ComPtr<IShellLinkW> psl;
        createShellLinkFromInteropObject(env, task, &psl);

        THROW_IF_FAILED(poc->AddObject(psl.Get()), "Failed to add a shell link to the collection.",);

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
            jobject obj = env->GetObjectArrayElement(itemsArray, i);

            ComPtr<IShellLinkW> psl;
            createShellLinkFromInteropObject(env, obj, &psl);

            THROW_IF_FAILED(poc->AddObject(psl.Get()), "Failed to add a shell link to the collection.",);
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

        org::jetbrains::skiko::windows::ensure(env);
        jobjectArray itemsArray = env->NewObjectArray(cItems, org::jetbrains::skiko::windows::JumpListInteropItem::cls, NULL);

        for (UINT i = 0; i < cItems; i++) {
            ComPtr<IShellLinkW> pslRemoved;
            if (SUCCEEDED(poaRemoved->GetAt(i, IID_PPV_ARGS(&pslRemoved)))) {
                jobject interopItem = createJumpListInteropItemObject(env, pslRemoved.Get());
                if (interopItem != NULL) {
                    env->SetObjectArrayElement(itemsArray, i, interopItem);
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
