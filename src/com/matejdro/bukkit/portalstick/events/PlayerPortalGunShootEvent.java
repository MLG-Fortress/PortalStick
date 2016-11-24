package com.matejdro.bukkit.portalstick.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

import java.util.List;

/**
 * Created by RoboMWM on 11/23/2016.
 */
public class PlayerPortalGunShootEvent extends Event
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
    private Action action;


    public PlayerPortalGunShootEvent(Player player, List<Block> targetBlocks, Action action)
    {
        this.player = player;
        this.blocksInLineOfSight = targetBlocks;
        this.action = action;
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
        return this.player;
    }

    public Action getAction()
    {
        return this.action;
    }

}
