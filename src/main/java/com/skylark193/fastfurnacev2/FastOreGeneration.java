package com.skylark193.fastfurnacev2;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

final class FastOreGeneration {
    static final float REPLACEMENT_CHANCE = 0.05F;
    private static final List<OreReplacement> REPLACEMENTS = List.of(
            new OreReplacement(Blocks.COAL_ORE, FastFurnaceV2.EXTREME_COAL_ORE, 0x4D4F9B2D6C31A57EL),
            new OreReplacement(Blocks.IRON_ORE, FastFurnaceV2.FAST_IRON_ORE, 0x1931A77E29BC4501L),
            new OreReplacement(Blocks.GOLD_ORE, FastFurnaceV2.FAST_GOLD_ORE, 0x19360D1DE5A248B3L),
            new OreReplacement(Blocks.DIAMOND_ORE, FastFurnaceV2.FAST_DIAMOND_ORE, 0x193D1A0D4E2F65C7L));

    private FastOreGeneration() {
    }

    static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            // ChunkEvent.Load covers both newly generated chunks and chunks loaded from disk.
            processChunk(level.getSeed(), event.getChunk());
        }
    }

    static int processChunk(long worldSeed, ChunkAccess chunk) {
        if (chunk.getData(FastFurnaceV2.FAST_ORE_CHUNK_PROCESSED)) {
            return 0;
        }

        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int replacements = 0;
        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);
            if (!section.maybeHas(state -> replacementFor(state) != null)) {
                continue;
            }
            int sectionMinY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        OreReplacement replacement = replacementFor(state);
                        if (replacement == null) {
                            continue;
                        }
                        pos.set(chunkPos.getMinBlockX() + x, sectionMinY + y, chunkPos.getMinBlockZ() + z);
                        if (shouldReplace(worldSeed, pos, replacement.source())) {
                            // These are simple, light-equivalent blocks; no neighbor or block-entity updates are needed.
                            chunk.setBlockState(pos, replacement.result().get().defaultBlockState(), 0);
                            replacements++;
                        }
                    }
                }
            }
        }

        chunk.setData(FastFurnaceV2.FAST_ORE_CHUNK_PROCESSED, true);
        chunk.markUnsaved();
        return replacements;
    }

    static boolean shouldReplace(long worldSeed, BlockPos pos, Block source) {
        for (OreReplacement replacement : REPLACEMENTS) {
            if (replacement.source() == source) {
                long seed = mix64(worldSeed ^ pos.asLong() ^ replacement.salt());
                return RandomSource.create(seed).nextFloat() < REPLACEMENT_CHANCE;
            }
        }
        throw new IllegalArgumentException("Unsupported Fast ore source: " + source);
    }

    private static OreReplacement replacementFor(BlockState state) {
        for (OreReplacement replacement : REPLACEMENTS) {
            if (state.is(replacement.source())) {
                return replacement;
            }
        }
        return null;
    }

    private static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private record OreReplacement(Block source, DeferredBlock<Block> result, long salt) {
    }
}
