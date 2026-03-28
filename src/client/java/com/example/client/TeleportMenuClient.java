package com.example.client;

import com.example.client.screen.TeleportMainScreen;
import com.example.client.screen.TeleportAdminScreen;
import com.example.client.screen.TeleportHomeScreen;
import com.example.client.screen.TeleportHomeCreateScreen;
import com.example.client.screen.TeleportHomeDeleteConfirmScreen;
import com.example.client.screen.TeleportHomeManageScreen;
import com.example.client.screen.TeleportItemManageScreen;
import com.example.client.screen.TeleportItemDisableConfirmScreen;
import com.example.client.screen.TeleportTerritoryCreateScreen;
import com.example.network.TeleportMenuPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TeleportMenuClient implements ClientModInitializer {
	public static final int UI_PROTOCOL_VERSION = 2;
	private static final Set<String> DISABLED_ITEM_IDS = new LinkedHashSet<>();

	public static Set<String> disabledItemIds() {
		return Set.copyOf(DISABLED_ITEM_IDS);
	}

	public static void setDisabledItemIds(List<String> ids) {
		DISABLED_ITEM_IDS.clear();
		DISABLED_ITEM_IDS.addAll(ids);
	}

	@Override
	public void onInitializeClient() {
		registerPayloadTypes();
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
			ClientPlayNetworking.send(new TeleportMenuPayloads.ClientHelloPayload(UI_PROTOCOL_VERSION)));

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
	}

	private static void registerPayloadTypes() {
		registerS2C(TeleportMenuPayloads.OpenMenuPayload.TYPE, TeleportMenuPayloads.OpenMenuPayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.HomeListPayload.TYPE, TeleportMenuPayloads.HomeListPayload.STREAM_CODEC);
		registerS2C(TeleportMenuPayloads.DisabledItemListPayload.TYPE, TeleportMenuPayloads.DisabledItemListPayload.STREAM_CODEC);

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
	}

	private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
		try {
			PayloadTypeRegistry.playS2C().register(type, codec);
		} catch (IllegalArgumentException ignored) {
			// Already registered by another mod instance; safe to ignore.
		}
	}

	private static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
		try {
			PayloadTypeRegistry.playC2S().register(type, codec);
		} catch (IllegalArgumentException ignored) {
			// Already registered by another mod instance; safe to ignore.
		}
	}
}
