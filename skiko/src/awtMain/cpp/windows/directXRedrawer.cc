#ifdef SK_DIRECT3D
#include <locale>
#include <Windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"
#include "exceptions_handler.h"
#include "window_util.h"

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

    void initSwapChain(UINT width, UINT height, jboolean transparency) {
        gr_cp<IDXGIFactory4> swapChainFactory4;
        gr_cp<IDXGISwapChain1> swapChain1;
        CreateDXGIFactory2(0, IID_PPV_ARGS(&swapChainFactory4));
        HRESULT result = S_OK;
        if (transparency) {
            result = CreateSwapChainForComposition(swapChainFactory4.get(), width, height, &swapChain1);
        }
        if (!transparency || FAILED(result)) {
            /*
             * It's just a fallback path that added for compatibility.
             * In this case transparency won't be supported.
             */
            swapChain1.reset(nullptr);
            CreateSwapChainForHwnd(swapChainFactory4.get(), width, height, &swapChain1);
        }
        swapChainFactory4->MakeWindowAssociation(hWnd, DXGI_MWA_NO_ALT_ENTER);
        swapChain1->QueryInterface(IID_PPV_ARGS(&swapChain));
        swapChainFactory4.reset(nullptr);
    }

private:
    HRESULT CreateSwapChainForComposition(IDXGIFactory4 *swapChainFactory4, UINT width, UINT height, IDXGISwapChain1 **swapChain1) {
        DXGI_SWAP_CHAIN_DESC1 swapChainDesc = {};
        swapChainDesc.Width = width;
        swapChainDesc.Height = height;
        swapChainDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
        swapChainDesc.SampleDesc.Count = 1;
        swapChainDesc.SampleDesc.Quality = 0;
        swapChainDesc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
        swapChainDesc.BufferCount = BuffersCount;
        swapChainDesc.Scaling = DXGI_SCALING_STRETCH;
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

    HRESULT CreateSwapChainForHwnd(IDXGIFactory4 *swapChainFactory4, UINT width, UINT height, IDXGISwapChain1 **swapChain1) {
        DXGI_SWAP_CHAIN_DESC1 swapChainDesc = {};
        swapChainDesc.Width = width;
        swapChainDesc.Height = height;
        swapChainDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
        swapChainDesc.SampleDesc.Count = 1;
        swapChainDesc.SampleDesc.Quality = 0;
        swapChainDesc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
        swapChainDesc.BufferCount = BuffersCount;
        swapChainDesc.Scaling = DXGI_SCALING_STRETCH;
        swapChainDesc.SwapEffect = DXGI_SWAP_EFFECT_FLIP_DISCARD;
        return swapChainFactory4->CreateSwapChainForHwnd(queue.get(), hWnd, &swapChainDesc, nullptr, nullptr, swapChain1);
    }
};

// ===================== Direct3D synchronous live-resize =====================
// The Windows/Direct3D live-resize fix. Subclasses the frame's WndProc and, during an interactive (drag)
// resize, renders the real renderDelegate content into a DirectComposition overlay swapchain on the frame,
// presenting it synchronously in WM_NCCALCSIZE / WM_PAINT — so DWM composites the frame-move and our content
// atomically and no white/transparent bars appear at the edges. Assumes a single hooked window.

static WNDPROC g_originalWndProc = nullptr;
static HWND g_frameHwnd = nullptr;
static HWND g_contentHwnd = nullptr;
static DirectXDevice *g_device = nullptr;
static jobject g_redrawer = nullptr;

// Attach the current (toolkit) thread to the JVM if needed and return its JNIEnv (nullptr if unavailable).
static JNIEnv *getJniEnv()
{
    if (!jvm) return nullptr;
    JNIEnv *env = nullptr;
    if (jvm->GetEnv((void **)&env, JNI_VERSION_1_6) == JNI_EDETACHED)
        jvm->AttachCurrentThread((void **)&env, nullptr);
    return env;
}

