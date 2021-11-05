#include "common.h"
#include "src/utils/SkUTF.h"
#include "include/core/SkImageInfo.h"
#include "TextStyle.h"
#include <stdio.h>

KLong packTwoInts(int32_t a, int32_t b) {
    return (uint64_t (a) << 32) | b;
}

KLong packIPoint(SkIPoint p) {
    return packTwoInts(p.fX, p.fY);
}

KLong packISize(SkISize p) {
    return packTwoInts(p.fWidth, p.fHeight);
}

namespace skija {

    namespace SamplingMode {
        SkSamplingOptions unpack(KLong val) {
            if (0x8000000000000000 & val) {
                val = val & 0x7FFFFFFFFFFFFFFF;
                float* ptr = reinterpret_cast<float*>(&val);
                return SkSamplingOptions(SkCubicResampler {ptr[1], ptr[0]});
            } else {
                int32_t filter = (int32_t) ((val >> 32) & 0xFFFFFFFF);
                int32_t mipmap = (int32_t) (val & 0xFFFFFFFF);
                return SkSamplingOptions(static_cast<SkFilterMode>(filter), static_cast<SkMipmapMode>(mipmap));
            }
        }

        SkSamplingOptions unpackFrom2Ints(KInt samplingModeVal1, KInt samplingModeVal2) {
            if (0x80000000 & samplingModeVal1) {
                uint64_t val1 = samplingModeVal1 & 0x7FFFFFFF;
                uint64_t val = (val1 << 32) | samplingModeVal2;

                float* ptr = reinterpret_cast<float*>(&val);
                return SkSamplingOptions(SkCubicResampler {ptr[1], ptr[0]});
            } else {
                int32_t filter = samplingModeVal1;
                int32_t mipmap = samplingModeVal2;

                return SkSamplingOptions(static_cast<SkFilterMode>(filter), static_cast<SkMipmapMode>(mipmap));
            }
        }
    }

    namespace FontFeature {
        std::vector<SkShaper::Feature> fromIntArray(KInt* array, KInt featuresLen) {
            std::vector<SkShaper::Feature> features(featuresLen);
            for (int i = 0; i < featuresLen; ++i) {
                int j = i * 4;
                features[i] = {
                    static_cast<SkFourByteTag>(array[j]),
                    static_cast<uint32_t>(array[j + 1]),
                    static_cast<size_t>(array[j + 2]),
                    static_cast<size_t>(array[j + 3])
                };
            }
            return features;
        }

        void writeToIntArray(std::vector<skia::textlayout::FontFeature> features, int* resultArr) {
            for (int i = 0; i < features.size(); ++i) {
                int j = i * 2;
                resultArr[j] = skija::FontFeature::FourByteTag::fromString(features[i].fName);
                resultArr[j + 1] = features[i].fValue;;
            }
        }

        namespace FourByteTag {
            int fromString(SkString str) {
                int code1 = (int)str[0];
                int code2 = (int)str[1];
                int code3 = (int)str[2];
                int code4 = (int)str[3];
                return (code1 & 0xFF << 24) | (code2 & 0xFF << 16) | (code3 & 0xFF << 8) | (code4 & 0xFF);
            }
        }
    }

    namespace shaper {
        namespace ShapingOptions {
            std::vector<SkShaper::Feature> getFeaturesFromIntsArray(KInt* featuresArray, KInt featuresLen) {
                return skija::FontFeature::fromIntArray(featuresArray, featuresLen);
            }
        }

        std::shared_ptr<UBreakIterator> graphemeBreakIterator(SkString& text) {
            UErrorCode status = U_ZERO_ERROR;
            ICUUText utext(utext_openUTF8(nullptr, text.c_str(), text.size(), &status));
            if (U_FAILURE(status)) {
                SkDEBUGF("utext_openUTF8 error: %s", u_errorName(status));
                return nullptr;
            }

            std::shared_ptr<UBreakIterator> graphemeIter(
                ubrk_open(static_cast<UBreakIteratorType>(UBreakIteratorType::UBRK_CHARACTER), uloc_getDefault(), nullptr, 0, &status),
                [](UBreakIterator* p) { ubrk_close(p); }
            );
            if (U_FAILURE(status)) {
                SkDEBUGF("ubrk_open error: %s", u_errorName(status));
                return nullptr;
            }

            ubrk_setUText(graphemeIter.get(), utext.get(), &status);
            if (U_FAILURE(status)) {
                SkDEBUGF("ubrk_setUText error: %s", u_errorName(status));
                return nullptr;
            }

            return graphemeIter;
        }
    }
}

