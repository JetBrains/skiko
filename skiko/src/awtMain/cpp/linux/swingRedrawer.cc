#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <cstdlib>
#include <unistd.h>
#include <stdio.h>
#include "jni_helpers.h"

class TextureHolder
{
public:
    GLuint tex;
    GLuint fbo;
    Display* display;
    GLXPbuffer pbuffer;
    GLXContext context;

    ~TextureHolder() {
        glDeleteFramebuffers(1, &fbo);
        glDeleteTextures(1, &tex);
        glXDestroyContext(display, context);
        glXDestroyPbuffer(display, pbuffer);
        XCloseDisplay(display);
    }
};

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_createAndBindFrameBuffer(
        JNIEnv *env, jobject redrawer, jint width, jint height)
    {
        const int glxContextAttribs[] {
            GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT,
            GLX_RENDER_TYPE, GLX_RGBA_BIT,
            GLX_DOUBLEBUFFER, False,
            GLX_RED_SIZE, 8,
            GLX_GREEN_SIZE, 8,
            GLX_BLUE_SIZE, 8,
            None
        };

        Display* display = XOpenDisplay(nullptr);

        int numConfigs = 0;
        GLXFBConfig* fbConfigs = glXChooseFBConfig(display, DefaultScreen(display), glxContextAttribs, &numConfigs);

        int pbufferAttribs[] = {
            GLX_PBUFFER_WIDTH, width,
            GLX_PBUFFER_HEIGHT, height,
            None  
        };

        GLXPbuffer pbuffer = glXCreatePbuffer(display, fbConfigs[0], pbufferAttribs);

        XVisualInfo* visual = glXGetVisualFromFBConfig(display, fbConfigs[0]);

        GLXContext context = glXCreateContext(display, visual, nullptr, True);
        glXMakeCurrent(display, pbuffer, context);

        XFree(visual);
        XFree(fbConfigs);

        GLuint tex;
        glGenTextures(1, &tex);
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);

        GLuint fbo;
        glGenFramebuffers(1, &fbo);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex, 0);

        TextureHolder *holder = new TextureHolder();
        holder->display = display;
        holder->context = context;
        holder->pbuffer=pbuffer;
        holder->tex = tex;
        holder->fbo = fbo;

        return toJavaPointer(holder);
    }

    JNIEXPORT GLuint JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_getFboId(
        JNIEnv *env, jobject redrawer, jlong holderPtr)
    {
        TextureHolder *d3dDevice = fromJavaPointer<TextureHolder *>(holderPtr);

        return d3dDevice->fbo;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_disposeTexture(
        JNIEnv *env, jobject redrawer, jlong holderPtr)
    {
        TextureHolder *d3dDevice = fromJavaPointer<TextureHolder *>(holderPtr);

        delete d3dDevice;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_finishRendering(
        JNIEnv *env, jobject redrawer, jlong holderPtr)
    {
        TextureHolder *d3dDevice = fromJavaPointer<TextureHolder *>(holderPtr);
        glFinish();
        glXMakeCurrent(d3dDevice->display, None, nullptr);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glFinish();
    }
} // extern "C"