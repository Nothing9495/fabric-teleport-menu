package com.example.client.screen;

import com.example.client.model.ItemManageEntry;
import com.example.client.widget.OreRegistryEntryButtonWidget;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TeleportItemManageScreen extends BaseTeleportScreen {
	private static final int LIST_HEIGHT = 184;

	private final TeleportAdminScreen parent;
	private final Set<String> disabledItemIds;
	private final List<ItemManageEntry> allEntries;
	private String searchQuery;

	private FlowLayout listLayout;

	public TeleportItemManageScreen(TeleportAdminScreen parent, Set<String> disabledItemIds, String searchQuery) {
		super(Component.translatable("screen.teleportmenu.admin.items.title"));
		this.parent = parent;
		this.disabledItemIds = Set.copyOf(disabledItemIds);
		this.allEntries = buildEntries();
		this.searchQuery = searchQuery == null ? "" : searchQuery;
	}

	public TeleportAdminScreen parentScreen() {
		return this.parent;
	}

	public String searchQuery() {
		return this.searchQuery;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);

		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.admin.items.title")).shadow(true));

		TextBoxComponent searchBox = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		searchBox.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		searchBox.setMaxLength(120);
		searchBox.text(this.searchQuery);
		searchBox.onChanged().subscribe(value -> {
			this.searchQuery = value == null ? "" : value;
			this.refreshList();
		});
		rootComponent.child(Components.label(Component.translatable("screen.teleportmenu.admin.items.search")));
		rootComponent.child(searchBox);

		this.listLayout = Containers.verticalFlow(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.content());
		this.listLayout.gap(3);
		this.refreshList();

		ScrollContainer<FlowLayout> itemList = Containers.verticalScroll(Sizing.fixed(MAIN_BUTTON_WIDTH + 4), Sizing.fixed(LIST_HEIGHT), this.listLayout);
		itemList.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0x90C7D5ED)));
		itemList.scrollbarThiccness(4);
		itemList.surface(Surface.BLANK);
		rootComponent.child(itemList);

		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.players.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
		rootComponent.child(menuButton(Component.translatable("screen.teleportmenu.players.close"), new ItemStack(Items.BARRIER), this::onClose));
	}

	private void refreshList() {
		if (this.listLayout == null) {
			return;
		}

		this.listLayout.clearChildren();
		List<ItemManageEntry> entries = filteredEntries(this.searchQuery);
		if (entries.isEmpty()) {
			this.listLayout.child(Components.label(Component.translatable("screen.teleportmenu.admin.items.empty")));
			return;
		}

		for (ItemManageEntry entry : entries) {
			boolean disabled = entry.disableSupported() && this.disabledItemIds.contains(entry.targetItemId());
			OreRegistryEntryButtonWidget widget = new OreRegistryEntryButtonWidget(
				MAIN_BUTTON_WIDTH,
				PLAYER_ENTRY_HEIGHT,
				entry,
				disabled,
				clickedEntry -> {
					if (this.minecraft != null) {
						this.minecraft.setScreen(new TeleportItemDisableConfirmScreen(clickedEntry, this, this.disabledItemIds));
					}
				}
			);

			var wrapped = Components.wrapVanillaWidget(widget);
			wrapped.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(PLAYER_ENTRY_HEIGHT));
			this.listLayout.child(wrapped);
		}
	}

	private List<ItemManageEntry> filteredEntries(String query) {
		String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		if (needle.isBlank()) {
			return this.allEntries;
		}

		return this.allEntries.stream()
			.filter(entry ->
				entry.registryId().toLowerCase(Locale.ROOT).contains(needle)
					|| entry.materialLabel().toLowerCase(Locale.ROOT).contains(needle)
					|| entry.displayName().toLowerCase(Locale.ROOT).contains(needle))
			.toList();
	}

	private static List<ItemManageEntry> buildEntries() {
		List<ItemManageEntry> entries = new ArrayList<>();

		for (Item item : BuiltInRegistries.ITEM) {
			String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
			if ("minecraft:air".equals(itemId)) {
				continue;
			}
			ItemStack icon = new ItemStack(item);
			entries.add(new ItemManageEntry(itemId, icon.getHoverName().getString(), "物品", itemId, icon));
		}

		for (Block block : BuiltInRegistries.BLOCK) {
			String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
			Item blockItem = block.asItem();
			String targetId = blockItem != Items.AIR ? BuiltInRegistries.ITEM.getKey(blockItem).toString() : "";
			ItemStack icon = blockItem != Items.AIR ? new ItemStack(blockItem) : new ItemStack(Items.GRASS_BLOCK);
			String displayName = blockItem != Items.AIR ? icon.getHoverName().getString() : block.getName().getString();
			entries.add(new ItemManageEntry(blockId, displayName, "方块", targetId, icon));
		}

		entries.sort(Comparator.comparing(ItemManageEntry::registryId));
		return List.copyOf(entries);
	}
}
