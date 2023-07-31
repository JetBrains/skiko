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

class OffScreenContext
{
public:
    Display* display;
    GLXContext context;
    GLXFBConfig* fbConfigs;

    OffScreenContext() {
        const int glxContextAttribs[] {
            GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT,
            GLX_RENDER_TYPE, GLX_RGBA_BIT,
            GLX_DOUBLEBUFFER, False,
            GLX_RED_SIZE, 8,
            GLX_GREEN_SIZE, 8,
            GLX_BLUE_SIZE, 8,
            None
        };

        display = XOpenDisplay(nullptr);

        int numConfigs = 0;
        fbConfigs = glXChooseFBConfig(display, DefaultScreen(display), glxContextAttribs, &numConfigs);

        XVisualInfo* visual = glXGetVisualFromFBConfig(display, fbConfigs[0]);

        context = glXCreateContext(display, visual, nullptr, True);

        XFree(visual);
    }

    ~OffScreenContext() {
        XFree(fbConfigs);
        glXDestroyContext(display, context);
        XCloseDisplay(display);
    }
};

class OffScreenBuffer
{
public:
    Display* display;
    GLXPbuffer pbuffer;
    int width;
    int height;

    OffScreenBuffer(OffScreenContext* context, int _width, int _height) {
        int pbufferAttribs[] = {
            GLX_PBUFFER_WIDTH, width,
            GLX_PBUFFER_HEIGHT, height,
            None  
        };

        width = _width;
        height = _height;
        display = context->display;

        pbuffer = glXCreatePbuffer(context->display, context->fbConfigs[0], pbufferAttribs);
    }

    ~OffScreenBuffer() {
        glXDestroyPbuffer(display, pbuffer);
    }
};

class OffScreenTexture
{
public:
    GLuint tex;
    GLuint fbo;

    OffScreenTexture(GLuint _tex, GLuint _fbo) {
        tex = _tex;
        fbo = _fbo;
    }

    ~OffScreenTexture() {
        glDeleteFramebuffers(1, &fbo);
        glDeleteTextures(1, &tex);
    }
};

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_makeOffScreenContext(
        JNIEnv *env, jobject redrawer)
    {
        OffScreenContext* context = new OffScreenContext();
        return toJavaPointer(context);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_disposeOffScreenContext(
        JNIEnv *env, jobject redrawer, jlong contextPtr)
    {
        OffScreenContext* context = fromJavaPointer<OffScreenContext *>(contextPtr);
        delete context;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_makeOffScreenBuffer(
        JNIEnv *env, jobject redrawer, jlong contextPtr, jlong oldBufferPtr, jint width, jint height)
    {
        OffScreenContext* context = fromJavaPointer<OffScreenContext *>(contextPtr);
        OffScreenBuffer* oldBuffer = fromJavaPointer<OffScreenBuffer *>(oldBufferPtr);
        
        OffScreenBuffer* buffer;
        if (oldBuffer == nullptr || oldBuffer->width != width || oldBuffer->height != height) {
            if (oldBuffer != nullptr) {
                delete oldBuffer;
            }
            buffer = new OffScreenBuffer(context, width, height);
        } else {
            buffer = oldBuffer;
        }

        return toJavaPointer(buffer);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_disposeOffScreenBuffer(
        JNIEnv *env, jobject redrawer, jlong bufferPtr)
    {
        OffScreenBuffer* buffer = fromJavaPointer<OffScreenBuffer *>(bufferPtr);
        delete buffer;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_startRendering(
        JNIEnv *env, jobject redrawer, jlong contextPtr, jlong bufferPtr)
    {
        OffScreenContext* context = fromJavaPointer<OffScreenContext *>(contextPtr);
        OffScreenBuffer* buffer = fromJavaPointer<OffScreenBuffer *>(bufferPtr);

        glXMakeCurrent(context->display, buffer->pbuffer, context->context);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_finishRendering(
        JNIEnv *env, jobject redrawer, jlong contextPtr)
    {
        OffScreenContext* context = fromJavaPointer<OffScreenContext *>(contextPtr);
        glXMakeCurrent(context->display, None, nullptr);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_createAndBindTexture(
        JNIEnv *env, jobject redrawer, jint width, jint height)
    {
        GLuint tex;
        glGenTextures(1, &tex);
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);

        GLuint fbo;
        glGenFramebuffers(1, &fbo);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex, 0);

        OffScreenTexture *texture = new OffScreenTexture(tex, fbo);

        return toJavaPointer(texture);
    }

    JNIEXPORT GLuint JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_getFboId(
        JNIEnv *env, jobject redrawer, jlong texturePtr)
    {
        OffScreenTexture *texture = fromJavaPointer<OffScreenTexture *>(texturePtr);

        return texture->fbo;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_unbindAndDisposeTexture(
        JNIEnv *env, jobject redrawer, jlong texturePtr)
    {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        OffScreenTexture *texture = fromJavaPointer<OffScreenTexture *>(texturePtr);

        delete texture;
    }
} // extern "C"