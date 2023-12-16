package daripher.apothiccurios.mixin;

import daripher.apothiccurios.ApothicCuriosMod;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LootCategory.class, remap = false)
public class LootCategoryMixin {
  @Inject(method = "byId", at = @At("HEAD"))
  private static void registerCurioCategory(
      String id, CallbackInfoReturnable<LootCategory> callbackInfo) {
    if (id.startsWith("curios:") && LootCategory.BY_ID.get(id) == null) {
      ApothicCuriosMod.registerCurioLootCategory(id);
    }
  }
}
