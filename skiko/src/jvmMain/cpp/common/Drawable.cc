#include <iostream>
#include <jni.h>
#include "SkDrawable.h"
#include "interop.hh"

class SkijaDrawableImpl: public SkDrawable {
public:
    SkijaDrawableImpl() {}

    ~SkijaDrawableImpl() {
        JNIEnv* env;
        if (fJavaVM->GetEnv(reinterpret_cast<void**>(&env), SKIKO_JNI_VERSION) == JNI_OK)
          env->DeleteGlobalRef(fObject);
    }

    void init(JNIEnv* e, jobject o) {
        fEnv = e;
        fEnv->GetJavaVM(&fJavaVM);
        fObject = fEnv->NewGlobalRef(o);
    }

protected:
    void onDraw(SkCanvas* canvas) override {
        fEnv->CallVoidMethod(fObject, skija::Drawable::onDraw, reinterpret_cast<jlong>(canvas));
        java::lang::Throwable::exceptionThrown(fEnv);
    }

    SkRect onGetBounds() override {
        skija::AutoLocal<jobject> rect(fEnv, fEnv->CallObjectMethod(fObject, skija::Drawable::onGetBounds));
        java::lang::Throwable::exceptionThrown(fEnv);
        return *(skija::Rect::toSkRect(fEnv, rect.get()));
    }

private:
    JNIEnv* fEnv;
    JavaVM* fJavaVM;
    jobject fObject;
};

static void deleteDrawable(SkijaDrawableImpl* drawable) {
    delete drawable;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DrawableKt_Drawable_1nGetFinalizer() {
    return reinterpret_cast<jlong>(&deleteDrawable);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DrawableKt_Drawable_1nMake
  (JNIEnv* env, jclass jclass) {
    SkijaDrawableImpl* instance = new SkijaDrawableImpl();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_Drawable_1jvmKt_Drawable_1nInit
  (JNIEnv* env,  jclass jclass, jobject jthis, jlong ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    instance->init(env, jthis);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DrawableKt__1nDraw
  (JNIEnv* env, jclass jclass, jlong ptr, jlong canvasPtr, jfloatArray matrixArr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArr);
    instance->draw(canvas, matrix.get());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DrawableKt__1nMakePictureSnapshot
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(instance->newPictureSnapshot());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_DrawableKt_Drawable_1nGetGenerationId
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DrawableKt__1nNotifyDrawingChanged
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    return instance->notifyDrawingChanged();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DrawableKt_Drawable_1nGetBounds
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray result) {
    SkijaDrawableImpl* instance = reinterpret_cast<SkijaDrawableImpl*>(static_cast<uintptr_t>(ptr));
    skija::Rect::copyToInterop(env, instance->getBounds(), result);
}
