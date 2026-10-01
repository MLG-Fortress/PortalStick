package com.matejdro.bukkit.portalstick.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap; // AI edit: portal-gun-only cleanup
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;

public class Config {
	
	private final PortalStick plugin;
	private final FileConfiguration mainConfig;
	private final FileConfiguration regionConfig;
	
	private final File mainConfigFile;
	private final File regionConfigFile;
	
	public HashSet<String> DisabledWorlds;
	public final HashMap<RegionSetting, Object> globalSettings = new HashMap<RegionSetting, Object>(); // AI edit: portal-gun-only cleanup
	public Material PortalTool;
	//public short portalToolData; //Short for spout compatiblity!
	public boolean CompactPortal;
	public int RegionTool;
	public boolean RestoreInvOnWorldChange;
	public List<String> ColorPresets;
	public Material FillPortalBack;
	
	public boolean useNativeSounds, useSpoutSounds;
	public int soundRange;
	public final String[] soundUrls = new String[Sound.values().length];
	public final String[] soundNative = new String[Sound.values().length];
	
	public String lang;
	
	public Config (PortalStick instance) {
		
		plugin = instance;
		
		mainConfigFile = getConfigFile("config.yml");
		regionConfigFile = getConfigFile("regions.yml");
		
		
		mainConfig = getConfig(mainConfigFile);
		regionConfig = getConfig(regionConfigFile);
	}
	
