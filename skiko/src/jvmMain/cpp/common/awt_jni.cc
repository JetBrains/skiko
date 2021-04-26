#include <cstdint>
#include <jawt_md.h>
#include "jni_helpers.h"

extern "C" jboolean Skiko_GetAWT(JNIEnv *env, JAWT *awt);

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getAWT(JNIEnv *env, jobject obj)
    {
        JAWT *awt = new JAWT();
        awt->version = (jint)JAWT_VERSION_9;
        jboolean result = Skiko_GetAWT(env, awt);

        if (result == JNI_FALSE)
        {
            return 0;
        }
        else
        {
            return toJavaPointer(awt);
        }
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getDrawingSurface(JNIEnv *env, jobject obj, jlong awtPtr, jobject layer)
    {
        JAWT *awt = fromJavaPointer<JAWT *>(awtPtr);
        JAWT_DrawingSurface *ds = awt->GetDrawingSurface(env, layer);
        return toJavaPointer(ds);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_freeDrawingSurface(JNIEnv *env, jobject obj, jlong awtPtr, jlong drawingSurfacePtr)
    {
        JAWT *awt = fromJavaPointer<JAWT *>(awtPtr);
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        awt->FreeDrawingSurface(ds);
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_AWTKt_lockDrawingSurface(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        return (ds->Lock(ds) & JAWT_LOCK_ERROR) != 0;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_unlockDrawingSurface(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        ds->Unlock(ds);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getDrawingSurfaceInfo(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        JAWT_DrawingSurfaceInfo *dsi = ds->GetDrawingSurfaceInfo(ds);
        return toJavaPointer(dsi);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_freeDrawingSurfaceInfo(JNIEnv *env, jobject obj, jlong drawingSurfacePtr, jlong drawingSurfaceInfoPtr)
    {
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        JAWT_DrawingSurfaceInfo *dsi = fromJavaPointer<JAWT_DrawingSurfaceInfo *>(drawingSurfaceInfoPtr);
        ds->FreeDrawingSurfaceInfo(dsi);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getPlatformInfo(JNIEnv *env, jobject obj, jlong drawingSurfaceInfoPtr)
    {
        JAWT_DrawingSurfaceInfo *dsi = fromJavaPointer<JAWT_DrawingSurfaceInfo *>(drawingSurfaceInfoPtr);
        return toJavaPointer(dsi->platformInfo);
    }
}