
// This file has been auto generated.

#include <iostream>
#include "SkCanvas.h"
#include "SkDrawable.h"
#include "SkPaintFilterCanvas.h"
#include "common.h"


extern "C" void org_jetbrains_skia_PaintFilterCanvas__1nAttachToJava
  (kref __Kinstance, jlong canvasPtr, jboolean unrollDrawable) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_PaintFilterCanvas__1nAttachToJava");
}
     
#if 0 
extern "C" void org_jetbrains_skia_PaintFilterCanvas__1nAttachToJava
  (kref __Kinstance, jlong canvasPtr, jboolean unrollDrawable) {
    SkijaPaintFilterCanvas* canvas = reinterpret_cast<SkijaPaintFilterCanvas*>(static_cast<uintptr_t>(canvasPtr));
    canvas->jobj = skija::PaintFilterCanvas::attach(env, jobj);
}
#endif



extern "C" jlong org_jetbrains_skia_PaintFilterCanvas__1nMake
  (kref __Kinstance, jlong canvasPtr, jboolean unrollDrawable) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_PaintFilterCanvas__1nMake");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_PaintFilterCanvas__1nMake
  (kref __Kinstance, jlong canvasPtr, jboolean unrollDrawable) {
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkijaPaintFilterCanvas* filterCanvas = new SkijaPaintFilterCanvas(canvas, unrollDrawable);
    return reinterpret_cast<jlong>(filterCanvas);
}
#endif

