#ifdef SK_DIRECT3D

#include <locale>
#include <Windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"
#include "exceptions_handler.h"
#include "window_util.h"

#include "SkColorSpace.h"
#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "SkSurface.h"
#include "../common/interop.hh"

#include "d3d/GrD3DTypes.h"
#include <d3d12sdklayers.h>
#include "d3d/GrD3DBackendContext.h"
#include <d3d12.h>
#include <dxgi1_4.h>
#include <dxgi1_6.h>

class DirectXOffscreenDevice
{
public:
    GrD3DBackendContext backendContext;

    D3D12_RESOURCE_DESC textureDesc;
    D3D12_HEAP_PROPERTIES textureHeapProperties;
    ID3D12Resource* texture;

    D3D12_RESOURCE_DESC readbackBufferDesc;
    D3D12_HEAP_PROPERTIES readbackHeapProperties;
    ID3D12Resource* readbackBuffer;

    ~DirectXOffscreenDevice()
    {
        if (texture) {
            texture->Release();
        }

        if (readbackBuffer) {
            readbackBuffer->Release();
        }

        backendContext.fQueue.reset(nullptr);
        backendContext.fDevice.reset(nullptr);
        backendContext.fAdapter.reset(nullptr);
    }
};


extern "C"
{

    bool isAdapterSupported2(JNIEnv *env, jobject redrawer, IDXGIAdapter1 *hardwareAdapter) {
        DXGI_ADAPTER_DESC1 desc;
        hardwareAdapter->GetDesc1(&desc);
        if ((desc.Flags & DXGI_ADAPTER_FLAG_SOFTWARE) != 0) {
            return false;
        }

        std::wstring tmp(desc.Description);
        std::string name(tmp.begin(), tmp.end());
        jstring jname = env->NewStringUTF(name.c_str());

        static jclass cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/swing/Direct3DSwingRedrawer"));
        static jmethodID method = env->GetMethodID(cls, "isAdapterSupported", "(Ljava/lang/String;)Z");

        return env->CallBooleanMethod(redrawer, method, jname);
    }

    // TODO: extract common code with directXRedrawer
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_Direct3DSwingRedrawer_chooseAdapter(
            JNIEnv *env, jobject redrawer, jint adapterPriority) {
        gr_cp<IDXGIFactory4> deviceFactory;
        if (!SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&deviceFactory)))) {
            return 0;
        }

        gr_cp<IDXGIFactory6> factory6;
        if (!SUCCEEDED(deviceFactory->QueryInterface(IID_PPV_ARGS(&factory6)))) {
            return 0;
        }

        for (UINT adapterIndex = 0;; ++adapterIndex) {
            IDXGIAdapter1 *adapter = nullptr;
            if (!SUCCEEDED(factory6->EnumAdapterByGpuPreference(adapterIndex, (DXGI_GPU_PREFERENCE) adapterPriority, IID_PPV_ARGS(&adapter)))) {
                break;
            }
            if (
                SUCCEEDED(D3D12CreateDevice(adapter, D3D_FEATURE_LEVEL_11_0, _uuidof(ID3D12Device), nullptr)) &&
                isAdapterSupported2(env, redrawer, adapter)
            ) {
                return toJavaPointer(adapter);
            } else {
                adapter->Release();
            }
        }

        return 0;
    }

    // TODO: extract common code with directXRedrawer
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_Direct3DSwingRedrawer_createDirectXOffscreenDevice(
        JNIEnv *env, jobject redrawer, jlong adapterPtr) {
        gr_cp<IDXGIFactory4> deviceFactory;
        if (!SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&deviceFactory)))) {
            return 0;
        }
        if (adapterPtr == 0) {
            return 0;
        }
        gr_cp<IDXGIAdapter1> adapter((IDXGIAdapter1 *) adapterPtr);

        D3D_FEATURE_LEVEL maxSupportedFeatureLevel = D3D_FEATURE_LEVEL_12_0;
        D3D_FEATURE_LEVEL featureLevels[] = {
            // TODO add D3D_FEATURE_LEVEL_12_2
            D3D_FEATURE_LEVEL_12_1,
            D3D_FEATURE_LEVEL_12_0
        };

        for (int i = 0; i < _countof(featureLevels); i++) {
            if (SUCCEEDED(D3D12CreateDevice(adapter.get(), featureLevels[i], _uuidof(ID3D12Device), nullptr))) {
                maxSupportedFeatureLevel = featureLevels[i];
                break;
            }
        }

        gr_cp<ID3D12Device> device;
        if (!SUCCEEDED(D3D12CreateDevice(adapter.get(), maxSupportedFeatureLevel, IID_PPV_ARGS(&device)))) {
            return 0;
        }

        // Create the command queue
        gr_cp<ID3D12CommandQueue> queue;
        D3D12_COMMAND_QUEUE_DESC queueDesc = {};
        queueDesc.Flags = D3D12_COMMAND_QUEUE_FLAG_NONE;
        queueDesc.Type = D3D12_COMMAND_LIST_TYPE_DIRECT;

        if (!SUCCEEDED(device->CreateCommandQueue(&queueDesc, IID_PPV_ARGS(&queue)))) {
            return 0;
        }

        DirectXOffscreenDevice *d3dDevice = new DirectXOffscreenDevice();
        d3dDevice->texture = nullptr;
        d3dDevice->readbackBuffer = nullptr;
        d3dDevice->backendContext.fAdapter = adapter;
        d3dDevice->backendContext.fDevice = device;
        d3dDevice->backendContext.fQueue = queue;
        d3dDevice->backendContext.fProtectedContext = GrProtected::kNo;

        auto& textureDesc = d3dDevice->textureDesc;
        textureDesc.Dimension = D3D12_RESOURCE_DIMENSION_TEXTURE2D;
        textureDesc.Alignment = 0;
        textureDesc.Width = 0;
        textureDesc.Height = 0;
        textureDesc.DepthOrArraySize = 1;
        textureDesc.MipLevels = 1;
        textureDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
        textureDesc.SampleDesc.Count = 1;
        textureDesc.SampleDesc.Quality = 0;
        textureDesc.Layout = D3D12_TEXTURE_LAYOUT_UNKNOWN;
        textureDesc.Flags = D3D12_RESOURCE_FLAG_ALLOW_RENDER_TARGET;

        auto& readbackBufferDesc = d3dDevice->readbackBufferDesc;
        readbackBufferDesc.Dimension = D3D12_RESOURCE_DIMENSION_BUFFER;
        readbackBufferDesc.Alignment = 0;
        readbackBufferDesc.Width = 0;
        readbackBufferDesc.Height = 1;
        readbackBufferDesc.DepthOrArraySize = 1;
        readbackBufferDesc.MipLevels = 1;
        readbackBufferDesc.Format = DXGI_FORMAT_UNKNOWN;
        readbackBufferDesc.SampleDesc.Count = 1;
        readbackBufferDesc.SampleDesc.Quality = 0;
        readbackBufferDesc.Layout = D3D12_TEXTURE_LAYOUT_UNKNOWN;
        readbackBufferDesc.Flags = D3D12_RESOURCE_FLAG_NONE;

        auto& textureHeapProperties = d3dDevice->textureHeapProperties;
        textureHeapProperties.Type = D3D12_HEAP_TYPE_DEFAULT;
        textureHeapProperties.CPUPageProperty = D3D12_CPU_PAGE_PROPERTY_UNKNOWN;
        textureHeapProperties.MemoryPoolPreference = D3D12_MEMORY_POOL_UNKNOWN;
        textureHeapProperties.CreationNodeMask = 1;
        textureHeapProperties.VisibleNodeMask = 1;

        auto& readbackHeapProperties = d3dDevice->readbackHeapProperties;
        readbackHeapProperties.Type = D3D12_HEAP_TYPE_READBACK;
        readbackHeapProperties.CPUPageProperty = D3D12_CPU_PAGE_PROPERTY_UNKNOWN;
        readbackHeapProperties.MemoryPoolPreference = D3D12_MEMORY_POOL_UNKNOWN;
        readbackHeapProperties.CreationNodeMask = 1;
        readbackHeapProperties.VisibleNodeMask = 1;

        return toJavaPointer(d3dDevice);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_Direct3DSwingRedrawer_makeDirectXContext(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXOffscreenDevice *d3dDevice = fromJavaPointer<DirectXOffscreenDevice *>(devicePtr);
        GrD3DBackendContext backendContext = d3dDevice->backendContext;
        return toJavaPointer(GrDirectContext::MakeDirect3D(backendContext).release());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_swing_Direct3DSwingRedrawer_getRenderTargetTexture(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jint width, jint height) {
        DirectXOffscreenDevice *device = fromJavaPointer<DirectXOffscreenDevice *>(devicePtr);

        auto& textureDesc = device->textureDesc;

        if (textureDesc.Width != width || textureDesc.Height != height) {
            textureDesc.Width = width;
            textureDesc.Height = height;

            auto& readbackBufferDesc = device->readbackBufferDesc;
            readbackBufferDesc.Width = width * height * 4; // 4 bytes per pixel in R8G8B8A8_UNORM format

            if (device->texture) {
                //device->texture->Release();
            }

            if (device->readbackBuffer) {
                //device->readbackBuffer->Release();
            }

            device->backendContext.fDevice->CreateCommittedResource(&device->readbackHeapProperties, D3D12_HEAP_FLAG_NONE, &textureDesc, D3D12_RESOURCE_STATE_RENDER_TARGET, nullptr, IID_PPV_ARGS(&device->texture));
            //device->backendContext.fDevice->CreateCommittedResource(&device->readbackHeapProperties, D3D12_HEAP_FLAG_NONE, &readbackBufferDesc, D3D12_RESOURCE_STATE_COPY_DEST, nullptr, IID_PPV_ARGS(&device->readbackBuffer));
        }

        return toJavaPointer(device->texture);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_Direct3DSwingRedrawer_readPixels(
            JNIEnv *env, jobject redrawer, jlong devicePtr, jbyteArray byteArray) {
//        jbyte *bytesPtr = env->GetByteArrayElements(byteArray, nullptr);
//
//        exit(0);
//
//        env->ReleaseByteArrayElements(byteArray, bytesPtr, 0);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_swing_Direct3DSwingRedrawer_disposeDevice(
        JNIEnv *env, jobject redrawer, jlong devicePtr) {
        DirectXOffscreenDevice *device = fromJavaPointer<DirectXOffscreenDevice *>(devicePtr);
        delete device;
    }
} // extern "C"

#endif