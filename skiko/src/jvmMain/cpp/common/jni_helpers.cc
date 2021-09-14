#include "jni_helpers.h"

JavaVM *jvm = NULL;

std::string handleException(std::string function)
{
    std::exception_ptr eptr = std::current_exception();
    if (!eptr)
    {
        throw std::bad_exception();
    }
    std::ostringstream oss;
    oss << "Native exception in [" << function << "]" << std::endl;
    try
    {
        std::rethrow_exception(eptr);
    }
    catch (const std::exception &e)
    {
        oss << e.what() << std::endl;
        return oss.str();
    }
    catch (const std::string &e)
    {
        oss << e << std::endl;
        return oss.str();
    }
    catch (const char *e)
    {
        oss << e << std::endl;
        return oss.str();
    }
    catch (...)
    {
        oss << "Unknown exception - no stack trace" << std::endl;
        return oss.str();
    }
}

void logJavaException(JNIEnv *env, std::string message)
{
    if (jvm == NULL)
    {
        env->GetJavaVM(&jvm);
    }

    static jclass logClass = NULL;
    if (!logClass)
    {
        logClass = env->FindClass("org/jetbrains/skiko/RenderExceptionsHandler");
    }

    static jmethodID logMethod = NULL;
    if (!logMethod)
    {
        logMethod = env->GetStaticMethodID(logClass, "logAndThrow", "(Ljava/lang/String;)V");
    }

    env->CallStaticVoidMethod(logClass, logMethod, env->NewStringUTF(message.c_str()));
}