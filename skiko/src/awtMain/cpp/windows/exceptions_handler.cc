#include "exceptions_handler.h"
#include "../common/interop.hh"

void throwJavaRenderException(JNIEnv *env, const char *function, DWORD code) {
    char fullMsg[1024];
    char *msg = 0;
    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
        NULL, code, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR) &msg, 0, NULL);
    int result = snprintf(fullMsg, sizeof(fullMsg) - 1, "Native exception in [%s], code %lu: %s", function, code, msg);
    LocalFree(msg);

    static jclass cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/RenderExceptionsHandler"));
    static jmethodID method = env->GetStaticMethodID(cls, "throwException", "(Ljava/lang/String;)V");
    env->CallStaticVoidMethod(cls, method, env->NewStringUTF(fullMsg));
}
