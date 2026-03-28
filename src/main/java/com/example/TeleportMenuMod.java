package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.example.network.TeleportMenuPayloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.commands.Commands.literal;

public class TeleportMenuMod implements ModInitializer {
	public static final String MOD_ID = "teleportmenu";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Map<UUID, List<HomePoint>> PLAYER_HOMES = new ConcurrentHashMap<>();
	private static final Map<UUID, List<TerritoryPoint>> PLAYER_TERRITORIES = new ConcurrentHashMap<>();
	private static final Map<UUID, TerritorySelection> TERRITORY_SELECTIONS = new ConcurrentHashMap<>();
	private static final Map<UUID, TerritoryHudState> TERRITORY_HUD_STATES = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> LAST_DENY_ACTIONBAR_TICK = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> PLAYER_UI_PROTOCOLS = new ConcurrentHashMap<>();
	private static final Set<String> DISABLED_ITEM_IDS = ConcurrentHashMap.newKeySet();
	private static final int STABLE_UI_PROTOCOL = 1;
	private static final int UNKNOWN_UI_PROTOCOL = 0;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String DATA_DIR_NAME = "teleport-menu-mod";
	private static final String DATA_FILE_NAME = "data.json";
	private static MinecraftServer ACTIVE_SERVER = null;

	private static final DustParticleOptions TERRITORY_SELECTION_PARTICLE = new DustParticleOptions(new Vector3f(0.25F, 1.0F, 0.25F), 1.0F);
	private static long SERVER_TICK = 0L;

