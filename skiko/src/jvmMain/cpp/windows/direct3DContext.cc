#ifdef SK_DIRECT3D
#include <stdexcept>
#include <locale>
#include <Windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"

#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "SkSurface.h"

extern "C"
{
    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_context_Direct3DContextHandler_flush(
        JNIEnv *env, jobject redrawer, jlong contextPtr, jlong surfacePtr)
    {
        SkSurface *surface = fromJavaPointer<SkSurface *>(surfacePtr);
        GrDirectContext *context = fromJavaPointer<GrDirectContext *>(contextPtr);

        surface->flushAndSubmit(true);
        surface->flush(SkSurface::BackendSurfaceAccess::kPresent, GrFlushInfo());
        context->flush({});
        context->submit(true);
    }
}

#endif
