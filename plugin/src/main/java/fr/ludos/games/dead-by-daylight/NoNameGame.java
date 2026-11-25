package fr.ludos.games.dead-by-daylight;

public class NoNameGame {
    public static final String ID = "noname";

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

		this.teamController = new RaidTeamController(
			this,
			builder().getLudos().playersConfig.getGameConfig(group, builder)
		);
	}
}
