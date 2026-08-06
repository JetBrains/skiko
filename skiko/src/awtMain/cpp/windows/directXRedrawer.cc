#ifdef SK_DIRECT3D
#include <locale>
#include <algorithm>
#include <Windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"
#include "exceptions_handler.h"
#include "window_util.h"
#include "winApiEdtInvoker.h"

#include "SkColorSpace.h"
#include "ganesh/GrBackendSurface.h"
#include "ganesh/GrDirectContext.h"
#include "ganesh/d3d/GrD3DDirectContext.h"
#include "SkSurface.h"
#include "include/gpu/ganesh/SkSurfaceGanesh.h"
#include "interop.hh"
#include "DCompLibrary.h"

#include "ganesh/d3d/GrD3DTypes.h"
#include "ganesh/d3d/GrD3DBackendSurface.h"
#include <d3d12sdklayers.h>
#include "ganesh/d3d/GrD3DBackendContext.h"
#include <d3d12.h>
#include <dxgi1_4.h>
#include <dxgi1_6.h>

// Set in JNI_OnLoad (jvmMain/cpp/common/impl/Library.cc).
extern "C" JavaVM *jvm;

const int BuffersCount = 2;

class DirectXDevice
{
public:
    HWND hWnd; // Handle of native view.
    GrD3DBackendContext backendContext;
    gr_cp<ID3D12Device> device;
    gr_cp<IDXGISwapChain3> swapChain;
    gr_cp<ID3D12CommandQueue> queue;
    gr_cp<ID3D12Resource> buffers[BuffersCount];
    gr_cp<ID3D12Fence> fence;
    gr_cp<IDCompositionDevice> dcDevice;
    gr_cp<IDCompositionTarget> dcTarget;
    gr_cp<IDCompositionVisual> dcVisual;
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

    void initSwapChain(UINT width, UINT height, jboolean transparency, jboolean preferNoneScaling) {
        gr_cp<IDXGIFactory4> swapChainFactory4;
        gr_cp<IDXGISwapChain1> swapChain1;
        CreateDXGIFactory2(0, IID_PPV_ARGS(&swapChainFactory4));
        HRESULT result = S_OK;
        // NONE is safe only behind the live-resize pre-render, which fills the content at every new size. Otherwise
        // it would expose a hard uncovered edge on any size change (maximize/snap/DPI/async).
        DXGI_SCALING scaling = preferNoneScaling ? DXGI_SCALING_NONE : DXGI_SCALING_STRETCH;
        if (transparency) {
            result = CreateSwapChainForComposition(swapChainFactory4.get(), width, height, scaling, &swapChain1);
        }
        if (!transparency || FAILED(result)) {
            /*
             * It's just a fallback path that added for compatibility.
             * In this case transparency won't be supported.
             */
            swapChain1.reset(nullptr);
            CreateSwapChainForHwnd(swapChainFactory4.get(), width, height, scaling, &swapChain1);
        }
        swapChainFactory4->MakeWindowAssociation(hWnd, DXGI_MWA_NO_ALT_ENTER);
        swapChain1->QueryInterface(IID_PPV_ARGS(&swapChain));
        swapChainFactory4.reset(nullptr);
    }

private:
    HRESULT CreateSwapChainForComposition(IDXGIFactory4 *swapChainFactory4, UINT width, UINT height, DXGI_SCALING scaling, IDXGISwapChain1 **swapChain1) {
        DXGI_SWAP_CHAIN_DESC1 swapChainDesc = {};
        swapChainDesc.Width = width;
        swapChainDesc.Height = height;
        swapChainDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
        swapChainDesc.SampleDesc.Count = 1;
        swapChainDesc.SampleDesc.Quality = 0;
        swapChainDesc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
        swapChainDesc.BufferCount = BuffersCount;
        swapChainDesc.Scaling = scaling;
        swapChainDesc.SwapEffect = DXGI_SWAP_EFFECT_FLIP_DISCARD;
        swapChainDesc.AlphaMode = DXGI_ALPHA_MODE_PREMULTIPLIED;
        HRESULT result = swapChainFactory4->CreateSwapChainForComposition(queue.get(), &swapChainDesc, nullptr, swapChain1);
        if (FAILED(result)) { return result; }

        result = DCompLibrary::DCompositionCreateDevice(0, IID_PPV_ARGS(&dcDevice));
        if (FAILED(result)) { return result; }
        result = dcDevice->CreateTargetForHwnd(hWnd, true, &dcTarget);
        if (FAILED(result)) { return result; }
        result = dcDevice->CreateVisual(&dcVisual);
        if (FAILED(result)) { return result; }
        result = dcVisual->SetContent(*swapChain1);
        if (FAILED(result)) { return result; }
        result = dcTarget->SetRoot(dcVisual.get());
        if (FAILED(result)) { return result; }
        result = dcDevice->Commit();
        if (FAILED(result)) { return result; }

        return S_OK;
    }

