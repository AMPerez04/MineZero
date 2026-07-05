package boomcow.minezero;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import boomcow.minezero.checkpoint.PlayerData;
import boomcow.minezero.command.RestoreCheckpointCommand;
import boomcow.minezero.command.SetAnchorCommand;
import boomcow.minezero.command.SetCheckpointCommand;
import boomcow.minezero.event.*;
import boomcow.minezero.input.KeyBindings;
import boomcow.minezero.items.ArtifactFluteItem;
import boomcow.minezero.network.PacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.Locale;


@Mod(MineZero.MODID)
public class MineZero {

    public static final String MODID = "minezero";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredHolder<Block, Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F)));

    public static final DeferredHolder<Item, BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
            () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, ArtifactFluteItem> ARTIFACT_FLUTE = ITEMS.register("artifact_flute",
            () -> new ArtifactFluteItem(new Item.Properties().stacksTo(1)));

    public MineZero(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ConfigHandler::onLoad);
        modEventBus.addListener(ConfigHandler::onReload);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ModSoundEvents.SOUND_EVENTS.register(modEventBus);
        PacketHandler.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new DeathEventHandler());

        NeoForge.EVENT_BUS.register(BlockChangeListener.class);
        NeoForge.EVENT_BUS.register(CheckpointTicker.class);
        NeoForge.EVENT_BUS.register(ExplosionEventHandler.class);
        NeoForge.EVENT_BUS.register(GlobalTickHandler.class);
        NeoForge.EVENT_BUS.register(LightningStrikeListener.class);
        NeoForge.EVENT_BUS.register(NonPlayerChangeHandler.class);





        if (modContainer != null) {
            modContainer.registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG_SPEC);
        } else {
            LOGGER.warn("ModContainer was not injected, attempting to get active container for config registration. This might be unreliable.");
            ModContainer fallbackContainer = ModLoadingContext.get().getActiveContainer();
            LOGGER.error("CRITICAL: ModContainer not available for config registration. Configs will not be loaded for {}.", MODID);
        }
    }
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SetCheckpointCommand.register(event.getDispatcher());
        SetAnchorCommand.register(event.getDispatcher());
        RestoreCheckpointCommand.register(event.getDispatcher());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info("Loading custom game rules: autoCheckpointEnabled = {}", ModGameRules.AUTO_CHECKPOINT_ENABLED);
        event.enqueueWork(() -> {
            LOGGER.info("This is a task running on the main thread after common setup dispatch (from commonSetup).");
        });
    }
    public void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ARTIFACT_FLUTE.get());
        }
    }


    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ServerLevel level = player.serverLevel();
        CheckpointData data = CheckpointData.get(level);
        HolderLookup.Provider lookupProvider = level.registryAccess();

        LOGGER.info("[MineZero][LOGIN] Player {} (UUID: {}) logged in.", player.getName().getString(), player.getUUID());

        if (player.level().getGameRules().getBoolean(ModGameRules.CHECKPOINT_ON_WORLD_CREATION) &&
                data.getAnchorPlayerUUID() == null &&
                data.getPlayerData(player.getUUID(), lookupProvider) == null) {

            CheckpointManager.setCheckpoint(player);

        } else if (data.getPlayerData(player.getUUID(), lookupProvider) == null) {
            LOGGER.info("[MineZero][LOGIN] Player {} (UUID: {}) not found in current checkpoint data. Adding them now.", player.getName().getString(), player.getUUID());
            PlayerData pDataForNewPlayer = new PlayerData();
            pDataForNewPlayer.posX = player.getX();
            pDataForNewPlayer.posY = player.getY();
            pDataForNewPlayer.posZ = player.getZ();
            pDataForNewPlayer.yaw = player.getYRot();
            pDataForNewPlayer.pitch = player.getXRot();
            pDataForNewPlayer.dimension = player.level().dimension();
            pDataForNewPlayer.gameMode = player.gameMode.getGameModeForPlayer().getName().toLowerCase(Locale.ROOT);
            pDataForNewPlayer.motionX = player.getDeltaMovement().x;
            pDataForNewPlayer.motionY = player.getDeltaMovement().y;
            pDataForNewPlayer.motionZ = player.getDeltaMovement().z;
            pDataForNewPlayer.fallDistance = player.fallDistance;
            pDataForNewPlayer.health = player.getHealth();
            pDataForNewPlayer.hunger = player.getFoodData().getFoodLevel();
            pDataForNewPlayer.experienceLevel = player.experienceLevel;
            pDataForNewPlayer.experienceProgress = player.experienceProgress;
            pDataForNewPlayer.fireTicks = player.getRemainingFireTicks();

            BlockPos spawn = player.getRespawnPosition();
            ResourceKey<Level> spawnDim = player.getRespawnDimension();
            if (spawn != null && spawnDim != null) {
                pDataForNewPlayer.spawnX = spawn.getX() + 0.5;
                pDataForNewPlayer.spawnY = spawn.getY();
                pDataForNewPlayer.spawnZ = spawn.getZ() + 0.5;
                pDataForNewPlayer.spawnDimension = spawnDim;
                pDataForNewPlayer.spawnForced = player.isRespawnForced();
            }

            pDataForNewPlayer.potionEffects.clear();
            for (MobEffectInstance effectInstance : player.getActiveEffects()) { 
                pDataForNewPlayer.potionEffects.add(new MobEffectInstance(effectInstance));
            }

            Inventory playerInventory = player.getInventory();
            ListTag inventoryTag = new ListTag();

            for (int i = 0; i < playerInventory.items.size(); ++i) {
                if (!playerInventory.items.get(i).isEmpty()) {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putByte("Slot", (byte) i);
                    playerInventory.items.get(i).save(player.registryAccess(), compoundtag);
                    inventoryTag.add(compoundtag);
                }
            }


            for (int j = 0; j < playerInventory.armor.size(); ++j) {
                if (!playerInventory.armor.get(j).isEmpty()) {
                    CompoundTag compoundtag1 = new CompoundTag();
                    compoundtag1.putByte("Slot", (byte) (j + 100));
                    playerInventory.armor.get(j).save(player.registryAccess(), compoundtag1);
                    inventoryTag.add(compoundtag1);
                }
            }


            for (int k = 0; k < playerInventory.offhand.size(); ++k) {
                if (!playerInventory.offhand.get(k).isEmpty()) {
                    CompoundTag compoundtag2 = new CompoundTag();
                    compoundtag2.putByte("Slot", (byte) (k + 150));
                    playerInventory.offhand.get(k).save(player.registryAccess(), compoundtag2);
                    inventoryTag.add(compoundtag2);
                }
            }


            pDataForNewPlayer.inventoryNBT = inventoryTag;

            CompoundTag advTag = new CompoundTag();
            if (player.server != null) {
                for (AdvancementHolder advancementHolder : player.server.getAdvancements().getAllAdvancements()) {
                    AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancementHolder);
                    CompoundTag progressTag = new CompoundTag();
                    for (String criterion : progress.getCompletedCriteria()) {
                        progressTag.putBoolean(criterion, true);
                    }
                    advTag.put(advancementHolder.id().toString(), progressTag);
                }
            }
            pDataForNewPlayer.advancements = advTag;

            CompoundTag playerDataNbt = pDataForNewPlayer.toNBT(lookupProvider);
            data.savePlayerData(player.getUUID(), playerDataNbt);
            LOGGER.info("[MineZero][LOGIN] Player {} added to checkpoint.", player.getName().getString());
        } else {
            LOGGER.info("[MineZero][LOGIN] Player {} (UUID: {}) already has data in checkpoint.", player.getName().getString(), player.getUUID());
            LOGGER.info("[MineZero][LOGIN] Player UUID = {}, Stored Anchor = {}",
                    player.getUUID(), data.getAnchorPlayerUUID());
        }
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }


    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("HELLO FROM CLIENT SETUP (Mod Event Bus)");


        NeoForge.EVENT_BUS.register(ClientForgeEvents.class);

        event.enqueueWork(() -> {
            LOGGER.info("Performing client-side enqueued work (from clientSetup).");
        });
    }
    private void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        LOGGER.info("Registering MineZero Key Mappings (Mod Event Bus)");
        if (KeyBindings.EXAMPLE_ACTION_KEY != null) {
            event.register(KeyBindings.EXAMPLE_ACTION_KEY.get());
        }
        if (KeyBindings.SELF_DAMAGE_KEY != null) {
            event.register(KeyBindings.SELF_DAMAGE_KEY.get());
        }
    }
}