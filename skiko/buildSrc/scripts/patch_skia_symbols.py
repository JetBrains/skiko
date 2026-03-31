#!/usr/bin/env python3
"""
Patches public symbols in Skia static libraries for iOS/tvOS to avoid symbol
conflicts when multiple copies of Skia (or libraries depending on Skia) are
linked into the same app binary.

Steps:
  1. Extract all public (globally visible, defined) symbols from every Skia
     static library (i.e. everything *except* the skiko C++ bridge).
  2. Write the collected symbol names to <output-dir>/symbols.txt.
  3. For *every* library (Skia libs + skiko bridge) rewrite each collected symbol with a "_skiko" suffix:
     _some_symbol  ->  _some_symbol_skiko
"""

import argparse
import os
import subprocess
import sys
from pathlib import Path

def run(cmd, cwd=None, check=True):
    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        cwd=cwd,
    )
    if check and result.returncode != 0:
        print(f"FAILED ({result.returncode}): {' '.join(cmd)}", file=sys.stderr)
        if result.stdout:
            print(result.stdout, file=sys.stderr)
        if result.stderr:
            print(result.stderr, file=sys.stderr)
        sys.exit(result.returncode)
    return result


def extract_global_defined_symbols(lib_path: str) -> set[str]:
    """
    Return the set of all globally-visible, defined symbol names found in
    a Mach-O static library.

    `nm -g --defined-only` outputs lines of the form:
        0000000000000020 T _SkPaint_Make
    followed by bare archive-member headers such as:
        /path/to/lib.a(object.o):
    We keep only lines with exactly three whitespace-separated tokens whose
    middle token is a single uppercase letter (defined global symbol type).
    """
    result = run(["xcrun", "nm", "-g", "--defined-only", lib_path])
    symbols: set[str] = set()
    for line in result.stdout.splitlines():
        parts = line.split()
        if (
            len(parts) == 3
            and len(parts[1]) == 1
            and parts[1].isupper()
        ):
            symbols.add(parts[2])
    return symbols


_SKIKO_NS = "skiko"
_NS_ENCODING = f"{len(_SKIKO_NS)}{_SKIKO_NS}"   # "5skiko"
# CV-qualifiers (K=const, V=volatile, r=restrict) and ref-qualifiers (R=&, O=&&)
# that may appear between N and the first prefix component in a nested-name.
_NESTED_QUALIFIERS = frozenset("KVrRO")

def _try_mangle_in_namespace(sym: str) -> "str | None":
    """
    Attempt to rewrite *sym* so that the 'skiko' namespace is encoded directly
    into the Itanium ABI mangled name.  Returns None when the symbol shape is
    too complex to rewrite safely without a full grammar parser.

    Handled forms
    -------------
    __ZN [cv/ref-quals] <source-name|nested-N…>
        Nested name (class methods, functions in a namespace).
        The namespace component is inserted after N (and after any leading
        CV / ref qualifiers), becoming the outermost qualifier:

          __ZN7SkPaint4MakeEv     →  __ZN5skiko7SkPaint4MakeEv
          demangles as: skiko::SkPaint::Make()

          __ZNK7SkPaint6getCapEv  →  __ZNK5skiko7SkPaint6getCapEv
          demangles as: skiko::SkPaint::getCap() const

    __ZTV|TI|TS|TT <type>
        Vtable / typeinfo / typeinfo-name / VTT.

        Simple source-name type (digit-prefixed):
          __ZTV7SkPaint           →  __ZTVN5skiko7SkPaintE
          demangles as: vtable for skiko::SkPaint

        Already-nested type (N…E):
          __ZTVN7SkPaint5InnerE   →  __ZTVN5skiko7SkPaint5InnerE
          demangles as: vtable for skiko::SkPaint::Inner

    Limitations
    -----------
    Symbols whose parameter types contain Itanium substitution back-references
    (S_, S0_, …) will have those references displaced by +1 after namespace
    insertion (the new 'skiko' namespace itself becomes substitution 0).
    In practice this affects parameter *type* display in the demangled output
    while leaving the function *name* readable — a significant improvement
    over the completely un-demangable suffix form.

    Symbols that start with a substitution (S_), template parameter (T), or
    other complex production as their first qualifier are left to the suffix
    fallback rather than risk silently producing wrong output.
    """
    if sym.startswith("__ZN"):
        body = sym[4:]
        # Skip any leading CV-qualifiers and ref-qualifiers.
        i = 0
        while i < len(body) and body[i] in _NESTED_QUALIFIERS:
            i += 1
        cv_quals = body[:i]
        rest = body[i:]
        # Only rewrite when the first qualifier is a source-name (digit) or a
        # further nested-name (N). Substitution references (S) and template
        # parameters (T) would need index-shifted rewrites — skip them.
        if rest and (rest[0].isdigit() or rest[0] == "N"):
            return f"__ZN{cv_quals}{_NS_ENCODING}{rest}"
        return None

    if sym.startswith("__Z") and len(sym) >= 5:
        tag = sym[3:5]
        if tag in ("TV", "TI", "TS", "TT"):
            type_enc = sym[5:]
            if type_enc.startswith("N"):
                # Already-nested type: N<body> — insert ns after N.
                return f"__Z{tag}N{_NS_ENCODING}{type_enc[1:]}"
            if type_enc and type_enc[0].isdigit():
                # Simple source-name type — wrap in N…E.
                return f"__Z{tag}N{_NS_ENCODING}{type_enc}E"

    return None


