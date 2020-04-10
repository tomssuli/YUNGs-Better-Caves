package com.yungnickyoung.minecraft.bettercaves.world.carver.surface;

import net.minecraft.block.state.IBlockState;

public class VanillaCaveCarverBuilder {
    private int
        bottomY  = 1,
        topY     = 1,
        density  = 0,
        priority = 0,
        liquidAltitude = 10;
    private IBlockState debugBlock;
    private boolean
        isDebugVisualizerEnabled,
        isReplaceGravel;

    public VanillaCaveCarver build() {
        return new VanillaCaveCarver(this);
    }

    /* ================================== Builder Setters ================================== */
    /**
     * @param bottomY minimum cave y-coordinate
     */
    public VanillaCaveCarverBuilder bottomY(int bottomY) {
        this.bottomY = bottomY;
        return this;
    }

    /**
     * @param topY maximum cave y-coordinate
     */
    public VanillaCaveCarverBuilder topY(int topY) {
        this.topY = topY;
        return this;
    }

    /**
     * @param density density of vanilla caves
     */
    public VanillaCaveCarverBuilder density(int density) {
        this.density = density;
        return this;
    }

    /**
     * @param priority priority of vanilla caves
     */
    public VanillaCaveCarverBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * @param debugBlock Block used for this cave type in the debug visualizer
     */
    public VanillaCaveCarverBuilder debugVisualizerBlock(IBlockState debugBlock) {
        this.debugBlock = debugBlock;
        return this;
    }

    /**
     * @param isDebugVisualizerEnabled set true to enable debug visualizer
     */
    public VanillaCaveCarverBuilder debugVisualizerEnabled(boolean isDebugVisualizerEnabled) {
        this.isDebugVisualizerEnabled = isDebugVisualizerEnabled;
        return this;
    }

    public VanillaCaveCarverBuilder liquidAltitude(int liquidAltitude) {
        this.liquidAltitude = liquidAltitude;
        return this;
    }

    public VanillaCaveCarverBuilder replaceGravel(boolean replaceGravel) {
        isReplaceGravel = replaceGravel;
        return this;
    }


    /* ================================== Builder Getters ================================== */
    public int getBottomY() {
        return bottomY;
    }

    public int getTopY() {
        return topY;
    }

    public int getDensity() {
        return density;
    }

    public int getPriority() {
        return priority;
    }

    public IBlockState getDebugBlock() {
        return debugBlock;
    }

    public boolean isDebugVisualizerEnabled() {
        return isDebugVisualizerEnabled;
    }

    public int getLiquidAltitude() {
        return liquidAltitude;
    }

    public boolean isReplaceGravel() {
        return isReplaceGravel;
    }
}
