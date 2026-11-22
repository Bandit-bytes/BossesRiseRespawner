package studio.pixeldream.bossesriserespawner;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.*;

public final class BossSigilItem extends Item {
    private final BossSigils.Definition definition;
    public BossSigilItem(BossSigils.Definition definition) {
        super(new Properties().stacksTo(16).rarity(definition.rarity()));
        this.definition = definition;
    }
    private static ResourceLocation bossId(String path) {
        return ResourceLocation.fromNamespaceAndPath("block_factorys_bosses", path);
    }
    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Use in " + definition.where() + " to summon " + definition.name() + ".").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.bossesrise_respawner.sigil_cooldown").withStyle(ChatFormatting.DARK_GRAY));
    }
    private InteractionResultHolder<ItemStack> refuse(Player player, ItemStack stack, String message) {
        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.RED), true);
        ((ServerLevel)player.level()).playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1, .6f);
        return InteractionResultHolder.fail(stack);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(world instanceof ServerLevel level)) return InteractionResultHolder.success(stack);
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(bossId(definition.structure()));
        StructureStart start = structure == null ? null : level.structureManager().getStructureAt(player.blockPosition(), structure);
        StructurePiece piece = null;
        if (start != null && start.isValid()) {
            for (var candidate : start.getPieces()) {
                if (!candidate.getBoundingBox().isInside(player.blockPosition())) continue;
                if (definition.piece().isEmpty() || candidate instanceof PoolElementStructurePiece pool && pool.getElement().toString().contains(definition.piece())) {
                    piece = candidate; break;
                }
            }
        }
        if (piece == null) return refuse(player, stack, "Use it in " + definition.where() + ".");
        var box = start.getBoundingBox();
        var area = new AABB(box.minX()-32, box.minY()-32, box.minZ()-32, box.maxX()+33, box.maxY()+33, box.maxZ()+33);
        if (!level.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).equals(bossId(definition.boss()))).isEmpty())
            return refuse(player, stack, definition.name() + " is already here.");
        SigilCooldowns data = SigilCooldowns.get(level);
        String key = definition.boss() + "/" + level.dimension().location() + "/" + start.getChunkPos().toLong();
        long now = level.getServer().overworld().getGameTime();
        long remaining = data.remaining(key, now);
        if (remaining > 0) return refuse(player, stack, definition.name() + " stirs again in " + ((remaining+1199)/1200) + " minutes.");
        Vec3 point = spawnPoint(level, player, piece);
        if (point == null) return refuse(player, stack, "Stand on the ship by open water.");
        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(bossId(definition.boss())).orElse(null);
        var boss = type == null ? null : type.create(level);
        if (boss == null) return refuse(player, stack, "The boss is unavailable.");
        boss.moveTo(point.x, point.y, point.z, player.getYRot()+180, 0);
        if (definition.boss().equals("sandworm")) boss.setNoGravity(true);
        if (!level.addFreshEntity(boss)) return refuse(player, stack, "The boss could not be summoned.");
        data.record(key, now);
        if (!player.isCreative()) stack.shrink(1);
        player.displayClientMessage(Component.literal(definition.name()+" answers the sigil.").withStyle(ChatFormatting.GOLD), false);
        player.displayClientMessage(Component.literal(definition.name()+" answers the sigil.").withStyle(ChatFormatting.GOLD), true);
        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1, .7f);
        return InteractionResultHolder.success(stack);
    }
    private Vec3 spawnPoint(ServerLevel level, Player player, StructurePiece piece) {
        return switch (definition.placement()) {
            case CENTER -> {
                var b = piece.getBoundingBox();
                yield new Vec3((b.minX()+b.maxX()+1)/2.0, b.minY()+1, (b.minZ()+b.maxZ()+1)/2.0);
            }
            case ABOVE -> player.position().add(0,6,0);
            case WATER -> waterSpot(level, player);
            case AHEAD -> {
                Vec3 look = player.getLookAngle(); double length = Math.sqrt(look.x*look.x+look.z*look.z);
                if (length < .01) yield player.position();
                Vec3 ahead = player.position().add(look.x/length*5,0,look.z/length*5);
                BlockPos feet = BlockPos.containing(ahead);
                yield level.isEmptyBlock(feet) && level.isEmptyBlock(feet.above()) ? ahead : player.position();
            }
        };
    }
    private static Vec3 waterSpot(ServerLevel level, Player player) {
        for (int radius=20; radius<=28; radius+=4) for (int step=0; step<16; step++) {
            int x=player.getBlockX()+(int)Math.round(Math.cos(step*Math.PI/8)*radius);
            int z=player.getBlockZ()+(int)Math.round(Math.sin(step*Math.PI/8)*radius);
            if (!level.hasChunkAt(new BlockPos(x,player.getBlockY(),z))) continue;
            for (int y=player.getBlockY(); y>=player.getBlockY()-14; y--) {
                var pos = new BlockPos(x,y,z);
                var state = level.getBlockState(pos);
                if (state.isAir()) continue;
                if (state.getFluidState().getType().isSame(Fluids.WATER) && level.getFluidState(pos.below(4)).getType().isSame(Fluids.WATER))
                    return new Vec3(x+.5,y-3.5,z+.5);
                break;
            }
        }
        return null;
    }
}
