package studio.pixeldream.bossesriserespawner;

import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BossSigils {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BossesRiseRespawner.MOD_ID);
    public record Definition(String id, String name, String boss, String structure, String piece,
                             Placement placement, String where, Rarity rarity) {}
    public enum Placement { CENTER, AHEAD, ABOVE, WATER }
    static {
        register(new Definition("sirok", "Sirok", "sandworm", "sandworm_nest", "sandworm_nest", Placement.CENTER, "a Sandworm Nest", Rarity.RARE));
        register(new Definition("skor", "Skor", "yeti", "yeti_hideout", "yeti_arena", Placement.CENTER, "Skor's hideout", Rarity.RARE));
        register(new Definition("helvar", "Helvar", "underworld_knight", "underworld_arena", "underworld_arena_1", Placement.AHEAD, "Helvar's arena, past the Underworld Arena door", Rarity.RARE));
        register(new Definition("ashlord", "Ashlord", "infernal_dragon", "dragon_tower", "", Placement.ABOVE, "a Dragon Tower", Rarity.EPIC));
        register(new Definition("nerakyss", "Nerakyss", "kraken", "kraken_ship", "", Placement.WATER, "the Kraken Ship, by open water", Rarity.EPIC));
    }
    private static void register(Definition definition) {
        ITEMS.register(definition.id() + "_sigil", () -> new BossSigilItem(definition));
    }
    private BossSigils() {}
}
