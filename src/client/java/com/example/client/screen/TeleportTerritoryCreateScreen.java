package com.example.client.screen;

import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TeleportTerritoryCreateScreen extends BaseTeleportScreen {
	private final TeleportMainScreen mainScreen;
	private final int ax;
	private final int ay;
	private final int az;
	private final int bx;
	private final int by;
	private final int bz;
	private String inputName = "";

	public TeleportTerritoryCreateScreen(TeleportMainScreen mainScreen, int ax, int ay, int az, int bx, int by, int bz) {
		super(Component.translatable("screen.teleportmenu.territories.create_title"));
		this.mainScreen = mainScreen;
		this.ax = ax;
		this.ay = ay;
		this.az = az;
		this.bx = bx;
		this.by = by;
		this.bz = bz;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.territories.create_title")).shadow(true));
		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.territories.range", this.ax, this.ay, this.az, this.bx, this.by, this.bz)));
		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.territories.input_prompt")));

		TextBoxComponent textBox = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		textBox.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		textBox.setMaxLength(20);
		textBox.onChanged().subscribe(value -> this.inputName = value);
		rootComponent.child(textBox);

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.territories.confirm_create"), new ItemStack(Items.LIME_CONCRETE), () -> {
			String name = this.inputName.trim();
			if (name.isEmpty()) {
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.sendSystemMessage(Component.translatable("message.teleportmenu.territory_name_empty"));
				}
				return;
			}

			ClientPlayNetworking.send(new TeleportMenuPayloads.CreateTerritoryRequestPayload(name));
			this.onClose();
		}));

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.territories.cancel_create"), new ItemStack(Items.BARRIER), () -> {
			ClientPlayNetworking.send(new TeleportMenuPayloads.CancelTerritoryCreatePayload());
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.mainScreen);
			}
		}));
	}
}
