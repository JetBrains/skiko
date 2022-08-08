#pragma once

#include <locale>
#include <Windows.h>
#include <jawt_md.h>
#include "GrDirectContext.h"
#include "d3d/GrD3DTypes.h"
#include <dxgi1_4.h>
#include <dxgi1_6.h>

HRESULT CreateDXGIFactory1(
    REFIID riid,
    void **ppFactory);

HRESULT CreateDXGIFactory2(
    UINT Flags,
    REFIID riid,
    void **ppFactory);

bool defineHardwareAdapter(
    DXGI_GPU_PREFERENCE adapterPriority,
    IDXGIAdapter1 **ppAdapter,
    std::function<bool(IDXGIAdapter1*)> const& accept
);