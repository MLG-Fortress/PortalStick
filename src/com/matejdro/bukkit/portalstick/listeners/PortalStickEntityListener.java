package com.matejdro.bukkit.portalstick.listeners;

import java.util.ArrayList;
import java.util.Iterator;

import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.BlockHolder;
import de.V10lator.PortalStick.V10Location;

public class PortalStickEntityListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickEntityListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
//	@EventHandler(ignoreCancelled = true)
//	public void onEntityDamage(EntityDamageEvent event) {
//		if(plugin.config.DisabledWorlds.contains(event.getEntity().getLocation().getWorld().getName()))
//		  return;
//
//		if (event.getEntity() instanceof Player)
//		{
//			Player player = (Player)event.getEntity();
//			if (!plugin.hasPermission(player, plugin.PERM_DAMAGE_BOOTS))
//			  return;
//			Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
//			ItemStack is = player.getInventory().getBoots();
//			if (event.getCause() == DamageCause.FALL && region.getBoolean(RegionSetting.ENABLE_FALL_DAMAGE_BOOTS))
//			{
//			  boolean ok;
//			  if(is == null)
//				ok = false;
//			  else
//				ok = region.getInt(RegionSetting.FALL_DAMAGE_BOOTS) == is.getTypeId();
//			  if(ok)
//				event.setCancelled(true);
//			}
//		}
//	}

	@EventHandler(ignoreCancelled = true)
	public void onHangingEntityBreak(HangingBreakEvent event)
	{
		switch (event.getCause())
		{
			case PHYSICS:
			case OBSTRUCTION:
				event.setCancelled(isNearPortal(event.getEntity().getLocation()));
		}
	}

	//copied from my PortalEntities plugin - RoboMWM
	public boolean isNearPortal(Location location)
	{
		World world = location.getWorld();
		if(plugin.config.DisabledWorlds.contains(location.getWorld().getName()))
			return false;
		for (Portal portal : plugin.portalManager.portals)
		{
			//I was about to do exact block checks, but then realized
			// a) it's a bit more work to do and
			// b) potentially may be a lot of location#getBlock calls (performance ?)
			Location portalLocation = portal.inside[0].getHandle();
			try
			{
				if (location.distanceSquared(portalLocation) <= 9) //3 blocks
					return true;
				continue;
			}
			catch (Exception e)
			{
				continue; //Just skip if there's an issue (null, not same world, etc.)
			}
		}
		return false;
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityTeleportedByGateway(PlayerTeleportEndGatewayEvent event)
	{
		event.setCancelled(isNearPortal(event.getGateway().getLocation()));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getLocation().getWorld().getName()))
		  return;
		Region region = plugin.regionManager.getRegion(new V10Location(event.getLocation()));
		Iterator<Block> iter = event.blockList().iterator();
		Block block;
		V10Location loc;
		Portal portal;
		while(iter.hasNext())
		{
			block = iter.next();
			loc = new V10Location(block.getLocation());
			if (Tag.WOOL.isTagged(block.getType()))
			{
				portal = plugin.portalManager.borderBlocks.get(loc);
				if (portal == null)
				  portal = plugin.portalManager.insideBlocks.get(loc);
				if (portal == null)
				  portal = plugin.portalManager.behindBlocks.get(loc);
				if (portal != null)
				{
					if (region.getBoolean(RegionSetting.PROTECT_PORTALS_FROM_TNT))
					  iter.remove();
					else
					{
					  portal.delete();
					  return;
					}
				}
			}
//			else if (plugin.blockUtil.compareBlockToString(block, region.getString(RegionSetting.GRILL_MATERIAL)))
//			{
//				Grill grill = plugin.grillManager.insideBlocks.get(loc);
//				if (grill == null) grill = plugin.grillManager.borderBlocks.get(loc);
//				if (grill != null )
//				{
//					event.setCancelled(true);
//					return;
//				}
//			} //RoboMWM: I don't care about grills (xddddd)
		}
	}
}
