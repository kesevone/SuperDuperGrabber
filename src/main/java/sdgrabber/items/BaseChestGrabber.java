package sdgrabber.items;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sdgrabber.SuperDuperGrabber;
import sdgrabber.configs.GrabberConfig;
import sdgrabber.configs.GrabberSettings;
import tech.thatgravyboat.ironchests.common.blocks.GenericChestBlock;

public abstract class BaseChestGrabber extends BaseGrabber {
    protected static Integer MAX_CHESTS;
    protected static final String NBT_CHESTS_DATA = "ChestsData";
    protected static final String NBT_BLOCKS_STATE = "BlocksState";
    protected static final String NBT_CHESTS_COUNT = "ChestsCount";

    public BaseChestGrabber(ToolMaterial material, Settings settings, Integer itemDamage, Integer maxChests) {
        super(material, settings, itemDamage);
        MAX_CHESTS = maxChests;
    }

    public boolean isValidChest(Block block) {
        boolean blockInstanceofIronChest = false;
        if (FabricLoader.getInstance().isModLoaded("ironchests")) {
            blockInstanceofIronChest = block instanceof GenericChestBlock;
        }

        return block instanceof ChestBlock ||
                block instanceof TrappedChestBlock ||
                block instanceof BarrelBlock ||
                blockInstanceofIronChest;
    }

    protected NbtCompound getBlockStateNbt(Block block, BlockState blockState) {
        NbtCompound blockStateNbt = new NbtCompound();
        blockStateNbt.putString("Name", Registries.BLOCK.getId(block).toString());
        if (block instanceof ChestBlock || block instanceof TrappedChestBlock) {
            if (blockState.contains(ChestBlock.FACING)) {
                blockStateNbt.putString("Facing", blockState.get(ChestBlock.FACING).toString());
            }
            if (blockState.contains(ChestBlock.CHEST_TYPE)) {
                blockStateNbt.putString("ChestType", blockState.get(ChestBlock.CHEST_TYPE).toString());
            }
        } else if (block instanceof BarrelBlock) {
            if (blockState.contains(BarrelBlock.FACING)) {
                blockStateNbt.putString("Facing", blockState.get(BarrelBlock.FACING).toString());
            }
        }
        return blockStateNbt;
    }

    protected void putItemsToInventory(NbtCompound data, Inventory inventory) {
        NbtList items = data.getList("ChestItems", 10);
        for (int i = 0; i < items.size(); i++) {
            NbtCompound chestItemNbt = items.getCompound(i);
            int slot = chestItemNbt.getInt("Slot");
            if (slot >= 0 && slot < inventory.size()) {
                inventory.setStack(slot, ItemStack.fromNbt(chestItemNbt));
            }
        }
        if (inventory instanceof BlockEntity blockEntity) {
            blockEntity.markDirty();
        }
    }

    protected NbtCompound putItemsToNbt(BlockEntity entity) {
        NbtCompound chestData = new NbtCompound();
        NbtList items = new NbtList();
        if (entity instanceof Inventory inventory) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack item = inventory.getStack(i);
                if (!item.isEmpty()) {
                    NbtCompound itemNbt = new NbtCompound();
                    itemNbt.putInt("Slot", i);
                    item.writeNbt(itemNbt);
                    items.add(itemNbt);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
        chestData.put("ChestItems", items);
        return chestData;
    }

    protected @Nullable Block getChestBlockFromNbt(NbtCompound blockStateNbt) {
        String blockName = blockStateNbt.getString("Name");
        Block block = Registries.BLOCK.get(new Identifier(blockName));
        if (isValidChest(block)) {
            return block;
        }
        return null;
    }

    public ActionResult pickupSingleChest(
            World world,
            BlockPos pos,
            ItemStack stack,
            BlockState blockState,
            Block block
    ) {
        if (!isValidChest(block)) {
            return ActionResult.FAIL;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof Inventory)) {
            return ActionResult.FAIL;
        }

        NbtCompound blockStateNbt = getBlockStateNbt(block, blockState);
        NbtCompound chestData = putItemsToNbt(blockEntity);

        NbtCompound itemNbt = stack.getOrCreateNbt();
        float chestsCount = itemNbt.getFloat(NBT_CHESTS_COUNT);
        NbtList listOfChestsData = itemNbt.getList(NBT_CHESTS_DATA, 10);
        NbtList listOfBlocksState = itemNbt.getList(NBT_BLOCKS_STATE, 10);

        if (!canPickupMoreChests(stack)) {
            return ActionResult.FAIL;
        }

        itemNbt.putBoolean(NBT_STORED_ANY, true);
        itemNbt.putFloat(NBT_CHESTS_COUNT, chestsCount + 1.0F);
        listOfChestsData.add(chestData);
        listOfBlocksState.add(blockStateNbt);
        itemNbt.put(NBT_CHESTS_DATA, listOfChestsData);
        itemNbt.put(NBT_BLOCKS_STATE, listOfBlocksState);

        world.removeBlockEntity(pos);
        world.removeBlock(pos, false);

        playPickupSound(world, pos);
        if (getGrabberConfig().showInteractionParticles) {
            spawnParticles(world, pos);
        }

        return ActionResult.SUCCESS;
    }

