package daripher.apothiccurios;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.datafixers.util.Either;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.client.SocketTooltipRenderer;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@Mod(ApothicCuriosMod.MOD_ID)
public class ApothicCuriosMod {
  public static final String MOD_ID = "apothiccurios";
  public static final EquipmentSlot FAKE_SLOT = EquipmentSlot.LEGS;

  public ApothicCuriosMod() {
    if (!adventureModuleEnabled()) return;
    IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
    forgeEventBus.addListener(EventPriority.LOW, this::addCurioSocketTooltip);
    forgeEventBus.addListener(this::applyCurioAttributeAffixes);
    forgeEventBus.addListener(this::applyCurioDamageAffixes);
    forgeEventBus.addListener(EventPriority.LOWEST, this::removeFakeCurioAttributes);
    forgeEventBus.addListener(EventPriority.LOWEST, this::removeGemAttributeTooltips);
  }

  private void addCurioSocketTooltip(RenderTooltipEvent.GatherComponents event) {
    ItemStack stack = event.getItemStack();
    if (isNonCurio(stack)) return;
    event
        .getTooltipElements()
        .removeIf(
            c ->
                c.right()
                    .filter(SocketTooltipRenderer.SocketComponent.class::isInstance)
                    .isPresent());
    SocketTooltipRenderer.SocketComponent component =
        new SocketTooltipRenderer.SocketComponent(stack, SocketHelper.getGems(stack));
    event.getTooltipElements().add(Either.right(component));
  }

  private void applyCurioAttributeAffixes(CurioAttributeModifierEvent event) {
    ItemStack stack = event.getItemStack();
    if (!stack.hasTag()) return;
    //noinspection ConstantValue
    if (!CuriosApi.isStackValid(event.getSlotContext(), stack)) return;
    AffixHelper.getAffixes(stack).forEach((a, i) -> i.addModifiers(FAKE_SLOT, event::addModifier));
  }

  private void applyCurioDamageAffixes(LivingHurtEvent event) {
    DamageSource source = event.getSource();
    LivingEntity entity = event.getEntity();
    AtomicDouble amount = new AtomicDouble(event.getAmount());
    getEquippedCurios(entity)
        .forEach(
            curio -> {
              for (AffixInstance affix : AffixHelper.getAffixes(curio).values()) {
                amount.set(affix.onHurt(source, entity, amount.floatValue()));
              }
            });
    event.setAmount(amount.floatValue());
  }

  private void removeFakeCurioAttributes(ItemAttributeModifierEvent event) {
    if (isNonCurio(event.getItemStack())) return;
    if (event.getSlotType() == FAKE_SLOT) event.clearModifiers();
  }

  private void removeGemAttributeTooltips(ItemTooltipEvent event) {
    ItemStack stack = event.getItemStack();
    if (!stack.hasTag()) return;
    if (isNonCurio(stack)) return;
    SocketHelper.getGemInstances(stack).forEach(g -> removeTooltip(event, g, stack));
  }

  private void removeTooltip(ItemTooltipEvent event, GemInstance gem, ItemStack stack) {
    gem.gem()
        .get()
        .getBonus(LootCategory.forItem(stack))
        .ifPresent(
            b -> {
              b.addModifiers(
                  gem.gemStack(),
                  gem.rarity().get(),
                  (a, m) ->
                      removeTooltip(
                          event,
                          IFormattableAttribute.toComponent(a, m, AttributesLib.getTooltipFlag())));
              removeTooltip(event, b.getSocketBonusTooltip(gem.gemStack(), gem.rarity().get()));
            });
  }

  private static void removeTooltip(ItemTooltipEvent event, Component tooltip) {
    event.getToolTip().removeIf(c -> c.getString().equals(tooltip.getString()));
  }

  public static void registerCurioLootCategory(String id) {
    String slotId = id.replace("curios:", "");
    SlotContext slotContext = new SlotContext(slotId, null, 0, false, false);
    Predicate<ItemStack> validator = s -> CuriosApi.isStackValid(slotContext, s);
    EquipmentSlot[] fakeSlots = {FAKE_SLOT};
    LootCategory.register(null, id, validator, fakeSlots);
  }

  private boolean adventureModuleEnabled() {
    return Apotheosis.enableAdventure;
  }

  private static boolean isNonCurio(ItemStack stack) {
    return CuriosApi.getItemStackSlots(stack).isEmpty();
  }

  public static List<ItemStack> getEquippedCurios(LivingEntity entity) {
    List<ItemStack> curios = new ArrayList<>();
    CuriosApi.getCuriosInventory(entity)
        .map(ICuriosItemHandler::getEquippedCurios)
        .ifPresent(
            i -> {
              for (int slot = 0; slot < i.getSlots(); slot++) {
                curios.add(i.getStackInSlot(slot));
              }
            });
    return curios;
  }
}
