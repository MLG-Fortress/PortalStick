package de.V10lator.PortalStick;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockHolder
{
  private final V10Location loc;
  public Material id;
  public BlockData data;
  
  public BlockHolder(Block block)
  {
	loc = new V10Location(block);
	id = block.getType();
	data = block.getBlockData();
  }
  
  public void reset()
  {
	Block b = loc.getHandle().getBlock();
	b.setType(id);
	b.setBlockData(data);
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
