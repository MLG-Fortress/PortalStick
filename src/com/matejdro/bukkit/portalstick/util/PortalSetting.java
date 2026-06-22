package com.matejdro.bukkit.portalstick.util;

import java.util.Arrays;

public enum PortalSetting {
	
	ENABLE_PORTALS("enable-portals", true, true),
	TELEPORT_VEHICLES("teleport-vehicles", true, true),
	TELEPORT_LIQUIDS("teleport-liquids", true, true),
	INFINITE_DISPENSERS("infinite-dispensers", true, true),
	CHECK_WORLDGUARD("obey-worldguard-permissions", false, true),
	TRANSPARENT_BLOCKS("transparent-blocks", Arrays.asList(new String[]{"AIR", "WATER", "STATIONARY_WATER", "LAVA", "STATIONARY_LAVA", "GLASS", "WOODEN_DOOR", "IRON_DOOR", "TRAP_DOOR", "LADDER", "THIN_GLASS", "TRIPWIRE"}), false),
	PORTAL_BLOCKS("portallable-blocks", Arrays.asList(new String[]{"IRON_BLOCK"}), false),
	ALL_BLOCKS_PORTAL("all-blocks-allow-portals", false, true),
	UNIQUE_INVENTORY("unique-inventory", false, true),
	UNIQUE_INVENTORY_ITEMS("unique-inventory-items", Arrays.asList(new Integer[]{280,1}), false),
	VELOCITY_MULTIPLIER("velocity-multiplier", 1.0, true),
	PREVENT_PORTAL_THROUGH_PORTAL("prevent-creating-portal-through-portal", false, true),
	PREVENT_PORTAL_CLOSED_DOOR("prevent-creating-portal-through-closed-door", true, true),
	ENABLE_SOUNDS("enable-sounds", true, true),
	PROTECT_PORTALS_FROM_TNT("protect-portals-from-tnt", false, true),
	ENABLE_REDSTONE_TRANSFER("enable-transferring-redstone-current", true, true),
	ENABLE_PISTON_BLOCK_TELEPORT("enable-teleporting-blocks-moved-by-pistons", true, true),
	LOCATION("location", "world:0,0,0:0,0,0");
	
	private final String yaml;
	private final Object def;
	private final boolean editable;
	
	private PortalSetting(String yaml, Object def) {
		this.yaml = yaml;
		this.def = def;
		this.editable = false;
	}
	
	private PortalSetting(String yaml, Object def, boolean editable) {
		this.yaml = yaml;
		this.def = def;
		this.editable = editable;
	}
	
	public String getYaml() {
		return yaml;
	}
	
	public Object getDefault() {
		return def;
	}
	
	public boolean getEditable() {
		return editable;
	}
}
