package boomcow.minezero;

import boomcow.minezero.command.SetCheckpointCommand;
import boomcow.minezero.command.SetAnchorCommand;
import boomcow.minezero.command.RestoreCheckpointCommand;
import boomcow.minezero.ConfigHandler;
import boomcow.minezero.event.*;
import boomcow.minezero.items.ArtifactFluteItem;
import boomcow.minezero.util.LightningScheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MineZeroMain implements ModInitializer {

    public static final String MOD_ID = "minezero";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Block EXAMPLE_BLOCK = new Block(AbstractBlock.Settings.create().strength(1.5F, 6.0F));
    public static final BlockItem EXAMPLE_BLOCK_ITEM = new BlockItem(EXAMPLE_BLOCK, new Item.Settings());
    public static final ArtifactFluteItem ARTIFACT_FLUTE = new ArtifactFluteItem(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing MineZero for Fabric with Yarn Mappings!");
        ConfigHandler.register();
        ModGameRules.initialize();

        registerBlocksAndItems();
        ModSoundEvents.registerSoundEvents();
        registerServerTickEvents();
        BlockChangeListener.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SetCheckpointCommand.register(dispatcher);
            SetAnchorCommand.register(dispatcher);
            RestoreCheckpointCommand.register(dispatcher);
        });

        registerServerEventHandlers();
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(this::addBuildingBlocksToCreativeTab);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS_AND_UTILITIES).register(this::addToolsToCreativeTab);

        LOGGER.info("DIRT BLOCK KEY >> {}", Registries.BLOCK.getId(Blocks.DIRT));
        LOGGER.info("Common setup tasks from onInitialize() completed.");
    }

    private static void registerBlocksAndItems() {
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "example_block"), EXAMPLE_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "example_block"), EXAMPLE_BLOCK_ITEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "artifact_flute"), ARTIFACT_FLUTE);

        LOGGER.info("MineZero blocks and items registered with Yarn mappings.");
    }

    private void addBuildingBlocksToCreativeTab(FabricItemGroupEntries entries) {
        entries.add(EXAMPLE_BLOCK_ITEM);
    }

    private void addToolsToCreativeTab(FabricItemGroupEntries entries) {
        entries.add(ARTIFACT_FLUTE);
    }

    private void registerServerEventHandlers() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            onPlayerLogin(player);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("MineZero: Server starting...");
        });
        BlockChangeListener.register();
        CheckpointTicker.register();
        ExplosionEventHandler.register();
        LightningStrikeListener.register();
        NonPlayerChangeHandler.register();
        DeathEventHandler.register();
    }
    public void onPlayerLogin(ServerPlayerEntity player) {
        if (player.getWorld().isClient()) return;

        ServerWorld world = (ServerWorld) player.getWorld();

        LOGGER.info("Player {} logged in. MineZero login logic to be fully ported.", player.getName().getString());
    }

    private void registerServerTickEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                LightningScheduler.tick(world);
            }
        });
    }
}