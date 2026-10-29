#!/usr/bin/env python3
"""Static asset/source checks for the Jenya Fabric mod. Uses only Python stdlib."""
from pathlib import Path
import json
import re
import struct
import sys

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"
GEO = RES / "assets/jenya/geckolib/models/entity/jenya.geo.json"
ANIM = RES / "assets/jenya/geckolib/animations/entity/jenya.animation.json"
TEX = RES / "assets/jenya/textures/entity/jenya.png"
MODJSON = RES / "fabric.mod.json"
ITEM_DEF = RES / "assets/jenya/items/jenya_spawn_egg.json"
ITEM_MODEL = RES / "assets/jenya/models/item/jenya_spawn_egg.json"
ITEM_TEX = RES / "assets/jenya/textures/item/jenya_spawn_egg.png"
RU_LANG = RES / "assets/jenya/lang/ru_ru.json"
EN_LANG = RES / "assets/jenya/lang/en_us.json"
JAVA_MAIN = ROOT / "src/main/java/ru/jenya/JenyaMod.java"
JAVA_ENTITY = ROOT / "src/main/java/ru/jenya/JenyaEntity.java"
JAVA_CLIENT = ROOT / "src/client/java/ru/jenya/client/JenyaClient.java"

errors = []
warnings = []
checks = []

def check(ok, message):
    (checks if ok else errors).append(("OK" if ok else "ERROR", message))

def read_json(path, label):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(("ERROR", f"{label}: не удалось прочитать JSON: {exc}"))
        return None

for p, label in [
    (GEO, "Геометрия модели"),
    (ANIM, "Анимации"),
    (TEX, "Основная текстура"),
    (MODJSON, "fabric.mod.json"),
    (ITEM_DEF, "Client item definition для spawn egg"),
    (ITEM_MODEL, "Модель предмета spawn egg"),
    (ITEM_TEX, "Текстура spawn egg"),
    (RU_LANG, "Русская локализация"),
    (EN_LANG, "Английская локализация"),
    (JAVA_MAIN, "Главный Java-класс"),
    (JAVA_ENTITY, "Класс сущности"),
    (JAVA_CLIENT, "Клиентский renderer-класс"),
]:
    check(p.is_file(), f"{label}: {'найден' if p.is_file() else 'ОТСУТСТВУЕТ'}")

geo = read_json(GEO, "Геометрия") if GEO.exists() else None
anim = read_json(ANIM, "Анимации") if ANIM.exists() else None
mod = read_json(MODJSON, "fabric.mod.json") if MODJSON.exists() else None
item_def = read_json(ITEM_DEF, "Client item definition") if ITEM_DEF.exists() else None
item_model = read_json(ITEM_MODEL, "Модель предмета") if ITEM_MODEL.exists() else None
ru_lang = read_json(RU_LANG, "Русская локализация") if RU_LANG.exists() else None
en_lang = read_json(EN_LANG, "Английская локализация") if EN_LANG.exists() else None

bone_names = set()
if geo:
    geometries = geo.get("minecraft:geometry")
    check(isinstance(geometries, list) and len(geometries) > 0, "В geo JSON есть minecraft:geometry")
    if isinstance(geometries, list) and geometries:
        bones = geometries[0].get("bones", [])
        bone_names = {b.get("name") for b in bones if isinstance(b, dict)}
        check(len(bone_names) > 0, f"Кости модели найдены: {len(bone_names)}")
        desc = geometries[0].get("description", {})
        check(bool(desc.get("texture_width")) and bool(desc.get("texture_height")),
              "В описании модели заданы размеры текстуры")
        if len(bone_names) > 300:
            warnings.append(("WARN", f"Модель содержит {len(bone_names)} костей: проверьте импорт в Blockbench/GeckoLib 5"))

animation_names = set()
if anim:
    animations = anim.get("animations", {})
    animation_names = set(animations.keys()) if isinstance(animations, dict) else set()
    check("animation.ellie.idle" in animation_names, "Найдена анимация animation.ellie.idle")
    check("animation.ellie.walk" in animation_names, "Найдена анимация animation.ellie.walk")
    check(len(animation_names) > 0, f"Клипы анимации найдены: {len(animation_names)}")

