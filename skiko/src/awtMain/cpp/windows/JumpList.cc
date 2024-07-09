#if SK_BUILD_FOR_WIN

#include <windows.h>
#include <wrl/client.h>
#include <shobjidl.h>
#include <propkey.h>
#include <propvarutil.h>

#include <optional>

#include "jni_helpers.h"

template <typename T>
using ComPtr = Microsoft::WRL::ComPtr<T>;

#define THROW_IF_FAILED(action, message, ret)                               \
    do {                                                                    \
        HRESULT hr { action };                                              \
        if (FAILED(hr)) {                                                   \
            throwJumpListException(env, __FUNCTION__, DWORD(hr), message);  \
            return ret;                                                     \
        }                                                                   \
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

/**
 * Convenience wrapper for PROPVARIANTs that need to be cleared.
 */
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

void throwJumpListException(JNIEnv *env, const char * function, DWORD code, const char * context) {
    char fullMsg[1024];
    char *msg = 0;
    FormatMessage(
        FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS | FORMAT_MESSAGE_MAX_WIDTH_MASK,
        NULL, code, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR) &msg, 0, NULL
    );
    int result = snprintf(fullMsg, sizeof(fullMsg) - 1,
        "Native exception in [%s], code %lu: %s\nContext: %s", function, code, msg, context);
    LocalFree(msg);

    static jclass cls = static_cast<jclass>(env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/windows/JumpListException")));
    static jmethodID init = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");

    jthrowable throwable = (jthrowable) env->NewObject(cls, init, env->NewStringUTF(fullMsg), code);
    env->Throw(throwable);
}

/**
 * Creates a CLSID_ShellLink to insert into the Jump List.
 */
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

    // Use current executable for the shell link. Can contain a verbatim (\\?\) path so MAX_PATH might not be sufficient.
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

    // The title property is required on Jump List items provided as an IShellLink instance.
    // This value is used as the display name in the Jump List.
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

bool createObjectArray(JNIEnv* env, jobjectArray jobjArray, IObjectArray **ppoa) {
    jsize jobjArraySize = env->GetArrayLength(jobjArray);
    if (0 == jobjArraySize) {
        return false;
    }

    ComPtr<IObjectCollection> poc;
    THROW_IF_FAILED(
        CoCreateInstance(CLSID_EnumerableObjectCollection, NULL, CLSCTX_INPROC, IID_PPV_ARGS(&poc)),
        "Failed to create an instance of EnumerableObjectCollection.", false
    );

    for (jsize i = 0; i < jobjArraySize; i++) {
        jobject obj = env->GetObjectArrayElement(jobjArray, i);

        ComPtr<IShellLinkW> psl;
        createShellLinkFromInteropObject(env, obj, &psl);

        THROW_IF_FAILED(poc->AddObject(psl.Get()), "Failed to add a shell link to the collection.", false);
    }

    THROW_IF_FAILED(poc->QueryInterface(IID_PPV_ARGS(ppoa)), "Failed to create an object array.", false);

    return true;
}

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1init(JNIEnv* env, jobject obj) {
        THROW_IF_FAILED(CoInitializeEx(NULL, COINIT_MULTITHREADED), "Failed to initialize COM apartment.", 0L);

        ComPtr<ICustomDestinationList> pcdl;
        THROW_IF_FAILED(
            CoCreateInstance(CLSID_DestinationList, NULL, CLSCTX_INPROC_SERVER, IID_PPV_ARGS(&pcdl)),
            "Failed to create CLSID_DestinationList.", 0L
        );

        return toJavaPointer(pcdl.Detach());
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1dispose(
        JNIEnv* env, jobject obj, jlong ptr)
    {
        ICustomDestinationList* pcdl = fromJavaPointer<ICustomDestinationList*>(ptr);
        pcdl->Release();
        CoUninitialize();
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1setAppID(JNIEnv *env, jobject obj, jlong ptr, jstring appID) {
        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJumpListException(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return;
        }

        std::wstring strAppID = toStdString(env, appID);

        THROW_IF_FAILED(pcdl->SetAppID(strAppID.c_str()), "Failed to set AppUserModelID for the Jump List.",);
    }

    JNIEXPORT jobjectArray JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1beginList(JNIEnv *env, jobject obj, jlong ptr) {
        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJumpListException(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return NULL;
        }

        UINT uMaxSlots;
        ComPtr<IObjectArray> poaRemoved;
        THROW_IF_FAILED(
            pcdl->BeginList(&uMaxSlots, IID_PPV_ARGS(&poaRemoved)),
            "Failed to BeginList.", NULL
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

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1addUserTasks(
        JNIEnv* env, jobject obj, jlong ptr, jobjectArray tasks)
    {
        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJumpListException(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return;
        }

        ComPtr<IObjectCollection> poc;
        THROW_IF_FAILED(
            CoCreateInstance(CLSID_EnumerableObjectCollection, NULL, CLSCTX_INPROC, IID_PPV_ARGS(&poc)),
            "Failed to create an instance of EnumerableObjectCollection.",
        );

        ComPtr<IObjectArray> poa;
        if (!createObjectArray(env, tasks, &poa)) {
            return;
        }

        THROW_IF_FAILED(pcdl->AddUserTasks(poa.Get()), "Failed to add user tasks.",);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1addCategory(
        JNIEnv* env, jobject obj, jlong ptr, jstring category, jobjectArray itemsArray)
    {
        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJumpListException(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return;
        }

        ComPtr<IObjectArray> poa;
        if (!createObjectArray(env, itemsArray, &poa)) {
            return;
        }

        std::wstring strCategory = toStdString(env, category);

        // Items listed in the removed list may not be re-added to the Jump List during this
        // list-building transaction.  They should not be re-added to the Jump List until
        // the user has used the item again.  The AppendCategory call will fail if
        // an attempt to add an item in the removed list is made.
        THROW_IF_FAILED(pcdl->AppendCategory(strCategory.c_str(), poa.Get()), "Failed to append category.",);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1commit(
        JNIEnv* env, jobject obj, jlong ptr)
    {
        ComPtr<ICustomDestinationList> pcdl { fromJavaPointer<ICustomDestinationList*>(ptr) };
        if (pcdl.Get() == NULL) {
            throwJumpListException(env, __FUNCTION__, E_POINTER, "Native pointer is null.");
            return;
        }

        THROW_IF_FAILED(pcdl->CommitList(), "Failed to commit the Jump List.",);
    }
}

#endif
