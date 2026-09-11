#import <jawt.h>
#import <jawt_md.h>

extern "C" {
JNIEXPORT void JNICALL Java_org_jetbrains_skiko_AccessibilityKt_initializeCAccessible(
    JNIEnv *env, jobject obj, jobject accessible)
{
    @autoreleasepool {
        jclass accClass = env->FindClass("sun/lwawt/macosx/CAccessible");
        if (accClass == NULL) {
            return;
        }

        jmethodID getCAccessibleMethod = env->GetStaticMethodID(
            accClass, "getCAccessible",
            "(Ljavax/accessibility/Accessible;)Lsun/lwawt/macosx/CAccessible;"
        );
        if (getCAccessibleMethod == NULL) {
            return;
        }

        jobject result = env->CallStaticObjectMethod(accClass, getCAccessibleMethod, accessible);
        if (result == NULL) {
            return;
        }
    }
}

} // extern "C"