package ru.jenya;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class JenyaMod implements ModInitializer {
    public static final String MOD_ID = "jenya";

    public static final EntityType<JenyaEntity> JENYA = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "jenya"),
        EntityType.Builder.create(JenyaEntity::new, SpawnGroup.CREATURE)
            .dimensions(0.75f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "jenya")))
    );

    public static final Item JENYA_SPAWN_EGG = Registry.register(
        Registries.ITEM,
        Identifier.of(MOD_ID, "jenya_spawn_egg"),
        new SpawnEggItem(new Item.Settings()
            .spawnEgg(JENYA)
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "jenya_spawn_egg"))))
    );

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(JENYA, JenyaEntity.createAttributes());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(JENYA_SPAWN_EGG));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            CommandManager.literal("zhenya")
                .executes(context -> spawnJenya(context.getSource()))
                .then(CommandManager.literal("talk")
                    .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> talkToJenya(context.getSource(),
                            StringArgumentType.getString(context, "message")))))
                .then(CommandManager.literal("model")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> setModel(context.getSource(),
                            StringArgumentType.getString(context, "name")))))
        ));
    }

    private static int spawnJenya(ServerCommandSource source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        BlockPos pos = player.getBlockPos().offset(player.getHorizontalFacing(), 2);
        JenyaEntity spawned = JENYA.spawn((ServerWorld) player.getEntityWorld(), pos, SpawnReason.COMMAND);
        if (spawned == null) {
            source.sendError(Text.literal("Не удалось создать Женю. Попробуй выбрать другое место."));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("§dЖеня появилась рядом с тобой. 💚 ПКМ по ней — приветствие; /zhenya talk <сообщение> — диалог с Ollama."), false);
        return 1;
    }

    private static int talkToJenya(ServerCommandSource source, String message) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        JenyaDialogue.talk(source.getPlayerOrThrow(), message);
        return 1;
    }

    private static int setModel(ServerCommandSource source, String name) {
        if (!name.matches("[A-Za-z0-9._:/-]{1,100}")) {
            source.sendError(Text.literal("Имя модели может содержать латиницу, цифры, точку, подчёркивание, двоеточие, дефис или слэш."));
            return 0;
        }
        JenyaDialogue.setModel(name);
        source.sendFeedback(() -> Text.literal("Модель Ollama для Жени: " + JenyaDialogue.getModel()), false);
        return 1;
    }
}