void TODO(const char* message) {
    printf("TODO: %s\n", message);
    fflush(stdout);
    abort();
}

std::unique_ptr<SkMatrix> skMatrix(KFloat* matrixArray) {
    if (matrixArray == nullptr)
        return std::unique_ptr<SkMatrix>(nullptr);
    else {
        KFloat* m = matrixArray;
        SkMatrix* ptr = new SkMatrix();
        ptr->setAll(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
        return std::unique_ptr<SkMatrix>(ptr);
    }
}

std::unique_ptr<SkM44> skM44(KFloat* matrixArray) {
    if (matrixArray == nullptr)
        return std::unique_ptr<SkM44>(nullptr);
    else {
        KFloat* m = matrixArray;
        SkM44* ptr = new SkM44(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10], m[11], m[12], m[13], m[14], m[15]);
        return std::unique_ptr<SkM44>(ptr);
    }
}

SkString skString(KInteropPointer s) {
    char* str = reinterpret_cast<char *>(s);
    if (str == nullptr) {
        return SkString();
    } else {
        return SkString(str);
    }
}

std::vector<SkString> skStringVector(KInteropPointerArray arr, KInt len) {
    if (arr == nullptr) {
        return std::vector<SkString>(0);
    } else {
        std::vector<SkString> res(len);
        char** strings = reinterpret_cast<char**>(arr);
        for (KInt i = 0; i < len; ++i) {
            res[i] = skString(strings[i]);
        }
        return res;
    }
}

namespace skija {
    namespace Rect {
        void copyToInterop(const SkRect& rect, KInteropPointer pointer) {
            float* ltrb = reinterpret_cast<float*>(pointer);
            if (ltrb != nullptr) {
                ltrb[0] = rect.left();
                ltrb[1] = rect.top();
                ltrb[2] = rect.right();
                ltrb[3] = rect.bottom();
            }
        }
    }
    namespace RRect {
        void copyToInterop(const SkRRect& rect, KInteropPointer pointer) {
            float* ltrb = reinterpret_cast<float*>(pointer);
            if (ltrb != nullptr) {
                ltrb[0] = rect.rect().left();
                ltrb[1] = rect.rect().top();
                ltrb[2] = rect.rect().right();
                ltrb[3] = rect.rect().bottom();

                ltrb[4] = rect.radii(SkRRect::kUpperLeft_Corner).x();
                ltrb[5] = rect.radii(SkRRect::kUpperLeft_Corner).y();
                ltrb[6] = rect.radii(SkRRect::kUpperRight_Corner).x();
                ltrb[7] = rect.radii(SkRRect::kUpperRight_Corner).y();
                ltrb[8] = rect.radii(SkRRect::kLowerRight_Corner).x();
                ltrb[9] = rect.radii(SkRRect::kLowerRight_Corner).y();
                ltrb[10] = rect.radii(SkRRect::kLowerLeft_Corner).x();
                ltrb[11] = rect.radii(SkRRect::kLowerLeft_Corner).y();
            }
        }
    }

    namespace Point {
        void copyToInterop(const SkPoint& point, KInteropPointer pointer) {
            float* xy = reinterpret_cast<float*>(pointer);
            if (xy != nullptr) {
                xy[0] = point.x();
                xy[1] = point.y();
            }
        }
    }

    namespace RRect {
        SkRRect toSkRRect(KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* radii, KInt radiiSize) {
            SkRect rect {left, top, right, bottom};
            SkRRect rrect = SkRRect::MakeEmpty();
            switch (radiiSize) {
                case 1:
                    rrect.setRectXY(rect, radii[0], radii[0]);
                    break;
                case 2:
                    rrect.setRectXY(rect, radii[0], radii[1]);
                    break;
                case 4: {
                    SkVector vradii[4] = {{radii[0], radii[0]}, {radii[1], radii[1]}, {radii[2], radii[2]}, {radii[3], radii[3]}};
                    rrect.setRectRadii(rect, vradii);
                    break;
                }
                case 8: {
                    SkVector vradii[4] = {{radii[0], radii[1]}, {radii[2], radii[3]}, {radii[4], radii[5]}, {radii[6], radii[7]}};
                    rrect.setRectRadii(rect, vradii);
                    break;
                }
            }
            return rrect;
        }
    }
    namespace FontStyle {
        SkFontStyle fromKotlin(KInt style) {
            return SkFontStyle(style & 0xFFFF, (style >> 16) & 0xFF, static_cast<SkFontStyle::Slant>((style >> 24) & 0xFF));
        }

