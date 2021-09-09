#include "jni_helpers.h"

std::string handleException(std::string function) {
    std::exception_ptr &eptr = std::current_exception();
    if (!eptr) {
        throw std::bad_exception();
    }
    try {
        std::rethrow_exception(eptr);
    }
    catch (const std::exception &e) {
        return e.what();
    }
    catch (const std::string &e) {
        return e;
    }
    catch (const char *e) {
        return e;
    }
    catch (...) {
        std::ostringstream oss;
        oss << "Unknown exception in [" << function << "]" << std::endl;
        return oss.str();
    }
}

void throwJavaException(JNIEnv *env, std::string message) {
    jclass exClass;
    char *className = "java/lang/RuntimeException" ;
    exClass = env->FindClass(className);
    if ( exClass == NULL ) {
        env->ThrowNew(exClass, message.c_str());
    }
    env->ThrowNew(exClass, message.c_str());
}