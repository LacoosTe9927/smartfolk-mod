package fr.smartfolk.entity.ai;

import fr.smartfolk.entity.SmartFolkEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

/**
 * Si l'affinite envers un autre Smart Folk est trop basse (voir
 * SmartFolkEntity#RIVAL_THRESHOLD), les deux personnages s'affrontent
 * au lieu de socialiser. Chaque coup degrade encore un peu plus leur
 * relation.
 */
public class RivalryGoal extends Goal {

    private static final double DETECT_RANGE = 10.0D;
    private static final double ATTACK_RANGE_SQ = 4.0D;

    private final SmartFolkEntity folk;
    private SmartFolkEntity rival;
    private int attackCooldown;

    public RivalryGoal(SmartFolkEntity folk) {
        this.folk = folk;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() {
        List<SmartFolkEntity> nearby = folk.getWorld().getEntitiesByClass(
                SmartFolkEntity.class,
                new Box(folk.getBlockPos()).expand(DETECT_RANGE),
                other -> other != folk && other.isAlive() && folk.isRival(other)
        );
        if (nearby.isEmpty()) {
            return false;
        }
        rival = folk.getRandom().nextInt(4) == 0 ? null : nearby.get(0); // laisse une petite chance d'ignorer
        return rival != null;
    }

    @Override
    public boolean shouldContinue() {
        return rival != null && rival.isAlive() && folk.isRival(rival)
                && folk.squaredDistanceTo(rival) < DETECT_RANGE * DETECT_RANGE * 2;
    }

    @Override
    public void start() {
        folk.setTarget(rival);
    }

    @Override
    public void tick() {
        folk.getLookControl().lookAt(rival);
        double distSq = folk.squaredDistanceTo(rival);

        if (distSq > ATTACK_RANGE_SQ) {
            folk.getNavigation().startMovingTo(rival, 0.9D);
            return;
        }

        folk.getNavigation().stop();
        attackCooldown--;
        if (attackCooldown <= 0) {
            folk.tryAttack(rival);
            attackCooldown = 20; // 1 seconde entre deux coups

            // Chaque affrontement degrade davantage la relation, des
            // deux cotes.
            folk.adjustAffinity(rival.getUuid(), -5);
            rival.adjustAffinity(folk.getUuid(), -5);
        }
    }

    @Override
    public void stop() {
        folk.setTarget(null);
        rival = null;
    }
}
