package tk.allele.duckshop.errors;

import org.bukkit.entity.Player;
import tk.allele.duckshop.items.Item;

/**
 * Thrown to indicate a player does not have enough of something, usually money or items.
 */
public class TooLittleException extends TradingException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7592848186550345739L;

	public TooLittleException(Player player, Item item) {
        super(player, item);
    }
}