	private static final List<Item> HOME_ICON_POOL = List.of(
		Items.COMPASS, Items.CLOCK, Items.RED_BED, Items.OAK_DOOR, Items.CHEST, Items.TORCH,
		Items.CAMPFIRE, Items.BREAD, Items.GOLDEN_APPLE, Items.DIAMOND, Items.ENDER_PEARL, Items.BOOK
	);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(TeleportMenuPayloads.OpenMenuPayload.TYPE, TeleportMenuPayloads.OpenMenuPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(TeleportMenuPayloads.HomeListPayload.TYPE, TeleportMenuPayloads.HomeListPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(TeleportMenuPayloads.DisabledItemListPayload.TYPE, TeleportMenuPayloads.DisabledItemListPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.TeleportRequestPayload.TYPE, TeleportMenuPayloads.TeleportRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.TeleportHomeRequestPayload.TYPE, TeleportMenuPayloads.TeleportHomeRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.TeleportTerritoryRequestPayload.TYPE, TeleportMenuPayloads.TeleportTerritoryRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.CreateHomeRequestPayload.TYPE, TeleportMenuPayloads.CreateHomeRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.CreateTerritoryRequestPayload.TYPE, TeleportMenuPayloads.CreateTerritoryRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.CancelTerritoryCreatePayload.TYPE, TeleportMenuPayloads.CancelTerritoryCreatePayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.UpdateTerritoryPermissionPayload.TYPE, TeleportMenuPayloads.UpdateTerritoryPermissionPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.UpdateTerritoryWhitelistPayload.TYPE, TeleportMenuPayloads.UpdateTerritoryWhitelistPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.ClientHelloPayload.TYPE, TeleportMenuPayloads.ClientHelloPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.DeleteHomeRequestPayload.TYPE, TeleportMenuPayloads.DeleteHomeRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.RenameHomeRequestPayload.TYPE, TeleportMenuPayloads.RenameHomeRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportMenuPayloads.DisableItemRequestPayload.TYPE, TeleportMenuPayloads.DisableItemRequestPayload.STREAM_CODEC);

		PlayerBlockBreakEvents.BEFORE.register(TeleportMenuMod::onBeforeBlockBreak);
		UseBlockCallback.EVENT.register(TeleportMenuMod::onUseBlock);
		AttackEntityCallback.EVENT.register(TeleportMenuMod::onAttackEntity);
		UseItemCallback.EVENT.register(TeleportMenuMod::onAnyItemUse);
		UseItemCallback.EVENT.register(TeleportMenuMod::onClockUse);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("clock").executes(context -> giveClock(context.getSource())));
			dispatcher.register(literal("territ")
				.executes(context -> showTerritoryUsage(context.getSource()))
				.then(literal("a").executes(context -> setTerritoryPoint(context.getSource(), true)))
				.then(literal("b").executes(context -> setTerritoryPoint(context.getSource(), false))));
		});

		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.TeleportRequestPayload.TYPE, (payload, context) -> handleTeleportRequest(context.player(), payload.targetId()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.TeleportHomeRequestPayload.TYPE, (payload, context) -> handleTeleportHomeRequest(context.player(), payload.homeId()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.TeleportTerritoryRequestPayload.TYPE, (payload, context) -> handleTeleportTerritoryRequest(context.player(), payload.territoryId()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.CreateHomeRequestPayload.TYPE, (payload, context) -> handleCreateHomeRequest(context.player(), payload.name()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.CreateTerritoryRequestPayload.TYPE, (payload, context) -> handleCreateTerritoryRequest(context.player(), payload.name()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.CancelTerritoryCreatePayload.TYPE, (payload, context) -> handleCancelTerritoryCreate(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.UpdateTerritoryPermissionPayload.TYPE, (payload, context) -> handleUpdateTerritoryPermission(context.player(), payload.territoryId(), payload.permissionKey(), payload.allowed()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.UpdateTerritoryWhitelistPayload.TYPE, (payload, context) -> handleUpdateTerritoryWhitelist(context.player(), payload.territoryId(), payload.playerId(), payload.allowed()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.ClientHelloPayload.TYPE, (payload, context) -> PLAYER_UI_PROTOCOLS.put(context.player().getUUID(), payload.protocol()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.DeleteHomeRequestPayload.TYPE, (payload, context) -> handleDeleteHomeRequest(context.player(), payload.homeId()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.RenameHomeRequestPayload.TYPE, (payload, context) -> handleRenameHomeRequest(context.player(), payload.homeId(), payload.name()));
		ServerPlayNetworking.registerGlobalReceiver(TeleportMenuPayloads.DisableItemRequestPayload.TYPE, (payload, context) -> handleDisableItemRequest(context.player(), payload.itemId(), payload.disable()));

		ServerLifecycleEvents.SERVER_STARTED.register(TeleportMenuMod::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPING.register(TeleportMenuMod::onServerStopping);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PLAYER_UI_PROTOCOLS.remove(handler.player.getUUID()));
		ServerTickEvents.END_SERVER_TICK.register(TeleportMenuMod::onServerTick);
		LOGGER.info("Teleport Menu 2.3 initialized");
	}

	private static void onServerStarted(MinecraftServer server) {
		ACTIVE_SERVER = server;
		loadPersistentData(server);
		replaceDisabledForAllPlayers(server);
	}

	private static void onServerStopping(MinecraftServer server) {
		savePersistentData(server);
		ACTIVE_SERVER = null;
		TERRITORY_SELECTIONS.clear();
		TERRITORY_HUD_STATES.clear();
		LAST_DENY_ACTIONBAR_TICK.clear();
		PLAYER_UI_PROTOCOLS.clear();
	}

	private static Path dataDir(MinecraftServer server) {
		return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(DATA_DIR_NAME);
	}

	private static Path dataFile(MinecraftServer server) {
		return dataDir(server).resolve(DATA_FILE_NAME);
	}

	private static void persistDataIfPossible() {
		if (ACTIVE_SERVER != null) {
			savePersistentData(ACTIVE_SERVER);
		}
	}

	private static void loadPersistentData(MinecraftServer server) {
		PLAYER_HOMES.clear();
		PLAYER_TERRITORIES.clear();
		DISABLED_ITEM_IDS.clear();

		Path file = dataFile(server);
		if (!Files.exists(file)) {
			return;
		}

		try {
			String json = Files.readString(file, StandardCharsets.UTF_8);
			JsonElement rootElement = JsonParser.parseString(json);
			if (!rootElement.isJsonObject()) {
				LOGGER.warn("Teleport menu data file is not a JSON object: {}", file);
				return;
			}

			JsonObject root = rootElement.getAsJsonObject();
			readDisabledItems(root);
			readHomes(root);
			readTerritories(root);
			LOGGER.info("Loaded teleport menu data from {}", file);
		} catch (Exception error) {
			LOGGER.error("Failed to load teleport menu data from {}", file, error);
		}
	}

	private static void savePersistentData(MinecraftServer server) {
		try {
			Path dir = dataDir(server);
			Files.createDirectories(dir);
			Path file = dataFile(server);

			JsonObject root = new JsonObject();
			root.addProperty("schema", 1);
			root.add("disabledItems", writeDisabledItems());
			root.add("homes", writeHomes());
			root.add("territories", writeTerritories());

			Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException error) {
			LOGGER.error("Failed to save teleport menu data", error);
		}
	}

	private static JsonArray writeDisabledItems() {
		JsonArray array = new JsonArray();
		getDisabledItemIdList().forEach(array::add);
		return array;
	}

	private static JsonArray writeHomes() {
		JsonArray array = new JsonArray();
		for (Map.Entry<UUID, List<HomePoint>> entry : PLAYER_HOMES.entrySet()) {
			for (HomePoint home : entry.getValue()) {
				JsonObject homeObj = new JsonObject();
				homeObj.addProperty("owner", entry.getKey().toString());
				homeObj.addProperty("id", home.id().toString());
				homeObj.addProperty("name", home.name());
				homeObj.addProperty("x", home.x());
				homeObj.addProperty("y", home.y());
				homeObj.addProperty("z", home.z());
				homeObj.addProperty("iconItemId", home.iconItemId());
				array.add(homeObj);
			}
		}
		return array;
	}

	private static JsonArray writeTerritories() {
		JsonArray array = new JsonArray();
		for (Map.Entry<UUID, List<TerritoryPoint>> entry : PLAYER_TERRITORIES.entrySet()) {
			for (TerritoryPoint territory : entry.getValue()) {
				JsonObject territoryObj = new JsonObject();
				territoryObj.addProperty("owner", entry.getKey().toString());
				territoryObj.addProperty("id", territory.id().toString());
				territoryObj.addProperty("ownerName", territory.ownerName());
				territoryObj.addProperty("name", territory.name());
				territoryObj.addProperty("dimensionId", territory.dimensionId().toString());
				territoryObj.addProperty("minX", territory.minX());
				territoryObj.addProperty("minY", territory.minY());
				territoryObj.addProperty("minZ", territory.minZ());
				territoryObj.addProperty("maxX", territory.maxX());
				territoryObj.addProperty("maxY", territory.maxY());
				territoryObj.addProperty("maxZ", territory.maxZ());
				territoryObj.addProperty("iconItemId", territory.iconItemId());
				territoryObj.addProperty("buildBreakAllowed", territory.buildBreakAllowed());
				territoryObj.addProperty("containerAllowed", territory.containerAllowed());
				territoryObj.addProperty("interactAllowed", territory.interactAllowed());
				territoryObj.addProperty("damageEntitiesAllowed", territory.damageEntitiesAllowed());
				territoryObj.addProperty("blockUseAllowed", territory.blockUseAllowed());
				JsonArray whitelistArray = new JsonArray();
				territory.whitelistPlayerIds().forEach(uuid -> whitelistArray.add(uuid.toString()));
				territoryObj.add("whitelist", whitelistArray);
				array.add(territoryObj);
			}
		}
		return array;
	}

	private static void readDisabledItems(JsonObject root) {
		if (!root.has("disabledItems") || !root.get("disabledItems").isJsonArray()) {
			return;
		}

		for (JsonElement element : root.getAsJsonArray("disabledItems")) {
			if (element.isJsonPrimitive()) {
				String itemId = element.getAsString();
				if (!itemId.isBlank()) {
					DISABLED_ITEM_IDS.add(itemId);
				}
			}
		}
	}

	private static void readHomes(JsonObject root) {
		if (!root.has("homes") || !root.get("homes").isJsonArray()) {
			return;
		}

		for (JsonElement element : root.getAsJsonArray("homes")) {
			if (!element.isJsonObject()) {
				continue;
			}

			JsonObject obj = element.getAsJsonObject();
			try {
				UUID owner = UUID.fromString(obj.get("owner").getAsString());
				HomePoint home = new HomePoint(
					UUID.fromString(obj.get("id").getAsString()),
					obj.get("name").getAsString(),
					obj.get("x").getAsInt(),
					obj.get("y").getAsInt(),
					obj.get("z").getAsInt(),
					obj.has("iconItemId") ? obj.get("iconItemId").getAsString() : "minecraft:compass"
				);
				PLAYER_HOMES.computeIfAbsent(owner, key -> new ArrayList<>()).add(home);
			} catch (Exception ignored) {
				// Skip invalid entry
			}
		}
	}

	private static void readTerritories(JsonObject root) {
		if (!root.has("territories") || !root.get("territories").isJsonArray()) {
			return;
		}

		for (JsonElement element : root.getAsJsonArray("territories")) {
			if (!element.isJsonObject()) {
				continue;
			}

			JsonObject obj = element.getAsJsonObject();
			try {
				UUID owner = UUID.fromString(obj.get("owner").getAsString());
				TerritoryPoint territory = new TerritoryPoint(
					UUID.fromString(obj.get("id").getAsString()),
					owner,
					obj.has("ownerName") ? obj.get("ownerName").getAsString() : "",
					obj.get("name").getAsString(),
					ResourceLocation.parse(obj.get("dimensionId").getAsString()),
					obj.get("minX").getAsInt(),
					obj.get("minY").getAsInt(),
					obj.get("minZ").getAsInt(),
					obj.get("maxX").getAsInt(),
					obj.get("maxY").getAsInt(),
					obj.get("maxZ").getAsInt(),
					obj.has("iconItemId") ? obj.get("iconItemId").getAsString() : "minecraft:grass_block",
					!obj.has("buildBreakAllowed") || obj.get("buildBreakAllowed").getAsBoolean(),
					!obj.has("containerAllowed") || obj.get("containerAllowed").getAsBoolean(),
					!obj.has("interactAllowed") || obj.get("interactAllowed").getAsBoolean(),
					!obj.has("damageEntitiesAllowed") || obj.get("damageEntitiesAllowed").getAsBoolean(),
					!obj.has("blockUseAllowed") || obj.get("blockUseAllowed").getAsBoolean(),
					readWhitelist(obj)
				);
				PLAYER_TERRITORIES.computeIfAbsent(owner, key -> new ArrayList<>()).add(territory);
			} catch (Exception ignored) {
				// Skip invalid entry
			}
		}
	}

	private static Set<UUID> readWhitelist(JsonObject obj) {
		Set<UUID> whitelist = ConcurrentHashMap.newKeySet();
		if (!obj.has("whitelist") || !obj.get("whitelist").isJsonArray()) {
			return whitelist;
		}

		for (JsonElement entry : obj.getAsJsonArray("whitelist")) {
			if (!entry.isJsonPrimitive()) {
				continue;
			}
			try {
				whitelist.add(UUID.fromString(entry.getAsString()));
			} catch (IllegalArgumentException ignored) {
				// ignore invalid uuid
			}
		}
		return whitelist;
	}

	private static InteractionResultHolder<ItemStack> onAnyItemUse(Player player, Level level, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (heldItem.isEmpty() || !isDisabledItem(heldItem.getItem())) {
			return InteractionResultHolder.pass(heldItem);
		}

		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			replaceDisabledInInventory(serverPlayer);
			sendError(serverPlayer, "message.teleportmenu.item_disabled_use_blocked");
		}

		return InteractionResultHolder.fail(player.getItemInHand(hand));
	}

	private static InteractionResultHolder<ItemStack> onClockUse(Player player, Level level, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (!heldItem.is(Items.CLOCK)) {
			return InteractionResultHolder.pass(heldItem);
		}

		if (level.isClientSide) {
			return InteractionResultHolder.success(heldItem);
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.pass(heldItem);
		}

		if (!ServerPlayNetworking.canSend(serverPlayer, TeleportMenuPayloads.OpenMenuPayload.TYPE)) {
			sendError(serverPlayer, "message.teleportmenu.client_missing");
			return InteractionResultHolder.consume(heldItem);
		}
		int uiProtocol = PLAYER_UI_PROTOCOLS.getOrDefault(serverPlayer.getUUID(), UNKNOWN_UI_PROTOCOL);
		if (uiProtocol < STABLE_UI_PROTOCOL) {
			sendError(serverPlayer, "message.teleportmenu.client_outdated");
			return InteractionResultHolder.consume(heldItem);
		}

		List<TeleportMenuPayloads.PlayerTarget> targets = serverPlayer.server
			.getPlayerList()
			.getPlayers()
			.stream()
			.filter(other -> !other.getUUID().equals(serverPlayer.getUUID()))
			.map(other -> new TeleportMenuPayloads.PlayerTarget(other.getUUID(), other.getGameProfile().getName()))
			.toList();

		TerritorySelection selection = TERRITORY_SELECTIONS.get(serverPlayer.getUUID());
		boolean selectionReady = selection != null && selection.pointA() != null && selection.pointB() != null
			&& selection.dimensionId().equals(serverPlayer.serverLevel().dimension().location());

		BlockPos a = selectionReady ? selection.pointA() : BlockPos.ZERO;
		BlockPos b = selectionReady ? selection.pointB() : BlockPos.ZERO;

		boolean admin = isAdmin(serverPlayer);
		ServerPlayNetworking.send(serverPlayer, new TeleportMenuPayloads.OpenMenuPayload(
			targets,
			getHomeTargets(serverPlayer),
			getTerritoryTargets(serverPlayer),
			admin,
			admin ? getDisabledItemIdList() : List.of(),
			selectionReady,
			a.getX(),
			a.getY(),
			a.getZ(),
			b.getX(),
			b.getY(),
			b.getZ()
		));

		return InteractionResultHolder.consume(heldItem);
	}

	private static boolean onBeforeBlockBreak(Level level, Player player, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, BlockEntity blockEntity) {
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
			return true;
		}

		return checkTerritoryPermission(serverPlayer, pos, TerritoryPermission.BUILD_BREAK);
	}

	private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		BlockPos pos = hitResult.getBlockPos();
		Block block = level.getBlockState(pos).getBlock();
		ItemStack held = player.getItemInHand(hand);

		if (held.getItem() instanceof BlockItem) {
			if (!checkTerritoryPermission(serverPlayer, pos, TerritoryPermission.BUILD_BREAK)) {
				return InteractionResult.FAIL;
			}
		}

		if (isContainerBlock(level, pos)) {
			if (!checkTerritoryPermission(serverPlayer, pos, TerritoryPermission.CONTAINER)) {
				return InteractionResult.FAIL;
			}
		}

		if (isRedstoneInteractBlock(block)) {
			if (!checkTerritoryPermission(serverPlayer, pos, TerritoryPermission.INTERACT)) {
				return InteractionResult.FAIL;
			}
		}

		if (isBlockUseControlled(block)) {
			if (!checkTerritoryPermission(serverPlayer, pos, TerritoryPermission.BLOCK_USE)) {
				return InteractionResult.FAIL;
			}
		}

		return InteractionResult.PASS;
	}

	private static InteractionResult onAttackEntity(Player player, Level level, InteractionHand hand, net.minecraft.world.entity.Entity entity, net.minecraft.world.phys.EntityHitResult hitResult) {
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		if (!isProtectedEntity(entity)) {
			return InteractionResult.PASS;
		}

		if (!checkTerritoryPermission(serverPlayer, entity.blockPosition(), TerritoryPermission.DAMAGE_ENTITIES)) {
			return InteractionResult.FAIL;
		}

		return InteractionResult.PASS;
	}

	private static int giveClock(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		if (player.getInventory().contains(new ItemStack(Items.CLOCK))) {
			sendError(source, "message.teleportmenu.clock_already_have");
			return 0;
		}

		ItemStack clock = new ItemStack(Items.CLOCK);
		if (!player.getInventory().add(clock)) {
			player.drop(clock, false);
		}

		sendSuccess(source, "message.teleportmenu.clock_given");
		return 1;
	}

	private static int showTerritoryUsage(CommandSourceStack source) {
		sendWarn(source, "message.teleportmenu.territory_usage_1");
		sendWarn(source, "message.teleportmenu.territory_usage_2");
		sendWarn(source, "message.teleportmenu.territory_usage_3");
		return 1;
	}

	private static int setTerritoryPoint(CommandSourceStack source, boolean setA) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		UUID playerId = player.getUUID();
		ResourceLocation dimensionId = player.serverLevel().dimension().location();
		BlockPos pos = player.blockPosition();

		if (setA) {
			TERRITORY_SELECTIONS.put(playerId, new TerritorySelection(dimensionId, pos, null));
			sendWarn(source, "message.teleportmenu.territory_set_a", pos.getX(), pos.getY(), pos.getZ());
			sendWarn(source, "message.teleportmenu.territory_step_next_b");
			return 1;
		}

		TerritorySelection selection = TERRITORY_SELECTIONS.get(playerId);
		if (selection == null || selection.pointA() == null) {
			sendWarn(source, "message.teleportmenu.territory_need_a_first");
			return 0;
		}

		if (!selection.dimensionId().equals(dimensionId)) {
			sendWarn(source, "message.teleportmenu.territory_dimension_mismatch");
			return 0;
		}

		TERRITORY_SELECTIONS.put(playerId, new TerritorySelection(dimensionId, selection.pointA(), pos));
		sendWarn(source, "message.teleportmenu.territory_set_b", pos.getX(), pos.getY(), pos.getZ());
		sendWarn(source, "message.teleportmenu.territory_open_clock_to_create");
		return 1;
	}

	private static void handleTeleportRequest(ServerPlayer source, UUID targetId) {
		if (targetId == null || targetId.equals(source.getUUID())) {
			sendError(source, "message.teleportmenu.invalid_target");
			return;
		}

		ServerPlayer target = source.server.getPlayerList().getPlayer(targetId);
		if (target == null) {
			sendError(source, "message.teleportmenu.target_offline");
			return;
		}

		source.teleportTo(target.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
		sendSuccess(source, "message.teleportmenu.teleport_success_player", target.getDisplayName());
	}

	private static void handleTeleportHomeRequest(ServerPlayer source, UUID homeId) {
		if (homeId == null) {
			sendError(source, "message.teleportmenu.home_not_found");
			return;
		}

		HomePoint home = PLAYER_HOMES
			.getOrDefault(source.getUUID(), List.of())
			.stream()
			.filter(candidate -> candidate.id().equals(homeId))
			.findFirst()
			.orElse(null);

		if (home == null) {
			sendError(source, "message.teleportmenu.home_not_found");
			return;
		}

		source.teleportTo(source.serverLevel(), home.x() + 0.5, home.y(), home.z() + 0.5, source.getYRot(), source.getXRot());
		sendSuccess(source, "message.teleportmenu.teleport_success_home", home.name(), home.x(), home.y(), home.z());
	}

	private static void handleTeleportTerritoryRequest(ServerPlayer source, UUID territoryId) {
		if (territoryId == null) {
			sendError(source, "message.teleportmenu.territory_not_found");
			return;
		}

		TerritoryPoint territory = PLAYER_TERRITORIES
			.getOrDefault(source.getUUID(), List.of())
			.stream()
			.filter(candidate -> candidate.id().equals(territoryId))
			.findFirst()
			.orElse(null);

		if (territory == null) {
			sendError(source, "message.teleportmenu.territory_not_found");
			return;
		}

		ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, territory.dimensionId());
		ServerLevel targetLevel = source.server.getLevel(levelKey);
		if (targetLevel == null) {
			sendError(source, "message.teleportmenu.territory_dimension_missing");
			return;
		}

		double targetX = ((double) territory.minX() + (double) territory.maxX() + 1D) / 2D;
		double targetY = territory.maxY() + 1D;
		double targetZ = ((double) territory.minZ() + (double) territory.maxZ() + 1D) / 2D;

		source.teleportTo(targetLevel, targetX, targetY, targetZ, source.getYRot(), source.getXRot());
		sendSuccess(source, "message.teleportmenu.teleport_success_territory", territory.name());
	}

	private static void handleCreateHomeRequest(ServerPlayer source, String rawName) {
		String name = rawName == null ? "" : rawName.trim();
		if (name.isEmpty()) {
			sendError(source, "message.teleportmenu.home_name_empty");
			return;
		}

		if (name.length() > 20) {
			name = name.substring(0, 20);
		}

		List<HomePoint> homes = PLAYER_HOMES.computeIfAbsent(source.getUUID(), key -> new ArrayList<>());
		final String finalName = name;
		boolean exists = homes.stream().anyMatch(home -> home.name().equalsIgnoreCase(finalName));
		if (exists) {
			sendError(source, "message.teleportmenu.home_name_exists", finalName);
			return;
		}

		int x = source.blockPosition().getX();
		int y = source.blockPosition().getY();
		int z = source.blockPosition().getZ();
		String iconItemId = BuiltInRegistries.ITEM.getKey(randomHomeIcon(source)).toString();

		homes.add(new HomePoint(UUID.randomUUID(), finalName, x, y, z, iconItemId));
		persistDataIfPossible();
		sendSuccess(source, "message.teleportmenu.home_created", finalName, x, y, z);
		syncHomesToClient(source);
	}

	private static void handleCreateTerritoryRequest(ServerPlayer source, String rawName) {
		TerritorySelection selection = TERRITORY_SELECTIONS.get(source.getUUID());
		if (selection == null || selection.pointA() == null || selection.pointB() == null) {
			sendError(source, "message.teleportmenu.territory_need_ab");
			return;
		}

		if (!selection.dimensionId().equals(source.serverLevel().dimension().location())) {
			sendError(source, "message.teleportmenu.territory_dimension_mismatch");
			return;
		}

		String name = rawName == null ? "" : rawName.trim();
		if (name.isEmpty()) {
			sendError(source, "message.teleportmenu.territory_name_empty");
			return;
		}

		if (name.length() > 20) {
			name = name.substring(0, 20);
		}

		List<TerritoryPoint> territories = PLAYER_TERRITORIES.computeIfAbsent(source.getUUID(), key -> new ArrayList<>());
		final String finalName = name;
		if (territories.stream().anyMatch(t -> t.name().equalsIgnoreCase(finalName))) {
			sendError(source, "message.teleportmenu.territory_name_exists", finalName);
			return;
		}

		BlockPos a = selection.pointA();
		BlockPos b = selection.pointB();
		int minX = Math.min(a.getX(), b.getX());
		int minY = Math.min(a.getY(), b.getY());
		int minZ = Math.min(a.getZ(), b.getZ());
		int maxX = Math.max(a.getX(), b.getX());
		int maxY = Math.max(a.getY(), b.getY());
		int maxZ = Math.max(a.getZ(), b.getZ());

		String iconItemId = BuiltInRegistries.ITEM.getKey(randomHomeIcon(source)).toString();
		territories.add(new TerritoryPoint(
			UUID.randomUUID(),
			source.getUUID(),
			source.getGameProfile().getName(),
			finalName,
			selection.dimensionId(),
			minX,
			minY,
			minZ,
			maxX,
			maxY,
			maxZ,
			iconItemId,
			true,
			true,
			true,
			true,
			true,
			ConcurrentHashMap.newKeySet()
		));

		TERRITORY_SELECTIONS.remove(source.getUUID());
		persistDataIfPossible();
		sendSuccess(source, "message.teleportmenu.territory_created", finalName, minX, minY, minZ, maxX, maxY, maxZ);
	}

	private static void handleCancelTerritoryCreate(ServerPlayer source) {
		if (TERRITORY_SELECTIONS.remove(source.getUUID()) != null) {
			sendWarn(source, "message.teleportmenu.territory_create_cancelled");
		} else {
			sendWarn(source, "message.teleportmenu.territory_nothing_to_cancel");
		}
	}

	private static void handleUpdateTerritoryPermission(ServerPlayer source, UUID territoryId, String permissionKey, boolean allowed) {
		TerritoryPoint territory = findOwnedTerritory(source, territoryId);
		if (territory == null) {
			sendError(source, "message.teleportmenu.territory_not_found");
			return;
		}

		TerritoryPermission permission = TerritoryPermission.fromKey(permissionKey);
		if (permission == null) {
			sendError(source, "message.teleportmenu.territory_permission_invalid");
			return;
		}

		applyTerritoryPermission(territory, permission, allowed);
		persistDataIfPossible();
		sendSuccess(
			source,
			"message.teleportmenu.territory_permission_updated",
			Component.translatable(permission.translationKey()),
			allowed ? Component.translatable("screen.teleportmenu.territories.allow") : Component.translatable("screen.teleportmenu.territories.deny")
		);
	}

	private static void handleUpdateTerritoryWhitelist(ServerPlayer source, UUID territoryId, UUID playerId, boolean allowed) {
		if (playerId == null || playerId.equals(source.getUUID())) {
			sendError(source, "message.teleportmenu.territory_whitelist_invalid_player");
			return;
		}

		TerritoryPoint territory = findOwnedTerritory(source, territoryId);
		if (territory == null) {
			sendError(source, "message.teleportmenu.territory_not_found");
			return;
		}

		if (allowed) {
			territory.whitelistPlayerIds().add(playerId);
		} else {
			territory.whitelistPlayerIds().remove(playerId);
		}
		persistDataIfPossible();
	}

	private static void handleDeleteHomeRequest(ServerPlayer source, UUID homeId) {
		if (homeId == null) {
			sendError(source, "message.teleportmenu.home_not_found");
			return;
		}

		List<HomePoint> homes = PLAYER_HOMES.get(source.getUUID());
		if (homes == null || homes.isEmpty()) {
			sendError(source, "message.teleportmenu.home_not_found");
			return;
		}

		HomePoint home = homes.stream().filter(candidate -> candidate.id().equals(homeId)).findFirst().orElse(null);
		if (home == null) {
			sendError(source, "message.teleportmenu.home_not_found");
			return;
		}

		homes.removeIf(candidate -> candidate.id().equals(homeId));
		persistDataIfPossible();
		sendSuccess(source, "message.teleportmenu.home_deleted", home.name());
		syncHomesToClient(source);
	}

	private static void handleRenameHomeRequest(ServerPlayer source, UUID homeId, String rawName) {
		if (homeId == null) {
			sendError(source, "message.teleportmenu.home_not_found");
			return;
		}

		String name = rawName == null ? "" : rawName.trim();
		if (name.isEmpty()) {
			sendError(source, "message.teleportmenu.home_name_empty");
			return;
		}

		if (name.length() > 20) {
			name = name.substring(0, 20);
		}

		List<HomePoint> homes = PLAYER_HOMES.get(source.getUUID());
		if (homes == null || homes.isEmpty()) {
			sendError(source, "message.teleportmenu.home_not_found");
			return;
		}

		int index = -1;
		for (int i = 0; i < homes.size(); i++) {
			if (homes.get(i).id().equals(homeId)) {
				index = i;
				break;
			}
		}

		if (index < 0) {
			sendError(source, "message.teleportmenu.home_not_found");
			return;
		}

		final String finalName = name;
		boolean duplicate = homes.stream().anyMatch(home -> !home.id().equals(homeId) && home.name().equalsIgnoreCase(finalName));
		if (duplicate) {
			sendError(source, "message.teleportmenu.home_name_exists", finalName);
			return;
		}

		HomePoint old = homes.get(index);
		homes.set(index, new HomePoint(old.id(), finalName, old.x(), old.y(), old.z(), old.iconItemId()));
		persistDataIfPossible();
		sendSuccess(source, "message.teleportmenu.home_renamed", old.name(), finalName);
		syncHomesToClient(source);
	}

	private static void handleDisableItemRequest(ServerPlayer source, String rawItemId, boolean disable) {
		if (!isAdmin(source)) {
			sendError(source, "message.teleportmenu.admin_no_permission");
			return;
		}

		String itemId = rawItemId == null ? "" : rawItemId.trim();
		if (itemId.isEmpty()) {
			sendError(source, "message.teleportmenu.item_disable_not_found");
			return;
		}

		ResourceLocation id;
		try {
			id = ResourceLocation.parse(itemId);
		} catch (IllegalArgumentException error) {
			sendError(source, "message.teleportmenu.item_disable_not_found");
			return;
		}

		Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
		if (item == null || item == Items.AIR || item == Items.GRASS_BLOCK) {
			sendError(source, "message.teleportmenu.item_disable_not_found");
			return;
		}

		String canonicalId = BuiltInRegistries.ITEM.getKey(item).toString();
		if (disable) {
			if (DISABLED_ITEM_IDS.contains(canonicalId)) {
				sendError(source, "message.teleportmenu.item_disable_already", canonicalId);
				return;
			}

			DISABLED_ITEM_IDS.add(canonicalId);
			persistDataIfPossible();
			disableRecipesForItem(source.server, item);
			replaceDisabledForAllPlayers(source.server);
			syncDisabledItemsToOperators(source.server);
			sendSuccess(source, "message.teleportmenu.item_disable_success", canonicalId);
		} else {
			if (!DISABLED_ITEM_IDS.contains(canonicalId)) {
				sendError(source, "message.teleportmenu.item_enable_already", canonicalId);
				return;
			}

			DISABLED_ITEM_IDS.remove(canonicalId);
			persistDataIfPossible();
			syncDisabledItemsToOperators(source.server);
			sendSuccess(source, "message.teleportmenu.item_enable_success", canonicalId);
		}
	}

	private static void onServerTick(MinecraftServer server) {
		SERVER_TICK++;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			replaceDisabledInInventory(player);
			updateTerritoryHud(player);
			if (SERVER_TICK % 5L == 0L) {
				showTerritorySelectionParticles(player);
			}
		}
	}

	private static void showTerritorySelectionParticles(ServerPlayer player) {
		TerritorySelection selection = TERRITORY_SELECTIONS.get(player.getUUID());
		if (selection == null || selection.pointA() == null || selection.pointB() == null) {
			return;
		}

		if (!selection.dimensionId().equals(player.serverLevel().dimension().location())) {
			return;
		}

		spawnSelectionBox(player, selection.pointA(), selection.pointB());
	}

	private static void spawnSelectionBox(ServerPlayer player, BlockPos a, BlockPos b) {
		ServerLevel level = player.serverLevel();
		int minX = Math.min(a.getX(), b.getX());
		int minY = Math.min(a.getY(), b.getY());
		int minZ = Math.min(a.getZ(), b.getZ());
		int maxX = Math.max(a.getX(), b.getX());
		int maxY = Math.max(a.getY(), b.getY());
		int maxZ = Math.max(a.getZ(), b.getZ());

		int maxSpan = Math.max(Math.max(maxX - minX, maxY - minY), maxZ - minZ);
		int step = Math.max(1, maxSpan / 48 + 1);

		for (int x = minX; x <= maxX; x += step) {
			spawnEdgeColumn(level, player, x, minY, maxY, minZ, maxZ, step);
		}
		for (int y = minY; y <= maxY; y += step) {
			spawnEdgeRow(level, player, minX, maxX, y, minZ, maxZ, step);
		}
		for (int z = minZ; z <= maxZ; z += step) {
			spawnEdgeDepth(level, player, minX, maxX, minY, maxY, z, step);
		}

		spawnBoxCorner(level, player, minX, minY, minZ);
		spawnBoxCorner(level, player, minX, minY, maxZ);
		spawnBoxCorner(level, player, minX, maxY, minZ);
		spawnBoxCorner(level, player, minX, maxY, maxZ);
		spawnBoxCorner(level, player, maxX, minY, minZ);
		spawnBoxCorner(level, player, maxX, minY, maxZ);
		spawnBoxCorner(level, player, maxX, maxY, minZ);
		spawnBoxCorner(level, player, maxX, maxY, maxZ);
	}

	private static void spawnEdgeColumn(ServerLevel level, ServerPlayer player, int x, int minY, int maxY, int minZ, int maxZ, int step) {
		for (int y = minY; y <= maxY; y += step) {
			spawnBoxCorner(level, player, x, y, minZ);
			spawnBoxCorner(level, player, x, y, maxZ);
		}
	}

	private static void spawnEdgeRow(ServerLevel level, ServerPlayer player, int minX, int maxX, int y, int minZ, int maxZ, int step) {
		for (int x = minX; x <= maxX; x += step) {
			spawnBoxCorner(level, player, x, y, minZ);
			spawnBoxCorner(level, player, x, y, maxZ);
		}
	}

	private static void spawnEdgeDepth(ServerLevel level, ServerPlayer player, int minX, int maxX, int minY, int maxY, int z, int step) {
		for (int x = minX; x <= maxX; x += step) {
			spawnBoxCorner(level, player, x, minY, z);
			spawnBoxCorner(level, player, x, maxY, z);
		}
	}

	private static void spawnBoxCorner(ServerLevel level, ServerPlayer player, int x, int y, int z) {
		level.sendParticles(player, TERRITORY_SELECTION_PARTICLE, true, x + 0.5D, y + 0.5D, z + 0.5D, 1, 0D, 0D, 0D, 0D);
	}

	private static void updateTerritoryHud(ServerPlayer player) {
		UUID playerId = player.getUUID();
		TerritoryPoint inTerritory = findTerritoryContaining(player);
		TerritoryHudState state = TERRITORY_HUD_STATES.get(playerId);

		if (inTerritory == null) {
			if (state != null) {
				if (SERVER_TICK < state.clearAtTick()) {
					player.displayClientMessage(Component.empty(), true);
				}
				TERRITORY_HUD_STATES.remove(playerId);
			}
			return;
		}

		if (state == null || !inTerritory.id().equals(state.territoryId())) {
			if (inTerritory.ownerId().equals(player.getUUID())) {
				player.displayClientMessage(Component.translatable("message.teleportmenu.territory_owner_welcome", inTerritory.name(), player.getGameProfile().getName()), true);
			} else {
				player.displayClientMessage(Component.translatable("message.teleportmenu.territory_entered", inTerritory.ownerName()), true);
			}
			TERRITORY_HUD_STATES.put(playerId, new TerritoryHudState(inTerritory.id(), SERVER_TICK + 40L));
			return;
		}

		if (SERVER_TICK >= state.clearAtTick()) {
			player.displayClientMessage(Component.empty(), true);
			TERRITORY_HUD_STATES.put(playerId, new TerritoryHudState(state.territoryId(), Long.MAX_VALUE));
		}
	}

	private static TerritoryPoint findTerritoryContaining(ServerPlayer player) {
		ResourceLocation currentDimension = player.serverLevel().dimension().location();
		BlockPos pos = player.blockPosition();

		for (List<TerritoryPoint> territories : PLAYER_TERRITORIES.values()) {
			for (TerritoryPoint territory : territories) {
				if (!territory.dimensionId().equals(currentDimension)) {
					continue;
				}
				if (territory.contains(pos.getX(), pos.getY(), pos.getZ())) {
					return territory;
				}
			}
		}

		return null;
	}

	private static boolean checkTerritoryPermission(ServerPlayer player, BlockPos pos, TerritoryPermission permission) {
		TerritoryPoint territory = findTerritoryContaining(player.serverLevel().dimension().location(), pos);
		if (territory == null) {
			return true;
		}

		if (territory.ownerId().equals(player.getUUID()) || territory.whitelistPlayerIds().contains(player.getUUID())) {
			return true;
		}

		boolean allowed = switch (permission) {
			case BUILD_BREAK -> territory.buildBreakAllowed();
			case CONTAINER -> territory.containerAllowed();
			case INTERACT -> territory.interactAllowed();
			case DAMAGE_ENTITIES -> territory.damageEntitiesAllowed();
			case BLOCK_USE -> territory.blockUseAllowed();
		};

		if (!allowed) {
			showNoPermissionActionbar(player);
		}
		return allowed;
	}

	private static TerritoryPoint findTerritoryContaining(ResourceLocation dimensionId, BlockPos pos) {
		for (List<TerritoryPoint> territories : PLAYER_TERRITORIES.values()) {
			for (TerritoryPoint territory : territories) {
				if (!territory.dimensionId().equals(dimensionId)) {
					continue;
				}
				if (territory.contains(pos.getX(), pos.getY(), pos.getZ())) {
					return territory;
				}
			}
		}
		return null;
	}

	private static void showNoPermissionActionbar(ServerPlayer player) {
		long last = LAST_DENY_ACTIONBAR_TICK.getOrDefault(player.getUUID(), -100L);
		if (SERVER_TICK - last < 10L) {
			return;
		}
		LAST_DENY_ACTIONBAR_TICK.put(player.getUUID(), SERVER_TICK);
		player.displayClientMessage(Component.translatable("message.teleportmenu.no_permission_action").withStyle(ChatFormatting.RED), true);
	}

	private static boolean isContainerBlock(Level level, BlockPos pos) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof Container) {
			return true;
		}

		Block block = level.getBlockState(pos).getBlock();
		return block instanceof CraftingTableBlock;
	}

	private static boolean isRedstoneInteractBlock(Block block) {
		return block instanceof LeverBlock || block instanceof ButtonBlock;
	}

	private static boolean isBlockUseControlled(Block block) {
		return block instanceof BedBlock
			|| block instanceof DoorBlock
			|| block instanceof TrapDoorBlock
			|| block instanceof FenceGateBlock
			|| block instanceof CraftingTableBlock;
	}

	private static boolean isProtectedEntity(net.minecraft.world.entity.Entity entity) {
		if (entity instanceof ArmorStand || entity instanceof ItemFrame || entity instanceof GlowItemFrame) {
			return true;
		}
		return entity instanceof Animal;
	}

	private static TerritoryPoint findOwnedTerritory(ServerPlayer owner, UUID territoryId) {
		if (territoryId == null) {
			return null;
		}
		return PLAYER_TERRITORIES
			.getOrDefault(owner.getUUID(), List.of())
			.stream()
			.filter(candidate -> candidate.id().equals(territoryId))
			.findFirst()
			.orElse(null);
	}

	private static void applyTerritoryPermission(TerritoryPoint territory, TerritoryPermission permission, boolean allowed) {
		switch (permission) {
			case BUILD_BREAK -> territory.setBuildBreakAllowed(allowed);
			case CONTAINER -> territory.setContainerAllowed(allowed);
			case INTERACT -> territory.setInteractAllowed(allowed);
			case DAMAGE_ENTITIES -> territory.setDamageEntitiesAllowed(allowed);
			case BLOCK_USE -> territory.setBlockUseAllowed(allowed);
		}
	}

	private static void replaceDisabledForAllPlayers(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			replaceDisabledInInventory(player);
		}
	}

	private static void replaceDisabledInInventory(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		boolean changed = false;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty() || !isDisabledItem(stack.getItem())) {
				continue;
			}

			int replacementCount = Math.max(1, Math.min(64, stack.getCount()));
			inventory.setItem(slot, new ItemStack(Blocks.GRASS_BLOCK, replacementCount));
			changed = true;
		}

		ItemStack carried = player.containerMenu.getCarried();
		if (!carried.isEmpty() && isDisabledItem(carried.getItem())) {
			player.containerMenu.setCarried(new ItemStack(Blocks.GRASS_BLOCK, Math.max(1, Math.min(64, carried.getCount()))));
			changed = true;
		}

		if (changed) {
			inventory.setChanged();
			player.containerMenu.broadcastChanges();
		}
	}

	private static void disableRecipesForItem(MinecraftServer server, Item disabledItem) {
		RegistryAccess registryAccess = server.registryAccess();
		List<RecipeHolder<?>> recipes = server.getRecipeManager().getRecipes().stream()
			.filter(recipe -> recipe.value().getResultItem(registryAccess).is(disabledItem))
			.toList();

		if (recipes.isEmpty()) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			player.getRecipeBook().removeRecipes(recipes, player);
		}
	}

	private static void syncDisabledItemsToOperators(MinecraftServer server) {
		List<String> disabledIds = getDisabledItemIdList();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!isAdmin(player) || !ServerPlayNetworking.canSend(player, TeleportMenuPayloads.DisabledItemListPayload.TYPE)) {
				continue;
			}
			ServerPlayNetworking.send(player, new TeleportMenuPayloads.DisabledItemListPayload(disabledIds));
		}
	}

	private static boolean isDisabledItem(Item item) {
		if (item == Items.AIR || item == Items.GRASS_BLOCK) {
			return false;
		}
		return DISABLED_ITEM_IDS.contains(BuiltInRegistries.ITEM.getKey(item).toString());
	}

	private static boolean isAdmin(ServerPlayer player) {
		return player.hasPermissions(2);
	}

	private static List<String> getDisabledItemIdList() {
		return DISABLED_ITEM_IDS.stream().sorted(Comparator.naturalOrder()).toList();
	}

	private static void syncHomesToClient(ServerPlayer player) {
		if (ServerPlayNetworking.canSend(player, TeleportMenuPayloads.HomeListPayload.TYPE)) {
			ServerPlayNetworking.send(player, new TeleportMenuPayloads.HomeListPayload(getHomeTargets(player)));
		}
	}

	private static List<TeleportMenuPayloads.HomeTarget> getHomeTargets(ServerPlayer player) {
		return PLAYER_HOMES
			.getOrDefault(player.getUUID(), List.of())
			.stream()
			.map(home -> new TeleportMenuPayloads.HomeTarget(home.id(), home.name(), home.x(), home.y(), home.z(), home.iconItemId()))
			.toList();
	}

	private static List<TeleportMenuPayloads.TerritoryTarget> getTerritoryTargets(ServerPlayer player) {
		return PLAYER_TERRITORIES
			.getOrDefault(player.getUUID(), List.of())
			.stream()
			.map(territory -> new TeleportMenuPayloads.TerritoryTarget(
				territory.id(),
				territory.name(),
				territory.ownerName(),
				territory.minX(),
				territory.minY(),
				territory.minZ(),
				territory.maxX(),
				territory.maxY(),
				territory.maxZ(),
				territory.iconItemId(),
				territory.buildBreakAllowed(),
				territory.containerAllowed(),
				territory.interactAllowed(),
				territory.damageEntitiesAllowed(),
				territory.blockUseAllowed(),
				List.copyOf(territory.whitelistPlayerIds())
			))
			.toList();
	}

	private static Item randomHomeIcon(ServerPlayer player) {
		return HOME_ICON_POOL.get(player.getRandom().nextInt(HOME_ICON_POOL.size()));
	}

	private static Component coloredTranslatable(ChatFormatting color, String key, Object... args) {
		return Component.translatable(key, args).withStyle(color);
	}

	private static void sendSuccess(ServerPlayer player, String key, Object... args) {
		player.sendSystemMessage(coloredTranslatable(ChatFormatting.GREEN, key, args));
	}

	private static void sendError(ServerPlayer player, String key, Object... args) {
		player.sendSystemMessage(coloredTranslatable(ChatFormatting.RED, key, args));
	}

	private static void sendWarn(ServerPlayer player, String key, Object... args) {
		player.sendSystemMessage(coloredTranslatable(ChatFormatting.YELLOW, key, args));
	}

	private static void sendSuccess(CommandSourceStack source, String key, Object... args) {
		source.sendSuccess(() -> coloredTranslatable(ChatFormatting.GREEN, key, args), false);
	}

	private static void sendError(CommandSourceStack source, String key, Object... args) {
		source.sendFailure(coloredTranslatable(ChatFormatting.RED, key, args));
	}

	private static void sendWarn(CommandSourceStack source, String key, Object... args) {
		source.sendSuccess(() -> coloredTranslatable(ChatFormatting.YELLOW, key, args), false);
	}

	private record HomePoint(UUID id, String name, int x, int y, int z, String iconItemId) {
	}

	private record TerritorySelection(ResourceLocation dimensionId, BlockPos pointA, BlockPos pointB) {
	}

	private record TerritoryHudState(UUID territoryId, long clearAtTick) {
	}

	private static final class TerritoryPoint {
		private final UUID id;
		private final UUID ownerId;
		private final String ownerName;
		private final String name;
		private final ResourceLocation dimensionId;
		private final int minX;
		private final int minY;
		private final int minZ;
		private final int maxX;
		private final int maxY;
		private final int maxZ;
		private final String iconItemId;
		private final Set<UUID> whitelistPlayerIds;
		private boolean buildBreakAllowed;
		private boolean containerAllowed;
		private boolean interactAllowed;
		private boolean damageEntitiesAllowed;
		private boolean blockUseAllowed;

		private TerritoryPoint(
			UUID id,
			UUID ownerId,
			String ownerName,
			String name,
			ResourceLocation dimensionId,
			int minX,
			int minY,
			int minZ,
			int maxX,
			int maxY,
			int maxZ,
			String iconItemId,
			boolean buildBreakAllowed,
			boolean containerAllowed,
			boolean interactAllowed,
			boolean damageEntitiesAllowed,
			boolean blockUseAllowed,
			Set<UUID> whitelistPlayerIds
		) {
			this.id = id;
			this.ownerId = ownerId;
			this.ownerName = ownerName == null ? "" : ownerName;
			this.name = name == null ? "" : name;
			this.dimensionId = dimensionId;
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxY = maxY;
			this.maxZ = maxZ;
			this.iconItemId = iconItemId == null ? "minecraft:grass_block" : iconItemId;
			this.buildBreakAllowed = buildBreakAllowed;
			this.containerAllowed = containerAllowed;
			this.interactAllowed = interactAllowed;
			this.damageEntitiesAllowed = damageEntitiesAllowed;
			this.blockUseAllowed = blockUseAllowed;
			this.whitelistPlayerIds = whitelistPlayerIds == null ? ConcurrentHashMap.newKeySet() : whitelistPlayerIds;
		}

		private UUID id() { return this.id; }
		private UUID ownerId() { return this.ownerId; }
		private String ownerName() { return this.ownerName; }
		private String name() { return this.name; }
		private ResourceLocation dimensionId() { return this.dimensionId; }
		private int minX() { return this.minX; }
		private int minY() { return this.minY; }
		private int minZ() { return this.minZ; }
		private int maxX() { return this.maxX; }
		private int maxY() { return this.maxY; }
		private int maxZ() { return this.maxZ; }
		private String iconItemId() { return this.iconItemId; }
		private boolean buildBreakAllowed() { return this.buildBreakAllowed; }
		private boolean containerAllowed() { return this.containerAllowed; }
		private boolean interactAllowed() { return this.interactAllowed; }
		private boolean damageEntitiesAllowed() { return this.damageEntitiesAllowed; }
		private boolean blockUseAllowed() { return this.blockUseAllowed; }
		private Set<UUID> whitelistPlayerIds() { return this.whitelistPlayerIds; }

		private void setBuildBreakAllowed(boolean value) { this.buildBreakAllowed = value; }
		private void setContainerAllowed(boolean value) { this.containerAllowed = value; }
		private void setInteractAllowed(boolean value) { this.interactAllowed = value; }
		private void setDamageEntitiesAllowed(boolean value) { this.damageEntitiesAllowed = value; }
		private void setBlockUseAllowed(boolean value) { this.blockUseAllowed = value; }

		private boolean contains(int x, int y, int z) {
			return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
		}
	}

	private enum TerritoryPermission {
		BUILD_BREAK("build_break", "screen.teleportmenu.territories.permission.build_break"),
		CONTAINER("container", "screen.teleportmenu.territories.permission.container"),
		INTERACT("interact", "screen.teleportmenu.territories.permission.interact"),
		DAMAGE_ENTITIES("damage_entities", "screen.teleportmenu.territories.permission.damage_entities"),
		BLOCK_USE("block_use", "screen.teleportmenu.territories.permission.block_use");

		private final String key;
		private final String translationKey;

		TerritoryPermission(String key, String translationKey) {
			this.key = key;
			this.translationKey = translationKey;
		}

		private String key() {
			return this.key;
		}

		private String translationKey() {
			return this.translationKey;
		}

		private static TerritoryPermission fromKey(String key) {
			if (key == null) {
				return null;
			}
			for (TerritoryPermission permission : values()) {
				if (permission.key.equalsIgnoreCase(key.trim())) {
					return permission;
				}
			}
			return null;
		}
	}
}
