package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;


import de.V10lator.PortalStick.V10Location;

public class Portal {
	private final PortalStick plugin;
	public final HashSet<V10Location> border;
	public final V10Location[] inside;
	public final V10Location[] teleport;
	private final V10Location[] behind;
	public final boolean horizontal;
	private final V10Location centerBlock;
	public final User owner;
	public final boolean orange;
	public boolean open = false;
	boolean disabled = false;
	public final BlockFace teleportFace;
	private final HashSet<V10Location> awayBlocks;
	final V10Location[] awayBlocksY = new V10Location[2];
	
	public Portal(PortalStick plugin, V10Location[] teleport, V10Location CenterBlock, HashSet<V10Location> Border, V10Location[] inside, V10Location[] behind, User Owner, boolean Orange, boolean horizontal, BlockFace Teleportface)
	{
		this.plugin = plugin;
		this.teleport = teleport;
		border = Border;
		this.inside = inside;
		orange = Orange;
		owner = Owner;
		this.horizontal = horizontal;
		teleportFace = Teleportface;
		this.behind = behind;
		centerBlock = CenterBlock;
		if(horizontal)
		  awayBlocks = new HashSet<V10Location>();
		else
		  awayBlocks = null;
	}

	// AI edit: fake-portal-visuals - begin
	// Real blocks stay untouched, portal is client-side overlay via sendBlockChange.
	// Send one fake block to one player (skips other worlds).
	private void sendTo(Player player, V10Location loc, Material material)
	{
		if (player == null || loc == null || material == null)
			return;
		Location handle = loc.getHandle();
		if (handle == null || handle.getWorld() == null || !player.getWorld().equals(handle.getWorld()))
			return;
		player.sendBlockChange(handle, material.createBlockData());
	}

	// Restore client view to real block for everyone (undoes fake).
	private void resetToAll(V10Location loc)
	{
		if (loc == null)
			return;
		Location handle = loc.getHandle();
		if (handle == null || handle.getWorld() == null)
			return;
		BlockData real = handle.getBlock().getBlockData();
		for (Player player : plugin.getServer().getOnlinePlayers())
		{
			if (!player.getWorld().equals(handle.getWorld()))
				continue;
			player.sendBlockChange(handle, real);
		}
	}

	// Full portal overlay for one player (used on join / chunk change).
	public void showTo(Player player)
	{
		int color = orange ? plugin.util.getRightPortalColor(owner.colorPreset) : plugin.util.getLeftPortalColor(owner.colorPreset);
		Material borderMaterial = plugin.util.getPortalColorMaterial(color);
		for (V10Location loc : border)
			sendTo(player, loc, borderMaterial);
		if (plugin.config.FillPortalBack != Material.AIR)
		{
			for (V10Location loc : behind)
			{
				if (loc == null)
					continue;
				if (plugin.config.CompactPortal)
					sendTo(player, loc, borderMaterial);
				else
					sendTo(player, loc, plugin.config.FillPortalBack);
			}
		}
		for (V10Location loc : inside)
		{
			if (loc == null)
				continue;
			if (open)
				sendTo(player, loc, Material.END_GATEWAY);
			else
				sendTo(player, loc, borderMaterial);
		}
	}

	// Full portal overlay for everyone.
	public void showToAll()
	{
		for (Player player : plugin.getServer().getOnlinePlayers())
			showTo(player);
	}

	// Clear overlay for everyone, real blocks were never changed.
	private void hideFromAll()
	{
		for (V10Location loc : border)
			resetToAll(loc);
		for (V10Location loc : inside)
			resetToAll(loc);
		if (plugin.config.FillPortalBack != Material.AIR)
		{
			for (V10Location loc : behind)
				resetToAll(loc);
		}
	}
	// AI edit: fake-portal-visuals - end
	
