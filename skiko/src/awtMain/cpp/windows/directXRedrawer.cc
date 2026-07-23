#ifdef SK_DIRECT3D
#include <locale>
#include <Windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"
#include "exceptions_handler.h"
#include "window_util.h"
#include "edtInvoker.h"

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

// JavaVM global set in JNI_OnLoad (jvmMain/cpp/common/impl/Library.cc); used to reach the JVM from
// the AWT-Windows toolkit thread that runs the subclassed WndProc.
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
        // Use NONE when the synchronous live-resize hook is installed. The WM_NCCALCSIZE pre-render fills the content
        // each, so NONE removes the scaling jitter). Every other window keeps STRETCH — with no pre-render behind
        // it, a NONE swapchain would expose a hard uncovered edge on any size change (maximize/snap/DPI/async).
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
// The Windows/Direct3D live-resize fix. Subclasses the frame's WndProc and, during an interactive (drag) resize,
// synchronously renders the real renderDelegate content into the window's on-screen swapchain at the new size and
// presents it in WM_NCCALCSIZE / WM_PAINT BEFORE the geometry commits — so content and geometry land together and
// no white bars appear at the edges.
//
// Multi-window: all identity + drag state lives in a heap LiveResizeState attached to the frame HWND (SetProp), so
// any number of windows can be hooked independently — LiveResizeWndProc recovers its own state from the hWnd it is
// handed. (g_monitorOutput below stays process-global on purpose: a shared monitor vblank source, not per-window.
// The reentrancy guard for the EDT pump-wait lives in edtInvoker as isPumpingEdt(), thread-scoped.)
//
// Why raw SetWindowLongPtr(GWLP_WNDPROC) + SetProp and NOT the comctl32 SetWindowSubclass helpers (which would give
// cleaner chaining): SetWindowSubclass must be called from the thread that OWNS the window, but we install from the
// EDT (Direct3DRedrawer is constructed in SkiaLayer.addNotify), while the AWT frame HWND is owned by the separate
// AWT-Windows toolkit thread. SetWindowSubclass silently returns FALSE across threads → the hook never attaches →
// white bars on resize. SetWindowLongPtr(GWLP_WNDPROC) tolerates cross-thread install within a process, so it is
// the correct primitive here. The subclass proc itself still runs on the toolkit thread (where the frame's messages
// are dispatched), exactly as intended.

// Per-hooked-window state, attached to the frame HWND under kLiveResizeStateProp and recovered in LiveResizeWndProc.
struct LiveResizeState
{
    WNDPROC originalProc = nullptr; // the frame's WndProc we subclassed (the chain target)
    HWND frameHwnd = nullptr;       // top-level frame (gets WM_ENTERSIZEMOVE/WM_NCCALCSIZE); owns the prop
    HWND contentHwnd = nullptr;     // the canvas child that must cover the frame during a drag
    jobject redrawer = nullptr;     // global ref to the owning Direct3DRedrawer
    int lastClientWidth = 0;        // client size the last resize step used (GetClientRect lags a step during a drag)
    int lastClientHeight = 0;
    bool inSizeMoveLoop = false;    // inside a WM_ENTERSIZEMOVE..WM_EXITSIZEMOVE modal loop (resize OR move)
    bool liveResizeEngaged = false; // an actual RESIZE is in progress (not a plain move)
};

static const wchar_t *kLiveResizeStateProp = L"SkikoLiveResizeState";

// Recover the per-window state a frame's LiveResizeWndProc was installed with (nullptr if not/never hooked).
static LiveResizeState *liveResizeStateFor(HWND hWnd)
{
    return reinterpret_cast<LiveResizeState *>(GetPropW(hWnd, kLiveResizeStateProp));
}

// Attach the current (toolkit) thread to the JVM if needed and return its JNIEnv (nullptr if unavailable).
static JNIEnv *getJniEnv()
{
    if (!jvm) return nullptr;
    JNIEnv *env = nullptr;
    if (jvm->GetEnv((void **)&env, JNI_VERSION_1_6) == JNI_EDETACHED)
        jvm->AttachCurrentThread((void **)&env, nullptr);
    return env;
}

