package com.matejdro.bukkit.portalstick;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class User {
	public Portal bluePortal;
	public Portal orangePortal;
	public int colorPreset = 0;
	public final String name;
	public final UUID uuid;
	public final boolean isPlayer;
	
	public User(String name)
	{
	  this.name = name;
	  uuid = null;
	  isPlayer = false;
	}
	
	public User(Entity entity)
	{
	  if(entity instanceof Player)
	  {
		name = ((Player)entity).getName();
		isPlayer = true;
	  }
	  else
	  {
		name = null;
		isPlayer = false;
	  }
	  uuid = entity.getUniqueId();
	}
	
	public void recreatePortals()
	{
		if (bluePortal != null) bluePortal.recreate();
		if (orangePortal != null) orangePortal.recreate();
	}
	
}
