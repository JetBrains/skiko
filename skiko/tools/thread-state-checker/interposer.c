// Attributes blocking calls to instrumented native bindings on macOS.

#include <CoreFoundation/CoreFoundation.h>
#include <dispatch/dispatch.h>
#include <execinfo.h>
#include <fcntl.h>
#include <mach-o/dyld.h>
#include <mach-o/loader.h>
#include <string.h>
#include <crt_externs.h>
#include <mach/semaphore.h>
#include <os/lock.h>
#include <pthread.h>
#include <stdarg.h>
#include <stdatomic.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <time.h>
#include <unistd.h>

extern void objc_release(void *obj);
extern void *objc_loadWeakRetained(void **location);
extern void os_unfair_lock_lock_with_options(os_unfair_lock_t lock, uint32_t options);
extern int open_nocancel(const char *path, int flags, ...) __asm("_open$NOCANCEL");
extern ssize_t read_nocancel(int fd, void *buf, size_t n) __asm("_read$NOCANCEL");
extern ssize_t pread_nocancel(int fd, void *buf, size_t n, off_t offset) __asm("_pread$NOCANCEL");
extern ssize_t write_nocancel(int fd, const void *buf, size_t n) __asm("_write$NOCANCEL");
extern ssize_t pwrite_nocancel(int fd, const void *buf, size_t n, off_t offset) __asm("_pwrite$NOCANCEL");

enum Sink {
    S_MUTEX_LOCK, S_COND_WAIT, S_COND_TIMEDWAIT, S_RWLOCK_RDLOCK, S_RWLOCK_WRLOCK, S_UNFAIR_LOCK,
    S_DISPATCH_SEMAPHORE_WAIT, S_SEMAPHORE_WAIT, S_OPEN, S_READ, S_MMAP, S_CFRELEASE,
    S_OBJC_RELEASE, S_OBJC_LOAD_WEAK_RETAINED, S_PREAD, S_WRITE, S_PWRITE,
    S_OPEN_NOCANCEL, S_READ_NOCANCEL, S_PREAD_NOCANCEL, S_WRITE_NOCANCEL, S_PWRITE_NOCANCEL,
    S_PTHREAD_JOIN, S_NANOSLEEP, S_DISPATCH_SYNC, S_DISPATCH_GROUP_WAIT, S_UNFAIR_LOCK_WITH_OPTIONS,
    S_COUNT
};

static const char *const SINK_NAMES[S_COUNT] = {
    "pthread_mutex_lock", "pthread_cond_wait", "pthread_cond_timedwait", "pthread_rwlock_rdlock",
    "pthread_rwlock_wrlock", "os_unfair_lock_lock", "dispatch_semaphore_wait", "semaphore_wait",
    "open", "read", "mmap", "CFRelease", "objc_release", "objc_loadWeakRetained",
    "pread", "write", "pwrite", "open$NOCANCEL", "read$NOCANCEL", "pread$NOCANCEL",
    "write$NOCANCEL", "pwrite$NOCANCEL", "pthread_join", "nanosleep", "dispatch_sync",
    "dispatch_group_wait", "os_unfair_lock_lock_with_options",
};

#define MAX_FRAMES 64
#define MAX_DEPTH 256
#define MAX_THREADS 1024

typedef struct {
    _Atomic uint64_t count;
    _Atomic int sample;  // 0: empty, 1: writing, 2: published
    int nframes;
    void *frames[MAX_FRAMES];
} Entry;

typedef struct {
    uintptr_t address;
    char *name;
    _Atomic int called;
} Binding;

static Binding *bindings;
static Entry *entries;
static size_t binding_count;
static _Atomic uint64_t dropped;
static _Atomic int ready;
static _Atomic int stopping;
static _Atomic unsigned active_recorders;
static pthread_key_t guard_key;
static uintptr_t slide;
static const struct mach_header_64 *main_header;
typedef struct {
    _Atomic int used;
    size_t stack[MAX_DEPTH];
    unsigned depth;
    unsigned overflow;
} ThreadState;

static ThreadState threads[MAX_THREADS];
static pthread_key_t state_key;

static void release_thread(void *value) {
    ThreadState *state = value;
    state->depth = 0;
    state->overflow = 0;
    atomic_store_explicit(&state->used, 0, memory_order_release);
}

static ThreadState *thread_state(int create) {
    ThreadState *state = pthread_getspecific(state_key);
    if (state || !create) return state;
    for (unsigned i = 0; i < MAX_THREADS; i++) {
        int expected = 0;
        if (atomic_compare_exchange_strong_explicit(&threads[i].used, &expected, 1,
                memory_order_acquire, memory_order_relaxed)) {
            if (pthread_setspecific(state_key, &threads[i])) {
                atomic_store_explicit(&threads[i].used, 0, memory_order_release);
                break;
            }
            return &threads[i];
        }
    }
    atomic_fetch_add(&dropped, 1);
    return NULL;
}

