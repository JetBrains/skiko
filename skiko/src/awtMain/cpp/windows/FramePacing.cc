#include <windows.h>

#include <dwmapi.h>
#include <dxgi.h>
#include <stdlib.h>
#include <wchar.h>

#include <jni.h>

/*
 * Skiko-owned pacing clocks, delivering ticks to
 * org.jetbrains.skiko.swing.WinNativeClock.onNativeTick from a daemon-attached
 * native thread. QPC timestamps are converted to the System.nanoTime() time
 * base (also QPC derived).
 *
 * Preferred source is IDXGIOutput::WaitForVBlank on the output belonging to the
 * clock's display: a genuine per-display hardware vblank, so each display is
 * paced at its own true refresh rate.
 *
 * Fallback source is DWM composition timing, which carries a single
 * desktop-wide cadence (DWM composes at the fastest connected display's rate),
 * waited out with a high-resolution waitable timer. Deliberately never calls
 * DwmFlush: in-process it can block for hundreds of milliseconds when the
 * client gates presents on the same tick.
 *
 * Displays are looked up by EnumDisplayMonitors index: AWT's Windows screen
 * indices follow the same enumeration order, so a screen index is enough to
 * find the matching output.
 */

typedef HRESULT (WINAPI *DwmGetCompositionTimingInfoType)(HWND, DWM_TIMING_INFO *);
typedef HRESULT (WINAPI *CreateDXGIFactory1Type)(REFIID, void **);

static DwmGetCompositionTimingInfoType pDwmGetCompositionTimingInfo = NULL;
static CreateDXGIFactory1Type pCreateDXGIFactory1 = NULL;
static JavaVM *framePacingJvm = NULL;
static jmethodID framePacingOnNativeTickMID = NULL;
static LONGLONG framePacingQpcFrequency = 0;

typedef struct {
    HANDLE thread;
    HANDLE stopEvent;
    jobject clockRef;
    jlong fallbackPeriodNanos;
    IDXGIOutput *output; // NULL for the DWM composition clock
} FramePacingClock;

static BOOL loadDwm()
{
    if (pDwmGetCompositionTimingInfo != NULL) {
        return TRUE;
    }
    HMODULE dwm = LoadLibraryW(L"dwmapi.dll");
    if (dwm == NULL) {
        return FALSE;
    }
    pDwmGetCompositionTimingInfo =
            (DwmGetCompositionTimingInfoType)GetProcAddress(dwm, "DwmGetCompositionTimingInfo");
    return pDwmGetCompositionTimingInfo != NULL;
}

static BOOL loadDxgi()
{
    if (pCreateDXGIFactory1 != NULL) {
        return TRUE;
    }
    HMODULE dxgi = LoadLibraryW(L"dxgi.dll");
    if (dxgi == NULL) {
        return FALSE;
    }
    pCreateDXGIFactory1 = (CreateDXGIFactory1Type)GetProcAddress(dxgi, "CreateDXGIFactory1");
    return pCreateDXGIFactory1 != NULL;
}

typedef struct {
    int index;
    int target;
    HMONITOR found;
} MonitorSearch;

static BOOL CALLBACK monitorEnumProc(HMONITOR monitor, HDC hdc, LPRECT rect, LPARAM param)
{
    MonitorSearch *search = (MonitorSearch *)param;
    if (search->index++ == search->target) {
        search->found = monitor;
        return FALSE;
    }
    return TRUE;
}

/*
 * Win32 display device name ("\\.\DISPLAY1") for an AWT screen index. This is
 * what DXGI_OUTPUT_DESC.DeviceName carries, and matching on it avoids
 * comparing desktop rectangles, which would have to account for per-monitor
 * DPI scaling to be correct. AWT's screen indices follow EnumDisplayMonitors
 * order, so the same enumeration recovers the HMONITOR.
 */
static BOOL monitorDeviceName(int screen, WCHAR *name, size_t nameChars)
{
    MonitorSearch search;
    search.index = 0;
    search.target = screen;
    search.found = NULL;
    ::EnumDisplayMonitors(NULL, NULL, monitorEnumProc, (LPARAM)&search);
    if (search.found == NULL) {
        return FALSE;
    }

    MONITORINFOEXW info;
    ZeroMemory(&info, sizeof(info));
    info.cbSize = sizeof(info);
    if (!::GetMonitorInfoW(search.found, (LPMONITORINFO)&info)) {
        return FALSE;
    }

    return wcscpy_s(name, nameChars, info.szDevice) == 0;
}

/* Returns the output with a reference held, or NULL. */
static IDXGIOutput *findOutput(const WCHAR *deviceName)
{
    if (!loadDxgi()) {
        return NULL;
    }

    IDXGIFactory1 *factory = NULL;
    if (FAILED(pCreateDXGIFactory1(__uuidof(IDXGIFactory1), (void **)&factory))) {
        return NULL;
    }

    IDXGIOutput *found = NULL;
    IDXGIAdapter1 *adapter = NULL;
    for (UINT ai = 0;
            found == NULL && factory->EnumAdapters1(ai, &adapter) != DXGI_ERROR_NOT_FOUND;
            ai++) {
        IDXGIOutput *output = NULL;
        for (UINT oi = 0; adapter->EnumOutputs(oi, &output) != DXGI_ERROR_NOT_FOUND; oi++) {
            DXGI_OUTPUT_DESC desc;
            if (SUCCEEDED(output->GetDesc(&desc)) && desc.AttachedToDesktop &&
                    wcscmp(desc.DeviceName, deviceName) == 0) {
                found = output; // keep this one's reference
                break;
            }
            output->Release();
        }
        adapter->Release();
    }

    factory->Release();
    return found;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_jetbrains_skiko_swing_WinNativeClock_nativeProbeVBlank(JNIEnv *env, jclass cls)
{
    if (!loadDxgi()) {
        return JNI_FALSE;
    }

    IDXGIFactory1 *factory = NULL;
    if (FAILED(pCreateDXGIFactory1(__uuidof(IDXGIFactory1), (void **)&factory))) {
        return JNI_FALSE;
    }

    // Available means at least one output is attached to the desktop; a remote
    // session enumerates adapters but no attached outputs.
    jboolean available = JNI_FALSE;
    IDXGIAdapter1 *adapter = NULL;
    for (UINT ai = 0;
            !available && factory->EnumAdapters1(ai, &adapter) != DXGI_ERROR_NOT_FOUND;
            ai++) {
        IDXGIOutput *output = NULL;
        for (UINT oi = 0; adapter->EnumOutputs(oi, &output) != DXGI_ERROR_NOT_FOUND; oi++) {
            DXGI_OUTPUT_DESC desc;
            if (SUCCEEDED(output->GetDesc(&desc)) && desc.AttachedToDesktop) {
                available = JNI_TRUE;
            }
            output->Release();
            if (available) {
                break;
            }
        }
        adapter->Release();
    }

    factory->Release();
    return available;
}

} // extern "C"
