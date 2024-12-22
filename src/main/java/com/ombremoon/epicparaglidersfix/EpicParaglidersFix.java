package com.ombremoon.epicparaglidersfix;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class EpicParaglidersFix {
    private static boolean FILTER_ANIMS;

    public EpicParaglidersFix() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public static void playerTick(final TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        var playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        Movement movement = Movement.get(event.player);
        if (movement.state().has(ParagliderPlayerStates.Flags.FLAG_PARAGLIDING) || player.isFallFlying()) {
            player.getTags().add("WasGliding");

            if (player.level().isClientSide && !FILTER_ANIMS)
                EpicFightMod.CLIENT_CONFIGS.filterAnimation.setValue(true);

            if (playerPatch.isBattleMode() && !player.level().isClientSide) {
                player.getTags().add("BattleMode");
                playerPatch.toMiningMode(true);
            }

        } else {
            if (player.level().isClientSide)
                EpicFightMod.CLIENT_CONFIGS.filterAnimation.setValue(FILTER_ANIMS);
        }

        if ((player.onGround() || player.isSwimming()) && player.getTags().contains("WasGliding") && player.getTags().contains("BattleMode") && !player.level().isClientSide) {
            playerPatch.toBattleMode(true);
            player.getTags().remove("BattleMode");
        }
    }

    @SubscribeEvent
    public static void joinLevel(final EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player && event.getLevel().isClientSide)
            FILTER_ANIMS = EpicFightMod.CLIENT_CONFIGS.filterAnimation.getValue();
    }
}
