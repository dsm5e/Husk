/* SPDX-License-Identifier: GPL-2.0-or-later */
#include "husk-tl-jni-internal.h"
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* No APK is loaded in this host test. Exercise the real JNI lookup and invoke
 * path with an absent optional dependency and a registered native binding. */
bool tl_dexidx_has_class(const char *name) { (void)name; return false; }
const char *tl_dexidx_super(const char *name, char *buf, size_t n)
{ (void)name; (void)buf; (void)n; return NULL; }
bool tl_dexidx_declares_method(const char *cls, const char *name, const char *sig, bool *is_static)
{ (void)cls; (void)name; (void)sig; (void)is_static; return false; }
bool tl_dexidx_method_named(const char *cls, const char *name)
{ (void)cls; (void)name; return false; }
bool tl_dexidx_field_sig(const char *cls, const char *name, char *out, size_t n)
{ (void)cls; (void)name; (void)out; (void)n; return false; }
bool tl_dexidx_declares_field(const char *cls, const char *name, const char *sig, bool *is_static)
{ (void)cls; (void)name; (void)sig; (void)is_static; return false; }
jvalue *dvm_grow_slots(jobj *object, uint32_t count)
{
    object->fields = realloc(object->fields, count * sizeof(jvalue));
    memset(object->fields + object->nfields, 0, (count - object->nfields) * sizeof(jvalue));
    object->nfields = count;
    return object->fields;
}
void tl_log_line(const char *fmt, ...) { (void)fmt; }

static bool attach(tl_jclass *cls) { (void)cls; return false; }
static const tl_dvm_hooks hooks = { .attach = attach };
static void answer(tl_jcall *call) { call->ret.i = 42; }
static const tl_jhle bindings[] = {
    { "test/NativeBinding", "answer", "()I", answer },
    { NULL, NULL, NULL, NULL }
};

int main(void)
{
    tl_dvm = &hooks;
    jobj *missing = tl_jni_class_object("test/OptionalBinding");
    /* Repeat to cover cached synthetic classes and method references. */
    for (int i = 0; i < 2; i++) {
        tl_jni_call(missing, "getSingleton", "()Ljava/lang/Object;", NULL);
        assert(tl_jni_pending());
        jobj *error = tl_jni_pending_object();
        assert(!strcmp(tl_jni_class_name(error), "java/lang/NoClassDefFoundError"));
        jobj *message = tl_jni_get_field(error, "detailMessage", "Ljava/lang/String;").l;
        assert(!strcmp(tl_jni_string(message), "test/OptionalBinding"));
        tl_jni_clear();
    }
    /* Existing framework stubs and actual HLE implementations still work. */
    assert(tl_jni_call(tl_jni_class_object("android/test/Stub"), "value", "()I", NULL).i == 0);
    assert(!tl_jni_pending());
    tl_jni_register_hle(bindings);
    assert(tl_jni_call(tl_jni_class_object("test/NativeBinding"), "answer", "()I", NULL).i == 42);
    assert(!tl_jni_pending());
    tl_jni_call(tl_jni_class_object("test/NativeBinding"), "optionalStub", "()V", NULL);
    assert(!tl_jni_pending());
    /* Native-only game mode retains its existing stub behavior. */
    tl_dvm = NULL;
    tl_jni_call(missing, "getSingleton", "()Ljava/lang/Object;", NULL);
    assert(!tl_jni_pending());
    puts("JNI missing-class checks passed");
    return 0;
}
