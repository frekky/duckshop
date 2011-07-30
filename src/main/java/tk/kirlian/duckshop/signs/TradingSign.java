package tk.kirlian.duckshop.signs;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import tk.kirlian.duckshop.DuckShop;
import tk.kirlian.duckshop.errors.CannotTradeException;
import tk.kirlian.duckshop.errors.ChestProtectionException;
import tk.kirlian.duckshop.errors.InvalidChestException;
import tk.kirlian.duckshop.errors.InvalidSyntaxException;
import tk.kirlian.duckshop.items.Item;
import tk.kirlian.duckshop.trading.ChestInventoryAdapter;
import tk.kirlian.duckshop.trading.GlobalSignAdapter;
import tk.kirlian.duckshop.trading.PlayerInventoryAdapter;
import tk.kirlian.duckshop.trading.TradeAdapter;
import tk.kirlian.permissions.PermissionsException;
import tk.kirlian.util.Locations;

import java.util.logging.Logger;

/**
 * Represents a sign that can be used as a shop.
 */
public class TradingSign {
    private DuckShop plugin;
    private Logger log;
    private String ownerName;
    private Location signLocation;
    private boolean global;
    private Item sellerToBuyer, buyerToSeller;

    /**
     * Create a new TradingSign instance. The placing player may be null
     * if unknown.
     *
     * @throws InvalidSyntaxException if the lines cannot be parsed.
     * @throws PermissionsException if the syntax is valid, but the
     *         placing player does not have the required permissions.
     */
    public TradingSign(DuckShop plugin, Player placingPlayer, Location signLocation, String[] lines)
      throws InvalidSyntaxException, PermissionsException {
        this.plugin = plugin;
        this.log = plugin.log;
        this.signLocation = signLocation;

        // Trim whitespace
        for(int i = 0; i < lines.length; ++i) {
            lines[i] = lines[i].trim();
        }

        // Check if global or personal
        this.global = lines[0].equalsIgnoreCase("[Global]");
        if(!global) {
            // If name is blank, use the placing player's name
            if(lines[0].length() == 0 || lines[0].equalsIgnoreCase("[Personal]")) {
                if(placingPlayer != null) {
                    this.ownerName = placingPlayer.getName();
                } else {
                    throw new InvalidSyntaxException();
                }
            // Otherwise, use the name written on the sign
            } else {
                this.ownerName = lines[0];
                // Normalize the name to use correct capitalization
                Player owner = plugin.getServer().getPlayer(ownerName);
                if(owner != null) {
                    this.ownerName = owner.getName();
                }
            }
        }

        // Parse the two middle lines
        sellerToBuyer = Item.fromString(lines[1]);
        buyerToSeller = Item.fromString(lines[2]);

        // Do permissions check at the end, after trying to parse
        // So players who don't have permissions can still place non-trading signs
        if(placingPlayer != null) {
            plugin.permissions.getBest().throwIfCannot(placingPlayer, "create." + getActionType(placingPlayer));
        }
    }

    /**
     * Utility method for permissions checking.
     */
    private String getActionType(Player actingPlayer) {
        if(global) {
            return "global";
        } else if(ownerName.equalsIgnoreCase(actingPlayer.getName())) {
            return "personal";
        } else {
            return "other";
        }
    }

    /**
     * Update a String array with the contents of this TradingSign.
     *
     * @throws IllegalArgumentException if the array is not of length 4.
     */
    public void writeToStringArray(String[] lines) {
        if(lines.length != 4) {
            throw new IllegalArgumentException("String array must be of length 4");
        }
        if(global) {
            lines[0] = "[Global]";
        } else {
            lines[0] = ownerName;
        }
        lines[1] = sellerToBuyer.toString();
        lines[2] = buyerToSeller.toString();
        lines[3] = "";
    }

    /**
     * Return a TradeAdapter corresponding to this sign.
     *
     * @throws InvalidChestException if it requires a chest, but is not connected to one.
     * @throws ChestProtectionException if the owner does not have access to the chest.
     *
     * @see TradeAdapter
     */
    public TradeAdapter getAdapter() throws InvalidChestException, ChestProtectionException {
        if(global) {
            return new GlobalSignAdapter(plugin);
        } else {
            Location chestLocation = getChestLocation();
            if(chestLocation != null) {
                return new ChestInventoryAdapter(plugin, ownerName, chestLocation);
            } else {
                throw new InvalidChestException();
            }
        }
    }

