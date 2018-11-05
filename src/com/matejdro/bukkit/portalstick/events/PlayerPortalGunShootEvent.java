package com.matejdro.bukkit.portalstick.events;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.User;
import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

import java.util.List;

/**
 * Created by RoboMWM on 11/23/2016.
 */
public class PlayerPortalGunShootEvent extends Event implements Cancellable
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
    private DyeColor color;


    public PlayerPortalGunShootEvent(PortalStick plugin, Player player, List<Block> targetBlocks, Action action, boolean orange)
    {
        this.player = player;
        this.blocksInLineOfSight = targetBlocks;
        this.action = action;
        User user = plugin.userManager.getUser(player);
        if (orange)
            color = DyeColor.values()[plugin.util.getRightPortalColor(user.colorPreset)];
        else
            color = DyeColor.values()[plugin.util.getLeftPortalColor(user.colorPreset)];
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

    public DyeColor getColor()
    {
        return color;
    }
}