if ru_lang:
    check(ru_lang.get("entity.jenya.jenya") == "Женя", "Русское имя сущности задано")
    check(ru_lang.get("item.jenya.jenya_spawn_egg") == "Яйцо призыва Жени", "Русское имя яйца призыва задано")
if en_lang:
    check(en_lang.get("entity.jenya.jenya") == "Jenya", "Английское имя сущности задано")
    check(en_lang.get("item.jenya.jenya_spawn_egg") == "Jenya Spawn Egg", "Английское имя яйца призыва задано")

if item_def:
    check(item_def.get("model", {}).get("type") == "minecraft:model",
          "Client item definition использует minecraft:model")
    check(item_def.get("model", {}).get("model") == "jenya:item/jenya_spawn_egg",
          "Client item definition ссылается на модель jenya:item/jenya_spawn_egg")
if item_model:
    check(item_model.get("parent") == "minecraft:item/generated",
          "Модель spawn egg наследует minecraft:item/generated")
    check(item_model.get("textures", {}).get("layer0") == "jenya:item/jenya_spawn_egg",
          "Модель spawn egg ссылается на собственную текстуру")

if ITEM_TEX.exists():
    try:
        with ITEM_TEX.open("rb") as f:
            header = f.read(24)
        if header[:4] == bytes([137, 80, 78, 71]):
            width, height = struct.unpack(">II", header[16:24])
            check((width, height) == (16, 16), f"Текстура яйца призыва имеет размер {width}×{height}")
        else:
            errors.append(("ERROR", "Текстура яйца призыва не похожа на PNG"))
    except Exception as exc:
        errors.append(("ERROR", f"Не удалось проверить текстуру яйца призыва: {exc}"))

if TEX.exists():
    try:
        with TEX.open("rb") as f:
            header = f.read(24)
        if header[:8] == b"\x89PNG\r\n\x1a\n":
            width, height = struct.unpack(">II", header[16:24])
            check(width > 0 and height > 0, f"PNG читается: {width}×{height}")
            if width != height:
                warnings.append(("WARN", "Текстура не квадратная; убедитесь, что размеры совпадают с geo JSON"))
            if width >= 1024:
                warnings.append(("WARN", "Текстура крупная; это допустимо, но визуальную UV-развёртку нужно проверить в игре"))
        else:
            errors.append(("ERROR", "Основная текстура не похожа на PNG"))
    except Exception as exc:
        errors.append(("ERROR", f"Не удалось проверить PNG: {exc}"))

if mod:
    check(mod.get("id") == "jenya", "ID мода = jenya")
    entry = mod.get("entrypoints", {})
    check("ru.jenya.JenyaMod" in entry.get("main", []), "Главный entrypoint объявлен")
    check("ru.jenya.client.JenyaClient" in entry.get("client", []), "Клиентский entrypoint объявлен")
    deps = mod.get("depends", {})
    check(deps.get("minecraft") == "~1.21.11", "fabric.mod.json ориентирован на Minecraft 1.21.11")
    check("geckolib" in deps, "GeckoLib объявлен зависимостью")

if JAVA_ENTITY.exists():
    text = JAVA_ENTITY.read_text(encoding="utf-8")
    check("registerControllers" in text, "GeoEntity регистрирует контроллер анимации")
    check("animation.ellie.idle" in text and "animation.ellie.walk" in text,
          "Java-контроллер использует найденные idle/walk клипы")
    check("FollowNearbyPlayerGoal" in text, "Добавлено поведение следования за ближайшим игроком")
    check("getClosestPlayer" in text and "startMovingTo" in text,
          "Follow goal ищет игрока и использует навигацию")
    check("stopDistance" in text and "followRange" in text,
          "Для следования заданы дистанции начала/остановки")
if JAVA_CLIENT.exists():
    text = JAVA_CLIENT.read_text(encoding="utf-8")
    check("EntityRendererRegistry.register" in text, "Регистрация рендера присутствует")

print("ПРОВЕРКА ПРОЕКТА JENYA — СТАТИЧЕСКИЙ PREFLIGHT")
print("=" * 52)
for status, message in checks + warnings + errors:
    print(f"[{status}] {message}")
print("-" * 52)
print(f"Ошибок: {len(errors)} | Предупреждений: {len(warnings)}")
print("ВАЖНО: этот скрипт не компилирует Java и не запускает Minecraft.")
sys.exit(1 if errors else 0)