	public void load() {
		try {
			mainConfig.load(mainConfigFile);
			regionConfig.load(regionConfigFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
        
        //Load main settings
        DisabledWorlds = new HashSet<String>(getStringList("main.disabled-worlds", new ArrayList<String>()));
        PortalTool = Material.matchMaterial(getString("main.portal-tool", "DIAMOND_HORSE_ARMOR"));
        CompactPortal = getBoolean("main.compact-portal", false);
        RestoreInvOnWorldChange = getBoolean("main.restore-inventory-on-world-change", true);
        ColorPresets = getStringList("main.portal-color-presets", Arrays.asList(new String[]{"3-1","2-6","9-10","5-13","8-7","15-4"}));
        FillPortalBack = Material.matchMaterial(getString("main.fill-portal-back", "AIR"));
        
        //Load sound settings
        useNativeSounds = getBoolean("sounds.use-minecraft-sounds", true);
        soundNative[Sound.PORTAL_CREATE_BLUE.ordinal()] = getString("sounds.minecraft.create-blue-portal", "STEP_WOOL:0.3");
        soundNative[Sound.PORTAL_CREATE_ORANGE.ordinal()] = getString("sounds.minecraft.create-orange-portal", "STEP_WOOL:0.3");
        soundNative[Sound.PORTAL_EXIT_BLUE.ordinal()] = getString("sounds.minecraft.exit-blue-portal", "ENDERMAN_TELEPORT0");
        soundNative[Sound.PORTAL_EXIT_ORANGE.ordinal()] = getString("sounds.minecraft.exit-orange-portal", "ENDERMAN_TELEPORT");
        soundNative[Sound.PORTAL_ENTER_BLUE.ordinal()] = getString("sounds.minecraft.enter-blue-portal", "fortress.portal.enter:0.3");
        soundNative[Sound.PORTAL_ENTER_ORANGE.ordinal()] = getString("sounds.minecraft.enter-orange-portal", "fortress.portal.enter:0.3");
        soundNative[Sound.PORTAL_CANNOT_CREATE.ordinal()] = getString("sounds.minecraft.cannot-create-portal", "");
        
        useSpoutSounds = getBoolean("sounds.use-spout-sounds", false);
        
        soundUrls[Sound.PORTAL_CREATE_BLUE.ordinal()] = getString("sounds.spout.create-blue-portal-url", "");
        soundUrls[Sound.PORTAL_CREATE_ORANGE.ordinal()] = getString("sounds.spout.create-orange-portal-url", "");
        soundUrls[Sound.PORTAL_EXIT_BLUE.ordinal()] = getString("sounds.spout.exit-blue-portal-url", "");
        soundUrls[Sound.PORTAL_EXIT_ORANGE.ordinal()] = getString("sounds.spout.exit-orange-portal-url", "");
        soundUrls[Sound.PORTAL_CANNOT_CREATE.ordinal()] = getString("sounds.spout.cannot-create-portal-url", "");
        
        soundRange = getInt("sounds.sound-range", 20);
        
        Locale locale = Locale.getDefault();
        lang = getString("Language", locale.getLanguage().toLowerCase()+"_"+locale.getCountry());
        
		//Load all current users
//		for (Player player : plugin.getServer().getOnlinePlayers())
//			plugin.userManager.createUser(player);
		
        // AI edit: portal-gun-only cleanup - begin
        loadGlobalSettings();
        plugin.getLogger().info("Global portal settings loaded");
        // AI edit: portal-gun-only cleanup - end
        
        saveAll();
	}
	
	// AI edit: portal-gun-only cleanup - begin
	public boolean getGlobalBoolean(RegionSetting setting) {
		return (Boolean) globalSettings.get(setting);
	}
	public int getGlobalInt(RegionSetting setting) {
		return (Integer) globalSettings.get(setting);
	}
	public List<?> getGlobalList(RegionSetting setting) {
		return (List<?>) globalSettings.get(setting);
	}
	public String getGlobalString(RegionSetting setting) {
		return (String) globalSettings.get(setting);
	}
	public double getGlobalDouble(RegionSetting setting) {
		Object value = globalSettings.get(setting);
		if (value instanceof Number)
			return ((Number) value).doubleValue();
		return Double.parseDouble(value.toString());
	}
	// AI edit: portal-gun-only cleanup - end
	private int getInt(String path, int def)
	{

		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);
	
		return mainConfig.getInt(path, def);
	}

	private String getString(String path, String def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getString(path, def);
	}

	private List<String> getStringList(String path, List<String> def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

	return mainConfig.getStringList(path);
	}

	private boolean getBoolean(String path, Boolean def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getBoolean(path, def);
	}
	
	public void reLoad() {
		unLoad();
		load();
	}
	
	public void unLoad()
	{
		for(Portal p: plugin.portalManager.portals.toArray(new Portal[0]))
			p.delete();
		plugin.portalManager.portals.clear();
	}
	
	// AI edit: portal-gun-only cleanup - begin
	public void loadGlobalSettings() {
		for (RegionSetting setting : RegionSetting.values()) {
			Object prop = regionConfig.get("global." + setting.getYaml());
    		if (prop == null)
    			globalSettings.put(setting, setting.getDefault());
    		else
    			globalSettings.put(setting, prop);
    		regionConfig.set("global." + setting.getYaml(), globalSettings.get(setting));
    	}
	// AI edit: portal-gun-only cleanup - end
	}
	
	private File getConfigFile(String filename)
	{
		if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
		
		File file = new File(plugin.getDataFolder(), filename);
		return file;
	}
	private FileConfiguration getConfig(File file) {
		FileConfiguration config = null;
		try {
			config = new YamlConfiguration();
			if (file.exists())
			{
				config.load(file);
				config.set("setup", null);
			}
			config.save(file);
			
			return config;
		} catch (Exception e) {
			plugin.getLogger().severe("Unable to load YAML file " + file.getAbsolutePath());
		}
		return null;
	}
	
	public void saveAll() {
		
		// AI edit: portal-gun-only cleanup - begin
		for (Entry<RegionSetting, Object> setting : globalSettings.entrySet())
			regionConfig.set("global." + setting.getKey().getYaml(), setting.getValue());
		// AI edit: portal-gun-only cleanup - end
		try
		{
			regionConfig.save(regionConfigFile);
		}
		catch (Exception ex)
		{
			plugin.getLogger().severe("Error while writing to regions.yml");
		}
		
		//Save main
		mainConfig.set("Language", lang);
		try
		{
			mainConfig.save(mainConfigFile);
		}
		catch (Exception ex)
		{
			plugin.getLogger().severe("Error while writing to config.yml");
		}
			
	}
	
	public enum Sound {
		PORTAL_CREATE_BLUE,
		PORTAL_CREATE_ORANGE,
		PORTAL_EXIT_BLUE,
		PORTAL_EXIT_ORANGE,
		PORTAL_ENTER_BLUE,
        PORTAL_ENTER_ORANGE,
		PORTAL_CANNOT_CREATE // AI edit: portal-gun-only cleanup
	}
	
}