def renamed(sym: str, suffix: str = "_skiko") -> str:
    """
    Rename *sym* so that the result is still valid and demangable.

    For C++ Itanium-ABI mangled names the 'skiko' namespace is encoded
    directly into the mangled grammar, producing names that LLDB, c++filt,
    Instruments, Crashlytics, and Sentry can decode:

        __ZN7SkPaint4MakeEv   →  __ZN5skiko7SkPaint4MakeEv
                                 ↳ skiko::SkPaint::Make()

        __ZNK7SkPaint6getCapEv →  __ZNK5skiko7SkPaint6getCapEv
                                  ↳ skiko::SkPaint::getCap() const

        __ZTV7SkPaint         →  __ZTVN5skiko7SkPaintE
                                 ↳ vtable for skiko::SkPaint

    For plain C symbols (single leading '_', no '__Z') the suffix is
    appended — C identifiers allow trailing underscores and the result
    remains a legal C name:

        _uloc_getDefault      →  _uloc_getDefault_skiko

    Symbols with mangled shapes too complex to rewrite without a full
    Itanium ABI parser also receive the suffix as a safe fallback.
    """
    rewritten = _try_mangle_in_namespace(sym)
    return rewritten if rewritten is not None else sym + suffix


def patch_library(lib_path: str, redefine_syms_file: str, output_path: str):
    """
    Rename symbols in *lib_path* according to *redefine_syms_file* and write
    the result to *output_path*.

    Uses `xcrun llvm-objcopy --redefine-syms` which processes every object
    file inside the archive individually and rewrites both the definitions and
    the undefined (imported) references of each renamed symbol.
    """
    run([
        "xcrun", "llvm-objcopy",
        f"--redefine-syms={redefine_syms_file}",
        lib_path,
        output_path,
    ])

