package fr.ludos.roles;

import org.bukkit.entity.Player;

import fr.ludos.Main;
import fr.ludos.skills.NecroticAuraSkill;
import fr.ludos.skills.VampiricLeechSkill;

public class NecromancerRole extends PlayerRole {

    private final Main plugin;

    public NecromancerRole(Main plugin) {
        this.plugin = plugin;
    }

    RolesUtility utility = new RolesUtility();

    @Override
    public void processCrafting(Player player) {}

    @Override
    public void processAbilities(Player player) {
        // change hunted param player to the true hunted player
        NecroticAuraSkill.activateNecroticAura(plugin, player, player);
        VampiricLeechSkill.activateVampiricLeech(plugin, player);
    }
}
