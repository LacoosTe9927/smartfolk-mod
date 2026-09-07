package fr.smartfolk.entity.ai;

import fr.smartfolk.entity.SmartFolkEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Le personnage part de temps en temps construire une petite structure
 * (une simple tour) a proximite. Plus son "buildSkill" est eleve, plus
 * il construit vite et plus haut. Chaque bloc pose fait gagner de
 * l'experience et fait evoluer le personnage.
 */
public class BuildGoal extends Goal {

    private final SmartFolkEntity folk;
    private BlockPos siteOrigin;
    private int blocksPlaced;
    private int cooldown;
    private int ticksSinceLastBlock;

    // Materiau "de base" utilise pour construire (facile a remplacer par
    // une liste variee si tu veux plus de diversite visuelle).
    private static final BlockState BUILD_MATERIAL = Blocks.COBBLESTONE.getDefaultState();

    public BuildGoal(SmartFolkEntity folk) {
        this.folk = folk;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        // Ne construit pas s'il est en train de fuir ou de se battre.
        return folk.getTarget() == null && folk.getRandom().nextInt(200) == 0;
    }

    @Override
    public void start() {
        siteOrigin = folk.getBlockPos();
        blocksPlaced = 0;
        ticksSinceLastBlock = 0;
    }

    @Override
    public boolean shouldContinue() {
        int maxHeight = 2 + Math.min(folk.getBuildSkill() / 10, 8); // plus fort = plus haut
        return blocksPlaced < maxHeight
                && folk.squaredDistanceTo(siteOrigin.getX() + 0.5, folk.getY(), siteOrigin.getZ() + 0.5) < 36.0D;
    }

    @Override
    public void tick() {
        folk.getNavigation().startMovingTo(siteOrigin.getX() + 0.5, siteOrigin.getY(), siteOrigin.getZ() + 0.5, 0.6D);
        folk.getLookControl().lookAt(siteOrigin.getX() + 0.5, siteOrigin.getY() + blocksPlaced, siteOrigin.getZ() + 0.5);

        ticksSinceLastBlock++;
        int placeDelay = Math.max(10, 40 - folk.getBuildSkill() / 3); // plus fort = plus rapide
        if (ticksSinceLastBlock < placeDelay) {
            return;
        }
        ticksSinceLastBlock = 0;

        World world = folk.getWorld();
        BlockPos target = siteOrigin.up(blocksPlaced + 1);
        if (world.getBlockState(target).isAir()) {
            world.setBlockState(target, BUILD_MATERIAL);
            blocksPlaced++;
            folk.gainBuildSkill(2);
        } else {
            // Emplacement bloque : on abandonne cette session de construction.
            blocksPlaced = 999;
        }
    }

    @Override
    public void stop() {
        cooldown = 300 + folk.getRandom().nextInt(300);
    }
}
