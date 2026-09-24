import { readFile, writeFile } from "node:fs/promises";
import { join } from "node:path";

const packageRoot = join(process.cwd(), "node_modules", "node-gles-webgl2");

await patchFile("dist/index.js", [
    {
        marker: [
            "binding.createWebGLRenderingContext(width, height, majorVersion, minorVersion, webGLCompatibility, enabledExtensions, disabledExtensions, window)",
            "binding.createWebGLRenderingContext(width, height, majorVersion, minorVersion, webGLCompatibility, enabledExtensions, disabledExtensions, window, shareContext)",
        ],
        before:
        "    var disabledExtensions = args.disabledExtensions;\n    return binding.createWebGLRenderingContext(width, height, majorVersion, minorVersion, webGLCompatibility, enabledExtensions, disabledExtensions);\n",
        after:
        "    var disabledExtensions = args.disabledExtensions;\n    var window = args.window && args.window.gl ? args.window.gl : undefined;\n    return binding.createWebGLRenderingContext(width, height, majorVersion, minorVersion, webGLCompatibility, enabledExtensions, disabledExtensions, window);\n",
    },
    {
        marker: "binding.createWebGLRenderingContext(width, height, majorVersion, minorVersion, webGLCompatibility, enabledExtensions, disabledExtensions, window, shareContext)",
        before:
        "    var window = args.window && args.window.gl ? args.window.gl : undefined;\n    return binding.createWebGLRenderingContext(width, height, majorVersion, minorVersion, webGLCompatibility, enabledExtensions, disabledExtensions, window);\n",
        after:
        "    var window = args.window && args.window.gl ? args.window.gl : undefined;\n    var shareContext = args.shareContext;\n    return binding.createWebGLRenderingContext(width, height, majorVersion, minorVersion, webGLCompatibility, enabledExtensions, disabledExtensions, window, shareContext);\n",
    },
]);

await patchFile("binding/egl_context_wrapper.h", [
    {
        marker: "EGLNativeWindowType native_window = 0;",
        before:
        "  uint32_t width = 1;\n  uint32_t height = 1;\n  bool has_enabled_extensions_filter = false;\n",
        after:
        "  uint32_t width = 1;\n  uint32_t height = 1;\n  EGLNativeWindowType native_window = 0;\n  bool has_enabled_extensions_filter = false;\n",
    },
    {
        marker: "EGLContext share_context = EGL_NO_CONTEXT;",
        before:
        "  EGLNativeWindowType native_window = 0;\n  bool has_enabled_extensions_filter = false;\n",
        after:
        "  EGLNativeWindowType native_window = 0;\n  EGLContext share_context = EGL_NO_CONTEXT;\n  bool has_enabled_extensions_filter = false;\n",
    },
    {
        marker: "  bool SwapBuffers() const;",
        before:
        "  bool MakeCurrent() const;\n  bool EnsureCurrent() const;\n",
        after:
        "  bool MakeCurrent() const;\n  bool SwapBuffers() const;\n  bool EnsureCurrent() const;\n",
    },
]);

await patchFile("binding/egl_context_wrapper.cc", [
    {
        marker: "context_options.native_window ? EGL_WINDOW_BIT : EGL_PBUFFER_BIT",
        before:
        "  EGLint attrib_list[] = {EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,\n                          EGL_RED_SIZE,     8,\n",
        after:
        "  EGLint attrib_list[] = {EGL_SURFACE_TYPE, context_options.native_window ? EGL_WINDOW_BIT : EGL_PBUFFER_BIT,\n                          EGL_RED_SIZE,     8,\n",
    },
    {
        marker: "eglCreateWindowSurface(display, config,",
        before:
        "  EGLint surface_attribs[] = {EGL_WIDTH, (EGLint)context_options.width,\n                              EGL_HEIGHT, (EGLint)context_options.height,\n                              EGL_NONE};\n  surface = eglCreatePbufferSurface(display, config, surface_attribs);\n",
        after:
        "  if (context_options.native_window) {\n    surface = eglCreateWindowSurface(display, config,\n                                     context_options.native_window, nullptr);\n  } else {\n    EGLint surface_attribs[] = {EGL_WIDTH, (EGLint)context_options.width,\n                                EGL_HEIGHT, (EGLint)context_options.height,\n                                EGL_NONE};\n    surface = eglCreatePbufferSurface(display, config, surface_attribs);\n  }\n",
    },
    {
        marker: "bool EGLContextWrapper::SwapBuffers() const {",
        before:
        "bool EGLContextWrapper::ResizeSurface(napi_env env, uint32_t width,\n                                      uint32_t height) {\n",
        after:
        "bool EGLContextWrapper::SwapBuffers() const {\n  if (display == EGL_NO_DISPLAY || surface == EGL_NO_SURFACE) {\n    return false;\n  }\n  return eglSwapBuffers(display, surface);\n}\n\nbool EGLContextWrapper::ResizeSurface(napi_env env, uint32_t width,\n                                      uint32_t height) {\n",
    },
    {
        marker: "eglCreateContext(display, config, context_options.share_context,",
        before:
        "  context = eglCreateContext(display, config, EGL_NO_CONTEXT,\n                             context_attributes.data());\n",
        after:
        "  context = eglCreateContext(display, config, context_options.share_context,\n                             context_attributes.data());\n",
    },
]);

