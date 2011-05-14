package com.matejdro.bukkit.portalstick;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class GelManager {
	public static Location useGel(Entity entity, Location LocTo, Vector vector)
	{
		Region region = RegionManager.getRegion(LocTo);
		Block block = LocTo.getBlock();
		if (vector.getY() <= 0)
		{
			if (BlockUtil.compareBlockToString(block.getRelative(0,-1,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 0);
			else if (Math.abs(vector.getY()) > 0.3 && BlockUtil.compareBlockToString(block.getRelative(0,-2,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 0);
			else if (Math.abs(vector.getY()) > 0.5 && BlockUtil.compareBlockToString(block.getRelative(0,-3,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 0);
		}
		if (vector.getX() >= 0)
		{
			if (BlockUtil.compareBlockToString(block.getRelative(1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 1);
			else if (BlockUtil.compareBlockToString(block.getRelative(2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 1);
			else if (BlockUtil.compareBlockToString(block.getRelative(3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 1);

		}
		else if (vector.getX() <= 0)
		{
			if (BlockUtil.compareBlockToString(block.getRelative(-1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 2);
			else if (BlockUtil.compareBlockToString(block.getRelative(-2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 2);
			else if (BlockUtil.compareBlockToString(block.getRelative(-3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 2);

		}
		if (vector.getZ() >= 0)
		{
			if (BlockUtil.compareBlockToString(block.getRelative(0,0,1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 3);
			else if (BlockUtil.compareBlockToString(block.getRelative(0,0,2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 3);
			else if (BlockUtil.compareBlockToString(block.getRelative(0,0,3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 3);

		}
		else if (vector.getZ() <=0 )
		{
			if (BlockUtil.compareBlockToString(block.getRelative(0,0,-1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 4);
			else if (BlockUtil.compareBlockToString(block.getRelative(0,0,-2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 4);
			else if (BlockUtil.compareBlockToString(block.getRelative(0,0,-3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 4);

		}
		
		return null;
	}
	
	public static Location BlueGel(Entity entity, Vector vector, Region region, int direction)
	{
		if (entity instanceof Player && ((Player) entity).isSneaking()) return null;
			vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
			vector = vector.setY(-vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
			vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
			switch (direction)
			{
				case 0:
					if (vector.getY() < region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_BOUNCE_VELOCITY)) vector.setY(region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_BOUNCE_VELOCITY));
					break;
				case 1:
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getX() > -region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setX(-region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 2:
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getX() < region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setX(region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 3:
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getZ() > -region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setZ(-region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 4:
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getZ() < region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setZ(region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
					
					
			}
			entity.setVelocity(vector);
		return null;
	}
	
	public Location redGel()
	{
		return null;
	}
}
