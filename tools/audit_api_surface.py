#!/usr/bin/env python3
"""Review likely API-sensitive Java usages in the Jenya Fabric project."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
checks = []
warnings = []
errors = []

def record(ok, message):
    (checks if ok else errors).append(("[OK]" if ok else "[ERROR]", message))

entity_path = ROOT / "src/main/java/ru/jenya/JenyaEntity.java"
main_path = ROOT / "src/main/java/ru/jenya/JenyaMod.java"
client_path = ROOT / "src/client/java/ru/jenya/client/JenyaClient.java"
build_path = ROOT / "build.gradle"
fabric_path = ROOT / "src/main/resources/fabric.mod.json"

for path, label in [
    (entity_path, "JenyaEntity.java"),
    (main_path, "JenyaMod.java"),
    (client_path, "JenyaClient.java"),
    (build_path, "build.gradle"),
    (fabric_path, "fabric.mod.json"),
]:
    record(path.is_file(), f"{label} присутствует")

if entity_path.exists():
    s = entity_path.read_text(encoding="utf-8")
    record("extends PathAwareEntity" in s, "Сущность использует PathAwareEntity для навигации")
    record("FollowNearbyPlayerGoal" in s, "FollowNearbyPlayerGoal объявлен")
    record("this.jenya.getNavigation().startMovingTo" in s, "Следование использует навигацию сущности")
    record("this.jenya.getWorld().getClosestPlayer" in s, "Поиск ближайшего игрока присутствует")
    record("AnimationController" in s and "registerControllers" in s,
           "GeckoLib controller объявлен")
    if "MathHelper" in s and "MathHelper." not in s:
        warnings.append(("[WARN]", "Импорт MathHelper не используется; можно удалить после компиляции"))
    if "isCreative()" in s and "isSpectator()" in s:
        warnings.append(("[WARN]", "Creative-игроки намеренно исключены из цели следования"))
if main_path.exists():
    s = main_path.read_text(encoding="utf-8")
    record("FabricDefaultAttributeRegistry.register" in s or "createMobAttributes" in s,
           "Код содержит регистрацию/создание атрибутов сущности")
    if "SpawnEggItem" in s:
        record("SpawnEggItem" in s, "В Java-коде присутствует регистрация spawn egg")
    else:
        warnings.append(("[WARN]", "Проверьте, как яйцо призыва регистрируется в JenyaMod.java"))
if client_path.exists():
    s = client_path.read_text(encoding="utf-8")
    record("EntityRendererRegistry.register" in s, "Регистрация renderer присутствует")
if build_path.exists():
    s = build_path.read_text(encoding="utf-8")
    record("1.21.11" in s, "build.gradle содержит Minecraft 1.21.11")
    record("geckolib-fabric-1.21.11" in s, "build.gradle содержит артефакт GeckoLib для 1.21.11")
if fabric_path.exists():
    try:
        data = __import__("json").loads(fabric_path.read_text(encoding="utf-8"))
        record(data.get("id") == "jenya", "fabric.mod.json ID совпадает с jenya")
        record("ru.jenya.JenyaMod" in data.get("entrypoints", {}).get("main", []),
               "Main entrypoint объявлен")
        record("ru.jenya.client.JenyaClient" in data.get("entrypoints", {}).get("client", []),
               "Client entrypoint объявлен")
    except Exception as e:
        errors.append(("[ERROR]", f"fabric.mod.json не читается: {e}"))

print("JENYA — API/BUILD STATIC AUDIT")
print("=" * 40)
for status, message in checks + warnings + errors:
    print(status, message)
print("-" * 40)
print(f"Ошибок проверок структуры: {len(errors)} | Предупреждений: {len(warnings)}")
print("Это не замена Gradle-компиляции: совместимость API подтверждается только реальной сборкой.")
raise SystemExit(1 if errors else 0)
