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

    ~TextureHolder() {
        glDeleteFramebuffers(1, &fbo);
        glDeleteTextures(1, &tex);
    }
};

extern "C"
{
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_LinuxOpenGLSwingRedrawer_createAndBindFrameBuffer(
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

        TextureHolder *holder = new TextureHolder();
        holder->tex = tex;
        holder->fbo = fbo;

        // DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);

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
} // extern "C"