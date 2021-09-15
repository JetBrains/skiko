
// This file has been auto generated.

#include <iostream>
#include "SkDrawable.h"
#include "common.h"


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Drawable__1nMake
  (){
    TODO("implement org_jetbrains_skia_Drawable__1nMake");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Drawable__1nMake
  (){
    SkijaDrawableImpl* instance = new SkijaDrawableImpl();
    return reinterpret_cast<KNativePointer>(instance);
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nInit
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Drawable__1nInit");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nInit
  (KNativePointer ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>((ptr));
    instance->init(env, jthis);
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nDraw
  (KNativePointer ptr, KNativePointer canvasPtr, KFloat* matrixArr) {
    TODO("implement org_jetbrains_skia_Drawable__1nDraw");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nDraw
  (KNativePointer ptr, KNativePointer canvasPtr, KFloat* matrixArr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    instance->draw(canvas, matrix.get());
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Drawable__1nMakePictureSnapshot
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Drawable__1nMakePictureSnapshot");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Drawable__1nMakePictureSnapshot
  (KNativePointer ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>((ptr));
    return reinterpret_cast<KNativePointer>(instance->newPictureSnapshot());
}
#endif



SKIKO_EXPORT KInt org_jetbrains_skia_Drawable__1nGetGenerationId
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Drawable__1nGetGenerationId");
}
     
#if 0 
SKIKO_EXPORT KInt org_jetbrains_skia_Drawable__1nGetGenerationId
  (KNativePointer ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>((ptr));
    return instance->getGenerationID();
}
#endif



SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nNotifyDrawingChanged
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Drawable__1nNotifyDrawingChanged");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_Drawable__1nNotifyDrawingChanged
  (KNativePointer ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>((ptr));
    return instance->notifyDrawingChanged();
}
#endif

