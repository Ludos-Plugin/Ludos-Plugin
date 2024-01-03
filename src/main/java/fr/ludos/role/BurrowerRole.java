package fr.ludos.role;

import fr.ludos.item.burrower.BurrowerPick;
import fr.ludos.item.burrower.BurrowerPickLevels;

import java.util.Collections;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class BurrowerRole extends Role {
    public static final String id = "burrower";

    // @EventHandler
    // public void onPlayerRespawn(PlayerRespawnEvent event) {
    // }



    public static class Builder extends Role.Builder {

        

        @Override
        public String getId() {
            return id;
        }

    }
}


