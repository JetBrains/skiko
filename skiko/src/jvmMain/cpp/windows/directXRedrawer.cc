#ifdef SK_DIRECT3D
#include <stdexcept>
#include <locale>
#include <Windows.h>
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
#include <dxgi1_6.h>

#define GR_D3D_CALL_ERRCHECK(X)                                        \
    do                                                                 \
    {                                                                  \
        HRESULT result = X;                                            \
        SkASSERT(SUCCEEDED(result));                                   \
        if (!SUCCEEDED(result))                                        \
        {                                                              \
            SkDebugf("Failed Direct3D call. Error: 0x%08x\n", result); \
            throw std::exception("ERROR");                             \
        }                                                              \
    } while (false)

const int BuffersCount = 2;

// This is a blacklist of graphics cards that have rendering issues (black screen, flickering)
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

class DirectXDevice
{
public:
    HWND window;
    GrD3DBackendContext backendContext;
    gr_cp<ID3D12Device> device;
    gr_cp<IDXGISwapChain3> swapChain;
    gr_cp<ID3D12CommandQueue> queue;
    gr_cp<ID3D12Resource> buffers[BuffersCount];
    gr_cp<ID3D12Fence> fence;
    uint64_t fenceValues[BuffersCount];
    HANDLE fenceEvent = NULL;
    unsigned int bufferIndex;

