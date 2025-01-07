package daripher.apothiccurios.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import daripher.apothiccurios.ApothCuriosTooltipHandler;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.ClientEventHandler;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ClientEventHandler.class, remap = false)
public abstract class ClientEventHandlerMixin {
    @Unique private static ApothCuriosTooltipHandler.StoredData apothCurios$storedData;

    @Inject(method = "onTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/util/LazyOptional;ifPresent(Lnet/minecraftforge/common/util/NonNullConsumer;)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void apothCurios$storeData(final ItemTooltipEvent event, final CallbackInfo callback, @Local(name = "slots") final List<String> slots, @Local(name = "attributeTooltip") final List<Component> attributeTooltip) {
        int currentIndex = event.getToolTip().size() - 1;
        int nextIndex = currentIndex + attributeTooltip.size();

        if (attributeTooltip.isEmpty() || nextIndex < currentIndex) {
            currentIndex = -1;
            nextIndex = -1;
        }

        // Some mods seem to clear the attribute list when receiving it
        apothCurios$storedData = new ApothCuriosTooltipHandler.StoredData(event, slots, new ArrayList<>(attributeTooltip), currentIndex, nextIndex);
    }

    /** Some items don't return all attributes */
    @ModifyVariable(method = "lambda$onTooltip$1", at = @At("STORE"), ordinal = 2)
    private static List<Component> apothCurios$setActualAttributeList(final List<Component> tooltips) {
        if (apothCurios$storedData == null) {
            return tooltips;
        }

        List<Component> attributeTooltip = apothCurios$storedData.attributeTooltip();

        if (tooltips.isEmpty() && !attributeTooltip.isEmpty()) {
            return attributeTooltip;
        }

        return tooltips;
    }

    @Inject(method = "onTooltip", at = @At(value = "TAIL"))
    private void apothCurios$handleTooltip(final ItemTooltipEvent event, final CallbackInfo callback) {
        if (apothCurios$storedData == null) {
            return;
        }

        ApothCuriosTooltipHandler.handleTooltip(apothCurios$storedData);
        apothCurios$storedData = null;
    }
}