package tk.allele.duckshop.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import tk.allele.duckshop.DuckShop;
import tk.allele.duckshop.errors.InvalidSyntaxException;
import tk.allele.duckshop.signs.ChestLinkManager;
import tk.allele.duckshop.signs.TradingSign;
import tk.allele.permissions.PermissionsException;
import tk.allele.util.commands.CommandSenderPlus;

/**
 * Listens for block events -- like placing a sign.
 */
public class DuckShopBlockListener implements Listener {
    private final DuckShop plugin;

    public DuckShopBlockListener(DuckShop plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        CommandSenderPlus playerPlus = new CommandSenderPlus(player);

        TradingSign sign = null;
        try {
            sign = new TradingSign(plugin,
                    player,
                    event.getBlock().getLocation(),
                    event.getLines());
        } catch (InvalidSyntaxException ex) {
            // Do nothing
        } catch (PermissionsException ex) {
            // Science fiction allusions FTW
            event.setCancelled(true);
            playerPlus.error("I'm sorry, " + player.getName() + ". I'm afraid I can't do that.");
            event.getBlock().getState().update();
        }

        if (sign != null) {
            sign.writeToStringArray(event.getLines());
            playerPlus.info("Created sign successfully.");
            if (!sign.isGlobal()) {
                playerPlus.action("Type \"/duckshop link\" to connect this sign with a chest.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        CommandSenderPlus playerPlus = new CommandSenderPlus(player);
        Block block = event.getBlock();
        BlockState state = (block != null ? block.getState() : null);

        if (state instanceof Sign) {
            TradingSign sign = null;
            try {
                sign = new TradingSign(plugin,
                        block.getLocation(),
                        ((Sign) state).getLines());
            } catch (InvalidSyntaxException ex) {
                // Do nothing!
            }

            if (sign != null) {
                try {
                    sign.destroy(player);
                } catch (PermissionsException ex) {
                    event.setCancelled(true);
                    playerPlus.error("You can't break this!");
                    // Fixes the sign ending up blank
                    state.update();
                }
            }
        } else if (state instanceof Chest) {
        	ChestLinkManager chestMan = ChestLinkManager.getInstance(plugin);
        	if (state instanceof DoubleChest)
        	{
        		Chest lchest, rchest;
        		DoubleChest dchest = (DoubleChest) state;
        		lchest = (Chest) dchest.getLeftSide();
        		rchest = (Chest) dchest.getRightSide();
        		if (chestMan.isChestConnected(lchest.getLocation()) || chestMan.isChestConnected(rchest.getLocation())) event.setCancelled(true);
        	}
            if (chestMan.isChestConnected(block.getLocation())) 
            {
            	
            	playerPlus.warning("Warning: This chest is used by a DuckShop sign. You have to break the sign before you can destroy this chest.");
                event.setCancelled(true);
            }
        }
    }
}
