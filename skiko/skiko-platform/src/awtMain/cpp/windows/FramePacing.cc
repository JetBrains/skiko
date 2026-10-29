#include <windows.h>

#include <dxgi.h>
#include <stdlib.h>
#include <wchar.h>

#include <jni.h>

/*
 * The vblank source behind WinVBlankClock: IDXGIOutput::WaitForVBlank on the
 * output that drives the AWT screen. The Kotlin clock thread opens it, waits on
 * it in a loop and closes it; nothing here runs on a thread of its own.
 *
 * AWT's screen index follows EnumDisplayMonitors order, and a monitor's device
 * name ("\\.\DISPLAY1") is what DXGI_OUTPUT_DESC.DeviceName carries, so the
 * index resolves to an output by name.
 */

typedef HRESULT (WINAPI *CreateDXGIFactory1Type)(REFIID, void **);

/*
 * Clocks for different displays open from different threads at the same time,
 * so the process-wide values are function-local statics: initialised exactly
 * once, under the runtime's lock, however many threads race to first use.
 */
static CreateDXGIFactory1Type createDxgiFactory1() {
    static const CreateDXGIFactory1Type entry = []() -> CreateDXGIFactory1Type {
        HMODULE dxgi = LoadLibraryW(L"dxgi.dll");
        return dxgi == NULL ? NULL : (CreateDXGIFactory1Type)GetProcAddress(dxgi, "CreateDXGIFactory1");
    }();
    return entry;
}

static LONGLONG qpcFrequency() {
    static const LONGLONG frequency = []() {
        LARGE_INTEGER value;
        QueryPerformanceFrequency(&value);
        return value.QuadPart;
    }();
    return frequency;
}

typedef struct {
    int index;
    int target;
    HMONITOR found;
} MonitorSearch;

static BOOL CALLBACK monitorEnumProc(HMONITOR monitor, HDC hdc, LPRECT rect, LPARAM param) {
    MonitorSearch *search = (MonitorSearch *)param;
    if (search->index++ == search->target) {
        search->found = monitor;
        return FALSE;
    }
    return TRUE;
}

static BOOL monitorDeviceName(int screen, WCHAR *name, size_t nameChars) {
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

/* The desktop-attached output with this device name, with a reference held, or NULL. */
static IDXGIOutput *findOutput(const WCHAR *deviceName) {
    CreateDXGIFactory1Type createFactory = createDxgiFactory1();
    if (createFactory == NULL) {
        return NULL;
    }

    IDXGIFactory1 *factory = NULL;
    if (FAILED(createFactory(__uuidof(IDXGIFactory1), (void **)&factory))) {
        return NULL;
    }

    // Both enumerations stop on any failure, not only DXGI_ERROR_NOT_FOUND: on a
    // failure the out-pointer is NULL, and an adapter can refuse to enumerate
    // outputs in a session that has no desktop.
    IDXGIOutput *found = NULL;
    IDXGIAdapter1 *adapter = NULL;
    for (UINT ai = 0; found == NULL && SUCCEEDED(factory->EnumAdapters1(ai, &adapter)); ai++) {
        IDXGIOutput *output = NULL;
        for (UINT oi = 0; SUCCEEDED(adapter->EnumOutputs(oi, &output)); oi++) {
            DXGI_OUTPUT_DESC desc;
            if (SUCCEEDED(output->GetDesc(&desc)) && desc.AttachedToDesktop
                    && wcscmp(desc.DeviceName, deviceName) == 0) {
                found = output;
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

JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_WinVBlankClock_nativeOpen(JNIEnv *env, jclass cls, jint screen)
{
    WCHAR deviceName[CCHDEVICENAME];
    if (!monitorDeviceName((int)screen, deviceName, CCHDEVICENAME)) {
        return 0;
    }
    return (jlong)(intptr_t)findOutput(deviceName);
}

/*
 * Returns the vblank time on the System.nanoTime() scale, which is QPC based as
 * well, or -1 when the output is gone.
 */
JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_swing_WinVBlankClock_nativeWaitTick(JNIEnv *env, jclass cls, jlong handle)
{
    IDXGIOutput *output = (IDXGIOutput *)(intptr_t)handle;
    if (FAILED(output->WaitForVBlank())) {
        return -1;
    }

    LARGE_INTEGER now;
    QueryPerformanceCounter(&now);
    return (jlong)((double)now.QuadPart * 1000000000.0 / (double)qpcFrequency());
}

JNIEXPORT void JNICALL
Java_org_jetbrains_skiko_swing_WinVBlankClock_nativeClose(JNIEnv *env, jclass cls, jlong handle)
{
    ((IDXGIOutput *)(intptr_t)handle)->Release();
}

} // extern "C"
