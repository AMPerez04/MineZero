package boomcow.minezero;

import boomcow.minezero.command.RestoreCheckpointCommand;
import boomcow.minezero.command.SetAnchorCommand;
import boomcow.minezero.command.SetCheckpointCommand;
import boomcow.minezero.event.BlockChangeListener;
import boomcow.minezero.event.CheckpointTicker;
import boomcow.minezero.event.DeathEventHandler;
import boomcow.minezero.event.ExplosionEventHandler;
import boomcow.minezero.event.LightningStrikeListener;
import boomcow.minezero.event.NonPlayerChangeHandler;
import boomcow.minezero.items.ArtifactFluteItem;
import boomcow.minezero.network.PacketHandler;
import boomcow.minezero.util.LightningScheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineZeroMain implements ModInitializer {

    public static final String MOD_ID = "minezero";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Block EXAMPLE_BLOCK = new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F));
    public static final BlockItem EXAMPLE_BLOCK_ITEM = new BlockItem(EXAMPLE_BLOCK, new Item.Properties());
    public static final ArtifactFluteItem ARTIFACT_FLUTE = new ArtifactFluteItem(new Item.Properties().stacksTo(1));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing MineZero for Fabric!");
        ConfigHandler.register();
        ModGameRules.initialize();

        registerBlocksAndItems();
        ModSoundEvents.registerSoundEvents();
        PacketHandler.register();
        registerServerTickEvents();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SetCheckpointCommand.register(dispatcher);
            SetAnchorCommand.register(dispatcher);
            RestoreCheckpointCommand.register(dispatcher);
        });

        registerServerEventHandlers();
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(this::addBuildingBlocksToCreativeTab);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(this::addToolsToCreativeTab);

        LOGGER.info("DIRT BLOCK KEY >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        LOGGER.info("Common setup tasks from onInitialize() completed.");
    }

    private static void registerBlocksAndItems() {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "example_block"), EXAMPLE_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "example_block"), EXAMPLE_BLOCK_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "artifact_flute"), ARTIFACT_FLUTE);

        LOGGER.info("MineZero blocks and items registered.");
    }

    private void addBuildingBlocksToCreativeTab(FabricItemGroupEntries entries) {
        entries.accept(EXAMPLE_BLOCK_ITEM);
    }

    private void addToolsToCreativeTab(FabricItemGroupEntries entries) {
        entries.accept(ARTIFACT_FLUTE);
    }

    private void registerServerEventHandlers() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            onPlayerLogin(handler.player);
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

    public void onPlayerLogin(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        LOGGER.info("Player {} logged in to {}. MineZero login checkpoint logic to be fully ported.",
                player.getName().getString(), level.dimension().location());
    }

    private void registerServerTickEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                LightningScheduler.tick(level);
            }
        });
    }
}
