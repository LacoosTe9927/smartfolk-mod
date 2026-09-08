package fr.smartfolk.entity;

import net.minecraft.util.math.random.Random;

/**
 * Les differentes couleurs possibles pour un Smart Folk.
 * La couleur est visuelle (teinte de son plastron en cuir) et sert
 * aussi de petit "clan" de depart : deux individus de la meme couleur
 * commencent avec un a priori plus favorable l'un envers l'autre
 * (voir SmartFolkEntity#initialAffinityBonus). Rien ne les empeche
 * pour autant de devenir rivaux plus tard, ni deux couleurs
 * differentes de devenir amies.
 */
public enum ColorVariant {
    ROUGE(0xB02E26),
    BLEU(0x3C44AA),
    VERT(0x5E7C16),
    JAUNE(0xFED83D),
    VIOLET(0x8932B8),
    ORANGE(0xF9801D),
    CYAN(0x169C9C),
    ROSE(0xF38BAA);

    private final int dyeRgb;

    ColorVariant(int dyeRgb) {
        this.dyeRgb = dyeRgb;
    }

    public int getDyeRgb() {
        return dyeRgb;
    }

    public static ColorVariant random(Random random) {
        ColorVariant[] values = values();
        return values[random.nextInt(values.length)];
    }

    public static ColorVariant byId(int id) {
        ColorVariant[] values = values();
        if (id < 0 || id >= values.length) {
            return ROUGE;
        }
        return values[id];
    }
}
