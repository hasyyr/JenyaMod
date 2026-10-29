package ru.jenya;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Optional local Ollama bridge for in-game chat. */
public final class JenyaDialogue {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private static final ConcurrentMap<UUID, List<Turn>> HISTORY = new ConcurrentHashMap<>();
    private static volatile String model = "qwen2.5";
    private static final String SYSTEM_PROMPT =
        "Ты Женя — дружелюбная, бойкая и заботливая спутница игрока в Minecraft. "
        + "Отвечай по-русски, естественно и кратко. Ты персонаж внутриигрового компаньона.";

    private JenyaDialogue() {}

    public static String getModel() { return model; }

    public static void setModel(String newModel) {
        if (newModel != null && newModel.matches("[A-Za-z0-9._:/-]{1,100}")) model = newModel;
    }

    public static void talk(ServerPlayerEntity player, String prompt) {
        String message = prompt == null ? "" : prompt.trim();
        if (message.isEmpty()) {
            player.sendMessage(Text.literal("§dЖеня: Напиши сообщение после /zhenya talk."), false);
            return;
        }
        if (message.length() > 1000) {
            player.sendMessage(Text.literal("§dЖеня: Сообщение слишком длинное — максимум 1000 символов."), false);
            return;
        }

        MinecraftServer server = player.getServer();
        UUID playerId = player.getUuid();
        String requestedModel = model;
        player.sendMessage(Text.literal("§dЖеня: §7Секундочку, думаю…"), false);

        List<Turn> history = HISTORY.computeIfAbsent(playerId, id -> new ArrayList<>());
        List<Turn> snapshot;
        synchronized (history) {
            history.add(new Turn("user", message));
            while (history.size() > 12) history.remove(0);
            snapshot = List.copyOf(history);
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", requestedModel);
        body.addProperty("stream", false);
        JsonArray messages = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", SYSTEM_PROMPT);
        messages.add(system);
        for (Turn turn : snapshot) {
            JsonObject item = new JsonObject();
            item.addProperty("role", turn.role());
            item.addProperty("content", turn.content());
            messages.add(item);
        }
        body.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:11434/api/chat"))
            .timeout(Duration.ofSeconds(90))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .whenComplete((response, failure) -> server.execute(() -> {
                if (failure != null) {
                    player.sendMessage(Text.literal("§dЖеня: §cНе могу подключиться к Ollama на этом компьютере. Запусти Ollama и проверь порт 11434."), false);
                    return;
                }
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    String detail = "HTTP " + response.statusCode();
                    try {
                        JsonObject errorJson = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (errorJson.has("error")) detail = errorJson.get("error").getAsString();
                    } catch (RuntimeException ignored) {}
                    player.sendMessage(Text.literal("§dЖеня: §cOllama вернула ошибку: " + detail + ". Проверь модель через /zhenya model <имя>."), false);
                    return;
                }
                try {
                    JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                    String answer = root.getAsJsonObject("message").get("content").getAsString().trim();
                    if (answer.isEmpty()) answer = "Я зависла на секунду. Скажи ещё раз?";
                    synchronized (history) {
                        history.add(new Turn("assistant", answer));
                        while (history.size() > 12) history.remove(0);
                    }
                    player.sendMessage(Text.literal("§dЖеня: §f" + answer), false);
                } catch (RuntimeException parseError) {
                    player.sendMessage(Text.literal("§dЖеня: §cНе смогла разобрать ответ Ollama. Проверь лог сервера."), false);
                }
            }));
    }

    private record Turn(String role, String content) {}
}