static int begin(void) {
    if (!atomic_load_explicit(&ready, memory_order_acquire) || atomic_load(&stopping) ||
        pthread_getspecific(guard_key)) return 0;
    pthread_setspecific(guard_key, (void *)1);
    atomic_fetch_add(&active_recorders, 1);
    if (atomic_load(&stopping)) {
        atomic_fetch_sub(&active_recorders, 1);
        pthread_setspecific(guard_key, NULL);
        return 0;
    }
    return 1;
}

static void end(void) {
    atomic_fetch_sub(&active_recorders, 1);
    pthread_setspecific(guard_key, NULL);
}

static size_t find_binding(void *function) {
    uintptr_t address = (uintptr_t)function - slide;
    size_t lo = 0, hi = binding_count;
    while (lo < hi) {
        size_t mid = lo + (hi - lo) / 2;
        if (bindings[mid].address < address) lo = mid + 1;
        else hi = mid;
    }
    return lo < binding_count && bindings[lo].address == address ? lo : binding_count;
}

__attribute__((no_instrument_function))
void __cyg_profile_func_enter(void *function, void *caller) {
    (void)caller;
    if (!begin()) return;
    size_t index = find_binding(function);
    if (index != binding_count) {
        atomic_store_explicit(&bindings[index].called, 1, memory_order_relaxed);
        ThreadState *state = thread_state(1);
        if (state) {
            if (state->depth == MAX_DEPTH) {
                state->overflow++;
                atomic_fetch_add(&dropped, 1);
            } else {
                state->stack[state->depth++] = index;
            }
        }
    }
    end();
}

__attribute__((no_instrument_function))
void __cyg_profile_func_exit(void *function, void *caller) {
    (void)caller;
    if (!begin()) return;
    size_t index = find_binding(function);
    if (index != binding_count) {
        ThreadState *state = thread_state(0);
        if (state) {
            if (state->overflow) state->overflow--;
            else if (state->depth && state->stack[state->depth - 1] == index) state->depth--;
            else atomic_fetch_add(&dropped, 1);
        }
    }
    end();
}

__attribute__((noinline)) static void record(int sink) {
    if (!begin()) return;
    ThreadState *state = thread_state(0);
    if (state && state->overflow) {
        atomic_fetch_add(&dropped, 1);
    } else if (state && state->depth) {
        Entry *e = &entries[state->stack[state->depth - 1] * S_COUNT + sink];
        atomic_fetch_add_explicit(&e->count, 1, memory_order_relaxed);
        int expected = 0;
        if (atomic_compare_exchange_strong(&e->sample, &expected, 1)) {
            void *frames[MAX_FRAMES + 2];
            int n = backtrace(frames, MAX_FRAMES + 2);
            e->nframes = n > 2 ? n - 2 : 0;
            for (int i = 0; i < e->nframes; i++) e->frames[i] = frames[i + 2];
            atomic_store_explicit(&e->sample, 2, memory_order_release);
        }
    }
    end();
}

static int compare_bindings(const void *a, const void *b) {
    uintptr_t x = ((const Binding *)a)->address, y = ((const Binding *)b)->address;
    return (x > y) - (x < y);
}

__attribute__((constructor)) static void init(void) {
    if (pthread_key_create(&guard_key, NULL) || pthread_key_create(&state_key, release_thread)) return;
    main_header = (const struct mach_header_64 *)_NSGetMachExecuteHeader();
    const struct load_command *command = (const void *)(main_header + 1);
    int found_text = 0;
    for (uint32_t i = 0; i < main_header->ncmds; i++) {
        if (command->cmd == LC_SEGMENT_64) {
            const struct segment_command_64 *segment = (const void *)command;
            if (!strcmp(segment->segname, "__TEXT")) {
                slide = (uintptr_t)main_header - segment->vmaddr;
                found_text = 1;
                break;
            }
        }
        command = (const void *)((const char *)command + command->cmdsize);
    }
    const char *path = getenv("SKIKO_THREAD_STATE_CHECKER_BINDINGS");
    if (!path || !found_text) return;
    FILE *f = fopen(path, "r");
    if (!f) return;
    size_t capacity = 0;
    char *line = NULL;
    size_t line_capacity = 0;
    int failed = 0;
    while (getline(&line, &line_capacity, f) >= 0) {
        unsigned long long address;
        char name[1024];
        if (sscanf(line, "%llx %1023s", &address, name) != 2) continue;
        if (binding_count == capacity) {
            size_t next = capacity ? capacity * 2 : 1024;
            Binding *grown = realloc(bindings, next * sizeof(Binding));
            if (!grown) { failed = 1; break; }
            bindings = grown;
            capacity = next;
        }
        char *copy = strdup(name);
        if (!copy) { failed = 1; break; }
        bindings[binding_count++] = (Binding){ .address = (uintptr_t)address, .name = copy };
    }
    free(line);
    fclose(f);
    if (failed || !binding_count) return;
    qsort(bindings, binding_count, sizeof(Binding), compare_bindings);
    entries = calloc(binding_count * S_COUNT, sizeof(Entry));
    if (!entries) return;
    void *warmup[MAX_FRAMES];
    backtrace(warmup, MAX_FRAMES);
    atomic_store_explicit(&ready, 1, memory_order_release);
}

