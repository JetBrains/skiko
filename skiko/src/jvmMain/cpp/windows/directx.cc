#ifdef SK_DIRECT3D
#include <stdexcept>
#include <locale>
#include <Windows.h>
#include <jawt_md.h>
#include "jni_helpers.h"

#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "SkSurface.h"

#include "d3d/GrD3DTypes.h"
#include <d3d12sdklayers.h>
#include "d3d/GrD3DBackendContext.h"
#include <d3d12.h>
#include <dxgi1_4.h>
#include <dxgi1_6.h>

UINT adapterIndex = 0;
extern "C"
{

bool defineAdapter(IDXGIFactory4 *pFactory, IDXGIAdapter1 **ppAdapter, int adapterIndex)
{
    *ppAdapter = nullptr;
    IDXGIAdapter1 *pAdapter = nullptr;
    if (DXGI_ERROR_NOT_FOUND == pFactory->EnumAdapters1(adapterIndex, &pAdapter))
    {
        return false;
    }
    if (SUCCEEDED(D3D12CreateDevice(pAdapter, D3D_FEATURE_LEVEL_11_0, _uuidof(ID3D12Device), nullptr)))
    {
        *ppAdapter = pAdapter;
        return true;
    }
    pAdapter->Release();
    return false;
}

JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_GraphicsApiKt_getNextDirectXAdapter(
    JNIEnv *env, jclass jclass, jint index)
{
    gr_cp<IDXGIFactory4> deviceFactory;
    if (!SUCCEEDED(CreateDXGIFactory1(IID_PPV_ARGS(&deviceFactory))))
    {
        return NULL;
    }
    gr_cp<IDXGIAdapter1> hardwareAdapter;
    if (!defineAdapter(deviceFactory.get(), &hardwareAdapter, index))
    {
        return NULL;
    }
    DXGI_ADAPTER_DESC1 desc;
    hardwareAdapter->GetDesc1(&desc);
    std::wstring currentAdapterName(desc.Description);

    const wchar_t *input = currentAdapterName.c_str();
    size_t size = (wcslen(input) + 1) * sizeof(wchar_t);
    char *buffer = new char[size];
    std::wcstombs(buffer, input, size);
    jstring result = env->NewStringUTF(buffer);
    delete buffer;

    return result;
}

}
#endif