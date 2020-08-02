#include <jawt.h>
#include <jawt_md.h>
#include <stdlib.h>

#if SK_BUILD_FOR_WIN
#else
#include <dlfcn.h>
#endif

typedef jboolean (*JAWT_GetAWT_t)(JNIEnv*, JAWT*);

extern "C" jboolean Skiko_GetAWT(JNIEnv* env, JAWT* awt) {
#if SK_BUILD_FOR_WIN
    fprintf(stderr, "Not implemented yet!");
    abort();
    return JNI_FALSE;
#else
    static JAWT_GetAWT_t func = nullptr;
    if (!func) {
        char* jdkHome = getenv("JAVA_HOME");
        if (!jdkHome) {
            fprintf(stderr, "No JAVA_HOME defined!");
            abort();
        }
        char path[FILENAME_MAX];
        snprintf(path, sizeof(path), "%s/lib/libjawt%s", jdkHome,
#if SK_BUILD_FOR_MAC
         ".dylib"
#else
         ".so"
#endif
        );
        void* lib = dlopen(path, RTLD_LAZY | RTLD_GLOBAL);
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
    return func(env, awt);
#endif
}