__attribute__((destructor)) static void dump(void) {
    atomic_store(&stopping, 1);
    struct timespec start, now;
    clock_gettime(CLOCK_MONOTONIC, &start);
    while (atomic_load(&active_recorders)) {
        clock_gettime(CLOCK_MONOTONIC, &now);
        int64_t elapsed = (int64_t)(now.tv_sec - start.tv_sec) * 1000000000 + now.tv_nsec - start.tv_nsec;
        if (elapsed >= 100000000) {
            atomic_fetch_add(&dropped, atomic_load(&active_recorders));
            break;
        }
    }
    const char *path = getenv("SKIKO_THREAD_STATE_CHECKER_LOG");
    if (!path) return;
    FILE *f = fopen(path, "w");
    if (!f) return;
    if (!atomic_load_explicit(&ready, memory_order_acquire)) {
        fprintf(f, "error\tChecker initialization failed\n");
        fclose(f);
        return;
    }
    fprintf(f, "header\t0x%lx\ndropped\t%llu\n", (unsigned long)main_header,
            (unsigned long long)atomic_load(&dropped));
    uint64_t records = 2;
    for (size_t i = 0; i < binding_count; i++) {
        if (atomic_load(&bindings[i].called)) {
            fprintf(f, "called\t%s\n", bindings[i].name);
            records++;
        }
        for (int sink = 0; sink < S_COUNT; sink++) {
            Entry *e = &entries[i * S_COUNT + sink];
            uint64_t count = atomic_load(&e->count);
            if (!count) continue;
            fprintf(f, "call\t%s\t%s\t%llu", bindings[i].name, SINK_NAMES[sink],
                    (unsigned long long)count);
            if (atomic_load_explicit(&e->sample, memory_order_acquire) == 2) {
                for (int j = 0; j < e->nframes; j++) fprintf(f, "\t0x%lx", (unsigned long)e->frames[j]);
            }
            fputc('\n', f);
            records++;
        }
    }
    if (fflush(f) == 0 && !ferror(f)) fprintf(f, "# end %llu\n", (unsigned long long)records);
    fclose(f);
}

