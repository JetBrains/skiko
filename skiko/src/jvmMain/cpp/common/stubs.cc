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

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_setSwapInterval(JNIEnv *env, jobject obj, jlong displayPtr, jlong windowPtr, jint interval) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_setSwapInterval");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_swapBuffers(JNIEnv *env, jobject obj, jlong displayPtr, jlong windowPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_swapBuffers");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_makeCurrent(JNIEnv *env, jobject obj, jlong displayPtr, jlong windowPtr, jlong contextPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_makeCurrent");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_createContext(JNIEnv *env, jobject obj, jlong displayPtr, jboolean transparency) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_createContext");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_destroyContext(JNIEnv *env, jobject obj, jlong displayPtr, jlong contextPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_LinuxOpenGLRendererKt_destroyContext");
}

#endif


#ifndef SK_BUILD_FOR_WIN
JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_WindowsOpenGLRendererKt_getDevice(JNIEnv *env, jobject obj, jlong platformInfoPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_WindowsOpenGLRendererKt_getDevice");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_WindowsOpenGLRendererKt_swapBuffers(JNIEnv *env, jobject obj, jlong devicePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_WindowsOpenGLRendererKt_swapBuffers");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_Direct3DRenderer_flush(
        JNIEnv *env, jobject obj, jlong contextPtr, jlong surfacePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_Direct3DRenderer_flush");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_Direct3DRenderer_chooseAdapter(JNIEnv *env, jobject obj, jint adapterPriority) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_Direct3DRenderer_chooseAdapter");
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_Direct3DRenderer_createDirectXDevice(
        JNIEnv *env, jobject obj, jint adapterPriority, jlong contentHandle, jboolean transparency) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_Direct3DRenderer_createDirectXDevice");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_Direct3DRenderer_initSwapChain(
        JNIEnv *env, jobject obj, jlong devicePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_Direct3DRenderer_initSwapChain");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_Direct3DRenderer_initFence(
        JNIEnv *env, jobject obj, jlong devicePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_Direct3DRenderer_initFence");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1init(JNIEnv *env, jobject obj) {
    skikoUnimplemented("Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1init");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1dispose(JNIEnv *env, jobject obj, jlong ptr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1dispose");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1setAppID(JNIEnv *env, jobject obj, jlong ptr, jstring appID) {
    skikoUnimplemented("Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1setAppID");
}

JNIEXPORT jobjectArray JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1beginList(JNIEnv *env, jobject obj, jlong ptr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1beginList");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1addUserTasks(
        JNIEnv *env, jobject obj, jlong ptr, jobjectArray tasks) {
    skikoUnimplemented("Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1addUserTasks");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1addCategory(
        JNIEnv *env, jobject obj, jlong ptr, jstring category, jobjectArray itemsArray) {
    skikoUnimplemented("Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1addCategory");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1commit(JNIEnv *env, jobject obj, jlong ptr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_windows_JumpListBuilder_jumpList_1commit");
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

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_makeMetalContext(
    JNIEnv* env, jobject obj, jlong devicePtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_makeMetalContext");
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_makeMetalRenderTarget(
    JNIEnv * env, jobject obj, jlong devicePtr, jint width, jint height) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_makeMetalRenderTarget");
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_startRendering(
    JNIEnv * env, jobject obj)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_startRendering");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_endRendering(
    JNIEnv * env, jobject obj, jlong handle)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_endRendering");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_createMetalDevice(
    JNIEnv *env, jobject obj, jlong windowPtr, jboolean transparency, jint adapterPriority, jlong platformInfoPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_createMetalDevice");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_resizeLayers(
    JNIEnv *env, jobject obj, jlong devicePtr, jint x, jint y, jint width, jint height)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_resizeLayers");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_setLayerVisible(
    JNIEnv *env, jobject obj, jlong devicePtr, jboolean isVisible)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_setLayerVisible");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_setContentScale(JNIEnv *env, jobject obj, jlong devicePtr, jfloat contentScale)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_setContentScale");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_setVSyncEnabled(JNIEnv *env, jobject obj, jlong devicePtr, jboolean enabled)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_setVSyncEnabled");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_finishFrameAsync(
    JNIEnv *env, jobject obj, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_finishFrameAsync");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_disposeDevice(
    JNIEnv *env, jobject obj, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_disposeDevice");
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_getAdapterName(
    JNIEnv *env, jobject obj, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_getAdapterName");
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_getAdapterMemorySize(
    JNIEnv *env, jobject obj, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_getAdapterMemorySize");
    return 0;
}

JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_NativeApplicationKt_getApplicationWindowCount(JNIEnv *env, jobject obj)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_NativeApplicationKt_getApplicationWindowCount");
    return 0;
}

JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_renderer_MetalRenderer_isOccluded(JNIEnv *env, jobject obj, jlong windowPtr) {
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_MetalRenderer_isOccluded");
    return false;
}
#endif


#ifndef SK_ANGLE
JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_AngleRendererKt_createAngleDevice(
    JNIEnv *env, jobject obj, jlong platformInfoPtr, jboolean transparency)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_AngleRendererKt_createAngleDevice");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_AngleRendererKt_makeCurrent(
    JNIEnv *env, jobject obj, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_AngleRendererKt_makeCurrent");
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_AngleRendererKt_makeAngleContext(
    JNIEnv *env, jobject obj, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_AngleRendererKt_makeAngleContext");
    return 0;
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_renderer_AngleRendererKt_makeAngleRenderTarget(
    JNIEnv *env, jobject obj, jlong devicePtr, jint width, jint height)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_AngleRendererKt_makeAngleRenderTarget");
    return 0;
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_AngleRendererKt_swapBuffers(
    JNIEnv *env, jobject obj, jlong devicePtr, jboolean waitForVsync)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_AngleRendererKt_swapBuffers");
}

JNIEXPORT void JNICALL Java_org_jetbrains_skiko_renderer_AngleRendererKt_disposeDevice(
    JNIEnv *env, jobject obj, jlong devicePtr)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_renderer_AngleRendererKt_disposeDevice");
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_AngleApi_glGetString(
    JNIEnv *env, jobject object, jint value)
{
    skikoUnimplemented("Java_org_jetbrains_skiko_AngleApi_glGetString");
    return 0;
}
#endif
