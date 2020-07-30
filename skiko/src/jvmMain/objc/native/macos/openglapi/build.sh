clang \
-I $JAVA_HOME/include ../openglapi/openglapi.m -shared -o libopenglapi.dylib \
-framework OpenGL \
-framework Foundation
