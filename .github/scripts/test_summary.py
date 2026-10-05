#!/usr/bin/env python3
import glob
import os
import xml.etree.ElementTree as ET

MAX_ANNOTATIONS = 10   # GitHub limita las anotaciones por step
TAIL_LINES = 80

paths = (glob.glob("**/target/surefire-reports/TEST-*.xml", recursive=True)
         + glob.glob("**/target/failsafe-reports/TEST-*.xml", recursive=True))

total = failures = errors = skipped = 0
bad, timings = [], []

for path in paths:
    for tc in ET.parse(path).getroot().iter("testcase"):
        total += 1
        cls, name = tc.get("classname", ""), tc.get("name", "")
        timings.append((float(tc.get("time") or 0), f"{cls}.{name}"))

        if tc.find("skipped") is not None:
            skipped += 1
            continue

        prob = tc.find("failure")
        if prob is None:
            prob = tc.find("error")
        if prob is not None:
            if prob.tag == "failure":
                failures += 1
            else:
                errors += 1
            lines = (prob.get("message") or "").strip().splitlines()
            msg = lines[0][:300] if lines else (prob.get("type") or "sin mensaje")
            bad.append((cls, name, msg, os.path.dirname(path)))

passed = total - failures - errors - skipped

# --- Anotaciones ---
for cls, name, msg, _ in bad[:MAX_ANNOTATIONS]:
    print(f"::error title={cls}.{name}::{msg}")
if len(bad) > MAX_ANNOTATIONS:
    print(f"::warning::{len(bad) - MAX_ANNOTATIONS} fallos más (ver resumen del job)")

# --- Log de las clases fallidas (colapsable) ---
seen = set()
for cls, _, _, d in bad:
    if (cls, d) in seen:
        continue
    seen.add((cls, d))
    out = os.path.join(d, f"{cls}-output.txt")
    if os.path.exists(out):
        print(f"::group::Log de {cls} (últimas {TAIL_LINES} líneas)")
        with open(out, errors="replace") as fh:
            print("".join(fh.readlines()[-TAIL_LINES:]))
        print("::endgroup::")

# --- Resumen Markdown ---
summary = os.environ.get("GITHUB_STEP_SUMMARY")
if summary:
    icon = "✅" if not bad else "❌"
    md = [f"## {icon} Resultados de pruebas\n",
          "| Total | Pasaron | Fallaron | Errores | Omitidas |",
          "|---|---|---|---|---|",
          f"| {total} | {passed} | {failures} | {errors} | {skipped} |\n"]
    if bad:
        md += ["### Pruebas fallidas\n", "| Clase | Test | Motivo |", "|---|---|---|"]
        for cls, name, msg, _ in bad:
            safe_msg = msg.replace("|", "\\|")
            short_cls = cls.split(".")[-1]
            md.append(f"| `{short_cls}` | `{name}` | {safe_msg} |")
        md.append("")
    md += ["<details><summary>10 pruebas más lentas</summary>\n",
           "| Segundos | Test |", "|---|---|"]
    for t, n in sorted(timings, reverse=True)[:10]:
        md.append(f"| {t:.2f} | `{n}` |")
    md.append("\n</details>")
    with open(summary, "a", encoding="utf-8") as fh:
        fh.write("\n".join(md) + "\n")
