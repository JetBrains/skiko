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
#include <cstdio> // SPIKE
#include <dwmapi.h> // SPIKE (DwmFlush)
#include <d3d11.h> // SPIKE (D3D11on12 bitblt-to-redirection test)
#include <d3d11_1.h> // SPIKE (ID3D11DeviceContext1::ClearView for edge markers)
#include <d2d1.h> // SPIKE (Direct2D HwndRenderTarget writes the redirection bitmap via the DC)

// SPIKE: JavaVM global set in JNI_OnLoad (jvmMain/cpp/common/impl/Library.cc); used to reach the JVM from
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

    // SPIKE: dedicated (isolated from Skia's) resources for the throwaway solid-color present.
    gr_cp<ID3D12DescriptorHeap> spikeRtvHeap;
    gr_cp<ID3D12CommandAllocator> spikeCmdAlloc;
    gr_cp<ID3D12GraphicsCommandList> spikeCmdList;
    gr_cp<ID3D12Fence> spikeFence;
    HANDLE spikeFenceEvent = NULL;
    uint64_t spikeFenceValue = 0;

    ~DirectXDevice()
    {
        if (fenceEvent != NULL)
        {
            CloseHandle(fenceEvent);
        }
        if (spikeFenceEvent != NULL) // SPIKE
        {
            CloseHandle(spikeFenceEvent);
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

    // SPIKE: clear the current back buffer to a solid color and present it synchronously, on the caller's
    // thread (the toolkit thread, from inside WM_SIZE). Deliberately does NOT call ResizeBuffers — a uniform
    // color is scale-invariant, so DXGI's stretch of the old-size buffer to the (grown) client area still
    // fills edge-to-edge; this sidesteps the buffer-lifetime entanglement with Skia's wrapped surfaces.
    void spikeClearAndPresent(IDXGISwapChain3 *target = nullptr, bool edgeMarkers = false, bool dwmFlush = true, bool vblankSync = false)
    {
        IDXGISwapChain3 *sc = target ? target : swapChain.get();
        if (!sc) return;

        if (!spikeCmdAlloc.get())
        {
            device->CreateCommandAllocator(D3D12_COMMAND_LIST_TYPE_DIRECT, IID_PPV_ARGS(&spikeCmdAlloc));
            device->CreateCommandList(0, D3D12_COMMAND_LIST_TYPE_DIRECT, spikeCmdAlloc.get(), nullptr, IID_PPV_ARGS(&spikeCmdList));
            spikeCmdList->Close();
            D3D12_DESCRIPTOR_HEAP_DESC rtvHeapDesc = {};
            rtvHeapDesc.NumDescriptors = 1;
            rtvHeapDesc.Type = D3D12_DESCRIPTOR_HEAP_TYPE_RTV;
            rtvHeapDesc.Flags = D3D12_DESCRIPTOR_HEAP_FLAG_NONE;
            device->CreateDescriptorHeap(&rtvHeapDesc, IID_PPV_ARGS(&spikeRtvHeap));
            device->CreateFence(0, D3D12_FENCE_FLAG_NONE, IID_PPV_ARGS(&spikeFence));
            spikeFenceEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
        }

        UINT index = sc->GetCurrentBackBufferIndex();
        gr_cp<ID3D12Resource> backBuffer;
        if (FAILED(sc->GetBuffer(index, IID_PPV_ARGS(&backBuffer)))) return;

        D3D12_CPU_DESCRIPTOR_HANDLE rtvHandle = spikeRtvHeap->GetCPUDescriptorHandleForHeapStart();
        device->CreateRenderTargetView(backBuffer.get(), nullptr, rtvHandle);

        spikeCmdAlloc->Reset();
        spikeCmdList->Reset(spikeCmdAlloc.get(), nullptr);

        D3D12_RESOURCE_BARRIER barrier = {};
        barrier.Type = D3D12_RESOURCE_BARRIER_TYPE_TRANSITION;
        barrier.Transition.pResource = backBuffer.get();
        barrier.Transition.Subresource = D3D12_RESOURCE_BARRIER_ALL_SUBRESOURCES;
        barrier.Transition.StateBefore = D3D12_RESOURCE_STATE_PRESENT;
        barrier.Transition.StateAfter = D3D12_RESOURCE_STATE_RENDER_TARGET;
        spikeCmdList->ResourceBarrier(1, &barrier);

        // Steady, saturated blue so any white/background intrusion at a growing edge is unmistakable.
        const float clearColor[] = { 0.15f, 0.45f, 0.95f, 1.0f };
        spikeCmdList->ClearRenderTargetView(rtvHandle, clearColor, 0, nullptr);

        // EDGE-TRACKING TEST: draw a hard red frame + a green corner square, all anchored to the CURRENT buffer
        // edges (buffer == exact client size via ResizeBuffers). If, during resize, the red doesn't stay welded
        // to the window edge (a desktop gap appears, it overshoots, or the fixed-thickness bars visibly thin/
        // thicken = stretch), then edge-anchored content does NOT track the window edge and solid blue was lying.
        if (edgeMarkers)
        {
            D3D12_RESOURCE_DESC rd = backBuffer->GetDesc();
            const LONG w = (LONG)rd.Width, h = (LONG)rd.Height;
            const LONG t = 16; // fixed bar thickness in px — appears constant iff there is no stretch
            const float red[] = { 0.95f, 0.10f, 0.10f, 1.0f };
            D3D12_RECT frame[4] = {
                { 0, 0, w, t },         // top
                { 0, h - t, w, h },     // bottom
                { 0, 0, t, h },         // left
                { w - t, 0, w, h },     // right
            };
            spikeCmdList->ClearRenderTargetView(rtvHandle, red, 4, frame);
            // Green square welded to the bottom-right corner — the most sensitive spot for shrink lag.
            const float green[] = { 0.10f, 0.85f, 0.20f, 1.0f };
            D3D12_RECT corner = { w - 60, h - 60, w, h };
            spikeCmdList->ClearRenderTargetView(rtvHandle, green, 1, &corner);
        }

        barrier.Transition.StateBefore = D3D12_RESOURCE_STATE_RENDER_TARGET;
        barrier.Transition.StateAfter = D3D12_RESOURCE_STATE_PRESENT;
        spikeCmdList->ResourceBarrier(1, &barrier);

        spikeCmdList->Close();
        ID3D12CommandList *lists[] = { spikeCmdList.get() };
        queue->ExecuteCommandLists(1, lists);

        if (vblankSync)
        {
            // Vblank-synced double-present (gamedev.net/708865 robustness lever): discard any queued frame,
            // then a sync-interval-1 present that blocks until the next vblank so our content lands on the
            // compositor's clock — closes a one-composite slip when the caller's proc (AWT) adds per-step work.
            sc->Present(0, DXGI_PRESENT_RESTART);
            sc->Present(1, DXGI_PRESENT_DO_NOT_SEQUENCE);
        }
        else
        {
            sc->Present(0, 0);
        }
        if (!target && dcDevice.get()) dcDevice->Commit(); // composition (transparency) path only

        // Block until the GPU is done so the back buffer is idle before the next WM_SIZE reuses it.
        const uint64_t fv = ++spikeFenceValue;
        queue->Signal(spikeFence.get(), fv);
        if (spikeFence->GetCompletedValue() < fv)
        {
            spikeFence->SetEventOnCompletion(fv, spikeFenceEvent);
            WaitForSingleObjectEx(spikeFenceEvent, INFINITE, FALSE);
        }

        // DwmFlush forces a composite NOW. That is WRONG inside WM_NCCALCSIZE (present-before-return): it shows
        // our content at the OLD window geometry for a frame, then the move happens => one-frame opposite-edge
        // jump on left/top drags. So the resize path passes dwmFlush=false and lets DWM composite the frame-move
        // and our content together after WM_NCCALCSIZE returns.
        if (dwmFlush) DwmFlush();
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

// ========================= SPIKE (throwaway) =========================
// Subclasses the top-level window's WndProc to drive a synchronous solid-color present during an
// interactive (drag) resize, so we can observe — before building the real fix — whether presenting inside
// WM_SIZE composites cleanly at the window's new client size. Assumes a single hooked window (the test).

static WNDPROC g_spikeOrigProc = nullptr;
static HWND g_spikeTopHwnd = nullptr;
static HWND g_spikeContentHwnd = nullptr;
static DirectXDevice *g_spikeDevice = nullptr;
static jobject g_spikeRedrawer = nullptr;

// TOP-LEVEL PROTOTYPE: a flip swapchain bound to the *frame* HWND (not the child canvas). Presenting to the
// same HWND the OS resizes removes the child/parent composite skew by construction.
static gr_cp<IDXGISwapChain3> g_spikeTopSwapChain;

static void spikeEnsureTopSwapChain()
{
    if (g_spikeTopSwapChain.get() || !g_spikeDevice || !g_spikeTopHwnd) return;
    gr_cp<IDXGIFactory4> factory;
    if (FAILED(CreateDXGIFactory2(0, IID_PPV_ARGS(&factory)))) return;
    RECT rc; GetClientRect(g_spikeTopHwnd, &rc);
    DXGI_SWAP_CHAIN_DESC1 desc = {};
    desc.Width = (rc.right - rc.left) > 0 ? (UINT)(rc.right - rc.left) : 1;
    desc.Height = (rc.bottom - rc.top) > 0 ? (UINT)(rc.bottom - rc.top) : 1;
    desc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
    desc.SampleDesc.Count = 1;
    desc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
    desc.BufferCount = BuffersCount;
    desc.Scaling = DXGI_SCALING_STRETCH;
    desc.SwapEffect = DXGI_SWAP_EFFECT_FLIP_DISCARD;
    gr_cp<IDXGISwapChain1> sc1;
    HRESULT hr = factory->CreateSwapChainForHwnd(
        g_spikeDevice->queue.get(), g_spikeTopHwnd, &desc, nullptr, nullptr, &sc1);
    if (FAILED(hr)) { fprintf(stderr, "[spike] top swapchain FAILED hr=0x%08lx\n", hr); return; }
    factory->MakeWindowAssociation(g_spikeTopHwnd, DXGI_MWA_NO_ALT_ENTER);
    sc1->QueryInterface(IID_PPV_ARGS(&g_spikeTopSwapChain));
    fprintf(stderr, "[spike] top swapchain created %ux%u on frame hwnd=%p\n",
            desc.Width, desc.Height, (void *)g_spikeTopHwnd);
    fflush(stderr);
}

// ---- NOREDIRECTIONBITMAP composition child spike ----------------------------------------------------
// A native child HWND we create ourselves with WS_EX_NOREDIRECTIONBITMAP + DirectComposition. Because it has
// NO GDI redirection bitmap, DWM composites ONLY our DComp visual (the flip swapchain) for it — there is no
// redirection surface to race/bleed. We size it synchronously with the parent inside WM_SIZE and present +
// Commit synchronously. This is the only construction that can be atomic on Windows (the AWT frame/canvas
// both carry redirection bitmaps and can't get this flag — it's creation-only).
static HWND g_spikeNoRedirHwnd = nullptr;
static gr_cp<IDCompositionDevice> g_spikeDComp;
static gr_cp<IDCompositionTarget> g_spikeDCompTarget;
static gr_cp<IDCompositionVisual> g_spikeDCompVisual;
static gr_cp<IDXGISwapChain3> g_spikeCompSwapChain;
static UINT g_spikeCompW = 0, g_spikeCompH = 0;
// Non-client border insets of the frame (constant for its style), so we can turn the frame's incoming WINDOW
// rect (from WM_WINDOWPOSCHANGING) into the incoming CLIENT screen rect the popup must occupy.
static int g_spikeInsetL = 0, g_spikeInsetT = 0, g_spikeInsetR = 0, g_spikeInsetB = 0;

static void spikeComputeInsets()
{
    RECT wr; GetWindowRect(g_spikeTopHwnd, &wr);
    RECT cr; GetClientRect(g_spikeTopHwnd, &cr);
    POINT tl = { cr.left, cr.top }; ClientToScreen(g_spikeTopHwnd, &tl);
    g_spikeInsetL = tl.x - wr.left;
    g_spikeInsetT = tl.y - wr.top;
    g_spikeInsetR = (wr.right - wr.left) - (cr.right - cr.left) - g_spikeInsetL;
    g_spikeInsetB = (wr.bottom - wr.top) - (cr.bottom - cr.top) - g_spikeInsetT;
}

static LRESULT CALLBACK SpikeNoRedirProc(HWND h, UINT m, WPARAM w, LPARAM l)
{
    return DefWindowProcW(h, m, w, l);
}

static void spikeEnsureNoRedirChild()
{
    if (g_spikeNoRedirHwnd || !g_spikeDevice || !g_spikeTopHwnd) return;
    HINSTANCE hInst = GetModuleHandleW(nullptr);
    static bool classRegistered = false;
    const wchar_t *clsName = L"SkikoSpikeNoRedir";
    if (!classRegistered)
    {
        WNDCLASSW wc = {};
        wc.lpfnWndProc = SpikeNoRedirProc;
        wc.hInstance = hInst;
        wc.lpszClassName = clsName;
        wc.hbrBackground = nullptr; // no GDI erase — content comes solely from the DComp visual
        wc.hCursor = LoadCursor(nullptr, IDC_ARROW);
        RegisterClassW(&wc);
        classRegistered = true;
    }
    // POPUP STRATEGY (atomic-position test): a screen-positioned WS_POPUP we OWN, so we control its absolute
    // position (a child auto-follows the parent and we can't decouple that). We reposition it to the frame's
    // INCOMING client rect in WM_WINDOWPOSCHANGING via DeferWindowPos — explicitly locking a second top-level
    // window to where the frame is about to be. If top/left drags still jitter, two windows genuinely can't be
    // kept registered during an origin move. Owned by the frame; NOACTIVATE/TOOLWINDOW so it never steals focus.
    RECT crc; GetClientRect(g_spikeTopHwnd, &crc);
    POINT tl = { crc.left, crc.top }; ClientToScreen(g_spikeTopHwnd, &tl);
    UINT w = (crc.right - crc.left) > 0 ? (UINT)(crc.right - crc.left) : 1;
    UINT h = (crc.bottom - crc.top) > 0 ? (UINT)(crc.bottom - crc.top) : 1;
    g_spikeNoRedirHwnd = CreateWindowExW(
        WS_EX_NOREDIRECTIONBITMAP | WS_EX_NOACTIVATE | WS_EX_TOOLWINDOW, clsName, L"", WS_POPUP | WS_VISIBLE,
        tl.x, tl.y, (int)w, (int)h, g_spikeTopHwnd, nullptr, hInst, nullptr);
    if (!g_spikeNoRedirHwnd) { fprintf(stderr, "[spike] NoRedir child FAILED err=%lu\n", GetLastError()); fflush(stderr); return; }

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
    desc.AlphaMode = DXGI_ALPHA_MODE_IGNORE; // opaque solid-color spike
    gr_cp<IDXGISwapChain1> sc1;
    HRESULT hr = factory->CreateSwapChainForComposition(g_spikeDevice->queue.get(), &desc, nullptr, &sc1);
    if (FAILED(hr)) { fprintf(stderr, "[spike] comp swapchain FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }
    sc1->QueryInterface(IID_PPV_ARGS(&g_spikeCompSwapChain));

    hr = DCompLibrary::DCompositionCreateDevice(nullptr, IID_PPV_ARGS(&g_spikeDComp));
    if (FAILED(hr)) { fprintf(stderr, "[spike] DComp device FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }
    hr = g_spikeDComp->CreateTargetForHwnd(g_spikeNoRedirHwnd, TRUE, &g_spikeDCompTarget);
    if (FAILED(hr)) { fprintf(stderr, "[spike] CreateTargetForHwnd FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }
    g_spikeDComp->CreateVisual(&g_spikeDCompVisual);
    g_spikeDCompVisual->SetContent(g_spikeCompSwapChain.get());
    g_spikeDCompTarget->SetRoot(g_spikeDCompVisual.get());
    g_spikeDComp->Commit();
    g_spikeCompW = w; g_spikeCompH = h;
    fprintf(stderr, "[spike] NoRedir composition child created %ux%u hwnd=%p\n", w, h, (void *)g_spikeNoRedirHwnd);
    fflush(stderr);
}

// Atomic-position test: move+size the popup to an absolute SCREEN rect via DeferWindowPos, ResizeBuffers, present
// edge markers, Commit. Called from the frame's WM_WINDOWPOSCHANGING with the frame's INCOMING client screen rect.
static void spikePositionPopup(int scrX, int scrY, UINT w, UINT h)
{
    if (!g_spikeNoRedirHwnd || !g_spikeCompSwapChain.get() || !g_spikeDevice || w == 0 || h == 0) return;
    HDWP hdwp = BeginDeferWindowPos(1);
    if (hdwp) hdwp = DeferWindowPos(hdwp, g_spikeNoRedirHwnd, HWND_TOP, scrX, scrY, (int)w, (int)h, SWP_NOACTIVATE);
    if (hdwp) EndDeferWindowPos(hdwp);
    if (w != g_spikeCompW || h != g_spikeCompH)
    {
        g_spikeCompSwapChain->ResizeBuffers(0, w, h, DXGI_FORMAT_UNKNOWN, 0);
        g_spikeCompW = w; g_spikeCompH = h;
    }
    g_spikeDevice->spikeClearAndPresent(g_spikeCompSwapChain.get(), /*edgeMarkers*/ true, /*dwmFlush*/ false);
    if (g_spikeDComp) g_spikeDComp->Commit();
}

// ---- DECISIVE "can we reach ZERO bars?" test: a fully self-owned top-level window --------------------
// No AWT frame, no child, no clipping tricks. Just the canonical clean-resize construction on a window WE
// create with WS_EX_NOREDIRECTIONBITMAP + DirectComposition: ResizeBuffers + present + Commit synchronously
// inside WM_SIZE. This is the window we'd want Skiko to eventually own. If dragging THIS shows zero bars,
// perfection is achievable when we own the window (→ the real fix has a provable target). If it still
// flickers, zero bars is impossible on Windows/D3D and no architecture reaches it.
static HWND g_spikeOwnedHwnd = nullptr;
static gr_cp<IDCompositionDevice> g_spikeOwnedDComp;
static gr_cp<IDCompositionTarget> g_spikeOwnedTarget;
static gr_cp<IDCompositionVisual> g_spikeOwnedVisual;
static gr_cp<IDXGISwapChain3> g_spikeOwnedSwapChain;

static void spikePresentOwned(UINT w, UINT h)
{
    if (!g_spikeOwnedSwapChain.get() || !g_spikeDevice || w == 0 || h == 0) return;
    DXGI_SWAP_CHAIN_DESC1 d = {};
    g_spikeOwnedSwapChain->GetDesc1(&d);
    if (d.Width != w || d.Height != h)
    {
        HRESULT rb = g_spikeOwnedSwapChain->ResizeBuffers(0, w, h, DXGI_FORMAT_UNKNOWN, 0);
        fprintf(stderr, "[spike] owned ResizeBuffers %ux%u hr=0x%08lx\n", w, h, rb); fflush(stderr);
    }
    g_spikeDevice->spikeClearAndPresent(g_spikeOwnedSwapChain.get(), /*edgeMarkers*/ true, /*dwmFlush*/ false);
    if (g_spikeOwnedDComp) g_spikeOwnedDComp->Commit();
}

static LRESULT CALLBACK SpikeOwnedProc(HWND h, UINT m, WPARAM w, LPARAM l)
{
    switch (m)
    {
        case WM_ERASEBKGND:
            return 1; // no GDI — content is the DComp visual
        case WM_NCCALCSIZE:
        {
            // THE DOCUMENTED SMOOTH-RESIZE SYNC POINT (Levien / gamedev / winit#786): resize + render + present
            // + DComp Commit HERE, before returning from WM_NCCALCSIZE, so DWM composites the frame-move and our
            // content in the same pass. Only works with DirectComposition (which we have). Let DefWindowProc
            // compute the new client rect first, then present at that exact size before we return.
            LRESULT r = DefWindowProcW(h, m, w, l);
            if (w) // wParam TRUE => lParam is NCCALCSIZE_PARAMS whose rgrc[0] now holds the new client rect
            {
                NCCALCSIZE_PARAMS *p = (NCCALCSIZE_PARAMS *)l;
                LONG cw = p->rgrc[0].right - p->rgrc[0].left;
                LONG ch = p->rgrc[0].bottom - p->rgrc[0].top;
                if (cw > 0 && ch > 0) spikePresentOwned((UINT)cw, (UINT)ch);
            }
            return r;
        }
        case WM_SIZE:
            if (w != SIZE_MINIMIZED) spikePresentOwned(LOWORD(l), HIWORD(l));
            return 0;
        case WM_CLOSE:
            DestroyWindow(h);
            g_spikeOwnedHwnd = nullptr;
            return 0;
    }
    return DefWindowProcW(h, m, w, l);
}

static bool g_spikeOwnedStarted = false; // latched BEFORE CreateWindowExW to defeat re-entrant double creation

static void spikeEnsureOwnedTopLevel()
{
    if (g_spikeOwnedStarted || !g_spikeDevice) return;
    g_spikeOwnedStarted = true;
    HINSTANCE hInst = GetModuleHandleW(nullptr);
    static bool reg = false;
    const wchar_t *cls = L"SkikoSpikeOwned";
    if (!reg)
    {
        WNDCLASSW wc = {};
        wc.lpfnWndProc = SpikeOwnedProc;
        wc.hInstance = hInst;
        wc.lpszClassName = cls;
        wc.hbrBackground = nullptr;
        wc.hCursor = LoadCursor(nullptr, IDC_ARROW);
        RegisterClassW(&wc);
        reg = true;
    }
    // Created on the AWT toolkit thread (this runs from SpikeWndProc), so AWT's own message loop pumps its
    // WM_SIZE while the user drags it. WS_OVERLAPPEDWINDOW = normal titlebar/border (DWM-drawn non-client).
    g_spikeOwnedHwnd = CreateWindowExW(
        WS_EX_NOREDIRECTIONBITMAP, cls, L"SPIKE owned NOREDIR window - DRAG ME to test zero bars",
        WS_OVERLAPPEDWINDOW | WS_VISIBLE, 80, 80, 640, 640, nullptr, nullptr, hInst, nullptr);
    if (!g_spikeOwnedHwnd) { fprintf(stderr, "[spike] owned window FAILED err=%lu\n", GetLastError()); fflush(stderr); return; }

    RECT rc; GetClientRect(g_spikeOwnedHwnd, &rc);
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
    HRESULT hr = factory->CreateSwapChainForComposition(g_spikeDevice->queue.get(), &desc, nullptr, &sc1);
    if (FAILED(hr)) { fprintf(stderr, "[spike] owned comp swapchain FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }
    sc1->QueryInterface(IID_PPV_ARGS(&g_spikeOwnedSwapChain));

    hr = DCompLibrary::DCompositionCreateDevice(nullptr, IID_PPV_ARGS(&g_spikeOwnedDComp));
    if (FAILED(hr)) { fprintf(stderr, "[spike] owned DComp FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }
    g_spikeOwnedDComp->CreateTargetForHwnd(g_spikeOwnedHwnd, TRUE, &g_spikeOwnedTarget);
    g_spikeOwnedDComp->CreateVisual(&g_spikeOwnedVisual);
    g_spikeOwnedVisual->SetContent(g_spikeOwnedSwapChain.get());
    g_spikeOwnedTarget->SetRoot(g_spikeOwnedVisual.get());
    g_spikeOwnedDComp->Commit();
    fprintf(stderr, "[spike] owned NOREDIR top-level created %ux%u hwnd=%p\n", w, h, (void *)g_spikeOwnedHwnd);
    fflush(stderr);
    spikePresentOwned(w, h);
}

// Calls a no-arg method on the Direct3DRedrawer from the current (toolkit) thread.
static void spikeCallRedrawer(const char *method)
{
    if (!g_spikeRedrawer || !jvm) return;
    JNIEnv *env = nullptr;
    jint stat = jvm->GetEnv((void **)&env, JNI_VERSION_1_6);
    if (stat == JNI_EDETACHED)
    {
        jvm->AttachCurrentThread((void **)&env, nullptr);
    }
    if (!env) return;
    jclass cls = env->GetObjectClass(g_spikeRedrawer);
    jmethodID mid = env->GetMethodID(cls, method, "()V");
    if (mid) env->CallVoidMethod(g_spikeRedrawer, mid);
    env->DeleteLocalRef(cls);
}

// SPIKE 2b: ask the redrawer (on this toolkit thread) to render the REAL renderDelegate content at (w,h) into
// the frame's comp swapchain and present it. onSpikeRenderFrame calls back into makeSpikeFrameSurface /
// flushSpikeFrame / presentSpikeFrame below.
static HANDLE g_spikeRenderDoneEvent = nullptr; // signaled by the EDT (signalSpikeRenderDone) when done
static bool g_spikeRendering = false;            // re-entrancy guard while pump-waiting

static void spikeCallRenderFrame(int w, int h)
{
    if (!g_spikeRedrawer || !jvm || g_spikeRendering) return;
    JNIEnv *env = nullptr;
    jint stat = jvm->GetEnv((void **)&env, JNI_VERSION_1_6);
    if (stat == JNI_EDETACHED)
    {
        jvm->AttachCurrentThread((void **)&env, nullptr);
    }
    if (!env) return;

    if (!g_spikeRenderDoneEvent) g_spikeRenderDoneEvent = CreateEventW(nullptr, FALSE, FALSE, nullptr);
    ResetEvent(g_spikeRenderDoneEvent);

    g_spikeRendering = true;
    // onSpikeRenderFrame POSTS the render to the EDT (invokeLater) and returns immediately.
    jclass cls = env->GetObjectClass(g_spikeRedrawer);
    jmethodID mid = env->GetMethodID(cls, "onSpikeRenderFrame", "(II)V");
    if (mid) env->CallVoidMethod(g_spikeRedrawer, mid, (jint)w, (jint)h);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
    env->DeleteLocalRef(cls);

    // Pump-wait: block until the EDT signals it finished rendering, but keep servicing cross-thread SENT
    // messages so the EDT's own window ops (beginValidate/SetWindowPos marshaled to this toolkit thread) can
    // complete instead of deadlocking against us. Windows analog of LWCToolkit.invokeAndWait spinning the loop.
    for (;;)
    {
        DWORD r = MsgWaitForMultipleObjectsEx(1, &g_spikeRenderDoneEvent, INFINITE, QS_SENDMESSAGE, MWMO_INPUTAVAILABLE);
        if (r == WAIT_OBJECT_0) break; // render done
        MSG msg;
        PeekMessageW(&msg, nullptr, 0, 0, PM_NOREMOVE); // deliver pending sent messages (does not consume input)
    }
    g_spikeRendering = false;
}

// SPIKE 2b: at drag-end, synchronously (same pump-wait) run onSpikeResizeFinalize on the EDT — validate the
// frame so the canvas HWND catches up to the final client size AND renderImmediately so the canvas has
// correct-size content — BEFORE we detach the overlay. Otherwise revealing the still-lagging canvas snaps
// a frame later (the visible 1-frame jump).
static void spikeCallFinalize()
{
    if (!g_spikeRedrawer || !jvm) return;
    JNIEnv *env = nullptr;
    jint stat = jvm->GetEnv((void **)&env, JNI_VERSION_1_6);
    if (stat == JNI_EDETACHED)
    {
        jvm->AttachCurrentThread((void **)&env, nullptr);
    }
    if (!env) return;

    if (!g_spikeRenderDoneEvent) g_spikeRenderDoneEvent = CreateEventW(nullptr, FALSE, FALSE, nullptr);
    ResetEvent(g_spikeRenderDoneEvent);

    jclass cls = env->GetObjectClass(g_spikeRedrawer);
    jmethodID mid = env->GetMethodID(cls, "onSpikeResizeFinalize", "()V");
    if (mid) env->CallVoidMethod(g_spikeRedrawer, mid);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); }
    env->DeleteLocalRef(cls);

    for (;;)
    {
        DWORD r = MsgWaitForMultipleObjectsEx(1, &g_spikeRenderDoneEvent, INFINITE, QS_SENDMESSAGE, MWMO_INPUTAVAILABLE);
        if (r == WAIT_OBJECT_0) break;
        MSG msg;
        PeekMessageW(&msg, nullptr, 0, 0, PM_NOREMOVE);
    }
}

// ---- EMBEDDED-PARITY test: the validated recipe applied to the AWT FRAME itself ----------------------
// A DComp target on the AWT frame HWND (which is a normal redirected window — its redirection bitmap sits
// BEHIND our visual, harmlessly, as long as our atomic present keeps it covered). The composition swapchain
// is presented in the FRAME's WM_NCCALCSIZE (no DwmFlush) — the exact recipe proven on the owned window,
// now embedded. If this is as clean as the owned window, embedded parity is proven with NO extra child HWND.
static gr_cp<IDCompositionDevice> g_spikeFrameDComp;
static gr_cp<IDCompositionTarget> g_spikeFrameTarget;
static gr_cp<IDCompositionVisual> g_spikeFrameVisual;
static gr_cp<IDXGISwapChain3> g_spikeFrameSwapChain;
static gr_cp<ID3D12Resource> g_spikeFrameBuffers[BuffersCount]; // SPIKE 2b: wrapped-as-Skia backbuffers
static gr_cp<ID3D12Fence> g_spikeFrameFence;                    // SPIKE 2b: drain the queue before ResizeBuffers
static HANDLE g_spikeFrameFenceEvent = nullptr;
static uint64_t g_spikeFrameFenceValue = 0;
static HANDLE g_spikeFrameWaitable = nullptr;                   // SPIKE 2b: frame-latency waitable (retire presents)

// Block until the command queue has finished all submitted work — crucially the DXGI Present operations, which
// hold references to the swapchain backbuffers on the GPU timeline. Without this ResizeBuffers fails with
// DXGI_ERROR_INVALID_CALL even though the buffers have no CPU/COM references (mirrors the device swapchain's
// per-buffer fence wait in resizeBuffers).
static void spikeFrameGpuSync()
{
    if (!g_spikeDevice) return;
    if (!g_spikeFrameFence.get())
    {
        g_spikeDevice->device->CreateFence(0, D3D12_FENCE_FLAG_NONE, IID_PPV_ARGS(&g_spikeFrameFence));
        g_spikeFrameFenceEvent = CreateEventW(nullptr, FALSE, FALSE, nullptr);
    }
    const uint64_t fv = ++g_spikeFrameFenceValue;
    g_spikeDevice->queue->Signal(g_spikeFrameFence.get(), fv);
    if (g_spikeFrameFence->GetCompletedValue() < fv)
    {
        g_spikeFrameFence->SetEventOnCompletion(fv, g_spikeFrameFenceEvent);
        WaitForSingleObjectEx(g_spikeFrameFenceEvent, INFINITE, FALSE);
    }
}

static void spikeEnsureFrameDComp()
{
    if (g_spikeFrameSwapChain.get() || !g_spikeDevice || !g_spikeTopHwnd) return;
    RECT rc; GetClientRect(g_spikeTopHwnd, &rc);
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
    desc.Flags = DXGI_SWAP_CHAIN_FLAG_FRAME_LATENCY_WAITABLE_OBJECT; // bound outstanding presents so ResizeBuffers can proceed
    gr_cp<IDXGISwapChain1> sc1;
    HRESULT hr = factory->CreateSwapChainForComposition(g_spikeDevice->queue.get(), &desc, nullptr, &sc1);
    if (FAILED(hr)) return;
    sc1->QueryInterface(IID_PPV_ARGS(&g_spikeFrameSwapChain));
    g_spikeFrameSwapChain->SetMaximumFrameLatency(1);
    g_spikeFrameWaitable = g_spikeFrameSwapChain->GetFrameLatencyWaitableObject();

    hr = DCompLibrary::DCompositionCreateDevice(nullptr, IID_PPV_ARGS(&g_spikeFrameDComp));
    if (FAILED(hr)) return;
    // topmost=TRUE so our visual composites ABOVE the frame's redirection bitmap (and the canvas child).
    hr = g_spikeFrameDComp->CreateTargetForHwnd(g_spikeTopHwnd, TRUE, &g_spikeFrameTarget);
    if (FAILED(hr)) return;
    g_spikeFrameDComp->CreateVisual(&g_spikeFrameVisual);
    g_spikeFrameVisual->SetContent(g_spikeFrameSwapChain.get());
}

// Show/hide our overlay by attaching/detaching the visual root (committed).
static void spikeFrameOverlay(bool show)
{
    if (!g_spikeFrameTarget.get() || !g_spikeFrameDComp.get()) return;
    g_spikeFrameTarget->SetRoot(show ? g_spikeFrameVisual.get() : nullptr);
    g_spikeFrameDComp->Commit();
}

static void spikePresentFrame(UINT w, UINT h)
{
    if (!g_spikeFrameSwapChain.get() || !g_spikeDevice || w == 0 || h == 0) return;
    DXGI_SWAP_CHAIN_DESC1 d = {};
    g_spikeFrameSwapChain->GetDesc1(&d);
    if (d.Width != w || d.Height != h)
        g_spikeFrameSwapChain->ResizeBuffers(0, w, h, DXGI_FORMAT_UNKNOWN, 0);
    g_spikeDevice->spikeClearAndPresent(g_spikeFrameSwapChain.get(), /*edgeMarkers*/ true, /*dwmFlush*/ false, /*vblankSync*/ true);
    if (g_spikeFrameDComp) g_spikeFrameDComp->Commit();
}

// ---- ROUTE 2 real mechanism: D3D11 BITBLT swapchain writing the frame's redirection surface --------------
// D3D12 can't do the legacy bitblt swap effects (only flip), so we spin up a standalone D3D11 device (loaded
// dynamically, like this file does for D3D12/DComp) and a DXGI_SWAP_EFFECT_DISCARD swapchain on the FRAME.
// A bitblt Present copies the back buffer into the window's redirection surface THROUGH DXGI — which
// coordinates with DWM's resize, unlike raw GDI FillRect that races DWM's clear. Draw solid blue + edge
// markers via ClearView (no shaders). If this is smooth on top/left resize, Route 2's mechanism works.
static gr_cp<ID3D11Device> g_spikeD11;
static gr_cp<ID3D11DeviceContext> g_spikeD11Ctx;
static gr_cp<IDXGISwapChain1> g_spikeBitblt;
static UINT g_spikeBitbltW = 0, g_spikeBitbltH = 0;

static void spikeEnsureBitblt()
{
    if (g_spikeBitblt.get() || !g_spikeTopHwnd) return;
    typedef HRESULT(WINAPI * PFN_D3D11CreateDevice)(IDXGIAdapter *, D3D_DRIVER_TYPE, HMODULE, UINT,
        const D3D_FEATURE_LEVEL *, UINT, UINT, ID3D11Device **, D3D_FEATURE_LEVEL *, ID3D11DeviceContext **);
    static PFN_D3D11CreateDevice createDev = nullptr;
    if (!createDev)
    {
        HMODULE dll = LoadLibraryW(L"d3d11.dll");
        if (dll) createDev = (PFN_D3D11CreateDevice)GetProcAddress(dll, "D3D11CreateDevice");
        if (!createDev) { fprintf(stderr, "[spike] load D3D11CreateDevice FAILED\n"); fflush(stderr); return; }
    }
    D3D_FEATURE_LEVEL fl;
    HRESULT hr = createDev(nullptr, D3D_DRIVER_TYPE_HARDWARE, nullptr, D3D11_CREATE_DEVICE_BGRA_SUPPORT,
                           nullptr, 0, D3D11_SDK_VERSION, &g_spikeD11, &fl, &g_spikeD11Ctx);
    if (FAILED(hr)) { fprintf(stderr, "[spike] D3D11 device FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }

    gr_cp<IDXGIFactory2> factory;
    if (FAILED(CreateDXGIFactory2(0, IID_PPV_ARGS(&factory)))) return;
    RECT rc; GetClientRect(g_spikeTopHwnd, &rc);
    UINT w = (rc.right - rc.left) > 0 ? (UINT)(rc.right - rc.left) : 1;
    UINT h = (rc.bottom - rc.top) > 0 ? (UINT)(rc.bottom - rc.top) : 1;
    DXGI_SWAP_CHAIN_DESC1 desc = {};
    desc.Width = w; desc.Height = h;
    desc.Format = DXGI_FORMAT_B8G8R8A8_UNORM;
    desc.SampleDesc.Count = 1;
    desc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
    desc.BufferCount = 1;
    desc.Scaling = DXGI_SCALING_STRETCH;
    desc.SwapEffect = DXGI_SWAP_EFFECT_DISCARD; // legacy BITBLT model → present copies into the redirection surface
    hr = factory->CreateSwapChainForHwnd(g_spikeD11.get(), g_spikeTopHwnd, &desc, nullptr, nullptr, &g_spikeBitblt);
    if (FAILED(hr)) { fprintf(stderr, "[spike] bitblt swapchain FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }
    factory->MakeWindowAssociation(g_spikeTopHwnd, DXGI_MWA_NO_ALT_ENTER);
    g_spikeBitbltW = w; g_spikeBitbltH = h;
    fprintf(stderr, "[spike] bitblt swapchain created %ux%u on frame hwnd=%p\n", w, h, (void *)g_spikeTopHwnd);
    fflush(stderr);
}

static void spikePresentBitblt(UINT w, UINT h)
{
    if (!g_spikeBitblt.get() || !g_spikeD11Ctx.get() || !g_spikeD11.get() || w == 0 || h == 0) return;
    if (w != g_spikeBitbltW || h != g_spikeBitbltH)
    {
        HRESULT rb = g_spikeBitblt->ResizeBuffers(0, w, h, DXGI_FORMAT_UNKNOWN, 0);
        if (FAILED(rb)) { fprintf(stderr, "[spike] bitblt ResizeBuffers FAILED hr=0x%08lx\n", rb); fflush(stderr); return; }
        g_spikeBitbltW = w; g_spikeBitbltH = h;
    }
    gr_cp<ID3D11Texture2D> back;
    if (FAILED(g_spikeBitblt->GetBuffer(0, IID_PPV_ARGS(&back)))) return;
    gr_cp<ID3D11RenderTargetView> rtv;
    if (FAILED(g_spikeD11->CreateRenderTargetView(back.get(), nullptr, &rtv))) return;
    const float blue[] = { 0.15f, 0.45f, 0.95f, 1.0f };
    g_spikeD11Ctx->ClearRenderTargetView(rtv.get(), blue);
    gr_cp<ID3D11DeviceContext1> ctx1;
    if (SUCCEEDED(g_spikeD11Ctx->QueryInterface(IID_PPV_ARGS(&ctx1))))
    {
        const float red[] = { 0.95f, 0.10f, 0.10f, 1.0f };
        const float green[] = { 0.10f, 0.85f, 0.20f, 1.0f };
        D3D11_RECT top = { 0, 0, (LONG)w, 16 };            ctx1->ClearView(rtv.get(), red, &top, 1);
        D3D11_RECT bot = { 0, (LONG)h - 16, (LONG)w, (LONG)h }; ctx1->ClearView(rtv.get(), red, &bot, 1);
        D3D11_RECT lft = { 0, 0, 16, (LONG)h };            ctx1->ClearView(rtv.get(), red, &lft, 1);
        D3D11_RECT rgt = { (LONG)w - 16, 0, (LONG)w, (LONG)h }; ctx1->ClearView(rtv.get(), red, &rgt, 1);
        D3D11_RECT corner = { (LONG)w - 60, (LONG)h - 60, (LONG)w, (LONG)h }; ctx1->ClearView(rtv.get(), green, &corner, 1);
    }
    g_spikeBitblt->Present(0, 0);
    g_spikeD11Ctx->Flush();
}

// ---- ROUTE 2 PROVEN mechanism: Direct2D HwndRenderTarget writes the redirection bitmap via the DC ---------
// Unlike a DXGI swapchain, ID2D1HwndRenderTarget renders GPU content straight into the window's redirection
// bitmap (EndDraw presents through the DC). This is what druid/Levien shipped for smooth resize incl. the
// left edge. If this is smooth on top/left, it's Route 2's real mechanism.
static gr_cp<ID2D1Factory> g_spikeD2DFactory;
static gr_cp<ID2D1HwndRenderTarget> g_spikeD2DRT;
static UINT g_spikeD2DW = 0, g_spikeD2DH = 0;

static void spikeEnsureD2D()
{
    if (g_spikeD2DRT.get() || !g_spikeTopHwnd) return;
    if (!g_spikeD2DFactory.get())
    {
        typedef HRESULT(WINAPI * PFN_D2D1CreateFactory)(D2D1_FACTORY_TYPE, REFIID, const D2D1_FACTORY_OPTIONS *, void **);
        static PFN_D2D1CreateFactory createFactory = nullptr;
        if (!createFactory)
        {
            HMODULE dll = LoadLibraryW(L"d2d1.dll");
            if (dll) createFactory = (PFN_D2D1CreateFactory)GetProcAddress(dll, "D2D1CreateFactory");
            if (!createFactory) { fprintf(stderr, "[spike] load D2D1CreateFactory FAILED\n"); fflush(stderr); return; }
        }
        HRESULT hr = createFactory(D2D1_FACTORY_TYPE_SINGLE_THREADED, __uuidof(ID2D1Factory), nullptr, (void **)&g_spikeD2DFactory);
        if (FAILED(hr)) { fprintf(stderr, "[spike] D2D1CreateFactory FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }
    }
    RECT rc; GetClientRect(g_spikeTopHwnd, &rc);
    UINT w = (rc.right - rc.left) > 0 ? (UINT)(rc.right - rc.left) : 1;
    UINT h = (rc.bottom - rc.top) > 0 ? (UINT)(rc.bottom - rc.top) : 1;
    D2D1_RENDER_TARGET_PROPERTIES rtProps = {};
    rtProps.type = D2D1_RENDER_TARGET_TYPE_DEFAULT;
    rtProps.pixelFormat.format = DXGI_FORMAT_B8G8R8A8_UNORM;
    rtProps.pixelFormat.alphaMode = D2D1_ALPHA_MODE_IGNORE;
    rtProps.dpiX = 96.0f; rtProps.dpiY = 96.0f; // 1 DIP == 1 px for the test (ignores HiDPI)
    rtProps.usage = D2D1_RENDER_TARGET_USAGE_NONE;
    rtProps.minLevel = D2D1_FEATURE_LEVEL_DEFAULT;
    D2D1_HWND_RENDER_TARGET_PROPERTIES hwndProps = {};
    hwndProps.hwnd = g_spikeTopHwnd;
    hwndProps.pixelSize.width = w;
    hwndProps.pixelSize.height = h;
    hwndProps.presentOptions = D2D1_PRESENT_OPTIONS_IMMEDIATELY;
    HRESULT hr = g_spikeD2DFactory->CreateHwndRenderTarget(&rtProps, &hwndProps, &g_spikeD2DRT);
    if (FAILED(hr)) { fprintf(stderr, "[spike] CreateHwndRenderTarget FAILED hr=0x%08lx\n", hr); fflush(stderr); return; }
    g_spikeD2DW = w; g_spikeD2DH = h;
    fprintf(stderr, "[spike] D2D HwndRenderTarget created %ux%u on frame hwnd=%p\n", w, h, (void *)g_spikeTopHwnd);
    fflush(stderr);
}

static void spikePresentD2D(UINT w, UINT h)
{
    if (!g_spikeD2DRT.get() || w == 0 || h == 0) return;
    if (w != g_spikeD2DW || h != g_spikeD2DH)
    {
        D2D1_SIZE_U size = { w, h };
        g_spikeD2DRT->Resize(&size);
        g_spikeD2DW = w; g_spikeD2DH = h;
    }
    g_spikeD2DRT->BeginDraw();
    D2D1_COLOR_F blue = { 0.15f, 0.45f, 0.95f, 1.0f };
    g_spikeD2DRT->Clear(&blue);
    gr_cp<ID2D1SolidColorBrush> red, green;
    D2D1_COLOR_F redC = { 0.95f, 0.10f, 0.10f, 1.0f };
    D2D1_COLOR_F greenC = { 0.10f, 0.85f, 0.20f, 1.0f };
    g_spikeD2DRT->CreateSolidColorBrush(&redC, nullptr, &red);
    g_spikeD2DRT->CreateSolidColorBrush(&greenC, nullptr, &green);
    float fw = (float)w, fh = (float)h;
    if (red.get())
    {
        D2D1_RECT_F t = { 0, 0, fw, 16 };           g_spikeD2DRT->FillRectangle(&t, red.get());
        D2D1_RECT_F b = { 0, fh - 16, fw, fh };      g_spikeD2DRT->FillRectangle(&b, red.get());
        D2D1_RECT_F l = { 0, 0, 16, fh };            g_spikeD2DRT->FillRectangle(&l, red.get());
        D2D1_RECT_F r = { fw - 16, 0, fw, fh };      g_spikeD2DRT->FillRectangle(&r, red.get());
    }
    if (green.get()) { D2D1_RECT_F c = { fw - 60, fh - 60, fw, fh }; g_spikeD2DRT->FillRectangle(&c, green.get()); }
    g_spikeD2DRT->EndDraw();
}

// ---- ROUTE 2 de-risk: write the frame's OWN redirection surface with GDI --------------------------------
// GDI FillRect draws straight into the window's redirection bitmap, which DWM moves ATOMICALLY with the frame
// (this is why Notepad resizes smoothly from the left edge). If this is smooth on a top/left drag, the
// redirection surface is atomic on origin-move and Route 2 is sound — the real fix then just needs Skia's
// D3D content in that surface (via D3D11on12/D2D) instead of these solid rects.
static bool g_spikeGdiActive = false;

static void spikeGdiFill(HWND hWnd, int w, int h)
{
    if (w <= 0 || h <= 0) return;
    HDC hdc = GetDC(hWnd);
    if (!hdc) return;
    HBRUSH blue = CreateSolidBrush(RGB(38, 115, 242));
    HBRUSH red = CreateSolidBrush(RGB(242, 26, 26));
    HBRUSH green = CreateSolidBrush(RGB(26, 217, 51));
    RECT full = { 0, 0, w, h };            FillRect(hdc, &full, blue);
    RECT top = { 0, 0, w, 16 };            FillRect(hdc, &top, red);
    RECT bot = { 0, h - 16, w, h };        FillRect(hdc, &bot, red);
    RECT lft = { 0, 0, 16, h };            FillRect(hdc, &lft, red);
    RECT rgt = { w - 16, 0, w, h };        FillRect(hdc, &rgt, red);
    RECT corner = { w - 60, h - 60, w, h };FillRect(hdc, &corner, green);
    DeleteObject(blue); DeleteObject(red); DeleteObject(green);
    ReleaseDC(hWnd, hdc);
}

static void spikeGdiFillClient(HWND hWnd)
{
    RECT rc; GetClientRect(hWnd, &rc);
    spikeGdiFill(hWnd, rc.right - rc.left, rc.bottom - rc.top);
}

// SPIKE milestone 2a: the AWT frame is now WS_EX_NOREDIRECTIONBITMAP (injected at creation by the inline
// hook, see SpikeNoRedir_arm). So the frame has NO redirection bitmap — our DComp visual on the frame IS the
// frame's content. During a drag we hide the canvas child and render solid-blue + edge-markers straight to
// the frame's own composition surface, presenting in WM_NCCALCSIZE (the proven owned-window sync point, no
// DwmFlush). Expectation: atomic on ALL edges incl. top/left origin-move (no redirection surface to race).
static bool g_spikeEngaged = false; // frame overlay active (only during actual RESIZE, not plain moves)

static LRESULT CALLBACK SpikeWndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
    switch (msg)
    {
        case WM_ERASEBKGND:
            if (g_spikeGdiActive) return 1; // NOREDIR frame has no bg to erase; harmless belt-and-suspenders
            break;
        case WM_ENTERSIZEMOVE:
        {
            g_spikeGdiActive = true;
            g_spikeEngaged = false;
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
            LRESULT r = CallWindowProcW(g_spikeOrigProc, hWnd, msg, wParam, lParam);
            if (g_spikeGdiActive && wParam)
            {
                bool justEngaged = false;
                if (!g_spikeEngaged)
                {
                    // First real resize step: stop the EDT (so drawLock is ours). Create the overlay now but do
                    // NOT reveal it yet — we reveal it below, only after it has content, so DWM never composites
                    // an empty (transparent) overlay frame. Deferred to here (not WM_ENTERSIZEMOVE) so plain
                    // moves keep animating.
                    g_spikeEngaged = true;
                    justEngaged = true;
                    spikeCallRedrawer("onSpikeResizeEngaged"); // spikeResizing=true
                    spikeEnsureFrameDComp();
                }
                NCCALCSIZE_PARAMS *p = (NCCALCSIZE_PARAMS *)lParam;
                RECT c = p->rgrc[0]; // now holds the new CLIENT rect
                spikeCallRenderFrame(c.right - c.left, c.bottom - c.top); // 2b: real Skia content
                if (justEngaged)
                {
                    // The overlay now holds a presented frame: reveal it (topmost, opaque) and hide the canvas
                    // underneath it. Revealing an already-drawn overlay avoids the one transparent engage frame.
                    spikeFrameOverlay(true);
                    if (g_spikeContentHwnd) ShowWindow(g_spikeContentHwnd, SW_HIDE);
                }
            }
            return r;
        }
        case WM_EXITSIZEMOVE:
            if (g_spikeEngaged)
            {
                // Render one last overlay frame at the SETTLED client size (it keeps covering the canvas).
                RECT rc; GetClientRect(hWnd, &rc);
                spikeCallRenderFrame(rc.right - rc.left, rc.bottom - rc.top);
                // Show the canvas UNDER the still-topmost overlay, then finalize it (validate + render at the
                // final size) while it's hidden behind the overlay, then detach the overlay to reveal a
                // correct-size canvas — no 1-frame snap.
                if (g_spikeContentHwnd) ShowWindow(g_spikeContentHwnd, SW_SHOW);
                spikeCallFinalize();
                spikeFrameOverlay(false);            // detach overlay; the canvas becomes the frame content again
                g_spikeEngaged = false;
            }
            g_spikeGdiActive = false;
            spikeCallRedrawer("onSpikeResizeEnded");
            break;
    }
    return CallWindowProcW(g_spikeOrigProc, hWnd, msg, wParam, lParam);
}
// ======================= END SPIKE (throwaway) =======================

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

    // SPIKE 2b: wrap the NOREDIR frame's comp-swapchain backbuffer as a Skia SkSurface so the renderDelegate
    // can draw REAL content into it during resize (mirrors makeDirectXSurface but targets g_spikeFrameSwapChain).
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_makeSpikeFrameSurface(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong contextPtr, jint width, jint height)
    {
        GrDirectContext *context = fromJavaPointer<GrDirectContext *>(contextPtr);
        if (!g_spikeFrameSwapChain.get() || width <= 0 || height <= 0) return 0;

        // Throttle to the compositor and, critically, ensure the previous Present has RETIRED (left the DXGI
        // present queue) so its backbuffers are free — otherwise ResizeBuffers fails with DXGI_ERROR_INVALID_CALL.
        if (g_spikeFrameWaitable) WaitForSingleObjectEx(g_spikeFrameWaitable, 1000, TRUE);

        DXGI_SWAP_CHAIN_DESC1 d = {};
        g_spikeFrameSwapChain->GetDesc1(&d);
        if (d.Width != (UINT)width || d.Height != (UINT)height)
        {
            context->flush();
            context->submit(GrSyncCpu::kYes);
            for (int i = 0; i < BuffersCount; i++) g_spikeFrameBuffers[i].reset(nullptr);
            spikeFrameGpuSync(); // also drain GPU-side work referencing the buffers
            g_spikeFrameSwapChain->ResizeBuffers(0, (UINT)width, (UINT)height, DXGI_FORMAT_UNKNOWN,
                                                 DXGI_SWAP_CHAIN_FLAG_FRAME_LATENCY_WAITABLE_OBJECT);
        }

        UINT index = g_spikeFrameSwapChain->GetCurrentBackBufferIndex();
        if (FAILED(g_spikeFrameSwapChain->GetBuffer(index, IID_PPV_ARGS(&g_spikeFrameBuffers[index])))) return 0;

        GrD3DTextureResourceInfo info(nullptr, nullptr, D3D12_RESOURCE_STATE_PRESENT, DXGI_FORMAT_R8G8B8A8_UNORM, 1, 1, 0);
        info.fResource = g_spikeFrameBuffers[index];
        GrBackendTexture backendTexture = GrBackendTextures::MakeD3D((int)width, (int)height, info);
        auto result = SkSurfaces::WrapBackendTexture(
                                 context, backendTexture, kTopLeft_GrSurfaceOrigin, 0,
                                 kRGBA_8888_SkColorType, SkColorSpace::MakeSRGB(), nullptr)
                                 .release();
        return toJavaPointer(result);
    }

    // SPIKE 2b: flush the renderDelegate's draw into the frame backbuffer and transition it to PRESENT.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_flushSpikeFrame(
        JNIEnv *env, jobject redrawer, jlong contextPtr, jlong surfacePtr)
    {
        SkSurface *surface = fromJavaPointer<SkSurface *>(surfacePtr);
        GrDirectContext *context = fromJavaPointer<GrDirectContext *>(contextPtr);
        context->flush(surface, SkSurfaces::BackendSurfaceAccess::kPresent, GrFlushInfo());
        context->submit(GrSyncCpu::kYes);
    }

    // SPIKE 2b: present the frame comp swapchain (vblank-synced double-present) + DComp Commit.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_presentSpikeFrame(
        JNIEnv *env, jobject redrawer)
    {
        if (!g_spikeFrameSwapChain.get()) return;
        // Vblank-synced double-present (the top/left-jump fix from 2a).
        g_spikeFrameSwapChain->Present(0, DXGI_PRESENT_RESTART);
        g_spikeFrameSwapChain->Present(1, DXGI_PRESENT_DO_NOT_SEQUENCE);
        if (g_spikeFrameDComp) g_spikeFrameDComp->Commit();
    }

    // SPIKE 2b: the EDT calls this after rendering to release the pump-waiting toolkit thread.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_signalSpikeRenderDone(
        JNIEnv *env, jobject redrawer)
    {
        if (g_spikeRenderDoneEvent) SetEvent(g_spikeRenderDoneEvent);
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

    // SPIKE: throwaway. See the SPIKE block above.
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_Direct3DRedrawer_installSpikeResizeHook(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong windowPtr, jlong contentPtr)
    {
        g_spikeDevice = fromJavaPointer<DirectXDevice *>(devicePtr);
        g_spikeContentHwnd = fromJavaPointer<HWND>(contentPtr);
        if (g_spikeRedrawer) env->DeleteGlobalRef(g_spikeRedrawer);
        g_spikeRedrawer = env->NewGlobalRef(redrawer);

        HWND passed = fromJavaPointer<HWND>(windowPtr);
        HWND top = GetAncestor(passed, GA_ROOT); // the frame that receives WM_ENTERSIZEMOVE / WM_SIZE
        g_spikeTopHwnd = top;
        g_spikeOrigProc = (WNDPROC)SetWindowLongPtrW(top, GWLP_WNDPROC, (LONG_PTR)SpikeWndProc);
    }

    // SPIKE (milestone 1): one-shot INLINE hook on user32!CreateWindowExW that injects
    // WS_EX_NOREDIRECTIONBITMAP into the frame's creation. `WS_EX_NOREDIRECTIONBITMAP` is creation-only.
    // IAT hooking couldn't catch AWT's frame (its import of CreateWindowExW isn't discoverable in awt.dll's
    // import table — ordinal/delay/dynamic), so we patch the function's own prologue: every caller, however
    // it imports the symbol, reaches this address. Arm AFTER the JFrame constructor and BEFORE the first
    // pack()/setVisible(): the peer HWND is realized lazily in addNotify(). JDK-agnostic (a runtime hook).
    typedef HWND(WINAPI *CreateWindowExW_t)(DWORD, LPCWSTR, LPCWSTR, DWORD, int, int, int, int, HWND, HMENU, HINSTANCE, LPVOID);
    static CreateWindowExW_t g_realCreateWindowExW = nullptr;
    static volatile LONG g_armNoRedir = 0;
    static void *g_hookAddr = nullptr;   // == user32!CreateWindowExW
    static BYTE g_origPrologue[14];      // saved bytes overwritten by the jmp
    static BYTE g_jmpPrologue[14];       // FF 25 00000000 <8-byte abs target> = jmp [rip+0]; qword target

    static void SpikeWriteCode(void *dst, const void *src, size_t n)
    {
        DWORD old;
        VirtualProtect(dst, n, PAGE_EXECUTE_READWRITE, &old);
        memcpy(dst, src, n);
        VirtualProtect(dst, n, old, &old);
        FlushInstructionCache(GetCurrentProcess(), dst, n);
    }

    static HWND WINAPI SpikeCreateWindowExW(DWORD dwExStyle, LPCWSTR lpClassName, LPCWSTR lpWindowName,
        DWORD dwStyle, int X, int Y, int nWidth, int nHeight, HWND hWndParent, HMENU hMenu, HINSTANCE hInstance, LPVOID lpParam)
    {
        bool applied = false;
        // Only the top-level frame (not the WS_CHILD canvas / focus proxy). One-shot: disarm on first match.
        if (g_armNoRedir && !(dwStyle & WS_CHILD)) {
            dwExStyle |= WS_EX_NOREDIRECTIONBITMAP;
            InterlockedExchange(&g_armNoRedir, 0);
            applied = true;
        }
        // Temporarily restore the original prologue and call through, then re-arm the hook. This avoids
        // building a relocated trampoline (no instruction-length decoding needed). The frame is created on
        // the single AWT-Windows toolkit thread, so the brief unhooked window is safe here.
        SpikeWriteCode(g_hookAddr, g_origPrologue, sizeof(g_origPrologue));
        HWND h = g_realCreateWindowExW(dwExStyle, lpClassName, lpWindowName, dwStyle, X, Y, nWidth, nHeight,
                                       hWndParent, hMenu, hInstance, lpParam);
        // Rehook to keep watching for our target; but once the one-shot has fired, leave the prologue
        // restored so we stop intercepting process-wide. A later arm() reinstalls it.
        if (!applied)
            SpikeWriteCode(g_hookAddr, g_jmpPrologue, sizeof(g_jmpPrologue));
        return h;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_redrawer_SpikeNoRedir_arm(JNIEnv *env, jobject obj)
    {
        if (!g_hookAddr) {
            HMODULE u32 = GetModuleHandleW(L"user32.dll");
            void *realAddr = u32 ? (void *)GetProcAddress(u32, "CreateWindowExW") : nullptr;
            if (!realAddr) return;
            g_hookAddr = realAddr;
            g_realCreateWindowExW = (CreateWindowExW_t)realAddr;
            memcpy(g_origPrologue, realAddr, sizeof(g_origPrologue));
            g_jmpPrologue[0] = 0xFF; // jmp qword ptr [rip+0]
            g_jmpPrologue[1] = 0x25;
            *(DWORD *)(g_jmpPrologue + 2) = 0;
            *(void **)(g_jmpPrologue + 6) = (void *)&SpikeCreateWindowExW;
        }
        // (Re)install the prologue jmp — the previous one-shot may have uninstalled it.
        SpikeWriteCode(g_hookAddr, g_jmpPrologue, sizeof(g_jmpPrologue));
        InterlockedExchange(&g_armNoRedir, 1);
    }
}

#endif
