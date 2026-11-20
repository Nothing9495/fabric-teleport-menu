package com.example.client.screen;

import com.example.client.i18n.ClientI18n;
import com.example.client.widget.OreShopGoodCardButtonWidget;
import com.example.client.widget.TeleportIcons;
import com.example.network.TeleportMenuPayloads;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

public class TeleportBoxShopScreen extends BaseTeleportScreen {
	private static final int CARD_WIDTH = 112;
	private static final int CARD_HEIGHT = 74;
	private static final int CARD_GAP = 8;
	private static final int CARDS_PER_ROW = 4;

	private final TeleportMenuPayloads.BoxShopTarget boxShop;
	private boolean openedNaming = false;

	public TeleportBoxShopScreen(TeleportMenuPayloads.BoxShopTarget boxShop) {
		super(Component.literal(boxShop.name()));
		this.boxShop = boxShop;
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		if (this.boxShop.owner() && this.boxShop.needsNaming() && !this.openedNaming && this.minecraft != null) {
			this.openedNaming = true;
			this.minecraft.setScreen(new TeleportBoxShopNameScreen(this.boxShop, this));
			return;
		}

		this.configureRoot(rootComponent);
		rootComponent.gap(8);
		rootComponent.child(Components.label(Component.literal(this.boxShop.name())).shadow(true));
		rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.boxshop.owner", this.boxShop.ownerName())));

		List<TeleportMenuPayloads.ShopGoodTarget> goods = this.boxShop.goods();
		if (goods.isEmpty()) {
			rootComponent.child(Components.label(ClientI18n.tr("screen.teleportmenu.shop.goods.empty")));
		} else {
			for (int start = 0; start < goods.size(); start += CARDS_PER_ROW) {
				FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(CARD_HEIGHT));
				row.gap(CARD_GAP);

				int end = Math.min(start + CARDS_PER_ROW, goods.size());
				for (int i = start; i < end; i++) {
					TeleportMenuPayloads.ShopGoodTarget good = goods.get(i);
					OreShopGoodCardButtonWidget widget = new OreShopGoodCardButtonWidget(
						CARD_WIDTH,
						CARD_HEIGHT,
						iconFor(good.iconItemId()),
						good.name(),
						ClientI18n.tr("screen.teleportmenu.shop.goods.stock", good.stock()).getString(),
						ClientI18n.tr("screen.teleportmenu.shop.buy.price", String.format(Locale.ROOT, "%.2f", good.price())).getString(),
						button -> {
							if (this.minecraft != null) {
								this.minecraft.setScreen(new TeleportBoxShopBuyScreen(this, this.boxShop, good));
							}
						}
					);
					var wrapped = Components.wrapVanillaWidget(widget);
					wrapped.sizing(Sizing.fixed(CARD_WIDTH), Sizing.fixed(CARD_HEIGHT));
					row.child(wrapped);
				}
				rootComponent.child(row);
			}
		}

		if (this.boxShop.owner()) {
			rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.boxshop.manage"), TeleportIcons.MANAGE, () -> {
				if (this.minecraft != null) {
					this.minecraft.setScreen(new TeleportBoxShopManageScreen(this.boxShop, this));
				}
			}));
		}

		rootComponent.child(menuButton(ClientI18n.tr("screen.teleportmenu.players.back"), TeleportIcons.BACK, this::onClose));
	}

	private static ItemStack iconFor(String itemId) {
		ResourceLocation id = ResourceLocation.tryParse(itemId);
		if (id == null) {
			return new ItemStack(Items.CHEST);
		}
		Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.CHEST);
		return new ItemStack(item);
	}
}
