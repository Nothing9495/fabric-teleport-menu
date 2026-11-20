package com.example.client.screen;

import com.example.client.i18n.ClientI18n;
import com.example.client.widget.TeleportIcons;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;

public class TeleportBoxShopManageScreen extends BaseTeleportScreen {
	private final TeleportMenuPayloads.BoxShopTarget boxShop;
	private final TeleportBoxShopScreen parent;

	public TeleportBoxShopManageScreen(TeleportMenuPayloads.BoxShopTarget boxShop, TeleportBoxShopScreen parent) {
		super(ClientI18n.tr("screen.teleportmenu.boxshop.manage"));
		this.boxShop = boxShop;
		this.parent = parent;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.boxshop.manage")).shadow(true));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.boxshop.name"), TeleportIcons.RENAME, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportBoxShopNameScreen(this.boxShop, this.parent));
			}
		}));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.goods.add"), TeleportIcons.ADD, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportBoxShopAddGoodScreen(this, this.boxShop));
			}
		}));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), TeleportIcons.BACK, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}
}
