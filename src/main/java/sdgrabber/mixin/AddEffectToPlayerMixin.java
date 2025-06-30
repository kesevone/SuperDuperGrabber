package sdgrabber.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sdgrabber.configs.GrabberSettings;
import sdgrabber.items.BaseChestGrabber;

@Mixin(PlayerEntity.class)
public abstract class AddEffectToPlayerMixin extends LivingEntity {
    @Unique
    private static final String GRABBER_SLOWNESS_TAG = "GrabberSlownessActive";

    protected AddEffectToPlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void applySlownessIfGrabberWithChest(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerInventory inventory = player.getInventory();
        int totalChestsCount = 0;
        int slownessAmplifier = 0;
        int maxSlownessAmplifier = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BaseChestGrabber grabber && grabber.hasStoredAny(stack)) {
                int chestCount = grabber.getChestsCount(stack);
                totalChestsCount += chestCount;

                @Nullable GrabberSettings settings = grabber.getSettingsForGrabber(stack.getItem());
                if (settings != null) {
                    slownessAmplifier += chestCount * settings.slownessAmplifierPerChest;
                    maxSlownessAmplifier = Math.max(maxSlownessAmplifier, settings.maxSlownessAmplifier);
                }
            }
        }

        NbtCompound playerNbt = new NbtCompound();
        this.writeCustomDataToNbt(playerNbt);
        boolean isGrabberSlownessActive = playerNbt.getBoolean(GRABBER_SLOWNESS_TAG);

        if (totalChestsCount > 0) {
            slownessAmplifier = Math.min(slownessAmplifier, maxSlownessAmplifier) - 1;

            StatusEffectInstance currentEffect = this.getStatusEffect(StatusEffects.SLOWNESS);
            boolean shouldApplyEffect = currentEffect == null || currentEffect.getAmplifier() != slownessAmplifier;

            if (shouldApplyEffect) {
                this.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS,
                        1,
                        slownessAmplifier,
                        false, false, true
                ));
                playerNbt.putBoolean(GRABBER_SLOWNESS_TAG, true);
                this.readCustomDataFromNbt(playerNbt);
            }
        } else if (isGrabberSlownessActive) {
            StatusEffectInstance currentEffect = this.getStatusEffect(StatusEffects.SLOWNESS);
            if (currentEffect != null) {
                this.removeStatusEffect(StatusEffects.SLOWNESS);
                playerNbt.putBoolean(GRABBER_SLOWNESS_TAG, false);
                this.readCustomDataFromNbt(playerNbt);
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        StatusEffectInstance currentEffect = this.getStatusEffect(StatusEffects.SLOWNESS);
        nbt.putBoolean(GRABBER_SLOWNESS_TAG, currentEffect != null && nbt.getBoolean(GRABBER_SLOWNESS_TAG));
    }
}