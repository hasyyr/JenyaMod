#!/usr/bin/env python3
"""Bundle non-secret build diagnostics for the Jenya project."""
from pathlib import Path
import json, os, platform, shutil, subprocess, sys, zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "JENYA_DIAGNOSTICS.zip"
entries = [
    "build.gradle", "settings.gradle", "gradle.properties",
    "src/main/resources/fabric.mod.json",
    "src/main/java/ru/jenya/JenyaMod.java",
    "src/main/java/ru/jenya/JenyaEntity.java",
    "src/client/java/ru/jenya/client/JenyaClient.java",
    "PREFLIGHT_RESULT_STAGE13.txt",
]
summary = [
    "JENYA DIAGNOSTICS",
    "=================",
    f"Platform: {platform.platform()}",
    f"Python: {sys.version.split()[0]}",
    f"Java path: {shutil.which('java') or 'NOT FOUND'}",
    f"Gradle path: {shutil.which('gradle') or 'NOT FOUND'}",
    f"Gradle wrapper: {'FOUND' if (ROOT / 'gradlew').exists() else 'NOT FOUND'}",
    f"Wrapper JAR: {'FOUND' if (ROOT / 'gradle/wrapper/gradle-wrapper.jar').exists() else 'NOT FOUND'}",
    "",
]
for cmd in (["java", "-version"], ["gradle", "--version"]):
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, timeout=15)
        summary.append("$ " + " ".join(cmd))
        summary.append((p.stdout + p.stderr).strip() or f"exit={p.returncode}")
    except Exception as exc:
        summary.append("$ " + " ".join(cmd))
        summary.append(f"Unavailable: {exc}")
    summary.append("")

# Do not include user home, credentials, gradle caches, .env, or arbitrary logs.
with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as zf:
    zf.writestr("DIAGNOSTICS_SUMMARY.txt", "\n".join(summary))
    for rel in entries:
        p = ROOT / rel
        if p.is_file():
            zf.write(p, rel)
print(f"Created: {OUT}")
print("Archive includes only project build files, relevant source, static audit result and tool versions.")
print("It intentionally excludes home folders, credentials, caches and arbitrary logs.")
