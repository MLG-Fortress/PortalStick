package com.matejdro.bukkit.portalstick.listeners;

import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class PortalEntitiesListener implements Listener
{
    private final PortalStick portalStick;

    public PortalEntitiesListener(PortalStick portalStick)
    {
        this.portalStick = portalStick;
    }

    public boolean disabledWorld(World world)
    {
        return portalStick.config.DisabledWorlds.contains(world.getName());
    }

    public boolean disabledWorld(Location location)
    {
        return disabledWorld(location.getWorld());
    }

    public void smartTrackEntity(Entity entity)
    {
        if (entity.getType() == EntityType.PLAYER)
            return;
        if (!entity.hasMetadata("TRACKED") && isNearPortal(entity.getLocation()))
        {
            trackEntity(entity, false);
        }
    }

    public boolean isNearPortal(Location location)
    {
        World world = location.getWorld();
        if(portalStick.config.DisabledWorlds.contains(world.getName()))
            return false;
        for (Portal portal : portalStick.portalManager.portals)
        {
            Location portalLocation = portal.inside[0].getHandle();
            try
            {
                if (location.distanceSquared(portalLocation) < 49) //7 blocks
                    return true;
            }
            catch (Exception e)
            {
                // Just skip if there's an issue (null, not same world, etc.)
            }
        }
        return false;
    }

    public void trackEntity(Entity entity)
    {
        trackEntity(entity, true);
    }

    public void trackEntity(Entity entity, boolean check)
    {
        if (check)
        {
            if (entity.getType() == EntityType.PLAYER)
                return;
            if (entity.hasMetadata("TRACKED"))
                return;
        }
        entity.setMetadata("TRACKED", new FixedMetadataValue(portalStick, true));
        new BukkitRunnable()
        {
            Location previousLocation = entity.getLocation();
            public void run()
            {
                if (entity.isDead() || !entity.isValid())
                {
                    this.cancel();
                    return;
                }
                try
                {
                    if (entity.getLocation().distanceSquared(previousLocation) == 0)
                        return; //Don't call if it didn't move
                }
                catch (Exception e) //Somehow moved to another world, or otherwise erroring
                {
                    this.cancel();
                    entity.removeMetadata("TRACKED", portalStick);
                    return;
                }

                Location to = portalStick.entityManager.onEntityMove(entity, previousLocation, entity.getLocation(), false);
                if (to != null)
                {
                    entity.teleport(to);
                    previousLocation = to;
                }
                else
                {
                    previousLocation = entity.getLocation();
                }
            }
        }.runTaskTimer(portalStick, 1L, 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onBlockFall(EntityChangeBlockEvent event)
    {
        if (disabledWorld(event.getBlock().getLocation())) return;

        if (event.getTo() == Material.AIR && event.getEntityType() == EntityType.FALLING_BLOCK)
            smartTrackEntity(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onItemSpawn(ItemSpawnEvent event)
    {
        if (disabledWorld(event.getLocation())) return;
        smartTrackEntity(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerPlaceABlockThatFalls(BlockPlaceEvent event)
    {
        if (disabledWorld(event.getBlock().getLocation())) return;
        Block block = event.getBlock();
        switch (block.getType())
        {
            case SAND:
            case GRAVEL:
                new BukkitRunnable()
                {
                    public void run()
                    {
                        for (Entity entity : block.getLocation().getChunk().getEntities())
                        {
                            if (entity.getType() == EntityType.FALLING_BLOCK)
                                smartTrackEntity(entity);
                        }
                    }
                }.runTaskLater(portalStick, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerIgniteTNT(PlayerInteractEvent event)
    {
        Block block = event.getClickedBlock();
        if (block == null || disabledWorld(block.getLocation())) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getMaterial() == Material.FLINT_AND_STEEL && block.getType() == Material.TNT)
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    for (Entity entity : block.getLocation().getChunk().getEntities())
                    {
                        if (entity.getType() == EntityType.PRIMED_TNT)
                            smartTrackEntity(entity);
                    }
                }
            }.runTaskLater(portalStick, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerIgniteTNTByBreaking(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        if (disabledWorld(block.getLocation())) return;

        if (event.getBlock().getType() == Material.TNT)
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    for (Entity entity : block.getLocation().getChunk().getEntities())
                    {
                        if (entity.getType() == EntityType.PRIMED_TNT)
                            smartTrackEntity(entity);
                    }
                }
            }.runTaskLater(portalStick, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onProjectileSomething(ProjectileLaunchEvent event)
    {
        if (disabledWorld(event.getEntity().getLocation())) return;
        trackEntity(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onMobSpawn(EntitySpawnEvent event)
    {
        if (disabledWorld(event.getLocation())) return;
        trackEntity(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onChunkLoad(ChunkLoadEvent event)
    {
        if (disabledWorld(event.getWorld())) return;

        if (event.isNewChunk())
            return;
        for (Entity entity : event.getChunk().getEntities())
        {
            if (entity instanceof LivingEntity) 
            {
                trackEntity(entity);
            }
        }

    }

}
