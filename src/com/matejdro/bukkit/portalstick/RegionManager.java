package com.matejdro.bukkit.portalstick;

import java.util.HashMap;

import org.bukkit.Location;

import de.V10lator.PortalStick.V10Location;

public class RegionManager {
	private final PortalStick plugin;
	
	RegionManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public final HashMap<String, Region> regions = new HashMap<String, Region>();
	
	public Region loadRegion(String name) {
		Region region = getRegion(name);
		if (region == null)
			region = new Region(name);
		plugin.config.loadRegionSettings(region);
		regions.put(name, region);
		return region;
	}
	
	
	public Region getRegion(V10Location location) {
		return getRegion("global");
	}
	
	public Region getRegion(String name) {
		return regions.get(name.toLowerCase());
	}
	
}
