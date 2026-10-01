package com.matejdro.bukkit.portalstick.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.matejdro.bukkit.portalstick.events.PlayerPortalGunShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class PortalStickPlayerListener implements Listener {
	private PortalStick plugin;
	public static Set<Material> nonSolidBlocks = new HashSet<>();
	
	public PortalStickPlayerListener(PortalStick plugin)
	{
		this.plugin = plugin;
		for (Material material : Material.values())
        {
        	if (material.name().startsWith("LEGACY_"))
        		continue;
            //if (material.isBlock() && (!material.isSolid() || material.isTransparent()))
			if (material.isBlock() && (!material.isSolid()))
                nonSolidBlocks.add(material);
			if (material.name().contains("SIGN")
			|| material.name().contains("FENCE"))
				nonSolidBlocks.add(material);
        }

		nonSolidBlocks.add(Material.IRON_BARS);
		nonSolidBlocks.add(Material.SPAWNER);
		nonSolidBlocks.add(Material.BARRIER);
		nonSolidBlocks.add(Material.IRON_DOOR);
		nonSolidBlocks.add(Material.IRON_TRAPDOOR);
		nonSolidBlocks.add(Material.OAK_DOOR);
		nonSolidBlocks.add(Material.OAK_TRAPDOOR);
		nonSolidBlocks.add(Material.JUNGLE_DOOR);
		nonSolidBlocks.add(Material.JUNGLE_TRAPDOOR);


        //nonSolidBlocks.remove(Material.WATER);
        //nonSolidBlocks.remove(Material.STATIONARY_WATER);
        //nonSolidBlocks.remove(Material.LAVA);
        //nonSolidBlocks.remove(Material.STATIONARY_LAVA);

        //These are solid blocks, despite being transparent.
        nonSolidBlocks.remove(Material.GLASS);
//        nonSolidBlocks.remove(Material.THIN_GLASS);
//        nonSolidBlocks.remove(Material.STAINED_GLASS);
//        nonSolidBlocks.remove(Material.STAINED_GLASS_PANE);
//        nonSolidBlocks.remove(Material.MOB_SPAWNER);
        nonSolidBlocks.remove(Material.ICE);
        nonSolidBlocks.remove(Material.FROSTED_ICE);
        nonSolidBlocks.remove(Material.BARRIER);
        nonSolidBlocks.remove(Material.SLIME_BLOCK);
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
		  return;
		
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
	
		//Portal tool
		//RoboMWM: account for 1.9 offhand changes

		if (event.getAction() == Action.PHYSICAL)
			return;

		ItemStack itemInHand;
		boolean mainHand = true;

		switch (event.getHand())
		{
			case HAND:
				itemInHand = player.getInventory().getItemInMainHand();
				break;
			case OFF_HAND: //Don't do anything if the player is holding a portal gun in both hands. Not like the portal gun in the offhand is useful anyways (can't left click)
				itemInHand = player.getInventory().getItemInOffHand();
				if (itemInHand.getType() == player.getInventory().getItemInMainHand().getType()) //or just anything that's similar, doesn't really matter for my purposes as of yet.
					return;
				mainHand = false;
				break;
			default:
				return;
		}

		if (itemInHand.getType() == plugin.config.PortalTool && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				Block block = event.getClickedBlock();
				Material mat = block.getType();
				if(Tag.BUTTONS.isTagged(mat) || mat == Material.LEVER)
					return;
			}

			event.setCancelled(true);
	

			if (!plugin.config.getGlobalBoolean(RegionSetting.ENABLE_PORTALS) || !plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL)) // AI edit: portal-gun-only cleanup
				return;

			//Convert strings to materials
			Set<Material> transparentMaterials = new HashSet<>();
//			for (String proposedMaterial : tb)
//			{
//				transparentMaterials.add(Material.getMaterial(proposedMaterial));
//			}

			if (transparentMaterials.isEmpty())
            {
                transparentMaterials.addAll(nonSolidBlocks);
            }

			List<Block> targetBlocks = event.getPlayer().getLineOfSight(transparentMaterials, 120);
			if (targetBlocks.isEmpty())
				return;

			boolean orange = false;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				orange = true;

			V10Location loc;
			if (plugin.config.getGlobalBoolean(RegionSetting.PREVENT_PORTAL_THROUGH_PORTAL)) // AI edit: portal-gun-only cleanup
			{
				List<Block> goodBlocks = new ArrayList<>(targetBlocks.size());
				for (Block b : targetBlocks)
				{
					loc = new V10Location(b);
					goodBlocks.add(b);
					for (Portal p : plugin.portalManager.portals)
					{
					  for(int i = 0; i < 2; i++)
						if(p.inside[i] != null && p.inside[i].equals(loc))
						{
							//plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
							//plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, loc);
							fireGunShootEvent(player, goodBlocks, event.getAction(), orange);
							return;
						}
					}
				}
			}

			fireGunShootEvent(player, targetBlocks, event.getAction(), orange);

			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || transparentMaterials.contains(event.getClickedBlock().getType()))
			{
				Block b = targetBlocks.get(targetBlocks.size() - 1);
				loc = new V10Location(b);
		        if (targetBlocks.size() < 2)
				{
					//plugin.portalManager.placePortal(loc, event.getPlayer(), orange);
					plugin.getLogger().warning("Player stuck?? " + player.getLocation());
					return;
				}
		        else
		    	   plugin.portalManager.placePortal(loc, b.getFace(targetBlocks.get(targetBlocks.size() - 2)), event.getPlayer(), orange, true);
			}
			else
				plugin.portalManager.placePortal(new V10Location(event.getClickedBlock()), event.getBlockFace(), event.getPlayer(), orange, true);
		}
		//Color changing
		else if (mainHand && event.getAction() == Action.RIGHT_CLICK_BLOCK && itemInHand.getType() == Material.AIR && Tag.WOOL.isTagged(event.getClickedBlock().getType()))
		{
			V10Location loc = new V10Location(event.getClickedBlock());
			Portal portal = plugin.portalManager.borderBlocks.get(loc);
			if (portal == null) portal = plugin.portalManager.insideBlocks.get(loc);
			if (portal == null && plugin.config.CompactPortal) portal = plugin.portalManager.behindBlocks.get(loc);
			if (portal == null) return;
			if (portal.owner.name != player.getName()) return;
		
			
			int preset = user.colorPreset;
			if (preset == plugin.config.ColorPresets.size() - 1)
				preset = 0;
			else
				preset++;
			
			user.colorPreset = preset;
			user.recreatePortals();

			String color1 = DyeColor.values()[plugin.util.getLeftPortalColor(preset)].toString().replace("_", " ");
			String color2 = DyeColor.values()[plugin.util.getRightPortalColor(preset)].toString().replace("_", " ");

			plugin.util.sendMessage(player, plugin.i18n.getString("SwitchedPortalColor", player.getName(), color1, color2));
		}

	}

	public boolean fireGunShootEvent(Player player, List<Block> targetBlocks, Action action, boolean orange)
	{
		PlayerPortalGunShootEvent shootEvent = new PlayerPortalGunShootEvent(plugin, player, targetBlocks, action, orange);
		plugin.getServer().getPluginManager().callEvent(shootEvent);
		return shootEvent.isCancelled(); //TODO: add more methods to determine whether to play "can't create" sound or not
	}
 	    
	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event)
	{
	  if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
		return;
	  Location to = plugin.entityManager.onEntityMove(event.getPlayer(), event.getFrom(), event.getTo(), false);
	  if(to != null)
		event.setTo(to);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void onPlayerJoin(PlayerJoinEvent event)
	{
		plugin.userManager.createUser(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void onPlayerQuit(PlayerQuitEvent event)
	{
		plugin.userManager.deleteUser(event.getPlayer());
	}
	
}
