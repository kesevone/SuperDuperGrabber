package sdgrabber.items;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sdgrabber.configs.GrabberConfig;

public abstract class BaseGrabber extends ToolItem {
    public final Integer ITEM_DAMAGE;
    public final String NBT_STORED_ANY = "StoredAny";

    public BaseGrabber(ToolMaterial material, Item.Settings settings, Integer itemDamage) {
        super(material, settings);
        this.ITEM_DAMAGE = itemDamage;
    }

    public void playPickupSound(World world, BlockPos pos) {
        SoundEvent soundEvent = SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
        if (getGrabberConfig().enablePufferfishSounds) {
            soundEvent = SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP;
        }
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 0.6f, 1.0f);
    }

    public void playPlaceSound(World world, BlockPos pos) {
        if (getGrabberConfig().enablePufferfishSounds) {
            world.playSound(null, pos, SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, SoundCategory.BLOCKS, 0.6f, 1.0f);
        }
        world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
    }

    public void spawnParticles(World world, BlockPos pos, int count) {
        spawnParticles(world, pos, count, 0.1);
    }

    public void spawnParticles(World world, BlockPos pos) {
        spawnParticles(world, pos, 10, 0.1);
    }

    public void spawnParticles(World world, BlockPos pos, int count, double speed) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            for (int i = 0; i < count; i++) {
                double offsetX = world.random.nextGaussian() * 0.3;
                double offsetY = world.random.nextGaussian() * 0.3;
                double offsetZ = world.random.nextGaussian() * 0.3;
                serverWorld.spawnParticles(
                        ParticleTypes.SMOKE,
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        1,
                        0.0, 0.1, 0.0,
                        speed
                );
                serverWorld.spawnParticles(
                        ParticleTypes.CLOUD,
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        1,
                        0.0, 0.1, 0.0,
                        speed
                );
            }
        } else {
            for (int i = 0; i < count; i++) {
                double offsetX = world.random.nextGaussian() * 0.3;
                double offsetY = world.random.nextGaussian() * 0.3;
                double offsetZ = world.random.nextGaussian() * 0.3;
                world.addParticle(
                        ParticleTypes.SMOKE,
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        0.0, 0.1, 0.0
                );
                world.addParticle(
                        ParticleTypes.CLOUD,
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        0.0, 0.1, 0.0
                );
            }
        }
    }

    public void damageGrabber(Hand hand, PlayerEntity player, ItemStack stack, World world) {
        if (!world.isClient()) {
            stack.damage(ITEM_DAMAGE, player, p -> p.sendToolBreakStatus(hand));
        }
    }

    public boolean hasStoredAny(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.getBoolean(NBT_STORED_ANY);
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);

        if (hasStoredAny(stack)) {
            return Text.literal(
                    baseName.getString()
            ).formatted(Formatting.AQUA);
        }

        return baseName;
    }

    public GrabberConfig getGrabberConfig() {
        return AutoConfig.getConfigHolder(GrabberConfig.class).getConfig();
    }
}
