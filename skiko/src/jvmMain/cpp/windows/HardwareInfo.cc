#include <Windows.h>
#include <sstream>
#include <iostream>
#include "jni_helpers.h"
#include <dxgi1_4.h>
#include <dxgi1_6.h>
#include "GpuChoose.h"

extern "C"
{
    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_HardwareInfoKt_getPreferredGpuName(
        JNIEnv *env, jobject object, jint priority
    ) {
        gr_cp<IDXGIAdapter1> hardwareAdapter;
        if (!defineHardwareAdapter(
            (DXGI_GPU_PREFERENCE) priority,
            &hardwareAdapter,
            [](IDXGIAdapter1* adapter) {
                return true;
            }
        )) {
            return 0;
        }

        DXGI_ADAPTER_DESC1 desc1;
        hardwareAdapter->GetDesc1(&desc1);
        std::wstring tmp(desc1.Description);
        std::string adapterName(tmp.begin(), tmp.end());
        return env->NewStringUTF(adapterName.c_str());
    }

    LONG GetStringRegKey(HKEY hKey, const std::wstring &strValueName, std::wstring &strValue)
    {
        strValue = L"Unknown";
        WCHAR szBuffer[512];
        DWORD dwBufferSize = sizeof(szBuffer);
        ULONG nError;
        nError = RegQueryValueExW(hKey, strValueName.c_str(), 0, NULL, (LPBYTE)szBuffer, &dwBufferSize);
        if (ERROR_SUCCESS == nError)
        {
            strValue = szBuffer;
        }
        return nError;
    }

    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_RenderExceptionsHandlerKt_getNativeCpuInfo(
        JNIEnv *env, jobject object)
    {
        auto path = L"HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0";
        auto key = L"ProcessorNameString";
        DWORD result;
        HKEY hKey;
        auto status = RegOpenKeyExW(HKEY_LOCAL_MACHINE, path, 0, KEY_READ, &hKey);
        if (ERROR_SUCCESS == status) {
            std::wstring strValue;
            
            GetStringRegKey(hKey, key, strValue);
            std::string result = std::string(strValue.begin(), strValue.end());
            return env->NewStringUTF(result.c_str());
        }
        return env->NewStringUTF("Can't get CPU info.");
    }
}
