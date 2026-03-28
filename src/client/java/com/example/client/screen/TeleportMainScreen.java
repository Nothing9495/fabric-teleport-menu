package com.example.client.screen;

import com.example.client.widget.OreCardButtonWidget;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Set;

public class TeleportMainScreen extends BaseTeleportScreen {
	private static final int CARD_WIDTH = 120;
	private static final int CARD_HEIGHT = 72;
	private static final int CARD_GAP = 8;

	private final List<TeleportMenuPayloads.PlayerTarget> targets;
	private final List<TeleportMenuPayloads.HomeTarget> homes;
	private final List<TeleportMenuPayloads.TerritoryTarget> territories;
	private final boolean admin;
	private final Set<String> disabledItemIds;

	public TeleportMainScreen(
		List<TeleportMenuPayloads.PlayerTarget> targets,
		List<TeleportMenuPayloads.HomeTarget> homes,
		List<TeleportMenuPayloads.TerritoryTarget> territories,
		boolean admin,
		Set<String> disabledItemIds
	) {
		super(Component.translatable("screen.teleportmenu.main.title"));
		this.targets = List.copyOf(targets);
		this.homes = List.copyOf(homes);
		this.territories = List.copyOf(territories);
		this.admin = admin;
		this.disabledItemIds = Set.copyOf(disabledItemIds);
	}

	public List<TeleportMenuPayloads.PlayerTarget> targets() {
		return this.targets;
	}

	public List<TeleportMenuPayloads.HomeTarget> homes() {
		return this.homes;
	}

	public List<TeleportMenuPayloads.TerritoryTarget> territories() {
		return this.territories;
	}

	public boolean admin() {
		return this.admin;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(10);

		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.main.title")).shadow(true));

		FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(CARD_HEIGHT));
		row.gap(CARD_GAP);
		row.child(cardButton(Component.translatable("screen.teleportmenu.main.players"), new ItemStack(Items.PLAYER_HEAD), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportPlayerListScreen(this.targets, this));
			}
		}));
		row.child(cardButton(Component.translatable("screen.teleportmenu.main.homes"), new ItemStack(Items.RED_BED), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportHomeScreen(this.homes, this));
			}
		}));
		row.child(cardButton(Component.translatable("screen.teleportmenu.main.territories"), new ItemStack(Items.GRASS_BLOCK), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportTerritoryScreen(this.territories, this.targets, this));
			}
		}));

		if (this.admin) {
			row.child(cardButton(Component.translatable("screen.teleportmenu.main.admin"), new ItemStack(Items.COMMAND_BLOCK), () -> {
				if (this.minecraft != null) {
					this.minecraft.setScreen(new TeleportAdminScreen(this, this.disabledItemIds));
				}
			}));
			rootComponent.child(row);
			rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.main.close"), new ItemStack(Items.BARRIER), this::onClose));
		} else {
			row.child(cardButton(Component.translatable("screen.teleportmenu.main.close"), new ItemStack(Items.BARRIER), this::onClose));
			rootComponent.child(row);
		}
	}

	private io.wispforest.owo.ui.core.Component cardButton(Component text, ItemStack icon, Runnable onPress) {
		OreCardButtonWidget widget = new OreCardButtonWidget(CARD_WIDTH, CARD_HEIGHT, text, icon, button -> onPress.run());
		var wrapped = Components.wrapVanillaWidget(widget);
		wrapped.sizing(Sizing.fixed(CARD_WIDTH), Sizing.fixed(CARD_HEIGHT));
		return wrapped;
	}
}
