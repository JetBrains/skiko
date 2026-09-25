#pragma once

#include <cstdint>
#include <memory>
#include <string>
#include <vector>

class SDLWindow {
public:
    SDLWindow(int width, int height, const std::string& title);
    ~SDLWindow();

    SDLWindow(const SDLWindow&) = delete;
    SDLWindow& operator=(const SDLWindow&) = delete;

    bool IsOpen() const;
    void PollEvents();
    void Present(const std::vector<uint8_t>& rgba);
    void SetTitle(const std::string& title);

private:
    struct Impl;
    std::unique_ptr<Impl> impl_;
};
