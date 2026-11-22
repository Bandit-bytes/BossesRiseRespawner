package studio.pixeldream.bossesriserespawner;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class SigilCooldowns extends SavedData {
    public static final long DURATION = 24000L;
    private CompoundTag timestamps = new CompoundTag();
    public static SigilCooldowns get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(SigilCooldowns::new, (tag, registries) -> {
                SigilCooldowns data = new SigilCooldowns();
                data.timestamps = tag.getCompound("timestamps"); return data;
            }), "bossesrise_respawner_sigils");
    }
    public long remaining(String key, long now) {
        return timestamps.contains(key) ? Math.max(0, DURATION - (now - timestamps.getLong(key))) : 0;
    }
    public void record(String key, long now) { timestamps.putLong(key, now); setDirty(); }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("timestamps", timestamps); return tag;
    }
}
