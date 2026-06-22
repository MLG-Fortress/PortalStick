package com.matejdro.bukkit.portalstick.listeners;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.util.PortalSetting;

import com.matejdro.bukkit.portalstick.util.SavedBlock;
import com.matejdro.bukkit.portalstick.util.BlockLocation;

public class PortalStickBlockListener implements Listener
{
	private PortalStick plugin;
	private HashSet<Block> blockedPistonBlocks = new HashSet<Block>();	
	private boolean fakeBBE;
	private final Field f;
	
	public PortalStickBlockListener(PortalStick instance)
	{
		plugin = instance;
		
		Field f;
//		try
//		{
//		  f = EntityFallingBlock.class.getDeclaredField("e");
//		  f.setAccessible(true);
//		}
//		catch(Exception e)
//		{
//		  e.printStackTrace();
//		  f = null;
//		}
		this.f = null;
	}

	@EventHandler()
	public void onBlockBreak(BlockBreakEvent event)
	{
	  Block block = event.getBlock();
	  BlockLocation loc = new BlockLocation(block);
	  if(plugin.config.DisabledWorlds.contains(loc.world))
		return;
	  
	  Portal portal = null;
	  if(plugin.portalManager.borderBlocks.containsKey(loc))
		portal = plugin.portalManager.borderBlocks.get(loc);
	  else if(plugin.portalManager.behindBlocks.containsKey(loc))
		portal = plugin.portalManager.behindBlocks.get(loc);
	  else if (plugin.portalManager.insideBlocks.containsKey(loc))
	  {
		portal = plugin.portalManager.insideBlocks.get(loc);
		if(portal.transmitter && block.getType() == Material.REDSTONE_TORCH)
		{
		  event.setCancelled(true);
		  fakeBBE = false;
		  return;
		}
		if(portal.open)
		  return;
	  }
	  if (portal != null)
	  {
		portal.delete();
		event.setCancelled(true);
		return;
	  }
	  
	  Material type = block.getType();
	  if(type == Material.REDSTONE_WIRE && plugin.config.getBoolean(PortalSetting.ENABLE_REDSTONE_TRANSFER))
	  {
		Location l = block.getLocation();
		
		for (int i = 0; i < 4; i++)
		{
		  BlockFace face = BlockFace.values()[i];
		  loc = new BlockLocation(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
		  if (plugin.portalManager.insideBlocks.containsKey(loc)) 
		  {
			portal = plugin.portalManager.insideBlocks.get(loc);
			if (!portal.open)
			  continue;
			
			Portal destination = portal.getDestination();
			if (destination == null || destination.transmitter)
			  continue;
			
			for (BlockLocation b: destination.inside)
			  if(b != null)
				b.getHandle().getBlock().setType(Material.AIR);
			portal.transmitter = false;
		  }
		}
	  }
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBurn(BlockIgniteEvent event)
	{
	  Block block = event.getBlock();
	  if(plugin.config.DisabledWorlds.contains(block.getLocation().getWorld().getName()))
		return;
	  BlockLocation loc;
	  for(BlockFace face: new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP})
	  {
		loc = new BlockLocation(block.getRelative(face));
		if (plugin.portalManager.borderBlocks.containsKey(loc) ||
				plugin.portalManager.behindBlocks.containsKey(loc))
		{
		  event.setCancelled(true);
		  return;
		}
		if(plugin.portalManager.insideBlocks.containsKey(loc))
		{
		  event.setCancelled(true);
		  Portal portal = plugin.portalManager.insideBlocks.get(loc);
		  if(!portal.open)
			return;
		  Portal dest = portal.getDestination();
		  
		  BlockLocation destl;
		  if(dest.horizontal || portal.inside[0].equals(loc))
			destl = dest.teleport[0];
		  else
			destl = dest.teleport[1];
		  block = destl.getHandle().getBlock();
		  if(block.getType() == Material.AIR)
			block.setType(Material.FIRE);
		  return;
		}
	  }
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBurn2(BlockBurnEvent event) {	
		BlockLocation loc = new BlockLocation(event.getBlock());
		if(plugin.config.DisabledWorlds.contains(loc.world))
		  return;
		if (plugin.portalManager.borderBlocks.containsKey(loc) ||
				plugin.portalManager.insideBlocks.containsKey(loc) ||
				plugin.portalManager.behindBlocks.containsKey(loc))
		{
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(plugin.config.DisabledWorlds.contains(event.getBlock().getLocation().getWorld().getName()))
		  return;
		Material block = event.getBlock().getType();
		
		if (block == Material.RAIL || block == Material.POWERED_RAIL || block == Material.DETECTOR_RAIL)
		  return;
		
		if (plugin.portalManager.insideBlocks.containsKey(new BlockLocation(event.getBlockPlaced())))
		  event.setCancelled(true);
	}

//	private boolean nearPortalOpening(Location loc)
//	{
////		if(plugin.config.DisabledWorlds.contains(loc.getWorld().getName()))
////			return false;
//		for (BlockLocation location : plugin.portalManager.insideBlocks.keySet())
//		{
//			if (loc.getWorld() != location.getHandle().getWorld())
//				continue;
//			if (loc.distanceSquared(location.getHandle()) <= 1)
//				return true;
//		}
//		return false;
//	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
//		Block block = event.getBlock();
		Block block = event.getSourceBlock();
//		if(block.getType() != Material.SUGAR_CANE || plugin.config.DisabledWorlds.contains(block.getLocation().getWorld().getName()))
//		  return;
		if(plugin.config.DisabledWorlds.contains(block.getLocation().getWorld().getName()))
			return;
//		BlockLocation loc = new BlockLocation(block);
//		if(plugin.grillManager.insideBlocks.containsKey(loc))
//		  event.setCancelled(true);
		if (plugin.portalManager.insideBlocks.containsKey(new BlockLocation(block)))
		{
			event.setCancelled(true);
			plugin.getLogger().info("canceled at " + event.getSourceBlock().getLocation());
		}
	}
	

	
//	@EventHandler(ignoreCancelled = true)
//	public void infiniteDispenser(BlockDispenseEvent event)
//	{
//	  if(plugin.config.DisabledWorlds.contains(event.getBlock().getLocation().getWorld().getName()))
//		return;
//	  BlockState bs = event.getBlock().getState();
//	  if(!(bs instanceof Dispenser))
//		return;
//	  Dispenser d = (Dispenser)bs;
//	  ItemStack is = d.getInventory().getItem(4);
//	  if(is == null)
//		return;
//	  Material mat = is.getType();
//	  BlockFace direction;
//	  if(plugin.config.getBoolean(PortalSetting.GEL_TUBE))
//	  {
//		ItemStack gel = plugin.util.getItemData(plugin.config.getString(PortalSetting.RED_GEL_BLOCK));
//		if(mat == gel.getType() && is.getDurability() == gel.getDurability())
//		{
//		  event.setCancelled(true);
//		  Block to = d.getBlock();
//		  BlockLocation from = new BlockLocation(to);
//		  if(plugin.gelManager.activeGelTubes.contains(from))
//			return;
//		  switch(d.getData().getData())
//		  {
//		  	case 2:
//		  	  direction = BlockFace.EAST;
//		  	  break;
//		  	case 3:
//		  	  direction = BlockFace.WEST;
//		  	  break;
//		  	case 4:
//			  direction = BlockFace.NORTH;
//			  break;
//		  	default:
//			  direction = BlockFace.SOUTH;
//		  }
//		  plugin.gelManager.tubePids.put(from, plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new GelTube(from, direction, mat.getId(), is.getData().getData()), 0L, 5L));
//		  plugin.gelManager.activeGelTubes.add(from);
//		  return;
//		}
//		else
//		{
//		  gel = plugin.util.getItemData(plugin.config.getString(PortalSetting.BLUE_GEL_BLOCK));
//		  if(mat == gel.getType() && is.getDurability() == gel.getDurability())
//		  {
//			event.setCancelled(true);
//			Block to = d.getBlock();
//			BlockLocation from = new BlockLocation(to);
//			if(plugin.gelManager.activeGelTubes.contains(from))
//			  return;
//			switch(d.getData().getData())
//			{
//			  case 2:
//			    direction = BlockFace.EAST;
//			  	break;
//			  case 3:
//			    direction = BlockFace.WEST;
//			    break;
//			  case 4:
//				direction = BlockFace.NORTH;
//				break;
//			  default:
//				direction = BlockFace.SOUTH;
//			}
//			  plugin.gelManager.tubePids.put(from, plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new GelTube(from, direction, mat.getId(), is.getData().getData()), 0L, 5L));
//			  plugin.gelManager.activeGelTubes.add(from);
//			  return;
//		  }
//		}
//	  }
//	  if(plugin.config.getBoolean(PortalSetting.INFINITE_DISPENSERS))
//	  {
//		if(is != null && is.getType() != Material.AIR)
//		  is.setAmount(is.getAmount() + 1);
//	  }
//	}
	
//	private class GelTube implements Runnable
//	{
//	  private final BlockLocation loc;
//	  private final BlockFace direction;
//	  private final Material mat;
//	  private final BlockData data;
//
//	  private GelTube(BlockLocation loc, BlockFace direction, Material mat, BlockData data)
//	  {
//		this.loc = loc;
//		this.direction = direction;
//		this.mat = mat;
//		this.data = data;
//	  }
//
//	  public void run()
//	  {
//		Block to = loc.getHandle().getBlock();
//		if(to.getType() != Material.DISPENSER || to.getBlockPower() == 0)
//		{
//		  plugin.gelManager.stopGelTube(loc);
//		  return;
//		}
//		to = to.getRelative(direction);
//		if(to.getType() != Material.AIR)
//		  return;
//		Location loc2 = to.getLocation();
//		to = to.getRelative(direction);
//		if(to.isLiquid())
//		  return;
//		Vector vector = new Vector();
//		double v = plugin.rand.nextDouble();
//		if(to.getType() != Material.AIR)
//		  vector.setY(-v);
//		else
//		{
//		  switch(direction)
//		  {
//		    case NORTH:
//		      vector.setX(-v);
//		      break;
//		  	case EAST:
//		  	  vector.setZ(-v);
//		  	  break;
//		  	case SOUTH:
//		  	  vector.setX(v);
//		  	  break;
//		  	default:
//		  	  vector.setZ(v);
//		  }
//		}
//		loc2.setX(loc2.getX()+0.5D);
//		loc2.setZ(loc2.getZ()+0.5D);
//		FallingBlock fb = loc2.getWorld().spawnFallingBlock(loc2, mat, data);
//		fb.setDropItem(false);
//		fb.setVelocity(vector);
//		plugin.gelManager.flyingGels.put(fb.getUniqueId(), loc);
//	  }
//	}
	
	@EventHandler()
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		if(event.getOldCurrent() == event.getNewCurrent())
		  return;
		 Block block = event.getBlock();
		 BlockLocation loc = new BlockLocation(block);
		 if(plugin.config.DisabledWorlds.contains(loc.world))
			 return;
		 
		 //Redstone teleportation
		 if (plugin.config.getBoolean(PortalSetting.ENABLE_REDSTONE_TRANSFER))
		 {			 
			 Location l = block.getLocation();
			 BlockFace face;
			 Block block2;
			 for (int i = 0; i < 5; i++)
			 {
				 face = BlockFace.values()[i];
				 loc = new BlockLocation(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
				 if (plugin.portalManager.insideBlocks.containsKey(loc)) 
					 {
					 	Portal portal = plugin.portalManager.insideBlocks.get(loc);
					 	if (!portal.open) continue;
					 
					 	Portal destination = portal.getDestination();
					 	if (destination == null || destination.transmitter) continue;
					 	
					 	Material mat1, mat2;
					 	if (event.getNewCurrent() > 0)
					 	{
					 		portal.transmitter = true;
					 		mat1 = Material.REDSTONE_TORCH;
					 		mat2 = Material.AIR;
					 	}
					 	else
					 	{
					 		portal.transmitter = false;
					 		mat1 = Material.AIR;
					 		mat2 = Material.REDSTONE_TORCH;
					 	}
					 	for (BlockLocation b: destination.inside)
					 	{
					 	  if(b != null)
					 	  {
					 		block2 = b.getHandle().getBlock();
					 		if(block2.getType() == mat2)
				 			block2.setType(mat1);
					 	  }
					 	}
					 }
			 }	 
		 }
		 

		 
		 //Portal Generators
		 if (event.getOldCurrent()  == 0 && event.getNewCurrent() > 0)
		 {
			 Block block2;
			 for (int i = 0; i < 5; i++)
			 {
				 block2 = block.getRelative(BlockFace.values()[i]);
				 if (Tag.WOOL.isTagged(block2.getType()))
					 plugin.portalManager.tryPlacingAutomatedPortal(block2);
			 }
		 }
	 }
	 
//	@EventHandler(ignoreCancelled = true)
//	 public void onBlockPistonExtend(BlockPistonExtendEvent event)
//	 {
//		if(plugin.config.DisabledWorlds.contains(event.getBlock().getLocation().getWorld().getName()))
//			  return;
//
//		 BlockBreakEvent bbe;
//		 BlockLocation loc = new BlockLocation(event.getBlock().getRelative(event.getDirection()));
//		 if(plugin.portalManager.insideBlocks.containsKey(loc))
//		 {
//			 Portal portal = plugin.portalManager.insideBlocks.get(loc);
//			 portal.delete();
//			 return;
//		 }
//
//		 for (final Block b : event.getBlocks())
//		 {
//			 fakeBBE = true;
//			 bbe = new BlockBreakEvent(b, null);
//			 onBlockBreak(bbe);
//			 if(bbe.isCancelled())
//			 {
//				 if(!fakeBBE)
//					 event.setCancelled(true);
//				 else
//					 fakeBBE = false;
//				 continue;
//			 }
//			 else
//				 fakeBBE = false;
//			 if (blockedPistonBlocks.contains(b))
//			 {
//				 event.setCancelled(true);
//				 return;
//			 }
//
//		 if (!plugin.config.getBoolean(PortalSetting.ENABLE_PISTON_BLOCK_TELEPORT))
//			 return;
//
//			 loc = new BlockLocation(b.getRelative(event.getDirection()));
//			 if(!plugin.portalManager.insideBlocks.containsKey(loc))
//				 continue;
//
//			 Portal portal = plugin.portalManager.insideBlocks.get(loc);
//			 if(!portal.open)
//				 continue;
//
//			 Portal destP = portal.getDestination();
//			 BlockLocation dest;
//
//			 if(destP.horizontal || portal.inside[0].equals(loc))
//				 dest = destP.teleport[0];
//			 else
//				 dest = destP.teleport[1];
//
//			 Block destB = dest.getHandle().getBlock();
//
//			 if (destB.isLiquid() || destB.getType() == Material.AIR)
//			 {
//				 destB.setTypeIdAndData(b.getType().getId(), b.getData(), true);
//				 final Block b2 = b.getRelative(event.getDirection());
//				 blockedPistonBlocks.add(b2);
//				 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
//				 {
//					 public void run()
//					 {
//						 b2.setType(Material.AIR);
//						 blockedPistonBlocks.remove(b2);
//					 }
//				 }, 2L);
//			 }
//			 else
//				 event.setCancelled(true);
//		}
//	 }
//
//	@EventHandler(ignoreCancelled = true)
//	 public void onBlockPistonRetract(BlockPistonRetractEvent event)
//	 {
//		 if(!event.isSticky())
//			 return;
//
//		 Block block = event.getRetractLocation().getBlock();
//		 if(plugin.config.DisabledWorlds.contains(block.getLocation().getWorld().getName()))
//			  return;
//
//		 fakeBBE = true;
//		 BlockBreakEvent bbe = new BlockBreakEvent(block, null);
//		 onBlockBreak(bbe);
//		 if(bbe.isCancelled())
//		 {
//			 if(!fakeBBE)
//				 event.setCancelled(true);
//			 else
//				 fakeBBE = false;
//			 return;
//		 }
//		 else
//			 fakeBBE = false;
//
//		 if (blockedPistonBlocks.contains(block))
//		 {
//			 event.setCancelled(true);
//			 return;
//		 }
//
//		 if (!plugin.config.getBoolean(PortalSetting.ENABLE_PISTON_BLOCK_TELEPORT))
//			 return;
//
//		 BlockLocation loc = new BlockLocation(event.getRetractLocation());
//		 Portal portal = plugin.portalManager.insideBlocks.get(loc);
//
//		 if (portal != null)
//		 {
//			 Portal destP = portal.getDestination();
//			 BlockLocation dest;
//			 if(destP.horizontal || portal.inside[0].equals(loc))
//				 dest = destP.teleport[0];
//			 else
//				 dest = destP.teleport[1];
//			 Block sourceB = dest.getHandle().getBlock();
//
//			 if (!sourceB.isLiquid() && sourceB.getType() != Material.AIR)
//			 {
//				 Block endBlock = event.getRetractLocation().getBlock();
//				 endBlock.setTypeIdAndData(sourceB.getTypeId(), sourceB.getData(), false);
//				 sourceB.setType(Material.AIR);
//			 }
//		 }
//		 else
//		 {
//			 if (plugin.portalManager.borderBlocks.containsKey(loc) || plugin.grillManager.borderBlocks.containsKey(loc) || plugin.grillManager.insideBlocks.containsKey(loc))
//				 event.setCancelled(true);
//		 }
//
//		 //Update bridge if piston made space
//		 plugin.funnelBridgeManager.updateBridge(new BlockLocation(event.getRetractLocation()));
//	 }
	 

}
