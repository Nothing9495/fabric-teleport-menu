package com.example.client;

import com.example.client.i18n.ClientI18n;
import com.example.client.screen.*;
import com.example.network.TeleportMenuPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TeleportMenuClient implements ClientModInitializer {
	public static final int UI_PROTOCOL_VERSION = 2;
	private static final Set<String> DISABLED_ITEM_IDS = new LinkedHashSet<>();
	private static List<TeleportMenuPayloads.EconomyLeaderboardEntry> LEADERBOARD = List.of();
	private static List<TeleportMenuPayloads.ShopTarget> SHOPS = List.of();
	private static int OWN_SHOP_COUNT = 0;
	private static int OWN_SHOP_LIMIT = 2;
	private static String BOX_HUD_NAME = "";
	private static int BOX_HUD_GOODS = 0;
	private static long BOX_HUD_EXPIRE_AT = 0L;
	private static BlockPos LAST_BOX_QUERY_POS = null;
	private static long NEXT_BOX_QUERY_AT = 0L;

	public static Set<String> disabledItemIds() {
		return Set.copyOf(DISABLED_ITEM_IDS);
	}

	public static void setDisabledItemIds(List<String> ids) {
		DISABLED_ITEM_IDS.clear();
		DISABLED_ITEM_IDS.addAll(ids);
	}

	public static List<TeleportMenuPayloads.EconomyLeaderboardEntry> leaderboardEntries() {
		return LEADERBOARD;
	}

	public static List<TeleportMenuPayloads.ShopTarget> shopTargets() {
		return SHOPS;
	}

	public static int ownShopCount() {
		return OWN_SHOP_COUNT;
	}

	public static int ownShopLimit() {
		return OWN_SHOP_LIMIT;
	}

	@Override
	public void onInitializeClient() {
		registerPayloadTypes();
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ClientI18n.onJoin(handler.getConnection().getRemoteAddress().toString());
			clearBoxHud();
			ClientPlayNetworking.send(new TeleportMenuPayloads.ClientHelloPayload(UI_PROTOCOL_VERSION));
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clearBoxHud());
		ClientTickEvents.END_CLIENT_TICK.register(TeleportMenuClient::tickBoxHudQuery);
		HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> renderBoxHud(guiGraphics));

		ClientPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.OpenMenuPayload.TYPE, (payload, context) -> {
			setDisabledItemIds(payload.disabledItemIds());
			TeleportMainScreen mainScreen = new TeleportMainScreen(payload.targets(), payload.homes(), payload.territories(), payload.admin(), disabledItemIds());
			if (payload.territorySelectionReady()) {
				context.client().setScreen(new TeleportTerritoryCreateScreen(
					mainScreen,
					payload.selectionAx(),
					payload.selectionAy(),
					payload.selectionAz(),
					payload.selectionBx(),
					payload.selectionBy(),
					payload.selectionBz()
				));
			} else {
				context.client().setScreen(mainScreen);
			}
		});


		ClientPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.CloseMenuPayload.TYPE, (payload, context) -> {
			var currentScreen = context.client().screen; if (currentScreen != null && currentScreen.getClass().getName().startsWith("com.example.client.screen.Teleport")) {
				context.client().setScreen(null);
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.HomeListPayload.TYPE, (payload, context) -> {
			if (context.client().screen instanceof TeleportHomeScreen homeScreen) {
				context.client().setScreen(new TeleportHomeScreen(payload.homes(), homeScreen.mainScreen()));
			} else if (context.client().screen instanceof TeleportHomeCreateScreen createScreen) {
				context.client().setScreen(new TeleportHomeScreen(payload.homes(), createScreen.parentScreen().mainScreen()));
			} else if (context.client().screen instanceof TeleportHomeManageScreen manageScreen) {
				context.client().setScreen(new TeleportHomeScreen(payload.homes(), manageScreen.parentScreen().mainScreen()));
			} else if (context.client().screen instanceof TeleportHomeDeleteConfirmScreen deleteConfirmScreen) {
				context.client().setScreen(new TeleportHomeScreen(payload.homes(), deleteConfirmScreen.parentScreen().parentScreen().mainScreen()));
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.DisabledItemListPayload.TYPE, (payload, context) -> {
			setDisabledItemIds(payload.disabledItemIds());

			if (context.client().screen instanceof TeleportItemManageScreen itemManageScreen) {
				context.client().setScreen(new TeleportItemManageScreen(itemManageScreen.parentScreen(), disabledItemIds(), itemManageScreen.searchQuery()));
			} else if (context.client().screen instanceof TeleportItemDisableConfirmScreen confirmScreen) {
				context.client().setScreen(new TeleportItemDisableConfirmScreen(confirmScreen.entry(), confirmScreen.parentScreen(), disabledItemIds()));
			} else if (context.client().screen instanceof TeleportAdminScreen adminScreen) {
				context.client().setScreen(new TeleportAdminScreen(adminScreen.mainScreen(), disabledItemIds()));
			} else if (context.client().screen instanceof TeleportMainScreen mainScreen) {
				context.client().setScreen(new TeleportMainScreen(mainScreen.targets(), mainScreen.homes(), mainScreen.territories(), mainScreen.admin(), disabledItemIds()));
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.EconomyStatePayload.TYPE, (payload, context) -> {
			LEADERBOARD = List.copyOf(payload.leaderboard());
			SHOPS = List.copyOf(payload.shops());
			OWN_SHOP_COUNT = payload.ownShopCount();
			OWN_SHOP_LIMIT = payload.ownShopLimit();
		});

		ClientPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.OpenBoxShopPayload.TYPE, (payload, context) -> {
			context.client().setScreen(new TeleportBoxShopScreen(payload.target()));
		});

		ClientPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.BoxShopHudPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Minecraft client = context.client();
				if (client.level == null) {
					clearBoxHud();
					return;
				}
				if (!payload.present()) {
					clearBoxHud();
					return;
				}
				BOX_HUD_NAME = payload.name();
				BOX_HUD_GOODS = Math.max(0, payload.goodsCount());
				BOX_HUD_EXPIRE_AT = client.level.getGameTime() + 30L;
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.LocalizationSyncPayload.TYPE, (payload, context) -> {
			java.util.Map<String, java.util.Map<String, String>> packs = new java.util.HashMap<>();
			for (TeleportMenuPayloads.LanguagePack pack : payload.packs()) {
				java.util.Map<String, String> values = new java.util.HashMap<>();
				for (TeleportMenuPayloads.LangEntry entry : pack.entries()) {
					values.put(entry.key(), entry.value());
				}
				packs.put(pack.languageCode(), values);
			}
			ClientI18n.applyServerTranslations(packs, payload.version());
		});
	}

	private static void registerPayloadTypes() {
		registerS2C(TeleportMenuPayloads.CloseMenuPayload.TYPE, TeleportMenuPayloads.CloseMenuPayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.OpenMenuPayload.TYPE, TeleportMenuPayloads.OpenMenuPayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.HomeListPayload.TYPE, TeleportMenuPayloads.HomeListPayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.DisabledItemListPayload.TYPE, TeleportMenuPayloads.DisabledItemListPayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.EconomyStatePayload.TYPE, TeleportMenuPayloads.EconomyStatePayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.OpenBoxShopPayload.TYPE, TeleportMenuPayloads.OpenBoxShopPayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.BoxShopHudPayload.TYPE, TeleportMenuPayloads.BoxShopHudPayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.LocalizationSyncPayload.TYPE, TeleportMenuPayloads.LocalizationSyncPayload.STREAM_CODEC);

		registerC2S(TeleportMenuPayloads.TeleportRequestPayload.TYPE, TeleportMenuPayloads.TeleportRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.TeleportHomeRequestPayload.TYPE, TeleportMenuPayloads.TeleportHomeRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.TeleportTerritoryRequestPayload.TYPE, TeleportMenuPayloads.TeleportTerritoryRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.CreateHomeRequestPayload.TYPE, TeleportMenuPayloads.CreateHomeRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.CreateTerritoryRequestPayload.TYPE, TeleportMenuPayloads.CreateTerritoryRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.CancelTerritoryCreatePayload.TYPE, TeleportMenuPayloads.CancelTerritoryCreatePayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.UpdateTerritoryPermissionPayload.TYPE, TeleportMenuPayloads.UpdateTerritoryPermissionPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.UpdateTerritoryWhitelistPayload.TYPE, TeleportMenuPayloads.UpdateTerritoryWhitelistPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.ClientHelloPayload.TYPE, TeleportMenuPayloads.ClientHelloPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.DeleteHomeRequestPayload.TYPE, TeleportMenuPayloads.DeleteHomeRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.RenameHomeRequestPayload.TYPE, TeleportMenuPayloads.RenameHomeRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.DisableItemRequestPayload.TYPE, TeleportMenuPayloads.DisableItemRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.EconomyAdminActionPayload.TYPE, TeleportMenuPayloads.EconomyAdminActionPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.CreateShopRequestPayload.TYPE, TeleportMenuPayloads.CreateShopRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.ExpandShopLimitRequestPayload.TYPE, TeleportMenuPayloads.ExpandShopLimitRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.DeleteShopRequestPayload.TYPE, TeleportMenuPayloads.DeleteShopRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.RenameShopRequestPayload.TYPE, TeleportMenuPayloads.RenameShopRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.SetShopIconRequestPayload.TYPE, TeleportMenuPayloads.SetShopIconRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.AddShopGoodRequestPayload.TYPE, TeleportMenuPayloads.AddShopGoodRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.BuyShopGoodRequestPayload.TYPE, TeleportMenuPayloads.BuyShopGoodRequestPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.RenameBoxShopPayload.TYPE, TeleportMenuPayloads.RenameBoxShopPayload.STREAM_CODEC);
		registerC2S(TeleportMenuPayloads.QueryBoxShopHudPayload.TYPE, TeleportMenuPayloads.QueryBoxShopHudPayload.STREAM_CODEC);
	}

	private static void clearBoxHud() {
		BOX_HUD_NAME = "";
		BOX_HUD_GOODS = 0;
		BOX_HUD_EXPIRE_AT = 0L;
		LAST_BOX_QUERY_POS = null;
		NEXT_BOX_QUERY_AT = 0L;
	}

	private static void tickBoxHudQuery(Minecraft client) {
		if (client.player == null || client.level == null) {
			clearBoxHud();
			return;
		}

		if (client.hitResult == null || client.hitResult.getType() != HitResult.Type.BLOCK) {
			LAST_BOX_QUERY_POS = null;
			if (client.level.getGameTime() > BOX_HUD_EXPIRE_AT) {
				clearBoxHud();
			}
			return;
		}

		BlockHitResult blockHit = (BlockHitResult) client.hitResult;
		BlockPos pos = blockHit.getBlockPos();
		if (!client.level.getBlockState(pos).is(Blocks.ENDER_CHEST)) {
			LAST_BOX_QUERY_POS = null;
			if (client.level.getGameTime() > BOX_HUD_EXPIRE_AT) {
				clearBoxHud();
			}
			return;
		}

		if (client.player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 144D) {
			clearBoxHud();
			return;
		}

		long now = client.level.getGameTime();
		boolean changed = LAST_BOX_QUERY_POS == null || !LAST_BOX_QUERY_POS.equals(pos);
		if (changed || now >= NEXT_BOX_QUERY_AT) {
			ClientPlayNetworking.send(new TeleportMenuPayloads.QueryBoxShopHudPayload(pos.getX(), pos.getY(), pos.getZ()));
			LAST_BOX_QUERY_POS = pos.immutable();
			NEXT_BOX_QUERY_AT = now + 12L;
		}
	}

	private static void renderBoxHud(net.minecraft.client.gui.GuiGraphics guiGraphics) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || client.options.hideGui) {
			return;
		}
		if (BOX_HUD_NAME.isBlank() || client.level.getGameTime() > BOX_HUD_EXPIRE_AT) {
			return;
		}

		String title = BOX_HUD_NAME;
		String subtitle = "商品数量: " + BOX_HUD_GOODS;
		int titleWidth = client.font.width(title);
		int subWidth = client.font.width(subtitle);
		int panelWidth = Math.max(titleWidth, subWidth) + 16;
		int panelHeight = 28;

		int x = (client.getWindow().getGuiScaledWidth() - panelWidth) / 2;
		int y = (client.getWindow().getGuiScaledHeight() / 2) + 18;

		guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, 0xA0101010);
		guiGraphics.drawCenteredString(client.font, Component.literal(title), x + panelWidth / 2, y + 5, 0xFFFFFF);
		guiGraphics.drawCenteredString(client.font, Component.literal(subtitle), x + panelWidth / 2, y + 16, 0xD0D8E8);
	}

	private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
		try {
			PayloadTypeRegistry.playS2C().register(type, codec);
		} catch (IllegalArgumentException ignored) {
		}
	}

	private static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
		try {
			PayloadTypeRegistry.playC2S().register(type, codec);
		} catch (IllegalArgumentException ignored) {
		}
	}
}