    ~DirectXDevice()
    {
        if (fenceEvent != NULL)
        {
            CloseHandle(fenceEvent);
        }
        for (int i = 0; i < BuffersCount; i++)
        {
            buffers[i].reset(nullptr);
        }
        fence.reset(nullptr);
        swapChain.reset(nullptr);
        queue.reset(nullptr);
        device.reset(nullptr);
    }
};

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

    bool isSupported(IDXGIAdapter1 *hardwareAdapter)
    {
        DXGI_ADAPTER_DESC1 desc;
        hardwareAdapter->GetDesc1(&desc);
        if ((desc.Flags & DXGI_ADAPTER_FLAG_SOFTWARE) != 0)
        {
            return false;
        }
        std::wstring currentAdapterName(desc.Description);
        for (std::wstring name : notSupportedAdapters)
        {
            if (currentAdapterName == name)
            {
                fwprintf(stderr, L"Graphics card: %s is not supported.\n", name.c_str());
                return false;
            }
        }
        return true;
    }

    bool defineHardwareAdapter(DXGI_GPU_PREFERENCE adapterPriority, IDXGIFactory4 *pFactory, IDXGIAdapter1 **ppAdapter)
    {
        *ppAdapter = nullptr;

#if defined(__dxgi1_6_h__) && defined(NTDDI_WIN10_RS4)
        gr_cp<IDXGIFactory6> factory6;
        if (SUCCEEDED(pFactory->QueryInterface(IID_PPV_ARGS(&factory6))))
        {
            for (UINT adapterIndex = 0;; ++adapterIndex)
            {
                IDXGIAdapter1 *pAdapter = nullptr;
                if (!SUCCEEDED(factory6->EnumAdapterByGpuPreference(adapterIndex, adapterPriority, IID_PPV_ARGS(&pAdapter))))
                {
                    break;
                }
                if (SUCCEEDED(D3D12CreateDevice(pAdapter, D3D_FEATURE_LEVEL_11_0, _uuidof(ID3D12Device), nullptr)))
                {
                    if (!isSupported(pAdapter))
                    {
                        pAdapter->Release();
                        continue;
                    }
                    *ppAdapter = pAdapter;
                    return true;
                }
                pAdapter->Release();
            }
        }
#endif

        if (*ppAdapter == nullptr)
        {
            for (UINT adapterIndex = 0;; ++adapterIndex)
            {
                IDXGIAdapter1 *pAdapter = nullptr;
                if (DXGI_ERROR_NOT_FOUND == pFactory->EnumAdapters1(adapterIndex, &pAdapter))
                {
                    break;
                }
                if (SUCCEEDED(D3D12CreateDevice(pAdapter, D3D_FEATURE_LEVEL_11_0, _uuidof(ID3D12Device), nullptr)))
                {
                    if (!isSupported(pAdapter))
                    {
                        pAdapter->Release();
                        continue;
                    }
                    *ppAdapter = pAdapter;
                    return true;
                }
                pAdapter->Release();
            }
        }
        return false;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_createDirectXDevice(
        JNIEnv *env, jobject redrawer, jint adapterPriority, jlong contentHandle)
    {
        gr_cp<IDXGIFactory4> deviceFactory;
        if (!SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&deviceFactory))))
        {
            return 0;
        }
        gr_cp<IDXGIAdapter1> hardwareAdapter;
        if (!defineHardwareAdapter((DXGI_GPU_PREFERENCE) adapterPriority, deviceFactory.get(), &hardwareAdapter))
        {
            return 0;
        }
        gr_cp<ID3D12Device> device;
        if (!SUCCEEDED(D3D12CreateDevice(hardwareAdapter.get(), D3D_FEATURE_LEVEL_11_0, IID_PPV_ARGS(&device))))
        {
            return 0;
        }
        deviceFactory.reset(nullptr);

        // Create the command queue
        gr_cp<ID3D12CommandQueue> queue;
        D3D12_COMMAND_QUEUE_DESC queueDesc = {};
        queueDesc.Flags = D3D12_COMMAND_QUEUE_FLAG_NONE;
        queueDesc.Type = D3D12_COMMAND_LIST_TYPE_DIRECT;

        if (!SUCCEEDED(device->CreateCommandQueue(&queueDesc, IID_PPV_ARGS(&queue))))
        {
            return 0;
        }

        DirectXDevice *d3dDevice = new DirectXDevice();
        d3dDevice->backendContext.fAdapter = hardwareAdapter;
        d3dDevice->backendContext.fDevice = device;
        d3dDevice->backendContext.fQueue = queue;
        d3dDevice->backendContext.fProtectedContext = GrProtected::kNo;

        d3dDevice->device = device;
        d3dDevice->queue = queue;
        d3dDevice->window = (HWND)contentHandle;

        return toJavaPointer(d3dDevice);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_initSwapChain(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);

        // Make the swapchain
        gr_cp<IDXGIFactory4> swapChainFactory;
        GR_D3D_CALL_ERRCHECK(CreateDXGIFactory2(0, IID_PPV_ARGS(&swapChainFactory)));

        DXGI_SWAP_CHAIN_DESC1 swapChainDesc = {};
        swapChainDesc.BufferCount = BuffersCount;
        swapChainDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
        swapChainDesc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
        swapChainDesc.SwapEffect = DXGI_SWAP_EFFECT_FLIP_DISCARD;
        swapChainDesc.SampleDesc.Count = 1;
        swapChainDesc.Scaling = DXGI_SCALING_NONE;

        gr_cp<IDXGISwapChain1> swapChain;
        GR_D3D_CALL_ERRCHECK(swapChainFactory->CreateSwapChainForHwnd(d3dDevice->queue.get(), d3dDevice->window, &swapChainDesc, nullptr, nullptr, &swapChain));
        GR_D3D_CALL_ERRCHECK(swapChainFactory->MakeWindowAssociation(d3dDevice->window, DXGI_MWA_NO_ALT_ENTER));
        GR_D3D_CALL_ERRCHECK(swapChain->QueryInterface(IID_PPV_ARGS(&d3dDevice->swapChain)));

        RECT windowRect;
        GetWindowRect(d3dDevice->window, &windowRect);
        unsigned int w = windowRect.right - windowRect.left;
        unsigned int h = windowRect.bottom - windowRect.top;
        GR_D3D_CALL_ERRCHECK(d3dDevice->swapChain->ResizeBuffers(BuffersCount, w, h, DXGI_FORMAT_R8G8B8A8_UNORM, 0));

        swapChainFactory.reset(nullptr);
        swapChainFactory.reset(nullptr);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_initFence(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        for (int i = 0; i < BuffersCount; i++)
        {
            d3dDevice->fenceValues[i] = 10000;
        }
        GR_D3D_CALL_ERRCHECK(d3dDevice->device->CreateFence(d3dDevice->fenceValues[0],
                                                            D3D12_FENCE_FLAG_NONE,
                                                            IID_PPV_ARGS(&d3dDevice->fence)));
        d3dDevice->fenceEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
        SkASSERT(d3dDevice->fenceEvent);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_makeDirectXContext(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        GrD3DBackendContext backendContext = d3dDevice->backendContext;
        return toJavaPointer(GrDirectContext::MakeDirect3D(backendContext).release());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_makeDirectXSurface(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr, jint width, jint height, jint index)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        GrDirectContext *context = fromJavaPointer<GrDirectContext *>(contextPtr);
        GrD3DTextureResourceInfo info(nullptr,
                                      nullptr,
                                      D3D12_RESOURCE_STATE_PRESENT,
                                      DXGI_FORMAT_R8G8B8A8_UNORM,
                                      1,
                                      1,
                                      0);
        GR_D3D_CALL_ERRCHECK(d3dDevice->swapChain->GetBuffer(index, IID_PPV_ARGS(&d3dDevice->buffers[index])));

        info.fResource = d3dDevice->buffers[index];

        SkSurfaceProps surfaceProps(0, kRGB_H_SkPixelGeometry);
        GrBackendTexture backendTexture((int)d3dDevice->buffers[index]->GetDesc().Width, (int)d3dDevice->buffers[index]->GetDesc().Height, info);
        return toJavaPointer(SkSurface::MakeFromBackendTexture(
                                 context, backendTexture, kTopLeft_GrSurfaceOrigin, 0,
                                 kRGBA_8888_SkColorType, SkColorSpace::MakeSRGB(), &surfaceProps)
                                 .release());
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_resizeBuffers(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        for (int i = 0; i < BuffersCount; i++)
        {
            if (d3dDevice->fence->GetCompletedValue() < d3dDevice->fenceValues[i])
            {
                GR_D3D_CALL_ERRCHECK(d3dDevice->fence->SetEventOnCompletion(d3dDevice->fenceValues[i], d3dDevice->fenceEvent));
                WaitForSingleObjectEx(d3dDevice->fenceEvent, INFINITE, FALSE);
            }
            d3dDevice->buffers[i].reset(nullptr);
        }
        GR_D3D_CALL_ERRCHECK(d3dDevice->swapChain->ResizeBuffers(BuffersCount, width, height, DXGI_FORMAT_R8G8B8A8_UNORM, 0));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_finishFrame(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr, jlong surfacePtr, jboolean isVsyncEnabled)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        SkSurface *surface = fromJavaPointer<SkSurface *>(surfacePtr);
        GrDirectContext *context = fromJavaPointer<GrDirectContext *>(contextPtr);

        surface->flushAndSubmit(true);
        surface->flush(SkSurface::BackendSurfaceAccess::kPresent, GrFlushInfo());
        context->flush({});
        context->submit(true);

        // 1 value in [Present(1, 0)] enables vblank wait so this is how vertical sync works in DirectX.
        const UINT64 fenceValue = d3dDevice->fenceValues[d3dDevice->bufferIndex];
        GR_D3D_CALL_ERRCHECK(d3dDevice->swapChain->Present((int)isVsyncEnabled, 0));
        GR_D3D_CALL_ERRCHECK(d3dDevice->queue->Signal(d3dDevice->fence.get(), fenceValue));
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_disposeDevice(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        delete d3dDevice;
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_getBufferIndex(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        const UINT64 fenceValue = d3dDevice->fenceValues[d3dDevice->bufferIndex];
        d3dDevice->bufferIndex = d3dDevice->swapChain->GetCurrentBackBufferIndex();
        if (d3dDevice->fence->GetCompletedValue() < fenceValue)
        {
            GR_D3D_CALL_ERRCHECK(d3dDevice->fence->SetEventOnCompletion(fenceValue, d3dDevice->fenceEvent));
            WaitForSingleObjectEx(d3dDevice->fenceEvent, INFINITE, FALSE);
        }
        d3dDevice->fenceValues[d3dDevice->bufferIndex] = fenceValue + 1;
        return d3dDevice->bufferIndex;
    }

    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_getAdapterName(JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);

        DXGI_ADAPTER_DESC1 desc;
        d3dDevice->backendContext.fAdapter->GetDesc1(&desc);
        std::wstring w_tmp(desc.Description);
        std::string currentAdapterName(w_tmp.begin(), w_tmp.end());
        jstring result = env->NewStringUTF(currentAdapterName.c_str());
        return result;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_getAdapterMemorySize(JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);

        DXGI_ADAPTER_DESC1 desc;
        d3dDevice->backendContext.fAdapter->GetDesc1(&desc);
        __int64 result = desc.DedicatedVideoMemory;
        return (jlong)result;
    }
}

#endif
