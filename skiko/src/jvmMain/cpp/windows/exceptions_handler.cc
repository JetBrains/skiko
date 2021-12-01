#if SK_BUILD_FOR_WIN

#include "exceptions_handler.h"
#include "../common/interop.hh"

bool isHandleException(JNIEnv *env)
{
    jstring propertyName = env->NewStringUTF("skiko.win.exception.handler.enabled");
    jstring propertyString = (jstring)env->CallStaticObjectMethod(java::lang::System::cls, java::lang::System::getProperty, propertyName);
    if (propertyString == 0)
    {
        return false;
    }
    const char *property = env->GetStringUTFChars(propertyString, 0);
    bool result = !strncmp(property, "true", 4);
    env->ReleaseStringUTFChars(propertyString, property);
    return result;
}

const char *getDescription(DWORD code)
{
    switch (code)
    {
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
    default:
        return "UNKNOWN EXCEPTION";
    }
}

void logJavaException(JNIEnv *env, const char *function, DWORD sehCode)
{
    if (isHandleException(env))
    {
        char buffer[200];
        int result = snprintf(
            buffer, 200, "Native exception in [%s]:\nSEH description: %s\n", function, getDescription(sehCode));
        jclass logClass = env->FindClass("org/jetbrains/skiko/RenderExceptionsHandler");
        jmethodID logMethod = env->GetStaticMethodID(logClass, "logAndThrow", "(Ljava/lang/String;)V");
        env->CallStaticVoidMethod(logClass, logMethod, env->NewStringUTF(buffer));
    }
}

#endif