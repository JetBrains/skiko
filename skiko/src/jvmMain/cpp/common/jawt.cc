#ifndef SK_BUILD_FOR_ANDROID
#include <jawt.h>
#include <jawt_md.h>

#include <stdlib.h>

#if SK_BUILD_FOR_WIN
#include <windows.h>
#include <strsafe.h>
#else
#include <dlfcn.h>
#endif

typedef jboolean (*JAWT_GetAWT_t)(JNIEnv *, JAWT *);

#if 0
bool check(JNIEnv* env, const char* msg) {
    if (env->ExceptionCheck()) {
        jthrowable exception = env->ExceptionOccurred();
        jclass throwableClass = env->FindClass("java/lang/Throwable");
        jmethodID toStringMethod = env->GetMethodID(
                throwableClass, "toString", "()Ljava/lang/String;");
        jstring messageObject = (jstring) env->CallObjectMethod(exception, toStringMethod);
        const char* messageString = env->GetStringUTFChars(messageObject, 0);
        fprintf(stderr, "Java exception: %s\n", messageString);
        env->ReleaseStringUTFChars(messageObject, messageString);
        jmethodID printStackTraceMethod = env->GetMethodID(throwableClass, "printStackTrace", "()V");
        env->CallVoidMethod(exception, printStackTraceMethod);
        env->ExceptionClear();
    }
}
#endif

#if SK_BUILD_FOR_WIN
void findJdkHome(JNIEnv* env, wchar_t* path, int pathLength) {
#else
void findJdkHome(JNIEnv* env, char* path, int pathLength) {
#endif
    jclass systemClass = env->FindClass("java/lang/System");
    jmethodID getPropertyMethod = env->GetStaticMethodID(
        systemClass,
        "getProperty",
        "(Ljava/lang/String;)Ljava/lang/String;");
    jstring javaHomeString = env->NewStringUTF("java.home");
    jstring propertyString = (jstring) env->CallStaticObjectMethod(systemClass, getPropertyMethod, javaHomeString);
#if SK_BUILD_FOR_WIN
    const jchar* propertyChars = env->GetStringChars(propertyString, 0);
    StringCchPrintfW(path, pathLength, L"%ls", propertyChars);
    env->ReleaseStringChars(propertyString, propertyChars);
#else
    const char* propertyChars = env->GetStringUTFChars(propertyString, 0);
    snprintf(path, pathLength, "%s", propertyChars);
    env->ReleaseStringUTFChars(propertyString, propertyChars);
#endif
}

extern "C" jboolean Skiko_GetAWT(JNIEnv *env, JAWT *awt) {
  static JAWT_GetAWT_t func = nullptr;
#if SK_BUILD_FOR_WIN
  if (!func) {
    wchar_t jdkHome[FILENAME_MAX];
    findJdkHome(env, jdkHome, sizeof(jdkHome));
    wchar_t path[FILENAME_MAX];
    StringCchPrintfW(path, sizeof(path), L"%ls\\bin\\jawt.dll", jdkHome);
    HMODULE lib = LoadLibraryW(path);
    if (!lib) {
      fprintf(stderr, "Cannot open %ls\n", path);
      abort();
    }
    func = reinterpret_cast<JAWT_GetAWT_t>(GetProcAddress(lib, "JAWT_GetAWT"));
    if (!func) {
      fprintf(stderr, "Cannot find JAWT_GetAWT in %ls\n", path);
      abort();
    }
  }
#else
    if (!func) {
        char jdkHome[FILENAME_MAX];
        findJdkHome(env, jdkHome, sizeof(jdkHome));
        char path[FILENAME_MAX];
        snprintf(path, sizeof(path), "%s/lib/libjawt%s", jdkHome,
#if SK_BUILD_FOR_MAC
             ".dylib"
#else
             ".so"
#endif
        );
        void *lib = dlopen(path, RTLD_LAZY | RTLD_GLOBAL);
        if (!lib) {
            fprintf(stderr, "Cannot open %s: %s\n", path, dlerror());
            abort();
        }
        func = reinterpret_cast<JAWT_GetAWT_t>(dlsym(lib, "JAWT_GetAWT"));
        if (!func) {
            fprintf(stderr, "Cannot find JAWT_GetAWT in %s\n", path);
            abort();
        }
    }
#endif
    return func(env, awt);
}

#endif