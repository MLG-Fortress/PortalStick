package com.matejdro.bukkit.portalstick.util;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class SavedBlock
{
  private final BlockLocation loc;
  public BlockData data;
  
  public SavedBlock(Block block)
  {
	loc = new BlockLocation(block);
	data = block.getBlockData();
  }
  
  public void reset()
  {
	Block b = loc.getHandle().getBlock();
	b.setBlockData(data, false);
  }
  
  @Override
  public int hashCode()
  {
	return loc.hashCode();
  }
  
  @Override
  public boolean equals(Object obj)
  {
	if(obj == null || !(obj instanceof SavedBlock))
	  return false;
	return loc.equals(((SavedBlock)obj).loc);
  }
}
