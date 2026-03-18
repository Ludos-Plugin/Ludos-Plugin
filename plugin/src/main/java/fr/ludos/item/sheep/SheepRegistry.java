package fr.ludos.item.sheep;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Registry that manages all sheep types and dispatches player interactions
 * to the appropriate sheep instance.
 */
public class SheepRegistry implements Listener {

    private final Map<Material, AbstractSheep> sheepByMaterial = new HashMap<>();
    private final HashSet<Action> validActions = new HashSet<>(
        Arrays.asList(Action.RIGHT_CLICK_AIR, Action.LEFT_CLICK_AIR, Action.PHYSICAL)
    );

    /**
     * Registers a sheep type in the registry.
     * @param sheep The sheep to register
     */
    public void register(AbstractSheep sheep) {
        sheepByMaterial.put(sheep.getMaterial(), sheep);
    }

    /**
     * Gets a sheep by its material type.
     * @param material The wool material
     * @return The AbstractSheep instance, or null if not found
     */
    @Nullable
    public AbstractSheep getSheep(Material material) {
        return sheepByMaterial.get(material);
    }

    /**
     * Handles player interaction events and dispatches wool clicks to the appropriate sheep.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        // Check if it's a registered wool item
        AbstractSheep sheep = sheepByMaterial.get(item.getType());
        if (sheep == null) return;

        // Cancel event if not a valid action
        if (!validActions.contains(event.getAction())) {
            event.setCancelled(true);
        }

        // Launch the sheep
        sheep.launch(player, event, item);
    }

    /**
     * Loads all sheep from a JSON array and returns a list of AbstractSheep instances.
     *
     * @param jsonArray The JSON array containing sheep definitions
     * @return List of AbstractSheep instances
     */
    public static List<AbstractSheep> loadFromJson(JSONArray jsonArray) {
        return (List<AbstractSheep>) (List<?>) jsonArray.stream()
            .map(obj -> createSheepFromJson((JSONObject) obj))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Factory method that creates the appropriate sheep subclass from JSON.
     *
     * @param json The JSON object containing sheep data (name, description, material)
     * @return An AbstractSheep instance of the correct subclass, or null if material is unknown
     */
    private static AbstractSheep createSheepFromJson(JSONObject json) {
        String name = json.get("name").toString();
        String description = json.get("description").toString();
        Material material = Material.valueOf(json.get("material").toString());

        // Map material to the correct subclass
        return switch (material) {
            case BROWN_WOOL -> new NuclearSheep(name, description, material);
            case LIGHT_BLUE_WOOL -> new FreezeSheep(name, description, material);
            case ORANGE_WOOL -> new MediumExplosionSheep(name, description, material);
            case YELLOW_WOOL -> new LavaSheep(name, description, material);
            case RED_WOOL -> new BigExplosionSheep(name, description, material);
            case GRAY_WOOL -> new AnvilsSheep(name, description, material);
            case BLUE_WOOL -> new WaterSheep(name, description, material);
            case GREEN_WOOL -> new GrassSheep(name, description, material);
            case BLACK_WOOL -> new BlindSheep(name, description, material);
            case MAGENTA_WOOL -> new PoisonSheep(name, description, material);
            case PURPLE_WOOL -> new TeleportSheep(name, description, material);
            case PINK_WOOL -> new HealSheep(name, description, material);
            case WHITE_WOOL -> new AllAboardSheep(name, description, material);
            case CYAN_WOOL -> new FireballSheep(name, description, material);
            case LIME_WOOL -> new NauseaSheep(name, description, material);
            case LIGHT_GRAY_WOOL -> new MonstersSheep(name, description, material);
            default -> null;
        };
    }
}
