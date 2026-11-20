package com.example.client.screen;

import com.example.client.i18n.ClientI18n;
import com.example.client.widget.OreIconListEntryButtonWidget;
import com.example.client.widget.TeleportIcons;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TeleportBoxShopAddGoodScreen extends BaseTeleportScreen {
	private final TeleportBoxShopManageScreen parent;
	private final TeleportMenuPayloads.BoxShopTarget boxShop;
	private final ItemStack selectedStack;
	private final String selectedItemId;
	private final String selectedItemName;
	private final int selectedStackCount;

	private String amountInput;
	private String priceInput;

	public TeleportBoxShopAddGoodScreen(TeleportBoxShopManageScreen parent, TeleportMenuPayloads.BoxShopTarget boxShop) {
		this(parent, boxShop, ItemStack.EMPTY, "1", "10");
	}

	public TeleportBoxShopAddGoodScreen(
		TeleportBoxShopManageScreen parent,
		TeleportMenuPayloads.BoxShopTarget boxShop,
		ItemStack selectedStack,
		String amountInput,
		String priceInput
	) {
		super(ClientI18n.tr("screen.teleportmenu.shop.goods.add"));
		this.parent = parent;
		this.boxShop = boxShop;
		this.selectedStack = selectedStack == null ? ItemStack.EMPTY : selectedStack.copy();
		this.selectedStackCount = this.selectedStack.isEmpty() ? 0 : this.selectedStack.getCount();
		if (!this.selectedStack.isEmpty()) {
			this.selectedItemId = BuiltInRegistries.ITEM.getKey(this.selectedStack.getItem()).toString();
			this.selectedItemName = this.selectedStack.getHoverName().getString();
		} else {
			this.selectedItemId = "";
			this.selectedItemName = "";
		}
		this.amountInput = amountInput == null || amountInput.isBlank() ? "1" : amountInput;
		this.priceInput = priceInput == null || priceInput.isBlank() ? "10" : priceInput;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		this.configureRoot(rootComponent);
		rootComponent.gap(6);
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.add")).shadow(true));

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.goods.select"), TeleportIcons.SELECT, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TeleportBoxShopGoodPickerScreen(this.parent, this.boxShop, this.selectedStack, this.amountInput, this.priceInput));
			}
		}));

		if (this.selectedItemId.isBlank()) {
			rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.select_empty")));
			rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.select_hint")));
		} else {
			OreIconListEntryButtonWidget widget = new OreIconListEntryButtonWidget(
				MAIN_BUTTON_WIDTH,
				PLAYER_ENTRY_HEIGHT,
				new ItemStack(this.selectedStack.getItem()),
				this.selectedItemName,
				ClientI18n.tr("screen.teleportmenu.shop.goods.selected_id", this.selectedItemId).getString(),
				String.valueOf(this.selectedStackCount),
				ClientI18n.tr("screen.teleportmenu.shop.goods.stock_unit").getString(),
				button -> {}
			);
			widget.active = false;
			var wrapped = Components.wrapVanillaWidget(widget);
			wrapped.sizing(Sizing.fixed(MAIN_BUTTON_WIDTH), Sizing.fixed(PLAYER_ENTRY_HEIGHT));
			rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.selected", this.selectedItemName)));
			rootComponent.child(wrapped);
		}

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

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.shop.goods.add_confirm"), TeleportIcons.CONFIRM, () -> submit()));
		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), TeleportIcons.BACK, () -> {
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

		ResourceLocation itemKey = ResourceLocation.tryParse(this.selectedItemId);
		if (itemKey == null || !BuiltInRegistries.ITEM.containsKey(itemKey)) {
			return;
		}

		ClientPlayNetworking.send(new TeleportMenuPayloads.AddShopGoodRequestPayload(this.boxShop.shopId(), this.selectedItemId, stock, price));
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}
}
