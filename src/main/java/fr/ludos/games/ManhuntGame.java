package fr.ludos.games;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.Listener;

import fr.ludos.controller.ManhuntTeamController;


public class ManhuntGame extends Game<ManhuntTeamController> implements Listener {

    public ManhuntGame(Scoreboard scoreboard) {
        super(scoreboard);
        super.teamController = new ManhuntTeamController(scoreboard);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if( 
            event.getDamager() instanceof Player damager && 
            event.getEntity() instanceof Player entity &&
            teamController.areAllies(damager, entity) 
        ) {
            event.setCancelled(false);
        }   
    }
    
}