package fr.ludos.roles;

import org.bukkit.entity.Player;

public class StalkerRole extends PlayerRole {
    RolesUtility utility = new RolesUtility();

    @Override
    public void processCrafting(Player player) {
        
    }

    @Override
    public void processAbilities(Player player) {
        // Passive Ability
        utility.removeNameTag(player.getName());
        
        // super.hidePlayerName();
    }
}