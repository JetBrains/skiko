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
#include <string>
#include <vector>

const std::vector<std::string> vendorBlacklist{ "VMware, Inc." };
const std::vector<std::string> adapterBlacklist{
    "llvmpipe (LLVM 5.0, 256 bits)",
    "llvmpipe (LLVM 11.0.1, 256 bits)",
    "Intel(R) HD Graphics 2000",
    "Intel(R) HD Graphics 3000"
};

extern "C" {

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_OpenGLApi_isCurrentAdapterBlacklisted(JNIEnv * env, jobject object) {
	std::string currentVendorName = std::string(reinterpret_cast<const char*>(glGetString(GL_VENDOR)));
	for (std::string name : vendorBlacklist)
    {
        if (currentVendorName == name)
        {
            fprintf(stderr, "Vendor: %s is not supported.\n", name.c_str());
            return true;
        }
    }
	std::string currentAdapterName = std::string(reinterpret_cast<const char*>(glGetString(GL_RENDERER)));
	for (std::string name : adapterBlacklist)
    {
        if (currentAdapterName == name)
        {
            fprintf(stderr, "Graphics card: %s is blacklisted.\n", name.c_str());
            return true;
        }
    }
    return false;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glViewport(JNIEnv * env, jobject object, jint x, jint y, jint w, jint h) {
	glViewport(x, y, w, h);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glClearColor(JNIEnv * env, jobject object, jfloat r, jfloat g, jfloat b, jfloat a) {
	glClearColor(r, g, b, a);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glClear(JNIEnv * env, jobject object, jint mask) {
	glClear(mask);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glFinish(JNIEnv * env, jobject object) {
	glFinish();
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glEnable(JNIEnv * env, jobject object, jint cap) {
	glEnable(cap);
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_OpenGLApi_glBindTexture(JNIEnv * env, jobject object, jint target, jint texture) {
	glBindTexture(target, texture);
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_OpenGLApi_glGetIntegerv(JNIEnv * env, jobject object, jint pname) {
	GLint data;
	glGetIntegerv(pname, &data);
	return (jint)data;
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_OpenGLApi_glGetString(JNIEnv * env, jobject object, jint value) {
	const char *content = reinterpret_cast<const char *>(glGetString(value));
    jstring result = env->NewStringUTF(content);
    return result;
}

}