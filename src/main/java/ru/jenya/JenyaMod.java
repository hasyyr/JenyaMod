package ru.jenya;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class JenyaMod implements ModInitializer {
 public static final String MOD_ID = "jenya";
 public static final EntityType<JenyaEntity> JENYA = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID,"jenya"),
  EntityType.Builder.create(JenyaEntity::new, SpawnGroup.CREATURE).dimensions(0.75f,1.95f)
   .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID,"jenya"))));
 public static final Item JENYA_SPAWN_EGG = Registry.register(Registries.ITEM, Identifier.of(MOD_ID,"jenya_spawn_egg"),
  new SpawnEggItem(new Item.Settings().spawnEgg(JENYA).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "jenya_spawn_egg")))));
 @Override public void onInitialize() {
  FabricDefaultAttributeRegistry.register(JENYA, JenyaEntity.createAttributes());
  ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(JENYA_SPAWN_EGG));
 }
}
