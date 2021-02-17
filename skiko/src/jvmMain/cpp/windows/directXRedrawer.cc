#ifdef SK_DIRECT3D
#include <windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"

#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "SkSurface.h"

#include "d3d/GrD3DTypes.h"
#include <d3d12sdklayers.h>
#include "d3d/GrD3DBackendContext.h"
#include <d3d12.h>
#include <dxgi1_4.h>

#define GR_D3D_CALL_ERRCHECK(X)                                        \
    do                                                                 \
    {                                                                  \
        HRESULT result = X;                                            \
        SkASSERT(SUCCEEDED(result));                                   \
        if (!SUCCEEDED(result))                                        \
        {                                                              \
            SkDebugf("Failed Direct3D call. Error: 0x%08x\n", result); \
        }                                                              \
    } while (false)

const int BuffersCount = 2;

class DirectXDevice
{
public:
    GrD3DBackendContext backendContext;
    gr_cp<ID3D12Device> device;
    gr_cp<IDXGISwapChain3> swapChain;
    gr_cp<ID3D12CommandQueue> queue;
    gr_cp<ID3D12Fence> fence;
    HANDLE fenceEvent;
    uint64_t fenceValue;
    unsigned int bufferIndex = 1;
    unsigned int bufferWidth = 0;
    unsigned int bufferHeight = 0;

    bool isSizeEqualTo(unsigned int width, unsigned int height)
    {
        return width == bufferWidth && height == bufferHeight;
    }
};

