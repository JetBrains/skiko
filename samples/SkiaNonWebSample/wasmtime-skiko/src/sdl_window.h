#pragma once

#include <cstdint>
#include <memory>
#include <string>
#include <vector>

class SDLWindow {
public:
    struct CanvasTexture {
        uint32_t texture;
        int source_width;
        int source_height;
        int x;
        int y;
        int display_width;
        int display_height;
    };

    SDLWindow(int width, int height, const std::string& title);
    ~SDLWindow();

    SDLWindow(const SDLWindow&) = delete;
    SDLWindow& operator=(const SDLWindow&) = delete;

    bool IsOpen() const;
    void PollEvents();
    void Present(const std::vector<CanvasTexture>& canvases);
    void SetTitle(const std::string& title);
    void* NativeGLContext() const;

private:
    struct Impl;
    std::unique_ptr<Impl> impl_;
};
