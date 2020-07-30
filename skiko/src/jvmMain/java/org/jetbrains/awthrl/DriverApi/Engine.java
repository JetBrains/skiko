package org.jetbrains.awthrl.DriverApi;

import org.jetbrains.awthrl.Common.OSType;
import org.jetbrains.awthrl.Components.Drawable;

public class Engine {
    private Engine() {
    }

    private static Engine instance = null;

    private GraphicsApi api = GraphicsApi.UNKNOWN;
    public GraphicsApi currentApi() {
        return api;
    }

    public static Engine get() {
        if (instance == null) {
            System.loadLibrary("drawcanvas");
            instance = new Engine();
            instance.initSuitableGraphicsApi(OSType.getCurrent());
        }
        return instance;
    }

    /**
     * GAG - At the moment always initializes OpenGL.
     * @param osType The current OS type of the device.
     */
    private void initSuitableGraphicsApi(OSType osType) {
        if (osType == OSType.MAC_OS) {
            OpenGLApi.get(); // for Metal
            instance.api = GraphicsApi.OPENGL;
        } else if (osType == OSType.LINUX) {
            OpenGLApi.get(); // for Vulkan and OpenGL
            instance.api = GraphicsApi.OPENGL;
        } else if (osType == OSType.WINDOWS) {
            OpenGLApi.get(); // for Vulkan and OpenGL
            instance.api = GraphicsApi.OPENGL;
        } else {
            OpenGLApi.get(); // default
            instance.api = GraphicsApi.OPENGL;
        }
    }

    public void render(Drawable drawable) {
        drawable.updateLayer();
        drawable.redrawLayer();
    }
}