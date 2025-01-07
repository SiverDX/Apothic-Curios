package daripher.apothiccurios.mixin.client;

import com.google.common.collect.Multimap;
import dev.shadowsoffire.attributeslib.client.AttributesLibClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(value = AttributesLibClient.class, remap = false)
public interface AttributesLibClientAccess {
    @Invoker("getHideFlags")
    static int apothCurios$getHideFlags(final ItemStack stack) {
        throw new AssertionError();
    }

    @Invoker("shouldShowInTooltip")
    static boolean apothCurios$shouldShowInTooltip(int hideFlags, final ItemStack.TooltipPart part) {
        throw new AssertionError();
    }

    @Invoker("applyModifierTooltips")
    static void apothCurios$applyModifierTooltips(@Nullable final Player player, final ItemStack stack, final Consumer<Component> tooltip, final TooltipFlag flag) {
        throw new AssertionError();
    }

    @Invoker("applyTextFor")
    static void apothCurios$applyTextFor(@Nullable final Player player, final ItemStack stack, final Consumer<Component> tooltip, final Multimap<Attribute, AttributeModifier> modifierMap, final String group, final Set<UUID> skips, final TooltipFlag flag) {
        throw new AssertionError();
    }
}