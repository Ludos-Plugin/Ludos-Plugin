package fr.ludos.games.noname;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.PlayerList;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.group.Group;
import fr.ludos.core.lobby.Lobby;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.world.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

public class NoNameGame extends Game {
    public static final String ID = "noname";

    private final Builder builder;
	public final Builder builder() {
		return this.builder;
	}

	private final WorldManager worldManager;
	@Override
	public WorldManager worldManager() {
		return worldManager;
	}

	private final NoNameTeamController teamController;
	@Override
	public NoNameTeamController teamController() {
		return teamController;
	}

    protected NoNameGame(Builder builder, Group group) {
        super(builder, group, null);
        this.builder = builder;


        this.worldManager = WorldManager.within(this)
            .of(builder.createWorldCreator())
            .withLobby(
                Lobby.within(this)
                    .clear(ClearMode.ALL)
                    .waitFor(new PlayerList.GroupWaitPlayers(group))
                    .then(this::start)
            )
            .inArea(
                WorldBorderArea.within(WorldBorderArea.CONFIG.getGameConfig(group, builder), plugin())
            )
            .build();

        this.teamController = new NoNameTeamController(
            this,
            builder().getLudos().playersConfig.getGameConfig(group, builder)
        );
    }


    public static class Builder extends Game.Builder {

		public final ConfigNodeMap configMap = getConfigMap(List.of(
			getLudos().playersConfig,
			WorldBorderArea.CONFIG
		));


		public Builder(GameManager manager) {
			super(manager);
		}

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public AbstractItemBuilder<?> createItem(Player player) {
			return new ItemBuilder(Material.SPRUCE_DOOR);
		}

		@Override
		public TextComponent displayName() {
			return Component.text("noname")
				.color(NamedTextColor.GOLD);
		}
		@Override
		public TextComponent getDescription() {
			return Component.text("Fight against hordes of Enemies with your friends.\n\n" +
				"Every few waves, you will have to defeat an especially ferocious enemy..."
			);
		}

		public WorldCreator createWorldCreator() {
			String worldName = "noname_" + UUID.randomUUID();
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(Ludos.NAMESPACE, worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
			return wc;
		}

		@Override
		public ConfigNodeCollection getConfig() {
			return configMap;
		}

		@Override
		public NoNameGame build(Group group) {
			return new NoNameGame(this, group);
		}
	}
}
