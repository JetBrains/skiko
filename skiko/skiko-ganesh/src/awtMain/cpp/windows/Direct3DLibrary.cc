#ifdef SK_DIRECT3D

#include <locale>
#include <Windows.h>
#include <d3d12sdklayers.h>
#include <d3d12.h>
#include <dxgi1_4.h>
#include <dxgi1_6.h>
#include <d3dcompiler.h>

extern "C"
{
    HRESULT D3D12CreateDevice(
        IUnknown *pAdapter,
        D3D_FEATURE_LEVEL MinimumFeatureLevel,
        REFIID riid,
        void **ppDevice)
    {
        typedef HRESULT (*D3D12CreateDevice_t)(
            IUnknown * pAdapter,
            D3D_FEATURE_LEVEL MinimumFeatureLevel,
            REFIID riid,
            void **ppDevice);
        static D3D12CreateDevice_t impl = nullptr;
        if (!impl)
        {
            auto d3d12dll = LoadLibrary(TEXT("D3D12.dll"));
            if (!d3d12dll)
                return E_NOTIMPL;
            impl = (D3D12CreateDevice_t)GetProcAddress(d3d12dll, "D3D12CreateDevice");
            if (!impl)
                return E_NOTIMPL;
        }
        return impl(pAdapter, MinimumFeatureLevel, riid, ppDevice);
    }

    HRESULT D3D12SerializeRootSignature(
        const D3D12_ROOT_SIGNATURE_DESC *pRootSignature,
        D3D_ROOT_SIGNATURE_VERSION Version,
        ID3DBlob **ppBlob,
        ID3DBlob **ppErrorBlob)
    {
        typedef HRESULT (*D3D12SerializeRootSignature_t)(
            const D3D12_ROOT_SIGNATURE_DESC *pRootSignature,
            D3D_ROOT_SIGNATURE_VERSION Version,
            ID3DBlob **ppBlob,
            ID3DBlob **ppErrorBlob);
        static D3D12SerializeRootSignature_t impl = nullptr;
        if (!impl)
        {
            auto d3d12dll = LoadLibrary(TEXT("D3D12.dll"));
            if (!d3d12dll)
                return E_NOTIMPL;
            impl = (D3D12SerializeRootSignature_t)GetProcAddress(d3d12dll, "D3D12SerializeRootSignature");
            if (!impl)
                return E_NOTIMPL;
        }
        return impl(pRootSignature, Version, ppBlob, ppErrorBlob);
    }

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

    HRESULT D3DCompile(
        LPCVOID pSrcData,
        SIZE_T SrcDataSize,
        LPCSTR pSourceName,
        const D3D_SHADER_MACRO *pDefines,
        ID3DInclude *pInclude,
        LPCSTR pEntrypoint,
        LPCSTR pTarget,
        UINT Flags1,
        UINT Flags2,
        ID3DBlob **ppCode,
        ID3DBlob **ppErrorMsgs)
    {
        typedef HRESULT (*D3DCompile_t)(
            LPCVOID pSrcData,
            SIZE_T SrcDataSize,
            LPCSTR pSourceName,
            const D3D_SHADER_MACRO *pDefines,
            ID3DInclude *pInclude,
            LPCSTR pEntrypoint,
            LPCSTR pTarget,
            UINT Flags1,
            UINT Flags2,
            ID3DBlob **ppCode,
            ID3DBlob **ppErrorMsgs);
        static D3DCompile_t impl = nullptr;
        if (!impl)
        {
            auto d3dcompilerdll = LoadLibrary(TEXT("d3dcompiler_47.dll"));
            if (!d3dcompilerdll)
                return E_NOTIMPL;
            impl = (D3DCompile_t)GetProcAddress(d3dcompilerdll, "D3DCompile");
            if (!impl)
                return E_NOTIMPL;
        }
        return impl(pSrcData, SrcDataSize, pSourceName, pDefines, pInclude, pEntrypoint, pTarget, Flags1, Flags2, ppCode, ppErrorMsgs);
    }
}

#endif // SK_DIRECT3D
