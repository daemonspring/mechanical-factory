package github.elmartino4.mechanicalFactory.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import github.elmartino4.mechanicalFactory.util.GeneratorIdentifier;
import github.elmartino4.mechanicalFactory.MechanicalFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public class FluidBlockMixin {
    @Final @Shadow static ImmutableList<Direction> field_34006;

    @Inject(method = "receiveNeighborFluids", at = @At("HEAD"), cancellable = true)
    private void receiveNeighbours(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir){
        //GeneratorIdentifier temp = new GeneratorIdentifier(Fluids.FLOWING_WATER.getDefaultState(), null, Blocks.BLUE_ICE.getDefaultState(), null);
        //System.out.println("0; " + temp.toString() + temp.hashCode(true, false));
        //System.out.println("1; " + temp.toString() + temp.hashCode(true, false));

        Block down = world.getBlockState(pos.down()).getBlock();

        int matchVal = 1;
        int matchIndex = -1;

        for (UnmodifiableIterator<Direction> unmodifiableIterator = field_34006.iterator(); unmodifiableIterator.hasNext(); ) {
            BlockPos iteratedPos = pos.offset(unmodifiableIterator.next().getOpposite());

            Fluid primary = world.getFluidState(pos).getFluid();

            Fluid secondaryFluid = world.getFluidState(iteratedPos).getFluid();

            Block secondaryBlock = world.getBlockState(iteratedPos).getBlock();

            //find best match

            for (int i = 0; i < MechanicalFactory.generatorMap.size(); i++) {
                GeneratorIdentifier gi = MechanicalFactory.generatorMap.get(i);
                int tempMatchVal = gi.getSimilarity(primary, secondaryFluid, secondaryBlock, down);
                if(tempMatchVal > matchVal){
                    matchVal = tempMatchVal;
                    matchIndex = i;
                    //System.out.println("found a match");
                }
            }
        }

        if(matchIndex != -1){
            BlockState out = MechanicalFactory.generatorMap.get(matchIndex).getBlockOut().getDefaultState();
            world.setBlockState(pos, out);
            if(out.equals(Blocks.FROSTED_ICE.getDefaultState()))
                world.getBlockTickScheduler().schedule(pos, Blocks.FROSTED_ICE, 50);

            if (world.getFluidState(pos).isIn((Tag)FluidTags.LAVA)) world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
            cir.setReturnValue(false);
            //System.out.println("found a match");
        }
    }
}
