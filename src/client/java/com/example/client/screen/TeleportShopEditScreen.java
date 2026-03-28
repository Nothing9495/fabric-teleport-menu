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

public class TeleportShopEditScreen extends BaseTeleportScreen {
	private final TeleportShopManageScreen parent;
	private final TeleportMenuPayloads.ShopTarget shop;
	private String renameInput;

	public TeleportShopEditScreen(TeleportShopManageScreen parent, TeleportMenuPayloads.ShopTarget shop) {
		super(ClientI18n.tr("screen.teleportmenu.shop.edit.title", shop.name()));
		this.parent = parent;
		this.shop = shop;
		this.renameInput = shop.name();
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.edit.title", this.shop.name())).shadow(true));

		TextBoxComponent text = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		text.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		text.setMaxLength(20);
		text.text(this.renameInput);
		text.onChanged().subscribe(value -> this.renameInput = value == null ? "" : value);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.rename.prompt")));
		rootComponent.child(text);

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.rename.confirm"), new ItemStack(Items.NAME_TAG), () -> {
			String name = this.renameInput == null ? "" : this.renameInput.trim();
			if (name.isEmpty()) {
				return;
			}
			ClientPlayNetworking.send(new TeleportMenuPayloads.RenameShopRequestPayload(this.shop.id(), name));
		}));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.icon.chest"), new ItemStack(Items.CHEST), () -> sendIcon("minecraft:chest")));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.icon.ender"), new ItemStack(Items.ENDER_CHEST), () -> sendIcon("minecraft:ender_chest")));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.icon.shulker"), new ItemStack(Items.SHULKER_BOX), () -> sendIcon("minecraft:shulker_box")));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.icon.barrel"), new ItemStack(Items.BARREL), () -> sendIcon("minecraft:barrel")));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.goods.add"), new ItemStack(Items.CHEST_MINECART), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportShopAddGoodScreen(this, this.shop));
			}
		}));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.delete"), new ItemStack(Items.LAVA_BUCKET), () -> {
			ClientPlayNetworking.send(new TeleportMenuPayloads.DeleteShopRequestPayload(this.shop.id()));
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

	private void sendIcon(String icon) {
		ClientPlayNetworking.send(new TeleportMenuPayloads.SetShopIconRequestPayload(this.shop.id(), icon));
	}
}
