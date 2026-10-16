#pragma once

#include <cstdint>
#include <memory>
#include <string>
#include <vector>

class MacOSWindow {
public:
    MacOSWindow(int width, int height, const std::string& title);
    ~MacOSWindow();

    MacOSWindow(const MacOSWindow&) = delete;
    MacOSWindow& operator=(const MacOSWindow&) = delete;

    bool IsOpen() const;
    void PollEvents();
    void Present(const std::vector<uint8_t>& rgba);
    void SetTitle(const std::string& title);

private:
    struct Impl;
    std::unique_ptr<Impl> impl_;
};
