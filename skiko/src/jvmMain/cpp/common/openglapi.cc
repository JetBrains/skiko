#if SK_BUILD_FOR_WIN
#include <SDKDDKVer.h>
#include <windows.h>
#endif
#if SK_BUILD_FOR_MAC
#import <OpenGL/gl3.h>
#elif SK_BUILD_FOR_ANDROID
#include <GLES/gl.h>
#elif SK_BUILD_FOR_IOS
// iOS uses Metal
#else
#include <GL/gl.h>
#endif
#include <jni.h>
#include <string>
#include <vector>

extern "C" {

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glFinish(JNIEnv * env, jobject object) {
#if !SK_BUILD_FOR_IOS
	glFinish();
#endif
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glFlush(JNIEnv * env, jobject object) {
#if !SK_BUILD_FOR_IOS
	glFlush();
#endif
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_OpenGLApi_glGetIntegerv(JNIEnv * env, jobject object, jint pname) {
#if !SK_BUILD_FOR_IOS
	GLint data;
	glGetIntegerv(pname, &data);
	return (jint)data;
#else
    return 0;
#endif
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_OpenGLApi_glGetString(JNIEnv * env, jobject object, jint value) {
#if !SK_BUILD_FOR_IOS
	const char *content = reinterpret_cast<const char *>(glGetString(value));
    return env->NewStringUTF(content);
#else
    return env->NewStringUTF("");
#endif
}

}