#include "types.h"

extern "C" jlong org_jetbrains_skia_Canvas__1nGetFinalizer();

extern "C" jlong org_jetbrains_skia_Canvas__1nMakeFromBitmap (jlong bitmapPtr, jint flags, jint pixelGeometry);

extern "C" void org_jetbrains_skia_Canvas__1nDrawPoint (jlong canvasPtr, jfloat x, jfloat y, jlong paintPtr);

extern "C" void org_jetbrains_skia_Canvas__1nDrawLine (jlong canvasPtr, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jlong paintPtr);

extern "C" void org_jetbrains_skia_Canvas__1nDrawArc (jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloat startAngle, jfloat sweepAngle, jboolean includeCenter, jlong paintPtr);

extern "C" void org_jetbrains_skia_Canvas__1nDrawRect (jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jlong paintPtr);

extern "C" void org_jetbrains_skia_Canvas__1nDrawOval (jlong canvasPtr, jfloat left, jfloat top, jfloat right, jfloat bottom, jlong paintPtr);


extern "C" void org_jetbrains_skia_Canvas__1nDrawPath (jlong canvasPtr, jlong pathPtr, jlong paintPtr);

// extern "C" void org_jetbrains_skia_Canvas__1nDrawImageRect (jlong canvasPtr, jlong imagePtr, jfloat sl, jfloat st, jfloat sr, jfloat sb, jfloat dl, jfloat dt, jfloat dr, jfloat db, jlong samplingMode, jlong paintPtr, jboolean strict);
  
