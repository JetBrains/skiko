#include <cstdint>
#include <jawt_md.h>

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
            return static_cast<jlong>(reinterpret_cast<uintptr_t>(awt));
        }
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getDrawingSurface(JNIEnv *env, jobject obj, jlong awtPtr, jobject layer)
    {
        JAWT *awt = reinterpret_cast<JAWT *>(static_cast<uintptr_t>(awtPtr));
        JAWT_DrawingSurface *ds = awt->GetDrawingSurface(env, layer);
        return static_cast<jlong>(reinterpret_cast<uintptr_t>(ds));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_freeDrawingSurface(JNIEnv *env, jobject obj, jlong awtPtr, jlong drawingSurfacePtr)
    {
        JAWT *awt = reinterpret_cast<JAWT *>(static_cast<uintptr_t>(awtPtr));
        JAWT_DrawingSurface *ds = reinterpret_cast<JAWT_DrawingSurface *>(static_cast<uintptr_t>(drawingSurfacePtr));
        awt->FreeDrawingSurface(ds);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_lockDrawingSurface(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = reinterpret_cast<JAWT_DrawingSurface *>(static_cast<uintptr_t>(drawingSurfacePtr));
        ds->Lock(ds);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_unlockDrawingSurface(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = reinterpret_cast<JAWT_DrawingSurface *>(static_cast<uintptr_t>(drawingSurfacePtr));
        ds->Unlock(ds);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getDrawingSurfaceInfo(JNIEnv *env, jobject obj, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = reinterpret_cast<JAWT_DrawingSurface *>(static_cast<uintptr_t>(drawingSurfacePtr));
        JAWT_DrawingSurfaceInfo *dsi = ds->GetDrawingSurfaceInfo(ds);
        return static_cast<jlong>(reinterpret_cast<uintptr_t>(dsi));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AWTKt_freeDrawingSurfaceInfo(JNIEnv *env, jobject obj, jlong drawingSurfacePtr, jlong drawingSurfaceInfoPtr)
    {
        JAWT_DrawingSurface *ds = reinterpret_cast<JAWT_DrawingSurface *>(static_cast<uintptr_t>(drawingSurfacePtr));
        JAWT_DrawingSurfaceInfo *dsi = reinterpret_cast<JAWT_DrawingSurfaceInfo *>(static_cast<uintptr_t>(drawingSurfaceInfoPtr));
        ds->FreeDrawingSurfaceInfo(dsi);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_AWTKt_getPlatformInfo(JNIEnv *env, jobject obj, jlong drawingSurfaceInfoPtr)
    {
        JAWT_DrawingSurfaceInfo *dsi = reinterpret_cast<JAWT_DrawingSurfaceInfo *>(static_cast<uintptr_t>(drawingSurfaceInfoPtr));
        return static_cast<jlong>(reinterpret_cast<uintptr_t>(dsi->platformInfo));
    }
}