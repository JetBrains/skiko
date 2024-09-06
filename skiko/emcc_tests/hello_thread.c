#include <pthread.h>
#include <emscripten.h>
#include <emscripten/console.h>

void *thread_main(void *arg)
{
    for (int i = 0; i < 909000000; i++) {}
	emscripten_out("hello from thread!");
//	emscripten_force_exit(0);
//	__builtin_trap();
}

int main()
{
    emscripten_out("hello from main!");
	pthread_t thread;
	if (pthread_create(&thread, NULL, thread_main, NULL)) {
	    emscripten_out("hello from main! - failed ");
	}
	emscripten_out("hello from main! - 2 ");
	emscripten_exit_with_live_runtime();
	__builtin_trap();
}