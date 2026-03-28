package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import com.example.client.model.ItemManageEntry;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashSet;
import java.util.Set;

public class TeleportItemDisableConfirmScreen extends BaseTeleportScreen {
	private final ItemManageEntry entry;
	private final TeleportItemManageScreen parent;
	private final Set<String> disabledItemIds;

	public TeleportItemDisableConfirmScreen(ItemManageEntry entry, TeleportItemManageScreen parent, Set<String> disabledItemIds) {
		super(ClientI18n.tr("screen.teleportmenu.admin.items.confirm_title"));
		this.entry = entry;
		this.parent = parent;
		this.disabledItemIds = Set.copyOf(disabledItemIds);
	}

	public ItemManageEntry entry() {
		return this.entry;
	}

	public TeleportItemManageScreen parentScreen() {
		return this.parent;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.admin.items.confirm_title")).shadow(true));
		rootComponent.child(Components.label(Component.literal(this.entry.registryId())));
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.admin.items.material", this.entry.materialLabel())));

		boolean disableSupported = this.entry.disableSupported();
		boolean alreadyDisabled = disableSupported && this.disabledItemIds.contains(this.entry.targetItemId());
		if (!disableSupported) {
			rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.admin.items.cannot_disable")));
		} else if (alreadyDisabled) {
			rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.admin.items.already_disabled")));
		}

		rootComponent.child(menuButton(
			alreadyDisabled
				? ClientI18n.tr("screen.teleportmenu.admin.items.enable")
				: ClientI18n.tr("screen.teleportmenu.admin.items.disable"),
			new ItemStack(Items.BARRIER),
			() -> {
			if (!disableSupported) {
				if (this.minecraft != null) {
					this.minecraft.setScreen(this.parent);
				}
				return;
			}

			boolean shouldDisable = !alreadyDisabled;
			ClientPlayNetworking.send(new TeleportMenuPayloads.DisableItemRequestPayload(this.entry.targetItemId(), shouldDisable));
			if (this.minecraft != null) {
				Set<String> nextState = new LinkedHashSet<>(this.disabledItemIds);
				if (shouldDisable) {
					nextState.add(this.entry.targetItemId());
				} else {
					nextState.remove(this.entry.targetItemId());
				}
				this.minecraft.setScreen(new TeleportItemManageScreen(this.parent.parentScreen(), nextState, this.parent.searchQuery()));
			}
		}));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.admin.items.cancel"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}
}
