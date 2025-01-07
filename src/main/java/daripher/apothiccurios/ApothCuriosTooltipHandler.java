package daripher.apothiccurios;

import com.google.common.collect.Multimap;
import daripher.apothiccurios.mixin.client.AttributesLibClientAccess;
import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import dev.shadowsoffire.attributeslib.api.client.AddAttributeTooltipsEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.common.CuriosHelper;

import java.util.*;

public class ApothCuriosTooltipHandler {
    public record StoredData(ItemTooltipEvent event, List<String> slots, List<Component> attributeTooltip, int firstIndex, int secondIndex) {}

    public static void handleTooltip(final StoredData storedData) {
        ItemTooltipEvent event = storedData.event();
        ItemStack stack = event.getItemStack();

        if (!(stack.getItem() instanceof ICurioItem)) {
            return;
        }

        List<Component> list = event.getToolTip();
        int markIdx1 = storedData.firstIndex();
        int markIdx2 = storedData.secondIndex();

        if (markIdx2 > list.size()) {
            // Mod provided attribute list is not empty and differs from the curios gathered attribute list
            markIdx1 = -1;
            markIdx2 = -1;
        }

        if (markIdx1 != -1 && markIdx2 != -1) {
            ListIterator<Component> it = list.listIterator(markIdx1);

            for (int i = markIdx1; i < markIdx2 + 1; ++i) {
                Component next = it.next();

                if (next instanceof MutableComponent mutable && !mutable.getSiblings().isEmpty() && mutable.getSiblings().get(0) instanceof MutableComponent sibling && sibling.getContents() instanceof TranslatableContents translatable && shouldSkipKey(translatable)) {
                    // Non-attribute modifiers / information which needs to be kept
                    continue;
                }

                it.remove();
            }

            if (AttributesLibClientAccess.apothCurios$shouldShowInTooltip(AttributesLibClientAccess.apothCurios$getHideFlags(stack), ItemStack.TooltipPart.MODIFIERS)) {
                AttributesLibClientAccess.apothCurios$applyModifierTooltips(event.getEntity(), stack, it::add, event.getFlags());

                List<String> slots = storedData.slots();

                if (slots != null) {
                    for (String slot : slots) {
                        Multimap<Attribute, AttributeModifier> unsorted = CuriosApi.getCuriosHelper().getAttributeModifiers(new SlotContext(slot, event.getEntity(), 0, false, true), UUID.randomUUID(), stack);
                        Multimap<Attribute, AttributeModifier> map = AttributeHelper.sortedMap();

                        List<Component> slotModifierTooltips = new ArrayList<>();

                        for (Map.Entry<Attribute, AttributeModifier> ent : unsorted.entries()) {
                            if (ent.getKey() != null && ent.getValue() != null) {
                                if (ent.getKey() instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
                                    // Curios slot modifiers are not part of the attribute registry
                                    Component slotModifierTooltip = getSlotModifierTooltip(ent.getValue(), wrapper);

                                    if (slotModifierTooltip != null) {
                                        slotModifierTooltips.add(slotModifierTooltip);
                                    }

                                    continue;
                                }

                                map.put(ent.getKey(), ent.getValue());
                            }
                        }

                        AttributesLibClientAccess.apothCurios$applyTextFor(event.getEntity(), stack, it::add, map, "ignore:" + slot, new HashSet<>(), event.getFlags());

                        for (Component slotModifier : slotModifierTooltips) {
                            it.add(slotModifier);
                        }
                    }
                }
            }

            MinecraftForge.EVENT_BUS.post(new AddAttributeTooltipsEvent(stack, event.getEntity(), list, it, event.getFlags()));

            if (it.previous().getContents() instanceof LiteralContents contents && contents.text().equals("APOTH_REMOVE_MARKER")) {
                it.remove();
            }

            // Add a newline between the apotheosis dot-affix description
            // And any potentially existing 'When worn as' pseudo-modifiers
            int count = 0;

            while (it.hasPrevious()) {
                Component component = it.previous();

                if (component.getContents() instanceof TranslatableContents translatable && translatable.getKey().startsWith("curios.modifiers.")) {
                    count++;

                    if (count == 2) {
                        it.next(); // Back to the 'curios.modifiers' component

                        while (it.hasNext()) {
                            Component next = it.next();

                            if (next.getContents() == LiteralContents.EMPTY) {
                                // No need to do anything if there is a newline
                                break;
                            }

                            if (next.getContents() instanceof TranslatableContents otherTranslatable && otherTranslatable.getKey().equals("text.apotheosis.dot_prefix")) {
                                it.previous(); // Make sure the newline is added before this entry
                                it.add(Component.empty());
                                break;
                            }
                        }

                        break;
                    }
                }
            }
        }
    }

    private static boolean shouldSkipKey(final TranslatableContents translatable) {
        return translatable.getKey().equals("tooltip.irons_spellbooks.enhance_spell_level") || translatable.getKey().equals("item.irons_spellbooks.concentration_amulet.desc");
    }

    private static Component getSlotModifierTooltip(final AttributeModifier modifier, CuriosHelper.SlotAttributeWrapper wrapper) {
        Component component = null;

        if (modifier.getAmount() > 0) {
            component = Component.translatable(
                            "curios.modifiers.slots.plus." +
                                    AttributeModifier.Operation.ADDITION.toValue(),
                            ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(modifier.getAmount()),
                            Component.translatable("curios.identifier." + wrapper.identifier))
                    .withStyle(ChatFormatting.BLUE);
        } else if (modifier.getAmount() < 0) {
            component = Component.translatable(
                            "curios.modifiers.slots.take." +
                                    AttributeModifier.Operation.ADDITION.toValue(),
                            ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(modifier.getAmount()),
                            Component.translatable("curios.identifier." + wrapper.identifier))
                    .withStyle(ChatFormatting.RED);
        }

        return component;
    }
}