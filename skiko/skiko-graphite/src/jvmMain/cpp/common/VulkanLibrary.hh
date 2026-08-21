#pragma once

#if defined(SK_VULKAN) && (defined(_WIN32) || defined(__linux__))

#include "include/third_party/vulkan/vulkan/vulkan_core.h"

void* skikoVulkanLibrary();
PFN_vkVoidFunction skikoVulkanLibrarySymbol(const char* name);
PFN_vkVoidFunction skikoVulkanProc(const char* name, VkInstance instance, VkDevice device);

#endif
