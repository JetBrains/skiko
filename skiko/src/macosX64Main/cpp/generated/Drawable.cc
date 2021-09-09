
// This file has been auto generated.

#include <iostream>
#include "SkDrawable.h"
#include "common.h"


extern "C" jlong org_jetbrains_skia_Drawable__1nMake
  (kref __Kinstance) {
    TODO("implement org_jetbrains_skia_Drawable__1nMake");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Drawable__1nMake
  (kref __Kinstance) {
    SkijaDrawableImpl* instance = new SkijaDrawableImpl();
    return reinterpret_cast<jlong>(instance);
}
#endif



extern "C" void org_jetbrains_skia_Drawable__1nInit
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Drawable__1nInit");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Drawable__1nInit
  (kref __Kinstance, jlong ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    instance->init(env, jthis);
}
#endif



extern "C" void org_jetbrains_skia_Drawable__1nDraw
  (kref __Kinstance, jlong ptr, jlong canvasPtr, jfloatArray matrixArr) {
    TODO("implement org_jetbrains_skia_Drawable__1nDraw");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Drawable__1nDraw
  (kref __Kinstance, jlong ptr, jlong canvasPtr, jfloatArray matrixArr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    instance->draw(canvas, matrix.get());
}
#endif



extern "C" jlong org_jetbrains_skia_Drawable__1nMakePictureSnapshot
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Drawable__1nMakePictureSnapshot");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Drawable__1nMakePictureSnapshot
  (kref __Kinstance, jlong ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->newPictureSnapshot());
}
#endif



extern "C" jint org_jetbrains_skia_Drawable__1nGetGenerationId
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Drawable__1nGetGenerationId");
}
     
#if 0 
extern "C" jint org_jetbrains_skia_Drawable__1nGetGenerationId
  (kref __Kinstance, jlong ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}
#endif



extern "C" void org_jetbrains_skia_Drawable__1nNotifyDrawingChanged
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Drawable__1nNotifyDrawingChanged");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Drawable__1nNotifyDrawingChanged
  (kref __Kinstance, jlong ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    return instance->notifyDrawingChanged();
}
#endif

