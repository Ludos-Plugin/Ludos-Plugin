package fr.ludos.core.item;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemUtilitiesTest {

	@Test
	@DisplayName("Should identify air as not breakable")
	void testIsBreakableAir() {
		Block mockBlock = mock(Block.class);
		when(mockBlock.getType()).thenReturn(Material.AIR);


		assertFalse(ItemUtilities.isBreakable(mockBlock));
	}


	@ParameterizedTest
	@DisplayName("Should identify blocks with negative hardness as not breakable")
	@ValueSource(booleans = {true, false})
	void testIsBreakableNegativeHardness(boolean isSolid) {
		Block mockBlock = mock(Block.class);
		Material mockMaterial = mock(Material.class);
		when(mockBlock.getType()).thenReturn(mockMaterial);
		when(mockMaterial.isSolid()).thenReturn(isSolid);
		when(mockMaterial.getHardness()).thenReturn(-12.0f);


		assertFalse(ItemUtilities.isBreakable(mockBlock));
	}
	@ParameterizedTest
	@DisplayName("Should identify blocks with positive hardness as breakable when solid")
	@ValueSource(booleans = {true, false})
	void testIsBreakablePositiveHardness(boolean isSolid) {
		Block mockBlock = mock(Block.class);
		Material mockMaterial = mock(Material.class);
		when(mockBlock.getType()).thenReturn(mockMaterial);
		when(mockMaterial.isSolid()).thenReturn(isSolid);
		when(mockMaterial.getHardness()).thenReturn(12.0f);


		assertEquals(isSolid, ItemUtilities.isBreakable(mockBlock));
	}


	@Test
	@DisplayName("Should spawn particle and damage entities on sweep attack")
	void testDoSweepAttack() {
		HumanEntity attacker = mock(HumanEntity.class);
		LivingEntity target = mock(LivingEntity.class);
		World world = mock(World.class);
		Location location = mock(Location.class);


		when(attacker.getAttackCooldown()).thenReturn(1.0f); // > 0.848
		when(attacker.getWorld()).thenReturn(world);
		when(attacker.getLocation()).thenReturn(location);
		when(location.add(any(Double.class), any(Double.class), any(Double.class))).thenReturn(location);
		when(attacker.getEyeLocation()).thenReturn(mock(Location.class));
		when(attacker.getEyeLocation().getDirection()).thenReturn(new Vector(0, 0, 1));


		// Mock nearby entities
		LivingEntity nearby = mock(LivingEntity.class);
		Entity nonLivingNearby = mock(Entity.class);
		when(attacker.getNearbyEntities(any(Double.class), any(Double.class), any(Double.class)))
			.thenReturn(List.of(nearby, nonLivingNearby));


		ItemUtilities.doSweepAttack(attacker, target, 5.0, 1, 1.5);


		verify(world).spawnParticle(eq(Particle.SWEEP_ATTACK), any(), eq(1));
		verify(nearby).damage(any(Double.class));
	}

	@Test
	@DisplayName("Should do nothing if attack cooldown is too low")
	void testDoSweepAttackNoCooldown() {
		HumanEntity attacker = mock(HumanEntity.class);
		LivingEntity target = mock(LivingEntity.class);
		World world = mock(World.class);


		when(attacker.getAttackCooldown()).thenReturn(0.5f); // < 0.848
		when(attacker.getWorld()).thenReturn(world);


		ItemUtilities.doSweepAttack(attacker, target, 5.0, 1, 1.5);


		verify(world, never()).spawnParticle(any(), any(), anyInt());
	}

	@Test
	@DisplayName("Sweep should do 1 damage with enchantment level O")
	void testDoSweepAttackEnchantLevel0() {
		HumanEntity attacker = mock(HumanEntity.class);
		LivingEntity target = mock(LivingEntity.class);
		World world = mock(World.class);
		Location location = mock(Location.class);


		when(attacker.getAttackCooldown()).thenReturn(1.0f); // > 0.848
		when(attacker.getWorld()).thenReturn(world);
		when(attacker.getLocation()).thenReturn(location);
		when(location.add(any(Double.class), any(Double.class), any(Double.class))).thenReturn(location);
		when(attacker.getEyeLocation()).thenReturn(mock(Location.class));
		when(attacker.getEyeLocation().getDirection()).thenReturn(new Vector(0, 0, 1));


		// Mock nearby entities
		LivingEntity nearby = mock(LivingEntity.class);
		when(attacker.getNearbyEntities(any(Double.class), any(Double.class), any(Double.class)))
			.thenReturn(Collections.singletonList(nearby));


		ItemUtilities.doSweepAttack(attacker, target, 5.0);

		verify(nearby).damage(1.0);
	}
}