#ifdef SK_ANGLE

#include <GLES/gl.h>
#include <EGL/egl.h>
#include <jni.h>

extern "C" {
    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_AngleApi_glGetString(JNIEnv *env, jobject object, jint value)
    {
        static auto glGetString = (PFNGLGETSTRINGPROC) eglGetProcAddress("glGetString");
        const char *content = reinterpret_cast<const char *>(glGetString(value));
        return env->NewStringUTF(content);
    }
}

#endif
