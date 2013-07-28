package tk.allele.protection.methods;

import com.bekvon.bukkit.residence.*;
import com.bekvon.bukkit.residence.protection.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import tk.allele.protection.ProtectionMethod;

public class ResidenceMethod implements ProtectionMethod {
    private Residence res;
    private Plugin plugin;

    public ResidenceMethod(Plugin plugin) 
    {
        Plugin resPlugin = null;
        try {
        	resPlugin = plugin.getServer().getPluginManager().getPlugin("Residence");
        } catch (Exception ex) {
        	resPlugin = null;
        }
        if (resPlugin != null) {
            res = (Residence) resPlugin;
        }
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return (res != null);
    }

    @Override
    public String toString() {
        return "Residence";
    }

    @Override
    public boolean canAccess(String playerName, Block block) {
    	return canAccess(plugin.getServer().getPlayer(playerName), block);
    }

    @Override
    public boolean canAccess(Player player, Block block) {
    	    FlagPermissions flags = null;
    		ClaimedResidence resAtLoc = null;
   		try {
    		resAtLoc = Residence.getResidenceManager().getByLoc(block.getLocation());
    		if (resAtLoc == null) return true;
    		flags = resAtLoc.getPermissions();
    		if (flags != null) return flags.playerHas(player.getName(), block.getWorld().getName(), "container", true);
    		return true;
    	} catch (Exception ex) {
    		return true;
    	}
    }
}

