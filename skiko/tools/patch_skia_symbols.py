#!/usr/bin/env python3
"""
Patches public symbols in Skia static libraries for iOS/tvOS to avoid symbol
conflicts when multiple copies of Skia (or libraries depending on Skia) are
linked into the same app binary.

Steps:
  1. Extract all public (globally visible, defined) symbols from every Skia
     static library (i.e. everything *except* the skiko C++ bridge).
  2. Write the collected symbol names to <output-dir>/symbols.txt.
  3. For *every* library (Skia libs + skiko bridge) rewrite each collected
     symbol with a "_skiko" suffix:
         _some_symbol  ->  _some_symbol_skiko
"""

import argparse
import os
import subprocess
import sys
from pathlib import Path


# ---------------------------------------------------------------------------
# helpers
# ---------------------------------------------------------------------------

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


def renamed(sym: str, suffix: str = "_skiko") -> str:
    """
    Apply the renaming rule: append "_skiko" to every symbol.

    Mach-O global symbols carry a leading underscore by convention:
      _uloc_getDefault     ->  _uloc_getDefault_skiko
      __ZN7SkPaint4MakeEv  ->  __ZN7SkPaint4MakeEv_skiko
    """
    return sym + suffix


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


# ---------------------------------------------------------------------------
# main
# ---------------------------------------------------------------------------

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
