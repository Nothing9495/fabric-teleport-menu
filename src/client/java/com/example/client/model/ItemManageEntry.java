package com.example.client.model;

import net.minecraft.world.item.ItemStack;

public record ItemManageEntry(String registryId, String displayName, String materialLabel, String targetItemId, ItemStack iconStack) {
	public ItemManageEntry {
		registryId = registryId == null ? "" : registryId;
		displayName = displayName == null ? "" : displayName;
		materialLabel = materialLabel == null ? "" : materialLabel;
		targetItemId = targetItemId == null ? "" : targetItemId;
		iconStack = iconStack == null ? ItemStack.EMPTY : iconStack.copy();
	}

	public boolean disableSupported() {
		return !this.targetItemId.isBlank();
	}
}
