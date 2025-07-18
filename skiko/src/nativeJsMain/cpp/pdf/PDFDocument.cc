#include "SkPDFDocument.h"
#include "common.h"

static void copyKIntArrayToDateTime(KInt* arr, SkPDF::DateTime* result) {
    if (arr == nullptr) {
        *result = {};
    } else {
        result->fTimeZoneMinutes = arr[6];
        result->fYear = arr[0];
        result->fMonth = arr[1];
        result->fDayOfWeek = -1;
        result->fDay = arr[2];
        result->fHour = arr[3];
        result->fMinute = arr[4];
        result->fSecond = arr[5];
    }
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_pdf_PDFDocument__1nMakeDocument(
    KNativePointer wstreamPtr,
    KInteropPointer ktitle,
    KInteropPointer kauthor,
    KInteropPointer ksubject,
    KInteropPointer kkeywords,
    KInteropPointer kcreator,
    KInteropPointer kproducer,
    KInt* kcreation,
    KInt* kmodified,
    KInteropPointer klang,
    KFloat rasterDPI,
    KBoolean pdfA,
    KInt encodingQuality,
    KInt compressionLevel
) {
    SkPDF::DateTime creation, modified;
    copyKIntArrayToDateTime(kcreation, &creation);
    copyKIntArrayToDateTime(kmodified, &modified);
    SkPDF::Metadata metadata;
    metadata.fTitle = skString(ktitle);
    metadata.fAuthor = skString(kauthor);
    metadata.fSubject = skString(ksubject);
    metadata.fKeywords = skString(kkeywords);
    metadata.fCreator = skString(kcreator);
    metadata.fProducer = skString(kproducer);
    metadata.fCreation = creation;
    metadata.fModified = modified;
    metadata.fLang = skString(klang);
    metadata.fRasterDPI = rasterDPI;
    metadata.fPDFA = pdfA;
    metadata.fEncodingQuality = encodingQuality;
    metadata.fCompressionLevel = static_cast<SkPDF::Metadata::CompressionLevel>(compressionLevel);
    SkWStream* wstream = reinterpret_cast<SkWStream*>((wstreamPtr));
    SkDocument* instance = SkPDF::MakeDocument(wstream, metadata).release();
    return reinterpret_cast<KNativePointer>(instance);
}
