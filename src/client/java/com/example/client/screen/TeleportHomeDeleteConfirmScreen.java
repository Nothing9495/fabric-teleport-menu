package com.example.client.screen;

import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TeleportHomeDeleteConfirmScreen extends BaseTeleportScreen {
	private final TeleportMenuPayloads.HomeTarget home;
	private final TeleportHomeManageScreen parent;

	public TeleportHomeDeleteConfirmScreen(TeleportMenuPayloads.HomeTarget home, TeleportHomeManageScreen parent) {
		super(Component.translatable("screen.teleportmenu.homes.delete_confirm_title"));
		this.home = home;
		this.parent = parent;
	}

	public TeleportHomeManageScreen parentScreen() {
		return this.parent;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.homes.delete_confirm_title")).shadow(true));
		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.homes.delete_confirm_desc", this.home.name())));

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.homes.delete_confirm_yes"), new ItemStack(Items.LAVA_BUCKET), () -> {
			ClientPlayNetworking.send(new TeleportMenuPayloads.DeleteHomeRequestPayload(this.home.id()));
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent.parentScreen());
			}
		}));
		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.homes.delete_confirm_no"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}
}
