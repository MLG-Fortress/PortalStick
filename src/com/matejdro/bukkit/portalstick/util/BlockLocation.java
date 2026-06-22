package com.matejdro.bukkit.portalstick.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * This represents a block location.
 * You can store this over time.
 * @author V10lator
 *
 */
public class BlockLocation
{
  public final String world;
  public final int x, y, z;
  
  public BlockLocation(Location loc)
  {
	this(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
  }
  
  public BlockLocation(Block block)
  {
	this(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
  }
  
  public BlockLocation(String world, int x, int y, int z)
  {
	this.world = world;
	this.x = x;
	this.y = y;
	this.z = z;
  }
  
  public Location getHandle()
  {
	World world = Bukkit.getWorld(this.world);
	return world == null ? null : new Location(world, x, y, z);
  }
  
  @Override
  public int hashCode()
  {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((world == null) ? 0 : world.hashCode());
	result = prime * result + x;
	result = prime * result + y;
	result = prime * result + z;
	return result;
  }
  
  @Override
  public boolean equals(Object obj)
  {
	if(this == obj)
	  return true;
	if(obj == null || !(obj instanceof BlockLocation))
	  return false;
	BlockLocation other = (BlockLocation)obj;
	return x == other.x && y == other.y && z == other.z && world.equals(other.world);
  }
  
  @Override
  public BlockLocation clone()
  {
	return new BlockLocation(world, x, y, z);
  }
}
