package fr.ludos.role;

public class NecromancerRole extends Role {

    public NecromancerRole(Builder builder) {
        super(builder);
    }

    // @Override
    // public void processCrafting(Player player) {}

    // @Override
    // public void processAbilities(Player player) {
    //     // change prey param player to the true prey player
    //     // NecroticAuraSkill.activateNecroticAura(plugin, player, player);
    //     // VampiricLeechSkill.activateVampiricLeech(plugin, player);
    // }>>>>>>> c12ccea39c04c170c97f852412f67f3dcdd6ce20
    // public void processAbilities(Player player) {
    //     // change prey param player to the true prey player
    //     // NecroticAuraSkill.activateNecroticAura(plugin, player, player);
    //     // VampiricLeechSkill.activateVampiricLeech(plugin, player);
    // }


    public static class Builder extends Role.Builder {

        @Override
        public String getId() {
            return "necromancer";
        }

        @Override
        public NecromancerRole build(String gameId) {
            return new NecromancerRole(this);
        }
    }
}
