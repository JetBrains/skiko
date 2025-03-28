#include "exceptions_handler.h"

const char *getDescription(DWORD code) {
    switch (code) {
    case EXCEPTION_ACCESS_VIOLATION:
        return "EXCEPTION_ACCESS_VIOLATION";
    case EXCEPTION_ARRAY_BOUNDS_EXCEEDED:
        return "EXCEPTION_ARRAY_BOUNDS_EXCEEDED";
    case EXCEPTION_BREAKPOINT:
        return "EXCEPTION_BREAKPOINT";
    case EXCEPTION_DATATYPE_MISALIGNMENT:
        return "EXCEPTION_DATATYPE_MISALIGNMENT";
    case EXCEPTION_FLT_DENORMAL_OPERAND:
        return "EXCEPTION_FLT_DENORMAL_OPERAND";
    case EXCEPTION_FLT_DIVIDE_BY_ZERO:
        return "EXCEPTION_FLT_DIVIDE_BY_ZERO";
    case EXCEPTION_FLT_INEXACT_RESULT:
        return "EXCEPTION_FLT_INEXACT_RESULT";
    case EXCEPTION_FLT_INVALID_OPERATION:
        return "EXCEPTION_FLT_INVALID_OPERATION";
    case EXCEPTION_FLT_OVERFLOW:
        return "EXCEPTION_FLT_OVERFLOW";
    case EXCEPTION_FLT_STACK_CHECK:
        return "EXCEPTION_FLT_STACK_CHECK";
    case EXCEPTION_FLT_UNDERFLOW:
        return "EXCEPTION_FLT_UNDERFLOW";
    case EXCEPTION_ILLEGAL_INSTRUCTION:
        return "EXCEPTION_ILLEGAL_INSTRUCTION";
    case EXCEPTION_IN_PAGE_ERROR:
        return "EXCEPTION_IN_PAGE_ERROR";
    case EXCEPTION_INT_DIVIDE_BY_ZERO:
        return "EXCEPTION_INT_DIVIDE_BY_ZERO";
    case EXCEPTION_INT_OVERFLOW:
        return "EXCEPTION_INT_OVERFLOW";
    case EXCEPTION_INVALID_DISPOSITION:
        return "EXCEPTION_INVALID_DISPOSITION";
    case EXCEPTION_NONCONTINUABLE_EXCEPTION:
        return "EXCEPTION_NONCONTINUABLE_EXCEPTION";
    case EXCEPTION_PRIV_INSTRUCTION:
        return "EXCEPTION_PRIV_INSTRUCTION";
    case EXCEPTION_SINGLE_STEP:
        return "EXCEPTION_SINGLE_STEP";
    case EXCEPTION_STACK_OVERFLOW:
        return "EXCEPTION_STACK_OVERFLOW";
    case ERROR_MOD_NOT_FOUND:
        return "ERROR_MOD_NOT_FOUND";
    default:
        return "UNKNOWN EXCEPTION";
    }
}

void throwJavaRenderExceptionByExceptionCode(JNIEnv *env, const char *function, DWORD code) {
    char fullMsg[200];
    int result = snprintf(fullMsg, sizeof(fullMsg) - 1,
        "Native exception in [%s], code %lu: %s", function, code, getDescription(code));

    static jclass cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/RenderExceptionsHandler"));
    static jmethodID method = env->GetStaticMethodID(cls, "throwException", "(Ljava/lang/String;)V");
    env->CallStaticVoidMethod(cls, method, env->NewStringUTF(fullMsg));
}


void throwJavaRenderExceptionByErrorCode(JNIEnv *env, const char *function, DWORD code) {
    char fullMsg[1024];
    char *msg = 0;
    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
        NULL, code, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR) &msg, 0, NULL);

    int result = snprintf(fullMsg, sizeof(fullMsg) - 1,
        "Native exception in [%s], code %lu: %s", function, code, msg);
    LocalFree(msg);

    static jclass cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/RenderExceptionsHandler"));
    static jmethodID method = env->GetStaticMethodID(cls, "throwException", "(Ljava/lang/String;)V");
    env->CallStaticVoidMethod(cls, method, env->NewStringUTF(fullMsg));
}


void throwJavaRenderExceptionWithMessage(JNIEnv *env, const char *function, const char *msg) {
    char fullMsg[1024];
    snprintf(fullMsg, sizeof(fullMsg) - 1, "Native exception in [%s], message: %s", function, msg);

    static jclass cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/RenderExceptionsHandler"));
    static jmethodID method = env->GetStaticMethodID(cls, "throwException", "(Ljava/lang/String;)V");
    env->CallStaticVoidMethod(cls, method, env->NewStringUTF(fullMsg));
}