        KInt toKotlin(const SkFontStyle& fs) {
            return (static_cast<int>(fs.slant()) << 24)| (fs.width() << 16) | fs.weight();
        }
    }

    namespace ImageInfo {
        void writeImageInfoForInterop(SkImageInfo imageInfo, KInt* imageInfoResult, KNativePointer* colorSpacePtrsArray) {
            imageInfoResult[0] = imageInfo.width();
            imageInfoResult[1] = imageInfo.height();
            imageInfoResult[2] = static_cast<int>(imageInfo.colorType());
            imageInfoResult[3] = static_cast<int>(imageInfo.alphaType());

            colorSpacePtrsArray[0] = imageInfo.refColorSpace().release();
        }
    }

    namespace SurfaceProps {
        std::unique_ptr<SkSurfaceProps> toSkSurfaceProps(KInt* surfacePropsInts) {
            if (surfacePropsInts == nullptr) {
                return std::unique_ptr<SkSurfaceProps>(nullptr);
            }
            int flags = surfacePropsInts[0];
            SkPixelGeometry geom = static_cast<SkPixelGeometry>(surfacePropsInts[1]);
            return std::make_unique<SkSurfaceProps>(flags, geom);
        }
    }

    namespace IRect {
        std::unique_ptr<SkIRect> toSkIRect(KInt* rectInts) {
            if (rectInts == nullptr)
                return std::unique_ptr<SkIRect>(nullptr);
            else {
                return std::unique_ptr<SkIRect>(new SkIRect{
                    rectInts[0], rectInts[1], rectInts[2], rectInts[3]
                });
            }
        }
    }

    namespace AnimationFrameInfo {
        static void copyToInteropAtIndex(const SkCodec::FrameInfo& info, KInt* repr, size_t index) {
            repr += (index * 11);
            repr[0] = info.fRequiredFrame;
            repr[1] = info.fDuration;
            repr[2] = static_cast<KInt>(info.fFullyReceived);
            repr[3] = static_cast<KInt>(info.fAlphaType);
            repr[4] = static_cast<KInt>(info.fHasAlphaWithinBounds);
            repr[5] = static_cast<KInt>(info.fDisposalMethod);
            repr[6] = static_cast<KInt>(info.fBlend);
            repr[7] = info.fFrameRect.left();
            repr[8] = info.fFrameRect.top();
            repr[9] = info.fFrameRect.right();
            repr[10] = info.fFrameRect.bottom();
        }

        void copyToInterop(const SkCodec::FrameInfo& info, KInteropPointer dst) {
            KInt* repr = reinterpret_cast<KInt*>(dst);
            copyToInteropAtIndex(info, repr, 0);
        }

        void copyToInterop(const std::vector<SkCodec::FrameInfo>& infos, KInteropPointer dst) {
            KInt* repr = reinterpret_cast<KInt*>(dst);
            size_t i = 0;
            for (const auto& info : infos) {
                copyToInteropAtIndex(info, repr, i++);
            }
        }

    }

    namespace svg {
        namespace SVGLength {
            void copyToInterop(const SkSVGLength& length, KInteropPointer dst) {
                KInt* result = reinterpret_cast<KInt*>(dst);
                result[0] = rawBits(length.value());
                result[1] = static_cast<KInt>(length.unit());
            }
        }

        namespace SVGPreserveAspectRatio {
            void copyToInterop(const SkSVGPreserveAspectRatio& aspectRatio, KInteropPointer dst) {
                KInt* result = reinterpret_cast<KInt*>(dst);
                result[0] = static_cast<KInt>(aspectRatio.fAlign);
                result[1] = static_cast<KInt>(aspectRatio.fScale);
            }
        }
    }
}