    HRESULT CreateSwapChainForHwnd(IDXGIFactory4 *swapChainFactory4, UINT width, UINT height, DXGI_SCALING scaling, IDXGISwapChain1 **swapChain1) {
        DXGI_SWAP_CHAIN_DESC1 swapChainDesc = {};
        swapChainDesc.Width = width;
        swapChainDesc.Height = height;
        swapChainDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
        swapChainDesc.SampleDesc.Count = 1;
        swapChainDesc.SampleDesc.Quality = 0;
        swapChainDesc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
        swapChainDesc.BufferCount = BuffersCount;
        swapChainDesc.Scaling = scaling;
        swapChainDesc.SwapEffect = DXGI_SWAP_EFFECT_FLIP_DISCARD;
        return swapChainFactory4->CreateSwapChainForHwnd(queue.get(), hWnd, &swapChainDesc, nullptr, nullptr, swapChain1);
    }
};

// ===================== Direct3D synchronous live-resize =====================
// Renders and presents the content synchronously inside WM_NCCALCSIZE, before the new geometry commits, so DWM never
// composites a frame we haven't painted yet.

struct LiveResizeState {
    WNDPROC originalProc = nullptr;
    WNDPROC originalContentProc = nullptr;
    HWND frameHwnd = nullptr;
    HWND contentHwnd = nullptr;
    jobject redrawer = nullptr;
    SIZE lastFrameClientSize = {};
    SIZE enforcedChildSize = {};    // what the child is held at during a drag; see enforcedChildSizeForResizeStep
    bool inSizeMoveLoop = false;    // WM_ENTERSIZEMOVE..WM_EXITSIZEMOVE, which covers plain moves too
    bool liveResizeEngaged = false; // ...whereas this means an actual resize
    bool detached = false;          // uninstalled, but a proc we couldn't remove still needs us to forward; see below
};

static const wchar_t *kLiveResizeStateProp = L"SkikoLiveResizeState";

static LiveResizeState *liveResizeStateFor(HWND hWnd)
{
    return reinterpret_cast<LiveResizeState *>(GetPropW(hWnd, kLiveResizeStateProp));
}

// Points hWnd at [ours] and returns the proc it replaced.
// Not SetWindowSubclass, which would chain more cleanly: it must be called from the thread that OWNS the window,
// and we install from the EDT while the AWT frame HWND belongs to the toolkit thread.
static WNDPROC installWndProcHook(HWND hWnd, LiveResizeState *state, WNDPROC ours) {
    if (!hWnd) return nullptr;
    SetPropW(hWnd, kLiveResizeStateProp, (HANDLE)state);
    const WNDPROC original = (WNDPROC)GetWindowLongPtrW(hWnd, GWLP_WNDPROC);
    SetWindowLongPtrW(hWnd, GWLP_WNDPROC, (LONG_PTR)ours);
    return original;
}

// Undoes installWndProcHook. Returns whether successful.
static bool uninstallWndProcHook(HWND hWnd, WNDPROC ours, WNDPROC original) {
    if (!hWnd || !IsWindow(hWnd)) return true;
    if ((WNDPROC)GetWindowLongPtrW(hWnd, GWLP_WNDPROC) != ours) {
        return false;
    }
    SetWindowLongPtrW(hWnd, GWLP_WNDPROC, (LONG_PTR)original);
    RemovePropW(hWnd, kLiveResizeStateProp);
    return true;
}