// ---- One dedicated invoker per Direct3DRedrawer method called from native (g_redrawer + env must be valid).
// Each caches its jmethodID in a local static on first use (the class is stable for the app's lifetime, and all
// callers run on the single toolkit thread, so the lazy init needs no synchronization). ----
static void drawFrameWhileLiveResizing(JNIEnv *env, int w, int h)
{
    static jmethodID mid = nullptr;
    if (!mid)
    {
        jclass cls = env->GetObjectClass(g_redrawer);
        mid = env->GetMethodID(cls, "drawFrameWhileLiveResizing", "(II)V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(g_redrawer, mid, (jint)w, (jint)h);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

static void onLiveResizeFinalize(JNIEnv *env)
{
    static jmethodID mid = nullptr;
    if (!mid)
    {
        jclass cls = env->GetObjectClass(g_redrawer);
        mid = env->GetMethodID(cls, "onLiveResizeFinalize", "()V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(g_redrawer, mid);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

static void onLiveResizeEnded(JNIEnv *env)
{
    static jmethodID mid = nullptr;
    if (!mid)
    {
        jclass cls = env->GetObjectClass(g_redrawer);
        mid = env->GetMethodID(cls, "onLiveResizeEnded", "()V");
        env->DeleteLocalRef(cls);
    }
    if (mid) env->CallVoidMethod(g_redrawer, mid);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
}

// ask the redrawer (on this toolkit thread) to render the REAL renderDelegate content at (w,h) into the frame's
// comp swapchain and present it. drawFrameWhileLiveResizing (Kotlin) hops to the EDT and blocks there via
// EdtInvoker.invokeAndWaitWhilePumping; the render calls back into makeLiveResizeSurface / presentLiveResizeFrame.
//
// g_rendering guards against re-entrant renders while an EDT round-trip is pump-waiting on THIS thread: the
// EDT's own window ops (SetWindowPos) are SENT back here and serviced during the wait, and one can re-enter
// WM_NCCALCSIZE — a nested pump-wait there would deadlock (the EDT is blocked in SendMessage). So the two
// callers below set it around their blocking round-trip and renderFrameOnEdt skips while it's set. (This is a
// live-resize coalescing concern, so it lives here in the callers, not in the general invoke-and-wait utility.)
static bool g_rendering = false;

static void renderFrameOnEdt(int w, int h)
{
    if (!g_redrawer || g_rendering) return;
    JNIEnv *env = getJniEnv();
    if (!env) return;
    g_rendering = true;
    drawFrameWhileLiveResizing(env, w, h); // blocks until the EDT render completes (EdtInvoker.invokeAndWaitWhilePumping)
    g_rendering = false;
}

// at drag-end, run onLiveResizeFinalize on the EDT (blocking, via EdtInvoker.invokeAndWaitWhilePumping) — validate the frame
// so the canvas HWND catches up to the final client size AND renderImmediately so the canvas has correct-size
// content — BEFORE we detach the overlay. Otherwise revealing the still-lagging canvas snaps a frame later.
static void finalizeLiveResizeOnEdt()
{
    if (!g_redrawer) return;
    JNIEnv *env = getJniEnv();
    if (!env) return;
    g_rendering = true;
    onLiveResizeFinalize(env);
    g_rendering = false;
}

// ---- The overlay: a DComp target + composition swapchain on the AWT frame HWND ----------------------
// The frame keeps its normal DWM redirection bitmap; our visual is created topmost (CreateTargetForHwnd below)
// so it composites ABOVE that bitmap and owns the visible content during a resize. The swapchain is presented
// (and DComp-committed) synchronously in the frame's WM_NCCALCSIZE (no DwmFlush), so DWM composites the
// frame-move and our content in one pass — clean on all edges incl. top/left origin-move, with no extra
// child HWND.
static gr_cp<IDCompositionDevice> g_frameDComp;
static gr_cp<IDCompositionTarget> g_frameTarget;
static gr_cp<IDCompositionVisual> g_frameVisual;
static gr_cp<IDXGISwapChain3> g_frameSwapChain;
static gr_cp<ID3D12Resource> g_frameBuffers[BuffersCount]; // wrapped-as-Skia backbuffers
// per-buffer fence discipline, mirroring the on-screen swapchain (DirectXDevice). This is what lets the
// overlay tolerate many presents per size and still ResizeBuffers cleanly — the earlier frame-latency-waitable
// approach wedged after ~2 presents at one size. presentLiveResizeFrame signals fenceValues[bufferIndex] after Present;
// the next frame waits it before reusing that buffer; ResizeBuffers waits ALL buffers' fences.
static gr_cp<ID3D12Fence> g_frameFence;
static HANDLE g_frameFenceEvent = nullptr;
static uint64_t g_frameFenceValues[BuffersCount] = {0};
static unsigned int g_frameBufferIndex = 0;
static gr_cp<IDXGIOutput> g_frameOutput; // for WaitForVBlank (vsync pacing of the resize render loop)

// Create the overlay swapchain's fence (mirrors initFence for the on-screen swapchain). Once per swapchain.
static void frameInitFence()
{
    if (!g_device || g_frameFence.get()) return;
    for (int i = 0; i < BuffersCount; i++) g_frameFenceValues[i] = 10000;
    g_device->device->CreateFence(g_frameFenceValues[0], D3D12_FENCE_FLAG_NONE, IID_PPV_ARGS(&g_frameFence));
    g_frameFenceEvent = CreateEventW(nullptr, FALSE, FALSE, nullptr);
    g_frameBufferIndex = 0;
}

// Block until the GPU fence reaches fv (a buffer's prior present finished).
static void frameWaitFence(uint64_t fv)
{
    if (g_frameFence.get() && g_frameFence->GetCompletedValue() < fv)
    {
        g_frameFence->SetEventOnCompletion(fv, g_frameFenceEvent);
        WaitForSingleObjectEx(g_frameFenceEvent, INFINITE, FALSE);
    }
}

static void ensureFrameSwapChain()
{
    if (g_frameSwapChain.get() || !g_device || !g_frameHwnd) return;
    RECT rc; GetClientRect(g_frameHwnd, &rc);
    UINT w = (rc.right - rc.left) > 0 ? (UINT)(rc.right - rc.left) : 1;
    UINT h = (rc.bottom - rc.top) > 0 ? (UINT)(rc.bottom - rc.top) : 1;

    gr_cp<IDXGIFactory4> factory;
    if (FAILED(CreateDXGIFactory2(0, IID_PPV_ARGS(&factory)))) return;
    DXGI_SWAP_CHAIN_DESC1 desc = {};
    desc.Width = w; desc.Height = h;
    desc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
    desc.SampleDesc.Count = 1;
    desc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
    desc.BufferCount = BuffersCount;
    desc.Scaling = DXGI_SCALING_STRETCH;
    desc.SwapEffect = DXGI_SWAP_EFFECT_FLIP_DISCARD;
    desc.AlphaMode = DXGI_ALPHA_MODE_IGNORE;
    gr_cp<IDXGISwapChain1> sc1;
    HRESULT hr = factory->CreateSwapChainForComposition(g_device->queue.get(), &desc, nullptr, &sc1);
    if (FAILED(hr)) return;
    sc1->QueryInterface(IID_PPV_ARGS(&g_frameSwapChain));
    frameInitFence(); // per-buffer fence discipline (mirrors the on-screen swapchain), no waitable

    hr = DCompLibrary::DCompositionCreateDevice(nullptr, IID_PPV_ARGS(&g_frameDComp));
    if (FAILED(hr)) return;
    // topmost=TRUE so our visual composites ABOVE the frame's redirection bitmap (and the canvas child).
    hr = g_frameDComp->CreateTargetForHwnd(g_frameHwnd, TRUE, &g_frameTarget);
    if (FAILED(hr)) return;
    g_frameDComp->CreateVisual(&g_frameVisual);
    g_frameVisual->SetContent(g_frameSwapChain.get());
}

// Show/hide our overlay by attaching/detaching the visual root (committed).
static void showFrameOverlay(bool show)
{
    if (!g_frameTarget.get() || !g_frameDComp.get()) return;
    g_frameTarget->SetRoot(show ? g_frameVisual.get() : nullptr);
    g_frameDComp->Commit();
}

// Our topmost DComp overlay visual on the frame composites above the frame's redirection bitmap, so it owns
// the visible content during a resize. During a drag we hide the canvas child and render the real
// renderDelegate content into the overlay swapchain, presenting synchronously in WM_NCCALCSIZE (the proven
// sync point, no DwmFlush) so DWM composites the frame-move and our content in one pass — atomic on all edges
// incl. top/left origin-move.
static bool g_inSizeMoveLoop = false; // inside a WM_ENTERSIZEMOVE..WM_EXITSIZEMOVE modal loop (resize OR move)
static bool g_liveResizeEngaged = false;   // frame overlay active (only during actual RESIZE, not plain moves)

// on-demand stationary-animation driver, driven by WM_PAINT (NOT a posted message). needRender (EDT)
// calls InvalidateRect on the frame window; the WM_PAINT handler does the SAME synchronized present as
// WM_NCCALCSIZE, at the size the last resize step used (g_lastClientWidth/H — GetClientRect lags a step behind
// during a drag, which caused the content to trail the window edge).
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
static int g_lastClientWidth = 0, g_lastClientHeight = 0; // client size the last resize step used (the true current size)
// whether presentLiveResizeFrame should WaitForVBlank for THIS frame. FALSE for the WM_NCCALCSIZE (active-drag)
// present — waiting there blocks inside NCCALCSIZE and displays the new-size content a full refresh BEFORE the
// frame geometry is applied (content leads the edge). TRUE only for the WM_PAINT (stationary-hold) present, to
// cap the idle animation loop at the refresh rate. Set on the toolkit thread right before the synchronous render.
static bool g_paceVBlank = false;

static LRESULT CALLBACK LiveResizeWndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
    switch (msg)
    {
        case WM_ERASEBKGND:
            if (g_inSizeMoveLoop) return 1; // overlay owns the content while sizing; skip the erase flash
            break;
        case WM_ENTERSIZEMOVE:
        {
            g_inSizeMoveLoop = true;
            g_liveResizeEngaged = false;
            // WM_ENTERSIZEMOVE fires for plain MOVES too. Do NOT hide the canvas, present, or suppress the EDT
            // here: during a move the canvas stays visible and must keep animating through the normal EDT loop,
            // and presenting into the frame swapchain without a resize wedges its flip buffers (the "two moves
            // then resize" bug). We engage the overlay AND stop the EDT only on the first real size change
            // (WM_NCCALCSIZE); moves never touch the frame swapchain and translate atomically with the frame.
            break;
        }
        case WM_NCCALCSIZE:
        {
            // THE sync point: compute the new client rect, then present our content at that size and
            // DComp-Commit BEFORE returning — so DWM composites the frame-move and our content in one pass.
            // Always let AWT's proc handle NCCALCSIZE (even during the drag) so AWT's own client-rect / inset
            // tracking stays correct — otherwise it lays the canvas out from a stale client rect and leaves a
            // 1-2px transparent sliver at rest. (DefWindowProc-vs-AWT-proc made no difference to the jump.)
            LRESULT r = CallWindowProcW(g_originalWndProc, hWnd, msg, wParam, lParam);
            if (g_inSizeMoveLoop && wParam)
            {
                bool justEngaged = false;
                if (!g_liveResizeEngaged)
                {
                    // First real resize step: create the overlay now but do NOT reveal it yet — we reveal it
                    // below, only after it has content, so DWM never composites an empty (transparent) overlay
                    // frame. Deferred to here (not WM_ENTERSIZEMOVE) so plain moves keep animating. The redrawer
                    // learns the resize started from the first drawFrameWhileLiveResizing below (it sets layerSizeInLiveResize,
                    // which stops the EDT loop) — no separate engage callback needed.
                    g_liveResizeEngaged = true;
                    justEngaged = true;
                    ensureFrameSwapChain();
                }
                NCCALCSIZE_PARAMS *p = (NCCALCSIZE_PARAMS *)lParam;
                RECT c = p->rgrc[0]; // now holds the new CLIENT rect
                g_lastClientWidth = c.right - c.left;
                g_lastClientHeight = c.bottom - c.top;
                g_paceVBlank = false; // atomic with the geometry change — no vblank wait (would lead the edge)
                renderFrameOnEdt(g_lastClientWidth, g_lastClientHeight); // 2b: real Skia content
                if (justEngaged)
                {
                    // The overlay now holds a presented frame: reveal it (topmost, opaque) and hide the canvas
                    // underneath it. Revealing an already-drawn overlay avoids the one transparent engage frame.
                    showFrameOverlay(true);
                    if (g_contentHwnd) ShowWindow(g_contentHwnd, SW_HIDE);
                }
            }
            return r;
        }
        case WM_PAINT:
            // On-demand stationary animation (armed by needRender's InvalidateRect). WM_PAINT is BELOW input in
            // GetMessage priority, so it only reaches us in an input lull — an active drag's mouse moves are
            // serviced first (WM_NCCALCSIZE drives those frames), so this cannot starve the drag. Validate the
            // region FIRST (before rendering) so the InvalidateRect that onRender→needRender issues during our
            // synchronous render re-arms the NEXT frame instead of being cleared. Only handle it while engaged;
            // otherwise let AWT's proc paint normally.
            if (g_liveResizeEngaged)
            {
                ValidateRect(hWnd, nullptr);
                g_paceVBlank = true; // idle-hold animation: cap at the refresh rate (no geometry change here)
                renderFrameOnEdt(g_lastClientWidth, g_lastClientHeight);
                return 0;
            }
            break;
        case WM_EXITSIZEMOVE:
            if (g_liveResizeEngaged)
            {
                // Render one last overlay frame at the SETTLED client size (it keeps covering the canvas).
                RECT rc; GetClientRect(hWnd, &rc);
                g_paceVBlank = false;
                renderFrameOnEdt(rc.right - rc.left, rc.bottom - rc.top);
                // Show the canvas UNDER the still-topmost overlay, then finalize it (validate + render at the
                // final size) while it's hidden behind the overlay, then detach the overlay to reveal a
                // correct-size canvas — no 1-frame snap.
                if (g_contentHwnd) ShowWindow(g_contentHwnd, SW_SHOW);
                finalizeLiveResizeOnEdt();
                showFrameOverlay(false);            // detach overlay; the canvas becomes the frame content again
                g_liveResizeEngaged = false;
            }
            g_inSizeMoveLoop = false;
            // Notify the redrawer (toolkit thread) that the live resize ended so it resumes the normal loop.
            if (g_redrawer)
                if (JNIEnv *env = getJniEnv()) onLiveResizeEnded(env);
            break;
    }
    return CallWindowProcW(g_originalWndProc, hWnd, msg, wParam, lParam);
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
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height, jboolean transparency)
    {
        __try
        {
            DirectXDevice *d3dDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
            d3dDevice->initSwapChain((UINT) width, (UINT) height, transparency);
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

    // wrap the frame overlay's comp-swapchain backbuffer as a Skia SkSurface so the renderDelegate
    // can draw REAL content into it during resize (mirrors makeDirectXSurface but targets g_frameSwapChain).
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_makeLiveResizeSurface(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr, jint width, jint height, jint index)
    {
        GrDirectContext *context = fromJavaPointer<GrDirectContext *>(contextPtr);
        if (!g_frameSwapChain.get() || width <= 0 || height <= 0 || index < 0 || index >= BuffersCount) return 0;
        if (FAILED(g_frameSwapChain->GetBuffer((UINT)index, IID_PPV_ARGS(&g_frameBuffers[index])))) return 0;

        GrD3DTextureResourceInfo info(nullptr, nullptr, D3D12_RESOURCE_STATE_PRESENT, DXGI_FORMAT_R8G8B8A8_UNORM, 1, 1, 0);
        info.fResource = g_frameBuffers[index];
        GrBackendTexture backendTexture = GrBackendTextures::MakeD3D((int)width, (int)height, info);
        auto result = SkSurfaces::WrapBackendTexture(
                                 context, backendTexture, kTopLeft_GrSurfaceOrigin, 0,
                                 kRGBA_8888_SkColorType, SkColorSpace::MakeSRGB(), nullptr)
                                 .release();
        return toJavaPointer(result);
    }

    // resize the overlay swapchain on size change (mirrors the on-screen resizeBuffers): flush Skia's
    // refs, wait EACH buffer's last-present fence so no backbuffer is still in flight, drop refs, ResizeBuffers.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_resizeLiveResizeBuffers(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr, jint width, jint height)
    {
        GrDirectContext *context = fromJavaPointer<GrDirectContext *>(contextPtr);
        if (!g_frameSwapChain.get() || width <= 0 || height <= 0) return;
        DXGI_SWAP_CHAIN_DESC1 d = {};
        g_frameSwapChain->GetDesc1(&d);
        if (d.Width == (UINT)width && d.Height == (UINT)height) return; // no size change
        context->flush();
        context->submit(GrSyncCpu::kYes);
        for (int i = 0; i < BuffersCount; i++)
        {
            frameWaitFence(g_frameFenceValues[i]);
            g_frameBuffers[i].reset(nullptr);
        }
        g_frameSwapChain->ResizeBuffers(BuffersCount, (UINT)width, (UINT)height, DXGI_FORMAT_R8G8B8A8_UNORM, 0);
    }

    // getBufferIndex discipline (mirrors the on-screen swapchain) — rotate to the current back buffer,
    // wait its previous present's fence, bump its next fence value. Returns the buffer index to draw into.
    JNIEXPORT jint JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_liveResizeBufferIndex(
        JNIEnv *env, jobject redrawer)
    {
        if (!g_frameSwapChain.get()) return 0;
        const uint64_t fv = g_frameFenceValues[g_frameBufferIndex];
        g_frameBufferIndex = g_frameSwapChain->GetCurrentBackBufferIndex();
        frameWaitFence(fv);
        g_frameFenceValues[g_frameBufferIndex] = fv + 1;
        return (jint)g_frameBufferIndex;
    }

    // present the frame comp swapchain (vblank-synced double-present) + DComp Commit.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_presentLiveResizeFrame(
        JNIEnv *env, jobject redrawer, jboolean vsync)
    {
        if (!g_frameSwapChain.get() || !g_device) return;
        // Present, then signal this buffer's fence (mirrors the on-screen swap()). Present(0, RESTART) is the
        // IMMEDIATE flip (content welded to the window edge in the same DWM compose as the geometry change);
        // Present(1, DO_NOT_SEQUENCE) re-presents the SAME buffer vblank-synced (top/left-jump stabilizer) WITHOUT
        // advancing the swapchain, so the per-buffer fence rotation still alternates buffers correctly.
        g_frameSwapChain->Present(0, DXGI_PRESENT_RESTART);
        g_frameSwapChain->Present(1, DXGI_PRESENT_DO_NOT_SEQUENCE);
        g_device->queue->Signal(g_frameFence.get(), g_frameFenceValues[g_frameBufferIndex]);
        if (g_frameDComp) g_frameDComp->Commit();
        // Block on the display's vblank — but ONLY for the WM_PAINT stationary-hold present (g_paceVBlank),
        // to cap that idle loop at the refresh rate. NEVER for the WM_NCCALCSIZE active-drag present: waiting there
        // blocks inside NCCALCSIZE and shows the new-size content a full refresh before the frame geometry lands,
        // making the content lead the window edge.
        if (vsync && g_paceVBlank)
        {
            if (!g_frameOutput.get())
            {
                gr_cp<IDXGIFactory1> factory;
                if (SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&factory))))
                {
                    gr_cp<IDXGIAdapter1> adapter;
                    if (SUCCEEDED(factory->EnumAdapters1(0, &adapter))) adapter->EnumOutputs(0, &g_frameOutput);
                }
            }
            if (g_frameOutput.get()) g_frameOutput->WaitForVBlank();
        }
    }

    // needRender during a resize routes here — invalidate the frame window so a WM_PAINT drives a
    // synchronized overlay present (stationary animation). WM_PAINT sits below input in GetMessage priority, so
    // (unlike a posted message) it yields to the modal resize loop instead of starving it. InvalidateRect
    // coalesces naturally (the update region just accumulates into one WM_PAINT), so no explicit gate is needed.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_postLiveResizeRender(
        JNIEnv *env, jobject redrawer)
    {
        if (g_frameHwnd) InvalidateRect(g_frameHwnd, nullptr, FALSE);
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

    // Installs the live-resize WndProc hook. See the live-resize block above.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_installLiveResizeHook(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong windowPtr, jlong contentPtr)
    {
        g_device = fromJavaPointer<DirectXDevice *>(devicePtr);
        g_contentHwnd = fromJavaPointer<HWND>(contentPtr);
        if (g_redrawer) env->DeleteGlobalRef(g_redrawer);
        g_redrawer = env->NewGlobalRef(redrawer);

        HWND passed = fromJavaPointer<HWND>(windowPtr);
        HWND top = GetAncestor(passed, GA_ROOT); // the frame that receives WM_ENTERSIZEMOVE / WM_SIZE
        g_frameHwnd = top;
        g_originalWndProc = (WNDPROC)SetWindowLongPtrW(top, GWLP_WNDPROC, (LONG_PTR)LiveResizeWndProc);
    }
}

#endif
