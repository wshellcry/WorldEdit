package com.sk89q.worldedit.sponge.data;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockMaterial;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import com.sk89q.worldedit.world.registry.State;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by zml on 4/14/15.
 */
public class SpongeBlockRegistry implements BlockRegistry {

    @Nullable
    @Override
    public BaseBlock createFromId(String id) {
        return null;
    }

    @Nullable
    @Override
    public BaseBlock createFromId(int id) {
        return null;
    }

    @Nullable
    @Override
    public BlockMaterial getMaterial(BaseBlock block) {
        return null;
    }

    @Nullable
    @Override
    public Map<String, ? extends State> getStates(BaseBlock block) {
        return null;
    }
}
