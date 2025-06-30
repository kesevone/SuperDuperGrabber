package sdgrabber.items;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("SameParameterValue")
public class Grabber extends BaseChestGrabber {

  public Grabber(ToolMaterial material, Settings settings, Integer itemDamage, Integer maxChests) {
    super(material, settings, itemDamage, maxChests);
  }

  @Override
  public ActionResult useOnBlock(ItemUsageContext context) {
    World world = context.getWorld();
    BlockPos pos = context.getBlockPos();
    PlayerEntity player = context.getPlayer();
    ItemStack stack = context.getStack();

    if (player == null) {
      return ActionResult.FAIL;
    }

    BlockState blockState = world.getBlockState(pos);
    Block block = blockState.getBlock();
    Hand hand = context.getHand();
    BlockHitResult blockHitResult = new BlockHitResult(
            context.getHitPos(),
            context.getSide(),
            context.getBlockPos(),
            context.hitsInsideBlock()
    );

    if (hasStoredAny(stack)) {
      return placeSingleChest(
              world,
              pos,
              player,
              stack,
              blockHitResult,
              hand,
              blockState
      );
    } else {
      return pickupSingleChest(
              world,
              pos,
              stack,
              blockState,
              block
      );
    }
  }
}