static int h_mutex_lock(pthread_mutex_t *m) { record(S_MUTEX_LOCK); return pthread_mutex_lock(m); }
static int h_cond_wait(pthread_cond_t *c, pthread_mutex_t *m) { record(S_COND_WAIT); return pthread_cond_wait(c, m); }
static int h_cond_timedwait(pthread_cond_t *c, pthread_mutex_t *m, const struct timespec *t) {
    record(S_COND_TIMEDWAIT);
    return pthread_cond_timedwait(c, m, t);
}
static int h_rwlock_rdlock(pthread_rwlock_t *l) { record(S_RWLOCK_RDLOCK); return pthread_rwlock_rdlock(l); }
static int h_rwlock_wrlock(pthread_rwlock_t *l) { record(S_RWLOCK_WRLOCK); return pthread_rwlock_wrlock(l); }
static void h_unfair_lock(os_unfair_lock_t l) { record(S_UNFAIR_LOCK); os_unfair_lock_lock(l); }
static void h_unfair_lock_with_options(os_unfair_lock_t l, uint32_t options) {
    record(S_UNFAIR_LOCK_WITH_OPTIONS);
    os_unfair_lock_lock_with_options(l, options);
}
static int h_pthread_join(pthread_t thread, void **value) { record(S_PTHREAD_JOIN); return pthread_join(thread, value); }
static int h_nanosleep(const struct timespec *request, struct timespec *remaining) {
    record(S_NANOSLEEP);
    return nanosleep(request, remaining);
}
static void h_dispatch_sync(dispatch_queue_t queue, dispatch_block_t block) {
    record(S_DISPATCH_SYNC);
    dispatch_sync(queue, block);
}
static intptr_t h_dispatch_group_wait(dispatch_group_t group, dispatch_time_t timeout) {
    record(S_DISPATCH_GROUP_WAIT);
    return dispatch_group_wait(group, timeout);
}
static intptr_t h_dispatch_semaphore_wait(dispatch_semaphore_t s, dispatch_time_t t) {
    record(S_DISPATCH_SEMAPHORE_WAIT);
    return dispatch_semaphore_wait(s, t);
}
static kern_return_t h_semaphore_wait(semaphore_t s) { record(S_SEMAPHORE_WAIT); return semaphore_wait(s); }
static int h_open(const char *path, int flags, ...) {
    record(S_OPEN);
    mode_t mode = 0;
    if (flags & O_CREAT) {
        va_list ap;
        va_start(ap, flags);
        mode = (mode_t)va_arg(ap, int);
        va_end(ap);
    }
    return open(path, flags, mode);
}
static ssize_t h_read(int fd, void *buf, size_t n) { record(S_READ); return read(fd, buf, n); }
static ssize_t h_pread(int fd, void *buf, size_t n, off_t off) { record(S_PREAD); return pread(fd, buf, n, off); }
static ssize_t h_write(int fd, const void *buf, size_t n) { record(S_WRITE); return write(fd, buf, n); }
static ssize_t h_pwrite(int fd, const void *buf, size_t n, off_t off) { record(S_PWRITE); return pwrite(fd, buf, n, off); }
static int h_open_nocancel(const char *path, int flags, ...) {
    record(S_OPEN_NOCANCEL);
    mode_t mode = 0;
    if (flags & O_CREAT) {
        va_list ap;
        va_start(ap, flags);
        mode = (mode_t)va_arg(ap, int);
        va_end(ap);
    }
    return open_nocancel(path, flags, mode);
}
static ssize_t h_read_nocancel(int fd, void *buf, size_t n) { record(S_READ_NOCANCEL); return read_nocancel(fd, buf, n); }
static ssize_t h_pread_nocancel(int fd, void *buf, size_t n, off_t off) {
    record(S_PREAD_NOCANCEL);
    return pread_nocancel(fd, buf, n, off);
}
static ssize_t h_write_nocancel(int fd, const void *buf, size_t n) {
    record(S_WRITE_NOCANCEL);
    return write_nocancel(fd, buf, n);
}
static ssize_t h_pwrite_nocancel(int fd, const void *buf, size_t n, off_t off) {
    record(S_PWRITE_NOCANCEL);
    return pwrite_nocancel(fd, buf, n, off);
}
static void *h_mmap(void *a, size_t len, int prot, int flags, int fd, off_t off) {
    record(S_MMAP);
    return mmap(a, len, prot, flags, fd, off);
}
static void h_cfrelease(CFTypeRef o) { record(S_CFRELEASE); CFRelease(o); }
static void h_objc_release(void *o) { if (o) record(S_OBJC_RELEASE); objc_release(o); }
static void *h_objc_load_weak_retained(void **l) { record(S_OBJC_LOAD_WEAK_RETAINED); return objc_loadWeakRetained(l); }

#define INTERPOSE(replacement, replacee) \
    __attribute__((used)) static const struct { const void *r; const void *o; } _interpose_##replacee \
        __attribute__((section("__DATA,__interpose"))) = {(const void *)(uintptr_t)&replacement, (const void *)(uintptr_t)&replacee}

INTERPOSE(h_mutex_lock, pthread_mutex_lock);
INTERPOSE(h_cond_wait, pthread_cond_wait);
INTERPOSE(h_cond_timedwait, pthread_cond_timedwait);
INTERPOSE(h_rwlock_rdlock, pthread_rwlock_rdlock);
INTERPOSE(h_rwlock_wrlock, pthread_rwlock_wrlock);
INTERPOSE(h_unfair_lock, os_unfair_lock_lock);
INTERPOSE(h_unfair_lock_with_options, os_unfair_lock_lock_with_options);
INTERPOSE(h_pthread_join, pthread_join);
INTERPOSE(h_nanosleep, nanosleep);
INTERPOSE(h_dispatch_sync, dispatch_sync);
INTERPOSE(h_dispatch_group_wait, dispatch_group_wait);
INTERPOSE(h_dispatch_semaphore_wait, dispatch_semaphore_wait);
INTERPOSE(h_semaphore_wait, semaphore_wait);
INTERPOSE(h_open, open);
INTERPOSE(h_read, read);
INTERPOSE(h_pread, pread);
INTERPOSE(h_write, write);
INTERPOSE(h_pwrite, pwrite);
INTERPOSE(h_open_nocancel, open_nocancel);
INTERPOSE(h_read_nocancel, read_nocancel);
INTERPOSE(h_pread_nocancel, pread_nocancel);
INTERPOSE(h_write_nocancel, write_nocancel);
INTERPOSE(h_pwrite_nocancel, pwrite_nocancel);
INTERPOSE(h_mmap, mmap);
INTERPOSE(h_cfrelease, CFRelease);
INTERPOSE(h_objc_release, objc_release);
INTERPOSE(h_objc_load_weak_retained, objc_loadWeakRetained);
