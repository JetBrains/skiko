#ifdef __EMSCRIPTEN__
#include "common.h"
#include <GLES2/gl2.h>

SKIKO_EXPORT KInt org_jetbrains_skiko_tests_GLTextureTest__1nGetTextureBinding2D() {
    GLint binding = 0;
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &binding);
    return binding;
}
#endif
