#include <jawt_md.h>
#include <dbus/dbus.h>
#include <iostream>
#include <dlfcn.h>

#include "jni_helpers.h"

static void* loadLibDbus() {
    static void* result = nullptr;
    if (result != nullptr) return result;
    result = dlopen("libdbus-1.so", RTLD_LAZY | RTLD_LOCAL);
    return result;
}

static DBusMessage* dbus_message_new_method_call_dynamic(const char *bus_name, const char *path, const char *iface, const char *method) {
    typedef DBusMessage* (*dbus_message_new_method_call_t)(const char*, const char*, const char*, const char*);
    static dbus_message_new_method_call_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return nullptr;
        func = (dbus_message_new_method_call_t) dlsym(lib, "dbus_message_new_method_call");
    }
    if(!func) return nullptr;
    return func(bus_name, path, iface, method);
}

static dbus_bool_t dbus_message_append_args_dynamic(DBusMessage *message, int first_arg_type, ...) {
    typedef dbus_bool_t (*dbus_message_append_args_valist_t)(DBusMessage *, int, va_list);
    static dbus_message_append_args_valist_t func = nullptr;
    if (!func) {
        void *lib = loadLibDbus();
        if (!lib) return false;
        func = (dbus_message_append_args_valist_t) dlsym(lib, "dbus_message_append_args_valist");
    }
    if (!func) return false;
    va_list var_args;
    va_start(var_args, first_arg_type);
    dbus_bool_t result = func(message, first_arg_type, var_args);
    va_end(var_args);
    return result;
}

static DBusMessage* dbus_connection_send_with_reply_and_block_dynamic(DBusConnection *connection, DBusMessage *message, int timeout_milliseconds, DBusError *error) {
    typedef DBusMessage* (*dbus_connection_send_with_reply_and_block_t)(DBusConnection*, DBusMessage*, int, DBusError*);
    static dbus_connection_send_with_reply_and_block_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return nullptr;
        func = (dbus_connection_send_with_reply_and_block_t) dlsym(lib, "dbus_connection_send_with_reply_and_block");
    }
    if(!func) return nullptr;
    return func(connection, message, timeout_milliseconds, error);
}

static bool dbus_message_unref_dynamic(DBusMessage *message) {
    typedef void* (*dbus_message_unref_t)(DBusMessage*);
    static dbus_message_unref_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return false;
        func = (dbus_message_unref_t) dlsym(lib, "dbus_message_unref");
    }
    if(!func) return false;
    func(message);
    return true;
}

static dbus_bool_t dbus_error_is_set_dynamic(const DBusError *error) {
    typedef dbus_bool_t (*dbus_error_is_set_t)(const DBusError*);
    static dbus_error_is_set_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return true;
        func = (dbus_error_is_set_t) dlsym(lib, "dbus_error_is_set");
    }
    if(!func) return true;
    return func(error);
}

static dbus_bool_t dbus_message_iter_init_dynamic(DBusMessage *message, DBusMessageIter *iter) {
    typedef dbus_bool_t (*dbus_message_iter_init_t)(DBusMessage*, DBusMessageIter*);
    static dbus_message_iter_init_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return false;
        func = (dbus_message_iter_init_t) dlsym(lib, "dbus_message_iter_init");
    }
    if(!func) return false;
    return func(message, iter);
}

static int dbus_message_iter_get_arg_type_dynamic(DBusMessageIter *iter) {
    typedef int (*dbus_message_iter_get_arg_type_t)(DBusMessageIter*);
    static dbus_message_iter_get_arg_type_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return -1;
        func = (dbus_message_iter_get_arg_type_t) dlsym(lib, "dbus_message_iter_get_arg_type");
    }
    if(!func) return -1;
    return func(iter);
}

static bool dbus_message_iter_recurse_dynamic(DBusMessageIter *iter, DBusMessageIter *sub) {
    typedef void (*dbus_message_iter_recurse_t)(DBusMessageIter*, DBusMessageIter*);
    static dbus_message_iter_recurse_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return false;
        func = (dbus_message_iter_recurse_t) dlsym(lib, "dbus_message_iter_recurse");
    }
    if(!func) return false;
    func(iter, sub);
    return true;
}

