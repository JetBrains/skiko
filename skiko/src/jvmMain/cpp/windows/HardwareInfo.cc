#ifdef SK_DIRECT3D

#include <locale>
#include <Windows.h>
#include <sstream>
#include <iostream>
#include "jni_helpers.h"

extern "C"
{
    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_RenderExceptionsHandlerKt_getNativeGraphicsAdapterInfo(
        JNIEnv *env, jobject object)
    {
        std::stringstream stream;
        std::string actualAdapter;

        for(int i = 0;; i++)
        {
            DISPLAY_DEVICE device = {sizeof(device), 0};
            BOOL result = EnumDisplayDevices(NULL, i, &device, EDD_GET_DEVICE_INTERFACE_NAME);
            if(!result) break;
            std::string currentAdapter = std::string(device.DeviceString);
            if (actualAdapter != currentAdapter) {
                actualAdapter = currentAdapter;
                stream << " - " << currentAdapter << std::endl;
            }
        }
        return env->NewStringUTF(stream.str().c_str());
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

#endif