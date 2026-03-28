package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

public class TeleportShopBuyScreen extends BaseTeleportScreen {
	private final TeleportShopGoodsScreen parent;
	private final TeleportMenuPayloads.ShopTarget shop;
	private final TeleportMenuPayloads.ShopGoodTarget good;
	private String amountText = "1";

	public TeleportShopBuyScreen(TeleportShopGoodsScreen parent, TeleportMenuPayloads.ShopTarget shop, TeleportMenuPayloads.ShopGoodTarget good) {
		super(ClientI18n.tr("screen.teleportmenu.shop.buy.title", good.name()));
		this.parent = parent;
		this.shop = shop;
		this.good = good;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.buy.title", this.good.name())).shadow(true));
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.stock", this.good.stock())));
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.buy.price", String.format(Locale.ROOT, "%.2f", this.good.price()))));

		TextBoxComponent amount = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		amount.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		amount.setMaxLength(6);
		amount.text(this.amountText);
		amount.onChanged().subscribe(value -> this.amountText = value == null ? "" : value);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.buy.amount")));
		rootComponent.child(amount);

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.buy.confirm"), new ItemStack(Items.EMERALD), () -> {
			int amountValue;
			try {
				amountValue = Integer.parseInt(this.amountText.trim());
			} catch (Exception ignored) {
				amountValue = -1;
			}
			if (amountValue <= 0) {
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(ClientI18n.tr("message.teleportmenu.shop_buy_invalid_amount").withStyle(net.minecraft.ChatFormatting.RED), false);
				}
				return;
			}

			ClientPlayNetworking.send(new TeleportMenuPayloads.BuyShopGoodRequestPayload(this.shop.id(), this.good.itemId(), amountValue));
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}
}
