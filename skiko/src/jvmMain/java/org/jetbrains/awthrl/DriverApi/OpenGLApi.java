package org.jetbrains.awthrl.DriverApi;

/**
 * The minimum required OpenGL constants and functions for Skia to work.
 * PS. In further development we should find a common pattern of using OpenGL,
 * Vulkan, Metal and implement common interface to all graphics APIs.
 */
public final class OpenGLApi {
    
    private OpenGLApi() {}

    private static OpenGLApi instance = null;

    public static OpenGLApi get() {
        if (instance == null) {
            System.loadLibrary("openglapi");
            instance = new OpenGLApi();
        }
        return instance;
    }

    // OpenGL constants

    public final int GL_TEXTURE_2D = 0x0DE1;

    public final int GL_TEXTURE_BINDING_2D = 0x8069;

    public final int GL_DRAW_FRAMEBUFFER_BINDING = 0x8CA6;

    public final int GL_COLOR_BUFFER_BIT = 0x00004000;

    // OpenGL functions

    public native void glViewport(int x, int y, int width, int height);

    public native void glClearColor(float r, float g, float b, float a);

    public native void glClear(int flags);

    public native void glFinish();

    public native void glEnable(int flag);

    public native void glBindTexture(int target, int texture);

    public native int glGetIntegerv(int pname);
}