package fr.ludos.role;

public class StalkerRole extends Role {

    public StalkerRole(Builder builder) {
        super(builder);
    }

    RolesUtility utility = new RolesUtility();

    // @Override
    // public void processCrafting(Player player) {}

    // @Override
    // public void processAbilities(Player player) {
    //     // Passive Ability
    //     utility.removeNameTag(player.getName());
    //     // super.hidePlayerName();
    // }


    public static class Builder extends Role.Builder {

        @Override
        public String getId() {
            return "stalker";
        }

        @Override
        public StalkerRole build(String gameId) {
            return new StalkerRole(this);
        }
    }
}