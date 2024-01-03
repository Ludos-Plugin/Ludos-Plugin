package fr.ludos.role;

import org.bukkit.entity.Player;

import fr.ludos.Main;

public class NecromancerRole extends Role {

    private final Main plugin;

    public NecromancerRole(Main plugin) {
        this.plugin = plugin;
    }

    RolesUtility utility = new RolesUtility();

    // @Override
    // public void processCrafting(Player player) {}

    // @Override
    // public void processAbilities(Player player) {
    //     // change hunted param player to the true hunted player
    //     // NecroticAuraSkill.activateNecroticAura(plugin, player, player);
    //     // VampiricLeechSkill.activateVampiricLeech(plugin, player);
    // }


    public static class Builder extends Role.Builder {

        @Override
        public String getId() {
            return "necromancer";
        }
    }
}
