package ru.jenya.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class JenyaCommandScreen extends Screen {
    public JenyaCommandScreen() {
        super(Text.literal("Женя"));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 105;
        int top = this.height / 2 - 45;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Иди за мной"), button -> {
            sendCommand("zhenya follow");
        }).dimensions(left, top, 210, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Стой здесь"), button -> {
            sendCommand("zhenya stay");
        }).dimensions(left, top + 26, 210, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Давай поговорим"), button -> {
            if (this.client != null) {
                this.client.setScreen(new ChatScreen("/zhenya talk ", false));
            }
        }).dimensions(left, top + 52, 210, 20).build());
    }

    private void sendCommand(String command) {
        if (this.client != null && this.client.player != null) {
            this.client.player.networkHandler.sendChatCommand(command);
        }
        this.close();
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 72, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