    public ActionResult placeSingleChest(
            World world,
            BlockPos pos,
            PlayerEntity player,
            ItemStack stack,
            BlockHitResult hitResult,
            Hand hand,
            BlockState targetBlockState
    ) {
        Direction side = hitResult.getSide();
        BlockPos targetPos = hitResult.getBlockPos().offset(side);

        if (!world.canPlayerModifyAt(player, targetPos)) {
            SuperDuperGrabber.LOGGER.info("Player {}: can't modify target pos, canPlayerModifyAt() == true.", player.getName());
            return ActionResult.FAIL;
        }

        if (targetBlockState.isAir()) {
            SuperDuperGrabber.LOGGER.info("Player {}: can't place chest, targetBlockState.isAir() == true.", player.getName());
            return ActionResult.FAIL;
        }

        if (world.getBlockEntity(targetPos) != null) {
            SuperDuperGrabber.LOGGER.info("Player {}: can't get block entity, getBlockEntity(targetPos) == null.", player.getName());
            return ActionResult.FAIL;
        }

        NbtCompound itemNbt = stack.getNbt();
        if (itemNbt == null) {
            SuperDuperGrabber.LOGGER.info("Player {}: can't get NBT data from item: {}, itemNbt == null.", player.getName(), stack.getItem().getName());
            return ActionResult.FAIL;
        }

        float chestsCount = itemNbt.getFloat(NBT_CHESTS_COUNT);
        NbtList listOfChestsData = itemNbt.getList(NBT_CHESTS_DATA, 10);
        NbtList listOfBlocksState = itemNbt.getList(NBT_BLOCKS_STATE, 10);

        if (listOfBlocksState.isEmpty() || listOfChestsData.isEmpty()) {
            SuperDuperGrabber.LOGGER.info("Player {}: can't retrieve a list of NBT data from the item, listOf[...].isEmpty() == true.", player.getName());
            return ActionResult.FAIL;
        }

        // Get the data of the last chest taken
        NbtCompound lastChestData = (NbtCompound) listOfChestsData.get(listOfChestsData.size() - 1);
        NbtCompound lastBlockState = (NbtCompound) listOfBlocksState.get(listOfBlocksState.size() - 1);

        @Nullable Block chestBlockState = getChestBlockFromNbt(lastBlockState);
        if (chestBlockState == null) {
            SuperDuperGrabber.LOGGER.error("Player {}: can't get block state from NBT data, chestBlockState == null.", player.getName());
            return ActionResult.FAIL;
        }

        BlockHitResult newHit = new BlockHitResult(
                hitResult.getPos(),
                side,
                targetPos,
                false
        );

        ItemPlacementContext placementContext = new ItemPlacementContext(
                player,
                hand,
                stack,
                newHit
        );

        BlockState placementState = chestBlockState.getPlacementState(placementContext);
        if (placementState == null || !world.canPlace(placementState, targetPos, ShapeContext.absent())) {
            return ActionResult.FAIL;
        }

        if (!world.isClient()) {
            world.setBlockState(targetPos, placementState);
            BlockEntity blockEntity = world.getBlockEntity(targetPos);
            if (blockEntity instanceof Inventory inventory) {
                putItemsToInventory(lastChestData, inventory);
                damageGrabber(hand, player, stack, world);
            } else {
                SuperDuperGrabber.LOGGER.error("Player {}: block entity is not an inventory instance, blockEntity instanceof Inventory inventory == false.", player.getName());
                return ActionResult.FAIL;
            }
        }

        listOfChestsData.remove(lastChestData);
        listOfBlocksState.remove(lastBlockState);
        if (chestsCount > 0) {
            itemNbt.putFloat(NBT_CHESTS_COUNT, chestsCount - 1.0F);
        }

        if (listOfChestsData.isEmpty() && listOfBlocksState.isEmpty()) {
            itemNbt.remove(NBT_BLOCKS_STATE);
            itemNbt.remove(NBT_CHESTS_DATA);
            itemNbt.remove(NBT_CHESTS_COUNT);
            itemNbt.remove(NBT_STORED_ANY);
        } else {
            itemNbt.put(NBT_BLOCKS_STATE, listOfBlocksState);
            itemNbt.put(NBT_CHESTS_DATA, listOfChestsData);
        }

        if (itemNbt.isEmpty()) {
            stack.setNbt(null);
        }

        playPlaceSound(world, pos);
        if (getGrabberConfig().showInteractionParticles) {
            spawnParticles(world, pos, 20);
        }

        return ActionResult.SUCCESS;
    }

    public boolean canPickupMoreChests(ItemStack stack) {
        return getChestsCount(stack) < MAX_CHESTS;
    }

    public int getChestsCount(ItemStack stack) {
        NbtCompound itemNbt = stack.getOrCreateNbt();
        return itemNbt.getInt(NBT_CHESTS_COUNT);
    }

    public int getDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }

    public int getGrabsCount(ItemStack stack) {
        return Math.round((float) getDurability(stack) / ITEM_DAMAGE);
    }

    @Override
    public void appendTooltip(
        ItemStack stack,
        World world,
        java.util.List<net.minecraft.text.Text> tooltip,
        net.minecraft.client.item.TooltipContext context
    ) {
        tooltip.add(Text.translatable("message.sdgrabber.desc.tooltip").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("message.sdgrabber.howtouse.tooltip").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("message.sdgrabber.contains_chest.tooltip", getChestsCount(stack), MAX_CHESTS).formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.translatable("message.sdgrabber.grabs_count.tooltip", getGrabsCount(stack)).formatted(Formatting.DARK_GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }

    public @Nullable GrabberSettings getSettingsForGrabber(Item item) {
        GrabberConfig config = getGrabberConfig();
        if (item == BaseItem.WOODEN_GRABBER) {
            return config.woodenGrabber;
        } else if (item == BaseItem.IRON_GRABBER) {
            return config.ironGrabber;
        } else if (item == BaseItem.DIAMOND_GRABBER) {
            return config.diamondGrabber;
        } else if (item == BaseItem.DOUBLE_GRABBER) {
            return config.doubleGrabber;
        }
        return null;
    }
}