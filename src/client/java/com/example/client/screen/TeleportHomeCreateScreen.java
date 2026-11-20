package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import com.example.client.widget.TeleportIcons;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class TeleportHomeCreateScreen extends BaseTeleportScreen {
	private final TeleportHomeScreen parent;
	private final int x;
	private final int y;
	private final int z;
	private String inputName = "";

	public TeleportHomeCreateScreen(TeleportHomeScreen parent) {
		super(ClientI18n.tr("screen.teleportmenu.homes.create"));
		this.parent = parent;

		BlockPos pos = currentPlayerPos();
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}

	public TeleportHomeScreen parentScreen() {
		return this.parent;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.homes.create")).shadow(true));
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.homes.current_pos", this.x, this.y, this.z)));
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.homes.input_prompt")));

		TextBoxComponent textBox = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		textBox.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		textBox.setMaxLength(20);
		textBox.onChanged().subscribe(value -> this.inputName = value);
		rootComponent.child(textBox);

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.homes.confirm_create"), TeleportIcons.CONFIRM, () -> {
			String name = this.inputName.trim();
			if (name.isEmpty()) {
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.sendSystemMessage(ClientI18n.tr("message.teleportmenu.home_name_empty"));
				}
				return;
			}

			ClientPlayNetworking.send(new TeleportMenuPayloads.CreateHomeRequestPayload(name));
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), TeleportIcons.BACK, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}

	private static BlockPos currentPlayerPos() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return BlockPos.ZERO;
		}

		return client.player.blockPosition();
	}
}
