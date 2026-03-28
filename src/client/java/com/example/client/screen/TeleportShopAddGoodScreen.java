package com.example.client.screen;

import com.example.client.i18n.ClientI18n;

import com.example.client.widget.OreIconListEntryButtonWidget;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeleportShopAddGoodScreen extends BaseTeleportScreen {
	private static final int LIST_HEIGHT = 120;

	private final TeleportShopEditScreen parent;
	private final TeleportMenuPayloads.ShopTarget shop;
	private final List<ItemChoice> itemChoices;
	private String searchQuery = "";
	private String amountInput = "1";
	private String priceInput = "10";
	private String selectedItemId = "";
	private FlowLayout listLayout;

	public TeleportShopAddGoodScreen(TeleportShopEditScreen parent, TeleportMenuPayloads.ShopTarget shop) {
		super(ClientI18n.tr("screen.teleportmenu.shop.goods.add"));
		this.parent = parent;
		this.shop = shop;
		this.itemChoices = buildItemChoices();
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.add")).shadow(true));

		TextBoxComponent search = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		search.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		search.setMaxLength(80);
		search.text(this.searchQuery);
		search.onChanged().subscribe(value -> {
			this.searchQuery = value == null ? "" : value;
			this.refreshList();
		});
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.search")));
		rootComponent.child(search);

		this.listLayout = Containers.verticalFlow(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.content());
		this.listLayout.gap(3);
		this.refreshList();

		ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fixed(MAIN_BUTTON_WIDTH + 4), Sizing.fixed(LIST_HEIGHT), this.listLayout);
		scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0x90C7D5ED)));
		scroll.scrollbarThiccness(4);
		scroll.surface(Surface.BLANK);
		rootComponent.child(scroll);

		TextBoxComponent amount = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		amount.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		amount.setMaxLength(6);
		amount.text(this.amountInput);
		amount.onChanged().subscribe(value -> this.amountInput = value == null ? "" : value);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.stock_input")));
		rootComponent.child(amount);

		TextBoxComponent price = Components.textBox(Sizing.fixed(MAIN_BUTTON_WIDTH));
		price.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(BUTTON_HEIGHT));
		price.setMaxLength(12);
		price.text(this.priceInput);
		price.onChanged().subscribe(value -> this.priceInput = value == null ? "" : value);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.price_input")));
		rootComponent.child(price);

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.goods.add_confirm"), new ItemStack(Items.EMERALD), () -> submit()));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), new ItemStack(Items.ARROW), () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(this.parent);
			}
		}));
	}

	private void submit() {
		if (this.selectedItemId.isBlank()) {
			return;
		}

		int stock;
		double price;
		try {
			stock = Integer.parseInt(this.amountInput.trim());
			price = Double.parseDouble(this.priceInput.trim());
		} catch (Exception ignored) {
			return;
		}
		if (stock <= 0 || price < 0D || !Double.isFinite(price)) {
			return;
		}

		ClientPlayNetworking.send(new TeleportMenuPayloads.AddShopGoodRequestPayload(this.shop.id(), this.selectedItemId, stock, price));
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}

	private void refreshList() {
		if (this.listLayout == null) {
			return;
		}
		this.listLayout.clearChildren();

		List<ItemChoice> choices = filteredChoices();
		if (choices.isEmpty()) {
			this.listLayout.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.empty")));
			return;
		}

		for (ItemChoice choice : choices) {
			String rightTop = String.valueOf(choice.count);
			String rightBottom = ClientI18n.tr("screen.teleportmenu.shop.goods.stock_unit").getString();
			OreIconListEntryButtonWidget widget = new OreIconListEntryButtonWidget(
				MAIN_BUTTON_WIDTH,
				PLAYER_ENTRY_HEIGHT,
				choice.stack,
				choice.name,
				choice.id,
				rightTop,
				rightBottom,
				button -> this.selectedItemId = choice.id
			);
			if (choice.id.equals(this.selectedItemId)) {
				widget.setMessage(Component.literal("* " + choice.name));
			}
			var wrapped = Components.wrapVanillaWidget(widget);
			wrapped.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(PLAYER_ENTRY_HEIGHT));
			this.listLayout.child(wrapped);
		}
	}

	private List<ItemChoice> filteredChoices() {
		String needle = this.searchQuery == null ? "" : this.searchQuery.trim().toLowerCase(Locale.ROOT);
		if (needle.isBlank()) {
			return this.itemChoices;
		}
		return this.itemChoices.stream().filter(choice ->
			choice.name.toLowerCase(Locale.ROOT).contains(needle)
				|| choice.id.toLowerCase(Locale.ROOT).contains(needle)
		).toList();
	}

	private List<ItemChoice> buildItemChoices() {
		if (this.minecraft == null || this.minecraft.player == null) {
			return List.of();
		}

		Map<String, Integer> counts = new LinkedHashMap<>();
		for (ItemStack stack : this.minecraft.player.getInventory().items) {
			if (stack.isEmpty()) {
				continue;
			}
			String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
			if ("minecraft:air".equals(id)) {
				continue;
			}
			counts.merge(id, stack.getCount(), Integer::sum);
		}

		List<ItemChoice> result = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			Item item = BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.ResourceLocation.parse(entry.getKey())).orElse(Items.AIR);
			if (item == Items.AIR) {
				continue;
			}
			ItemStack icon = new ItemStack(item);
			result.add(new ItemChoice(entry.getKey(), icon.getHoverName().getString(), icon, entry.getValue()));
		}
		result.sort(Comparator.comparing(choice -> choice.id));
		return result;
	}

	private static final class ItemChoice {
		private final String id;
		private final String name;
		private final ItemStack stack;
		private final int count;

		private ItemChoice(String id, String name, ItemStack stack, int count) {
			this.id = id;
			this.name = name;
			this.stack = stack;
			this.count = count;
		}
	}
}
