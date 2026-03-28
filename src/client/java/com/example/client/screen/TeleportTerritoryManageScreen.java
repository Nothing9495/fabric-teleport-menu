package com.example.client.screen;

import com.example.client.widget.OreToggleSliderWidget;
import com.example.client.widget.OreSmallActionButtonWidget;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class TeleportTerritoryManageScreen extends BaseTeleportScreen {
	private static final int TOGGLE_WIDTH = 52;

	private TeleportMenuPayloads.TerritoryTarget territory;
	private final List<TeleportMenuPayloads.PlayerTarget> players;
	private final TeleportTerritoryScreen parent;

	public TeleportTerritoryManageScreen(TeleportMenuPayloads.TerritoryTarget territory, List<TeleportMenuPayloads.PlayerTarget> players, TeleportTerritoryScreen parent) {
		super(Component.translatable("screen.teleportmenu.territories.manage_title"));
		this.territory = territory;
		this.players = List.copyOf(players);
		this.parent = parent;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.territories.manage_title")).shadow(true));
		rootComponent.child(Components.label(Component.literal(this.territory.name())));

		rootComponent.child(permissionRow(Component.translatable("screen.teleportmenu.territories.permission.build_break"), this.territory.buildBreakAllowed(), "build_break"));
		rootComponent.child(permissionRow(Component.translatable("screen.teleportmenu.territories.permission.container"), this.territory.containerAllowed(), "container"));
		rootComponent.child(permissionRow(Component.translatable("screen.teleportmenu.territories.permission.interact"), this.territory.interactAllowed(), "interact"));
		rootComponent.child(permissionRow(Component.translatable("screen.teleportmenu.territories.permission.damage_entities"), this.territory.damageEntitiesAllowed(), "damage_entities"));
		rootComponent.child(permissionRow(Component.translatable("screen.teleportmenu.territories.permission.block_use"), this.territory.blockUseAllowed(), "block_use"));

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.territories.whitelist"), new ItemStack(Items.WRITABLE_BOOK), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportTerritoryWhitelistScreen(this.territory, this.players, this));
			}
		}));

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.players.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}

	public TeleportMenuPayloads.TerritoryTarget territory() {
		return this.territory;
	}

	void updateTerritory(TeleportMenuPayloads.TerritoryTarget territory) {
		this.territory = territory;
	}

	private io.wispforest.owo.ui.core.Component permissionRow(Component label, boolean enabled, String key) {
		FlowLayout row = Containers.horizontalFlow(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		row.gap(6);
		row.verticalAlignment(VerticalAlignment.CENTER);

		OreSmallActionButtonWidget labelWidget = new OreSmallActionButtonWidget(172, BUTTON_HEIGHT, label, button -> {
		});
		labelWidget.active = false;
		var labelComponent = Components.wrapVanillaWidget(labelWidget);
		labelComponent.sizing(Sizing.fixed(172), Sizing.fixed(BUTTON_HEIGHT));
		row.child(labelComponent);

		OreToggleSliderWidget toggleWidget = new OreToggleSliderWidget(TOGGLE_WIDTH, BUTTON_HEIGHT - 4, enabled, value -> {
			ClientPlayNetworking.send(new TeleportMenuPayloads.UpdateTerritoryPermissionPayload(this.territory.id(), key, value));
			this.territory = withPermission(this.territory, key, value);
		});
		var toggle = Components.wrapVanillaWidget(toggleWidget);
		toggle.sizing(Sizing.fixed(TOGGLE_WIDTH), Sizing.fixed(BUTTON_HEIGHT - 4));
		row.child(toggle);

		return row;
	}

	private static TeleportMenuPayloads.TerritoryTarget withPermission(TeleportMenuPayloads.TerritoryTarget t, String key, boolean value) {
		boolean buildBreak = t.buildBreakAllowed();
		boolean container = t.containerAllowed();
		boolean interact = t.interactAllowed();
		boolean damage = t.damageEntitiesAllowed();
		boolean blockUse = t.blockUseAllowed();

		switch (key) {
			case "build_break" -> buildBreak = value;
			case "container" -> container = value;
			case "interact" -> interact = value;
			case "damage_entities" -> damage = value;
			case "block_use" -> blockUse = value;
			default -> {
			}
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
			buildBreak,
			container,
			interact,
			damage,
			blockUse,
			new ArrayList<>(t.whitelistPlayerIds())
		);
	}
}
