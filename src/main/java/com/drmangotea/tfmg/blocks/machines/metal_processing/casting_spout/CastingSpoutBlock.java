package com.drmangotea.tfmg.blocks.machines.metal_processing.casting_spout;


import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CastingSpoutBlock extends Block implements IBE<CastingSpoutBlockEntity> {
    public CastingSpoutBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public Class<CastingSpoutBlockEntity> getBlockEntityClass() {
        return CastingSpoutBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CastingSpoutBlockEntity> getBlockEntityType() {
        return TFMGBlockEntities.CASTING_SPOUT.get();
    }
}
