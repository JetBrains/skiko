#include "GpuChoose.h"

// This is a list of not supported graphics cards that have rendering issues (black screen, flickering)
// with the current Swing/Skia integration.
// If PC has other graphics cards suitable for DirectX12, one of them will be used. Otherwise,
// rendering will falls back to OpenGL.
const std::vector<std::wstring> notSupportedAdapters{
    L"Intel(R) HD Graphics 520",
    L"Intel(R) HD Graphics 530",
    L"Intel(R) HD Graphics 4400",
    L"NVIDIA GeForce GTX 750 Ti",
    L"NVIDIA GeForce GTX 960M",
    L"NVIDIA Quadro M2000M"};

HRESULT CreateDXGIFactory1(
    REFIID riid,
    void **ppFactory)
{
    typedef HRESULT (*CreateDXGIFactory1_t)(
        REFIID riid,
        void **ppFactory);
    static CreateDXGIFactory1_t impl = nullptr;
    if (!impl)
    {
        auto dxgidll = LoadLibrary(TEXT("Dxgi.dll"));
        if (!dxgidll)
            return E_NOTIMPL;
        impl = (CreateDXGIFactory1_t)GetProcAddress(dxgidll, "CreateDXGIFactory1");
        if (!impl)
            return E_NOTIMPL;
    }
    return impl(riid, ppFactory);
}

HRESULT CreateDXGIFactory2(
    UINT Flags,
    REFIID riid,
    void **ppFactory)
{
    typedef HRESULT (*CreateDXGIFactory2_t)(
        UINT Flags,
        REFIID riid,
        void **ppFactory);
    static CreateDXGIFactory2_t impl = nullptr;
    if (!impl)
    {
        auto dxgidll = LoadLibrary(TEXT("Dxgi.dll"));
        if (!dxgidll)
            return E_NOTIMPL;
        impl = (CreateDXGIFactory2_t)GetProcAddress(dxgidll, "CreateDXGIFactory2");
        if (!impl)
            return E_NOTIMPL;
    }
    return impl(Flags, riid, ppFactory);
}

bool isNotSupported(IDXGIAdapter1 *hardwareAdapter)
{
    DXGI_ADAPTER_DESC1 desc;
    hardwareAdapter->GetDesc1(&desc);
    if ((desc.Flags & DXGI_ADAPTER_FLAG_SOFTWARE) != 0)
    {
        return true;
    }
    std::wstring currentAdapterName(desc.Description);
    for (std::wstring name : notSupportedAdapters)
    {
        if (currentAdapterName == name)
        {
            return true;
        }
    }
    return false;
}

bool defineHardwareAdapter(
    DXGI_GPU_PREFERENCE adapterPriority,
    IDXGIAdapter1 **adapter,
    std::function<bool(IDXGIAdapter1*)> const& accept
) {
    gr_cp<IDXGIFactory4> factory4;
    if (!SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&factory4)))) {
        return 0;
    }

    *adapter = nullptr;

#if defined(__dxgi1_6_h__) && defined(NTDDI_WIN10_RS4)
    gr_cp<IDXGIFactory6> factory6;
    if (SUCCEEDED(factory4->QueryInterface(IID_PPV_ARGS(&factory6)))) {
        for (UINT i = 0;; ++i) {
            IDXGIAdapter1 *current = nullptr;
            if (!SUCCEEDED(factory6->EnumAdapterByGpuPreference(i, adapterPriority, IID_PPV_ARGS(&current)))) {
                break;
            }
            if (isNotSupported(current)) {
                current->Release();
                continue;
            }
            if (accept(current)) {
                *adapter = current;
                return true;
            }
            current->Release();
        }
    }
#endif

    if (*adapter == nullptr) {
        for (UINT i = 0;; ++i) {
            IDXGIAdapter1 *current = nullptr;
            if (DXGI_ERROR_NOT_FOUND == factory4->EnumAdapters1(i, &current)) {
                break;
            }
            if (isNotSupported(current)) {
                current->Release();
                continue;
            }
            if (accept(current)) {
                *adapter = current;
                return true;
            }
            current->Release();
        }
    }

    return false;
}
