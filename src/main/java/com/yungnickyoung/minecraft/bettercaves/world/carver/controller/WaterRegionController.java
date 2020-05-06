package com.yungnickyoung.minecraft.bettercaves.world.carver.controller;

import com.yungnickyoung.minecraft.bettercaves.BetterCaves;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseUtils;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;
import java.util.function.Function;

import static com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils.isPosInWorld;

public class WaterRegionController {
    private FastNoise waterRegionController;
    private IWorld world;
    private int dimensionId;
    private String dimensionName;
    private Random rand;

    // Vars determined from config
    private BlockState lavaBlock;
    private BlockState waterBlock;
    private float waterRegionThreshold;

    // Constants
    private static final float SMOOTH_RANGE = .04f;
    private static final float SMOOTH_DELTA = .01f;

    public WaterRegionController(IWorld worldIn, ConfigHolder config) {
        this.world = worldIn;
        this.dimensionId = worldIn.getDimension().getType().getId();
        this.dimensionName = DimensionType.getKey(worldIn.getDimension().getType()).toString();
        this.rand = new Random();

        // Vars from config
        lavaBlock = getLavaBlockFromString(config.lavaBlock.get());
        waterBlock = getWaterBlockFromString(config.waterBlock.get());
        waterRegionThreshold = NoiseUtils.simplexNoiseOffsetByPercent(-1f, config.waterRegionSpawnChance.get().floatValue() / 100f);

        // Water region controller
        float waterRegionSize = config.cavernRegionSize.get().equals("ExtraLarge") ? .001f : .004f;
        waterRegionController = new FastNoise();
        waterRegionController.SetSeed((int)world.getSeed() + 444);
        waterRegionController.SetFrequency(waterRegionSize);
    }

