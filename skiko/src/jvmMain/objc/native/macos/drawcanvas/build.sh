clang \
-I $JAVA_HOME/include -I $JAVA_HOME/include/darwin \
../drawcanvas/drawcanvas.m -shared -o libdrawcanvas.dylib \
-L $JAVA_HOME/lib/ -ljawt \
-framework OpenGL \
-framework Cocoa \
-framework Foundation \
-framework QuartzCore \
-framework CoreGraphics
