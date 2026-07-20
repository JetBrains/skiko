/**
 * @license
 * Copyright 2010 The Emscripten Authors
 * SPDX-License-Identifier: MIT
 */

// Specifies the size of the GL temp buffer pool, in bytes. Must be a multiple
// of 9 and 16.


export var LibraryGL = {
  // For functions such as glDrawBuffers, glInvalidateFramebuffer and
  // glInvalidateSubFramebuffer that need to pass a short array to the WebGL
  // API, create a set of short fixed-length arrays to avoid having to generate
  // any garbage when calling those functions.
  $tempFixedLengthArray__postset: 'for (let i = 0; i < 32; ++i) tempFixedLengthArray.push(new Array(i));',
  $tempFixedLengthArray: [],

  $miniTempWebGLFloatBuffers: [],
  $miniTempWebGLFloatBuffers__postset: `var miniTempWebGLFloatBuffersStorage = new Float32Array(288);
// Create GL_POOL_TEMP_BUFFERS_SIZE+1 temporary buffers, for uploads of size 0 through GL_POOL_TEMP_BUFFERS_SIZE inclusive
for (/**@suppress{duplicate}*/var i = 0; i <= 288; ++i) {
  miniTempWebGLFloatBuffers[i] = miniTempWebGLFloatBuffersStorage.subarray(0, i);
}`,

  $miniTempWebGLIntBuffers: [],
  $miniTempWebGLIntBuffers__postset: `var miniTempWebGLIntBuffersStorage = new Int32Array(288);
// Create GL_POOL_TEMP_BUFFERS_SIZE+1 temporary buffers, for uploads of size 0 through GL_POOL_TEMP_BUFFERS_SIZE inclusive
for (/**@suppress{duplicate}*/var i = 0; i <= 288; ++i) {
  miniTempWebGLIntBuffers[i] = miniTempWebGLIntBuffersStorage.subarray(0, i);
}`,

  $heapObjectForWebGLType: (type) => {
    // Micro-optimization for size: Subtract lowest GL enum number (0x1400/* GL_BYTE */) from type to compare
    // smaller values for the heap, for shorter generated code size.
    // Also the type HEAPU16 is not tested for explicitly, but any unrecognized type will return out HEAPU16.
    // (since most types are HEAPU16)
    type -= 0x1400;
    if (type == 0) return HEAP8;

    if (type == 1) return HEAPU8;

    if (type == 2) return HEAP16;

    if (type == 4) return HEAP32;

    if (type == 6) return HEAPF32;

    if (type == 5
      || type == 28922
      || type == 28520
      || type == 30779
      || type == 30782
      )
      return HEAPU32;

    return HEAPU16;
  },

  $toTypedArrayIndex: (pointer, heap) =>
    pointer >>> (31 - Math.clz32(heap.BYTES_PER_ELEMENT)),

  $webgl_enable_WEBGL_multi_draw: (ctx) =>
    // Closure is expected to be allowed to minify the '.multiDrawWebgl' property, so not accessing it quoted.
    !!(ctx.multiDrawWebgl = ctx.getExtension('WEBGL_multi_draw')),

  emscripten_webgl_enable_WEBGL_multi_draw__deps: ['$webgl_enable_WEBGL_multi_draw'],
  emscripten_webgl_enable_WEBGL_multi_draw: (ctx) => webgl_enable_WEBGL_multi_draw(GL.contexts[ctx].GLctx),

  $webgl_enable_EXT_polygon_offset_clamp: (ctx) =>
    !!(ctx.extPolygonOffsetClamp = ctx.getExtension('EXT_polygon_offset_clamp')),

  emscripten_webgl_enable_EXT_polygon_offset_clamp__deps: ['$webgl_enable_EXT_polygon_offset_clamp'],
  emscripten_webgl_enable_EXT_polygon_offset_clamp: (ctx) => webgl_enable_EXT_polygon_offset_clamp(GL.contexts[ctx].GLctx),

  $webgl_enable_EXT_clip_control: (ctx) =>
    !!(ctx.extClipControl = ctx.getExtension('EXT_clip_control')),

  emscripten_webgl_enable_EXT_clip_control__deps: ['$webgl_enable_EXT_clip_control'],
  emscripten_webgl_enable_EXT_clip_control: (ctx) => webgl_enable_EXT_clip_control(GL.contexts[ctx].GLctx),

  $webgl_enable_WEBGL_polygon_mode: (ctx) =>
    !!(ctx.webglPolygonMode = ctx.getExtension('WEBGL_polygon_mode')),

  emscripten_webgl_enable_WEBGL_polygon_mode__deps: ['$webgl_enable_WEBGL_polygon_mode'],
  emscripten_webgl_enable_WEBGL_polygon_mode: (ctx) => webgl_enable_WEBGL_polygon_mode(GL.contexts[ctx].GLctx),

  $getEmscriptenSupportedExtensions__internal: true,
  $getEmscriptenSupportedExtensions: (ctx) => {
    // Restrict the list of advertised extensions to those that we actually
    // support.
    var supportedExtensions = [
      // WebGL 2 extensions
      'EXT_color_buffer_float',
      'EXT_conservative_depth',
      'EXT_disjoint_timer_query_webgl2',
      'EXT_texture_norm16',
      'NV_shader_noperspective_interpolation',
      'WEBGL_clip_cull_distance',
      // WebGL 1 and WebGL 2 extensions
      'EXT_clip_control',
      'EXT_color_buffer_half_float',
      'EXT_depth_clamp',
      'EXT_float_blend',
      'EXT_polygon_offset_clamp',
      'EXT_texture_compression_bptc',
      'EXT_texture_compression_rgtc',
      'EXT_texture_filter_anisotropic',
      'KHR_parallel_shader_compile',
      'OES_texture_float_linear',
      'WEBGL_blend_func_extended',
      'WEBGL_compressed_texture_astc',
      'WEBGL_compressed_texture_etc',
      'WEBGL_compressed_texture_etc1',
      'WEBGL_compressed_texture_s3tc',
      'WEBGL_compressed_texture_s3tc_srgb',
      'WEBGL_debug_renderer_info',
      'WEBGL_debug_shaders',
      'WEBGL_lose_context',
      'WEBGL_multi_draw',
      'WEBGL_polygon_mode'
    ];
    // .getSupportedExtensions() can return null if context is lost, so coerce to empty array.
    return ctx.getSupportedExtensions()?.filter(ext => supportedExtensions.includes(ext)) ?? [];
  },

  $GLctx__internal: true,
  $GLctx: undefined,
  $GL__deps: [
    '$GLctx',
  // If GL_SUPPORT_AUTOMATIC_ENABLE_EXTENSIONS is enabled, GL.initExtensions() will call to initialize these.
    '$webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance',
    '$webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance',
    '$webgl_enable_EXT_polygon_offset_clamp',
    '$webgl_enable_EXT_clip_control',
    '$webgl_enable_WEBGL_polygon_mode',
    '$webgl_enable_WEBGL_multi_draw',
    '$getEmscriptenSupportedExtensions',
  ],
  $GL: {

/* We do not depend on the exact initial values of falsey member fields - these
   fields can be populated on-demand to save code size.
   (but still documented here to keep track of what is supposed to be present)
    lastError: 0,
    currentContext: null,

*/

    counter: 1, // 0 is reserved as 'null' in gl
    buffers: [],
    programs: [],
    framebuffers: [],
    renderbuffers: [],
    textures: [],
    shaders: [],
    vaos: [],
    contexts: [],
    // DOM ID -> OffscreenCanvas mappings of <canvas> elements that have their
    // rendering control transferred to offscreen.
    offscreenCanvases: {},
    // on WebGL1 stores WebGLTimerQueryEXT, on WebGL2 WebGLQuery
    queries: [],
    samplers: [],
    transformFeedbacks: [],
    syncs: [],

    stringCache: {},
    stringiCache: {},

    unpackAlignment: 4, // default alignment is 4 bytes
    unpackRowLength: 0,

    // Records a GL error condition that occurred, stored until user calls
    // glGetError() to fetch it. As per GLES2 spec, only the first error is
    // remembered, and subsequent errors are discarded until the user has
    // cleared the stored error by a call to glGetError().
    recordError: (errorCode) => {
      if (!GL.lastError) {
        GL.lastError = errorCode;
      }
    },
    // Get a new ID for a texture/buffer/etc., while keeping the table dense and
    // fast. Creation is fairly rare so it is worth optimizing lookups later.
    getNewId: (table) => {
      var ret = GL.counter++;
      for (var i = table.length; i < ret; i++) {
        table[i] = null;
      }
      return ret;
    },

    // The code path for creating textures, buffers, framebuffers and other
    // objects is the same (and not in fast path), so we merge the functions
    // together.
    // 'createFunction' refers to the WebGL context function name to do the actual
    // creation, 'objectTable' points to the GL object table where to populate the
    // created objects, and 'functionName' carries the name of the caller for
    // debug information.
    genObject: (n, buffers, createFunction, objectTable
      ) => {
      for (var i = 0; i < n; i++) {
        var buffer = GLctx[createFunction]();
        var id = buffer && GL.getNewId(objectTable);
        if (buffer) {
          buffer.name = id;
          objectTable[id] = buffer;
        } else {
          GL.recordError(0x502 /* GL_INVALID_OPERATION */);
        }
        HEAP32[(((buffers)+(i*4))>>2)] = id;
      }
    },

    getSource: (shader, count, string, length) => {
      var source = '';
      for (var i = 0; i < count; ++i) {
        var len = length ? HEAPU32[(((length)+(i*4))>>2)] : undefined;
        source += UTF8ToString(HEAPU32[(((string)+(i*4))>>2)], len);
      }
      return source;
    },

    // Returns the context handle to the new context.
    createContext: (/** @type {HTMLCanvasElement} */ canvas, webGLContextAttributes) => {
      // In proxied operation mode, rAF()/setTimeout() functions do not delimit
      // frame boundaries, so can't have WebGL implementation try to detect when
      // it's ok to discard contents of the rendered backbuffer.
      if (webGLContextAttributes.renderViaOffscreenBackBuffer) webGLContextAttributes['preserveDrawingBuffer'] = true;

      // BUG: Workaround Safari WebGL issue: After successfully acquiring WebGL
      // context on a canvas, calling .getContext() will always return that
      // context independent of which 'webgl' or 'webgl2'
      // context version was passed. See:
      //   https://webkit.org/b/222758
      // and:
      //   https://github.com/emscripten-core/emscripten/issues/13295.
      // TODO: Once the bug is fixed and shipped in Safari, adjust the Safari
      // version field in above check.
      if (!canvas.getContextSafariWebGL2Fixed) {
        canvas.getContextSafariWebGL2Fixed = canvas.getContext;
        /** @type {function(this:HTMLCanvasElement, string, (Object|null)=): (Object|null)} */
        function fixedGetContext(ver, attrs) {
          var gl = canvas.getContextSafariWebGL2Fixed(ver, attrs);
          return ((ver == 'webgl') == (gl instanceof WebGLRenderingContext)) ? gl : null;
        }
        canvas.getContext = fixedGetContext;
      }

      var ctx = canvas.getContext("webgl2", webGLContextAttributes);

      if (!ctx) return 0;

      var handle = GL.registerContext(ctx, webGLContextAttributes);

      return handle;
    },

    enableOffscreenFramebufferAttributes: (webGLContextAttributes) => {
      webGLContextAttributes.renderViaOffscreenBackBuffer = true;
      webGLContextAttributes.preserveDrawingBuffer = true;
    },

    // If WebGL is being proxied from a pthread to the main thread, we can't
    // directly render to the WebGL default back buffer because of WebGL's
    // implicit swap behavior. Therefore in such modes, create an offscreen
    // render target surface to which rendering is performed to, and finally
    // flipped to the main screen.
    createOffscreenFramebuffer: (context) => {
      var gl = context.GLctx;

      // Create FBO
      var fbo = gl.createFramebuffer();
      gl.bindFramebuffer(0x8D40 /*GL_FRAMEBUFFER*/, fbo);
      context.defaultFbo = fbo;

      context.defaultFboForbidBlitFramebuffer = false;
      if (gl.getContextAttributes().antialias) {
        context.defaultFboForbidBlitFramebuffer = true;
      }

      // Create render targets to the FBO
      context.defaultColorTarget = gl.createTexture();
      context.defaultDepthTarget = gl.createRenderbuffer();
      // Size them up correctly (use the same mechanism when resizing on demand)
      GL.resizeOffscreenFramebuffer(context);

      gl.bindTexture(0xDE1 /*GL_TEXTURE_2D*/, context.defaultColorTarget);
      gl.texParameteri(0xDE1 /*GL_TEXTURE_2D*/, 0x2801 /*GL_TEXTURE_MIN_FILTER*/, 0x2600 /*GL_NEAREST*/);
      gl.texParameteri(0xDE1 /*GL_TEXTURE_2D*/, 0x2800 /*GL_TEXTURE_MAG_FILTER*/, 0x2600 /*GL_NEAREST*/);
      gl.texParameteri(0xDE1 /*GL_TEXTURE_2D*/, 0x2802 /*GL_TEXTURE_WRAP_S*/, 0x812F /*GL_CLAMP_TO_EDGE*/);
      gl.texParameteri(0xDE1 /*GL_TEXTURE_2D*/, 0x2803 /*GL_TEXTURE_WRAP_T*/, 0x812F /*GL_CLAMP_TO_EDGE*/);
      gl.texImage2D(0xDE1 /*GL_TEXTURE_2D*/, 0, 0x1908 /*GL_RGBA*/, gl.canvas.width, gl.canvas.height, 0, 0x1908 /*GL_RGBA*/, 0x1401 /*GL_UNSIGNED_BYTE*/, null);
      gl.framebufferTexture2D(0x8D40 /*GL_FRAMEBUFFER*/, 0x8CE0 /*GL_COLOR_ATTACHMENT0*/, 0xDE1 /*GL_TEXTURE_2D*/, context.defaultColorTarget, 0);
      gl.bindTexture(0xDE1 /*GL_TEXTURE_2D*/, null);

      // Create depth render target to the FBO
      var depthTarget = gl.createRenderbuffer();
      gl.bindRenderbuffer(0x8D41 /*GL_RENDERBUFFER*/, context.defaultDepthTarget);
      gl.renderbufferStorage(0x8D41 /*GL_RENDERBUFFER*/, 0x81A5 /*GL_DEPTH_COMPONENT16*/, gl.canvas.width, gl.canvas.height);
      gl.framebufferRenderbuffer(0x8D40 /*GL_FRAMEBUFFER*/, 0x8D00 /*GL_DEPTH_ATTACHMENT*/, 0x8D41 /*GL_RENDERBUFFER*/, context.defaultDepthTarget);
      gl.bindRenderbuffer(0x8D41 /*GL_RENDERBUFFER*/, null);

      // Create blitter
      var vertices = [
        -1, -1,
        -1,  1,
         1, -1,
         1,  1
      ];
      var vb = gl.createBuffer();
      gl.bindBuffer(0x8892 /*GL_ARRAY_BUFFER*/, vb);
      gl.bufferData(0x8892 /*GL_ARRAY_BUFFER*/, new Float32Array(vertices), 0x88E4 /*GL_STATIC_DRAW*/);
      gl.bindBuffer(0x8892 /*GL_ARRAY_BUFFER*/, null);
      context.blitVB = vb;

      var vsCode =
        'attribute vec2 pos;' +
        'varying lowp vec2 tex;' +
        'void main() { tex = pos * 0.5 + vec2(0.5,0.5); gl_Position = vec4(pos, 0.0, 1.0); }';
      var vs = gl.createShader(0x8B31 /*GL_VERTEX_SHADER*/);
      gl.shaderSource(vs, vsCode);
      gl.compileShader(vs);

      var fsCode =
        'varying lowp vec2 tex;' +
        'uniform sampler2D sampler;' +
        'void main() { gl_FragColor = texture2D(sampler, tex); }';
      var fs = gl.createShader(0x8B30 /*GL_FRAGMENT_SHADER*/);
      gl.shaderSource(fs, fsCode);
      gl.compileShader(fs);

      var blitProgram = gl.createProgram();
      gl.attachShader(blitProgram, vs);
      gl.attachShader(blitProgram, fs);
      gl.linkProgram(blitProgram);
      context.blitProgram = blitProgram;
      context.blitPosLoc = gl.getAttribLocation(blitProgram, "pos");
      gl.useProgram(blitProgram);
      gl.uniform1i(gl.getUniformLocation(blitProgram, "sampler"), 0);
      gl.useProgram(null);

      if (gl.createVertexArray) {
        context.defaultVao = gl.createVertexArray();
        gl.bindVertexArray(context.defaultVao);
        gl.enableVertexAttribArray(context.blitPosLoc);
        gl.bindVertexArray(null);
      }
    },

    resizeOffscreenFramebuffer: (context) => {
      var gl = context.GLctx;

      // Resize color buffer
      if (context.defaultColorTarget) {
        var prevTextureBinding = gl.getParameter(0x8069 /*GL_TEXTURE_BINDING_2D*/);
        gl.bindTexture(0xDE1 /*GL_TEXTURE_2D*/, context.defaultColorTarget);
        gl.texImage2D(0xDE1 /*GL_TEXTURE_2D*/, 0, 0x1908 /*GL_RGBA*/, gl.drawingBufferWidth, gl.drawingBufferHeight, 0, 0x1908 /*GL_RGBA*/, 0x1401 /*GL_UNSIGNED_BYTE*/, null);
        gl.bindTexture(0xDE1 /*GL_TEXTURE_2D*/, prevTextureBinding);
      }

      // Resize depth buffer
      if (context.defaultDepthTarget) {
        var prevRenderBufferBinding = gl.getParameter(0x8CA7 /*GL_RENDERBUFFER_BINDING*/);
        gl.bindRenderbuffer(0x8D41 /*GL_RENDERBUFFER*/, context.defaultDepthTarget);
        gl.renderbufferStorage(0x8D41 /*GL_RENDERBUFFER*/, 0x81A5 /*GL_DEPTH_COMPONENT16*/, gl.drawingBufferWidth, gl.drawingBufferHeight); // TODO: Read context creation parameters for what type of depth and stencil to use
        gl.bindRenderbuffer(0x8D41 /*GL_RENDERBUFFER*/, prevRenderBufferBinding);
      }
    },

    // Renders the contents of the offscreen render target onto the visible screen.
    blitOffscreenFramebuffer: (context) => {
      var gl = context.GLctx;

      var prevScissorTest = gl.getParameter(0xC11 /*GL_SCISSOR_TEST*/);
      if (prevScissorTest) gl.disable(0xC11 /*GL_SCISSOR_TEST*/);

      var prevFbo = gl.getParameter(0x8CA6 /*GL_FRAMEBUFFER_BINDING*/);

      if (gl.blitFramebuffer && !context.defaultFboForbidBlitFramebuffer) {
        gl.bindFramebuffer(0x8CA8 /*GL_READ_FRAMEBUFFER*/, context.defaultFbo);
        gl.bindFramebuffer(0x8CA9 /*GL_DRAW_FRAMEBUFFER*/, null);
        gl.blitFramebuffer(0, 0, gl.canvas.width, gl.canvas.height,
                           0, 0, gl.canvas.width, gl.canvas.height,
                           0x4000 /*GL_COLOR_BUFFER_BIT*/, 0x2600/*GL_NEAREST*/);
      }
      else
      {
        gl.bindFramebuffer(0x8D40 /*GL_FRAMEBUFFER*/, null);

        var prevProgram = gl.getParameter(0x8B8D /*GL_CURRENT_PROGRAM*/);
        gl.useProgram(context.blitProgram);
        // If prevProgram was already marked for deletion, then, since it was
        // still bound, it was not *actually* deleted. Binding a new program
        // just now, thus, deleted the old one. This makes it impossible to
        // restore. Hopefully the application didn't actually need it!
        if (!gl.isProgram(prevProgram)) prevProgram = null;

        var prevVB = gl.getParameter(0x8894 /*GL_ARRAY_BUFFER_BINDING*/);
        gl.bindBuffer(0x8892 /*GL_ARRAY_BUFFER*/, context.blitVB);

        var prevActiveTexture = gl.getParameter(0x84E0 /*GL_ACTIVE_TEXTURE*/);
        gl.activeTexture(0x84C0 /*GL_TEXTURE0*/);

        var prevTextureBinding = gl.getParameter(0x8069 /*GL_TEXTURE_BINDING_2D*/);
        gl.bindTexture(0xDE1 /*GL_TEXTURE_2D*/, context.defaultColorTarget);

        var prevBlend = gl.getParameter(0xBE2 /*GL_BLEND*/);
        if (prevBlend) gl.disable(0xBE2 /*GL_BLEND*/);

        var prevCullFace = gl.getParameter(0xB44 /*GL_CULL_FACE*/);
        if (prevCullFace) gl.disable(0xB44 /*GL_CULL_FACE*/);

        var prevDepthTest = gl.getParameter(0xB71 /*GL_DEPTH_TEST*/);
        if (prevDepthTest) gl.disable(0xB71 /*GL_DEPTH_TEST*/);

        var prevStencilTest = gl.getParameter(0xB90 /*GL_STENCIL_TEST*/);
        if (prevStencilTest) gl.disable(0xB90 /*GL_STENCIL_TEST*/);

        function draw() {
          gl.vertexAttribPointer(context.blitPosLoc, 2, 0x1406 /*GL_FLOAT*/, false, 0, 0);
          gl.drawArrays(5/*GL_TRIANGLE_STRIP*/, 0, 4);
        }

        if (context.defaultVao) {
          // WebGL 2 or OES_vertex_array_object
          var prevVAO = gl.getParameter(0x85B5 /*GL_VERTEX_ARRAY_BINDING*/);
          gl.bindVertexArray(context.defaultVao);
          draw();
          gl.bindVertexArray(prevVAO);
        } else {
          var prevVertexAttribPointer = {
            buffer: gl.getVertexAttrib(context.blitPosLoc, 0x889F /*GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING*/),
            size: gl.getVertexAttrib(context.blitPosLoc, 0x8623 /*GL_VERTEX_ATTRIB_ARRAY_SIZE*/),
            stride: gl.getVertexAttrib(context.blitPosLoc, 0x8624 /*GL_VERTEX_ATTRIB_ARRAY_STRIDE*/),
            type: gl.getVertexAttrib(context.blitPosLoc, 0x8625 /*GL_VERTEX_ATTRIB_ARRAY_TYPE*/),
            normalized: gl.getVertexAttrib(context.blitPosLoc, 0x886A /*GL_VERTEX_ATTRIB_ARRAY_NORMALIZED*/),
            pointer: gl.getVertexAttribOffset(context.blitPosLoc, 0x8645 /*GL_VERTEX_ATTRIB_ARRAY_POINTER*/),
          };
          var maxVertexAttribs = gl.getParameter(0x8869 /*GL_MAX_VERTEX_ATTRIBS*/);
          var prevVertexAttribEnables = [];
          for (var i = 0; i < maxVertexAttribs; ++i) {
            var prevEnabled = gl.getVertexAttrib(i, 0x8622 /*GL_VERTEX_ATTRIB_ARRAY_ENABLED*/);
            var wantEnabled = i == context.blitPosLoc;
            if (prevEnabled && !wantEnabled) {
              gl.disableVertexAttribArray(i);
            }
            if (!prevEnabled && wantEnabled) {
              gl.enableVertexAttribArray(i);
            }
            prevVertexAttribEnables[i] = prevEnabled;
          }

          draw();

          for (var i = 0; i < maxVertexAttribs; ++i) {
            var prevEnabled = prevVertexAttribEnables[i];
            var nowEnabled = i == context.blitPosLoc;
            if (prevEnabled && !nowEnabled) {
              gl.enableVertexAttribArray(i);
            }
            if (!prevEnabled && nowEnabled) {
              gl.disableVertexAttribArray(i);
            }
          }
          gl.bindBuffer(0x8892 /*GL_ARRAY_BUFFER*/, prevVertexAttribPointer.buffer);
          gl.vertexAttribPointer(context.blitPosLoc,
                                 prevVertexAttribPointer.size,
                                 prevVertexAttribPointer.type,
                                 prevVertexAttribPointer.normalized,
                                 prevVertexAttribPointer.stride,
                                 prevVertexAttribPointer.offset);
        }

        if (prevStencilTest) gl.enable(0xB90 /*GL_STENCIL_TEST*/);
        if (prevDepthTest) gl.enable(0xB71 /*GL_DEPTH_TEST*/);
        if (prevCullFace) gl.enable(0xB44 /*GL_CULL_FACE*/);
        if (prevBlend) gl.enable(0xBE2 /*GL_BLEND*/);

        gl.bindTexture(0xDE1 /*GL_TEXTURE_2D*/, prevTextureBinding);
        gl.activeTexture(prevActiveTexture);
        gl.bindBuffer(0x8892 /*GL_ARRAY_BUFFER*/, prevVB);
        gl.useProgram(prevProgram);
      }
      gl.bindFramebuffer(0x8D40 /*GL_FRAMEBUFFER*/, prevFbo);
      if (prevScissorTest) gl.enable(0xC11 /*GL_SCISSOR_TEST*/);
    },

    registerContext: (ctx, webGLContextAttributes) => {
      // without pthreads a context is just an integer ID
      var handle = GL.getNewId(GL.contexts);

      var context = {
        handle,
        attributes: webGLContextAttributes,
        version: webGLContextAttributes.majorVersion,
        GLctx: ctx
      };

      // Store the created context object so that we can access the context
      // given a canvas without having to pass the parameters again.
      if (ctx.canvas) ctx.canvas.GLctxObject = context;
      GL.contexts[handle] = context;
      if (typeof webGLContextAttributes.enableExtensionsByDefault == 'undefined' || webGLContextAttributes.enableExtensionsByDefault) {
        GL.initExtensions(context);
      }

      if (webGLContextAttributes.renderViaOffscreenBackBuffer) GL.createOffscreenFramebuffer(context);
      return handle;
    },

    makeContextCurrent: (contextHandle) => {

      // Active Emscripten GL layer context object.
      GL.currentContext = GL.contexts[contextHandle];
      // Active WebGL context object.
      Module['ctx'] = GLctx = GL.currentContext?.GLctx;
      return !(contextHandle && !GLctx);
    },

    getContext: (contextHandle) => {
      return GL.contexts[contextHandle];
    },

    deleteContext: (contextHandle) => {
      if (GL.currentContext === GL.contexts[contextHandle]) {
        GL.currentContext = null;
      }
      if (typeof JSEvents == 'object') {
        // Release all JS event handlers on the DOM element that the GL context is
        // associated with since the context is now deleted.
        JSEvents.removeAllHandlersOnTarget(GL.contexts[contextHandle].GLctx.canvas);
      }
      // Make sure the canvas object no longer refers to the context object so
      // there are no GC surprises.
      if (GL.contexts[contextHandle]?.GLctx.canvas) {
        GL.contexts[contextHandle].GLctx.canvas.GLctxObject = undefined;
      }
      GL.contexts[contextHandle] = null;
    },

    // In WebGL, extensions must be explicitly enabled to be active, see
    // http://www.khronos.org/registry/webgl/specs/latest/1.0/#5.14.14
    // In GLES2, all extensions are enabled by default without additional
    // operations. Init all extensions we need to give to GLES2 user code here,
    // so that GLES2 code can operate without changing behavior.
    initExtensions: (context) => {
      // If this function is called without a specific context object, init the
      // extensions of the currently active context.
      context ||= GL.currentContext;

      if (context.initExtensionsDone) return;
      context.initExtensionsDone = true;

      var GLctx = context.GLctx;

      // Detect the presence of a few extensions manually, since the GL interop
      // layer itself will need to know if they exist.

      // Extensions that are available in both WebGL 1 and WebGL 2
      webgl_enable_WEBGL_multi_draw(GLctx);
      webgl_enable_EXT_polygon_offset_clamp(GLctx);
      webgl_enable_EXT_clip_control(GLctx);
      webgl_enable_WEBGL_polygon_mode(GLctx);
      // Extensions that are available from WebGL >= 2 (no-op if called on a WebGL 1 context active)
      webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance(GLctx);
      webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance(GLctx);

      // On WebGL 2, EXT_disjoint_timer_query is replaced with an alternative
      // that's based on core APIs, and exposes only the queryCounterEXT()
      // entrypoint.
      if (context.version >= 2) {
        GLctx.disjointTimerQueryExt = GLctx.getExtension("EXT_disjoint_timer_query_webgl2");
      }

      // However, Firefox exposes the WebGL 1 version on WebGL 2 as well and
      // thus we look for the WebGL 1 version again if the WebGL 2 version
      // isn't present. https://bugzil.la/1328882
      if (context.version < 2 || !GLctx.disjointTimerQueryExt)
      {
        GLctx.disjointTimerQueryExt = GLctx.getExtension("EXT_disjoint_timer_query");
      }

      for (var ext of getEmscriptenSupportedExtensions(GLctx)) {
        // WEBGL_lose_context, WEBGL_debug_renderer_info and WEBGL_debug_shaders
        // are not enabled by default.
        if (!ext.includes('lose_context') && !ext.includes('debug')) {
          // Call .getExtension() to enable that extension permanently.
          GLctx.getExtension(ext);
        }
      }
    },

  },

  // Wrapper around GLctx.bufferSubData that can hangle both WebGL1 (which
  // requires new subarray on each call) and WebGL2 (which does not).
  // Argument ordering is a little strange here, since we want a default
  // for `src` is has to come last.
  $webglBufferSubData__internal: true,
  $webglBufferSubData: (target, offset, size, data, src = HEAPU8) => {
    GLctx.bufferSubData(target, offset, src.subarray(data, data + size));
  },

  $webglGetExtensions__internal: true,
  $webglGetExtensions__deps: ['$getEmscriptenSupportedExtensions'],
  $webglGetExtensions: () => {
    var exts = getEmscriptenSupportedExtensions(GLctx);
    exts = exts.concat(exts.map((e) => "GL_" + e));
    return exts;
  },

  glPixelStorei: (pname, param) => {
    if (pname == 3317) {
      GL.unpackAlignment = param;
    } else if (pname == 3314) {
      GL.unpackRowLength = param;
    }
    GLctx.pixelStorei(pname, param);
  },

  // The allocated strings are cached and never freed.
  glGetString__noleakcheck: true,
  glGetString__deps: ['$stringToNewUTF8', '$webglGetExtensions'],
  glGetString: (name_) => {
    var ret = GL.stringCache[name_];
    if (!ret) {
      switch (name_) {
        case 0x1F03 /* GL_EXTENSIONS */:
          ret = stringToNewUTF8(webglGetExtensions().join(' '));
          break;
        case 0x1F00 /* GL_VENDOR */:
        case 0x1F01 /* GL_RENDERER */:
        case 0x9245 /* UNMASKED_VENDOR_WEBGL */:
        case 0x9246 /* UNMASKED_RENDERER_WEBGL */:
          var s = GLctx.getParameter(name_);
          if (!s) {
            GL.recordError(0x500/*GL_INVALID_ENUM*/);
          }
          ret = s ? stringToNewUTF8(s) : 0;
          break;

        case 0x1F02 /* GL_VERSION */:
          var webGLVersion = GLctx.getParameter(0x1F02 /*GL_VERSION*/);
          // return GLES version string corresponding to the version of the WebGL context
          var glVersion = `OpenGL ES 2.0 (${webGLVersion})`;
          if (true) glVersion = `OpenGL ES 3.0 (${webGLVersion})`;
          ret = stringToNewUTF8(glVersion);
          break;
        case 0x8B8C /* GL_SHADING_LANGUAGE_VERSION */:
          var glslVersion = GLctx.getParameter(0x8B8C /*GL_SHADING_LANGUAGE_VERSION*/);
          // extract the version number 'N.M' from the string 'WebGL GLSL ES N.M ...'
          var ver_re = /^WebGL GLSL ES ([0-9]\.[0-9][0-9]?)(?:$| .*)/;
          var ver_num = glslVersion.match(ver_re);
          if (ver_num !== null) {
            if (ver_num[1].length == 3) ver_num[1] = ver_num[1] + '0'; // ensure minor version has 2 digits
            glslVersion = `OpenGL ES GLSL ES ${ver_num[1]} (${glslVersion})`;
          }
          ret = stringToNewUTF8(glslVersion);
          break;
        default:
          GL.recordError(0x500/*GL_INVALID_ENUM*/);
          // fall through
      }
      GL.stringCache[name_] = ret;
    }
    return ret;
  },

  $emscriptenWebGLGet__deps: ['$writeI53ToI64',
    '$webglGetExtensions', // For GL_NUM_EXTENSIONS
  ],
  $emscriptenWebGLGet: (name_, p, type) => {
    // Guard against user passing a null pointer.
    // Note that GLES2 spec does not say anything about how passing a null
    // pointer should be treated.  Testing on desktop core GL 3, the application
    // crashes on glGetIntegerv to a null pointer, but better to report an error
    // instead of doing anything random.
    if (!p) {
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    var ret = undefined;
    switch (name_) { // Handle a few trivial GLES values
      case 0x8DFA: // GL_SHADER_COMPILER
        ret = 1;
        break;
      case 0x8DF8: // GL_SHADER_BINARY_FORMATS
        if (type != 0 && type != 1) {
          GL.recordError(0x500); // GL_INVALID_ENUM
        }
        // Do not write anything to the out pointer, since no binary formats are
        // supported.
        return;
      case 0x87FE: // GL_NUM_PROGRAM_BINARY_FORMATS
      case 0x8DF9: // GL_NUM_SHADER_BINARY_FORMATS
        ret = 0;
        break;
      case 0x86A2: // GL_NUM_COMPRESSED_TEXTURE_FORMATS
        // WebGL doesn't have GL_NUM_COMPRESSED_TEXTURE_FORMATS (it's obsolete
        // since GL_COMPRESSED_TEXTURE_FORMATS returns a JS array that can be
        // queried for length), so implement it ourselves to allow C++ GLES2
        // code to get the length.
        var formats = GLctx.getParameter(0x86A3 /*GL_COMPRESSED_TEXTURE_FORMATS*/);
        ret = formats ? formats.length : 0;
        break;

      case 0x821D: // GL_NUM_EXTENSIONS
        if (GL.currentContext.version < 2) {
          // Calling GLES3/WebGL2 function with a GLES2/WebGL1 context
          GL.recordError(0x502 /* GL_INVALID_OPERATION */);
          return;
        }
        ret = webglGetExtensions().length;
        break;
      case 0x821B: // GL_MAJOR_VERSION
      case 0x821C: // GL_MINOR_VERSION
        if (GL.currentContext.version < 2) {
          GL.recordError(0x500); // GL_INVALID_ENUM
          return;
        }
        ret = name_ == 0x821B ? 3 : 0; // return version 3.0
        break;
    }

    if (ret === undefined) {
      var result = GLctx.getParameter(name_);
      switch (typeof result) {
        case "number":
          ret = result;
          break;
        case "boolean":
          ret = result ? 1 : 0;
          break;
        case "string":
          GL.recordError(0x500); // GL_INVALID_ENUM
          return;
        case "object":
          if (result === null) {
            // null is a valid result for some (e.g., which buffer is bound -
            // perhaps nothing is bound), but otherwise can mean an invalid
            // name_, which we need to report as an error
            switch (name_) {
              case 0x8894: // ARRAY_BUFFER_BINDING
              case 0x8B8D: // CURRENT_PROGRAM
              case 0x8895: // ELEMENT_ARRAY_BUFFER_BINDING
              case 0x8CA6: // FRAMEBUFFER_BINDING or DRAW_FRAMEBUFFER_BINDING
              case 0x8CA7: // RENDERBUFFER_BINDING
              case 0x8069: // TEXTURE_BINDING_2D
              case 0x85B5: // WebGL 2 GL_VERTEX_ARRAY_BINDING, or WebGL 1 extension OES_vertex_array_object GL_VERTEX_ARRAY_BINDING_OES
              case 0x8F36: // COPY_READ_BUFFER_BINDING or COPY_READ_BUFFER
              case 0x8F37: // COPY_WRITE_BUFFER_BINDING or COPY_WRITE_BUFFER
              case 0x88ED: // PIXEL_PACK_BUFFER_BINDING
              case 0x88EF: // PIXEL_UNPACK_BUFFER_BINDING
              case 0x8CAA: // READ_FRAMEBUFFER_BINDING
              case 0x8919: // SAMPLER_BINDING
              case 0x8C1D: // TEXTURE_BINDING_2D_ARRAY
              case 0x806A: // TEXTURE_BINDING_3D
              case 0x8E25: // TRANSFORM_FEEDBACK_BINDING
              case 0x8C8F: // TRANSFORM_FEEDBACK_BUFFER_BINDING
              case 0x8A28: // UNIFORM_BUFFER_BINDING
              case 0x8514: { // TEXTURE_BINDING_CUBE_MAP
                ret = 0;
                break;
              }
              default: {
                GL.recordError(0x500); // GL_INVALID_ENUM
                return;
              }
            }
          } else if (result instanceof Float32Array ||
                     result instanceof Uint32Array ||
                     result instanceof Int32Array ||
                     result instanceof Array) {
            for (var i = 0; i < result.length; ++i) {
              switch (type) {
                case 0: HEAP32[(((p)+(i*4))>>2)] = result[i]; break;
                case 2: HEAPF32[(((p)+(i*4))>>2)] = result[i]; break;
                case 4: HEAP8[(p)+(i)] = result[i] ? 1 : 0; break;
              }
            }
            return;
          } else {
            try {
              ret = result.name | 0;
            } catch(e) {
              GL.recordError(0x500); // GL_INVALID_ENUM
              err(`GL_INVALID_ENUM in glGet${type}v: Unknown object returned from WebGL getParameter(${name_})! (error: ${e})`);
              return;
            }
          }
          break;
        default:
          GL.recordError(0x500); // GL_INVALID_ENUM
          err(`GL_INVALID_ENUM in glGet${type}v: Native code calling glGet${type}v(${name_}) and it returns ${result} of type ${typeof(result)}!`);
          return;
      }
    }

    switch (type) {
      case 1: writeI53ToI64(p, ret); break;
      case 0: HEAP32[((p)>>2)] = ret; break;
      case 2:   HEAPF32[((p)>>2)] = ret; break;
      case 4: HEAP8[p] = ret ? 1 : 0; break;
    }
  },

  glGetIntegerv__deps: ['$emscriptenWebGLGet'],
  glGetIntegerv: (name_, p) => emscriptenWebGLGet(name_, p, 0),

  glGetFloatv__deps: ['$emscriptenWebGLGet'],
  glGetFloatv: (name_, p) => emscriptenWebGLGet(name_, p, 2),

  glGetBooleanv__deps: ['$emscriptenWebGLGet'],
  glGetBooleanv: (name_, p) => emscriptenWebGLGet(name_, p, 4),

  glDeleteTextures: (n, textures) => {
    for (var i = 0; i < n; i++) {
      var id = HEAP32[(((textures)+(i*4))>>2)];
      var texture = GL.textures[id];
      // GL spec: "glDeleteTextures silently ignores 0s and names that do not
      // correspond to existing textures".
      if (!texture) continue;
      GLctx.deleteTexture(texture);
      texture.name = 0;
      GL.textures[id] = null;
    }
  },

  glCompressedTexImage2D: (target, level, internalFormat, width, height, border, imageSize, data) => {
    // `data` may be null here, which means "allocate uninitialized space but
    // don't upload" in GLES parlance, but `compressedTexImage2D` requires the
    // final data parameter, so we simply pass a heap view starting at zero
    // effectively uploading whatever happens to be near address zero.  See
    // https://github.com/emscripten-core/emscripten/issues/19300.
    if (true) {
      if (GLctx.currentPixelUnpackBufferBinding || !imageSize) {
        GLctx.compressedTexImage2D(target, level, internalFormat, width, height, border, imageSize, data);
        return;
      }
    }
    GLctx.compressedTexImage2D(target, level, internalFormat, width, height, border, HEAPU8.subarray(data, data + imageSize));
  },

  glCompressedTexSubImage2D: (target, level, xoffset, yoffset, width, height, format, imageSize, data) => {
    if (true) {
      if (GLctx.currentPixelUnpackBufferBinding || !imageSize) {
        GLctx.compressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
        return;
      }
    }
    GLctx.compressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, HEAPU8.subarray(data, data + imageSize));
  },

  $computeUnpackAlignedImageSize: (width, height, sizePerPixel) => {
    function roundedToNextMultipleOf(x, y) {
      return (x + y - 1) & -y;
    }
    var plainRowSize = (GL.unpackRowLength || width) * sizePerPixel;
    var alignedRowSize = roundedToNextMultipleOf(plainRowSize, GL.unpackAlignment);
    return height * alignedRowSize;
  },

  $colorChannelsInGlTextureFormat: (format) => {
    // Micro-optimizations for size: map format to size by subtracting smallest
    // enum value (0x1902) from all values first.  Also omit the most common
    // size value (1) from the list, which is assumed by formats not on the
    // list.
    var colorChannels = {
      // 0x1902 /* GL_DEPTH_COMPONENT */ - 0x1902: 1,
      // 0x1906 /* GL_ALPHA */ - 0x1902: 1,
      5: 3,
      6: 4,
      // 0x1909 /* GL_LUMINANCE */ - 0x1902: 1,
      8: 2,
      29502: 3,
      29504: 4,
      // 0x1903 /* GL_RED */ - 0x1902: 1,
      26917: 2,
      26918: 2,
      // 0x8D94 /* GL_RED_INTEGER */ - 0x1902: 1,
      29846: 3,
      29847: 4
    };
    return colorChannels[format - 0x1902]||1;
  },

  $emscriptenWebGLGetTexPixelData__deps: ['$computeUnpackAlignedImageSize', '$colorChannelsInGlTextureFormat', '$heapObjectForWebGLType', '$toTypedArrayIndex'],
  $emscriptenWebGLGetTexPixelData: (type, format, width, height, pixels) => {
    var heap = heapObjectForWebGLType(type);
    var sizePerPixel = colorChannelsInGlTextureFormat(format) * heap.BYTES_PER_ELEMENT;
    var bytes = computeUnpackAlignedImageSize(width, height, sizePerPixel);
    return heap.subarray(toTypedArrayIndex(pixels, heap), toTypedArrayIndex(pixels + bytes, heap));
  },

  glTexImage2D__deps: ['$emscriptenWebGLGetTexPixelData'
                       , '$heapObjectForWebGLType', '$toTypedArrayIndex'
  ],
  glTexImage2D: (target, level, internalFormat, width, height, border, format, type, pixels) => {
    if (true) {
      if (GLctx.currentPixelUnpackBufferBinding) {
        GLctx.texImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
        return;
      }
    }
    var pixelData = pixels ? emscriptenWebGLGetTexPixelData(type, format, width, height, pixels) : null;
    GLctx.texImage2D(target, level, internalFormat, width, height, border, format, type, pixelData);
  },

  glTexSubImage2D__deps: ['$emscriptenWebGLGetTexPixelData'
                          , '$heapObjectForWebGLType', '$toTypedArrayIndex'
  ],
  glTexSubImage2D: (target, level, xoffset, yoffset, width, height, format, type, pixels) => {
    if (true) {
      if (GLctx.currentPixelUnpackBufferBinding) {
        GLctx.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
        return;
      }
    }
    var pixelData = pixels ? emscriptenWebGLGetTexPixelData(type, format, width, height, pixels) : null;
    GLctx.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixelData);
  },

  glReadPixels__deps: [
    '$emscriptenWebGLGetTexPixelData',
    '$heapObjectForWebGLType', '$toTypedArrayIndex',
  ],
  glReadPixels: (x, y, width, height, format, type, pixels) => {
    if (true) {
      if (GLctx.currentPixelPackBufferBinding) {
        GLctx.readPixels(x, y, width, height, format, type, pixels);
        return;
      }
    }
    var pixelData = emscriptenWebGLGetTexPixelData(type, format, width, height, pixels);
    if (!pixelData) {
      GL.recordError(0x500/*GL_INVALID_ENUM*/);
      return;
    }
    GLctx.readPixels(x, y, width, height, format, type, pixelData);
  },

  glBindTexture: (target, texture) => {
    GLctx.bindTexture(target, GL.textures[texture]);
  },

  glGetTexParameterfv: (target, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null
      // pointer. Since calling this function does not make sense if p == null,
      // issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAPF32[((params)>>2)] = GLctx.getTexParameter(target, pname);
  },

  glGetTexParameteriv: (target, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null
      // pointer. Since calling this function does not make sense if p == null,
      // issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAP32[((params)>>2)] = GLctx.getTexParameter(target, pname);
  },

  glTexParameterfv: (target, pname, params) => {
    var param = HEAPF32[((params)>>2)];
    GLctx.texParameterf(target, pname, param);
  },

  glTexParameteriv: (target, pname, params) => {
    var param = HEAP32[((params)>>2)];
    GLctx.texParameteri(target, pname, param);
  },

  glIsTexture: (id) => {
    var texture = GL.textures[id];
    if (!texture) return 0;
    return GLctx.isTexture(texture);
  },

  glGenBuffers: (n, buffers) => {
    GL.genObject(n, buffers, 'createBuffer', GL.buffers
      );
  },

  glGenTextures: (n, textures) => {
    GL.genObject(n, textures, 'createTexture', GL.textures
      );
  },

  glDeleteBuffers: (n, buffers) => {
    for (var i = 0; i < n; i++) {
      var id = HEAP32[(((buffers)+(i*4))>>2)];
      var buffer = GL.buffers[id];

      // From spec: "glDeleteBuffers silently ignores 0's and names that do not
      // correspond to existing buffer objects."
      if (!buffer) continue;

      GLctx.deleteBuffer(buffer);
      buffer.name = 0;
      GL.buffers[id] = null;

      if (id == GLctx.currentPixelPackBufferBinding) GLctx.currentPixelPackBufferBinding = 0;
      if (id == GLctx.currentPixelUnpackBufferBinding) GLctx.currentPixelUnpackBufferBinding = 0;
    }
  },

  glGetBufferParameteriv: (target, value, data) => {
    if (!data) {
      // GLES2 specification does not specify how to behave if data is a null
      // pointer. Since calling this function does not make sense if data ==
      // null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAP32[((data)>>2)] = GLctx.getBufferParameter(target, value);
  },

  glBufferData: (target, size, data, usage) => {

    // N.b. here first form specifies a heap subarray, second form an integer
    // size, so the ?: code here is polymorphic. It is advised to avoid
    // randomly mixing both uses in calling code, to avoid any potential JS
    // engine JIT issues.
    GLctx.bufferData(target, data ? HEAPU8.subarray(data, data+size) : size, usage);
  },

  // This cannot be simple alias because under wasm64 we need to be able modify
  // the function at compile time to provide automatically marshal of the pointer arguments.
  glBufferSubData__deps: ['$webglBufferSubData'],
  glBufferSubData: (target, offset, size, data) => webglBufferSubData(target, offset, size, data),

  // Queries EXT
  glGenQueriesEXT__sig: 'vip',
  glGenQueriesEXT: (n, ids) => {
    for (var i = 0; i < n; i++) {
      var query = GLctx.disjointTimerQueryExt['createQueryEXT']();
      if (!query) {
        GL.recordError(0x502 /* GL_INVALID_OPERATION */);
        while (i < n) HEAP32[(((ids)+(i++*4))>>2)] = 0;
        return;
      }
      var id = GL.getNewId(GL.queries);
      query.name = id;
      GL.queries[id] = query;
      HEAP32[(((ids)+(i*4))>>2)] = id;
    }
  },

  glDeleteQueriesEXT__sig: 'vip',
  glDeleteQueriesEXT: (n, ids) => {
    for (var i = 0; i < n; i++) {
      var id = HEAP32[(((ids)+(i*4))>>2)];
      var query = GL.queries[id];
      if (!query) continue; // GL spec: "unused names in ids are ignored, as is the name zero."
      GLctx.disjointTimerQueryExt['deleteQueryEXT'](query);
      GL.queries[id] = null;
    }
  },

  glIsQueryEXT__sig: 'ii',
  glIsQueryEXT: (id) => {
    var query = GL.queries[id];
    if (!query) return 0;
    return GLctx.disjointTimerQueryExt['isQueryEXT'](query);
  },

  glBeginQueryEXT__sig: 'vii',
  glBeginQueryEXT: (target, id) => {
    GLctx.disjointTimerQueryExt['beginQueryEXT'](target, GL.queries[id]);
  },

  glEndQueryEXT__sig: 'vi',
  glEndQueryEXT: (target) => {
    GLctx.disjointTimerQueryExt['endQueryEXT'](target);
  },

  // This one is either from EXT_disjoint_timer_query on WebGL 1 (taking a
  // WebGLTimerQueryEXT) or from EXT_disjoint_timer_query_webgl2 (taking a
  // WebGLQuery)
  glQueryCounterEXT__sig: 'vii',
  glQueryCounterEXT: (id, target) => {
    GLctx.disjointTimerQueryExt['queryCounterEXT'](GL.queries[id], target);
  },

  glGetQueryivEXT__sig: 'viip',
  glGetQueryivEXT: (target, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if p == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAP32[((params)>>2)] = GLctx.disjointTimerQueryExt['getQueryEXT'](target, pname);
  },

  glGetQueryObjectivEXT__sig: 'viip',
  glGetQueryObjectivEXT: (id, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if p == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    var query = GL.queries[id];
    var param = GLctx.disjointTimerQueryExt['getQueryObjectEXT'](query, pname);
    var ret;
    if (typeof param == 'boolean') {
      ret = param ? 1 : 0;
    } else {
      ret = param;
    }
    HEAP32[((params)>>2)] = ret;
  },
  glGetQueryObjectuivEXT: 'glGetQueryObjectivEXT',

  glGetQueryObjecti64vEXT__sig: 'viip',
  glGetQueryObjecti64vEXT__deps: ['$writeI53ToI64'],
  glGetQueryObjecti64vEXT: (id, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if p == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    var query = GL.queries[id];
    var param;
    if (GL.currentContext.version < 2)
    {
      param = GLctx.disjointTimerQueryExt['getQueryObjectEXT'](query, pname);
    }
    else {
      param = GLctx.getQueryParameter(query, pname);
    }
    var ret;
    if (typeof param == 'boolean') {
      ret = param ? 1 : 0;
    } else {
      ret = param;
    }
    writeI53ToI64(params, ret);
  },
  glGetQueryObjectui64vEXT: 'glGetQueryObjecti64vEXT',

  glIsBuffer: (buffer) => {
    var b = GL.buffers[buffer];
    if (!b) return 0;
    return GLctx.isBuffer(b);
  },

  glGenRenderbuffers: (n, renderbuffers) => {
    GL.genObject(n, renderbuffers, 'createRenderbuffer', GL.renderbuffers
      );
  },

  glDeleteRenderbuffers: (n, renderbuffers) => {
    for (var i = 0; i < n; i++) {
      var id = HEAP32[(((renderbuffers)+(i*4))>>2)];
      var renderbuffer = GL.renderbuffers[id];
      if (!renderbuffer) continue; // GL spec: "glDeleteRenderbuffers silently ignores 0s and names that do not correspond to existing renderbuffer objects".
      GLctx.deleteRenderbuffer(renderbuffer);
      renderbuffer.name = 0;
      GL.renderbuffers[id] = null;
    }
  },

  glBindRenderbuffer: (target, renderbuffer) => {
    GLctx.bindRenderbuffer(target, GL.renderbuffers[renderbuffer]);
  },

  glGetRenderbufferParameteriv: (target, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if params == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAP32[((params)>>2)] = GLctx.getRenderbufferParameter(target, pname);
  },

  glIsRenderbuffer: (renderbuffer) => {
    var rb = GL.renderbuffers[renderbuffer];
    if (!rb) return 0;
    return GLctx.isRenderbuffer(rb);
  },

  // This function intentionally assigns `HEAP32[x] = someBoolean;` Don't let
  // Closure mind about that.
  $emscriptenWebGLGetUniform__docs: '/** @suppress{checkTypes} */',
  $emscriptenWebGLGetUniform__deps: ['$webglGetProgramUniformLocation', '$webglPrepareUniformLocationsBeforeFirstUse'],
  $emscriptenWebGLGetUniform: (program, location, params, type) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null
      // pointer. Since calling this function does not make sense if params ==
      // null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    program = GL.programs[program];
    webglPrepareUniformLocationsBeforeFirstUse(program);
    var data = GLctx.getUniform(program, webglGetProgramUniformLocation(program, location));
    if (typeof data == 'number' || typeof data == 'boolean') {
      switch (type) {
        case 0: HEAP32[((params)>>2)] = data; break;
        case 2: HEAPF32[((params)>>2)] = data; break;
      }
    } else {
      for (var i = 0; i < data.length; i++) {
        switch (type) {
          case 0: HEAP32[(((params)+(i*4))>>2)] = data[i]; break;
          case 2: HEAPF32[(((params)+(i*4))>>2)] = data[i]; break;
        }
      }
    }
  },

  glGetUniformfv__deps: ['$emscriptenWebGLGetUniform'],
  glGetUniformfv: (program, location, params) => {
    emscriptenWebGLGetUniform(program, location, params, 2);
  },

  glGetUniformiv__deps: ['$emscriptenWebGLGetUniform'],
  glGetUniformiv: (program, location, params) => {
    emscriptenWebGLGetUniform(program, location, params, 0);
  },

  // Returns the WebGLUniformLocation object corresponding to the location index
  // integer on the currently active shader in this GL context.
  $webglGetProgramUniformLocation__deps: ['$webglPrepareUniformLocationsBeforeFirstUse'],
  $webglGetProgramUniformLocation: (program, location) => {

    if (program) {
      var webglLoc = program.uniformLocsById[location];
      // program.uniformLocsById[location] stores either an integer, or a
      // WebGLUniformLocation.
      // If an integer, we have not yet bound the location, so do it now. The
      // integer value specifies the array index we should bind to.
      if (typeof webglLoc == 'number') {
        program.uniformLocsById[location] = webglLoc = GLctx.getUniformLocation(program, program.uniformArrayNamesById[location] + (webglLoc > 0 ? `[${webglLoc}]` : ''));
      }
      // Else an already cached WebGLUniformLocation, return it.
      return webglLoc;
    } else {
      GL.recordError(0x502/*GL_INVALID_OPERATION*/);
    }
  },

  $webglGetUniformLocation__deps: ['$webglGetProgramUniformLocation'],
  $webglGetUniformLocation: (location) => {

    return webglGetProgramUniformLocation(GLctx.currentProgram, location);
  },

  $webglPrepareUniformLocationsBeforeFirstUse__deps: ['$webglGetLeftBracePos'],
  $webglPrepareUniformLocationsBeforeFirstUse: (program) => {
    var uniformLocsById = program.uniformLocsById, // Maps GLuint -> WebGLUniformLocation
      uniformSizeAndIdsByName = program.uniformSizeAndIdsByName, // Maps name -> [uniform array length, GLuint]
      i, j;

    // On the first time invocation of glGetUniformLocation on this shader program:
    // initialize cache data structures and discover which uniforms are arrays.
    if (!uniformLocsById) {
      // maps GLint integer locations to WebGLUniformLocations
      program.uniformLocsById = uniformLocsById = {};
      // maps integer locations back to uniform name strings, so that we can lazily fetch uniform array locations
      program.uniformArrayNamesById = {};

      var numActiveUniforms = GLctx.getProgramParameter(program, 0x8B86/*GL_ACTIVE_UNIFORMS*/);
      for (i = 0; i < numActiveUniforms; ++i) {
        var u = GLctx.getActiveUniform(program, i);
        var nm = u.name;
        var sz = u.size;
        var lb = webglGetLeftBracePos(nm);
        var arrayName = lb > 0 ? nm.slice(0, lb) : nm;

        // Assign a new location.
        var id = program.uniformIdCounter;
        program.uniformIdCounter += sz;
        // Eagerly get the location of the uniformArray[0] base element.
        // The remaining indices >0 will be left for lazy evaluation to
        // improve performance. Those may never be needed to fetch, if the
        // application fills arrays always in full starting from the first
        // element of the array.
        uniformSizeAndIdsByName[arrayName] = [sz, id];

        // Store placeholder integers in place that highlight that these
        // >0 index locations are array indices pending population.
        for (j = 0; j < sz; ++j) {
          uniformLocsById[id] = j;
          program.uniformArrayNamesById[id++] = arrayName;
        }
      }
    }
  },

  // Returns the index of '[' character in a uniform that represents an array
  // of uniforms (e.g. colors[10])
  // Closure does counterproductive inlining:
  // https://github.com/google/closure-compiler/issues/3203, so prevent inlining
  // manually.
  $webglGetLeftBracePos__docs: '/** @noinline */',
  $webglGetLeftBracePos: (name) => name.slice(-1) == ']' && name.lastIndexOf('['),

  glGetUniformLocation__deps: ['$jstoi_q', '$webglPrepareUniformLocationsBeforeFirstUse', '$webglGetLeftBracePos'],
  glGetUniformLocation: (program, name) => {

    name = UTF8ToString(name);

    if (program = GL.programs[program]) {
      webglPrepareUniformLocationsBeforeFirstUse(program);
      var uniformLocsById = program.uniformLocsById; // Maps GLuint -> WebGLUniformLocation
      var arrayIndex = 0;
      var uniformBaseName = name;

      // Invariant: when populating integer IDs for uniform locations, we must
      // maintain the precondition that arrays reside in contiguous addresses,
      // i.e. for a 'vec4 colors[10];', colors[4] must be at location
      // colors[0]+4.  However, user might call glGetUniformLocation(program,
      // "colors") for an array, so we cannot discover based on the user input
      // arguments whether the uniform we are dealing with is an array. The only
      // way to discover which uniforms are arrays is to enumerate over all the
      // active uniforms in the program.
      var leftBrace = webglGetLeftBracePos(name);

      // If user passed an array accessor "[index]", parse the array index off the accessor.
      if (leftBrace > 0) {
        arrayIndex = jstoi_q(name.slice(leftBrace + 1)) >>> 0; // "index]", coerce parseInt(']') with >>>0 to treat "foo[]" as "foo[0]" and foo[-1] as unsigned out-of-bounds.
        uniformBaseName = name.slice(0, leftBrace);
      }

      // Have we cached the location of this uniform before?
      // A pair [array length, GLint of the uniform location]
      var sizeAndId = program.uniformSizeAndIdsByName[uniformBaseName];

      // If a uniform with this name exists, and if its index is within the
      // array limits (if it's even an array), query the WebGLlocation, or
      // return an existing cached location.
      if (sizeAndId && arrayIndex < sizeAndId[0]) {
        arrayIndex += sizeAndId[1]; // Add the base location of the uniform to the array index offset.
        if ((uniformLocsById[arrayIndex] = uniformLocsById[arrayIndex] || GLctx.getUniformLocation(program, name))) {
          return arrayIndex;
        }
      }
    }
    else {
      // N.b. we are currently unable to distinguish between GL program IDs that
      // never existed vs GL program IDs that have been deleted, so report
      // GL_INVALID_VALUE in both cases.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
    }
    return -1;
  },

  // This function intentionally assigns `HEAP32[x] = someBoolean;` Don't let
  // Closure mind about that.
  $emscriptenWebGLGetVertexAttrib__docs: '/** @suppress{checkTypes} */',
  $emscriptenWebGLGetVertexAttrib: (index, pname, params, type) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null
      // pointer. Since calling this function does not make sense if params ==
      // null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    var data = GLctx.getVertexAttrib(index, pname);
    if (pname == 0x889F/*VERTEX_ATTRIB_ARRAY_BUFFER_BINDING*/) {
      HEAP32[((params)>>2)] = data && data["name"];
    } else if (typeof data == 'number' || typeof data == 'boolean') {
      switch (type) {
        case 0: HEAP32[((params)>>2)] = data; break;
        case 2: HEAPF32[((params)>>2)] = data; break;
        case 5: HEAP32[((params)>>2)] = Math.fround(data); break;
      }
    } else {
      for (var i = 0; i < data.length; i++) {
        switch (type) {
          case 0: HEAP32[(((params)+(i*4))>>2)] = data[i]; break;
          case 2: HEAPF32[(((params)+(i*4))>>2)] = data[i]; break;
          case 5: HEAP32[(((params)+(i*4))>>2)] = Math.fround(data[i]); break;
        }
      }
    }
  },

  glGetVertexAttribfv__deps: ['$emscriptenWebGLGetVertexAttrib'],
  glGetVertexAttribfv: (index, pname, params) => {
    // N.B. This function may only be called if the vertex attribute was
    // specified using the function glVertexAttrib*f(), otherwise the results
    // are undefined. (GLES3 spec 6.1.12)
    emscriptenWebGLGetVertexAttrib(index, pname, params, 2);
  },

  glGetVertexAttribiv__deps: ['$emscriptenWebGLGetVertexAttrib'],
  glGetVertexAttribiv: (index, pname, params) => {
    // N.B. This function may only be called if the vertex attribute was
    // specified using the function glVertexAttrib*f(), otherwise the results
    // are undefined. (GLES3 spec 6.1.12)
    emscriptenWebGLGetVertexAttrib(index, pname, params, 5);
  },

  glGetVertexAttribPointerv: (index, pname, pointer) => {
    if (!pointer) {
      // GLES2 specification does not specify how to behave if pointer is a null
      // pointer. Since calling this function does not make sense if pointer ==
      // null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAP32[((pointer)>>2)] = GLctx.getVertexAttribOffset(index, pname);
  },

  glUniform1f__deps: ['$webglGetUniformLocation'],
  glUniform1f: (location, v0) => {
    GLctx.uniform1f(webglGetUniformLocation(location), v0);
  },

  glUniform2f__deps: ['$webglGetUniformLocation'],
  glUniform2f: (location, v0, v1) => {
    GLctx.uniform2f(webglGetUniformLocation(location), v0, v1);
  },

  glUniform3f__deps: ['$webglGetUniformLocation'],
  glUniform3f: (location, v0, v1, v2) => {
    GLctx.uniform3f(webglGetUniformLocation(location), v0, v1, v2);
  },

  glUniform4f__deps: ['$webglGetUniformLocation'],
  glUniform4f: (location, v0, v1, v2, v3) => {
    GLctx.uniform4f(webglGetUniformLocation(location), v0, v1, v2, v3);
  },

  glUniform1i__deps: ['$webglGetUniformLocation'],
  glUniform1i: (location, v0) => {
    GLctx.uniform1i(webglGetUniformLocation(location), v0);
  },

  glUniform2i__deps: ['$webglGetUniformLocation'],
  glUniform2i: (location, v0, v1) => {
    GLctx.uniform2i(webglGetUniformLocation(location), v0, v1);
  },

  glUniform3i__deps: ['$webglGetUniformLocation'],
  glUniform3i: (location, v0, v1, v2) => {
    GLctx.uniform3i(webglGetUniformLocation(location), v0, v1, v2);
  },

  glUniform4i__deps: ['$webglGetUniformLocation'],
  glUniform4i: (location, v0, v1, v2, v3) => {
    GLctx.uniform4i(webglGetUniformLocation(location), v0, v1, v2, v3);
  },

  glUniform1iv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLIntBuffers'
  ],
  glUniform1iv: (location, count, value) => {

    if (count <= 288) {
      // avoid allocation when uploading few enough uniforms
      var view = miniTempWebGLIntBuffers[count];
      for (var i = 0; i < count; ++i) {
        view[i] = HEAP32[(((value)+(4*i))>>2)];
      }
    } else
    {
      var view = HEAP32.subarray((((value)>>2)), ((value+count*4)>>2));
    }
    GLctx.uniform1iv(webglGetUniformLocation(location), view);
  },

  glUniform2iv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLIntBuffers'
  ],
  glUniform2iv: (location, count, value) => {

    if (count <= 144) {
      // avoid allocation when uploading few enough uniforms
      count *= 2;
      var view = miniTempWebGLIntBuffers[count];
      for (var i = 0; i < count; i += 2) {
        view[i] = HEAP32[(((value)+(4*i))>>2)];
        view[i+1] = HEAP32[(((value)+(4*i+4))>>2)];
      }
    } else
    {
      var view = HEAP32.subarray((((value)>>2)), ((value+count*8)>>2));
    }
    GLctx.uniform2iv(webglGetUniformLocation(location), view);
  },

  glUniform3iv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLIntBuffers'
  ],
  glUniform3iv: (location, count, value) => {

    if (count <= 96) {
      // avoid allocation when uploading few enough uniforms
      count *= 3;
      var view = miniTempWebGLIntBuffers[count];
      for (var i = 0; i < count; i += 3) {
        view[i] = HEAP32[(((value)+(4*i))>>2)];
        view[i+1] = HEAP32[(((value)+(4*i+4))>>2)];
        view[i+2] = HEAP32[(((value)+(4*i+8))>>2)];
      }
    } else
    {
      var view = HEAP32.subarray((((value)>>2)), ((value+count*12)>>2));
    }
    GLctx.uniform3iv(webglGetUniformLocation(location), view);
  },

  glUniform4iv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLIntBuffers'
  ],
  glUniform4iv: (location, count, value) => {

    if (count <= 72) {
      // avoid allocation when uploading few enough uniforms
      count *= 4;
      var view = miniTempWebGLIntBuffers[count];
      for (var i = 0; i < count; i += 4) {
        view[i] = HEAP32[(((value)+(4*i))>>2)];
        view[i+1] = HEAP32[(((value)+(4*i+4))>>2)];
        view[i+2] = HEAP32[(((value)+(4*i+8))>>2)];
        view[i+3] = HEAP32[(((value)+(4*i+12))>>2)];
      }
    } else
    {
      var view = HEAP32.subarray((((value)>>2)), ((value+count*16)>>2));
    }
    GLctx.uniform4iv(webglGetUniformLocation(location), view);
  },

  glUniform1fv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLFloatBuffers'
  ],
  glUniform1fv: (location, count, value) => {

    if (count <= 288) {
      // avoid allocation when uploading few enough uniforms
      var view = miniTempWebGLFloatBuffers[count];
      for (var i = 0; i < count; ++i) {
        view[i] = HEAPF32[(((value)+(4*i))>>2)];
      }
    } else
    {
      var view = HEAPF32.subarray((((value)>>2)), ((value+count*4)>>2));
    }
    GLctx.uniform1fv(webglGetUniformLocation(location), view);
  },

  glUniform2fv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLFloatBuffers'
  ],
  glUniform2fv: (location, count, value) => {

    if (count <= 144) {
      // avoid allocation when uploading few enough uniforms
      count *= 2;
      var view = miniTempWebGLFloatBuffers[count];
      for (var i = 0; i < count; i += 2) {
        view[i] = HEAPF32[(((value)+(4*i))>>2)];
        view[i+1] = HEAPF32[(((value)+(4*i+4))>>2)];
      }
    } else
    {
      var view = HEAPF32.subarray((((value)>>2)), ((value+count*8)>>2));
    }
    GLctx.uniform2fv(webglGetUniformLocation(location), view);
  },

  glUniform3fv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLFloatBuffers'
  ],
  glUniform3fv: (location, count, value) => {

    if (count <= 96) {
      // avoid allocation when uploading few enough uniforms
      count *= 3;
      var view = miniTempWebGLFloatBuffers[count];
      for (var i = 0; i < count; i += 3) {
        view[i] = HEAPF32[(((value)+(4*i))>>2)];
        view[i+1] = HEAPF32[(((value)+(4*i+4))>>2)];
        view[i+2] = HEAPF32[(((value)+(4*i+8))>>2)];
      }
    } else
    {
      var view = HEAPF32.subarray((((value)>>2)), ((value+count*12)>>2));
    }
    GLctx.uniform3fv(webglGetUniformLocation(location), view);
  },

  glUniform4fv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLFloatBuffers'
  ],
  glUniform4fv: (location, count, value) => {

    if (count <= 72) {
      // avoid allocation when uploading few enough uniforms
      var view = miniTempWebGLFloatBuffers[4*count];
      // hoist the heap out of the loop for size and for pthreads+growth.
      var heap = HEAPF32;
      value = ((value)>>2);
      count *= 4;
      for (var i = 0; i < count; i += 4) {
        var dst = value + i;
        view[i] = heap[dst];
        view[i + 1] = heap[dst + 1];
        view[i + 2] = heap[dst + 2];
        view[i + 3] = heap[dst + 3];
      }
    } else
    {
      var view = HEAPF32.subarray((((value)>>2)), ((value+count*16)>>2));
    }
    GLctx.uniform4fv(webglGetUniformLocation(location), view);
  },

  glUniformMatrix2fv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLFloatBuffers'
  ],
  glUniformMatrix2fv: (location, count, transpose, value) => {

    if (count <= 72) {
      // avoid allocation when uploading few enough uniforms
      count *= 4;
      var view = miniTempWebGLFloatBuffers[count];
      for (var i = 0; i < count; i += 4) {
        view[i] = HEAPF32[(((value)+(4*i))>>2)];
        view[i+1] = HEAPF32[(((value)+(4*i+4))>>2)];
        view[i+2] = HEAPF32[(((value)+(4*i+8))>>2)];
        view[i+3] = HEAPF32[(((value)+(4*i+12))>>2)];
      }
    } else
    {
      var view = HEAPF32.subarray((((value)>>2)), ((value+count*16)>>2));
    }
    GLctx.uniformMatrix2fv(webglGetUniformLocation(location), !!transpose, view);
  },

  glUniformMatrix3fv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLFloatBuffers'
  ],
  glUniformMatrix3fv: (location, count, transpose, value) => {

    if (count <= 32) {
      // avoid allocation when uploading few enough uniforms
      count *= 9;
      var view = miniTempWebGLFloatBuffers[count];
      for (var i = 0; i < count; i += 9) {
        view[i] = HEAPF32[(((value)+(4*i))>>2)];
        view[i+1] = HEAPF32[(((value)+(4*i+4))>>2)];
        view[i+2] = HEAPF32[(((value)+(4*i+8))>>2)];
        view[i+3] = HEAPF32[(((value)+(4*i+12))>>2)];
        view[i+4] = HEAPF32[(((value)+(4*i+16))>>2)];
        view[i+5] = HEAPF32[(((value)+(4*i+20))>>2)];
        view[i+6] = HEAPF32[(((value)+(4*i+24))>>2)];
        view[i+7] = HEAPF32[(((value)+(4*i+28))>>2)];
        view[i+8] = HEAPF32[(((value)+(4*i+32))>>2)];
      }
    } else
    {
      var view = HEAPF32.subarray((((value)>>2)), ((value+count*36)>>2));
    }
    GLctx.uniformMatrix3fv(webglGetUniformLocation(location), !!transpose, view);
  },

  glUniformMatrix4fv__deps: ['$webglGetUniformLocation'
    , '$miniTempWebGLFloatBuffers'
  ],
  glUniformMatrix4fv: (location, count, transpose, value) => {

    if (count <= 18) {
      // avoid allocation when uploading few enough uniforms
      var view = miniTempWebGLFloatBuffers[16*count];
      // hoist the heap out of the loop for size and for pthreads+growth.
      var heap = HEAPF32;
      value = ((value)>>2);
      count *= 16;
      for (var i = 0; i < count; i += 16) {
        var dst = value + i;
        view[i] = heap[dst];
        view[i + 1] = heap[dst + 1];
        view[i + 2] = heap[dst + 2];
        view[i + 3] = heap[dst + 3];
        view[i + 4] = heap[dst + 4];
        view[i + 5] = heap[dst + 5];
        view[i + 6] = heap[dst + 6];
        view[i + 7] = heap[dst + 7];
        view[i + 8] = heap[dst + 8];
        view[i + 9] = heap[dst + 9];
        view[i + 10] = heap[dst + 10];
        view[i + 11] = heap[dst + 11];
        view[i + 12] = heap[dst + 12];
        view[i + 13] = heap[dst + 13];
        view[i + 14] = heap[dst + 14];
        view[i + 15] = heap[dst + 15];
      }
    } else
    {
      var view = HEAPF32.subarray((((value)>>2)), ((value+count*64)>>2));
    }
    GLctx.uniformMatrix4fv(webglGetUniformLocation(location), !!transpose, view);
  },

  glBindBuffer: (target, buffer) => {

    if (target == 0x88EB /*GL_PIXEL_PACK_BUFFER*/) {
      // In WebGL 2 glReadPixels entry point, we need to use a different WebGL 2
      // API function call when a buffer is bound to
      // GL_PIXEL_PACK_BUFFER_BINDING point, so must keep track whether that
      // binding point is non-null to know what is the proper API function to
      // call.
      GLctx.currentPixelPackBufferBinding = buffer;
    } else if (target == 0x88EC /*GL_PIXEL_UNPACK_BUFFER*/) {
      // In WebGL 2 gl(Compressed)Tex(Sub)Image[23]D entry points, we need to
      // use a different WebGL 2 API function call when a buffer is bound to
      // GL_PIXEL_UNPACK_BUFFER_BINDING point, so must keep track whether that
      // binding point is non-null to know what is the proper API function to
      // call.
      GLctx.currentPixelUnpackBufferBinding = buffer;
    }
    GLctx.bindBuffer(target, GL.buffers[buffer]);
  },

  glVertexAttrib1fv: (index, v) => {

    GLctx.vertexAttrib1f(index, HEAPF32[v>>2]);
  },

  glVertexAttrib2fv: (index, v) => {

    GLctx.vertexAttrib2f(index, HEAPF32[v>>2], HEAPF32[v+4>>2]);
  },

  glVertexAttrib3fv: (index, v) => {

    GLctx.vertexAttrib3f(index, HEAPF32[v>>2], HEAPF32[v+4>>2], HEAPF32[v+8>>2]);
  },

  glVertexAttrib4fv: (index, v) => {

    GLctx.vertexAttrib4f(index, HEAPF32[v>>2], HEAPF32[v+4>>2], HEAPF32[v+8>>2], HEAPF32[v+12>>2]);
  },

  glGetAttribLocation: (program, name) =>
    GLctx.getAttribLocation(GL.programs[program], UTF8ToString(name)),

  $__glGetActiveAttribOrUniform__deps: ['$stringToUTF8'],
  $__glGetActiveAttribOrUniform: (funcName, program, index, bufSize, length, size, type, name) => {
    program = GL.programs[program];
    var info = GLctx[funcName](program, index);
    if (info) {
      // If an error occurs, nothing will be written to length, size and type and name.
      var numBytesWrittenExclNull = name && stringToUTF8(info.name, name, bufSize);
      if (length) HEAP32[((length)>>2)] = numBytesWrittenExclNull;
      if (size) HEAP32[((size)>>2)] = info.size;
      if (type) HEAP32[((type)>>2)] = info.type;
    }
  },

  glGetActiveAttrib__deps: ['$__glGetActiveAttribOrUniform'],
  glGetActiveAttrib: (program, index, bufSize, length, size, type, name) =>
    __glGetActiveAttribOrUniform('getActiveAttrib', program, index, bufSize, length, size, type, name),

  glGetActiveUniform__deps: ['$__glGetActiveAttribOrUniform'],
  glGetActiveUniform: (program, index, bufSize, length, size, type, name) =>
    __glGetActiveAttribOrUniform('getActiveUniform', program, index, bufSize, length, size, type, name),

  glCreateShader: (shaderType) => {
    var id = GL.getNewId(GL.shaders);
    GL.shaders[id] = GLctx.createShader(shaderType);

    return id;
  },

  glDeleteShader: (id) => {
    if (!id) return;
    var shader = GL.shaders[id];
    if (!shader) {
      // glDeleteShader actually signals an error when deleting a nonexisting
      // object, unlike some other GL delete functions.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    GLctx.deleteShader(shader);
    GL.shaders[id] = null;
  },

  glGetAttachedShaders: (program, maxCount, count, shaders) => {
    var result = GLctx.getAttachedShaders(GL.programs[program]);
    var len = result.length;
    if (len > maxCount) {
      len = maxCount;
    }
    HEAP32[((count)>>2)] = len;
    for (var i = 0; i < len; ++i) {
      var id = GL.shaders.indexOf(result[i]);
      HEAP32[(((shaders)+(i*4))>>2)] = id;
    }
  },

  glShaderSource: (shader, count, string, length) => {
    var source = GL.getSource(shader, count, string, length);

    GLctx.shaderSource(GL.shaders[shader], source);
  },

  glGetShaderSource: (shader, bufSize, length, source) => {
    var result = GLctx.getShaderSource(GL.shaders[shader]);
    if (!result) return; // If an error occurs, nothing will be written to length or source.
    var numBytesWrittenExclNull = (bufSize > 0 && source) ? stringToUTF8(result, source, bufSize) : 0;
    if (length) HEAP32[((length)>>2)] = numBytesWrittenExclNull;
  },

  glCompileShader: (shader) => {
    GLctx.compileShader(GL.shaders[shader]);
  },

  glGetShaderInfoLog__deps: ['$stringToUTF8'],
  glGetShaderInfoLog: (shader, maxLength, length, infoLog) => {
    var log = GLctx.getShaderInfoLog(GL.shaders[shader]);
    if (log === null) log = '(unknown error)';
    var numBytesWrittenExclNull = (maxLength > 0 && infoLog) ? stringToUTF8(log, infoLog, maxLength) : 0;
    if (length) HEAP32[((length)>>2)] = numBytesWrittenExclNull;
  },

  glGetShaderiv: (shader, pname, p) => {
    if (!p) {
      // GLES2 specification does not specify how to behave if p is a null
      // pointer. Since calling this function does not make sense if p == null,
      // issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    if (pname == 0x8B84) { // GL_INFO_LOG_LENGTH
      var log = GLctx.getShaderInfoLog(GL.shaders[shader]);
      if (log === null) log = '(unknown error)';
      // The GLES2 specification says that if the shader has an empty info log,
      // a value of 0 is returned. Otherwise the log has a null char appended.
      // (An empty string is falsey, so we can just check that instead of
      // looking at log.length.)
      var logLength = log ? log.length + 1 : 0;
      HEAP32[((p)>>2)] = logLength;
    } else if (pname == 0x8B88) { // GL_SHADER_SOURCE_LENGTH
      var source = GLctx.getShaderSource(GL.shaders[shader]);
      // source may be a null, or the empty string, both of which are falsey
      // values that we report a 0 length for.
      var sourceLength = source ? source.length + 1 : 0;
      HEAP32[((p)>>2)] = sourceLength;
    } else {
      HEAP32[((p)>>2)] = GLctx.getShaderParameter(GL.shaders[shader], pname);
    }
  },

  glGetProgramiv : (program, pname, p) => {
    if (!p) {
      // GLES2 specification does not specify how to behave if p is a null
      // pointer. Since calling this function does not make sense if p == null,
      // issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }

    if (program >= GL.counter) {
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }

    program = GL.programs[program];

    if (pname == 0x8B84) { // GL_INFO_LOG_LENGTH
      var log = GLctx.getProgramInfoLog(program);
      if (log === null) log = '(unknown error)';
      HEAP32[((p)>>2)] = log.length + 1;
    } else if (pname == 0x8B87 /* GL_ACTIVE_UNIFORM_MAX_LENGTH */) {
      if (!program.maxUniformLength) {
        var numActiveUniforms = GLctx.getProgramParameter(program, 0x8B86/*GL_ACTIVE_UNIFORMS*/);
        for (var i = 0; i < numActiveUniforms; ++i) {
          program.maxUniformLength = Math.max(program.maxUniformLength, GLctx.getActiveUniform(program, i).name.length+1);
        }
      }
      HEAP32[((p)>>2)] = program.maxUniformLength;
    } else if (pname == 0x8B8A /* GL_ACTIVE_ATTRIBUTE_MAX_LENGTH */) {
      if (!program.maxAttributeLength) {
        var numActiveAttributes = GLctx.getProgramParameter(program, 0x8B89/*GL_ACTIVE_ATTRIBUTES*/);
        for (var i = 0; i < numActiveAttributes; ++i) {
          program.maxAttributeLength = Math.max(program.maxAttributeLength, GLctx.getActiveAttrib(program, i).name.length+1);
        }
      }
      HEAP32[((p)>>2)] = program.maxAttributeLength;
    } else if (pname == 0x8A35 /* GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH */) {
      if (!program.maxUniformBlockNameLength) {
        var numActiveUniformBlocks = GLctx.getProgramParameter(program, 0x8A36/*GL_ACTIVE_UNIFORM_BLOCKS*/);
        for (var i = 0; i < numActiveUniformBlocks; ++i) {
          program.maxUniformBlockNameLength = Math.max(program.maxUniformBlockNameLength, GLctx.getActiveUniformBlockName(program, i).length+1);
        }
      }
      HEAP32[((p)>>2)] = program.maxUniformBlockNameLength;
    } else {
      HEAP32[((p)>>2)] = GLctx.getProgramParameter(program, pname);
    }
  },

  glIsShader: (shader) => {
    var s = GL.shaders[shader];
    if (!s) return 0;
    return GLctx.isShader(s);
  },

  glCreateProgram: () => {
    var id = GL.getNewId(GL.programs);
    var program = GLctx.createProgram();
    // Store additional information needed for each shader program:
    program.name = id;
    // Lazy cache results of
    // glGetProgramiv(GL_ACTIVE_UNIFORM_MAX_LENGTH/GL_ACTIVE_ATTRIBUTE_MAX_LENGTH/GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH)
    program.maxUniformLength = program.maxAttributeLength = program.maxUniformBlockNameLength = 0;
    program.uniformIdCounter = 1;
    GL.programs[id] = program;
    return id;
  },

  glDeleteProgram: (id) => {
    if (!id) return;
    var program = GL.programs[id];
    if (!program) {
      // glDeleteProgram actually signals an error when deleting a nonexisting
      // object, unlike some other GL delete functions.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    GLctx.deleteProgram(program);
    program.name = 0;
    GL.programs[id] = null;
  },

  glAttachShader: (program, shader) => {
    GLctx.attachShader(GL.programs[program], GL.shaders[shader]);
  },

  glDetachShader: (program, shader) => {
    GLctx.detachShader(GL.programs[program], GL.shaders[shader]);
  },

  glGetShaderPrecisionFormat: (shaderType, precisionType, range, precision) => {
    var result = GLctx.getShaderPrecisionFormat(shaderType, precisionType);
    HEAP32[((range)>>2)] = result.rangeMin;
    HEAP32[(((range)+(4))>>2)] = result.rangeMax;
    HEAP32[((precision)>>2)] = result.precision;
  },

  glLinkProgram: (program) => {
    program = GL.programs[program];
    GLctx.linkProgram(program);
    // Invalidate earlier computed uniform->ID mappings, those have now become stale
    program.uniformLocsById = 0; // Mark as null-like so that glGetUniformLocation() knows to populate this again.
    program.uniformSizeAndIdsByName = {};

  },

  glGetProgramInfoLog: (program, maxLength, length, infoLog) => {
    var log = GLctx.getProgramInfoLog(GL.programs[program]);
    if (log === null) log = '(unknown error)';
    var numBytesWrittenExclNull = (maxLength > 0 && infoLog) ? stringToUTF8(log, infoLog, maxLength) : 0;
    if (length) HEAP32[((length)>>2)] = numBytesWrittenExclNull;
  },

  glUseProgram: (program) => {
    program = GL.programs[program];
    GLctx.useProgram(program);
    // Record the currently active program so that we can access the uniform
    // mapping table of that program.
    GLctx.currentProgram = program;
  },

  glValidateProgram: (program) => {
    GLctx.validateProgram(GL.programs[program]);
  },

  glIsProgram: (program) => {
    program = GL.programs[program];
    if (!program) return 0;
    return GLctx.isProgram(program);
  },

  glBindAttribLocation: (program, index, name) => {
    GLctx.bindAttribLocation(GL.programs[program], index, UTF8ToString(name));
  },

  glBindFramebuffer: (target, framebuffer) => {

    // defaultFbo may not be present if 'renderViaOffscreenBackBuffer' was not enabled during context creation time,
    // i.e. setting -sOFFSCREEN_FRAMEBUFFER at compilation time does not yet mandate that offscreen back buffer
    // is being used, but that is ultimately decided at context creation time.
    GLctx.bindFramebuffer(target, framebuffer ? GL.framebuffers[framebuffer] : GL.currentContext.defaultFbo);

  },

  glGenFramebuffers: (n, ids) => {
    GL.genObject(n, ids, 'createFramebuffer', GL.framebuffers
      );
  },

  glDeleteFramebuffers: (n, framebuffers) => {
    for (var i = 0; i < n; ++i) {
      var id = HEAP32[(((framebuffers)+(i*4))>>2)];
      var framebuffer = GL.framebuffers[id];
      if (!framebuffer) continue; // GL spec: "glDeleteFramebuffers silently ignores 0s and names that do not correspond to existing framebuffer objects".
      GLctx.deleteFramebuffer(framebuffer);
      framebuffer.name = 0;
      GL.framebuffers[id] = null;
    }
  },

  glFramebufferRenderbuffer: (target, attachment, renderbuffertarget, renderbuffer) => {
    GLctx.framebufferRenderbuffer(target, attachment, renderbuffertarget,
                                       GL.renderbuffers[renderbuffer]);
  },

  glFramebufferTexture2D: (target, attachment, textarget, texture, level) => {
    GLctx.framebufferTexture2D(target, attachment, textarget,
                                    GL.textures[texture], level);
  },

  glGetFramebufferAttachmentParameteriv: (target, attachment, pname, params) => {
    var result = GLctx.getFramebufferAttachmentParameter(target, attachment, pname);
    if (result instanceof WebGLRenderbuffer ||
        result instanceof WebGLTexture) {
      result = result.name | 0;
    }
    HEAP32[((params)>>2)] = result;
  },

  glIsFramebuffer: (framebuffer) => {
    var fb = GL.framebuffers[framebuffer];
    if (!fb) return 0;
    return GLctx.isFramebuffer(fb);
  },

  glGenVertexArrays: (n, arrays) => {
    GL.genObject(n, arrays, 'createVertexArray', GL.vaos
      );
  },

  glDeleteVertexArrays: (n, vaos) => {
    for (var i = 0; i < n; i++) {
      var id = HEAP32[(((vaos)+(i*4))>>2)];
      GLctx.deleteVertexArray(GL.vaos[id]);
      GL.vaos[id] = null;
    }
  },

  glBindVertexArray: (vao) => {
    GLctx.bindVertexArray(GL.vaos[vao]);
  },

  glIsVertexArray: (array) => {

    var vao = GL.vaos[array];
    if (!vao) return 0;
    return GLctx.isVertexArray(vao);
  },

  glVertexPointer: (size, type, stride, ptr) =>
    abort('Legacy GL function (glVertexPointer) called. If you want legacy GL emulation, you need to compile with -sLEGACY_GL_EMULATION to enable legacy GL emulation.'),
  glMatrixMode: () =>
    abort('Legacy GL function (glMatrixMode) called. If you want legacy GL emulation, you need to compile with -sLEGACY_GL_EMULATION to enable legacy GL emulation.'),
  glBegin: () =>
    abort('Legacy GL function (glBegin) called. If you want legacy GL emulation, you need to compile with -sLEGACY_GL_EMULATION to enable legacy GL emulation.'),
  glLoadIdentity: () =>
    abort('Legacy GL function (glLoadIdentity) called. If you want legacy GL emulation, you need to compile with -sLEGACY_GL_EMULATION to enable legacy GL emulation.'),

  // Open GLES1.1 vao compatibility (Could work w/o -sLEGACY_GL_EMULATION)

  glGenVertexArraysOES: 'glGenVertexArrays',
  glDeleteVertexArraysOES: 'glDeleteVertexArrays',
  glBindVertexArrayOES: 'glBindVertexArray',
  glIsVertexArrayOES: 'glIsVertexArray',

  // GLES2 emulation

  glVertexAttribPointer: (index, size, type, normalized, stride, ptr) => {
    GLctx.vertexAttribPointer(index, size, type, !!normalized, stride, ptr);
  },

  glEnableVertexAttribArray: (index) => {
    GLctx.enableVertexAttribArray(index);
  },

  glDisableVertexAttribArray: (index) => {
    GLctx.disableVertexAttribArray(index);
  },

  glDrawArrays: (mode, first, count) => {

    GLctx.drawArrays(mode, first, count);

  },

  glDrawElements__deps: ['$webglBufferSubData'],
  glDrawElements: (mode, count, type, indices) => {

    GLctx.drawElements(mode, count, type, indices);

  },

  glShaderBinary: (count, shaders, binaryformat, binary, length) => {
    GL.recordError(0x500/*GL_INVALID_ENUM*/);
  },

  glReleaseShaderCompiler: () => {
    // NOP (as allowed by GLES 2.0 spec)
  },

  glGetError: () => {
    var error = GLctx.getError() || GL.lastError;
    GL.lastError = 0/*GL_NO_ERROR*/;
    return error;
  },

  // ANGLE_instanced_arrays WebGL extension related functions (in core in WebGL 2)

  glVertexAttribDivisor: (index, divisor) => {
    GLctx.vertexAttribDivisor(index, divisor);
  },

  glDrawArraysInstanced: (mode, first, count, primcount) => {
    GLctx.drawArraysInstanced(mode, first, count, primcount);
  },

  glDrawElementsInstanced: (mode, count, type, indices, primcount) => {
    GLctx.drawElementsInstanced(mode, count, type, indices, primcount);
  },

  // OpenGL Desktop/ES 2.0 instancing extensions compatibility

  glVertexAttribDivisorNV: 'glVertexAttribDivisor',
  glDrawArraysInstancedNV: 'glDrawArraysInstanced',
  glDrawElementsInstancedNV: 'glDrawElementsInstanced',
  glVertexAttribDivisorEXT: 'glVertexAttribDivisor',
  glDrawArraysInstancedEXT: 'glDrawArraysInstanced',
  glDrawElementsInstancedEXT: 'glDrawElementsInstanced',
  glVertexAttribDivisorARB: 'glVertexAttribDivisor',
  glDrawArraysInstancedARB: 'glDrawArraysInstanced',
  glDrawElementsInstancedARB: 'glDrawElementsInstanced',
  glVertexAttribDivisorANGLE: 'glVertexAttribDivisor',
  glDrawArraysInstancedANGLE: 'glDrawArraysInstanced',
  glDrawElementsInstancedANGLE: 'glDrawElementsInstanced',

  glDrawBuffers__deps: ['$tempFixedLengthArray'],
  glDrawBuffers: (n, bufs) => {

    var bufArray = tempFixedLengthArray[n];
    for (var i = 0; i < n; i++) {
      bufArray[i] = HEAP32[(((bufs)+(i*4))>>2)];
    }

    GLctx.drawBuffers(bufArray);
  },

  // OpenGL ES 2.0 draw buffer extensions compatibility

  glDrawBuffersEXT: 'glDrawBuffers',
  glDrawBuffersWEBGL: 'glDrawBuffers',

  // passthrough functions with GLboolean parameters

  glColorMask: (red, green, blue, alpha) => {
    GLctx.colorMask(!!red, !!green, !!blue, !!alpha);
  },

  glDepthMask: (flag) => {
    GLctx.depthMask(!!flag);
  },

  glSampleCoverage: (value, invert) => {
    GLctx.sampleCoverage(value, !!invert);
  },

  glMultiDrawArraysWEBGL__sig: 'vippi',
  glMultiDrawArrays: 'glMultiDrawArraysWEBGL',
  glMultiDrawArraysANGLE: 'glMultiDrawArraysWEBGL',
  glMultiDrawArraysWEBGL: (mode, firsts, counts, drawcount) => {
    GLctx.multiDrawWebgl['multiDrawArraysWEBGL'](
      mode,
      HEAP32,
      ((firsts)>>2),
      HEAP32,
      ((counts)>>2),
      drawcount);
  },

  glMultiDrawArraysInstancedWEBGL__sig: 'vipppi',
  glMultiDrawArraysInstancedANGLE: 'glMultiDrawArraysInstancedWEBGL',
  glMultiDrawArraysInstancedWEBGL: (mode, firsts, counts, instanceCounts, drawcount) => {
    GLctx.multiDrawWebgl['multiDrawArraysInstancedWEBGL'](
      mode,
      HEAP32,
      ((firsts)>>2),
      HEAP32,
      ((counts)>>2),
      HEAP32,
      ((instanceCounts)>>2),
      drawcount);
  },

  glMultiDrawElementsWEBGL__sig: 'vipipi',
  glMultiDrawElements: 'glMultiDrawElementsWEBGL',
  glMultiDrawElementsANGLE: 'glMultiDrawElementsWEBGL',
  glMultiDrawElementsWEBGL: (mode, counts, type, offsets, drawcount) => {
    GLctx.multiDrawWebgl['multiDrawElementsWEBGL'](
      mode,
      HEAP32,
      ((counts)>>2),
      type,
      HEAP32,
      ((offsets)>>2),
      drawcount);
  },

  glMultiDrawElementsInstancedWEBGL__sig: 'vipippi',
  glMultiDrawElementsInstancedANGLE: 'glMultiDrawElementsInstancedWEBGL',
  glMultiDrawElementsInstancedWEBGL: (mode, counts, type, offsets, instanceCounts, drawcount) => {
    GLctx.multiDrawWebgl['multiDrawElementsInstancedWEBGL'](
      mode,
      HEAP32,
      ((counts)>>2),
      type,
      HEAP32,
      ((offsets)>>2),
      HEAP32,
      ((instanceCounts)>>2),
      drawcount);
  },

  // As a small peculiarity, we currently allow building with -sFULL_ES3 to emulate client side arrays,
  // but without targeting WebGL 2, so this FULL_ES3 block is in library_webgl.js instead of library_webgl2.js

  glPolygonOffsetClampEXT__sig: 'vfff',
  glPolygonOffsetClampEXT: (factor, units, clamp) => {
    GLctx.extPolygonOffsetClamp['polygonOffsetClampEXT'](factor, units, clamp);
  },

  glClipControlEXT__sig: 'vii',
  glClipControlEXT: (origin, depth) => {
    GLctx.extClipControl['clipControlEXT'](origin, depth);
  },

  glPolygonModeWEBGL__sig: 'vii',
  glPolygonModeWEBGL: (face, mode) => {
    GLctx.webglPolygonMode['polygonModeWEBGL'](face, mode);
  },
};

// Simple pass-through functions.
// - Starred ones have return values.
// - [X] ones have X in the C name but not in the JS name
var glPassthroughFuncs = [
  [0, 'finish flush'],
  [1, 'clearDepth clearDepth[f] depthFunc enable disable frontFace cullFace clear lineWidth clearStencil stencilMask checkFramebufferStatus* generateMipmap activeTexture blendEquation isEnabled*'],
  [2, 'blendFunc blendEquationSeparate depthRange depthRange[f] stencilMaskSeparate hint polygonOffset vertexAttrib1f'],
  [3, 'texParameteri texParameterf vertexAttrib2f stencilFunc stencilOp'],
  [4, 'viewport clearColor scissor vertexAttrib3f renderbufferStorage blendFuncSeparate blendColor stencilFuncSeparate stencilOpSeparate'],
  [5, 'vertexAttrib4f'],
  [8, 'copyTexImage2D copyTexSubImage2D'],
];

function createGLPassthroughFunctions(lib, funcs) {
  for (const [num, names] of funcs) {
    const args = range(num).map((i) => 'x' + i ).join(', ');
    const stub = `(${args}) => GLctx.NAME(${args})`;
    const sigEnd = range(num).map(() => 'i').join('');
    for (var name of names.split(' ')) {
      let sig;
      if (name.endsWith('*')) {
        name = name.slice(0, -1);
        sig = 'i' + sigEnd;
      } else {
        sig = 'v' + sigEnd;
      }
      let cName = name;
      if (name.includes('[')) {
        cName = name.replace('[', '').replace(']', '');
        name = cName.slice(0, -1);
      }
      cName = 'gl' + cName[0].toUpperCase() + cName.slice(1);
      assert(!(cName in lib), "Cannot reimplement the existing function " + cName);
      lib[cName] = eval(stub.replace('NAME', name));
      assert(lib[cName + '__sig'] || LibraryManager.library[cName + '__sig'], 'missing sig for ' + cName);
    }
  }
}

createGLPassthroughFunctions(LibraryGL, glPassthroughFuncs);

autoAddDeps(LibraryGL, '$GL');

function renameSymbol(lib, oldName, newName) {
  lib[newName] = lib[oldName];
  delete lib[oldName];
  for (const suffix of decoratorSuffixes) {
    const oldDecorator = oldName + suffix;
    if (lib.hasOwnProperty(oldDecorator)) {
      const newDecorator = newName + suffix;
      lib[newDecorator] = lib[oldDecorator];
      delete lib[oldDecorator];
    }
  }
}

function recordGLProcAddressGet(lib) {
  // GL proc address retrieval - allow access through glX and emscripten_glX, to
  // allow name collisions with user-implemented things having the same name
  // (see gl.c)
  //
  // We do this by renaming `glX` symbols to `emscripten_glX` and then setting
  // `glX` as an alias of `emscripten_glX`.  The reason for this renaming is to
  // ensure that `emscripten_glX` is always available, even in cases where native
  // code defines `glX`.
  const glSyms = [];
  for (const sym of Object.keys(lib)) {
    if (sym.startsWith('gl') && !isDecorator(sym)) {
      const newSym = 'emscripten_' + sym;
      renameSymbol(lib, sym, newSym);
      lib[sym] = newSym;
      var sig = LibraryManager.library[sym + '__sig'];
      if (sig) {
        lib[newSym + '__sig'] = sig;
      }
    }
  }
}

recordGLProcAddressGet(LibraryGL);

// Final merge
addToLibrary(LibraryGL);