await patchFile("binding/webgl_rendering_context.h", [
    {
        marker: "  static napi_value Swap(napi_env env, napi_callback_info info);",
        before:
        "  static napi_value Dispose(napi_env env, napi_callback_info info);\n  static napi_value DepthFunc(napi_env env, napi_callback_info info);\n",
        after:
        "  static napi_value Dispose(napi_env env, napi_callback_info info);\n  static napi_value Swap(napi_env env, napi_callback_info info);\n  static napi_value DepthFunc(napi_env env, napi_callback_info info);\n",
    },
]);

await patchFile("binding/webgl_rendering_context.cc", [
    {
        marker: "NAPI_DEFINE_METHOD(\"swap\", Swap)",
        before:
        "      NAPI_DEFINE_METHOD(\"destroy\", Destroy),\n      NAPI_DEFINE_METHOD(\"dispose\", Dispose),\n      NAPI_DEFINE_METHOD(\"depthFunc\", DepthFunc),\n",
        after:
        "      NAPI_DEFINE_METHOD(\"destroy\", Destroy),\n      NAPI_DEFINE_METHOD(\"dispose\", Dispose),\n      NAPI_DEFINE_METHOD(\"swap\", Swap),\n      NAPI_DEFINE_METHOD(\"depthFunc\", DepthFunc),\n",
    },
    {
        marker: "napi_value WebGLRenderingContext::Swap(napi_env env, napi_callback_info info)",
        before:
        "napi_value WebGLRenderingContext::Dispose(napi_env env,\n                                          napi_callback_info info) {\n  return Destroy(env, info);\n}\n\n/* static */\nnapi_value WebGLRenderingContext::DepthFunc(napi_env env,\n",
        after:
        "napi_value WebGLRenderingContext::Dispose(napi_env env,\n                                          napi_callback_info info) {\n  return Destroy(env, info);\n}\n\n/* static */\nnapi_value WebGLRenderingContext::Swap(napi_env env, napi_callback_info info) {\n  LOG_CALL(\"Swap\");\n\n  WebGLRenderingContext *context = nullptr;\n  napi_status nstatus = GetContext(env, info, &context);\n  ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n\n  if (!context->eglContextWrapper_->SwapBuffers()) {\n    NAPI_THROW_ERROR(env, \"Could not swap WebGLRenderingContext buffers\");\n    return nullptr;\n  }\n\n  return nullptr;\n}\n\n/* static */\nnapi_value WebGLRenderingContext::DepthFunc(napi_env env,\n",
    },
    {
        marker: ["  size_t argc = 8;", "  size_t argc = 9;"],
        before:
        "  size_t argc = 7;\n  napi_value args[7];\n",
        after:
        "  size_t argc = 8;\n  napi_value args[8];\n",
    },
    {
        marker: "  size_t argc = 9;",
        before:
        "  size_t argc = 8;\n  napi_value args[8];\n",
        after:
        "  size_t argc = 9;\n  napi_value args[9];\n",
    },
    {
        marker: ["argc = std::min(argc, static_cast<size_t>(8));", "argc = std::min(argc, static_cast<size_t>(9));"],
        before:
        "  argc = std::min(argc, static_cast<size_t>(7));\n",
        after:
        "  argc = std::min(argc, static_cast<size_t>(8));\n",
    },
    {
        marker: "argc = std::min(argc, static_cast<size_t>(9));",
        before:
        "  argc = std::min(argc, static_cast<size_t>(8));\n",
        after:
        "  argc = std::min(argc, static_cast<size_t>(9));\n",
    },
    {
        marker: "opts.native_window = *reinterpret_cast<EGLNativeWindowType *>(data);",
        before:
        "  if (argc > 6) {\n    nstatus = GetOptionalStringArrayParam(env, args[6], \"disabledExtensions\",\n                                          &opts.disabled_extensions);\n    ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n  }\n\n  WebGLRenderingContext *context =\n",
        after:
        "  if (argc > 6) {\n    nstatus = GetOptionalStringArrayParam(env, args[6], \"disabledExtensions\",\n                                          &opts.disabled_extensions);\n    ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n  }\n\n  if (argc > 7) {\n    napi_valuetype value_type;\n    nstatus = napi_typeof(env, args[7], &value_type);\n    ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n    if (value_type != napi_undefined && value_type != napi_null) {\n      void *data = nullptr;\n      size_t length = 0;\n      nstatus = napi_get_buffer_info(env, args[7], &data, &length);\n      ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n      if (length < sizeof(EGLNativeWindowType)) {\n        NAPI_THROW_ERROR(env, \"window native GL handle buffer is too small\");\n        return nullptr;\n      }\n      opts.native_window = *reinterpret_cast<EGLNativeWindowType *>(data);\n    }\n  }\n\n  WebGLRenderingContext *context =\n",
    },
    {
        marker: "opts.share_context = share_context->eglContextWrapper_->context;",
        before:
        "  if (argc > 7) {\n    napi_valuetype value_type;\n    nstatus = napi_typeof(env, args[7], &value_type);\n    ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n    if (value_type != napi_undefined && value_type != napi_null) {\n      void *data = nullptr;\n      size_t length = 0;\n      nstatus = napi_get_buffer_info(env, args[7], &data, &length);\n      ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n      if (length < sizeof(EGLNativeWindowType)) {\n        NAPI_THROW_ERROR(env, \"window native GL handle buffer is too small\");\n        return nullptr;\n      }\n      opts.native_window = *reinterpret_cast<EGLNativeWindowType *>(data);\n    }\n  }\n\n  WebGLRenderingContext *context =\n",
        after:
        "  if (argc > 7) {\n    napi_valuetype value_type;\n    nstatus = napi_typeof(env, args[7], &value_type);\n    ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n    if (value_type != napi_undefined && value_type != napi_null) {\n      void *data = nullptr;\n      size_t length = 0;\n      nstatus = napi_get_buffer_info(env, args[7], &data, &length);\n      ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n      if (length < sizeof(EGLNativeWindowType)) {\n        NAPI_THROW_ERROR(env, \"window native GL handle buffer is too small\");\n        return nullptr;\n      }\n      opts.native_window = *reinterpret_cast<EGLNativeWindowType *>(data);\n    }\n  }\n\n  if (argc > 8) {\n    napi_valuetype value_type;\n    nstatus = napi_typeof(env, args[8], &value_type);\n    ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n    if (value_type != napi_undefined && value_type != napi_null) {\n      WebGLRenderingContext *share_context = nullptr;\n      nstatus = napi_unwrap(env, args[8], reinterpret_cast<void **>(&share_context));\n      ENSURE_NAPI_OK_RETVAL(env, nstatus, nullptr);\n      if (share_context != nullptr && share_context->HasNativeResources()) {\n        opts.share_context = share_context->eglContextWrapper_->context;\n      }\n    }\n  }\n\n  WebGLRenderingContext *context =\n",
    },
]);

async function patchFile(relativePath, replacements) {
    const path = join(packageRoot, relativePath);
    let source = await readFile(path, "utf8");
    let changed = false;

    for (const { marker, before, after } of replacements) {
        const markers = Array.isArray(marker) ? marker : [marker];
        if (markers.some((item) => source.includes(item))) {
            continue;
        }
        if (!source.includes(before)) {
            throw new Error(`Cannot patch ${relativePath}; expected source block was not found`);
        }
        source = source.split(before).join(after);
        changed = true;
    }

    if (changed) {
        await writeFile(path, source);
        console.log(`patched node-gles-webgl2/${relativePath}`);
    }
}
