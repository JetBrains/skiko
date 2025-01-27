#ifdef SK_DIRECT3D

#include <locale>
#include <Windows.h>
#include <jawt_md.h>
#include <d3d12sdklayers.h>
#include <d3d12.h>
#include <dxgi1_4.h>
#include <dxgi1_6.h>
#include "jni_helpers.h"
#include "exceptions_handler.h"
#include "window_util.h"

#include "SkColorSpace.h"
#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "SkSurface.h"
#include "../common/interop.hh"

#include "d3d/GrD3DTypes.h"
#include "d3d/GrD3DBackendContext.h"

class DirectXOffscreenDevice
{
public:
    GrD3DBackendContext backendContext;

    ID3D12CommandAllocator* commandAllocator;
    ID3D12GraphicsCommandList* commandList;

    ID3D12Fence* fence;
    HANDLE fenceEvent;
    UINT64 fenceValue = 0;

    ~DirectXOffscreenDevice()
    {
        if (fenceEvent) {
            CloseHandle(fenceEvent);
        }

        if (fence) {
            fence->Release();
        }

        if (commandList) {
            commandList->Release();
        }

        if (commandAllocator) {
            commandAllocator->Release();
        }

        backendContext.fQueue.reset(nullptr);
        backendContext.fDevice.reset(nullptr);
        backendContext.fAdapter.reset(nullptr);
    }
};

UINT calculateRowPitch(UINT width) {
    UINT rowPitch = width * 4; // 4 bytes per pixel for DXGI_FORMAT_B8G8R8A8_UNORM
    rowPitch = (rowPitch + (D3D12_TEXTURE_DATA_PITCH_ALIGNMENT - 1)) & ~(D3D12_TEXTURE_DATA_PITCH_ALIGNMENT - 1);
    return rowPitch;
}

class DirectXOffScreenTexture {
public:
    int width;
    int height;
    ID3D12Resource* resource;
    ID3D12Resource* readbackBufferResource;
    
    DirectXOffScreenTexture(DirectXOffscreenDevice* device, int _width, int _height) {
        width = _width;
        height = _height;
        D3D12_RESOURCE_DESC textureDesc;
        textureDesc.Dimension = D3D12_RESOURCE_DIMENSION_TEXTURE2D;
        textureDesc.Alignment = 0;
        textureDesc.Width = _width;
        textureDesc.Height = _height;
        textureDesc.DepthOrArraySize = 1;
        textureDesc.MipLevels = 1;
        textureDesc.Format = DXGI_FORMAT_B8G8R8A8_UNORM;
        textureDesc.SampleDesc.Count = 1;
        textureDesc.SampleDesc.Quality = 0;
        textureDesc.Layout = D3D12_TEXTURE_LAYOUT_UNKNOWN;
        textureDesc.Flags = D3D12_RESOURCE_FLAG_ALLOW_RENDER_TARGET;

        D3D12_HEAP_PROPERTIES textureHeapProperties;
        textureHeapProperties.Type = D3D12_HEAP_TYPE_DEFAULT;
        textureHeapProperties.CPUPageProperty = D3D12_CPU_PAGE_PROPERTY_UNKNOWN;
        textureHeapProperties.MemoryPoolPreference = D3D12_MEMORY_POOL_UNKNOWN;
        textureHeapProperties.CreationNodeMask = 1;
        textureHeapProperties.VisibleNodeMask = 1;

        D3D12_RESOURCE_DESC readbackBufferDesc;
        readbackBufferDesc.Dimension = D3D12_RESOURCE_DIMENSION_BUFFER;
        readbackBufferDesc.Alignment = 0;
        readbackBufferDesc.Width = readbackBufferWidth(_width, _height);
        readbackBufferDesc.Height = 1;
        readbackBufferDesc.DepthOrArraySize = 1;
        readbackBufferDesc.MipLevels = 1;
        readbackBufferDesc.Format = DXGI_FORMAT_UNKNOWN;
        readbackBufferDesc.SampleDesc.Count = 1;
        readbackBufferDesc.SampleDesc.Quality = 0;
        readbackBufferDesc.Layout = D3D12_TEXTURE_LAYOUT_ROW_MAJOR;
        readbackBufferDesc.Flags = D3D12_RESOURCE_FLAG_NONE;

        D3D12_HEAP_PROPERTIES readbackHeapProperties;
        readbackHeapProperties.Type = D3D12_HEAP_TYPE_READBACK;
        readbackHeapProperties.CPUPageProperty = D3D12_CPU_PAGE_PROPERTY_UNKNOWN;
        readbackHeapProperties.MemoryPoolPreference = D3D12_MEMORY_POOL_UNKNOWN;
        readbackHeapProperties.CreationNodeMask = 1;
        readbackHeapProperties.VisibleNodeMask = 1;

        device->backendContext.fDevice->CreateCommittedResource(&textureHeapProperties, D3D12_HEAP_FLAG_NONE, &textureDesc, D3D12_RESOURCE_STATE_RENDER_TARGET, nullptr, IID_PPV_ARGS(&resource));
        device->backendContext.fDevice->CreateCommittedResource(&readbackHeapProperties, D3D12_HEAP_FLAG_NONE, &readbackBufferDesc, D3D12_RESOURCE_STATE_COPY_DEST, nullptr, IID_PPV_ARGS(&readbackBufferResource));
    }