    public BlockState[][] getLiquidBlocksForChunk(int chunkX, int chunkZ) {
        rand.setSeed(world.getSeed() ^ chunkX ^ chunkZ);
        BlockState[][] blocks = new BlockState[16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;
                BlockPos pos = new BlockPos(realX, 1, realZ);
                blocks[x][z] = getLiquidBlockAtPos(rand, pos);
            }
        }
        return blocks;
    }

    private BlockState getLiquidBlockAtPos(Random rand, BlockPos blockPos) {
        BlockState liquidBlock = lavaBlock;
        if (waterRegionThreshold > -1f) { // Don't bother calculating noise if water regions are disabled
            float waterRegionNoise = waterRegionController.GetNoise(blockPos.getX(), blockPos.getZ());

            // If water region threshold check is passed, change liquid block to water
            float randOffset = rand.nextFloat() * SMOOTH_DELTA + SMOOTH_RANGE;
            if (waterRegionNoise < waterRegionThreshold - randOffset)
                liquidBlock = waterBlock;
            else if (waterRegionNoise < waterRegionThreshold + randOffset)
                liquidBlock = null;
        }
        return liquidBlock;
    }

    private BlockState getLavaBlockFromString(String lavaString) {
        BlockState lavaBlock;
        try {
            lavaBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(lavaString)).getDefaultState();
            BetterCaves.LOGGER.info("Using block '" + lavaString + "' as lava in cave generation for dimension " +
                BetterCavesUtils.dimensionAsString(dimensionId, dimensionName) + " ...");
        } catch (Exception e) {
            BetterCaves.LOGGER.warn("Unable to use block '" + lavaString + "': " + e);
            BetterCaves.LOGGER.warn("Using vanilla lava instead...");
            lavaBlock = Blocks.LAVA.getDefaultState();
        }

        // Default to vanilla lava if lavaBlock is null or contains air (the default registry block) when air was not specified
        if (lavaBlock == null || (lavaBlock == Blocks.AIR.getDefaultState() && !lavaString.equals("minecraft:air"))) {
            BetterCaves.LOGGER.warn("Unable to use block '" + lavaString + "': null block returned.\n Using vanilla lava instead...");
            lavaBlock = Blocks.LAVA.getDefaultState();
        }
        return lavaBlock;
    }

    private BlockState getWaterBlockFromString(String waterString) {
        BlockState waterBlock;
        try {
            waterBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(waterString)).getDefaultState();
            BetterCaves.LOGGER.info("Using block '" + waterString + "' as water in cave generation for dimension " +
                BetterCavesUtils.dimensionAsString(dimensionId, dimensionName) + " ...");
        } catch (Exception e) {
            BetterCaves.LOGGER.warn("Unable to use block '" + waterString + "': " + e);
            BetterCaves.LOGGER.warn("Using vanilla water instead...");
            waterBlock = Blocks.WATER.getDefaultState();
        }

        // Default to vanilla water if waterBlock is null or contains air (the default registry block) when air was not specified
        if (waterBlock == null || (waterBlock == Blocks.AIR.getDefaultState() && !waterString.equals("minecraft:air"))) {
            BetterCaves.LOGGER.warn("Unable to use block '" + waterString + "': null block returned.\n Using vanilla water instead...");
            waterBlock = Blocks.WATER.getDefaultState();
        }

        return waterBlock;
    }

    public void setWorld(IWorld worldIn) {
        this.world = worldIn;
    }

    public static float getDistFactor(IWorld worldIn, BlockPos pos, int distance, Function<Biome.Category, Boolean> isTargetBiome) {
        WorldGenRegion worldGenRegion = (WorldGenRegion)worldIn;
        // First check unit circle
        // Cardinal directions
        if (
            (isPosInWorld(pos.north(), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.north()).getCategory())) ||
                (isPosInWorld(pos.east(), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.east()).getCategory())) ||
                (isPosInWorld(pos.south(), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.south()).getCategory())) ||
                (isPosInWorld(pos.west(), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.west()).getCategory()))
        ) {
            return 0;
        }
        // Corners
        if (
            (isPosInWorld(pos.north().east(), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.north().east()).getCategory())) ||
            (isPosInWorld(pos.east().south(), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.east().south()).getCategory())) ||
            (isPosInWorld(pos.south().west(), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.south().west()).getCategory())) ||
            (isPosInWorld(pos.west().north(), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.west().north()).getCategory()))
        ) {
            return 2f / (distance * 2);
        }

        // 2-circle
        // Cardinal directions
        if (
            (isPosInWorld(pos.north(2), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.north(2)).getCategory())) ||
            (isPosInWorld(pos.east(2), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.east(2)).getCategory())) ||
            (isPosInWorld(pos.south(2), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.south(2)).getCategory())) ||
            (isPosInWorld(pos.west(2), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.west(2)).getCategory()))
        ) {
            return 2f / (distance * 2);
        }
        // 3 away
        if (
            (isPosInWorld(pos.north(2).east(1), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.north(2).east(1)).getCategory())) ||
            (isPosInWorld(pos.north(2).west(1), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.north(2).west(1)).getCategory())) ||
            (isPosInWorld(pos.east(2).north(1), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.east(2).north(1)).getCategory())) ||
            (isPosInWorld(pos.east(2).south(1), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.east(2).south(1)).getCategory())) ||
            (isPosInWorld(pos.south(2).east(1), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.south(2).east(1)).getCategory())) ||
            (isPosInWorld(pos.south(2).west(1), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.south(2).west(1)).getCategory())) ||
            (isPosInWorld(pos.west(2).south(1), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.west(2).south(1)).getCategory())) ||
            (isPosInWorld(pos.west(2).north(1), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.west(2).north(1)).getCategory()))
        ) {
            return 3f / (distance * 2);
        }
        // Corners
        if (
            (isPosInWorld(pos.north(2).east(2), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.north(2).east(2)).getCategory())) ||
            (isPosInWorld(pos.east(2).south(2), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.east(2).south(2)).getCategory())) ||
            (isPosInWorld(pos.south(2).west(2), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.south(2).west(2)).getCategory())) ||
            (isPosInWorld(pos.west(2).north(2), worldGenRegion) && isTargetBiome.apply(worldIn.getBiome(pos.west(2).north(2)).getCategory()))
        ) {
            return 1;
        }
        return 1;
    }
}