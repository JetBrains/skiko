#include "common.h"

struct SkikoTestGlContext;
SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext();
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext(KNativePointer ptr);
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent(KNativePointer ptr);
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers(KNativePointer ptr);

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nGlContextGetFinalizer() {
    return reinterpret_cast<KNativePointer>(org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext);
}

#ifdef SK_GL
#ifdef __linux__

#define SKIKO_TEST_GL_INCLUDED

#include <GL/glx.h>

struct SkikoTestGlContext {
    Display* display;
    GLXWindow surface;
    GLXContext context;
    Window window;
};

static Bool waitForWindow(Display *dpy, XEvent *event, XPointer arg) {
  return (event->xmap.window == (Window)arg) && (event->type == MapNotify);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext() {
   const int glxContextAttribs[] {
        GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT,
        GLX_RENDER_TYPE, GLX_RGBA_BIT,
        GLX_DOUBLEBUFFER, True,
        GLX_RED_SIZE, 8,
        GLX_GREEN_SIZE, 8,
        GLX_BLUE_SIZE, 8,
        None
    };

    Display* display = XOpenDisplay(nullptr);
    SKIKO_ASSERT(display, "Failed to connect to Xserver");

    int numConfigs = 0;
    GLXFBConfig* fbConfigs = glXChooseFBConfig(display, DefaultScreen(display), glxContextAttribs, &numConfigs);
    SKIKO_ASSERT(fbConfigs != nullptr && numConfigs > 0, "No suitable fbconfig available");
    GLXFBConfig fbConfig = fbConfigs[0];
    XFree(fbConfigs);

    XVisualInfo* visual = glXGetVisualFromFBConfig(display, fbConfig);
    XSetWindowAttributes setWindowAttributes;
    setWindowAttributes.event_mask = StructureNotifyMask;
    setWindowAttributes.border_pixel = 0;
    setWindowAttributes.colormap = XCreateColormap(display, RootWindow(display, visual->screen), visual->visual, AllocNone);
    int setWindowAttributesMask = CWBorderPixel | CWColormap | CWEventMask;

    Window window = XCreateWindow(display, RootWindow(display, visual->screen),
        0, 0, 1280, 720, 0, visual->depth, InputOutput, visual->visual,
        setWindowAttributesMask, &setWindowAttributes);

    GLXWindow surface = glXCreateWindow(display, fbConfig, window, nullptr);
    SKIKO_ASSERT(surface, "Failed to create surface");

    GLXContext context = glXCreateNewContext(display, fbConfig, GLX_RGBA_TYPE, nullptr, True);
    SKIKO_ASSERT(context, "Failed to create context");

    XMapWindow(display, window);
    XEvent event;
    XIfEvent(display, &event, waitForWindow, (XPointer)window);

    return reinterpret_cast<KInteropPointer>(new SkikoTestGlContext { display, surface, context, window });
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    glXMakeContextCurrent(instance->display, None, None, nullptr);
    glXDestroyWindow(instance->display, instance->surface);
    glXDestroyContext(instance->display, instance->context);
    XDestroyWindow(instance->display, instance->window);
    XCloseDisplay(instance->display);
    delete instance;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    glXMakeContextCurrent(instance->display, instance->surface, instance->surface, instance->context);
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers(KNativePointer ptr) {
    auto* instance = reinterpret_cast<SkikoTestGlContext*>(ptr);
    glXSwapBuffers(instance->display, instance->surface);
}

#endif // __linux__
#endif // SK_GL

#ifndef SKIKO_TEST_GL_INCLUDED
SKIKO_EXPORT KNativePointer org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext() {
    TODO("OpenGL context is not supported for this platform");
    return nullptr;
}

SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nDeleteTestGlContext(KNativePointer ptr) {}
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent(KNativePointer ptr) {}
SKIKO_EXPORT void org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers(KNativePointer ptr) {}
#endif // SKIKO_TEST_GL_INCLUDED