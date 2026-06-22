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
import com.matejdro.bukkit.portalstick.util.PortalSetting;

import com.matejdro.bukkit.portalstick.util.SavedBlock;
import com.matejdro.bukkit.portalstick.util.BlockLocation;

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
	public final HashMap<BlockLocation, Portal> borderBlocks = new HashMap<BlockLocation, Portal>();
	public final HashMap<BlockLocation, Portal> behindBlocks = new HashMap<BlockLocation, Portal>();
	public final HashMap<BlockLocation, Portal> insideBlocks = new HashMap<BlockLocation, Portal>();
	final HashMap<BlockLocation, Portal> awayBlocks = new HashMap<BlockLocation, Portal>();
	final HashMap<BlockLocation, Portal> awayBlocksY = new HashMap<BlockLocation, Portal>();
	public final HashMap<BlockLocation, SavedBlock> oldBlocks = new HashMap<BlockLocation, SavedBlock>();
	private Set<Material> nonPortalableMaterials = new HashSet<>();


	
	private boolean checkPortal(PortalCoord portal)
	{
		Material id;
		ArrayList<Portal> overlap = new ArrayList<Portal>();
		boolean ol;
		SavedBlock bh;
		Block block;
		for (BlockLocation loc: portal.border)
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
			  if(!plugin.config.getBoolean(PortalSetting.ALL_BLOCKS_PORTAL))
			  {
				bh = new SavedBlock(block);
				if(!plugin.config.getList(PortalSetting.PORTAL_BLOCKS).contains(blockType.name()))
				  return false;
			  }
			  if (nonoBlock(block))
				return false;
			}
		}
		for (BlockLocation loc: portal.inside)
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
			  if(!plugin.config.getBoolean(PortalSetting.ALL_BLOCKS_PORTAL))
			  {
				bh = new SavedBlock(block);
				if(!plugin.config.getList(PortalSetting.PORTAL_BLOCKS).contains(id.name()))
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

	private PortalCoord generateHorizontalPortal(BlockLocation block, BlockFace face)
	{
		PortalCoord portal;
		//autocorrect to ground level if selected block is only one block above ground
		if (block.getHandle().getBlock().getRelative(BlockFace.DOWN).getRelative(face).getType() != Material.AIR)
			block = new BlockLocation(block.getHandle().getBlock().getRelative(BlockFace.UP));

		portal = generatePortal(block, face); // 0
		if(!checkPortal(portal))
		{
		  block = new BlockLocation(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -1
		  portal = generatePortal(block, face);
		  if(!checkPortal(portal))
		  {
			block = new BlockLocation(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -2
			portal = generatePortal(block, face);
			if(!checkPortal(portal))
			{
			  block = new BlockLocation(block.getHandle().getBlock().getRelative(BlockFace.UP, 3)); // 1 (-2 + 3)
			  portal = generatePortal(block, face);
			  if(!checkPortal(portal))
			  {
				block = new BlockLocation(block.getHandle().getBlock().getRelative(BlockFace.UP)); // 2
				portal = generatePortal(block, face);
				if(!checkPortal(portal))
				  portal.finished = true;
			  }
			}
		  }
		}
		return portal;
	}

	private PortalCoord generatePortal(BlockLocation block, BlockFace face)
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
				portal.border.add(new BlockLocation(rb.getRelative(BlockFace.NORTH)));
				if(!plugin.config.CompactPortal)
				{
				  portal.border.add(new BlockLocation(rb.getRelative(BlockFace.NORTH_WEST))); 
				  portal.border.add(new BlockLocation(rb.getRelative(BlockFace.WEST)));
				  portal.border.add(new BlockLocation(rb.getRelative(BlockFace.SOUTH_WEST)));
				  portal.border.add(new BlockLocation(rb.getRelative(BlockFace.SOUTH)));
				  portal.border.add(new BlockLocation(rb.getRelative(BlockFace.SOUTH_EAST)));
				  portal.border.add(new BlockLocation(rb.getRelative(BlockFace.EAST)));
				  portal.border.add(new BlockLocation(rb.getRelative(BlockFace.NORTH_EAST)));
				}
			}
			
			portal.inside[0] = new BlockLocation(rb);
	    	
	    	portal.destLoc[0] = new BlockLocation(rb.getRelative(face));
	    	face = face.getOppositeFace();
			portal.behind[0] = new BlockLocation(rb.getRelative(face));
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
	      portal.border.add(new BlockLocation(block2));
	      
	      if(!plugin.config.CompactPortal)
	      {
	    	block2 = block2.getRelative(face);
	    	portal.border.add(new BlockLocation(block2));
	    	for(int i = 0; i < 3; i++)
		    {
	    	  block2 = block2.getRelative(BlockFace.UP);
	    	  portal.border.add(new BlockLocation(block2));
		    }
	    	face = face.getOppositeFace();
	    	for(int i = 0; i < 2; i++)
	    	{
	    	  block2 = block2.getRelative(face);
	    	  portal.border.add(new BlockLocation(block2));
	    	}
	    	for(int i = 0; i < 3; i++)
	    	{
	    	  block2 = block2.getRelative(BlockFace.DOWN);
	    	  portal.border.add(new BlockLocation(block2));
	    	}
	      }
	    }
	    
	    portal.inside[1] = block;
	    Block block2 = rb.getRelative(BlockFace.DOWN);
	    portal.inside[0] = new BlockLocation(block2);
	    
	    Block block3 = block2.getRelative(portal.tpFace.getOppositeFace());
	    portal.destLoc[0] = new BlockLocation(block3);
	    portal.destLoc[1] = new BlockLocation(block3.getRelative(BlockFace.UP));
	    
	    portal.vertical = false;
	    
	    block2 = block2.getRelative(portal.tpFace);
	    portal.behind[0] = new BlockLocation(block2);
	    portal.behind[1] = new BlockLocation(block2.getRelative(BlockFace.UP));
	    
		return portal;
	}

	public boolean placePortal(BlockLocation block, BlockFace face, Player player, boolean orange, boolean end)
	{
		//Check if player can place here
		Location loc = block.getHandle();
		Block bBlock = loc.getBlock();
		if (!plugin.config.getBoolean(PortalSetting.ENABLE_PORTALS) || !plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL))
			return false;
		//RoboMWM: Don't allow player to put portal on transparent blocks
		//or containers
		if (loc.getY() > 255
				|| plugin.config.getList(PortalSetting.TRANSPARENT_BLOCKS).contains(bBlock.getType().name())
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
		portals.add(portal);
		portal.create();
		return true;
		
	}

	@Deprecated
	@Warning(reason = "Does not perform any checks")
	public void placePortal(BlockLocation block, Player player, boolean orange)
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


	
	public void tryPlacingAutomatedPortal(Block rb)
	{
//		//Check if wool is correct
//		Wool wool = (Wool) Material.WOOL.getNewData(rb.getData());
//		if (wool.getColor() != DyeColor.BLACK && wool.getColor() != DyeColor.LIGHT_BLUE && wool.getColor() != DyeColor.ORANGE) return;
//		//Check for first iron bar
//		Block firstIronBar = null;
//		for (int i = 0; i < 6; i++)
//		 {
//			 if (rb.getRelative(BlockFace.values()[i], 2).getType() == Material.IRON_FENCE)
//			 {
//				firstIronBar = rb.getRelative(BlockFace.values()[i], 2);
//				break;
//			 }
//			 else if (rb.getRelative(BlockFace.values()[i]).getType() == Material.IRON_FENCE)
//			 {
//				firstIronBar = rb.getRelative(BlockFace.values()[i]);
//				break;
//			 }
//		 }
//		if (firstIronBar == null)
//		  return;
//		//Find other iron bars at same side of portal generator
//		ArrayList<Block> ironBars = new ArrayList<Block>();
//
//		for (int i = 0; i < 6; i++)
//		 {
//			BlockFace face = BlockFace.values()[i];
//			 if (firstIronBar.getRelative(face).getType() == Material.IRON_FENCE)
//			 {
//				 while (firstIronBar.getRelative(face).getType() == Material.IRON_FENCE)
//				 {
//					 firstIronBar = firstIronBar.getRelative(face);
//				 }
//
//				//firstIronBar.setType(Material.WOOD);
//				 ironBars.add(firstIronBar);
//
//				 int counter = 1;
//				 while (firstIronBar.getRelative(face.getOppositeFace(), counter).getType() == Material.IRON_FENCE)
//				 {
//					 ironBars.add(firstIronBar.getRelative(face.getOppositeFace(), counter));
//					 counter++;
//				 }
//
//
//				 break;
//			 }
//		 }
//
//		//Find, in which direction is other side of portal generator
//		int size = plugin.config.CompactPortal ? 2 : 4; // How far is another side of portal generator
//		BlockFace otherSide = null;
//		for (int i = 0; i < 6; i++)
//		 {
//			BlockFace face = BlockFace.values()[i];
//			if (firstIronBar.getRelative(face, size).getType() == Material.IRON_FENCE)
//			{
//				otherSide = face;
//				break;
//			}
//		 }
//		if (otherSide == null)
//		  return;
//		//Search for iron bars on other side of portal generator
//		for (Block ironBar : ironBars.toArray(new Block[0]))
//		{
//			if (ironBar.getRelative(otherSide, size).getType() == Material.IRON_FENCE)
//				//ironBar.setType(Material.WOOD);
//				ironBars.add(ironBar.getRelative(otherSide, size));
//		}
//
//		BlockFace portalFace = null;
//		Portal oldPortal = null;
//		//Find, where portal surface is
//		for (int i = 0; i < 6; i++)
//		{
//			BlockFace face2 = BlockFace.values()[i];
//			if (face2 == otherSide || face2.getOppositeFace() == otherSide)
//				continue;
//			Block firstPortalBlock = firstIronBar.getRelative(otherSide).getRelative(face2);
//			if (region.getBoolean(PortalSetting.ALL_BLOCKS_PORTAL) || region.getList(PortalSetting.PORTAL_BLOCKS).contains(firstPortalBlock.getTypeId()))
//			{
//				portalFace = face2;
//				break;
//			}
//			else
//			{
//				BlockLocation loc = new BlockLocation(firstPortalBlock);
//
//				if (oldPortal == null)
//					oldPortal = borderBlocks.get(loc);
//				if (oldPortal == null)
//					oldPortal = insideBlocks.get(loc);
//
//				if (oldPortal != null)
//				{
//					portalFace = face2;
//					break;
//				}
//			}
//		}
//
//		if (portalFace == null)
//			return;
//		//Is portal generator right size?
//		if ((!plugin.config.CompactPortal &&
//		(((portalFace == BlockFace.UP || portalFace == BlockFace.DOWN) && ironBars.size() != 6 ) ||
//		(portalFace != BlockFace.UP && portalFace != BlockFace.DOWN && ironBars.size() != 8 ))) ||
//		(plugin.config.CompactPortal &&
//		(((portalFace == BlockFace.UP || portalFace == BlockFace.DOWN) && ironBars.size() != 2 ) ||
//		(portalFace != BlockFace.UP && portalFace != BlockFace.DOWN && ironBars.size() != 4 ))))
//			return;
//
//		if (wool.getColor() == DyeColor.BLACK)
//		{
//			if (oldPortal != null)
//			  oldPortal.delete();
//			return;
//		}
//
//		//Check if portal is big enough and start making a portal
//		PortalCoord portalc = new PortalCoord();
//		int c = 0;
//		for (int i = 0; i < ironBars.size() / 2; i++)
//		{
//			portalc.border.add(new BlockLocation(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 1)));
//			portalc.border.add(new BlockLocation(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 3)));
//
//			if (i == 0 || i == (ironBars.size() / 2) - 1)
//				portalc.border.add(new BlockLocation(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 2)));
//			else
//				portalc.inside[c++] = new BlockLocation(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 2));
//		}
//
//		portalc.vertical = portalFace == BlockFace.UP || portalFace == BlockFace.DOWN;
//		portalc.block = portalc.inside[0];
//
//		if (oldPortal != null)
//		  oldPortal.delete();
//
//		if (portalc.border.size() == 0 || portalc.inside[0] == null)
//		  return;
//		for (BlockLocation tb : portalc.border)
//		{
//			oldPortal = borderBlocks.get(tb);
//			if (oldPortal != null)
//				oldPortal.delete();
//
//			if ((!region.getBoolean(PortalSetting.ALL_BLOCKS_PORTAL) && !region.getList(PortalSetting.PORTAL_BLOCKS).contains(tb.getHandle().getBlock().getTypeId())) || borderBlocks.containsKey(tb) || insideBlocks.containsKey(tb)) return;
//
//		}
//		for (BlockLocation tb : portalc.inside)
//		{
//			if (tb != null)
//			{
//				oldPortal = borderBlocks.get(tb);
//				if (oldPortal != null)
//					oldPortal.delete();
//
//				if ((!region.getBoolean(PortalSetting.ALL_BLOCKS_PORTAL) && !region.getList(PortalSetting.PORTAL_BLOCKS).contains(tb.getHandle().getBlock().getTypeId())) || borderBlocks.containsKey(tb) || insideBlocks.containsKey(tb)) return;
//			}
//		}
//
//		if (portalc.vertical)
//		  portalc.destLoc[0] = new BlockLocation(portalc.inside[0].getHandle().getBlock().getRelative(portalFace.getOppositeFace()));
//		else
//		{
//		  Block block = portalc.inside[0].getHandle().getBlock().getRelative(portalFace.getOppositeFace());
//		  portalc.destLoc[0] = new BlockLocation(block);
//		  block = block.getRelative(BlockFace.UP);
//		  portalc.destLoc[1] = new BlockLocation(block);
//		}
//
//		portalc.tpFace = portalFace;
//
//		boolean orange = wool.getColor() == DyeColor.ORANGE;
//
//		Portal portal = new Portal(plugin, portalc.destLoc, portalc.block, portalc.border, portalc.inside, portalc.behind, region, orange, portalc.vertical, portalc.tpFace);
//
//		if (orange)
//		{
//			if (region.orangePortal != null) region.orangePortal.delete();
//			region.orangePortal = portal;
//		}
//		else
//		{
//			if (region.bluePortal != null) region.bluePortal.delete();
//			region.bluePortal = portal;
//		}
//		portals.add(portal);
//		region.portals.add(portal);
//
//		portal.create();
	}
}