    ~DirectXOffScreenTexture() {
        if (resource) {
            resource->Release();
        }

        if (readbackBufferResource) {
            readbackBufferResource->Release();
        }
    }

    int readbackBufferWidth() {
        return readbackBufferWidth(width, height);
    }
private: 
    static int readbackBufferWidth(int width, int height) {
         return calculateRowPitch(width) * height;
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

        static jclass cls = (jclass) env->NewGlobalRef(env->FindClass("org/jetbrains/skiko/graphicapi/InternalDirectXApi"));
        static jmethodID method = env->GetMethodID(cls, "isAdapterSupported", "(Ljava/lang/String;)Z");

        return env->CallBooleanMethod(redrawer, method, jname);
    }

    // TODO: extract common code with directXRedrawer
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_chooseAdapter(
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

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_createDirectXOffscreenDevice(
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

        ID3D12CommandAllocator* commandAllocator;
        if (!SUCCEEDED(device->CreateCommandAllocator(D3D12_COMMAND_LIST_TYPE_DIRECT, IID_PPV_ARGS(&commandAllocator)))) {
            return 0;
        }

        ID3D12GraphicsCommandList* commandList;
        if (!SUCCEEDED(device->CreateCommandList(0, D3D12_COMMAND_LIST_TYPE_DIRECT, commandAllocator, nullptr, IID_PPV_ARGS(&commandList)))) {
            return 0;
        }

        ID3D12Fence* fence;
        if (!SUCCEEDED(device->CreateFence(0, D3D12_FENCE_FLAG_NONE, IID_PPV_ARGS(&fence)))) {
            return 0;
        }

        HANDLE fenceEvent = CreateEventEx(nullptr, false, false, EVENT_ALL_ACCESS);
        if (!fenceEvent) {
            return 0;
        }

        DirectXOffscreenDevice *d3dDevice = new DirectXOffscreenDevice();
        d3dDevice->commandAllocator = commandAllocator;
        d3dDevice->commandList = commandList;
        d3dDevice->fence = fence;
        d3dDevice->fenceEvent = fenceEvent;
        d3dDevice->backendContext.fAdapter = adapter;
        d3dDevice->backendContext.fDevice = device;
        d3dDevice->backendContext.fQueue = queue;
        d3dDevice->backendContext.fProtectedContext = GrProtected::kNo;

        return toJavaPointer(d3dDevice);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_makeDirectXRenderTargetOffScreen(
            JNIEnv *env, jobject redrawer, jlong texturePtr) {
        DirectXOffScreenTexture *texture = fromJavaPointer<DirectXOffScreenTexture *>(texturePtr);
        ID3D12Resource* resource = texture->resource;

        GrD3DTextureResourceInfo texResInfo = {};
        texResInfo.fResource.retain(resource);
        texResInfo.fResourceState = D3D12_RESOURCE_STATE_COMMON;
        texResInfo.fFormat = DXGI_FORMAT_B8G8R8A8_UNORM;
        texResInfo.fSampleCount = 1;
        texResInfo.fLevelCount = 1;
        GrBackendRenderTarget* renderTarget = new GrBackendRenderTarget(texture->width, texture->height, texResInfo);
        return reinterpret_cast<jlong>(renderTarget);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_makeDirectXContext(
        JNIEnv *env, jobject redrawer, jlong devicePtr)
    {
        DirectXOffscreenDevice *d3dDevice = fromJavaPointer<DirectXOffscreenDevice *>(devicePtr);
        GrD3DBackendContext backendContext = d3dDevice->backendContext;
        return toJavaPointer(GrDirectContext::MakeDirect3D(backendContext).release());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_makeDirectXTexture(
        JNIEnv *env, jobject redrawer, jlong devicePtr, jlong oldTexturePtr, jint width, jint height) {
        DirectXOffscreenDevice *device = fromJavaPointer<DirectXOffscreenDevice *>(devicePtr);
        DirectXOffScreenTexture *oldTexture = fromJavaPointer<DirectXOffScreenTexture *>(oldTexturePtr);

        DirectXOffScreenTexture *texture;

        if (oldTexture == nullptr || oldTexture->width != width || oldTexture->height != height) {
            if (oldTexture != nullptr) {
                delete oldTexture;
            }
            texture = new DirectXOffScreenTexture(device, width, height);

            if (texture->resource == nullptr || texture->readbackBufferResource == nullptr) {
                delete texture;
                return 0;
            }
        } else {
            texture = oldTexture;
        }

        return toJavaPointer(texture);
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_disposeDirectXTexture(
        JNIEnv *env, jobject redrawer, jlong texturePtr) {
        DirectXOffScreenTexture *texture = fromJavaPointer<DirectXOffScreenTexture *>(texturePtr);
        delete texture;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_waitForCompletion(
            JNIEnv *env, jobject redrawer, jlong devicePtr, jlong texturePtr) {

        DirectXOffscreenDevice *device = fromJavaPointer<DirectXOffscreenDevice *>(devicePtr);

        DirectXOffScreenTexture *texture = fromJavaPointer<DirectXOffScreenTexture *>(texturePtr);

        auto commandAllocator = device->commandAllocator;
        auto commandList = device->commandList;

        commandAllocator->Reset();
        commandList->Reset(commandAllocator, nullptr);

        D3D12_RESOURCE_BARRIER textureResourceBarrier;
        textureResourceBarrier.Type = D3D12_RESOURCE_BARRIER_TYPE_TRANSITION;
        textureResourceBarrier.Transition.pResource = texture->resource;
        textureResourceBarrier.Transition.StateBefore = D3D12_RESOURCE_STATE_RENDER_TARGET;
        textureResourceBarrier.Transition.StateAfter = D3D12_RESOURCE_STATE_COPY_SOURCE;
        textureResourceBarrier.Transition.Subresource = D3D12_RESOURCE_BARRIER_ALL_SUBRESOURCES ;
        textureResourceBarrier.Flags = D3D12_RESOURCE_BARRIER_FLAG_NONE;

        commandList->ResourceBarrier(1, &textureResourceBarrier);

        D3D12_TEXTURE_COPY_LOCATION src = {};
        src.pResource = texture->resource;
        src.Type = D3D12_TEXTURE_COPY_TYPE_SUBRESOURCE_INDEX;
        src.SubresourceIndex = 0;

        D3D12_TEXTURE_COPY_LOCATION dst = {};
        dst.pResource = texture->readbackBufferResource;
        dst.Type = D3D12_TEXTURE_COPY_TYPE_PLACED_FOOTPRINT;
        dst.PlacedFootprint.Offset = 0;
        dst.PlacedFootprint.Footprint.Format = DXGI_FORMAT_B8G8R8A8_UNORM;
        dst.PlacedFootprint.Footprint.Width = texture->width;
        dst.PlacedFootprint.Footprint.Height = texture->height;
        dst.PlacedFootprint.Footprint.Depth = 1;
        dst.PlacedFootprint.Footprint.RowPitch = calculateRowPitch(texture->width);

        D3D12_BOX srcBox = {0, 0, 0, texture->width, texture->height, 1};

        commandList->CopyTextureRegion(&dst, 0, 0, 0, &src, &srcBox);

        commandList->Close();

        ID3D12CommandList* commandLists[] = { commandList };
        device->backendContext.fQueue->ExecuteCommandLists(_countof(commandLists), commandLists);

        // Wait for the command list to finish executing; the readback buffer will be ready to read
        auto fence = device->fence;
        auto fenceEvent = device->fenceEvent;
        auto& fenceValue = device->fenceValue;

        fenceValue += 1;
        device->backendContext.fQueue->Signal(fence, fenceValue);

        if (fence->GetCompletedValue() < fenceValue) {
            fence->SetEventOnCompletion(fenceValue, fenceEvent);
            WaitForSingleObject(fenceEvent, INFINITE);
        }
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_readPixels(
            JNIEnv *env, jobject redrawer, jlong texturePtr, jbyteArray byteArray) {
        jbyte *bytesPtr = env->GetByteArrayElements(byteArray, nullptr);

        DirectXOffScreenTexture *texture = fromJavaPointer<DirectXOffScreenTexture *>(texturePtr);

        auto rangeLength = texture->readbackBufferWidth();
        D3D12_RANGE readbackBufferRange{ 0, rangeLength };

        /*
         * TODO: memcpy from unaligned texture is not supported, line by line copy is very slow,
         *       write compute shader to copy texture to readback buffer with no RowPitch padding
         *       to support arbitary texture size
         */
        if (rangeLength != texture->width * texture->height * 4) {
            return false;
        }

        void *readbackBufferBytesPtr = nullptr;
        texture->readbackBufferResource->Map(
            0,
            &readbackBufferRange,
            &readbackBufferBytesPtr
        );

        if (!readbackBufferBytesPtr) {
            // Couldn't map readback buffer
            return false;
        }

        memcpy(bytesPtr, readbackBufferBytesPtr, rangeLength);

        D3D12_RANGE emptyRange{ 0, 0 };
        texture->readbackBufferResource->Unmap(0, &emptyRange);

        env->ReleaseByteArrayElements(byteArray, bytesPtr, 0);

        return true;
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_disposeDevice(
        JNIEnv *env, jobject redrawer, jlong devicePtr) {
        DirectXOffscreenDevice *device = fromJavaPointer<DirectXOffscreenDevice *>(devicePtr);
        delete device;
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_graphicapi_InternalDirectXApi_getTextureAlignment(
            JNIEnv *env, jobject redrawer) {
        return D3D12_TEXTURE_DATA_PITCH_ALIGNMENT;
    }

} // extern "C"

#endif // SK_DIRECT3D