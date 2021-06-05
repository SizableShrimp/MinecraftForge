package net.minecraftforge.debug.block;

import net.minecraft.world.level.material.Material;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

@Mod(CustomSignsTest.MODID)
public class CustomSignsTest
{
    public static final boolean ENABLE = true;
    public static final String MODID = "custom_signs_test";

    public static final WoodType TEST_WOOD_TYPE = WoodType.create(new ResourceLocation(MODID, "test").toString());

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<CustomStandingSignBlock> TEST_STANDING_SIGN = BLOCKS.register("test_sign", () -> new CustomStandingSignBlock(Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD), CustomSignsTest.TEST_WOOD_TYPE));
    public static final RegistryObject<CustomWallSignBlock> TEST_WALL_SIGN = BLOCKS.register("test_wall_sign", () -> new CustomWallSignBlock(Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD), CustomSignsTest.TEST_WOOD_TYPE));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<SignItem> TEST_SIGN = ITEMS.register("test_sign", () -> new SignItem((new Item.Properties()).stacksTo(16).tab(CreativeModeTab.TAB_DECORATIONS), TEST_STANDING_SIGN.get(), TEST_WALL_SIGN.get()));

    private static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    public static final RegistryObject<BlockEntityType<CustomSignTileEntity>> CUSTOM_SIGN = TILE_ENTITIES.register("custom_sign", () -> BlockEntityType.Builder.of(CustomSignTileEntity::new, TEST_WALL_SIGN.get(), TEST_STANDING_SIGN.get()).build(null));

    public CustomSignsTest()
    {
        if (ENABLE)
        {
            final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
            BLOCKS.register(eventBus);
            ITEMS.register(eventBus);
            TILE_ENTITIES.register(eventBus);

            eventBus.addListener(this::clientSetup);
            eventBus.addListener(this::commonSetup);
        }
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(CUSTOM_SIGN.get(), SignRenderer::new);
        event.enqueueWork(() -> {
           Sheets.addWoodType(TEST_WOOD_TYPE);
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> WoodType.register(TEST_WOOD_TYPE));
    }

    public static class CustomStandingSignBlock extends StandingSignBlock
    {

        public CustomStandingSignBlock(Properties propertiesIn, WoodType woodTypeIn)
        {
            super(propertiesIn, woodTypeIn);
        }

        @Override
        public boolean hasTileEntity(BlockState stateIn)
        {
            return true;
        }

        @Override
        public BlockEntity newBlockEntity(BlockGetter worldIn)
        {
            return new CustomSignTileEntity();
        }
    }

    public static class CustomWallSignBlock extends WallSignBlock
    {

        public CustomWallSignBlock(Properties propertiesIn, WoodType woodTypeIn)
        {
            super(propertiesIn, woodTypeIn);
        }

        @Override
        public boolean hasTileEntity(BlockState stateIn)
        {
            return true;
        }

        @Override
        public BlockEntity newBlockEntity(BlockGetter worldIn)
        {
            return new CustomSignTileEntity();
        }
    }

    public static class CustomSignTileEntity extends SignBlockEntity
    {
        @Override
        public BlockEntityType<CustomSignTileEntity> getType()
        {
            return CUSTOM_SIGN.get();
        }
    }
}
