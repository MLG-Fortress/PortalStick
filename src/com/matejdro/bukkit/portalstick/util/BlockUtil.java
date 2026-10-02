package com.matejdro.bukkit.portalstick.util;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import de.V10lator.PortalStick.V10Location;

public class BlockUtil {
	public boolean compareBlockToString(V10Location block, String blockData)
	{
	  return compareBlockToString(block.getHandle().getBlock(), blockData);
	}
	
	public boolean compareBlockToString(Block block, String blockData) {
		return block.getType().name().equals(blockData);
//		String[] blockArr = blockData.split(":");
//		if (blockArr.length > 1)
//			return (block.getTypeId() == Integer.parseInt(blockArr[0]) && block.getData() == Integer.parseInt(blockArr[1]));
//		else
//			return block.getTypeId() == Integer.parseInt(blockArr[0]);
	}
	
//	public void setBlockData(V10Location block, String blockData) {
//	  setBlockData(block.getHandle().getBlock(), blockData);
//	}
//
//	public void setBlockData(Block block, String blockData) {
//		String[] blockArr = blockData.split(":");
//		block.setTypeId(Integer.parseInt(blockArr[0]));
//		if (blockArr.length > 1)
//			block.setData((byte) Integer.parseInt(blockArr[1]));
//	}
//
//	public String getBlockData(Block block) {
//		if (block.getData() != 0)
//			return block.getTypeId() + ":" + block.getData();
//		return Integer.toString(block.getTypeId());
//	}
	
	public BlockFace getFaceOfMaterial(Block block, BlockFace[] faces, String material, HashMap<BlockFace, Block> faceMap) {
		Block block2;
		for (BlockFace face : faces)
		{
			if(faceMap.containsKey(face))
				block2 = faceMap.get(face);
			else
			{
				block2 = block.getRelative(face);
				faceMap.put(face, block2);
			}
			if (compareBlockToString(block2, material))
				return face;
		}
		return null;
	}
}
