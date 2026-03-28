package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import com.example.client.widget.OreHomeEntryButtonWidget;
import com.example.client.widget.OreSmallActionButtonWidget;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class TeleportTerritoryScreen extends BaseTeleportScreen {
	private static final int LIST_HEIGHT = 176;
	private static final int MORE_BUTTON_WIDTH = 30;

	private final List<TeleportMenuPayloads.TerritoryTarget> territories;
	private final List<TeleportMenuPayloads.PlayerTarget> players;
	private final TeleportMainScreen mainScreen;

	public TeleportTerritoryScreen(List<TeleportMenuPayloads.TerritoryTarget> territories, List<TeleportMenuPayloads.PlayerTarget> players, TeleportMainScreen mainScreen) {
		super(ClientI18n.tr("screen.teleportmenu.territories.title"));
		this.territories = List.copyOf(territories);
		this.players = List.copyOf(players);
		this.mainScreen = mainScreen;
	}

	public TeleportMainScreen mainScreen() {
		return this.mainScreen;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(8);

		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.territories.title")).shadow(true));

		FlowLayout listLayout = Containers.verticalFlow(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.content());
		listLayout.gap(3);

		if (this.territories.isEmpty()) {
			listLayout.child(Components.label(ClientI18n.tr("screen.teleportmenu.territories.empty")));
		} else {
			for (TeleportMenuPayloads.TerritoryTarget territory : this.territories) {
				FlowLayout row = Containers.horizontalFlow(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(PLAYER_ENTRY_HEIGHT));
				row.gap(4);

				OreHomeEntryButtonWidget widget = new OreHomeEntryButtonWidget(
					MAIN_BUTTON_WIDTH - MORE_BUTTON_WIDTH - 4,
					PLAYER_ENTRY_HEIGHT,
					territory.id(),
					territory.name(),
					territory.minX(),
					territory.minY(),
					territory.minZ(),
					territory.iconItemId(),
					territoryId -> {
						ClientPlayNetworking.send(new TeleportMenuPayloads.TeleportTerritoryRequestPayload(territoryId));
						this.onClose();
					}
				);
				var entryComponent = Components.wrapVanillaWidget(widget);
				entryComponent.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH - MORE_BUTTON_WIDTH - 4), Sizing.fixed(PLAYER_ENTRY_HEIGHT));

				OreSmallActionButtonWidget moreWidget = new OreSmallActionButtonWidget(
					MORE_BUTTON_WIDTH,
					PLAYER_ENTRY_HEIGHT,
					ClientI18n.tr("screen.teleportmenu.homes.more"),
					button -> {
						if (this.minecraft != null) {
							this.minecraft.setScreen(new TeleportTerritoryManageScreen(territory, this.players, this));
						}
					}
				);
				var moreComponent = Components.wrapVanillaWidget(moreWidget);
				moreComponent.sizing(Sizing.fixed(MORE_BUTTON_WIDTH), Sizing.fixed(PLAYER_ENTRY_HEIGHT));

				row.child(entryComponent);
				row.child(moreComponent);
				listLayout.child(row);
			}
		}

		ScrollContainer<FlowLayout> territoryList = Containers.verticalScroll(Sizing.fixed(MAIN_BUTTON_WIDTH + 4), Sizing.fixed(LIST_HEIGHT), listLayout);
		territoryList.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0x90C7D5ED)));
		territoryList.scrollbarThiccness(4);
		territoryList.surface(Surface.BLANK);

		rootComponent.child(territoryList);
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.territories.create"), new ItemStack(Items.LIME_CONCRETE), () -> {
			Minecraft client = this.minecraft;
			if (client == null) {
				return;
			}

			client.setScreen(null);
			if (client.player != null) {
				client.player.sendSystemMessage(ClientI18n.tr("message.teleportmenu.territory_create_guide"));
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.mainScreen);
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.close"), new ItemStack(Items.BARRIER), this::onClose));
	}
}
