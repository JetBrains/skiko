
// This file has been auto generated.

#include <iostream>
#include "SkCanvas.h"
#include "SkDrawable.h"
#include "SkPaintFilterCanvas.h"
#include "common.h"


SKIKO_EXPORT void org_jetbrains_skia_PaintFilterCanvas__1nAttachToJava
  (KInteropPointer __Kinstance, KNativePointer canvasPtr, KBoolean unrollDrawable) {
    TODO("implement org_jetbrains_skia_PaintFilterCanvas__1nAttachToJava");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_PaintFilterCanvas__1nAttachToJava
  (KInteropPointer __Kinstance, KNativePointer canvasPtr, KBoolean unrollDrawable) {
    SkijaPaintFilterCanvas* canvas = reinterpret_cast<SkijaPaintFilterCanvas*>((canvasPtr));
    canvas->jobj = skija::PaintFilterCanvas::attach(env, jobj);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_PaintFilterCanvas__1nMake
  (KInteropPointer __Kinstance, KNativePointer canvasPtr, KBoolean unrollDrawable) {
    TODO("implement org_jetbrains_skia_PaintFilterCanvas__1nMake");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_PaintFilterCanvas__1nMake
  (KInteropPointer __Kinstance, KNativePointer canvasPtr, KBoolean unrollDrawable) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkijaPaintFilterCanvas* filterCanvas = new SkijaPaintFilterCanvas(canvas, unrollDrawable);
    return reinterpret_cast<KNativePointer>(filterCanvas);
}
#endif

