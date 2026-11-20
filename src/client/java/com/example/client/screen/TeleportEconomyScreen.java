package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import com.example.client.widget.TeleportIcons;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.network.chat.Component;

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

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.economy.leaderboard"), TeleportIcons.LEADERBOARD, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportEconomyLeaderboardScreen(this));
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.economy.shops"), TeleportIcons.SHOP, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportShopListScreen(this));
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.economy.manage"), TeleportIcons.MANAGE, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportShopManageScreen(this));
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.economy.boxshop"), TeleportIcons.BOXSHOP, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(null);
				if (this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(ClientI18n.tr("message.teleportmenu.boxshop_hint").withStyle(net.minecraft.ChatFormatting.GREEN), false);
				}
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), TeleportIcons.BACK, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.mainScreen);
			}
		}));
	}
}
