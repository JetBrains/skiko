#include <cstdint>
#include <jni.h>
#ifdef SK_BUILD_FOR_ANDROID
#include <stdlib.h>
#else
#include <jawt_md.h>
#include "jni_helpers.h"
#endif

#ifndef SK_BUILD_FOR_ANDROID
extern "C" jboolean Skiko_GetAWT(JNIEnv *env, JAWT *awt);
#endif
extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getAWT(JNIEnv *env, jobject obj)
    {
    #ifdef SK_BUILD_FOR_ANDROID
        abort();
        return 0;
    #else
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
    #endif
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getDrawingSurface(JNIEnv *env, jobject obj, jlong awtPtr, jobject layer)
    {
    #ifdef SK_BUILD_FOR_ANDROID
        abort();
        return 0;
    #else
        JAWT *awt = fromJavaPointer<JAWT *>(awtPtr);
        JAWT_DrawingSurface *ds = awt->GetDrawingSurface(env, layer);
        return toJavaPointer(ds);
    #endif
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_freeDrawingSurface(JNIEnv *env, jobject obj, jlong awtPtr, jlong drawingSurfacePtr)
    {
    #ifdef SK_BUILD_FOR_ANDROID
        abort();
        return;
    #else
        JAWT *awt = fromJavaPointer<JAWT *>(awtPtr);
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        awt->FreeDrawingSurface(ds);
    #endif
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_AWTKt_lockDrawingSurface(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
    #ifdef SK_BUILD_FOR_ANDROID
         abort();
         return 0;
    #else
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        return (ds->Lock(ds) & JAWT_LOCK_ERROR) != 0;
    #endif
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_unlockDrawingSurface(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
    #ifdef SK_BUILD_FOR_ANDROID
        abort();
        return;
    #else
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        ds->Unlock(ds);
    #endif
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getDrawingSurfaceInfo(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
     #ifdef SK_BUILD_FOR_ANDROID
         abort();
         return 0;
     #else
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        JAWT_DrawingSurfaceInfo *dsi = ds->GetDrawingSurfaceInfo(ds);
        return toJavaPointer(dsi);
     #endif
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_freeDrawingSurfaceInfo(JNIEnv *env, jobject obj, jlong drawingSurfacePtr, jlong drawingSurfaceInfoPtr)
    {
    #ifdef SK_BUILD_FOR_ANDROID
         abort();
         return;
    #else
        JAWT_DrawingSurface *ds = fromJavaPointer<JAWT_DrawingSurface *>(drawingSurfacePtr);
        JAWT_DrawingSurfaceInfo *dsi = fromJavaPointer<JAWT_DrawingSurfaceInfo *>(drawingSurfaceInfoPtr);
        ds->FreeDrawingSurfaceInfo(dsi);
    #endif
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getPlatformInfo(JNIEnv *env, jobject obj, jlong drawingSurfaceInfoPtr)
    {
    #ifdef SK_BUILD_FOR_ANDROID
        abort();
        return 0;
    #else
        JAWT_DrawingSurfaceInfo *dsi = fromJavaPointer<JAWT_DrawingSurfaceInfo *>(drawingSurfaceInfoPtr);
        return toJavaPointer(dsi->platformInfo);
    #endif
    }
}