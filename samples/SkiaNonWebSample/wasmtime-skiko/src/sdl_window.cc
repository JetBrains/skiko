#include "sdl_window.h"

#include <SDL.h>

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
    SDL_Renderer* renderer = nullptr;
    SDL_Texture* texture = nullptr;

    ~Impl() {
        if (texture != nullptr) SDL_DestroyTexture(texture);
        if (renderer != nullptr) SDL_DestroyRenderer(renderer);
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
    impl_->window = SDL_CreateWindow(
        title.c_str(),
        SDL_WINDOWPOS_CENTERED,
        SDL_WINDOWPOS_CENTERED,
        width,
        height,
        SDL_WINDOW_SHOWN | SDL_WINDOW_ALLOW_HIGHDPI);
    if (impl_->window == nullptr) {
        throw SDLError("SDL_CreateWindow");
    }

    impl_->renderer = SDL_CreateRenderer(
        impl_->window,
        -1,
        SDL_RENDERER_ACCELERATED | SDL_RENDERER_PRESENTVSYNC);
    if (impl_->renderer == nullptr) {
        throw SDLError("SDL_CreateRenderer");
    }
    SDL_RenderSetLogicalSize(impl_->renderer, width, height);

    impl_->texture = SDL_CreateTexture(
        impl_->renderer,
        SDL_PIXELFORMAT_RGBA32,
        SDL_TEXTUREACCESS_STREAMING,
        width,
        height);
    if (impl_->texture == nullptr) {
        throw SDLError("SDL_CreateTexture");
    }
#if SDL_VERSION_ATLEAST(2, 0, 12)
    SDL_SetTextureScaleMode(impl_->texture, SDL_ScaleModeLinear);
#endif
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

void SDLWindow::Present(const std::vector<uint8_t>& rgba) {
    const size_t expected =
        static_cast<size_t>(impl_->width) * impl_->height * 4;
    if (rgba.size() != expected) {
        throw std::runtime_error("Invalid RGBA frame size");
    }
    if (SDL_UpdateTexture(
            impl_->texture, nullptr, rgba.data(), impl_->width * 4) != 0) {
        throw SDLError("SDL_UpdateTexture");
    }
    if (SDL_RenderClear(impl_->renderer) != 0) {
        throw SDLError("SDL_RenderClear");
    }
    if (SDL_RenderCopy(impl_->renderer, impl_->texture, nullptr, nullptr) != 0) {
        throw SDLError("SDL_RenderCopy");
    }
    SDL_RenderPresent(impl_->renderer);
}

void SDLWindow::SetTitle(const std::string& title) {
    SDL_SetWindowTitle(impl_->window, title.c_str());
}
