package fr.smartfolk;

import fr.smartfolk.entity.SmartFolkEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.objet.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SmartFolkMod implements ModInitializer {

    public static final String MOD_ID = "smartfolk";

    public static EntityType<SmartFolkEntity> SMART_FOLK;
    public static Item SMART_FOLK_SPAWN_EGG;

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // --- Enregistrement du type d'entite ---
        // Petite taille : environ la moitie d'un joueur, comme demande
        // ("tout ca en petit").
        SMART_FOLK = Registry.register(
                Registries.ENTITY_TYPE,
                id("smart_folk"),
                EntityType.Builder.<SmartFolkEntity>create(SmartFolkEntity::new, SpawnGroup.CREATURE)
                        .dimensions(0.4F, 0.9F)
                        .trackRangeBlocks(10)
                        .build("smart_folk")
        );

        // --- Attributs par defaut (obligatoire pour toute entite vivante) ---
        FabricDefaultAttributeRegistry.register(SMART_FOLK, SmartFolkEntity.createSmartFolkAttributes());

        // --- Oeuf d'apparition (pratique pour tester le mod en creatif) ---
        // Utilise la commande /summon smartfolk:smart_folk si tu preferes,
        // ou attrape l'oeuf dans l'onglet creatif "Outils et utilitaires".
        SMART_FOLK_SPAWN_EGG = Registry.register(
                Registries.ITEM,
                id("smart_folk_spawn_egg"),
                new SpawnEggItem(SMART_FOLK, 0x5E7C16, 0xFED83D, new Item.Settings())
        );

        // Ajoute l'oeuf dans l'onglet creatif "Outils et utilitaires".
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(SMART_FOLK_SPAWN_EGG));

        // Remarque : l'apparition naturelle dans le monde n'est pas
        // configuree ici pour rester simple et fiable. Utilise l'oeuf
        // d'apparition ou /summon pour l'instant. Voir le README pour
        // ajouter une apparition naturelle si tu le souhaites.
    }
}
