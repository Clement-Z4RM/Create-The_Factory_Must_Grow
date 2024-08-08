package com.drmangotea.createindustry.blocks.machines.oil_processing.distillation.controller;

import com.drmangotea.createindustry.blocks.machines.oil_processing.distillation.output.DistillationOutputBlockEntity;
import com.drmangotea.createindustry.blocks.tanks.SteelTankBlockEntity;
import com.drmangotea.createindustry.recipes.distillation.DistillationRecipe;
import com.drmangotea.createindustry.registry.TFMGBlocks;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.Lang;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;


import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.drmangotea.createindustry.base.util.TFMGUtils.fill;

public class DistillationControllerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final Object DistillationRecipesKey = new Object();

    public DistillationRecipe recipe;


    protected LazyOptional<FluidTank> fluidCapability;

    public final FluidTank tank= new SmartFluidTank(8000,this::onFluidStackChanged);






    public DistillationControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        fluidCapability = LazyOptional.of(()->tank);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}


    @Override
    public void tick() {
        super.tick();


        ArrayList<DistillationOutputBlockEntity> outputs = getOutputs();

        BlockEntity beBehind = level.getBlockEntity(getBlockPos().relative(DistillationControllerBlock.getFacing(getBlockState()).getOpposite()));
        if(!(beBehind instanceof SteelTankBlockEntity be))
            return;


        if(outputs.isEmpty()||be.activeHeat==0)
            return;
        ///



        if (recipe == null || !recipe.matches(tank,outputs.toArray().length)) {
            DistillationRecipe recipe = getMatchingRecipes();
            if (recipe!=null) {
                this.recipe = recipe;
                sendData();
            }
        }

        if(recipe==null)
            return;

        ///
        float speedModifier = (float) be.activeHeat /2;
        if(recipe.getInputFluid().getRequiredAmount()*speedModifier>tank.getFluidAmount())
            return;

        if(recipe.getFluidResults().toArray().length!=getOutputs().toArray().length)
            return;
    if(be.isController()) {
        if (be.getHeight() < outputs.toArray().length * 2||(be.width<2&&outputs.toArray().length>3))
            return;
    }else if (be.getControllerBE().getHeight() < outputs.toArray().length * 2||be.getControllerBE().width<2)
        return;



        for(DistillationOutputBlockEntity be1 : outputs){
            if(be1.tank.isEmpty())
                return;
        }



        int numero = 0;

        for(DistillationOutputBlockEntity output : outputs){

            FluidStack fluidStack = recipe.getFluidResults().get(numero);

            if(fluidStack.isEmpty())
                break;

            if(fill(output.tank, new FluidStack(fluidStack, (int) (fluidStack.getAmount()*speedModifier)))>output.tank.getCapacity())
                break;


            fill(output.tank, new FluidStack(fluidStack, (int) (fluidStack.getAmount()*speedModifier)));
            int consumption = (recipe.getInputFluid().getRequiredAmount()/6);

            try (Transaction t = TransferUtil.getTransaction()) {
                tank.extract(tank.variant, (long) (consumption*speedModifier), t);
                t.commit();
            }

            numero++;

        }








    }
    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel())
            return;



        if (!level.isClientSide) {
            setChanged();
            sendData();
        }


    }
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        BlockEntity beBehind = level.getBlockEntity(getBlockPos().relative(DistillationControllerBlock.getFacing(getBlockState()).getOpposite()));
        if(beBehind instanceof SteelTankBlockEntity be) {


                Lang.translate("goggles.distillation_tower.status")
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip, 1);

        if(be.activeHeat>0) {
            Lang.translate("goggles.distillation_tower.level", be.activeHeat)
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip, 1);
        }else
            Lang.translate("goggles.distillation_tower.level", be.activeHeat)
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
        if(getOutputs().toArray().length>0) {
            Lang.translate("goggles.distillation_tower.found_outputs", getOutputs().toArray().length)
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip, 1);
        }else
            Lang.translate("goggles.distillation_tower.found_outputs", getOutputs().toArray().length)
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);


        } else
            Lang.translate("goggles.distillation_tower.tank_not_found")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
        containedFluidTooltip(tooltip, isPlayerSneaking,
                getCapability(ForgeCapabilities.FLUID_HANDLER));

        return true;
    }

    protected DistillationRecipe getMatchingRecipes() {


        List<Recipe<?>> list = RecipeFinder.get(getRecipeCacheKey(), level, this::matchStaticFilters);


            for(int i = 0; i < list.toArray().length;i++){

                DistillationRecipe recipe = (DistillationRecipe) list.get(i);

              // Create.LOGGER.debug("AAAA:  "+recipe.getFluidResults().toArray().length);
              // Create.LOGGER.debug("BBBB:  "+getOutputs().toArray().length);
                if(recipe.getFluidResults().toArray().length==getOutputs().toArray().length)
                    for(int y = 0; y < recipe.getFluidIngredients().get(0).getMatchingFluidStacks().toArray().length;y++)
                        if(tank.getFluid().getFluid()==recipe.getFluidIngredients().get(0).getMatchingFluidStacks().get(y).getFluid())
                            if(tank.getFluidAmount()>=recipe.getFluidIngredients().get(0).getRequiredAmount())
                                return recipe;
        }

        return null;
    }
    protected <C extends Container> boolean matchStaticFilters(Recipe<C> r) {

        return r instanceof DistillationRecipe;



    }

    protected Object getRecipeCacheKey() {
        return DistillationRecipesKey;
    }

    public ArrayList<DistillationOutputBlockEntity> getOutputs(){
        ArrayList<DistillationOutputBlockEntity> outputs = new ArrayList<>();

        BlockPos checkedPos = this.getBlockPos().above();
        for(int i = 0;i<11;i++){

            if(i==0||i==2||i==4||i==6||i==8||i==10){
                if(level.getBlockEntity(checkedPos) instanceof DistillationOutputBlockEntity be){
                    outputs.add(be);
                } else break;

            }else {
                if(!(level.getBlockState(checkedPos).is(TFMGBlocks.INDUSTRIAL_PIPE.get())))
                    break;
            }


            checkedPos = checkedPos.above();
        }


        return outputs;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

            tank.readFromNBT(compound.getCompound("TankContent"));

    }


    @Override
    public void write(CompoundTag compound, boolean clientPacket) {


            compound.put("TankContent", tank.writeToNBT(new CompoundTag()));

    }
}