	public void delete()
	{
		// AI edit: fake-portal-visuals - begin
		// No BlockHolder restore, just unregister maps and reset client views.
		for (V10Location loc: border)
		{
			plugin.portalManager.borderBlocks.remove(loc);
		}
		for (V10Location loc: inside)
		{
		  if(loc == null)
			continue;
		  plugin.portalManager.insideBlocks.remove(loc);
		}
		if (plugin.config.FillPortalBack != Material.AIR)
		{
			for (V10Location loc: behind)
			{
				if (loc == null)
					continue;
				plugin.portalManager.behindBlocks.remove(loc);
			}
		}
		hideFromAll();
		// AI edit: fake-portal-visuals - end
		if(horizontal)
		{
		  for(V10Location l: awayBlocks)
			plugin.portalManager.awayBlocks.remove(l);
		  plugin.portalManager.awayBlocksY.remove(awayBlocksY[0]);
		  plugin.portalManager.awayBlocksY.remove(awayBlocksY[1]);
		}
		
		Portal oldDestination = getDestination();
		
		if (orange)
			owner.orangePortal = null;
		else
			owner.bluePortal = null;
			
		open = false;
				
		plugin.portalManager.portals.remove(this);
		
		if (oldDestination != null && oldDestination.getDestination() == null) oldDestination.close();

    	}
	
	// AI edit: fake-portal-visuals - begin
	// Flip flag then push overlay, no setType. Closed portal shows wool in inside slots.
	public void open()
	{
		open = true;
		showToAll();
	}
	
	public void close()
	{
		open = false;
		showToAll();
	}
	
	public void recreate()
	{
		// Color preset changed, just resend overlay.
		showToAll();
	}
	// AI edit: fake-portal-visuals - end
	
	public void create()
	{
		// AI edit: fake-portal-visuals - begin
		// Only register maps, never touch real blocks (no BlockHolder/oldBlocks).
    	for (V10Location loc: border)
    	{
    		if (plugin.portalManager.insideBlocks.containsKey(loc))
    			plugin.portalManager.insideBlocks.get(loc).delete();
    		if (plugin.portalManager.behindBlocks.containsKey(loc))
    			plugin.portalManager.behindBlocks.get(loc).delete();
    		
    		plugin.portalManager.borderBlocks.put(loc, this);
       	}
    	for (V10Location loc: inside)
    	{
    	  if(loc != null)
    	  {
    		plugin.portalManager.insideBlocks.put(loc, this);
    	  }
    	}
    	if (plugin.config.FillPortalBack != Material.AIR)
    	{
    		for (V10Location loc: behind)
        	{
    			if (loc == null)
    				continue;
        		if (plugin.portalManager.borderBlocks.containsKey(loc))
        			plugin.portalManager.borderBlocks.get(loc).delete();
        		if (plugin.portalManager.insideBlocks.containsKey(loc))
        			plugin.portalManager.insideBlocks.get(loc).delete();

        		plugin.portalManager.behindBlocks.put(loc, this);
        	}
    	}
		// AI edit: fake-portal-visuals - end

		V10Location oloc;
		V10Location loc;
		int i;
		oloc = inside[0].clone();
		plugin.portalManager.insideBlocks.put(inside[0], this);
		if(inside[1] != null)
			plugin.portalManager.insideBlocks.put(inside[1], this);
    	
    	if (getDestination() == null)
    	{
    		close();
    	}
    		
    	else
    	{
    		open();
    		getDestination().open();
    	}
    	
    	

    	
    	if(horizontal)
    	{
    	  for (int y = -1;y<2;y++)
    	  {
    		if(y != 0)
    		{
    		  loc = new V10Location(oloc.world, oloc.x, oloc.y + y, oloc.z);
    		  plugin.portalManager.awayBlocksY.put(loc, this);
    		  if(y < 1)
    			i = 0;
    		  else
    			i = 1;
    		  awayBlocksY[i] = loc;
    		}
    		for (int x = -1;x<2;x++)
    		{
    		  for (int z = -1;z<2;z++)
    		  {
    			loc = new V10Location(oloc.world, oloc.x + x, oloc.y + y, oloc.z + z);
    			plugin.portalManager.awayBlocks.put(loc, this);
    			awayBlocks.add(loc);
    		  }
    		}
    	  }
    	}
    	
	}
	
	public Portal getDestination()
	{
		
		if (orange)
		{
			if (owner.bluePortal != null)
				return owner.bluePortal;
			return null; // AI edit: portal-gun-only cleanup
		}
		else
		{
			if (owner.orangePortal != null)
				return owner.orangePortal;
			return null; // AI edit: portal-gun-only cleanup

		}
	}
	
}
