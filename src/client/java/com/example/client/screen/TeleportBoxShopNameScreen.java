package com.example.client.screen;

import com.example.client.i18n.ClientI18n;
import com.example.client.widget.TeleportIcons;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class TeleportBoxShopNameScreen extends BaseTeleportScreen {
	private final TeleportMenuPayloads.BoxShopTarget boxShop;
	private final TeleportBoxShopScreen parent;
	private String nameInput;

	public TeleportBoxShopNameScreen(TeleportMenuPayloads.BoxShopTarget boxShop, TeleportBoxShopScreen parent) {
		super(ClientI18n.tr("screen.teleportmenu.boxshop.name"));
		this.boxShop = boxShop;
		this.parent = parent;
		this.nameInput = boxShop.name();
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.boxshop.name")).shadow(true));

		TextBoxComponent textBox = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		textBox.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		textBox.setMaxLength(20);
		textBox.text(this.nameInput);
		textBox.onChanged().subscribe(value -> this.nameInput = value == null ? "" : value);
		rootComponent.child(textBox);

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.boxshop.name_confirm"), TeleportIcons.CONFIRM, () -> {
			String value = this.nameInput == null ? "" : this.nameInput.trim();
			if (value.isEmpty()) {
				return;
			}
			ClientPlayNetworking.send(new TeleportMenuPayloads.RenameBoxShopPayload(this.boxShop.boxShopId(), value));
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
}
