// Windows binds an import to any already loaded DLL with the same file name, e.g. another IDEA plugin's older
// skiko-windows-x64.dll. This delay-load hook loads /DELAYLOAD dependencies by full path from our own directory.
//
// Hook mechanism: https://learn.microsoft.com/en-us/cpp/build/reference/understanding-the-helper-function

#include <windows.h>
#include <delayimp.h>
#include <wchar.h>  // wcsrchr

// This DLL's handle, found through the address of one of its own functions.
static HMODULE getThisModule() {
    HMODULE module = nullptr;
    GetModuleHandleExW(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
                       reinterpret_cast<LPCWSTR>(&getThisModule), &module);
    return module;
}

// Writes <directory of this DLL>\<dllName> to `path`. Returns false if that fails or does not fit.
static bool getSiblingPath(const char* dllName, wchar_t* path, DWORD capacity) {
    HMODULE self = getThisModule();
    // Never pass a null module: that would be the host executable.
    DWORD length = self ? GetModuleFileNameW(self, path, capacity) : 0;
    wchar_t* name = length > 0 && length < capacity ? wcsrchr(path, L'\\') : nullptr;
    if (!name) return false;

    // Replace the file name. Import names are narrow (ASCII), the path is wide. Returns 0 if it does not fit.
    return MultiByteToWideChar(CP_UTF8, 0, dllName, -1, name + 1, static_cast<int>(path + capacity - (name + 1))) > 0;
}

static FARPROC WINAPI loadSiblingLibrary(unsigned event, PDelayLoadInfo info) {
    if (event != dliNotePreLoadLibrary) return nullptr;

    wchar_t path[1024];
    if (getSiblingPath(info->szDll, path, ARRAYSIZE(path))) {
        // A full path is a different module from a same-named DLL in another directory.
        HMODULE module = LoadLibraryExW(path, nullptr, LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR | LOAD_LIBRARY_SEARCH_SYSTEM32);
        if (module) return reinterpret_cast<FARPROC>(module);
    }

    // Returning null would load by name, possibly binding to another IDEA plugin's DLL: fail instead.
    info->dwLastError = GetLastError();
    ULONG_PTR arguments[] = {reinterpret_cast<ULONG_PTR>(info)};
    RaiseException(VcppException(ERROR_SEVERITY_ERROR, ERROR_MOD_NOT_FOUND), EXCEPTION_NONCONTINUABLE, 1, arguments);
    return nullptr;
}

extern "C" const PfnDliHook __pfnDliNotifyHook2 = loadSiblingLibrary;