extern "C"
{

HRESULT D3D12CreateDevice(
  IUnknown          *pAdapter,
  D3D_FEATURE_LEVEL MinimumFeatureLevel,
  REFIID            riid,
  void              **ppDevice
) {
    typedef HRESULT (*D3D12CreateDevice_t)(
        IUnknown          *pAdapter,
        D3D_FEATURE_LEVEL MinimumFeatureLevel,
        REFIID            riid,
        void              **ppDevice
    );
    static D3D12CreateDevice_t impl = nullptr;
    if (!impl) {
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
  D3D_ROOT_SIGNATURE_VERSION      Version,
  ID3DBlob                        **ppBlob,
  ID3DBlob                        **ppErrorBlob
) {
    typedef HRESULT (*D3D12SerializeRootSignature_t)(
              const D3D12_ROOT_SIGNATURE_DESC *pRootSignature,
              D3D_ROOT_SIGNATURE_VERSION      Version,
              ID3DBlob                        **ppBlob,
              ID3DBlob                        **ppErrorBlob
    );
    static D3D12SerializeRootSignature_t impl = nullptr;
    if (!impl) {
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
  void   **ppFactory
) {
    typedef HRESULT (*CreateDXGIFactory1_t)(
        REFIID riid,
        void   **ppFactory
    );
    static CreateDXGIFactory1_t impl = nullptr;
    if (!impl) {
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
  UINT   Flags,
  REFIID riid,
  void   **ppFactory
) {
    typedef HRESULT (*CreateDXGIFactory2_t)(
        UINT   Flags,
        REFIID riid,
        void   **ppFactory
    );
    static CreateDXGIFactory2_t impl = nullptr;
    if (!impl) {
        auto dxgidll = LoadLibrary(TEXT("Dxgi.dll"));
        if (!dxgidll)
            return E_NOTIMPL;
        impl = (CreateDXGIFactory2_t)GetProcAddress(dxgidll, "CreateDXGIFactory2");
        if (!impl)
            return E_NOTIMPL;
    }
    return impl(Flags, riid, ppFactory);
}

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_makeDirectXContext(
        JNIEnv* env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice*>(devicePtr);
        GrD3DBackendContext backendContext = d3dDevice->backendContext;
        return toJavaPointer(GrDirectContext::MakeDirect3D(backendContext).release());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_makeDirectXRenderTarget(
        JNIEnv * env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice*>(devicePtr);
        d3dDevice->bufferIndex = d3dDevice->swapChain->GetCurrentBackBufferIndex();
        ID3D12Resource* buffer;
        GR_D3D_CALL_ERRCHECK(d3dDevice->swapChain->GetBuffer(d3dDevice->bufferIndex, IID_PPV_ARGS(&buffer)));
        GrD3DTextureResourceInfo info(buffer,
                                      nullptr,
                                      D3D12_RESOURCE_STATE_PRESENT,
                                      DXGI_FORMAT_R8G8B8A8_UNORM,
                                      1,
                                      1,
                                      0);
        GrBackendRenderTarget* renderTarget = new GrBackendRenderTarget(width, height, info);
        return toJavaPointer(renderTarget);
    }

    void defineHardwareAdapter(IDXGIFactory4 *pFactory, IDXGIAdapter1 **ppAdapter)
    {
        *ppAdapter = nullptr;
        for (UINT adapterIndex = 0;; ++adapterIndex)
        {
            IDXGIAdapter1 *pAdapter = nullptr;
            if (DXGI_ERROR_NOT_FOUND == pFactory->EnumAdapters1(adapterIndex, &pAdapter))
            {
                break;
            }
            if (SUCCEEDED(D3D12CreateDevice(pAdapter, D3D_FEATURE_LEVEL_11_0, _uuidof(ID3D12Device), nullptr)))
            {
                *ppAdapter = pAdapter;
                return;
            }
            pAdapter->Release();
        }
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_createDevice(
        JNIEnv *env, jobject redrawer, jlong windowHandle)
    {
        DirectXDevice *d3dDevice = new DirectXDevice();

        gr_cp<IDXGIFactory4> deviceFactory;
        if (!SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&deviceFactory))))
        {
            return 0;
        }
        gr_cp<IDXGIAdapter1> hardwareAdapter;
        defineHardwareAdapter(deviceFactory.get(), &hardwareAdapter);
        gr_cp<ID3D12Device> device;
        if (!SUCCEEDED(D3D12CreateDevice(hardwareAdapter.get(), D3D_FEATURE_LEVEL_11_0, IID_PPV_ARGS(&device))))
        {
            return 0;
        }

        // Create the command queue
        gr_cp<ID3D12CommandQueue> queue;
        D3D12_COMMAND_QUEUE_DESC queueDesc = {};
        queueDesc.Flags = D3D12_COMMAND_QUEUE_FLAG_NONE;
        queueDesc.Type = D3D12_COMMAND_LIST_TYPE_DIRECT;

        if (!SUCCEEDED(device->CreateCommandQueue(&queueDesc, IID_PPV_ARGS(&queue))))
        {
            return 0;
        }

        d3dDevice->backendContext.fAdapter = hardwareAdapter;
        d3dDevice->backendContext.fDevice = device;
        d3dDevice->backendContext.fQueue = queue;

        d3dDevice->device = device;
        d3dDevice->queue = queue;

        // Make the swapchain
        HWND fWindow = (HWND)windowHandle;

        gr_cp<IDXGIFactory4> swapChainFactory;
        GR_D3D_CALL_ERRCHECK(CreateDXGIFactory2(0, IID_PPV_ARGS(&swapChainFactory)));

        DXGI_SWAP_CHAIN_DESC1 swapChainDesc = {};
        swapChainDesc.BufferCount = BuffersCount;
        swapChainDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
        swapChainDesc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
        swapChainDesc.SwapEffect = DXGI_SWAP_EFFECT_FLIP_DISCARD;
        swapChainDesc.SampleDesc.Count = 1;

        DXGI_SWAP_CHAIN_FULLSCREEN_DESC swapChainFSDesc = {};
        swapChainFSDesc.Windowed = TRUE;

        gr_cp<IDXGISwapChain1> swapChain;
        GR_D3D_CALL_ERRCHECK(swapChainFactory->CreateSwapChainForHwnd(d3dDevice->queue.get(), fWindow, &swapChainDesc, &swapChainFSDesc, nullptr, &swapChain));
        GR_D3D_CALL_ERRCHECK(swapChain->QueryInterface(IID_PPV_ARGS(&d3dDevice->swapChain)));
        GR_D3D_CALL_ERRCHECK(d3dDevice->device->CreateFence(0, D3D12_FENCE_FLAG_NONE, IID_PPV_ARGS(&d3dDevice->fence)));
        d3dDevice->fenceEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);

        return toJavaPointer(d3dDevice);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_resizeBuffers(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice*>(devicePtr);
        if (!d3dDevice->isSizeEqualTo(width, height))
        {
            GR_D3D_CALL_ERRCHECK(d3dDevice->swapChain->ResizeBuffers(BuffersCount, width, height, DXGI_FORMAT_R8G8B8A8_UNORM, 0));
            d3dDevice->bufferWidth = width;
            d3dDevice->bufferHeight = height;
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_finishFrame(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr, jlong surfacePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice*>(devicePtr);

        SkSurface *surface = fromJavaPointer<SkSurface*>(surfacePtr);
        surface->flushAndSubmit();
        GrDirectContext *fContext = fromJavaPointer<GrDirectContext*>(contextPtr);
        surface->flush(SkSurface::BackendSurfaceAccess::kPresent, GrFlushInfo());
        fContext->flush({});
        fContext->submit(true);
        GR_D3D_CALL_ERRCHECK(d3dDevice->swapChain->Present(1, 0));

        const UINT64 fence = d3dDevice->fenceValue;
        GR_D3D_CALL_ERRCHECK(d3dDevice->queue->Signal(d3dDevice->fence.get(), fence));
        d3dDevice->fenceValue++;
        if (d3dDevice->fence->GetCompletedValue() < fence)
        {
            GR_D3D_CALL_ERRCHECK(d3dDevice->fence->SetEventOnCompletion(fence, d3dDevice->fenceEvent));
            WaitForSingleObjectEx(d3dDevice->fenceEvent, INFINITE, FALSE);
        }
    }
}

#endif
