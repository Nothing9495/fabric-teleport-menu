package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import com.example.client.widget.OrePlayerEntryButtonWidget;
import com.example.client.widget.OreToggleSliderWidget;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TeleportTerritoryWhitelistScreen extends BaseTeleportScreen {
	private static final int LIST_HEIGHT = 176;
	private static final int TOGGLE_WIDTH = 52;

	private TeleportMenuPayloads.TerritoryTarget territory;
	private final List<TeleportMenuPayloads.PlayerTarget> players;
	private final TeleportTerritoryManageScreen parent;

	public TeleportTerritoryWhitelistScreen(TeleportMenuPayloads.TerritoryTarget territory, List<TeleportMenuPayloads.PlayerTarget> players, TeleportTerritoryManageScreen parent) {
		super(ClientI18n.tr("screen.teleportmenu.territories.whitelist"));
		this.territory = territory;
		this.players = List.copyOf(players);
		this.parent = parent;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(8);

		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.territories.whitelist")).shadow(true));

		FlowLayout listLayout = Containers.verticalFlow(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.content());
		listLayout.gap(3);

		if (this.players.isEmpty()) {
			listLayout.child(Components.label(ClientI18n.tr("screen.teleportmenu.territories.whitelist.empty")));
		} else {
			for (TeleportMenuPayloads.PlayerTarget player : this.players) {
				FlowLayout row = Containers.horizontalFlow(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(PLAYER_ENTRY_HEIGHT));
				row.gap(4);
				row.verticalAlignment(VerticalAlignment.CENTER);

				OrePlayerEntryButtonWidget entryWidget = new OrePlayerEntryButtonWidget(
					MAIN_BUTTON_WIDTH - TOGGLE_WIDTH - 4,
					PLAYER_ENTRY_HEIGHT,
					player.uuid(),
					player.name(),
					uuid -> {
					}
				);
				entryWidget.active = false;
				var entryComponent = Components.wrapVanillaWidget(entryWidget);
				entryComponent.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH - TOGGLE_WIDTH - 4), Sizing.fixed(PLAYER_ENTRY_HEIGHT));

				boolean whitelisted = this.territory.whitelistPlayerIds().contains(player.uuid());
				OreToggleSliderWidget toggleWidget = new OreToggleSliderWidget(TOGGLE_WIDTH, 20, whitelisted, value -> {
					ClientPlayNetworking.send(new TeleportMenuPayloads.UpdateTerritoryWhitelistPayload(this.territory.id(), player.uuid(), value));
					this.territory = withWhitelist(this.territory, player.uuid(), value);
					this.parent.updateTerritory(this.territory);
				});
				var toggleComponent = Components.wrapVanillaWidget(toggleWidget);
				toggleComponent.sizing(Sizing.fixed(TOGGLE_WIDTH), Sizing.fixed(20));

				row.child(entryComponent);
				row.child(toggleComponent);
				listLayout.child(row);
			}
		}

		ScrollContainer<FlowLayout> whitelistList = Containers.verticalScroll(Sizing.fixed(MAIN_BUTTON_WIDTH + 4), Sizing.fixed(LIST_HEIGHT), listLayout);
		whitelistList.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0x90C7D5ED)));
		whitelistList.scrollbarThiccness(4);
		whitelistList.surface(Surface.BLANK);

		rootComponent.child(whitelistList);
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.territories.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}

	private static TeleportMenuPayloads.TerritoryTarget withWhitelist(TeleportMenuPayloads.TerritoryTarget t, UUID playerId, boolean allow) {
		Set<UUID> whitelist = new LinkedHashSet<>(t.whitelistPlayerIds());
		if (allow) {
			whitelist.add(playerId);
		} else {
			whitelist.remove(playerId);
		}

		return new TeleportMenuPayloads.TerritoryTarget(
			t.id(),
			t.name(),
			t.ownerName(),
			t.minX(),
			t.minY(),
			t.minZ(),
			t.maxX(),
			t.maxY(),
			t.maxZ(),
			t.iconItemId(),
			t.buildBreakAllowed(),
			t.containerAllowed(),
			t.interactAllowed(),
			t.damageEntitiesAllowed(),
			t.blockUseAllowed(),
			new ArrayList<>(whitelist)
		);
	}
}
