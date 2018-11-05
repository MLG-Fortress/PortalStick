package de.V10lator.PortalStick;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockHolder
{
  private final V10Location loc;
  public BlockData data;
  
  public BlockHolder(Block block)
  {
	loc = new V10Location(block);
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
	if(obj == null || !(obj instanceof BlockHolder))
	  return false;
	return loc.equals(((BlockHolder)obj).loc);
  }
}
