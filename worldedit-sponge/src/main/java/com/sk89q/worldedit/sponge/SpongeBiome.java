package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.world.biome.BaseBiome;
import org.spongepowered.api.world.biome.BiomeType;

/**
 * Created by zml on 4/14/15.
 */
public class SpongeBiome extends BaseBiome {
    private final BiomeType type;

    public SpongeBiome(BiomeType biome) {
        super(biome.getId());
        this.type = biome;
    }

    public SpongeBiome(BaseBiome biome) {
        super(biome);
    }
}
