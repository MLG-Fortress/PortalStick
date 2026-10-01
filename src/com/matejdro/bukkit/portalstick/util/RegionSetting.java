package com.matejdro.bukkit.portalstick.util;

import java.util.Arrays;

public enum RegionSetting {
	
	ENABLE_PORTALS("enable-portals", true, true),
	TRANSPARENT_BLOCKS("transparent-blocks", Arrays.asList(new String[]{"AIR", "WATER", "STATIONARY_WATER", "LAVA", "STATIONARY_LAVA", "GLASS", "WOODEN_DOOR", "IRON_DOOR", "TRAP_DOOR", "LADDER", "THIN_GLASS", "TRIPWIRE"}), false),
	PORTAL_BLOCKS("portallable-blocks", Arrays.asList(new String[]{"IRON_BLOCK"}), false),
	ALL_BLOCKS_PORTAL("all-blocks-allow-portals", false, true),
	VELOCITY_MULTIPLIER("velocity-multiplier", 1.0, true),
	PREVENT_PORTAL_THROUGH_PORTAL("prevent-creating-portal-through-portal", false, true),
	ENABLE_SOUNDS("enable-sounds", true, true),
	PROTECT_PORTALS_FROM_TNT("protect-portals-from-tnt", false, true); // AI edit: portal-gun-only cleanup
	
	private final String yaml;
	private final Object def;
	private final boolean editable;
	
	private RegionSetting(String yaml, Object def) {
		this.yaml = yaml;
		this.def = def;
		this.editable = false;
	}
	
	private RegionSetting(String yaml, Object def, boolean editable) {
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
