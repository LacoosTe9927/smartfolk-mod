package fr.smartfolk.client;

import fr.smartfolk.SmartFolkMod;
import fr.smartfolk.entity.SmartFolkEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/**
 * Rendu du Smart Folk : un modele humanoide generique (le meme squelette
 * que le joueur, PAS celui du villageois) associe a une texture propre
 * au mod (visage simple, teinte de peau neutre). La couleur du
 * personnage vient ensuite d'un jeu complet d'armure en cuir teintee,
 * appliquee dans SmartFolkEntity#applyColorGear, qui recouvre la
 * majeure partie du corps.
 *
 * Si tu veux un jour une silhouette 100% originale (pas juste un
 * joueur repeint), l'etape suivante est de modeliser une forme custom
 * dans Blockbench et de l'exporter en "Java Entity Model" pour Fabric.
 */
public class SmartFolkModClient implements ClientModInitializer {

    private static final Identifier TEXTURE =
            new Identifier(SmartFolkMod.MOD_ID, "textures/entity/smart_folk.png");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(SmartFolkMod.SMART_FOLK, context ->
                new MobEntityRenderer<>(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.3F) {
                    @Override
                    public Identifier getTexture(SmartFolkEntity entity) {
                        return TEXTURE;
                    }
                }
        );
    }
}
