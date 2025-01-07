package daripher.apothiccurios.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = AttributesLibClient.class, remap = false)
public abstract class AttributesLibClientMixin {
    @SuppressWarnings("unchecked")
    @ModifyArg(method = "applyTextFor", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1))
    private static <T> T apothCurios$addCuriosGroupTooltip(final T component, @Local(argsOnly = true) final String group) {
        if (group.startsWith("ignore:")) {
            return (T) Component.translatable("curios.modifiers." + group.substring(group.indexOf(":") + 1)).withStyle(ChatFormatting.GOLD);
        } else if (group.startsWith("empty:")) {
            return (T) Component.empty();
        }

        return component;
    }
}