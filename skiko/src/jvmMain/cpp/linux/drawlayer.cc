#include <jawt_md.h>
#include <GL/gl.h>
#include <GL/glx.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xresource.h>
#include <cstdlib>
#include <unistd.h>
#include <stdio.h>
#include <dbus/dbus.h>
#include <iostream>
#include "jni_helpers.h"

typedef GLXContext (*glXCreateContextAttribsARBProc)(Display *, GLXFBConfig, GLXContext, Bool, const int *);

extern "C"
{
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeInit(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_HardwareLayer_nativeDispose(JNIEnv *env, jobject canvas)
    {
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getWindowHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        Window win = dsi_x11->drawable;
        Window parent = win;
        Window root = None;
        Window *children;
        unsigned int nchildren;
        Status s;

        while (parent != root) {
            win = parent;
            s = XQueryTree(dsi_x11->display, win, &root, &parent, &children, &nchildren);

            if (s)
                XFree(children);
            else
                return 0;
        }
        return (jlong)win;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_HardwareLayer_getContentHandle(JNIEnv *env, jobject canvas, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        return (jlong) dsi_x11->drawable;
    }

    double getDpiScaleByDisplay(Display *display)
    {
        char *resourceManager = XResourceManagerString(display);
        if (resourceManager != nullptr)
        {
            XrmDatabase db = XrmGetStringDatabase(resourceManager);
            if (db != nullptr)
            {
                XrmValue value;

                char *type;
                XrmGetResource(db, "Xft.dpi", "Xft.dpi", &type, &value);

                if (value.addr != nullptr)
                {
                    return atof(value.addr) / 96.0;
                }
            }
        }
        return 1;
    }

    double getDpiScale() {
        Display *display = XOpenDisplay(nullptr);
        if (display != nullptr) {
            double result = getDpiScaleByDisplay(display);
            XCloseDisplay(display);
            return result;
        } else {
            return 1;
        }
    }

    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_PlatformOperationsKt_linuxGetDpiScaleNative(JNIEnv *env, jobject properties, jlong platformInfoPtr)
    {
        JAWT_X11DrawingSurfaceInfo *dsi_x11 = fromJavaPointer<JAWT_X11DrawingSurfaceInfo *>(platformInfoPtr);
        return (float) getDpiScaleByDisplay(dsi_x11->display);
    }

    DBusMessage *readSetting(DBusConnection *connection, std::string key, std::string value)
    {
        DBusError error;
        dbus_error_init(&error);

        DBusMessage *message = dbus_message_new_method_call(
                "org.freedesktop.portal.Desktop",
                "/org/freedesktop/portal/desktop",
                "org.freedesktop.portal.Settings",
                "Read"
        );

        dbus_bool_t wasSuccessfully = dbus_message_append_args(
                message,
                DBUS_TYPE_STRING, &key,
                DBUS_TYPE_STRING, &value,
                DBUS_TYPE_INVALID
        );

        if (!wasSuccessfully) return nullptr;

        DBusMessage *result = dbus_connection_send_with_reply_and_block(
                connection,
                message,
                DBUS_TIMEOUT_USE_DEFAULT,
                &error
        );

        dbus_message_unref(message);

        if (dbus_error_is_set(&error)) return nullptr;

        return result;
    }

    bool parseSettingValue(DBusMessage *message, int type, void *value)
    {
        DBusMessageIter messageIter[3];

        dbus_message_iter_init(message, &messageIter[0]);
        if (dbus_message_iter_get_arg_type(&messageIter[0]) != DBUS_TYPE_VARIANT) return false;

        dbus_message_iter_recurse(&messageIter[0], &messageIter[1]);
        if (dbus_message_iter_get_arg_type(&messageIter[1]) != DBUS_TYPE_VARIANT) return false;

        dbus_message_iter_recurse(&messageIter[1], &messageIter[2]);
        if (dbus_message_iter_get_arg_type(&messageIter[2]) != type) return false;

        dbus_message_iter_get_basic(&messageIter[2], value);

        return true;
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_SystemTheme_1awtKt_getCurrentSystemTheme(JNIEnv *env, jobject topLevel)
    {
        int systemTheme = 0;

        DBusError error;
        dbus_error_init(&error);

        DBusConnection *connection = dbus_bus_get(DBUS_BUS_SESSION, &error);

        if (dbus_error_is_set(&error)) return 2; // Unknown.

        DBusMessage *valueMessage = readSetting(connection, "org.freedesktop.appearance", "color-scheme");
        if (valueMessage == nullptr) return 2; // Unknown.

        parseSettingValue(valueMessage, DBUS_TYPE_UINT32, &systemTheme);
        dbus_message_unref(valueMessage);

        switch (systemTheme) {
            case 1:
                return 1; // Dark.
            case 2:
                return 0; // Light.
            default:
                return 2; // Unknown.
        }
    }


    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skiko_SetupKt_linuxGetSystemDpiScale(JNIEnv *env, jobject layer)
    {
        return (float) getDpiScale();
    }

} // extern "C"