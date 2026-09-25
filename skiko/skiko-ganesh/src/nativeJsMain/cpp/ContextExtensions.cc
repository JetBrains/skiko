#include "SkCanvas.h"
#include "SkSurface.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_ganesh_ContextExtensions__1nGetCanvasRecordingContext
  (KNativePointer canvasPtr) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    return reinterpret_cast<KNativePointer>(canvas->recordingContext());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_ganesh_ContextExtensions__1nGetSurfaceRecordingContext
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return reinterpret_cast<KNativePointer>(surface->recordingContext());
}