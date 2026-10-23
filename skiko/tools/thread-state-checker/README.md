# Native thread-state checker

The checker finds native bindings that call blocking APIs without `ForceNativeThreadState`. It also reports marked bindings and bindings that made no blocking call. It instruments Skiko C++ and injects a macOS library into native tests. Skia stays prebuilt and uninstrumented.

Run from `skiko/`:

```sh
./gradlew -Pskiko.native.enabled=true -Pskiko.native.mac.enabled=true -Pskiko.test.threadStateChecker.enabled=true :macosArm64Test :skiko-graphite:macosArm64Test :skiko-skottie:macosArm64Test
```

The checker covers the macOS native tests of the main module, `skiko-graphite` and `skiko-skottie`. Each module has its own bindings files and log under `build/thread-state-checker/<target>/` and XML report under `build/test-results/`. The XML report includes one test case per binding the tests called, matched to its external Kotlin declaration. Bindings come from a compiler plugin that reads every external function with a symbol name, including direct `SymbolName` annotations and resolved `ExternalSymbolName` annotations. It collects symbol names, `ForceNativeThreadState` marks and module-relative source paths from Kotlin IR during each macOS native compilation. The checker matches these declarations to the test executable with `nm`. Conflicting marks for the same symbol fail the build. Bindings the tests never called produce no test case.

Read `build/test-results/<target>ThreadStateChecker/TEST-org.jetbrains.skiko.ThreadStateChecker.xml`, where `<target>` is `macosArm64` or `macosX64`. The JUnit XML report includes call counts and one sample stack per binding and blocking API. The task fails on `ThreadStateMismatch` when an unmarked binding makes a blocking call or `IncompleteData` when collection drops records or the log is incomplete or malformed. A complete log ends with `# end <number of records written>` after all preceding record lines have been flushed; the count includes the header and dropped counter.

The property also enables the checker for `macosX64Test`. Each target keeps compilation bindings in `<compilation>-bindings.txt`, executable bindings in `bindings.txt`, logs in `log.txt` and its injected library in `build/thread-state-checker/<target>/`. Each target has its own XML report.

The checker intercepts `pthread_mutex_lock`, `pthread_cond_wait`, `pthread_cond_timedwait`, `pthread_rwlock_rdlock`, `pthread_rwlock_wrlock`, `pthread_join`, `os_unfair_lock_lock`, `os_unfair_lock_lock_with_options`, `dispatch_semaphore_wait`, `dispatch_sync`, `dispatch_group_wait`, `semaphore_wait`, `nanosleep`, `open`, `read`, `pread`, `write`, `pwrite`, `mmap`, `CFRelease`, `objc_release` and `objc_loadWeakRetained`. It also intercepts the `$NOCANCEL` variants of `open`, `read`, `pread`, `write` and `pwrite`, including calls from libc stdio.

Uncontended Skia `SkMutex` and `SkOnce` paths use inline atomics and are invisible. The checker sees only bindings the tests call and calls that reach an intercepted API. It sees calls, not whether they waited. A marked binding can need its annotation for long CPU work or a callback into Kotlin even when it makes no blocking call.
