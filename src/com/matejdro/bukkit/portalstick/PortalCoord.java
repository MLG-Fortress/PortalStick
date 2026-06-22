package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.block.BlockFace;

import com.matejdro.bukkit.portalstick.util.BlockLocation;

class PortalCoord {
	public final HashSet<BlockLocation> border = new HashSet<BlockLocation>();
	public final BlockLocation[] inside = new BlockLocation[2];
	public final BlockLocation[] behind = new BlockLocation[2];
	public BlockLocation block;
	public BlockLocation[] destLoc = new BlockLocation[2];
	public BlockFace tpFace;
	public boolean finished = false;
	public boolean vertical;
}