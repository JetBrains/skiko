#include <cstdint>
#include <cstdio>
#include <node_api.h>

// Take a javascript, add one and return it
static napi_value NativeAddOne(
    napi_env env,
    napi_callback_info info
) {
    size_t argc = 1;
    napi_value args[1];
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);

    int32_t value;
    napi_get_value_int32(env, args[0], &value);

    napi_value result;
    napi_create_int32(env, value + 1, &result);
    return result;
}

extern "C" int8_t
org_jetbrains_skia_ColorType__1nIsAlwaysOpaque(int32_t value);

static napi_value colorTypeIsAlwaysOpaque(
    napi_env env,
    napi_callback_info info
) {
    size_t argc = 1;
    napi_value argv[1];

    napi_get_cb_info(env, info, &argc, argv, nullptr, nullptr);

    if (argc != 1) {
        napi_throw_type_error(env, nullptr, "Expected one ColorType ordinal");
        return nullptr;
    }

    int32_t colorType;
    napi_get_value_int32(env, argv[0], &colorType);

    int8_t result =
        org_jetbrains_skia_ColorType__1nIsAlwaysOpaque(colorType);

    std::fprintf(
        stderr,
        "[NATIVE SKIKO] ColorType.isAlwaysOpaque(%d) = %d\n",
        colorType,
        result
    );

    // The Wasm implementation returns 0 or 1, so preserve that ABI.
    napi_value jsResult;
    napi_create_int32(env, result != 0 ? 1 : 0, &jsResult);
    return jsResult;
}

static napi_value Init(napi_env env, napi_value exports) {
    napi_property_descriptor methods[] = {
        { "nativeAddOne", nullptr, NativeAddOne, nullptr, nullptr, nullptr, napi_default, nullptr},
        { "org_jetbrains_skia_ColorType__1nIsAlwaysOpaque", nullptr, colorTypeIsAlwaysOpaque, nullptr, nullptr, nullptr, napi_default, nullptr }
    };

    napi_define_properties(env, exports, sizeof(methods) / sizeof(methods[0]), methods);
    return exports;
}

NAPI_MODULE(NODE_GYP_MODULE_NAME, Init)