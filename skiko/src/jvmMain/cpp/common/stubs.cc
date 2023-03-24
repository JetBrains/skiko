// This file contains stubs of native methods form platforms where they are not supported
#include <jni.h>

#include <stdio.h>
#include <stdlib.h>

namespace {
void skikoUnimplemented(const char* message) {
    fprintf(stderr, "NOT IMPLEMENTED: %s\n", message);
    abort();
}
}  // namespace

// To ensure we could always link the final binary, i.e. Graal Native Image
// we put here stubs for all OS specific native methods.

#ifndef SK_BUILD_FOR_LINUX
JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_SetupKt_linuxGetSystemDpiScale(JNIEnv *env, jobject layer) {
    skikoUnimplemented("Java_org_jetbrains_skiko_SetupKt_linuxGetSystemDpiScale");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_setSwapInterval(JNIEnv *env, jobject redrawer, jlong displayPtr, jlong windowPtr, jint interval) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_setSwapInterval");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_swapBuffers(JNIEnv *env, jobject redrawer, jlong displayPtr, jlong windowPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_swapBuffers");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_makeCurrent(JNIEnv *env, jobject redrawer, jlong displayPtr, jlong windowPtr, jlong contextPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_makeCurrent");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_createContext(JNIEnv *env, jobject redrawer, jlong displayPtr, jboolean transparency) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_createContext");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_destroyContext(JNIEnv *env, jobject redrawer, jlong displayPtr, jlong contextPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_destroyContext");
}

#endif


#ifndef SK_BUILD_FOR_WIN
JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_getDevice(JNIEnv *env, jobject redrawer, jlong platformInfoPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_getDevice");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_swapBuffers(JNIEnv *env, jobject redrawer, jlong devicePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_WindowsOpenGLRedrawerKt_swapBuffers");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_context_Direct3DContextHandler_flush(
        JNIEnv *env, jobject redrawer, jlong contextPtr, jlong surfacePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_context_Direct3DContextHandler_flush");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_createDirectXDevice(
        JNIEnv *env, jobject redrawer, jint adapterPriority, jlong contentHandle, jboolean transparency) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_createDirectXDevice");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_initSwapChain(
        JNIEnv *env, jobject redrawer, jlong devicePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_initSwapChain");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_initFence(
        JNIEnv *env, jobject redrawer, jlong devicePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_initFence");
}
#endif


#ifndef SK_BUILD_FOR_MAC
JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxIsFullscreenNative(JNIEnv *env, jobject properties, jobject component) {
    skikoUnimplemented("Java_org_jetbrains_skiko_PlatformOperationsKt_osxIsFullscreenNative");
    return false;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxSetFullscreenNative(JNIEnv *env, jobject properties, jobject component, jboolean value) {
    skikoUnimplemented("Java_org_jetbrains_skiko_PlatformOperationsKt_osxSetFullscreenNative");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_osxDisableTitleBar(JNIEnv *env, jobject properties, jobject component, jfloat customHeaderHeight) {
    skikoUnimplemented("Java_org_jetbrains_skiko_PlatformOperationsKt_osxDisableTitleBar");
}

JNIEXPORT void Java_org_jetbrains_skiko_PlatformOperationsKt_osxOrderEmojiAndSymbolsPopup() {
    skikoUnimplemented("Java_org_jetbrains_skiko_PlatformOperationsKt_osxOrderEmojiAndSymbolsPopup");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_makeMetalContext(
    JNIEnv* env, jobject redrawer, jlong devicePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_makeMetalContext");
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_makeMetalRenderTarget(
    JNIEnv * env, jobject redrawer, jlong devicePtr, jint width, jint height) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_makeMetalRenderTarget");
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_startRendering(
    JNIEnv * env, jobject redrawer)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_startRendering");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_endRendering(
    JNIEnv * env, jobject redrawer, jlong handle)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_endRendering");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_createMetalDevice(
    JNIEnv *env, jobject redrawer, jlong windowPtr, jboolean transparency, jint adapterPriority, jlong platformInfoPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_createMetalDevice");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_resizeLayers(
    JNIEnv *env, jobject redrawer, jlong devicePtr, jint x, jint y, jint width, jint height)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_resizeLayers");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setLayerVisible(
    JNIEnv *env, jobject redrawer, jlong devicePtr, jboolean isVisible)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setLayerVisible");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setContentScale(JNIEnv *env, jobject obj, jlong devicePtr, jfloat contentScale)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setContentScale");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setVSyncEnabled(JNIEnv *env, jobject obj, jlong devicePtr, jboolean enabled)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_setVSyncEnabled");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_finishFrame(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_finishFrame");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_disposeDevice(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_disposeDevice");
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_getAdapterName(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_getAdapterName");
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_getAdapterMemorySize(
    JNIEnv *env, jobject redrawer, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_getAdapterMemorySize");
    return 0;
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_NativeApplicationKt_getApplicationWindowCount(JNIEnv *env, jobject obj)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_NativeApplicationKt_getApplicationWindowCount");
    return 0;
}

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_redrawer_MetalRedrawer_isOccluded(JNIEnv *env, jobject redrawer, jlong windowPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_redrawer_MetalRedrawer_isOccluded");
    return false;
}
#endif