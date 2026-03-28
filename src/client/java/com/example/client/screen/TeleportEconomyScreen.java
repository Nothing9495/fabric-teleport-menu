package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TeleportEconomyScreen extends BaseTeleportScreen {
	private final TeleportMainScreen mainScreen;

	public TeleportEconomyScreen(TeleportMainScreen mainScreen) {
		super(ClientI18n.tr("screen.teleportmenu.economy.title"));
		this.mainScreen = mainScreen;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(8);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.economy.title")).shadow(true));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.economy.leaderboard"), new ItemStack(Items.GOLD_INGOT), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportEconomyLeaderboardScreen(this));
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.economy.shops"), new ItemStack(Items.CHEST), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportShopListScreen(this));
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.economy.manage"), new ItemStack(Items.EMERALD_BLOCK), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportShopManageScreen(this));
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.mainScreen);
			}
		}));
	}
}
