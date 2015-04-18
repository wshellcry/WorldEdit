package com.sk89q.worldedit.sponge.data;

import com.sk89q.worldedit.blocks.BlockMaterial;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;

/**
 * Created by zml on 4/17/15.
 */
public class SpongeBlockMaterial implements BlockMaterial {
    private final ItemType block;

    @Override
    public boolean isRenderedAsNormalBlock() {
        return block.getDefaultProperty()
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isPowerSource() {
        return false;
    }

    @Override
    public boolean isLiquid() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public float getHardness() {
        return 0;
    }

    @Override
    public float getResistance() {
        return 0;
    }

    @Override
    public float getSlipperiness() {
        return 0;
    }

    @Override
    public boolean isGrassBlocking() {
        return false;
    }

    @Override
    public float getAmbientOcclusionLightValue() {
        return 0;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public int getLightValue() {
        return 0;
    }

    @Override
    public boolean isFragileWhenPushed() {
        return false;
    }

    @Override
    public boolean isUnpushable() {
        return false;
    }

    @Override
    public boolean isAdventureModeExempt() {
        return false;
    }

    @Override
    public boolean isTicksRandomly() {
        return false;
    }

    @Override
    public boolean isUsingNeighborLight() {
        return false;
    }

    @Override
    public boolean isMovementBlocker() {
        return false;
    }

    @Override
    public boolean isBurnable() {
        return false;
    }

    @Override
    public boolean isToolRequired() {
        return false;
    }

    @Override
    public boolean isReplacedDuringPlacement() {
        return false;
    }
}
