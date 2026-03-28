package com.example.client.screen;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

public class TeleportAdminScreen extends BaseTeleportScreen {
	private final TeleportMainScreen mainScreen;
	private final Set<String> disabledItemIds;

	public TeleportAdminScreen(TeleportMainScreen mainScreen, Set<String> disabledItemIds) {
		super(Component.translatable("screen.teleportmenu.admin.title"));
		this.mainScreen = mainScreen;
		this.disabledItemIds = Set.copyOf(disabledItemIds);
	}

	public TeleportMainScreen mainScreen() {
		return this.mainScreen;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.admin.title")).shadow(true));

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.admin.item_manage"), new ItemStack(Items.CHEST), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportItemManageScreen(this, this.disabledItemIds, ""));
			}
		}));
		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.players.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.mainScreen);
			}
		}));
		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.players.close"), new ItemStack(Items.BARRIER), this::onClose));
	}
}
