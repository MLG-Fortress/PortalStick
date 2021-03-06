package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;
import de.V10lator.PortalStick.V10Teleport;

public class EntityManager implements Runnable {
	private final PortalStick plugin;
	private final HashSet<Entity> blockedEntities = new HashSet<Entity>();
	final HashMap<UUID, Location> oldLocations = new HashMap<UUID, Location>();

	EntityManager(PortalStick instance)
	{
		plugin = instance;
	}

	public V10Teleport teleport(Entity entity, Location oloc, V10Location locTo, Vector vector, boolean really)
	{
		if (entity == null || entity.isDead() || blockedEntities.contains(entity))
		  return null;

		Region regionTo = plugin.regionManager.getRegion(locTo);
		Portal portal = plugin.portalManager.insideBlocks.get(locTo);
		final Location teleport;
		final Portal destination;
		boolean ab = portal == null;
		if(!ab)
		{
		  if(!portal.open)
			return null;
		  destination = portal.getDestination();
		  if(destination.horizontal || portal.inside[0].equals(locTo))
			teleport = destination.teleport[0].getHandle();
		  else
			teleport = destination.teleport[1].getHandle();
		}
		else
		{
		  if((entity instanceof FallingBlock || entity instanceof TNTPrimed) && vector.getX() == 0.0D && vector.getZ() == 0.0D)
		  {
			portal = plugin.portalManager.awayBlocksY.get(locTo);
			if(!plugin.portalManager.awayBlocksY.containsKey(locTo))
			  return null;
			portal = plugin.portalManager.awayBlocksY.get(locTo);
			if(!portal.open)
			  return null;
			destination = portal.getDestination();
			teleport = destination.teleport[0].getHandle();
		  }
		  else if((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat)
		  {
			if(!plugin.portalManager.awayBlocks.containsKey(locTo))
			  return null;
			portal = plugin.portalManager.awayBlocks.get(locTo);
			
			if(!portal.open)
			  return null;
			
			destination = portal.getDestination();
			if(destination.horizontal || portal.teleport[0].y <= locTo.y)
			  teleport = destination.teleport[0].getHandle();
			else
			  teleport = destination.teleport[1].getHandle();
			
			Block to = locTo.getHandle().getBlock();
			for(int i = 0; i < 2; i++)
			{
			  BlockFace face = portal.awayBlocksY[i].getHandle().getBlock().getFace(to);
			  if(face == null)
				continue;
			  if(face != BlockFace.SELF)
			  {
				double x = 1.0D, z = 1.0D;
				boolean nef = false;
				boolean nwf = false;
				switch(face)
				{
			  	  case NORTH_WEST:
			  		z = 0.5D;
			  	  case NORTH:
			  		x = 0.5D;
			  		nwf = true;
				  break;
			  	  case NORTH_EAST:
			  		x = 1.5D;
			  	  case EAST:
			  		z = 0.5D;
			  		nef = true;
			  		break;
			  	  case SOUTH_EAST:
			  		z = 1.5D;
			  	  case SOUTH:
			  		x = 0.5D;
			  		break;
			  	  case SOUTH_WEST:
			  		x = 0.5D;
			  	  default:
			  		z = 0.5D;
				}
				if(nef)
				{
				  if(oloc.getX() - locTo.x > x || oloc.getZ() - locTo.z < z)
					return null;
				}
				else if(nwf)
				{
				  if(oloc.getX() - locTo.x < x || oloc.getZ() - locTo.z > z)
					return null;
				}
				else if(oloc.getX() - locTo.x > x || oloc.getZ() - locTo.z > z)
				  return null; 
			  }
			  else
				break;
			}
		  }
		  else
			return null;
		}
		
		if(portal.disabled || (Math.abs(vector.getY()) > 1 && !portal.horizontal))
		  return null;
		
		teleport.setX(teleport.getX() + 0.5D);
		teleport.setZ(teleport.getZ() + 0.5D);
							 
		float yaw = (entity.getLocation().getYaw() + 360) % 360; //https://github.com/EssentialsX/Essentials/blob/849efa9756495a34260d57ff1d81df1283e5f5e7/Essentials/src/com/earth2me/essentials/commands/Commandgetpos.java#L43
		float pitch = entity.getLocation().getPitch();
		final float startyaw = yaw;
		final float startpitch = pitch; //Just to make it easier to read what's going on
		double momentum = 0.0;
		
		/*
		* The following sets the yaw (or pitch) relative to the portal.
		* E.g. yaw = 0 means player is directly facing portal
		* The following assumptions are made (also present in Location javadoc):
		* yaw: 0 - 360 (south -> west -> north -> east) (increasing values = turning right)
		* pitch: -90 - 90 (up -> down) (increasing values = looking down)
		*/
		
		switch(portal.teleportFace)
	       {
	       	case NORTH:
				yaw -= 180;
	       		momentum = vector.getZ();
				//Bukkit.broadcastMessage("North enter");
	       		break;
	       	case EAST:
	       		yaw -= 270;
	       		momentum = vector.getX();
				//Bukkit.broadcastMessage("East enter");
	       		break;
	       	case SOUTH:
	       		momentum = vector.getZ();
				//Bukkit.broadcastMessage("South enter");
	       		break;
	       	case WEST:
	       		yaw -= 90;
	       		momentum = vector.getX();
				//Bukkit.broadcastMessage("West enter");
	       		break;
	       	case UP: //bottom portal
				momentum = vector.getY();
				pitch += 90;
				yaw = 0; //Not possible to determine yaw, since ground/ceiling portals do NOT have an orientation!
				//Bukkit.broadcastMessage("Up enter");
				break;
	       	case DOWN: //top portal
	       		momentum = vector.getY();
				pitch -= 90;
				yaw = 0;
				//Bukkit.broadcastMessage("Down enter");
	       		break;
	       }

	    yaw = (yaw + 360) % 360;
		momentum = Math.abs(momentum);
		momentum *= regionTo.getDouble(RegionSetting.VELOCITY_MULTIPLIER);
			//reposition velocity to match output portal's orientation
			//Also sets direction (yaw and/or pitch) relative to portal
		Vector outvector = entity.getVelocity().zero();
		switch(destination.teleportFace)
        {
        	case NORTH:
        		outvector = outvector.setZ(momentum);
				//Bukkit.broadcastMessage("North exit");
        		break;
        	case EAST:
        		yaw += 90;
        		outvector = outvector.setX(-momentum);
				//Bukkit.broadcastMessage("East exit");
        		break;
        	case SOUTH:
        		yaw += 180;
        		outvector = outvector.setZ(-momentum);
				//Bukkit.broadcastMessage("South exit");
        		break;
        	case WEST:
        		yaw += 270;
        		outvector = outvector.setX(momentum);
				//Bukkit.broadcastMessage("West exit");
        		break;
			//Ground portal
        	case DOWN:
				//Bukkit.broadcastMessage("Down exit");
				switch (portal.teleportFace)
				{
					case UP:
						yaw = startyaw;
						pitch = startpitch;
						break;
					case DOWN:
						//yaw = startyaw + 180; //Not really possible to determine yaw due to no orientation, so we'll just use same assumptions as before.
						yaw = startyaw; //Flipping client's yaw can cause client side lag(?) (Possibly because it needs to render what was previously not visible? Testing with low render distance seems to alleviate this lag, so answer is probably yes)
						pitch = -startpitch;
						break;
					default:
						//translate yaw range to -180 - 180,
						// get absolute value (we only care about player's facing angle to originating portal),
						// Multiply by -1 to invert
						pitch = (Math.abs(180 - yaw) - 90) * -1;
						yaw = startyaw;
						//if (yaw <= 180)
						//	pitch = (yaw - 180) + 90;
						//else
						//	pitch = (180 - yaw) + 90;
				}
        		outvector = outvector.setY(momentum);
        		break;
			//Ceiling portal
        	case UP:
				//Bukkit.broadcastMessage("Up exit");
        		switch (portal.teleportFace)
				{
					case DOWN:
						yaw = startyaw;
						pitch = startpitch;
						break;
					case UP:
						//yaw = startyaw + 180;
						yaw = startyaw;
						pitch = -startpitch;
						break;
					default:
						//translate yaw range to -180 - 180,
						// get absolute value (we only care about player's facing angle to originating portal)
						pitch = Math.abs(180 - yaw) - 90;
						//Bukkit.broadcastMessage(yaw + " " + pitch);
						yaw = startyaw;
				}
        		outvector = outvector.setY(-momentum);
        		break;
        }

		//if pitch exceeds |120|, rotate yaw accordingly
		if (Math.abs(pitch) > 120) //TODO: positive pitch values probably work differently
		{
			yaw = yaw - 180;
			pitch = -180 - pitch;
			//Bukkit.broadcastMessage("Flipped");
		}
		
		if (!(entity instanceof Player) && !(entity instanceof Chicken) && !(entity instanceof Bat) && (portal.teleportFace == BlockFace.UP || portal.teleportFace == BlockFace.DOWN) && (destination.teleportFace == BlockFace.UP || destination.teleportFace == BlockFace.DOWN) && plugin.rand.nextInt(100) < 5)
		{
		  double d = plugin.rand.nextDouble();
		  if(d > 0.5D)
			d -= 0.5D;
		  if(ab)
			d += 0.5D;
		  if(plugin.rand.nextBoolean())
			d = -d;
		  if(plugin.rand.nextBoolean())
			teleport.setX(teleport.getX() + d);
		  else
			teleport.setZ(teleport.getZ() + d);
		}
		
		entity.setFallDistance(0);
		teleport.setPitch(pitch);
		teleport.setYaw(yaw);
		
		if (entity instanceof Arrow)
			teleport.setY(teleport.getY() + 0.5);
		
		if(really)
		{
		  if(!entity.teleport(teleport))
			return null;
		  entity.setVelocity(outvector);
		}
		
		destination.disabled = true;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){destination.disabled = false;}}, 10L);

		final boolean orange = portal.orange;

        if (orange)
            plugin.util.playSound(Sound.PORTAL_ENTER_ORANGE, new V10Location(oloc));
        else
            plugin.util.playSound(Sound.PORTAL_ENTER_BLUE, new V10Location(oloc));


		//Delay playing the sound by a tick so the player exiting will hear it
		//Also play the enter sound, since the player entering won't hear it (unless the portals are nearby each other)
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (orange)
				{
					plugin.util.playSound(Sound.PORTAL_ENTER_ORANGE, new V10Location(teleport));
					plugin.util.playSound(Sound.PORTAL_EXIT_ORANGE, new V10Location(teleport));
				}
				else
				{
					plugin.util.playSound(Sound.PORTAL_ENTER_BLUE, new V10Location(teleport));
					plugin.util.playSound(Sound.PORTAL_EXIT_BLUE, new V10Location(teleport));
				}
			}
		}.runTask(plugin);

		return new V10Teleport(teleport, outvector);
	}
	
	@Override
	public void run()
	{
		faceCache.clear();
	}
	
	HashMap<V10Location, HashMap<BlockFace, Block>> faceCache = new HashMap<V10Location, HashMap<BlockFace, Block>>();
	
	public Location onEntityMove(final Entity entity, Location locFrom, Location locTo, boolean tp)
	{
		if (entity.isInsideVehicle())
		  return null;
		
		double d = locTo.getBlockY();
		if(d > locTo.getWorld().getMaxHeight() - 1 || d < 0)
		  return null;

		//TODO: may need to check world/try-catch?
		Vector vec2 = locTo.toVector();
		V10Location vlocTo = new V10Location(locTo);
		Location oloc = locTo;
		locTo = vlocTo.getHandle();
		Vector vec1 = locFrom.toVector();
		V10Location vlocFrom = new V10Location(locFrom);
		if(vlocTo.equals(vlocFrom))
		  return null;
		
	    Vector vector = vec2.subtract(vec1);
	    vector.setY(entity.getVelocity().getY());
	    
	    Region regionTo = plugin.regionManager.getRegion(vlocTo);
		Region regionFrom = plugin.regionManager.getRegion(vlocFrom);
		
		//Check for changing regions
	    plugin.portalManager.checkEntityMove(entity, regionFrom, regionTo);
		
		//Emancipation grill
		if (regionTo.getBoolean(RegionSetting.ENABLE_GRILLS))
		{
			Grill grill = plugin.grillManager.insideBlocks.get(vlocTo);
			if (grill != null && !grill.disabled)
			{
				plugin.grillManager.emancipate(entity);
				return null;
			}
		}
		
		//Aerial faith plate
		Block blockIn = locTo.getBlock();
		HashMap<BlockFace, Block> faceMap;
		if(faceCache.containsKey(vlocTo))
		  faceMap = faceCache.get(vlocTo);
		else
		{
		  faceMap = new HashMap<BlockFace, Block>();
		  faceCache.put(vlocTo, faceMap);
		}
		Block blockUnder;
		if(faceMap.containsKey(BlockFace.DOWN))
		  blockUnder = faceMap.get(BlockFace.DOWN);
		else
		{
		  blockUnder = blockIn.getRelative(BlockFace.DOWN);
		  faceMap.put(BlockFace.DOWN, blockUnder);
		}
		  
		//  blockUnder = blockIn.getRelative(BlockFace.DOWN);
		  
		if (regionTo.getBoolean(RegionSetting.ENABLE_AERIAL_FAITH_PLATES))
		{
			Block blockStart = null;
			d = Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[0]);
			String faithBlock = regionTo.getString(RegionSetting.FAITH_PLATE_BLOCK);
			Vector velocity = new Vector(0, Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[1]),0);
			
			if (blockIn.getType() == Material.STONE_PRESSURE_PLATE && plugin.blockUtil.compareBlockToString(blockUnder, faithBlock))
				blockStart = blockUnder;
			else
				blockStart = blockIn;
			if (blockStart != null) {
				BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
				BlockFace face = plugin.blockUtil.getFaceOfMaterial(blockStart, faces, faithBlock, faceMap);
				if (face != null) {
					switch (face) {
						case NORTH:
							velocity.setX(d);
							break;
						case SOUTH:
							velocity.setX(-d);
							break;
						case EAST:
							velocity.setZ(d);
							break;
						case WEST:
							velocity.setZ(-d);
							break;
					}
					if (blockStart == blockUnder) {
						velocity.setX(-velocity.getX());
						velocity.setZ(-velocity.getZ());
					}
					entity.setVelocity(velocity);
					plugin.util.playSound(Sound.FAITHPLATE_LAUNCH, new V10Location(blockStart.getLocation()));
					return null;
				}
			}
		
		}
		Location ret = null;
		//Teleport
		if (!(entity instanceof Player) || plugin.hasPermission((Player)entity, plugin.PERM_TELEPORT))
		{
		  final V10Teleport to = teleport(entity, oloc, vlocTo, vector, tp);
		  if(to != null)
		  {
			ret = to.to;
			vlocTo = new V10Location(ret);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){entity.setVelocity(to.velocity);}});
		  }
		}
		
		//Gel
		if(!plugin.gelManager.flyingGels.containsKey(entity.getUniqueId()))
		  plugin.gelManager.useGel(entity, vlocTo, vector, blockIn, blockUnder, faceMap);
		
		//Funnel
//		plugin.funnelBridgeManager.EntityMoveCheck(entity);
		
		return ret;
	}
}

