// openglapi.cpp : Defines the exported functions for the DLL application.
//

#include "stdafx.h"
#include "openglapi.h"
#include <gl\GL.h>

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glViewport(JNIEnv * env, jobject object, jint x, jint y, jint w, jint h) {
	glViewport(x, y, w, h);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glClearColor(JNIEnv * env, jobject object, jfloat r, jfloat g, jfloat b, jfloat a) {
	glClearColor(r, g, b, a);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glClear(JNIEnv * env, jobject object, jint mask) {
	glClear(mask);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glFinish(JNIEnv * env, jobject object) {
	glFinish();
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glEnable(JNIEnv * env, jobject object, jint cap) {
	glEnable(cap);
}

JNIEXPORT void JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glBindTexture(JNIEnv * env, jobject object, jint target, jint texture) {
	glBindTexture(target, texture);
}

JNIEXPORT jint JNICALL Java_org_jetbrains_awthrl_DriverApi_OpenGLApi_glGetIntegerv(JNIEnv * env, jobject object, jint pname) {
	GLint data;
	glGetIntegerv(pname, &data);
	return (jint)data;
}

