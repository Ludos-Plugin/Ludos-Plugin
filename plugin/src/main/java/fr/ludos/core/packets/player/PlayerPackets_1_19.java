package fr.ludos.core.packets.player;

import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.ludos.other.ExcludeFromJacocoGeneratedReport;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;


/**
 * Player packet implementation for Minecraft 1.19.
 */
@ExcludeFromJacocoGeneratedReport
public final class PlayerPackets_1_19 implements PlayerPackets {

	@Override
	public void setGlowForPlayer(Entity target, Player viewer, boolean value) {
		try {
			EntityDataAccessor<Byte> glowingAccessor = EntityDataSerializers.BYTE.createAccessor(0);
			if (glowingAccessor == null) throw new NullPointerException("Glowing Accessor");

			net.minecraft.world.entity.Entity targetEntity = ((CraftEntity) target).getHandle();
			SynchedEntityData targetData = targetEntity.getEntityData();

			byte newValue = targetData.get(glowingAccessor);
			if (value)
				newValue = (byte) (newValue | 0x40);
			else
				newValue = (byte) (newValue & ~0x40);

			targetData.set(glowingAccessor, newValue);

			ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(
				targetEntity.getId(),
				targetData,
				true
			);


			ServerPlayer viewerEntity = ((CraftPlayer) viewer).getHandle();
			viewerEntity.connection.send(packet);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}