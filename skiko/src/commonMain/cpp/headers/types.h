#ifndef SKIKO_TYPES_H
#define SKIKO_TYPES_H

#ifdef PROVIDE_JNI_TYPES

#include <stdint.h>
#include <stdbool.h>

typedef int8_t  jboolean;
typedef int8_t  jbyte;
typedef int16_t jchar;
typedef int16_t jshort;
typedef int32_t jint;
typedef float   jfloat;
typedef int64_t jlong;
typedef double  jdouble;

typedef jint jsize;

// These are not implemented yet,
// so just jave something as a fallback for the exception throwing stub.
typedef void* jstring;
typedef void* jobject;
typedef void* jobjectArray;
typedef void* jbyteArray;
typedef void* jshortArray;
typedef void* jintArray;
typedef void* jfloatArray;
typedef void* jlongArray;
typedef void* jdoubleArray;

typedef void* kref;

#else
#include <jni.h>
#endif /* PROVIDE_JNI_TYPES */

#endif /* SKIKO_TYPES_H */

