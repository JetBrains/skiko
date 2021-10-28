#include "common.h"
#include "src/utils/SkUTF.h"
#include "include/core/SkImageInfo.h"
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
}

skija::UtfIndicesConverter::UtfIndicesConverter(const char* chars8, size_t len8):
  fStart8(chars8),
  fPtr8(chars8),
  fEnd8(chars8 + len8),
  fPos16(0)
{}

skija::UtfIndicesConverter::UtfIndicesConverter(const SkString& str):
  skija::UtfIndicesConverter::UtfIndicesConverter(str.c_str(), str.size())
{}

size_t skija::UtfIndicesConverter::from16To8(uint32_t i16) {
    if (i16 >= fPos16) {
        // if new i16 >= last fPos16, continue from where we started
    } else {
        fPtr8 = fStart8;
        fPos16 = 0;
    }

    while (fPtr8 < fEnd8 && fPos16 < i16) {
        SkUnichar u = SkUTF::NextUTF8(&fPtr8, fEnd8);
        fPos16 += (uint32_t) SkUTF::ToUTF16(u);
    }

    return fPtr8 - fStart8;
}

uint32_t skija::UtfIndicesConverter::from8To16(size_t i8) {
    if (i8 >= (size_t) (fPtr8 - fStart8)) {
        // if new i8 >= last fPtr8, continue from where we started
    } else {
        fPtr8 = fStart8;
        fPos16 = 0;
    }

    while (fPtr8 < fEnd8 && (size_t) (fPtr8 - fStart8) < i8) {
        SkUnichar u = SkUTF::NextUTF8(&fPtr8, fEnd8);
        fPos16 += (uint32_t) SkUTF::ToUTF16(u);
    }

    return fPos16;
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
}
