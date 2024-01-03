package fr.ludos.role;

import org.bukkit.entity.Player;

public class StalkerRole extends Role {
    RolesUtility utility = new RolesUtility();

    @Override
    public void processCrafting(Player player) {}

    @Override
    public void processAbilities(Player player) {
        // Passive Ability
        utility.removeNameTag(player.getName());
        // super.hidePlayerName();
    }


    public static class Builder extends Role.Builder {

        @Override
        public String getId() {
            return "stalker";
        }
    }
}