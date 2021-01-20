#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <cstdlib>
#include <unistd.h>
#include <stdio.h>

typedef GLXContext (*glXCreateContextAttribsARBProc)(Display *, GLXFBConfig, GLXContext, Bool, const int *);

extern "C" jboolean Skiko_GetAWT(JNIEnv *env, JAWT *awt);

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_lockDrawingSurfaceNative(JNIEnv *env, jobject redrawer, jobject layer)
    {
        JAWT awt;
        awt.version = (jint)JAWT_VERSION_9;
        if (!Skiko_GetAWT(env, &awt))
        {
            fprintf(stderr, "JAWT_GetAWT failed! Result is JNI_FALSE\n");
            return 0;
        }

        JAWT_DrawingSurface *ds = awt.GetDrawingSurface(env, layer);
        ds->Lock(ds);

        return static_cast<jlong>(reinterpret_cast<uintptr_t>(ds));
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_unlockDrawingSurfaceNative(JNIEnv *env, jobject redrawer, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = reinterpret_cast<JAWT_DrawingSurface *>(static_cast<uintptr_t>(drawingSurfacePtr));
        JAWT awt;
        awt.version = (jint)JAWT_VERSION_9;
        if (!Skiko_GetAWT(env, &awt))
        {
            fprintf(stderr, "JAWT_GetAWT failed! Result is JNI_FALSE\n");
            return 0;
        }

        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);

        return static_cast<jlong>(reinterpret_cast<uintptr_t>(ds));
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_getDisplay(JNIEnv *env, jobject redrawer, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = reinterpret_cast<JAWT_DrawingSurface *>(static_cast<uintptr_t>(drawingSurfacePtr));
        JAWT_DrawingSurfaceInfo *dsi = ds->GetDrawingSurfaceInfo(ds);
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = (JAWT_X11DrawingSurfaceInfo *)dsi->platformInfo;

        Display *display = dsi_x11->display;

        ds->FreeDrawingSurfaceInfo(dsi);
        return static_cast<jlong>(reinterpret_cast<uintptr_t>(display));
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_getWindow(JNIEnv *env, jobject redrawer, jlong drawingSurfacePtr)
    {
        JAWT_DrawingSurface *ds = reinterpret_cast<JAWT_DrawingSurface *>(static_cast<uintptr_t>(drawingSurfacePtr));
        JAWT_DrawingSurfaceInfo *dsi = ds->GetDrawingSurfaceInfo(ds);
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = (JAWT_X11DrawingSurfaceInfo *)dsi->platformInfo;

        Window window = dsi_x11->drawable;

        ds->FreeDrawingSurfaceInfo(dsi);
        return static_cast<jlong>(reinterpret_cast<uintptr_t>(window));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_setSwapInterval(JNIEnv *env, jobject redrawer, jlong displayPtr, jlong windowPtr, jint interval)
    {
        Display *display = reinterpret_cast<Display *>(static_cast<uintptr_t>(displayPtr));
        Window window = reinterpret_cast<Window>(static_cast<uintptr_t>(windowPtr));

        // according to:
        // https://opengl.gpuinfo.org/listreports.php?extension=GLX_EXT_swap_control
        // https://opengl.gpuinfo.org/listreports.php?extension=GLX_MESA_swap_control
        // https://opengl.gpuinfo.org/listreports.php?extension=GLX_SGI_swap_control
        // there is no Linux that doesn't support at least one of these extensions
        static PFNGLXSWAPINTERVALEXTPROC glXSwapIntervalEXT = (PFNGLXSWAPINTERVALEXTPROC) glXGetProcAddress((const GLubyte*)"glXSwapIntervalEXT");
        if (glXSwapIntervalEXT != NULL)
        {
            glXSwapIntervalEXT(display, window, interval);
        }
        else
        {
            static PFNGLXSWAPINTERVALMESAPROC glXSwapIntervalMESA = (PFNGLXSWAPINTERVALMESAPROC) glXGetProcAddress((const GLubyte*)"glXSwapIntervalMESA");
            if (glXSwapIntervalMESA != NULL)
            {
                glXSwapIntervalMESA(interval);
            }
            else
            {
                static PFNGLXSWAPINTERVALSGIPROC glXSwapIntervalSGI = (PFNGLXSWAPINTERVALSGIPROC) glXGetProcAddress((const GLubyte*)"glXSwapIntervalSGI");
                if (glXSwapIntervalSGI != NULL)
                {
                    glXSwapIntervalSGI(interval);
                }
            }
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_swapBuffers(JNIEnv *env, jobject redrawer, jlong displayPtr, jlong windowPtr)
    {
        Display *display = reinterpret_cast<Display *>(static_cast<uintptr_t>(displayPtr));
        Window window = reinterpret_cast<Window>(static_cast<uintptr_t>(windowPtr));

        glXSwapBuffers(display, window);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_makeCurrent(JNIEnv *env, jobject redrawer, jlong displayPtr, jlong windowPtr, jlong contextPtr)
    {
        Display *display = reinterpret_cast<Display *>(static_cast<uintptr_t>(displayPtr));
        Window window = reinterpret_cast<Window>(static_cast<uintptr_t>(windowPtr));
        GLXContext *context = reinterpret_cast<GLXContext *>(static_cast<uintptr_t>(contextPtr));

        glXMakeCurrent(display, window, *context);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_createContext(JNIEnv *env, jobject redrawer, jlong displayPtr)
    {
        Display *display = reinterpret_cast<Display *>(static_cast<uintptr_t>(displayPtr));
        GLint att[] = {GLX_RGBA, GLX_DOUBLEBUFFER, True, None};
        XVisualInfo *vi = glXChooseVisual(display, 0, att);

        GLXContext *context = new GLXContext(glXCreateContext(display, vi, NULL, GL_TRUE));
        return static_cast<jlong>(reinterpret_cast<uintptr_t>(context));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_LinuxRedrawerKt_destroyContext(JNIEnv *env, jobject redrawer, jlong displayPtr, jlong contextPtr)
    {
        Display *display = reinterpret_cast<Display *>(static_cast<uintptr_t>(displayPtr));
        GLXContext *context = reinterpret_cast<GLXContext *>(static_cast<uintptr_t>(contextPtr));

        glXDestroyContext(display, *context);
        delete context;
    }
}