static bool dbus_message_iter_get_basic_dynamic(DBusMessageIter *iter, void *value) {
    typedef void (*dbus_message_iter_get_basic_t)(DBusMessageIter*, void*);
    static dbus_message_iter_get_basic_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return false;
        func = (dbus_message_iter_get_basic_t) dlsym(lib, "dbus_message_iter_get_basic");
    }
    if(!func) return false;
    func(iter, value);
    return true;
}

static DBusConnection* dbus_bus_get_dynamic(DBusBusType type, DBusError *error) {
    typedef DBusConnection* (*dbus_bus_get_t)(DBusBusType, DBusError*);
    static dbus_bus_get_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return nullptr;
        func = (dbus_bus_get_t) dlsym(lib, "dbus_bus_get");
    }
    if(!func) return nullptr;
    return func(type, error);
}

static bool dbus_error_init_dynamic(DBusError *error) {
    typedef void (*dbus_error_init_t)(DBusError*);
    static dbus_error_init_t func = nullptr;
    if(!func) {
        void* lib = loadLibDbus();
        if(!lib) return false;
        func = (dbus_error_init_t) dlsym(lib, "dbus_error_init");
    }
    if(!func) return false;
    func(error);
    return true;
}

extern "C"
{
    static DBusMessage *getSetting(DBusConnection *connection, std::string key, std::string value)
    {
        DBusError error;
        if(!dbus_error_init_dynamic(&error)) return nullptr;

        DBusMessage *message = dbus_message_new_method_call_dynamic(
                "org.freedesktop.portal.Desktop",
                "/org/freedesktop/portal/desktop",
                "org.freedesktop.portal.Settings",
                "Read"
        );
        if(!message) return nullptr;

        dbus_bool_t wasSuccessfully = dbus_message_append_args_dynamic(
                message,
                DBUS_TYPE_STRING, &key,
                DBUS_TYPE_STRING, &value,
                DBUS_TYPE_INVALID
        );

        if (!wasSuccessfully) return nullptr;

        DBusMessage *result = dbus_connection_send_with_reply_and_block_dynamic(
                connection,
                message,
                DBUS_TIMEOUT_USE_DEFAULT,
                &error
        );
        if(!result) return nullptr;

        if(!dbus_message_unref_dynamic(message)) return nullptr;

        if (dbus_error_is_set_dynamic(&error)) return nullptr;

        return result;
    }

    static bool parseSettingValue(DBusMessage *message, int type, void *value)
    {
        DBusMessageIter messageIter[3];

        if(!dbus_message_iter_init_dynamic(message, &messageIter[0])) return false;
        if (dbus_message_iter_get_arg_type_dynamic(&messageIter[0]) != DBUS_TYPE_VARIANT) return false;

        if(!dbus_message_iter_recurse_dynamic(&messageIter[0], &messageIter[1])) return false;
        if (dbus_message_iter_get_arg_type_dynamic(&messageIter[1]) != DBUS_TYPE_VARIANT) return false;

        if(!dbus_message_iter_recurse_dynamic(&messageIter[1], &messageIter[2])) return false;
        if (dbus_message_iter_get_arg_type_dynamic(&messageIter[2]) != type) return false;

        if(!dbus_message_iter_get_basic_dynamic(&messageIter[2], value)) return false;

        return true;
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_SystemTheme_1awtKt_getCurrentSystemTheme(JNIEnv *env, jobject topLevel)
    {
        int systemTheme = 0;

        DBusError error;
        if(!dbus_error_init_dynamic(&error)) return 2; // Unknown.

        DBusConnection *connection = dbus_bus_get_dynamic(DBUS_BUS_SESSION, &error);
        if(!connection) return 2; // Unknown.

        if (dbus_error_is_set_dynamic(&error)) return 2; // Unknown.

        DBusMessage *valueMessage = getSetting(connection, "org.freedesktop.appearance", "color-scheme");
        if (!valueMessage) return 2; // Unknown.

        parseSettingValue(valueMessage, DBUS_TYPE_UINT32, &systemTheme);
        if(!dbus_message_unref_dynamic(valueMessage)) return 2; // Unknown.

        switch (systemTheme) {
            case 1:
                return 1; // Dark.
            case 2:
                return 0; // Light.
            default:
                return 2; // Unknown.
        }
    }
}