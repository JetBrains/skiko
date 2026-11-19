package org.jetbrains.skiko.tests

// Implemented in the test helpers of the core library (src/jvmTest/cpp), identifies its copy.
external fun _nCoreInstance(): Long

// Implemented in the test helpers of skiko-platform (src/jvmTest/cpp), asks the core library it is linked to.
external fun _nCoreInstanceSeenByPlatform(): Long
