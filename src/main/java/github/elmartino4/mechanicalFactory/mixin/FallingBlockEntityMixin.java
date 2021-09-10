package github.elmartino4.mechanicalFactory.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.MessageType;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import github.elmartino4.mechanicalFactory.config.ModConfig;

import java.util.*;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    @Shadow BlockState block;

    public int active = 0;
    public int lastBroken = 0;

    HashMap<List<BlockState>, List<BlockState>> anvilMap = new HashMap<List<BlockState>, List<BlockState>>();
    public FallingBlockEntityMixin(EntityType<?> entityType_1, World world_1)
    {
        super(entityType_1, world_1);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void onTick(CallbackInfo ci)
    {
        if(anvilMap.size() == 0){
            anvilMap.put(Arrays.asList(Blocks.COBBLESTONE.getDefaultState()), Arrays.asList(Blocks.GRAVEL.getDefaultState()));
            anvilMap.put(Arrays.asList(Blocks.STONE.getDefaultState(), Blocks.GRAVEL.getDefaultState()), Arrays.asList(Blocks.ANDESITE.getDefaultState()));
            anvilMap.put(Arrays.asList(Blocks.GRAVEL.getDefaultState(), Blocks.STONE.getDefaultState()), Arrays.asList(Blocks.ANDESITE.getDefaultState()));
            anvilMap.put(Arrays.asList(Blocks.ANDESITE.getDefaultState()), Arrays.asList(Blocks.DIRT.getDefaultState()));
            anvilMap.put(Arrays.asList(Blocks.NETHERRACK.getDefaultState(), Blocks.SOUL_SOIL.getDefaultState()), Arrays.asList(Blocks.SOUL_SAND.getDefaultState(), Blocks.SOUL_SOIL.getDefaultState()));
        }

        if(active > 5 * lastBroken){
            active = 0;
            lastBroken = 0;
        }

        if (block.isOf(Blocks.ANVIL))
        {

            BlockPos pos = getBlockPos();
            BlockPos hit =
                    this.world.raycast(new RaycastContext(
                            new Vec3d(this.prevX, this.prevY, this.prevZ),
                            getPos(),
                            RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.SOURCE_ONLY, this
                    )).getBlockPos().down();

            if(world.getBlockState(new BlockPos(this.getX(), this.getY() + getVelocity().y + 0.04, this.getZ())).getBlock() != Blocks.AIR){
                onCollision(hit);
            }
        }
    }

    private void onCollision(BlockPos hit){
        boolean match = false;
        if(lastBroken == 0){
            System.out.println("call tick");
        }else{
            active++;
        }

        if(lastBroken == 0)
            for (int i = ModConfig.INSTANCE.scanDistance; i > 0 && !match; i--) {
                List<BlockState> blockList = new ArrayList<>();

                for (int j = 0; j < i; j++) {
                    blockList.add(world.getBlockState(hit.down(j)));
                }

                System.out.println(blockList.toString());
                System.out.println("map size = " + anvilMap.size());

                List<BlockState> changeBlocks = anvilMap.get(blockList);

                match = changeBlocks != null;//!changeBlocks.isEmpty();

                System.out.println("match = " + match);

                if(match){

                    // matches value in anvilMap
                    for (int j = 0; j < i; j++) {
                        world.breakBlock(hit.down(j), false);
                    }

                    for (int j = 0; j < changeBlocks.size(); j++) {
                        world.setBlockState(hit.down(i - j - 1), changeBlocks.get(j), 3);
                        //System.out.println("j = " + i - j + ", id = " + changeBlocks.get(j));
                    }

                    System.out.println("i = " + i + ", j = " + (i + changeBlocks.size()));
                    lastBroken = i - changeBlocks.size();
                }
            }
    }
}
