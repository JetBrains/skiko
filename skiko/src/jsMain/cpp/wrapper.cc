#include "GrBackendSurface.h"
#include "GrDirectContext.h"

void* init_surface() {
   return nullptr;
}

int main() {
    printf("Hello from WASM\n");
    //void* ctx = GrDirectContext::MakeGL().release();
    //printf("Context is %p\n");
    return 0;
}