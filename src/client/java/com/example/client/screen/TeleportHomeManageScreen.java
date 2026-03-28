package com.example.client.screen;

import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TeleportHomeManageScreen extends BaseTeleportScreen {
	private final TeleportMenuPayloads.HomeTarget home;
	private final TeleportHomeScreen parent;
	private String renameInput;

	public TeleportHomeManageScreen(TeleportMenuPayloads.HomeTarget home, TeleportHomeScreen parent) {
		super(Component.translatable("screen.teleportmenu.homes.manage_title"));
		this.home = home;
		this.parent = parent;
		this.renameInput = home.name();
	}

	public TeleportHomeScreen parentScreen() {
		return this.parent;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.homes.manage_title")).shadow(true));
		rootComponent.child(Components.label(Component.literal(this.home.name())));
		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.homes.current_pos", this.home.x(), this.home.y(), this.home.z())));
		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.homes.rename_prompt")));

		TextBoxComponent textBox = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		textBox.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		textBox.setMaxLength(20);
		textBox.text(this.home.name());
		textBox.onChanged().subscribe(value -> this.renameInput = value);
		rootComponent.child(textBox);

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.homes.rename"), new ItemStack(Items.NAME_TAG), () -> {
			String newName = this.renameInput.trim();
			if (newName.isEmpty()) {
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.sendSystemMessage(Component.translatable("message.teleportmenu.home_name_empty"));
				}
				return;
			}

			ClientPlayNetworking.send(new TeleportMenuPayloads.RenameHomeRequestPayload(this.home.id(), newName));
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.homes.delete"), new ItemStack(Items.LAVA_BUCKET), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportHomeDeleteConfirmScreen(this.home, this));
			}
		}));

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.players.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}
}
