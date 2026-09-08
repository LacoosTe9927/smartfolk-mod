package fr.smartfolk.entity.ai;

import fr.smartfolk.entity.SmartFolkEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

/**
 * Quand deux Smart Folk se croisent, ils "discutent" : leur affinite
 * evolue petit a petit. S'ils s'entendent bien, ils se suivent et
 * gagnent de l'experience sociale ensemble. S'ils ne s'entendent pas
 * du tout, cette IA laisse la main a RivalryGoal (voir seuils dans
 * SmartFolkEntity : FRIEND_THRESHOLD / RIVAL_THRESHOLD).
 */
public class SocializeGoal extends Goal {

    private static final double RANGE = 6.0D;
    private static final int MAX_DURATION_TICKS = 200; // ~10 secondes max par rencontre

    private final SmartFolkEntity folk;
    private SmartFolkEntity companion;
    private int interactionCooldown;
    private int elapsedTicks;
    private int restCooldown;

    public SocializeGoal(SmartFolkEntity folk) {
        this.folk = folk;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (restCooldown > 0) {
            restCooldown--;
            return false;
        }
        if (folk.getTarget() != null) {
            return false; // priorite au combat s'il y en a un
        }
        List<SmartFolkEntity> nearby = folk.getWorld().getEntitiesByClass(
                SmartFolkEntity.class,
                new Box(folk.getBlockPos()).expand(RANGE),
                other -> other != folk && !folk.isRival(other)
        );
        if (nearby.isEmpty()) {
            return false;
        }
        companion = nearby.get(folk.getRandom().nextInt(nearby.size()));
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return elapsedTicks < MAX_DURATION_TICKS
                && companion != null && companion.isAlive()
                && folk.squaredDistanceTo(companion) < (RANGE * 2) * (RANGE * 2)
                && !folk.isRival(companion);
    }

    @Override
    public void start() {
        interactionCooldown = 0;
        elapsedTicks = 0;
        // Premiere rencontre : applique le petit bonus "meme couleur".
        if (folk.getAffinity(companion.getUuid()) == 0) {
            int bonus = folk.initialAffinityBonus(companion);
            if (bonus != 0) {
                folk.adjustAffinity(companion.getUuid(), bonus);
                companion.adjustAffinity(folk.getUuid(), bonus);
            }
        }
    }

    @Override
    public void tick() {
        elapsedTicks++;
        folk.getLookControl().lookAt(companion);
        if (folk.squaredDistanceTo(companion) > 9.0D) {
            folk.getNavigation().startMovingTo(companion, 0.5D);
        }

        interactionCooldown--;
        if (interactionCooldown > 0) {
            return;
        }
        interactionCooldown = 60 + folk.getRandom().nextInt(40);

        // L'interaction fait evoluer l'affinite dans un sens aleatoire,
        // avec une tendance positive (la plupart des rencontres se
        // passent bien), et fait progresser la competence sociale.
        int delta = folk.getRandom().nextInt(9) - 3; // -3 a +5
        folk.adjustAffinity(companion.getUuid(), delta);
        companion.adjustAffinity(folk.getUuid(), delta);
        folk.gainSocialSkill(1);
    }

    @Override
    public void stop() {
        companion = null;
        // Periode de repos avant de pouvoir re-socialiser, pour laisser
        // la place a la construction et a l'exploration.
        restCooldown = 100 + folk.getRandom().nextInt(200);
    }
}
