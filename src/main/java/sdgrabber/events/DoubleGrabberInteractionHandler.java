package sdgrabber.events;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sdgrabber.items.BaseChestGrabber;
import sdgrabber.materials.GrabberMaterial;

public class DoubleGrabberInteractionHandler {

    public static void register() {
        UseBlockCallback.EVENT.register(DoubleGrabberInteractionHandler::useOnBlock);
    }

    private static ActionResult useOnBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();

        if (!(item instanceof BaseChestGrabber grabber)) {
            return ActionResult.PASS;
        }

        if (grabber.getMaterial() != GrabberMaterial.DOUBLE) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        
        if (player.isSneaking() && grabber.isValidChest(block)) {
            ActionResult result = grabber.pickupSingleChest(world, pos, stack, blockState, block);
            if (result == ActionResult.SUCCESS) {
                return ActionResult.SUCCESS;
            }
        } else if (grabber.hasStoredAny(stack)) {
            ActionResult result = grabber.placeSingleChest(world, pos, player, stack, hitResult, hand, blockState);
            if (result == ActionResult.SUCCESS) {
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}