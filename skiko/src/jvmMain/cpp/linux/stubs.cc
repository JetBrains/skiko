#include <stdio.h>
#include <stdlib.h>

extern "C" void __attribute__((weak)) __warn_memset_zero_len() {
    fprintf(stderr, "__warn_memset_zero_len called\n");
    abort();
}
