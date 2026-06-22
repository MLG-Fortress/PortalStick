package com.matejdro.bukkit.portalstick.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * This holds a location + a velocity to give it across classes.
 * This isn't meaned to be saved over time.
 * @author V10lator
 *
 */
public class TeleportResult
{
  public final Location to;
  public final Vector velocity;
  
  public TeleportResult(Location to, Vector velocity)
  {
	this.to = to;
	this.velocity = velocity;
  }
}
