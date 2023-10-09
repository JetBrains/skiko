#if SK_BUILD_FOR_WIN
#include <SDKDDKVer.h>
#include <windows.h>
#endif
#if SK_BUILD_FOR_MAC
#import <OpenGL/gl3.h>
#elif SK_BUILD_FOR_ANDROID
#include <GLES/gl.h>
#else
#include <GL/gl.h>
#endif
#include <jni.h>
#include <string>
#include <vector>

extern "C" {

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glFinish(JNIEnv * env, jobject object) {
	glFinish();
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_OpenGLApi_glGetIntegerv(JNIEnv * env, jobject object, jint pname) {
	GLint data;
	glGetIntegerv(pname, &data);
	return (jint)data;
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_OpenGLApi_glGetString(JNIEnv * env, jobject object, jint value) {
	const char *content = reinterpret_cast<const char *>(glGetString(value));
    return env->NewStringUTF(content);
}

}