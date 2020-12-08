#if SK_BUILD_FOR_WIN
#include <SDKDDKVer.h>
#include <windows.h>
#endif
#if SK_BUILD_FOR_MAC
#import <OpenGL/gl3.h>
#else
#include <GL/gl.h>
#endif
#include <jni.h>

extern "C" {

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_render_OpenGLApi_glViewport(JNIEnv * env, jobject object, jint x, jint y, jint w, jint h) {
	glViewport(x, y, w, h);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_render_OpenGLApi_glClearColor(JNIEnv * env, jobject object, jfloat r, jfloat g, jfloat b, jfloat a) {
	glClearColor(r, g, b, a);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_render_OpenGLApi_glClear(JNIEnv * env, jobject object, jint mask) {
	glClear(mask);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_render_OpenGLApi_glFinish(JNIEnv * env, jobject object) {
	glFinish();
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_render_OpenGLApi_glEnable(JNIEnv * env, jobject object, jint cap) {
	glEnable(cap);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_render_OpenGLApi_glBindTexture(JNIEnv * env, jobject object, jint target, jint texture) {
	glBindTexture(target, texture);
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_render_OpenGLApi_glGetIntegerv(JNIEnv * env, jobject object, jint pname) {
	GLint data;
	glGetIntegerv(pname, &data);
	return (jint)data;
}

}