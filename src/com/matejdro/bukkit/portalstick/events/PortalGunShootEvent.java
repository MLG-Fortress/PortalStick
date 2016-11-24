package com.matejdro.bukkit.portalstick.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * Created by RoboMWM on 11/23/2016.
 */
public class PortalGunShootEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private Player player;
    private List<Block> blocksInLineOfSight;
    private boolean cancelled = false;

    public PortalGunShootEvent(Player player, List<Block> targetBlocks)
    {
        this.player = player;
        this.blocksInLineOfSight = targetBlocks;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean b)
    {
        cancelled = b;
    }

    public List<Block> getBlocksInLineOfSight()
    {
        return blocksInLineOfSight;
    }

    public Player getPlayer()
    {
        return player;
    }

}
