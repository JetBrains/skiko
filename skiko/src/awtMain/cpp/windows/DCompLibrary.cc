#pragma once

#include "include/DCompLibrary.h"

namespace DCompLibrary {
    HINSTANCE getLibrary() {
        static HINSTANCE library = LoadLibrary("dcomp.dll");
        return library;
    }

    HRESULT DCompositionCreateDevice(
        IDXGIDevice *dxgiDevice,
        REFIID iid,
        void **dcompositionDevice
    ) {
        HINSTANCE library = getLibrary();

        if (library != nullptr) {
            typedef HRESULT(WINAPI * PROC_DCompositionCreateDevice)(IDXGIDevice * dxgiDevice, REFIID iid, void **dcompositionDevice);
            static auto compositionCreateDevice = (PROC_DCompositionCreateDevice) GetProcAddress(library, "DCompositionCreateDevice");
            return compositionCreateDevice(dxgiDevice, iid, dcompositionDevice);
        } else {
            return E_FAIL;
        }
    }
};
