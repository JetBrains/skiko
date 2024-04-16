#pragma once

#include <dcomp.h>

namespace DCompLibrary {
    HRESULT DCompositionCreateDevice(
        IDXGIDevice *dxgiDevice,
        REFIID iid,
        void **dcompositionDevice
    );
};
