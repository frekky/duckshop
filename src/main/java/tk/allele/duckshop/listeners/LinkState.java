package tk.allele.duckshop.listeners;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import tk.allele.duckshop.signs.ChestLinkManager;
import tk.allele.duckshop.signs.TradingSign;
import tk.allele.permissions.PermissionsManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the state of any chest links in progress.
 */
public class LinkState {
    final ChestLinkManager manager;
    final PermissionsManager permissions;
    final Map<Player, Boolean> playerStartedLink = new HashMap<Player, Boolean>();
    final Map<Player, TradingSign> playerLinkSign = new HashMap<Player, TradingSign>();

    public LinkState(ChestLinkManager manager, PermissionsManager permissions) {
        this.manager = manager;
        this.permissions = permissions;
    }

    public void startLink(Player player) {
        playerStartedLink.put(player, Boolean.TRUE);
    }

    public boolean hasStartedLink(Player player) {
        return playerStartedLink.containsKey(player);
    }

    public void markSign(Player player, TradingSign sign) {
        playerLinkSign.put(player, sign);
    }

    public boolean hasMarkedSign(Player player) {
        return playerLinkSign.containsKey(player);
    }

    public void markChest(Player player, Location location) {
        TradingSign sign = playerLinkSign.get(player);
        sign.setChestLocation(location);
        cancelLink(player);
        Sign state = (Sign) sign.getSignLocation().getBlock().getState();
        sign.writeToStringArray(state.getLines()); // Update sign after linking to generate fill-o-meter
        state.update();
    }

    public void cancelLink(Player player) {
        playerStartedLink.remove(player);
        playerLinkSign.remove(player);
    }
}
