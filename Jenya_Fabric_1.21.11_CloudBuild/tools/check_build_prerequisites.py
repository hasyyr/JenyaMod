#!/usr/bin/env python3
"""Summarize local project prerequisites; does not compile the mod."""
from pathlib import Path
import shutil
import sys

root = Path(__file__).resolve().parents[1]
print("JENYA BUILD READINESS")
print("=====================")
print("Java executable:", shutil.which("java") or "NOT FOUND")
print("Gradle executable:", shutil.which("gradle") or "NOT FOUND")
print("Gradle wrapper:", "FOUND" if (root / "gradlew").exists() else "NOT FOUND")
print("Gradle wrapper JAR:", "FOUND" if (root / "gradle/wrapper/gradle-wrapper.jar").exists() else "NOT FOUND")
print("build.gradle:", "FOUND" if (root / "build.gradle").exists() else "NOT FOUND")
print("fabric.mod.json:", "FOUND" if (root / "src/main/resources/fabric.mod.json").exists() else "NOT FOUND")
print()
print("Этот скрипт только проверяет наличие инструментов и файлов; сборку не выполняет.")
if not shutil.which("java"):
    sys.exit(1)
