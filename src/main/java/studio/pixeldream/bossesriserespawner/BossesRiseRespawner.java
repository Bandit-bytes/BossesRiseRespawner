package studio.pixeldream.bossesriserespawner;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;

@Mod(BossesRiseRespawner.MOD_ID)
public final class BossesRiseRespawner {
    public static final String MOD_ID = "bossesrise_respawner";
    public BossesRiseRespawner(IEventBus bus) { BossSigils.ITEMS.register(bus); }
}
