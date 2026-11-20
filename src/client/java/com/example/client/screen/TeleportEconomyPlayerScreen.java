package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import com.example.client.widget.TeleportIcons;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;

public class TeleportEconomyPlayerScreen extends BaseTeleportScreen {
	private final TeleportEconomyManageScreen parent;
	private final TeleportMenuPayloads.PlayerTarget target;

	public TeleportEconomyPlayerScreen(TeleportEconomyManageScreen parent, TeleportMenuPayloads.PlayerTarget target) {
		super(ClientI18n.tr("screen.teleportmenu.admin.economy.player_title", target.name()));
		this.parent = parent;
		this.target = target;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.admin.economy.player_title", this.target.name())).shadow(true));

		TextBoxComponent amountBox = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		amountBox.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		amountBox.setMaxLength(32);
		amountBox.text("10");
		final String[] amountHolder = new String[]{"10"};
		amountBox.onChanged().subscribe(value -> amountHolder[0] = value == null ? "" : value);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.admin.economy.amount")));
		rootComponent.child(amountBox);

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.admin.economy.query"), TeleportIcons.QUERY, () -> sendAction("query", 0D)));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.admin.economy.add"), TeleportIcons.PLUS, () -> {
			Double amount = parseAmount(amountHolder[0]);
			if (amount != null) {
				sendAction("add", amount);
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.admin.economy.subtract"), TeleportIcons.MINUS, () -> {
			Double amount = parseAmount(amountHolder[0]);
			if (amount != null) {
				sendAction("subtract", amount);
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.admin.economy.set"), TeleportIcons.SET, () -> {
			Double amount = parseAmount(amountHolder[0]);
			if (amount != null) {
				sendAction("set", amount);
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.admin.economy.reset"), TeleportIcons.RESET, () -> sendAction("reset", 0D)));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), TeleportIcons.BACK, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.close"), TeleportIcons.CLOSE, this::onClose));
	}

	private void sendAction(String action, double amount) {
		ClientPlayNetworking.send(new TeleportMenuPayloads.EconomyAdminActionPayload(this.target.uuid(), action, amount));
	}

	private Double parseAmount(String raw) {
		String trimmed = raw == null ? "" : raw.trim();
		if (trimmed.isEmpty()) {
			return null;
		}

		try {
			double value = Double.parseDouble(trimmed);
			if (Double.isFinite(value) && value >= 0D) {
				return value;
			}
		} catch (NumberFormatException ignored) {
		}

		if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.displayClientMessage(ClientI18n.tr("message.teleportmenu.economy_invalid_amount").withStyle(net.minecraft.ChatFormatting.RED), false);
		}
		return null;
	}
}
