#include "sdl_window.h"

#include <SDL.h>

#define GL_SILENCE_DEPRECATION
#include <OpenGL/OpenGL.h>
#include <OpenGL/gl3.h>

#include <algorithm>
#include <stdexcept>
#include <string>

namespace {

std::runtime_error SDLError(const char* operation) {
    return std::runtime_error(
        std::string(operation) + ": " + SDL_GetError());
}

} // namespace

struct SDLWindow::Impl {
    int width = 0;
    int height = 0;
    bool open = true;
    SDL_Window* window = nullptr;
    SDL_GLContext gl_context = nullptr;
    GLuint read_framebuffer = 0;

    ~Impl() {
        if (window != nullptr && gl_context != nullptr) {
            SDL_GL_MakeCurrent(window, gl_context);
            if (read_framebuffer != 0) {
                glDeleteFramebuffers(1, &read_framebuffer);
            }
            SDL_GL_DeleteContext(gl_context);
        }
        if (window != nullptr) SDL_DestroyWindow(window);
        SDL_QuitSubSystem(SDL_INIT_VIDEO);
    }
};

SDLWindow::SDLWindow(int width, int height, const std::string& title)
    : impl_(std::make_unique<Impl>()) {
    SDL_SetMainReady();
    if (SDL_InitSubSystem(SDL_INIT_VIDEO) != 0) {
        throw SDLError("SDL_InitSubSystem");
    }

    impl_->width = width;
    impl_->height = height;
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 2);
    SDL_GL_SetAttribute(
        SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE);
    SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1);
    SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, 24);
    SDL_GL_SetAttribute(SDL_GL_STENCIL_SIZE, 8);
    impl_->window = SDL_CreateWindow(
        title.c_str(),
        SDL_WINDOWPOS_CENTERED,
        SDL_WINDOWPOS_CENTERED,
        width,
        height,
        SDL_WINDOW_SHOWN | SDL_WINDOW_ALLOW_HIGHDPI | SDL_WINDOW_OPENGL);
    if (impl_->window == nullptr) {
        throw SDLError("SDL_CreateWindow");
    }

    impl_->gl_context = SDL_GL_CreateContext(impl_->window);
    if (impl_->gl_context == nullptr) {
        throw SDLError("SDL_GL_CreateContext");
    }
    if (SDL_GL_MakeCurrent(impl_->window, impl_->gl_context) != 0) {
        throw SDLError("SDL_GL_MakeCurrent");
    }
    SDL_GL_SetSwapInterval(1);
    glGenFramebuffers(1, &impl_->read_framebuffer);
}

SDLWindow::~SDLWindow() {
}

bool SDLWindow::IsOpen() const {
    return impl_->open;
}

void SDLWindow::PollEvents() {
    SDL_Event event;
    while (SDL_PollEvent(&event)) {
        if (event.type == SDL_QUIT ||
            (event.type == SDL_WINDOWEVENT &&
             event.window.windowID == SDL_GetWindowID(impl_->window) &&
             event.window.event == SDL_WINDOWEVENT_CLOSE)) {
            impl_->open = false;
        }
    }
}

void SDLWindow::Present(const std::vector<CanvasTexture>& canvases) {
    if (SDL_GL_MakeCurrent(impl_->window, impl_->gl_context) != 0) {
        throw SDLError("SDL_GL_MakeCurrent");
    }
    int pixel_width = 0;
    int pixel_height = 0;
    SDL_GL_GetDrawableSize(impl_->window, &pixel_width, &pixel_height);
    const double scale_x = static_cast<double>(pixel_width) / impl_->width;
    const double scale_y = static_cast<double>(pixel_height) / impl_->height;

    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    glViewport(0, 0, pixel_width, pixel_height);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_BLEND);
    glEnable(GL_SCISSOR_TEST);
    glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
    glScissor(0, 0, pixel_width, pixel_height);
    glClearColor(1, 1, 1, 1);
    glClear(GL_COLOR_BUFFER_BIT);

    for (const CanvasTexture& canvas : canvases) {
        const int x = static_cast<int>(canvas.x * scale_x);
        const int top = static_cast<int>(canvas.y * scale_y);
        const int width = static_cast<int>(canvas.display_width * scale_x);
        const int height = static_cast<int>(canvas.display_height * scale_y);
        const int bottom = pixel_height - top - height;
        const int border_x = std::max(1, static_cast<int>(scale_x));
        const int border_y = std::max(1, static_cast<int>(scale_y));

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glScissor(x, bottom, width, height);
        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, impl_->read_framebuffer);
        glFramebufferTexture2D(
            GL_READ_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            canvas.texture,
            0);
        if (glCheckFramebufferStatus(GL_READ_FRAMEBUFFER) !=
            GL_FRAMEBUFFER_COMPLETE) {
            throw std::runtime_error("Shared canvas framebuffer is incomplete");
        }
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glScissor(
            x + border_x,
            bottom + border_y,
            width - 2 * border_x,
            height - 2 * border_y);
        glBlitFramebuffer(
            0,
            0,
            canvas.source_width,
            canvas.source_height,
            x + border_x,
            bottom + border_y,
            x + width - border_x,
            bottom + height - border_y,
            GL_COLOR_BUFFER_BIT,
            GL_LINEAR);
    }
    glDisable(GL_SCISSOR_TEST);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    SDL_GL_SwapWindow(impl_->window);
}

void SDLWindow::SetTitle(const std::string& title) {
    SDL_SetWindowTitle(impl_->window, title.c_str());
}

void* SDLWindow::NativeGLContext() const {
    SDL_GL_MakeCurrent(impl_->window, impl_->gl_context);
    return CGLGetCurrentContext();
}
