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

public class TeleportShopCreateScreen extends BaseTeleportScreen {
	private final TeleportShopManageScreen parent;
	private String input = "";

	public TeleportShopCreateScreen(TeleportShopManageScreen parent) {
		super(ClientI18n.tr("screen.teleportmenu.shop.manage.create"));
		this.parent = parent;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.manage.create")).shadow(true));
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.create.prompt")));

		TextBoxComponent text = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		text.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		text.setMaxLength(20);
		text.text(this.input);
		text.onChanged().subscribe(value -> this.input = value == null ? "" : value);
		rootComponent.child(text);

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.create.confirm"), new ItemStack(Items.EMERALD), () -> {
			String name = this.input == null ? "" : this.input.trim();
			if (name.isEmpty()) {
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(ClientI18n.tr("message.teleportmenu.shop_name_empty").withStyle(net.minecraft.ChatFormatting.RED), false);
				}
				return;
			}
			ClientPlayNetworking.send(new TeleportMenuPayloads.CreateShopRequestPayload(name));
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