// Hands a message on to the proc we replaced. A null [original] means we couldn't find our state at all, so there's
// nothing left to hand it to and DefWindowProc is the best available - see the procs below.
static LRESULT forwardToOriginal(WNDPROC original, HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    return original ? CallWindowProcW(original, hWnd, msg, wParam, lParam)
                    : DefWindowProcW(hWnd, msg, wParam, lParam);
}

static JNIEnv *getJniEnv() {
    if (!jvm) return nullptr;
    JNIEnv *env = nullptr;
    if (jvm->GetEnv((void **)&env, JNI_VERSION_1_6) == JNI_EDETACHED)
        jvm->AttachCurrentThread((void **)&env, nullptr);
    return env;
}

static void javaOnLiveResizeStarted(LiveResizeState *s) {
    if (!s->redrawer) return;
    JNIEnv *env = getJniEnv();
    if (!env) return;
    static jmethodID mid = nullptr;
    if (!mid) {
        jclass cls = env->GetObjectClass(s->redrawer);
        mid = env->GetMethodID(cls, "onLiveResizeStarted", "()V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(s->redrawer, mid);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

static void javaOnLiveResizeEnded(LiveResizeState *s) {
    if (!s->redrawer) return;
    JNIEnv *env = getJniEnv();
    if (!env) return;
    static jmethodID mid = nullptr;
    if (!mid) {
        jclass cls = env->GetObjectClass(s->redrawer);
        mid = env->GetMethodID(cls, "onLiveResizeEnded", "()V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(s->redrawer, mid);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

static void javaDrawFrameWhileLiveResizing(LiveResizeState *s, bool isResizeFrame) {
    if (!s->redrawer) return;
    JNIEnv *env = getJniEnv();
    if (!env) return;
    static jmethodID mid = nullptr;
    if (!mid) {
        jclass cls = env->GetObjectClass(s->redrawer);
        mid = env->GetMethodID(cls, "drawFrameWhileLiveResizing", "(IIZ)V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(s->redrawer, mid, (jint)s->lastFrameClientSize.cx, (jint)s->lastFrameClientSize.cy, (jboolean)isResizeFrame);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

// Keep the child at the maximum size at each resize step to avoid a lagging (when growing)
// or an early (when shrinking) part of the pipeline from drawing or clipping at a too-small size.
static SIZE enforcedChildSizeForResizeStep(SIZE pending, SIZE committed) {
    return { std::max(pending.cx, committed.cx), std::max(pending.cy, committed.cy) };
}

static void applyEnforcedChildSize(LiveResizeState *s) {
    if (!s->contentHwnd) return;
    SetWindowPos(s->contentHwnd, nullptr, 0, 0, s->enforcedChildSize.cx, s->enforcedChildSize.cy,
                 SWP_NOMOVE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_NOOWNERZORDER);
}

// DXGI clips the presented buffer to the child, so the child's size AT THE PRESENT bounds what reaches the screen.
// This prevents the wrong size from being applied to the child during live resize.
static LRESULT CALLBACK LiveResizeContentWndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    LiveResizeState *s = liveResizeStateFor(hWnd);
    if (!s || s->detached) return forwardToOriginal(s ? s->originalContentProc : nullptr, hWnd, msg, wParam, lParam);
    if (msg == WM_WINDOWPOSCHANGING && s->liveResizeEngaged) {
        WINDOWPOS *p = (WINDOWPOS *)lParam;
        if (!(p->flags & SWP_NOSIZE))
        {
            p->cx = static_cast<int>(s->enforcedChildSize.cx);
            p->cy = static_cast<int>(s->enforcedChildSize.cy);
        }
    }
    const LRESULT result = forwardToOriginal(s->originalContentProc, hWnd, msg, wParam, lParam);
    if (msg == WM_WINDOWPOSCHANGED && s->liveResizeEngaged) {
        // AWT can leave a stale child region during live resize, causing visual artifacts.
        SetWindowRgn(hWnd, nullptr, FALSE);
    }
    return result;
}

static LRESULT CALLBACK LiveResizeWndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    LiveResizeState *s = liveResizeStateFor(hWnd);
    if (!s || s->detached) return forwardToOriginal(s ? s->originalProc : nullptr, hWnd, msg, wParam, lParam);
    switch (msg)
    {
        case WM_ERASEBKGND:
            if (s->inSizeMoveLoop) return 1;
            break;
        case WM_ENTERSIZEMOVE: {
            s->inSizeMoveLoop = true;
            s->liveResizeEngaged = false;
            RECT rc; GetClientRect(hWnd, &rc);
            const SIZE clientSize = { rc.right - rc.left, rc.bottom - rc.top };
            s->enforcedChildSize = enforcedChildSizeForResizeStep(clientSize, clientSize);
            s->lastFrameClientSize = clientSize;
            break;
        }
        case WM_NCCALCSIZE: {
            LRESULT r = forwardToOriginal(s->originalProc, hWnd, msg, wParam, lParam);
            // isPumpingEdt(): our own render re-enters here, because the EDT's SetWindowPos is SENT back to this
            // thread. Starting a second round-trip would deadlock against the EDT already blocked in SendMessage.
            if (s->inSizeMoveLoop && wParam && !isPumpingEdt())
            {
                if (!s->liveResizeEngaged)
                {
                    s->liveResizeEngaged = true;
                    javaOnLiveResizeStarted(s); // must quiesce the async EDT renders before the first render here
                }
                NCCALCSIZE_PARAMS *p = (NCCALCSIZE_PARAMS *)lParam;
                RECT c = p->rgrc[0];
                RECT committed; GetClientRect(hWnd, &committed);
                const SIZE pendingSize = { c.right - c.left, c.bottom - c.top };
                s->enforcedChildSize = enforcedChildSizeForResizeStep(
                    pendingSize, 
                    { committed.right - committed.left, committed.bottom - committed.top }
                );
                s->lastFrameClientSize = pendingSize;
                applyEnforcedChildSize(s);
                javaDrawFrameWhileLiveResizing(s, /*isResizeFrame*/ true);
            }
            return r;
        }
        case WM_PAINT:
            // Drives frames while the drag is paused and no WM_NCCALCSIZE fires. It has to be WM_PAINT: a
            // self-reposting POSTED message outranks the modal loop's queued input and locks the drag up, and
            // peek-and-yield is no alternative because that loop's input is invisible to PeekMessage from in here
            // (both tried). Only WM_PAINT sits below input and so cannot starve the drag. needRender re-arms it by
            // invalidating the frame.
            if (s->inSizeMoveLoop && s->liveResizeEngaged && !isPumpingEdt()) {
                ValidateRect(hWnd, nullptr); // before rendering, so the re-arm below isn't cleared
                // A hold is where Swing's async doLayout lands, so the size has to be re-asserted here too.
                applyEnforcedChildSize(s);
                javaDrawFrameWhileLiveResizing(s, /*isResizeFrame*/ false);
                return 0;
            }
            break;
        case WM_EXITSIZEMOVE:
            s->inSizeMoveLoop = false;
            if (s->liveResizeEngaged) {
                s->liveResizeEngaged = false;
                // Ensure the client size is correct when exiting live resize mode
                if (s->contentHwnd) {
                    RECT rc; GetClientRect(hWnd, &rc);
                    SetWindowPos(s->contentHwnd, nullptr, 0, 0, rc.right - rc.left, rc.bottom - rc.top,
                                 SWP_NOMOVE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_NOOWNERZORDER);
                }
                javaOnLiveResizeEnded(s);
            }
            break;
    }
    return forwardToOriginal(s->originalProc, hWnd, msg, wParam, lParam);
}
// ===================== end Direct3D synchronous live-resize =====================

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

    bool isAdapterSupported(JNIEnv *env, jobject redrawer, IDXGIAdapter1 *hardwareAdapter) {
        DXGI_ADAPTER_DESC1 desc;
        hardwareAdapter->GetDesc1(&desc);
        if ((desc.Flags & DXGI_ADAPTER_FLAG_SOFTWARE) != 0) {
            return false;
        }

        std::wstring tmp(desc.Description);
        std::string name(tmp.begin(), tmp.end());
        jstring jname = env->NewStringUTF(name.c_str());

        static jclass cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/redrawer/Direct3DRedrawer"));
        static jmethodID method = env->GetMethodID(cls, "isAdapterSupported", "(Ljava/lang/String;)Z");

        return env->CallBooleanMethod(redrawer, method, jname);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_chooseAdapter(
            JNIEnv *env, jobject redrawer, jint adapterPriority) {
        gr_cp<IDXGIFactory4> deviceFactory;
        if (!SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&deviceFactory)))) {
            return 0;
        }

        gr_cp<IDXGIFactory6> factory6;
        if (!SUCCEEDED(deviceFactory->QueryInterface(IID_PPV_ARGS(&factory6)))) {
            return 0;
        }

        for (UINT adapterIndex = 0;; ++adapterIndex) {
            IDXGIAdapter1 *adapter = nullptr;
            if (!SUCCEEDED(factory6->EnumAdapterByGpuPreference(adapterIndex, (DXGI_GPU_PREFERENCE) adapterPriority, IID_PPV_ARGS(&adapter)))) {
                break;
            }
            if (
                SUCCEEDED(D3D12CreateDevice(adapter, D3D_FEATURE_LEVEL_11_0, _uuidof(ID3D12Device), nullptr)) &&
                isAdapterSupported(env, redrawer, adapter)
            ) {
                return toJavaPointer(adapter);
            } else {
                adapter->Release();
            }
        }

        return 0;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_createDirectXDevice(
        JNIEnv *env, jobject redrawer, jlong adapterPtr, jlong contentHandle, jboolean transparency) {
        gr_cp<IDXGIFactory4> deviceFactory;
        if (!SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&deviceFactory)))) {
            return 0;
        }
        if (adapterPtr == 0) {
            return 0;
        }
        gr_cp<IDXGIAdapter1> adapter((IDXGIAdapter1 *) adapterPtr);

        D3D_FEATURE_LEVEL maxSupportedFeatureLevel = D3D_FEATURE_LEVEL_12_0;
        D3D_FEATURE_LEVEL featureLevels[] = {
            // TODO add D3D_FEATURE_LEVEL_12_2
            D3D_FEATURE_LEVEL_12_1,
            D3D_FEATURE_LEVEL_12_0
        };

        for (int i = 0; i < _countof(featureLevels); i++) {
            if (SUCCEEDED(D3D12CreateDevice(adapter.get(), featureLevels[i], _uuidof(ID3D12Device), nullptr))) {
                maxSupportedFeatureLevel = featureLevels[i];
                break;
            }
        }

        gr_cp<ID3D12Device> device;
        if (!SUCCEEDED(D3D12CreateDevice(adapter.get(), maxSupportedFeatureLevel, IID_PPV_ARGS(&device)))) {
            return 0;
        }

        // Create the command queue
        gr_cp<ID3D12CommandQueue> queue;
        D3D12_COMMAND_QUEUE_DESC queueDesc = {};
        queueDesc.Flags = D3D12_COMMAND_QUEUE_FLAG_NONE;
        queueDesc.Type = D3D12_COMMAND_LIST_TYPE_DIRECT;

        if (!SUCCEEDED(device->CreateCommandQueue(&queueDesc, IID_PPV_ARGS(&queue)))) {
            return 0;
        }

        HWND hWnd = fromJavaPointer<HWND>(contentHandle);
        DirectXDevice *d3dDevice = new DirectXDevice();
        d3dDevice->backendContext.fAdapter = adapter;
        d3dDevice->backendContext.fDevice = device;
        d3dDevice->backendContext.fQueue = queue;
        d3dDevice->backendContext.fProtectedContext = GrProtected::kNo;

        d3dDevice->device = device;
        d3dDevice->queue = queue;
        d3dDevice->hWnd = hWnd;

        if (transparency) {
            const LONG style = GetWindowLong(hWnd, GWL_EXSTYLE);
            SetWindowLong(hWnd, GWL_EXSTYLE, style | WS_EX_TRANSPARENT);
        }

        return toJavaPointer(d3dDevice);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_initSwapChain(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height, jboolean transparency, jboolean preferNoneScaling)
    {
        __try
        {
            DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
            d3dDevice->initSwapChain((UINT) width, (UINT) height, transparency, preferNoneScaling);
        }
        __except(EXCEPTION_EXECUTE_HANDLER) {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_initFence(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        __try
        {
            DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
            for (int i = 0; i < BuffersCount; i++)
            {
                d3dDevice->fenceValues[i] = 10000;
            }
            d3dDevice->device->CreateFence(d3dDevice->fenceValues[0], D3D12_FENCE_FLAG_NONE, IID_PPV_ARGS(&d3dDevice->fence));
            d3dDevice->fenceEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
            SkASSERT(d3dDevice->fenceEvent);
        }
        __except(EXCEPTION_EXECUTE_HANDLER) {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_makeDirectXContext(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        GrD3DBackendContext backendContext = d3dDevice->backendContext;
        return toJavaPointer(GrDirectContexts::MakeD3D(backendContext).release());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_makeDirectXSurface(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr, jint width, jint height, jintArray surfacePropsInts, jint index)
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
        d3dDevice->swapChain->GetBuffer(index, IID_PPV_ARGS(&d3dDevice->buffers[index]));

        info.fResource = d3dDevice->buffers[index];

        std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsInts);
        GrBackendTexture backendTexture = GrBackendTextures::MakeD3D(
                                 (int)d3dDevice->buffers[index]->GetDesc().Width,
                                 (int)d3dDevice->buffers[index]->GetDesc().Height,
                                 info);
        auto result = SkSurfaces::WrapBackendTexture(
                                 context, backendTexture, kTopLeft_GrSurfaceOrigin, 0,
                                 kRGBA_8888_SkColorType, SkColorSpace::MakeSRGB(), surfaceProps.get())
                                 .release();
        return toJavaPointer(result);
    }

    // From the present until the geometry commits, the buffer is the new size and the window still the old one, and
    // DWM must not sample in there. Waiting opens that window at the start of a composition interval, and caps a
    // high-rate mouse at one step per composition.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_waitForComposition(
        JNIEnv *env, jobject redrawer)
    {
        DwmFlush();
    }

    // Arms the WM_PAINT hold path. Repeated calls coalesce into one update region, so no explicit gate is needed.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_postLiveResizeRender(
        JNIEnv *env, jobject redrawer, jlong handle)
    {
        LiveResizeState *s = fromJavaPointer<LiveResizeState *>(handle);
        if (s && s->frameHwnd) InvalidateRect(s->frameHwnd, nullptr, FALSE);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_resizeBuffers(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height)
    {
        __try {
            DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
            for (int i = 0; i < BuffersCount; i++)
            {
                if (d3dDevice->fence->GetCompletedValue() < d3dDevice->fenceValues[i])
                {
                    d3dDevice->fence->SetEventOnCompletion(d3dDevice->fenceValues[i], d3dDevice->fenceEvent);
                    WaitForSingleObjectEx(d3dDevice->fenceEvent, INFINITE, FALSE);
                }
                d3dDevice->buffers[i].reset(nullptr);
            }
            d3dDevice->swapChain->ResizeBuffers(BuffersCount, width, height, DXGI_FORMAT_R8G8B8A8_UNORM, 0);
        }
        __except(EXCEPTION_EXECUTE_HANDLER) {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_swap(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jboolean isVsyncEnabled)
    {
        __try
        {
            DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
            // 1 value in [Present(1, 0)] enables vblank wait so this is how vertical sync works in DirectX.
            const UINT64 fenceValue = d3dDevice->fenceValues[d3dDevice->bufferIndex];
            d3dDevice->swapChain->Present((int)isVsyncEnabled, 0);
            d3dDevice->queue->Signal(d3dDevice->fence.get(), fenceValue);
        }
        __except(EXCEPTION_EXECUTE_HANDLER) {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
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
        __try {
            DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
            const UINT64 fenceValue = d3dDevice->fenceValues[d3dDevice->bufferIndex];
            d3dDevice->bufferIndex = d3dDevice->swapChain->GetCurrentBackBufferIndex();
            if (d3dDevice->fence->GetCompletedValue() < fenceValue)
            {
                d3dDevice->fence->SetEventOnCompletion(fenceValue, d3dDevice->fenceEvent);
                WaitForSingleObjectEx(d3dDevice->fenceEvent, INFINITE, FALSE);
            }
            d3dDevice->fenceValues[d3dDevice->bufferIndex] = fenceValue + 1;
            return d3dDevice->bufferIndex;
        }
        __except(EXCEPTION_EXECUTE_HANDLER) {
            auto code = GetExceptionCode();
            throwJavaRenderExceptionByExceptionCode(env, __FUNCTION__, code);
        }
    }

    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_getAdapterName(JNIEnv *env, jobject redrawer, jlong adapterPtr)
    {
        IDXGIAdapter1 *adapter = fromJavaPointer<IDXGIAdapter1 *>(adapterPtr);

        DXGI_ADAPTER_DESC1 desc;
        adapter->GetDesc1(&desc);
        std::wstring w_tmp(desc.Description);
        std::string currentAdapterName(w_tmp.begin(), w_tmp.end());
        return env->NewStringUTF(currentAdapterName.c_str());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_getAdapterMemorySize(JNIEnv *env, jobject redrawer, jlong adapterPtr)
    {
        IDXGIAdapter1 *adapter = fromJavaPointer<IDXGIAdapter1 *>(adapterPtr);

        DXGI_ADAPTER_DESC1 desc;
        adapter->GetDesc1(&desc);
        __int64 result = desc.DedicatedVideoMemory;
        return (jlong)result;
    }

    // Returns the state as an opaque handle (0 on failure) for the two calls below.
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_installLiveResizeHook(
        JNIEnv *env, jobject redrawer, jlong windowPtr, jlong contentPtr)
    {
        HWND top = GetAncestor(fromJavaPointer<HWND>(windowPtr), GA_ROOT);
        if (!top) return 0;
        if (liveResizeStateFor(top)) return 0; // a second install would capture our own proc and recurse forever

        LiveResizeState *state = new LiveResizeState();
        state->frameHwnd = top;
        state->contentHwnd = fromJavaPointer<HWND>(contentPtr);
        state->redrawer = env->NewGlobalRef(redrawer);
        state->originalProc = installWndProcHook(top, state, LiveResizeWndProc);
        state->originalContentProc = installWndProcHook(state->contentHwnd, state, LiveResizeContentWndProc);
        return toJavaPointer(state);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_uninstallLiveResizeHook(
        JNIEnv *env, jobject redrawer, jlong handle)
    {
        LiveResizeState *state = fromJavaPointer<LiveResizeState *>(handle);
        if (!state) return;
        const bool frameUnhooked = uninstallWndProcHook(state->frameHwnd, LiveResizeWndProc, state->originalProc);
        const bool contentUnhooked =
            uninstallWndProcHook(state->contentHwnd, LiveResizeContentWndProc, state->originalContentProc);
        // A proc we couldn't unlink is still in the window's chain, so the state has to outlive us for it to forward
        // through. So instead of deleting the state, we mark it as detached. It leaks the state object, but it's
        // better than the alternative
        state->detached = !frameUnhooked || !contentUnhooked;
        if (state->redrawer) env->DeleteGlobalRef(state->redrawer);
        state->redrawer = nullptr;
        if (!state->detached)
        {
            delete state;
        }
    }
}

#endif
