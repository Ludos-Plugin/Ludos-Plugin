package fr.ludos.item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import fr.ludos.role.Role;

public abstract class LevelItemEvents<T extends LevelItem<TLevels>, TLevels extends SpecialItemLevels> extends SpecialItemEvents<T> {

	private Map<String, TLevels> deadPlayerLevels = new HashMap<>();

	protected abstract T createItem(Player owner, TLevels level);
	protected abstract TLevels getDefaultLevel();

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
        String roleId = getRoleId();
		if ( roleId != null && ! Role.isPlayerRole(player, roleId) ) {
			return;
		}

		T specialItem = SpecialItem.findIn(player.getInventory(), (item) -> getItem(item));
		if ( specialItem == null ) {
			return;
		}

		deadPlayerLevels.put( player.getName(), specialItem.getLevel().getPrevious() );
	}

    @Override
    protected final T createItem(Player owner) {
		TLevels level = getDefaultLevel();
		if (owner != null && deadPlayerLevels.containsKey(owner.getName())) {
			level = deadPlayerLevels.get(owner.getName());
		}

        return createItem(owner, level);
    }
}