// One dedicated invoker per Direct3DRedrawer method called from native.
static void javaOnLiveResizeStarted(LiveResizeState *s)
{
    if (!s->redrawer) return;
    JNIEnv *env = getJniEnv();
    if (!env) return;
    static jmethodID mid = nullptr;
    if (!mid)
    {
        jclass cls = env->GetObjectClass(s->redrawer);
        mid = env->GetMethodID(cls, "onLiveResizeStarted", "()V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(s->redrawer, mid);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

// At drag-end: the Kotlin side hops to the EDT (blocking) and forces a layout so the canvas catches up to the final
// client size, renders it, then resumes the normal animation loop.
static void javaOnLiveResizeEnded(LiveResizeState *s)
{
    if (!s->redrawer) return;
    JNIEnv *env = getJniEnv();
    if (!env) return;
    static jmethodID mid = nullptr;
    if (!mid)
    {
        jclass cls = env->GetObjectClass(s->redrawer);
        mid = env->GetMethodID(cls, "onLiveResizeEnded", "()V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(s->redrawer, mid);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

// Render the REAL content at the last recorded client size (s->lastClientWidth/Height) into the on-screen swapchain
// and present it synchronously. The Kotlin side hops to the EDT and blocks there (invokeAndWaitWhilePumping). The
// present itself is always unpaced (Present(0)); the stationary-hold loop is paced separately, natively, by
// waitForPrimaryVBlank() in the WM_PAINT handler (an active drag stays unpaced, driven by mouse-move WM_NCCALCSIZE).
static void javaDrawFrameWhileLiveResizing(LiveResizeState *s)
{
    if (!s->redrawer) return;
    JNIEnv *env = getJniEnv();
    if (!env) return;
    static jmethodID mid = nullptr;
    if (!mid)
    {
        jclass cls = env->GetObjectClass(s->redrawer);
        mid = env->GetMethodID(cls, "drawFrameWhileLiveResizing", "(II)V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(s->redrawer, mid, (jint)s->lastClientWidth, (jint)s->lastClientHeight);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

// ===================== state shared by the live-resize WndProc =====================
static gr_cp<IDXGIOutput> g_monitorOutput; // primary monitor output, for WaitForVBlank pacing

// Block until the primary monitor's next vblank. Used to pace the stationary-hold render loop at the refresh
// rate. Prefer this over Present(1) on a windowed (DWM-composited) flip swapchain: Present(1)'s sync interval
// beats against DWM's own composition cadence and lands the loop at refresh x 2/3 (~40 on a 60 Hz screen);
// pacing on the actual vblank with a non-blocking Present(0) holds a clean refresh rate.
static void waitForPrimaryVBlank()
{
    if (!g_monitorOutput.get())
    {
        gr_cp<IDXGIFactory1> factory;
        if (SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&factory))))
        {
            gr_cp<IDXGIAdapter1> adapter;
            if (SUCCEEDED(factory->EnumAdapters1(0, &adapter))) adapter->EnumOutputs(0, &g_monitorOutput);
        }
    }
    if (g_monitorOutput.get()) g_monitorOutput->WaitForVBlank();
}

// on-demand stationary-animation driver, driven by WM_PAINT (NOT a posted message). needRender (EDT)
// calls InvalidateRect on the frame window; the WM_PAINT handler does the SAME synchronized present as
// WM_NCCALCSIZE, at the size the last resize step used (state->lastClientWidth/Height — GetClientRect lags a step
// behind during a drag, which caused the content to trail the window edge).
//
// Why WM_PAINT and not a posted message: GetMessage priority is  sent > posted > INPUT > WM_PAINT > WM_TIMER.
// A posted message (an earlier posted-message design) outranks input, so a render that re-posts itself permanently
// beats the modal loop's queued mouse moves and the drag locks up (validated: a run of renders at a frozen size
// with no NCCALC between). We cannot instead peek-and-yield, because the modal size loop's input is invisible to
// GetQueueStatus/PeekMessage from in here (also validated). WM_PAINT sits BELOW input in that ladder: GetMessage
// only hands it to us when no input is waiting, so it physically cannot starve the drag — during an active drag
// the mouse moves win and WM_NCCALCSIZE drives the frame (carrying the animation via onRender); only in an input
// lull (a genuine stationary hold) does WM_PAINT fire. We ValidateRect first, then render; onRender's needRender
// InvalidateRect's again, re-arming the next frame — so the animation self-perpetuates while held and stops
// cleanly when needRender stops, all without ever outranking input.

// Resize the content child to AT LEAST (w,h), never shrinking it during a drag. Growing ahead of a GROW covers the
// newly-exposed area; on a SHRINK we must NOT shrink it preemptively — a child narrower than the still-old frame
// exposes a strip on the fixed (origin-moving) edge for one composite (the tiny bar + jump at the very start of a
// left/top-edge shrink). Left large, the child is simply clipped by the shrinking frame, and settles to the exact
// size at drag-end finalize (Swing's doLayout).
static void growContentChildTo(LiveResizeState *s, int w, int h)
{
    if (!s->contentHwnd) return;
    RECT cr; GetClientRect(s->contentHwnd, &cr);
    int childW = (cr.right - cr.left) > w ? (cr.right - cr.left) : w;
    int childH = (cr.bottom - cr.top) > h ? (cr.bottom - cr.top) : h;
    SetWindowPos(s->contentHwnd, nullptr, 0, 0, childW, childH,
                 SWP_NOMOVE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_NOOWNERZORDER);
}

// The live-resize WndProc. Keep the window's own on-screen swapchain as the content and, on each resize step,
// synchronously render+present it at the NEW client size BEFORE letting the geometry commit — "delay the resize
// until content is drawn" — so DWM never exposes an unrendered (white) region at the edges. One instance is
// installed per window; per-window state is recovered from the frame HWND (liveResizeStateFor).
static LRESULT CALLBACK LiveResizeWndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
    LiveResizeState *s = liveResizeStateFor(hWnd);
    if (!s) return DefWindowProcW(hWnd, msg, wParam, lParam); // hooked but state already torn down; nothing to chain to
    switch (msg)
    {
        case WM_ERASEBKGND:
            if (s->inSizeMoveLoop) return 1; // suppress the frame's white erase flash while sizing
            break;
        case WM_ENTERSIZEMOVE:
            s->inSizeMoveLoop = true;
            s->liveResizeEngaged = false; // engage only on the first real size step (not on plain moves)
            break;
        case WM_NCCALCSIZE:
        {
            // Let AWT compute the new client rect first (keeps its inset/layout tracking correct), then render
            // our content at that size and present, all before returning (which lets the geometry commit).
            LRESULT r = CallWindowProcW(s->originalProc, hWnd, msg, wParam, lParam);
            // Skip while a render is already pump-waiting on this thread: the EDT's SetWindowPos is SENT back here
            // and re-enters WM_NCCALCSIZE; starting another EDT round-trip would deadlock (the EDT is blocked in
            // SendMessage). isPumpingEdt() is that guard, owned by the pump-wait itself (edtInvoker).
            if (s->inSizeMoveLoop && wParam && !isPumpingEdt())
            {
                if (!s->liveResizeEngaged)
                {
                    // First real (size-changing) step of this drag: tell Kotlin to engage BEFORE the first render,
                    // so the async EDT renders quiesce for the rest of the drag (see onLiveResizeStarted).
                    s->liveResizeEngaged = true;
                    javaOnLiveResizeStarted(s);
                }
                NCCALCSIZE_PARAMS *p = (NCCALCSIZE_PARAMS *)lParam;
                RECT c = p->rgrc[0]; // now holds the new CLIENT rect
                s->lastClientWidth = c.right - c.left;
                s->lastClientHeight = c.bottom - c.top;
                // Grow (never shrink) the content child so it covers the soon-to-be-exposed region on a grow and
                // never exposes a strip on a shrink; see growContentChildTo.
                growContentChildTo(s, s->lastClientWidth, s->lastClientHeight);
                javaDrawFrameWhileLiveResizing(s); // active drag: unpaced
            }
            return r;
        }
        case WM_PAINT:
            // Stationary hold: while the drag is paused no WM_NCCALCSIZE fires, so drive the animation (and keep
            // content present, no white) from WM_PAINT. It sits BELOW input in GetMessage priority, so it can't
            // starve the active drag (mouse moves win and drive frames via WM_NCCALCSIZE). needRender re-arms it
            // via postLiveResizeRender's InvalidateRect while isHandlingLiveResizeNow. The WM_PAINT stationary-hold driver.
            if (s->inSizeMoveLoop && s->liveResizeEngaged)
            {
                ValidateRect(hWnd, nullptr); // validate FIRST so the re-arm InvalidateRect isn't cleared
                // Re-assert the child size (>= client), same as the NCCALCSIZE path: during a hold Swing's async
                // doLayout has time to shrink the child to a lagging size, leaving a frame-redirection strip past
                // its edge. The active drag never drifts because every NCCALCSIZE re-asserts it; the hold must too.
                growContentChildTo(s, s->lastClientWidth, s->lastClientHeight);
                javaDrawFrameWhileLiveResizing(s);
                // Pace the stationary hold at the refresh rate — one present per vblank (a pure monitor wait on
                // this toolkit thread; no render/Skia state, so it needn't run on the EDT). Beyond capping idle
                // FPS, this is what keeps ORIGIN-MOVE (top/left) resize clean: real drags are full of micro-pauses
                // that hit this WM_PAINT path, and an unpaced hold floods DWM's present queue, adding latency that
                // desyncs content from the moving window origin. A shallow queue keeps top/left welded to the edge
                // (right/bottom don't move the origin, so they tolerate the latency).
                waitForPrimaryVBlank();
                return 0;
            }
            break;
        case WM_EXITSIZEMOVE:
            s->inSizeMoveLoop = false;
            // Only when a real resize engaged. A plain move never sets isHandlingLiveResizeNow (so it never quiesced
            // the async loop) and never renders here — there's nothing to finalize or resume.
            if (s->liveResizeEngaged)
            {
                s->liveResizeEngaged = false;
                // Snap the content child to the exact settled client size. growContentChildTo only ever grew it
                // during the drag, so after a shrink it can be left larger than the client (clipped, so invisible);
                // Swing's post-drag layout may no-op (its model already matches), leaving the native HWND oversized.
                // Reset it explicitly here — safe now the frame is settled (child == client, no strip).
                if (s->contentHwnd)
                {
                    RECT rc; GetClientRect(hWnd, &rc);
                    SetWindowPos(s->contentHwnd, nullptr, 0, 0, rc.right - rc.left, rc.bottom - rc.top,
                                 SWP_NOMOVE | SWP_NOZORDER | SWP_NOACTIVATE | SWP_NOOWNERZORDER);
                }
                // We quiesced onPlatformComponentResized for the whole drag, so the layer's Swing size is stale.
                // onLiveResizeEnded (on the EDT) forces a layout to the settled size and renders BEFORE the async
                // loop resumes — else it renders at the stale size and flashes white — then resumes the loop.
                javaOnLiveResizeEnded(s);
            }
            break;
    }
    return CallWindowProcW(s->originalProc, hWnd, msg, wParam, lParam);
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

    // needRender during a resize routes here — invalidate the frame window so a WM_PAINT drives a
    // synchronized present (stationary animation). WM_PAINT sits below input in GetMessage priority, so
    // (unlike a posted message) it yields to the modal resize loop instead of starving it. InvalidateRect
    // coalesces naturally (the update region just accumulates into one WM_PAINT), so no explicit gate is needed.
    // `handle` is the LiveResizeState* returned by installLiveResizeHook for this window.
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

    // Subclass the frame's WndProc with LiveResizeWndProc and allocate the per-window LiveResizeState, attached to
    // the frame under kLiveResizeStateProp. Returns the state as an opaque handle (0 on failure); Direct3DRedrawer
    // threads it back through postLiveResizeRender/uninstallLiveResizeHook. Any number of windows may be hooked at
    // once (state is per-frame, not global). See LiveResizeState for why this uses SetWindowLongPtr, not
    // SetWindowSubclass (cross-thread install from the EDT).
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_installLiveResizeHook(
        JNIEnv *env, jobject redrawer, jlong windowPtr, jlong contentPtr)
    {
        HWND top = GetAncestor(fromJavaPointer<HWND>(windowPtr), GA_ROOT); // frame that gets WM_ENTERSIZEMOVE / WM_SIZE
        if (!top) return 0;
        // Refuse a double-install on the same frame (would leak the first state and, capturing our own proc as the
        // chain target, recurse forever). Shouldn't happen — one redrawer per frame, and dispose uninstalls first.
        if (liveResizeStateFor(top)) return 0;

        LiveResizeState *state = new LiveResizeState();
        state->frameHwnd = top;
        state->contentHwnd = fromJavaPointer<HWND>(contentPtr);
        state->redrawer = env->NewGlobalRef(redrawer);
        state->originalProc = (WNDPROC)GetWindowLongPtrW(top, GWLP_WNDPROC); // capture AWT's proc (the chain target)
        // Attach the state BEFORE swapping the proc: once LiveResizeWndProc is live, any message the toolkit thread
        // dispatches must be able to find its state, else it would DefWindowProc that message and bypass AWT's proc.
        SetPropW(top, kLiveResizeStateProp, (HANDLE)state);
        SetWindowLongPtrW(top, GWLP_WNDPROC, (LONG_PTR)LiveResizeWndProc); // go live
        return toJavaPointer(state);
    }

    // Restores the frame's original WndProc, detaches and frees the per-window state, so a resize after dispose
    // can't call into freed state. `handle` is what installLiveResizeHook returned; a 0 handle is a no-op. Called
    // from Direct3DRedrawer.dispose().
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_uninstallLiveResizeHook(
        JNIEnv *env, jobject redrawer, jlong handle)
    {
        LiveResizeState *state = fromJavaPointer<LiveResizeState *>(handle);
        if (!state) return;
        if (state->frameHwnd) {
            // Only unhook if we're still the top proc; if something subclassed over us, leave the chain intact
            // (restoring blindly would drop the other subclass). Detach the prop regardless so no stale/freed
            // state is ever recovered — a later LiveResizeWndProc call with no state falls through to DefWindowProc.
            WNDPROC current = (WNDPROC)GetWindowLongPtrW(state->frameHwnd, GWLP_WNDPROC);
            if (current == LiveResizeWndProc && state->originalProc) {
                SetWindowLongPtrW(state->frameHwnd, GWLP_WNDPROC, (LONG_PTR)state->originalProc);
            }
            RemovePropW(state->frameHwnd, kLiveResizeStateProp);
        }
        if (state->redrawer) env->DeleteGlobalRef(state->redrawer);
        delete state;
    }

}

#endif
