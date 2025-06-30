package sdgrabber.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sdgrabber.items.BaseGrabber;

@Mixin(PlayerInventory.class)
public abstract class PreventDropGrabberMixin {
    @Shadow
    public abstract ItemStack getMainHandStack();

    @Shadow @Final public PlayerEntity player;

    @Inject(at = @At("HEAD"), method = "dropSelectedItem", cancellable = true)
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
        if (getMainHandStack().getItem() instanceof BaseGrabber grabber) {
            if (grabber.hasStoredAny(getMainHandStack())) {
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
    }
}