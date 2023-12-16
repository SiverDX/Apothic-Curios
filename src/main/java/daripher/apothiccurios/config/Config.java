package daripher.apothiccurios.config;

import daripher.apothiccurios.ApothicCuriosMod;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ApothicCuriosMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
  public static final ForgeConfigSpec SPEC;
  private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
  private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SLOTS;

  static {
    BUILDER.comment("List of all curio slots that should support Apotheosis affixes");
    SLOTS = BUILDER.defineList("Curio Slots", List.of(), s -> true);
    SPEC = BUILDER.build();
  }

  @SubscribeEvent
  static void load(ModConfigEvent event) {
    if (event.getConfig().getSpec() != SPEC) return;
    SLOTS.get().forEach(ApothicCuriosMod::registerCurioLootCategory);
  }
}
