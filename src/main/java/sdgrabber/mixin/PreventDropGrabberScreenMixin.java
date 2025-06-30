package sdgrabber.mixin;

import java.util.EnumSet;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sdgrabber.items.BaseGrabber;

@Mixin(HandledScreen.class)
public abstract class PreventDropGrabberScreenMixin<T extends ScreenHandler> {
    @Unique
    private static final EnumSet<SlotActionType> RESTRICTED_ACTIONS = EnumSet.of(SlotActionType.THROW, SlotActionType.PICKUP);

    @Inject(at = @At("HEAD"), method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", cancellable = true)
    public void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slot != null) {
            ItemStack slotStack = slot.getStack();

            if (!slotStack.isEmpty() && slotStack.getItem() instanceof BaseGrabber grabber && grabber.hasStoredAny(slotStack)) {
                if (RESTRICTED_ACTIONS.contains(actionType)) {
                    ci.cancel();
                }
            }
        }

    }
}
