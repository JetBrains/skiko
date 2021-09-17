#include <regex>
#include <iostream>
#include <cstdio>
#include <memory>
#include <stdexcept>
#include <string>
#include <array>
#include "jni_helpers.h"

extern "C"
{
    std::string process(const char* command) {
        std::array<char, 128> buffer;
        std::string result;
        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(command, "r"), pclose);
        if (!pipe) {
            return "";
        }
        while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
            result += buffer.data();
        }
        return result;
    }

    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_RenderExceptionsHandlerKt_getNativeGraphicsAdapterInfo(
        JNIEnv *env, jobject object)
    {
        std::stringstream stream;
        std::string searchSentence = "VGA compatible controller: ";
        stream << "lspci | grep " << "\"" << searchSentence << "\"";
        std::string gpu = process(stream.str().c_str());
        if (gpu == "") {
            return env->NewStringUTF("Can't get GPU info.");
        }
        size_t pos = gpu.find(searchSentence);
        gpu.erase(0, pos + searchSentence.length());
        stream.str(std::string());
        stream << " - " << gpu << std::endl;
        return env->NewStringUTF(stream.str().c_str());
    }

    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_RenderExceptionsHandlerKt_getNativeCpuInfo(
        JNIEnv *env, jobject object)
    {
        std::stringstream stream;
        std::string searchSentence = "Model name: ";
        stream << "lscpu | grep " << "\"" << searchSentence << "\"";
        std::string cpu = process(stream.str().c_str());
        if (cpu == "") {
            return env->NewStringUTF("Can't get CPU info.");
        }
        size_t pos = cpu.find(searchSentence);
        cpu.erase(0, pos + searchSentence.length());
        std::string result = std::regex_replace(cpu, std::regex("^ +"), "");
        result.erase(std::remove(result.begin(), result.end(), '\n'), result.end());
        return env->NewStringUTF(result.c_str());
    }
}