    /**
     * Return whether this sign is <i>global</i>.
     * <p>
     * A sign is global if it is not associated with a player.
     */
     public boolean isGlobal() {
         return global;
     }

    /**
     * Attempt to trade with another TradeAdapter.
     *
     * @throws InvalidChestException if the chest is invalid (duh)
     * @throws CannotTradeException if any party doesn't have enough to trade
     * @throws ChestProtectionException if the chest is protected
     */
    public void tradeWith(final TradeAdapter buyerAdapter)
      throws InvalidChestException, CannotTradeException, ChestProtectionException {
        final TradeAdapter sellerAdapter = getAdapter();
        // ARGH!!!
        if(sellerAdapter.canAddItem(buyerToSeller) &&
           sellerAdapter.canSubtractItem(sellerToBuyer) &&
           buyerAdapter.canAddItem(sellerToBuyer) &&
           buyerAdapter.canSubtractItem(buyerToSeller)) {
            sellerAdapter.addItem(buyerToSeller);
            sellerAdapter.subtractItem(sellerToBuyer);
            buyerAdapter.addItem(sellerToBuyer);
            buyerAdapter.subtractItem(buyerToSeller);
        } else {
            throw new CannotTradeException();
        }
    }

    /**
     * Attempt to trade with a Player.
     *
     * @throws InvalidChestException if the chest is invalid (duh)
     * @throws CannotTradeException if any party doesn't have enough to trade
     * @throws ChestProtectionException if the chest is protected
     * @throws PermissionsException if the player doesn't have "use" permissions
     */
    public void tradeWith(final Player buyer)
      throws InvalidChestException, CannotTradeException, ChestProtectionException, PermissionsException {
        plugin.permissions.getBest().throwIfCannot(buyer, "use." + getActionType(buyer));
        tradeWith(new PlayerInventoryAdapter(plugin, buyer.getName()));
    }

    /**
     * Create a human-readable representation of this TradingSign.
     * <p>
     * Do not use this for updating the actual sign; to do this, use
     * updateSign() and writeToStringArray().
     */
    public String toString() {
        Location chestLocation = getChestLocation();
        return "TradingSign: Global=" + global + "; " + sellerToBuyer + "; " + buyerToSeller + "; Chest=" + (chestLocation != null ? Locations.toString(chestLocation) : "<null>");
    }

    /**
     * Get the location of the chest for this sign.
     */
    public Location getChestLocation() {
        return SignManager.getInstance(plugin).getChestLocation(signLocation);
    }

    /**
     * Called before the player starts linking the sign.
     *
     * @throws UnsupportedOperationException if this is a global sign
     * @throws PermissionsException if the player doesn't have permission to
     *         link this sign.
     */
    public void preSetChestLocation(Player linkingPlayer)
      throws UnsupportedOperationException, PermissionsException {
        if(isGlobal()) {
            throw new UnsupportedOperationException("global signs cannot be connected to chests");
        }
        plugin.permissions.getBest().throwIfCannot(linkingPlayer, "create." + getActionType(linkingPlayer));
    }

    /**
     * Set the location of the chest for this sign.
     * <p>
     * If the chest location is set interactively, be sure to call
     * {@link #preSetChestLocation(Player)} to make sure the player has
     * permission first.
     */
    public void setChestLocation(Location chestLocation) {
        SignManager.getInstance(plugin).setChestLocation(signLocation, chestLocation);
    }

    /**
     * Called when the sign is destroyed.
     *
     * @throws PermissionsException if player is not allowed to break
     *         the sign.
     */
    public void destroy(Player breakingPlayer) throws PermissionsException {
        plugin.permissions.getBest().throwIfCannot(breakingPlayer, "break." + getActionType(breakingPlayer));
        SignManager.getInstance(plugin).removeChestLocation(signLocation);
    }
}
