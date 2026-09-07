package fr.smartfolk.client;

import fr.smartfolk.SmartFolkMod;
import fr.smartfolk.entity.SmartFolkEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.util.Identifier;

/**
 * Cote rendu, un Smart Folk reutilise le modele "robe" du villageois
 * vanilla (silhouette humanoide simple, deja disponible dans le jeu),
 * mis a l'echelle pour rester petit. Sa couleur vient du plastron en
 * cuir teinte applique dans SmartFolkEntity#applyColorGear, qui se
 * dessine par-dessus grace au rendu d'equipement standard.
 *
 * Si tu veux plus tard un modele 100% custom (silhouette differente
 * d'un villageois), le plus simple est de le creer avec Blockbench,
 * de l'exporter en "Bedrock" ou "Java Entity" et de suivre un tutoriel
 * Fabric "custom entity model" pour 1.20.1 : la structure ci-dessous
 * (renderer + texture) reste la meme, seul le modele change.
 */
public class SmartFolkModClient implements ClientModInitializer {

    private static final Identifier TEXTURE =
            new Identifier("minecraft", "textures/entity/villager/villager.png");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(SmartFolkMod.SMART_FOLK, context ->
                new MobEntityRenderer<>(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.VILLAGER)), 0.3F) {
                    @Override
                    public Identifier getTexture(SmartFolkEntity entity) {
                        return TEXTURE;
                    }
                }
        );
    }
}
