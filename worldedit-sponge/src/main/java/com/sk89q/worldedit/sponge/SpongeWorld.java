/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.sponge;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.sponge.data.SpongeWorldData;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tile.TileEntity;
import org.spongepowered.api.block.tile.carrier.TileEntityCarrier;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpongeWorld extends AbstractWorld {

    private static final Logger logger = WorldEdit.logger;

    private final SpongeWorldData worldData;
    private final WeakReference<World> worldRef;

    /**
     * Construct the object.
     *
     * @param world the world
     */
    @SuppressWarnings("unchecked")
    SpongeWorld(SpongeWorldData worldData, World world) {
        this.worldData = worldData;
        this.worldRef = new WeakReference<World>(world);
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws WorldEditException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorldChecked() throws WorldEditException {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new WorldReferenceLostException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorld() {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        getWorld().getName();
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        World world = getWorldChecked();
        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        Chunk chunk = world.loadChunk(new Vector3i(x, y, z), true).get();
        BlockState blockState = chunk.getBlock(x & 15, y, z & 15);


        BlockType previousId = notifyAndLight ? blockState.getType() : BlockTypes.AIR;

        boolean successful = false;
        // TODO ID Issue chunk.setBlock(x & 15, y, z & 15, Block.getBlockById(block.getId()), block.getData());

        // TODO figure out how to do that Create the TileEntity
//        if (successful) {
//            CompoundTag tag = block.getNbtData();
//            if (tag != null) {
//                NBTTagCompound nativeTag = NBTConverter.toNative(tag);
//                nativeTag.setString("id", block.getNbtId());
//                TileEntityUtils.setTileEntity(getWorld(), position, nativeTag);
//            }
//        }
//
//        if (notifyAndLight) {
//            world.func_147451_t(x, y, z);
//            world.markBlockForUpdate(x, y, z);
//            world.notifyBlockChange(x, y, z, Block.getBlockById(previousId));
//
//            Block mcBlock = Block.getBlockById(previousId);
//            if (mcBlock != null && mcBlock.hasComparatorInputOverride()) {
//                world.func_147453_f(x, y, z, Block.getBlockById(block.getId()));
//            }
//        }

        return successful;
    }

    @Override
    public int getBlockLightLevel(Vector position) {
        checkNotNull(position);
        return getWorld().getLuminance(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        Optional<TileEntity> optTile = getWorld().getTileEntity(
                position.getBlockX(),
                position.getBlockY(),
                position.getBlockZ()
        );

        if (!optTile.isPresent()) {
            return false;
        }

        TileEntity tile = optTile.get();
        if (tile instanceof TileEntityCarrier) {
            TileEntityCarrier carrier = (TileEntityCarrier) tile;
            carrier.getInventory().clear();
            return true;
        }
        return false;
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == 0) {
            return;
        }

        Optional<Entity> optEntity = getWorld().createEntity(
                EntityTypes.DROPPED_ITEM,
                new Vector3d(
                        position.getX(),
                        position.getY(),
                        position.getZ()
                )
        );

        if (optEntity.isPresent()) {
            Item entity = (Item) optEntity.get();
            // TODO ID Issue entity.getItemData().setValue();
            getWorld().spawnEntity(entity);
        }
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        return false;
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public SpongeWorldData getWorldData() {
        return worldData;
    }

    @Override
    public List<? extends com.sk89q.worldedit.entity.Entity> getEntities(Region region) {
        List<com.sk89q.worldedit.entity.Entity> entities = new ArrayList<com.sk89q.worldedit.entity.Entity>();
        for (Entity entity : getWorld().getEntities()) {
            org.spongepowered.api.world.Location loc = entity.getLocation();
            if (region.contains(new Vector(loc.getX(), loc.getY(), loc.getZ()))) {
                entities.add(new SpongeEntity(entity));
            }
        }
        return entities;
    }

    @Override
    public List<? extends com.sk89q.worldedit.entity.Entity> getEntities() {
        List<com.sk89q.worldedit.entity.Entity> entities = new ArrayList<com.sk89q.worldedit.entity.Entity>();
        for (Entity entity : getWorld().getEntities()) {
            entities.add(new SpongeEntity(entity));
        }
        return entities;
    }

    @Nullable
    @Override
    public com.sk89q.worldedit.entity.Entity createEntity(Location location, BaseEntity entity) {
        return null;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        BlockState blockState = getWorld().getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        BaseBlock block = worldData.getBlockRegistry().createFromId(blockState.getType().getId());
        // TODO tile entity data
        return block;
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        BlockState blockState = getWorld().getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        return worldData.getBlockRegistry().createFromId(blockState.getType().getId());
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        BiomeType biome = getWorld().getBiome(position.getBlockX(), position.getBlockZ());
        BaseBiome baseBiome = null;
        // baseBiome = worldData.getBiomeRegistry().createFromId(biome.getId());
        return baseBiome;
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        String biomeID = worldData.getBiomeRegistry().getData(biome).getName();
        getWorld().setBiome(position.getBlockX(), position.getBlockZ(), biomeID);
        return true;
    }

    /**
     * Thrown when the reference to the world is lost.
     */
    private static class WorldReferenceLostException extends WorldEditException {
        private WorldReferenceLostException(String message) {
            super(message);
        }
    }

//    @Override
//    public boolean regenerate(Region region, EditSession editSession) {
//        BaseBlock[] history = new BaseBlock[16 * 16 * (getMaxY() + 1)];
//
//        for (Vector2D chunk : region.getChunks()) {
//            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
//
//            // First save all the blocks inside
//            for (int x = 0; x < 16; ++x) {
//                for (int y = 0; y < (getMaxY() + 1); ++y) {
//                    for (int z = 0; z < 16; ++z) {
//                        Vector pt = min.add(x, y, z);
//                        int index = y * 16 * 16 + z * 16 + x;
//                        history[index] = editSession.getBlock(pt);
//                    }
//                }
//            }
//
//            try {
//                // TODO
//                // getWorld().regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
//            } catch (Throwable t) {
//                logger.log(Level.WARNING, "Chunk generation via Bukkit raised an error", t);
//            }
//
//            // Then restore
//            for (int x = 0; x < 16; ++x) {
//                for (int y = 0; y < (getMaxY() + 1); ++y) {
//                    for (int z = 0; z < 16; ++z) {
//                        Vector pt = min.add(x, y, z);
//                        int index = y * 16 * 16 + z * 16 + x;
//
//                        // We have to restore the block if it was outside
//                        if (!region.contains(pt)) {
//                            editSession.smartSetBlock(pt, history[index]);
//                        } else { // Otherwise fool with history
//                            editSession.rememberChange(pt, history[index],
//                                    editSession.rawGetBlock(pt));
//                        }
//                    }
//                }
//            }
//        }
//
//        return true;
//    }
//
//    /**
//     * Gets the single block inventory for a potentially double chest.
//     * Handles people who have an old version of Bukkit.
//     * This should be replaced with {@link org.bukkit.block.Chest#getBlockInventory()}
//     * in a few months (now = March 2012) // note from future dev - lol
//     *
//     * @param chest The chest to get a single block inventory for
//     * @return The chest's inventory
//     */
//    private Inventory getBlockInventory(Chest chest) {
//        try {
//            chest
//            return chest.getBlockInventory();
//        } catch (Throwable t) {
//            if (chest.getInventory() instanceof DoubleChestInventory) {
//                DoubleChestInventory inven = (DoubleChestInventory) chest.getInventory();
//                if (inven.getLeftSide().getHolder().equals(chest)) {
//                    return inven.getLeftSide();
//                } else if (inven.getRightSide().getHolder().equals(chest)) {
//                    return inven.getRightSide();
//                } else {
//                    return inven;
//                }
//            } else {
//                return chest.getInventory();
//            }
//        }
//    }
//
//    @SuppressWarnings("deprecation")
//    @Override
//    public boolean isValidBlockType(int type) {
//        // TODO: Item type registry
//    }
//
//    @Override
//    public void checkLoadedChunk(Vector pt) {
//        getWorld().loadChunk(new Vector3i(pt.getBlockX() >> 4, 0, pt.getBlockZ() >> 4), true);
//    }
//
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if ((other instanceof SpongeWorld)) {
            return ((SpongeWorld) other).getWorld().equals(getWorld());
        } else if (other instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) other).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public int getMaxY() {
        return getWorld().getHeight() - 1;
    }

//    @Override
//    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
//        World world = getWorld();
//        for (BlockVector2D chunkPos : chunks) {
//            //world.getChunk(new Vector3i(chunkPos.getBlockX(), 0, chunkPos.getBlockZ())).get().reload()
//            //world.refreshChunk(chunkPos.getBlockX(), chunkPos.getBlockZ());
//        }
//    }
//
//    @Override
//    public boolean playEffect(Vector position, int type, int data) {
//        World world = getWorld();
//        world.playSound(SoundTypes.AMBIENCE_CAVE);
//
//        final Effect effect = effects.get(type);
//        if (effect == null) {
//            return false;
//        }
//
//        world.playEffect(SpongeUtil.toLocation(world, position), effect, data);
//
//        return true;
//    }
//
//    @Override
//    public void simulateBlockMine(Vector pt) {
//        getWorld().getBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).cycleProperty()
//    }
}
