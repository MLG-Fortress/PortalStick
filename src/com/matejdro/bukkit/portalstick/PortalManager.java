package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.matejdro.bukkit.portalstick.listeners.PortalStickPlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.Warning;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class PortalManager {
	private PortalStick plugin;
	
	PortalManager(PortalStick plugin)
	{
		this.plugin = plugin;
		for (Material material : Material.values())
		{
			if (material.name().startsWith("LEGACY_"))
				continue;
			if (!material.isSolid())
				continue;
			if (material.name().contains("GLASS"))
				nonPortalableMaterials.add(material);
		}
		nonPortalableMaterials.add(Material.WATER);
		nonPortalableMaterials.add(Material.LAVA);
		nonPortalableMaterials.add(Material.OBSIDIAN);
		nonPortalableMaterials.add(Material.FARMLAND);
		nonPortalableMaterials.add(Material.COMMAND_BLOCK);
		nonPortalableMaterials.add(Material.REPEATING_COMMAND_BLOCK);
		nonPortalableMaterials.add(Material.CHAIN_COMMAND_BLOCK);
	}
	
	public final HashSet<Portal> portals = new HashSet<Portal>();
	public final HashMap<V10Location, Portal> borderBlocks = new HashMap<V10Location, Portal>();
	public final HashMap<V10Location, Portal> behindBlocks = new HashMap<V10Location, Portal>();
	public final HashMap<V10Location, Portal> insideBlocks = new HashMap<V10Location, Portal>();
	final HashMap<V10Location, Portal> awayBlocks = new HashMap<V10Location, Portal>();
	final HashMap<V10Location, Portal> awayBlocksY = new HashMap<V10Location, Portal>();
	// AI edit: fake-portal-visuals - oldBlocks removed, real blocks never changed so nothing to restore.
	private Set<Material> nonPortalableMaterials = new HashSet<>();

	
	private boolean checkPortal(PortalCoord portal)
	{
		Material id;
		ArrayList<Portal> overlap = new ArrayList<Portal>();
		boolean ol;
		Block block;
		for (V10Location loc: portal.border)
		{
			if(borderBlocks.containsKey(loc))
			{
			  overlap.add(borderBlocks.get(loc));
			  ol = true;
			}
			else if(insideBlocks.containsKey(loc))
			{
			  overlap.add(insideBlocks.get(loc));
			  ol = true;
			}
			else if(behindBlocks.containsKey(loc))
			{
			  overlap.add(behindBlocks.get(loc));
			  ol = false;
			}
			else
			  ol = false;
			
			if(!ol)
			{
			  block = loc.getHandle().getBlock();
			  Material blockType = block.getType();
			  if(!plugin.config.getGlobalBoolean(RegionSetting.ALL_BLOCKS_PORTAL)) // AI edit: portal-gun-only cleanup
			  {
				if(!plugin.config.getGlobalList(RegionSetting.PORTAL_BLOCKS).contains(blockType.name())) // AI edit: portal-gun-only cleanup
				  return false;
			  }
			  if (nonoBlock(block))
				return false;
			}
		}
		for (V10Location loc: portal.inside)
		{
			if(loc == null)
			  continue;
			if(borderBlocks.containsKey(loc))
			{
			  overlap.add(borderBlocks.get(loc));
			  ol = true;
			}
			else if(insideBlocks.containsKey(loc))
			{
			  overlap.add(insideBlocks.get(loc));
			  ol = true;
			}
			else if(behindBlocks.containsKey(loc))
			{
			  overlap.add(behindBlocks.get(loc));
			  ol = false;
			}
			else
			  ol = false;

			if(!ol)
			{
			  block = loc.getHandle().getBlock();
			  id = block.getType();
			  if(!plugin.config.getGlobalBoolean(RegionSetting.ALL_BLOCKS_PORTAL)) // AI edit: portal-gun-only cleanup
			  {
				if(!plugin.config.getGlobalList(RegionSetting.PORTAL_BLOCKS).contains(id.name())) // AI edit: portal-gun-only cleanup
				  return false;
			  }
			  if (nonoBlock(block))
			  	return false;
			}
		}
		for(Portal p: overlap)
		  p.delete();
		return true;
	}

//	private BlockFace[] getFacesOfInsideBlocks(PortalCoord portal)
//	{
//		BlockFace[] faces = new BlockFace[2];
//		faces[0] = (portal.tpFace);
//		faces[1] = (portal.tpFace.getOppositeFace());
//		return faces;
//	}
//
//	private boolean nonoAttachedInsideBlock(Block block)
//	{
//		Material material = block.getType();
//		switch (material)
//		{
//			case WATER:
//			case LAVA:
//				return false;
//		}
//		return !material.isSolid();
//	}

	//Prevents portals from replacing blocks with state
	//Not only are states not stored, but things like containers drop their items
	//And I ain't gonna deal with that.
	private boolean nonoBlock(Block block)
    {
    	Material material = block.getType();
//        if (Tag.DOORS.isTagged(material))
//            return true;
        switch (material)
		{
			case END_GATEWAY:
			case PLAYER_HEAD:
			case PLAYER_WALL_HEAD:
				return true;
		}

        //Got any better ideas? No way to check if the class is not a child of class we're looking for
		// since Bukkit is only an interface... unless we get CB itself ofc.
		BlockState state = block.getState();
		return state instanceof Container
				|| state instanceof Banner
				|| state instanceof Bed
				|| state instanceof Sign;
	}

	public void deletePortals(User user)
	{
		if (user == null) return;
		if (user.bluePortal != null) user.bluePortal.delete();
		if (user.orangePortal != null) user.orangePortal.delete();
	}

	private PortalCoord generateHorizontalPortal(V10Location block, BlockFace face)
	{
		PortalCoord portal;
		//autocorrect to ground level if selected block is only one block above ground
		if (block.getHandle().getBlock().getRelative(BlockFace.DOWN).getRelative(face).getType() != Material.AIR)
			block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.UP));

		portal = generatePortal(block, face); // 0
		if(!checkPortal(portal))
		{
		  block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -1
		  portal = generatePortal(block, face);
		  if(!checkPortal(portal))
		  {
			block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -2
			portal = generatePortal(block, face);
			if(!checkPortal(portal))
			{
			  block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.UP, 3)); // 1 (-2 + 3)
			  portal = generatePortal(block, face);
			  if(!checkPortal(portal))
			  {
				block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.UP)); // 2
				portal = generatePortal(block, face);
				if(!checkPortal(portal))
				  portal.finished = true;
			  }
			}
		  }
		}
		return portal;
	}

	private PortalCoord generatePortal(V10Location block, BlockFace face)
	{
		PortalCoord portal = new PortalCoord();
		portal.block = block;
		Block rb = block.getHandle().getBlock();
		
		switch(face)
		{
		  case DOWN:
		  case UP:
			if (!plugin.config.CompactPortal || plugin.config.FillPortalBack == Material.AIR)
			{
				portal.border.add(new V10Location(rb.getRelative(BlockFace.NORTH)));
				if(!plugin.config.CompactPortal)
				{
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.NORTH_WEST))); 
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.WEST)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.SOUTH_WEST)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.SOUTH)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.SOUTH_EAST)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.EAST)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.NORTH_EAST)));
				}
			}
			
			portal.inside[0] = new V10Location(rb);
	    	
	    	portal.destLoc[0] = new V10Location(rb.getRelative(face));
	    	face = face.getOppositeFace();
			portal.behind[0] = new V10Location(rb.getRelative(face));
	    	portal.tpFace = face;
	    	portal.vertical = true;
	    	return portal;
		  case NORTH:
		  case NORTH_EAST:
			face = BlockFace.SOUTH;
			break;
		  case EAST:
		  case SOUTH_EAST:
		    face = BlockFace.WEST;
		    break;
		  case SOUTH:
		  case SOUTH_WEST:
	    	face = BlockFace.NORTH;
	    	break;
		  default:
	    	face = BlockFace.EAST;
	    	break;
		}
	    
	    portal.tpFace = face;
	    
	    switch(face)
	    {
	      case NORTH:
	      case SOUTH:
	    	face = BlockFace.EAST;
	    	break;
	      default:
	    	face = BlockFace.NORTH;
	    }
	    
	    if (!plugin.config.CompactPortal || plugin.config.FillPortalBack == Material.AIR)
	    {
	      Block block2 = rb.getRelative(BlockFace.DOWN, 2);
	      portal.border.add(new V10Location(block2));
	      
	      if(!plugin.config.CompactPortal)
	      {
	    	block2 = block2.getRelative(face);
	    	portal.border.add(new V10Location(block2));
	    	for(int i = 0; i < 3; i++)
		    {
	    	  block2 = block2.getRelative(BlockFace.UP);
	    	  portal.border.add(new V10Location(block2));
		    }
	    	face = face.getOppositeFace();
	    	for(int i = 0; i < 2; i++)
	    	{
	    	  block2 = block2.getRelative(face);
	    	  portal.border.add(new V10Location(block2));
	    	}
	    	for(int i = 0; i < 3; i++)
	    	{
	    	  block2 = block2.getRelative(BlockFace.DOWN);
	    	  portal.border.add(new V10Location(block2));
	    	}
	      }
	    }
	    
	    portal.inside[1] = block;
	    Block block2 = rb.getRelative(BlockFace.DOWN);
	    portal.inside[0] = new V10Location(block2);
	    
	    Block block3 = block2.getRelative(portal.tpFace.getOppositeFace());
	    portal.destLoc[0] = new V10Location(block3);
	    portal.destLoc[1] = new V10Location(block3.getRelative(BlockFace.UP));
	    
	    portal.vertical = false;
	    
	    block2 = block2.getRelative(portal.tpFace);
	    portal.behind[0] = new V10Location(block2);
	    portal.behind[1] = new V10Location(block2.getRelative(BlockFace.UP));
	    
		return portal;
	}

	public boolean placePortal(V10Location block, BlockFace face, Player player, boolean orange, boolean end)
	{
		//Check if player can place here
		Location loc = block.getHandle();
		Block bBlock = loc.getBlock();

		if (!plugin.config.getGlobalBoolean(RegionSetting.ENABLE_PORTALS) || !plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL)) // AI edit: portal-gun-only cleanup
			return false;
		//RoboMWM: Don't allow player to put portal on transparent blocks
		//or containers
		if (loc.getY() > 255
				|| plugin.config.getGlobalList(RegionSetting.TRANSPARENT_BLOCKS).contains(bBlock.getType().name()) // AI edit: portal-gun-only cleanup
				|| PortalStickPlayerListener.nonSolidBlocks.contains(bBlock.getType())
				|| nonPortalableMaterials.contains(bBlock.getType()))
        {
            plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, block);
            return false;
        }
		
		boolean vertical = false;
		
		PortalCoord portalc;
		
		User owner = plugin.userManager.getUser(player);

		if (face == BlockFace.DOWN || face == BlockFace.UP)
		{
			vertical = true;
			portalc = generatePortal(block, face);
			if (!checkPortal(portalc))
			{
				//if (end) plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
				plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, block);
				return false;
			}
		}
		else
		{
			portalc = generateHorizontalPortal(block, face);
			if (portalc.finished)
			{
				//if (end) plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
				plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, block);
				return false;
			}
		}

		Portal portal = new Portal(plugin, portalc.destLoc, portalc.block, portalc.border, portalc.inside, portalc.behind, owner, orange, vertical, portalc.tpFace);
		
		
		if (orange)
		{
			if (owner.orangePortal != null)
			  owner.orangePortal.delete();
			owner.orangePortal = portal;
			plugin.util.playSound(Sound.PORTAL_CREATE_ORANGE, block);
			
		}
		else
		{
			if (owner.bluePortal != null)
			  owner.bluePortal.delete();
			owner.bluePortal = portal;
			plugin.util.playSound(Sound.PORTAL_CREATE_BLUE, block);
		}
		
		portals.add(portal);
		portal.create();
		return true;
		
	}

	@Deprecated
	@Warning(reason = "Does not perform any checks")
	public void placePortal(V10Location block, Player player, boolean orange)
	{
		
		float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - block.x, block.z - player.getLocation().getBlockZ()));
		dir = dir % 360;
	    if(dir < 0)
	    	dir += 360;
	    
		//Try WEST/EAST
		if (dir < 90 || dir > 270)
		{
			if (placePortal(block, BlockFace.EAST, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.WEST, player, orange, false))
		  return;
		
		//Try NORTH/SOUTH
		if (dir < 180) 
		{
			if (placePortal(block, BlockFace.SOUTH, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.NORTH, player, orange, false))
		  return;
		
		//Try UP/DOWN
		if (player.getEyeLocation().getY() >= block.y )
		{
			if (placePortal(block, BlockFace.UP, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.DOWN, player, orange, true))
		  return;
	
	 }

}
