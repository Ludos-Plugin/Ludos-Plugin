package fr.ludos.role;

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


