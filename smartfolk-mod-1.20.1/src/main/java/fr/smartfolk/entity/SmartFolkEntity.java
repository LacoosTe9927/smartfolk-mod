package fr.smartfolk.entity;

import fr.smartfolk.entity.ai.BuildGoal;
import fr.smartfolk.entity.ai.RivalryGoal;
import fr.smartfolk.entity.ai.SocializeGoal;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Un "Smart Folk" : petit personnage independant qui explore, construit,
 * s'ameliore avec le temps, et noue des relations (amitie ou rivalite)
 * avec les autres membres de son espece.
 */
public class SmartFolkEntity extends PathAwareEntity {

    // --- Donnees synchronisees client/serveur ---
    private static final TrackedData<Integer> COLOR_ID =
            DataTracker.registerData(SmartFolkEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> LEVEL =
            DataTracker.registerData(SmartFolkEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // --- Progression / evolution ---
    private int experience = 0;
    private int buildSkill = 0;      // ameliore la vitesse et la hauteur de construction
    private int socialSkill = 0;     // ameliore le gain d'affinite

    // --- Relations avec les autres Smart Folk (uuid -> affinite de -100 a 100) ---
    private final Map<UUID, Integer> relationships = new HashMap<>();

    public static final int AFFINITY_MIN = -100;
    public static final int AFFINITY_MAX = 100;
    public static final int FRIEND_THRESHOLD = 30;
    public static final int RIVAL_THRESHOLD = -30;

    public SmartFolkEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    // ------------------------------------------------------------------
    // Attributs de base (sante, vitesse, degats). Un niveau plus eleve
    // augmente legerement ces valeurs via applyLevelBonuses().
    // ------------------------------------------------------------------
    public static DefaultAttributeContainer.Builder createSmartFolkAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(COLOR_ID, 0);
        this.dataTracker.startTracking(LEVEL, 1);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty,
                                    SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt) {
        // Attribution d'une couleur aleatoire a la naissance / au spawn.
        setColorVariant(ColorVariant.random(this.random));
        applyColorGear();
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    // ------------------------------------------------------------------
    // Couleur
    // ------------------------------------------------------------------
    public ColorVariant getColorVariant() {
        return ColorVariant.byId(this.dataTracker.get(COLOR_ID));
    }

    public void setColorVariant(ColorVariant variant) {
        this.dataTracker.set(COLOR_ID, variant.ordinal());
    }

    /** Teinte un jeu d'armure en cuir complet pour rendre la couleur bien visible en jeu. */
    private void applyColorGear() {
        int rgb = getColorVariant().getDyeRgb();

        ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
        ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);

        for (ItemStack piece : new ItemStack[]{helmet, chest, legs, boots}) {
            NbtCompound display = piece.getOrCreateSubNbt("display");
            display.putInt("color", rgb);
        }

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chest);
        this.equipStack(EquipmentSlot.LEGS, legs);
        this.equipStack(EquipmentSlot.FEET, boots);

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            this.setEquipmentDropChance(slot, 0.0F);
        }
    }

    // ------------------------------------------------------------------
    // Evolution / niveaux
    // ------------------------------------------------------------------
    public int getLevel() {
        return this.dataTracker.get(LEVEL);
    }

    private void setLevel(int level) {
        this.dataTracker.set(LEVEL, level);
        applyLevelBonuses();
    }

    public int getBuildSkill() {
        return buildSkill;
    }

    public int getSocialSkill() {
        return socialSkill;
    }

    /** A appeler quand l'entite termine une action utile (bloc pose, lien social...). */
    public void gainExperience(int amount) {
        experience += amount;
        int neededForNextLevel = 20 + (getLevel() * 15);
        if (experience >= neededForNextLevel) {
            experience -= neededForNextLevel;
            setLevel(getLevel() + 1);
        }
    }

    public void gainBuildSkill(int amount) {
        buildSkill = Math.min(buildSkill + amount, 100);
        gainExperience(amount);
    }

    public void gainSocialSkill(int amount) {
        socialSkill = Math.min(socialSkill + amount, 100);
        gainExperience(amount);
    }

    /** Les niveaux plus eleves rendent le personnage un peu plus robuste et rapide. */
    private void applyLevelBonuses() {
        int level = getLevel();
        var healthAttr = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        var speedAttr = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (healthAttr != null) {
            healthAttr.setBaseValue(12.0D + (level - 1) * 1.5D);
        }
        if (speedAttr != null) {
            speedAttr.setBaseValue(0.28D + (level - 1) * 0.01D);
        }
        this.setHealth(this.getMaxHealth());
    }

    // ------------------------------------------------------------------
    // Relations : amitie ou rivalite
    // ------------------------------------------------------------------
    public int getAffinity(UUID otherUuid) {
        return relationships.getOrDefault(otherUuid, 0);
    }

    public void adjustAffinity(UUID otherUuid, int delta) {
        int current = getAffinity(otherUuid);
        int updated = Math.max(AFFINITY_MIN, Math.min(AFFINITY_MAX, current + delta));
        relationships.put(otherUuid, updated);
    }

    public boolean isFriend(SmartFolkEntity other) {
        return getAffinity(other.getUuid()) >= FRIEND_THRESHOLD;
    }

    public boolean isRival(SmartFolkEntity other) {
        return getAffinity(other.getUuid()) <= RIVAL_THRESHOLD;
    }

    /** Deux personnages de la meme couleur partent avec un a priori legerement positif. */
    public int initialAffinityBonus(SmartFolkEntity other) {
        return this.getColorVariant() == other.getColorVariant() ? 10 : 0;
    }

    // ------------------------------------------------------------------
    // IA : deplacement, construction, socialisation, rivalite
    // ------------------------------------------------------------------
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new RivalryGoal(this));
        // Riposte generale : si un joueur ou un monstre les attaque, ils
        // se defendent (le RivalryGoal ci-dessus ne gere que les
        // affrontements entre Smart Folk rivaux).
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.add(2, new SocializeGoal(this));
        this.goalSelector.add(3, new BuildGoal(this));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8D));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));

        // Determine qui attaquer en retour : quiconque les blesse.
        this.targetSelector.add(1, new RevengeGoal(this));
    }

    // ------------------------------------------------------------------
    // Sauvegarde / lecture des donnees persistantes (niveau, competences,
    // relations) pour qu'elles survivent a un rechargement du monde.
    // ------------------------------------------------------------------
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("SmartFolkLevel", getLevel());
        nbt.putInt("SmartFolkExperience", experience);
        nbt.putInt("SmartFolkBuildSkill", buildSkill);
        nbt.putInt("SmartFolkSocialSkill", socialSkill);
        nbt.putInt("SmartFolkColor", getColorVariant().ordinal());

        NbtList relationsTag = new NbtList();
        for (Map.Entry<UUID, Integer> entry : relationships.entrySet()) {
            NbtCompound entryTag = new NbtCompound();
            entryTag.putUuid("Uuid", entry.getKey());
            entryTag.putInt("Affinity", entry.getValue());
            relationsTag.add(entryTag);
        }
        nbt.put("SmartFolkRelationships", relationsTag);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("SmartFolkLevel")) {
            this.dataTracker.set(LEVEL, nbt.getInt("SmartFolkLevel"));
        }
        experience = nbt.getInt("SmartFolkExperience");
        buildSkill = nbt.getInt("SmartFolkBuildSkill");
        socialSkill = nbt.getInt("SmartFolkSocialSkill");
        setColorVariant(ColorVariant.byId(nbt.getInt("SmartFolkColor")));
        applyColorGear();
        applyLevelBonuses();

        relationships.clear();
        NbtList relationsTag = nbt.getList("SmartFolkRelationships", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < relationsTag.size(); i++) {
            NbtCompound entryTag = relationsTag.getCompound(i);
            relationships.put(entryTag.getUuid("Uuid"), entryTag.getInt("Affinity"));
        }
    }
}
