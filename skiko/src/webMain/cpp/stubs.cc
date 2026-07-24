#include <semaphore.h>

extern "C" {

int sem_init(sem_t *sem, int pshared, unsigned int value) {
    return 0;
}

int sem_destroy(sem_t *sem) {
    return 0;
}

int sem_post(sem_t *sem) {
    return 0;
}

int sem_wait(sem_t *sem) {
    return 0;
}

int sem_trywait(sem_t *sem) {
    return 0;
}

int sem_getvalue(sem_t *sem, int *sval) {
    if (sval) *sval = 1;
    return 0;
}

}
