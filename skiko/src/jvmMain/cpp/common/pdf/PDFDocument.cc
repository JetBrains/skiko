#include <jni.h>
#include "SkPDFDocument.h"
#include "../interop.hh"

static void copyJIntArrayToDateTime(JNIEnv* env, jintArray& jarr, SkPDF::DateTime* result) {
    if (jarr == nullptr) {
        *result = {};
    } else {
        jint* arr = env->GetIntArrayElements(jarr, 0);
        result->fTimeZoneMinutes = arr[6];
        result->fYear = arr[0];
        result->fMonth = arr[1];
        result->fDayOfWeek = -1;
        result->fDay = arr[2];
        result->fHour = arr[3];
        result->fMinute = arr[4];
        result->fSecond = arr[5];
        env->ReleaseIntArrayElements(jarr, arr, 0);
    }
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_pdf_PDFDocumentKt__1nMakeDocument(
    JNIEnv* env,
    jclass jclass,
    jlong wstreamPtr,
    jstring jtitle,
    jstring jauthor,
    jstring jsubject,
    jstring jkeywords,
    jstring jcreator,
    jstring jproducer,
    jintArray jcreation,
    jintArray jmodified,
    jstring jlang,
    jfloat rasterDPI,
    jboolean pdfA,
    jint encodingQuality,
    jint compressionLevel
) {
    SkPDF::DateTime creation, modified;
    copyJIntArrayToDateTime(env, jcreation, &creation);
    copyJIntArrayToDateTime(env, jmodified, &modified);
    SkPDF::Metadata metadata;
    metadata.fTitle = skString(env, jtitle);
    metadata.fAuthor = skString(env, jauthor);
    metadata.fSubject = skString(env, jsubject);
    metadata.fKeywords = skString(env, jkeywords);
    metadata.fCreator = skString(env, jcreator);
    metadata.fProducer = skString(env, jproducer);
    metadata.fCreation = creation;
    metadata.fModified = modified;
    metadata.fLang = skString(env, jlang);
    metadata.fRasterDPI = rasterDPI;
    metadata.fPDFA = pdfA;
    metadata.fEncodingQuality = encodingQuality;
    metadata.fCompressionLevel = static_cast<SkPDF::Metadata::CompressionLevel>(compressionLevel);
    SkWStream* wstream = reinterpret_cast<SkWStream*>(static_cast<uintptr_t>(wstreamPtr));
    SkDocument* instance = SkPDF::MakeDocument(wstream, metadata).release();
    return reinterpret_cast<jlong>(instance);
}
