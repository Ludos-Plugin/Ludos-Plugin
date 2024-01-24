package fr.ludos.role;


public class HuntsmanRole extends Role {

    // @EventHandler
    // public void onPlayerRespawn(PlayerRespawnEvent event) {
    //     ItemStack crossBowHunter = new ItemStack(Material.CROSSBOW, 1);
    //     ItemMeta crossBowMeta = crossBowHunter.getItemMeta();
    //     crossBowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
    //     ItemStack arrow = new ItemStack(Material.ARROW, 1);
    // }






    // @EventHandler
    // public void upgradeStuff(Player player) {        
    //     if (player.getTotalExperience() >= 100/*  && ! HuntsmanCrossbow.playerOwns(player) */) {
    //         // HuntsmanCrossbow.createNew(player);
    //     }
        
    //     if (player.getTotalExperience() >= 300/*  && ! HuntsmanSpear.playerOwns(player) */) {
    //         // HuntsmanSpear.createNew(player);
    //     }
    // }


    public static class Builder extends Role.Builder {

        @Override
        public String getId() {
            return "huntsman";
        }
    }
}