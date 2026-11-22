package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.neoforge.event.level.ChunkEvent;

final class ExtremeCoalOreGeneration {
    static final float REPLACEMENT_CHANCE = 0.05F;
    private static final long REPLACEMENT_SALT = 0x4D4F9B2D6C31A57EL;

    private ExtremeCoalOreGeneration() {
    }

    static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            // ChunkEvent.Load covers both newly generated chunks and chunks loaded from disk.
            processChunk(level.getSeed(), event.getChunk());
        }
    }

    static int processChunk(long worldSeed, ChunkAccess chunk) {
        if (chunk.getData(FastFurnaceV2.EXTREME_COAL_CHUNK_PROCESSED)) {
            return 0;
        }

        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int replacements = 0;
        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);
            if (!section.maybeHas(state -> state.is(Blocks.COAL_ORE))) {
                continue;
            }
            int sectionMinY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (!state.is(Blocks.COAL_ORE)) {
                            continue;
                        }
                        pos.set(chunkPos.getMinBlockX() + x, sectionMinY + y, chunkPos.getMinBlockZ() + z);
                        if (shouldReplace(worldSeed, pos)) {
                            // These are simple, light-equivalent blocks; no neighbor or block-entity updates are needed.
                            chunk.setBlockState(pos, FastFurnaceV2.EXTREME_COAL_ORE.get().defaultBlockState(), 0);
                            replacements++;
                        }
                    }
                }
            }
        }

        chunk.setData(FastFurnaceV2.EXTREME_COAL_CHUNK_PROCESSED, true);
        chunk.markUnsaved();
        return replacements;
    }

    static boolean shouldReplace(long worldSeed, BlockPos pos) {
        long seed = mix64(worldSeed ^ pos.asLong() ^ REPLACEMENT_SALT);
        return RandomSource.create(seed).nextFloat() < REPLACEMENT_CHANCE;
    }

    private static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }
}
