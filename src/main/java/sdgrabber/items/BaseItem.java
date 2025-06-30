package sdgrabber.items;


import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sdgrabber.SuperDuperGrabber;
import sdgrabber.materials.GrabberMaterial;

public class BaseItem {
    public static final Item WOODEN_GRABBER = register(
            new Grabber(
                    GrabberMaterial.WOODEN,
                    new FabricItemSettings(),
                    10,
                    1
            ),
            "wooden_grabber"
    );
    public static final Item IRON_GRABBER = register(
            new Grabber(
                    GrabberMaterial.IRON,
                    new FabricItemSettings(),
                    5,
                    1
            ),
            "iron_grabber"
    );
    public static final Item DIAMOND_GRABBER = register(
            new Grabber(
                    GrabberMaterial.DIAMOND,
                    new FabricItemSettings(),
                    5,
                    1
            ),
            "diamond_grabber"
    );
    public static final Item DOUBLE_GRABBER = register(
            new Grabber(
                    GrabberMaterial.DOUBLE,
                    new FabricItemSettings(),
                    1,
                    2
            ),
            "double_grabber"
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(BaseItem::addToolsToTab);
        registerModelPredicates();
    }

    private static void addToolsToTab(FabricItemGroupEntries entries) {
        entries.add(WOODEN_GRABBER);
        entries.add(IRON_GRABBER);
        entries.add(DIAMOND_GRABBER);
        entries.add(DOUBLE_GRABBER);
    }

    public static Item register(Item item, String id) {
        Item registeredItem = Registry.register(Registries.ITEM, new Identifier(SuperDuperGrabber.MOD_ID, id), item);
        SuperDuperGrabber.LOGGER.info("(Item) sdgrabber:{} registered.", id);
        return registeredItem;
    }

    private static void registerModelPredicates() {
        Identifier stored_any_identifier = new Identifier(SuperDuperGrabber.MOD_ID, "stored_any");
        Identifier chest_count_identifier = new Identifier(SuperDuperGrabber.MOD_ID, "chests_count");
        String storedAny = "StoredAny";
        String chestsIntegerCountKey = "ChestsCount";

        ModelPredicateProviderRegistry.register(
                WOODEN_GRABBER,
                stored_any_identifier,
                (stack, world, entity, seed) -> stack.getOrCreateNbt().getBoolean(storedAny) ? 1.0f : 0.0f
        );

        ModelPredicateProviderRegistry.register(
                IRON_GRABBER,
                stored_any_identifier,
                (stack, world, entity, seed) -> stack.getOrCreateNbt().getBoolean(storedAny) ? 1.0f : 0.0f
        );

        ModelPredicateProviderRegistry.register(
                DIAMOND_GRABBER,
                stored_any_identifier,
                (stack, world, entity, seed) -> stack.getOrCreateNbt().getBoolean(storedAny) ? 1.0f : 0.0f
        );

        ModelPredicateProviderRegistry.register(
                BaseItem.DOUBLE_GRABBER,
                chest_count_identifier,
                (stack, world, entity, seed) -> stack.getOrCreateNbt().getFloat(chestsIntegerCountKey)
        );
    }
}