def main():
    parser = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "--skia-libs",
        nargs="+",
        required=True,
        metavar="LIB",
        help="Paths to Skia static libraries (.a). "
             "Public symbols are extracted from these libraries.",
    )
    parser.add_argument(
        "--skiko-bridge",
        required=True,
        metavar="LIB",
        help="Path to the skiko C++ native-bridges static library (.a). "
             "Symbols are NOT extracted from this library, but it IS patched.",
    )
    parser.add_argument(
        "--output-dir",
        required=True,
        metavar="DIR",
        help="Directory where patched libraries, symbols.txt and "
             "redefine-syms.txt will be written.",
    )
    args = parser.parse_args()

    out_dir = Path(args.output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    skia_libs: list[str] = args.skia_libs
    all_libs: list[str] = skia_libs + [args.skiko_bridge]

    # ------------------------------------------------------------------
    # 1. Collect all publicly defined symbols from the Skia libraries,
    #    skipping any that already carry the "_skiko" suffix — those were
    #    pre-renamed at the skia-pack build level (e.g. ICU C functions
    #    compiled with -DU_LIB_SUFFIX_C_NAME=_skiko) and must not be
    #    renamed a second time.
    # ------------------------------------------------------------------
    print("Extracting public symbols from Skia libraries …")
    all_symbols: set[str] = set()
    for lib in skia_libs:
        syms = extract_global_defined_symbols(lib)
        new_syms = {s for s in syms if not s.endswith("_skiko")}
        print(f"  {os.path.basename(lib):40s}  {len(new_syms):6d} symbols "
              f"({len(syms) - len(new_syms)} already-renamed skipped)")
        all_symbols.update(new_syms)
    print(f"  {'TOTAL':40s}  {len(all_symbols):6d} unique symbols to rename")

    # ------------------------------------------------------------------
    # 1b. Also collect C++ mangled symbols defined in the bridge itself.
    #
    #     The bridge's .cpp files include Skia headers and may produce
    #     symbol definitions locally — template instantiations or `inline`
    #     function copies — that were never instantiated inside the Skia
    #     static libraries.  Those symbols are NOT captured by the scan
    #     above, so without this step they would survive patching under
    #     their original names and could clash with a second copy of Skia
    #     linked into the same binary.
    #
    #     We rename ONLY symbols whose Mach-O name starts with "__Z"
    #     (the mandatory Itanium ABI prefix for every C++ mangled symbol
    #     on arm64/x86_64 Mach-O).  The bridge's own public API uses
    #     `extern "C"` (SKIKO_EXPORT), so those symbols have C linkage
    #     and a single-underscore prefix ("_org_jetbrains_…") — they
    #     never match "__Z" and are left unchanged, preserving the
    #     Kotlin/Native cinterop ABI.
    # ------------------------------------------------------------------
    print("Extracting C++ symbols from skiko bridge …")
    bridge_syms = extract_global_defined_symbols(args.skiko_bridge)
    bridge_cxx_syms = {
        s for s in bridge_syms
        if s.startswith("__Z") and not s.endswith("_skiko")
    }
    new_in_bridge = bridge_cxx_syms - all_symbols
    all_symbols.update(bridge_cxx_syms)
    print(f"  {'bridge (C++ only)':40s}  {len(bridge_cxx_syms):6d} symbols "
          f"({len(new_in_bridge)} not already covered by Skia libs)")

    # ------------------------------------------------------------------
    # 2. Write symbols.txt  (one symbol per line, sorted)
    # ------------------------------------------------------------------
    symbols_sorted = sorted(all_symbols)
    symbols_file = out_dir / "symbols.txt"
    symbols_file.write_text("\n".join(symbols_sorted) + "\n")
    print(f"Written: {symbols_file}")

    # ------------------------------------------------------------------
    # 3. Write redefine-syms.txt  (llvm-objcopy --redefine-syms format)
    # ------------------------------------------------------------------
    redefine_syms_file = out_dir / "redefine-syms.txt"
    lines = [f"{sym} {renamed(sym)}" for sym in symbols_sorted]
    redefine_syms_file.write_text("\n".join(lines) + "\n")
    print(f"Written: {redefine_syms_file}")

    # ------------------------------------------------------------------
    # 4. Patch every library (Skia libs + skiko bridge)
    # ------------------------------------------------------------------
    print("Patching libraries …")
    for lib in all_libs:
        out_lib = str(out_dir / os.path.basename(lib))
        print(f"  {os.path.basename(lib):40s}  ->  {out_lib}")
        patch_library(lib, str(redefine_syms_file), out_lib)

    print("Done.")


if __name__ == "__main__":
    main()
