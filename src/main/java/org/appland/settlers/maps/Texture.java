/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.maps;

/**
 *
 * @author johan
 */
public enum Texture {
    SAVANNAH(0),
    MOUNTAIN(1),
    SNOW(2),
    SWAMP(3),
    DESERT_1(4),
    WATER(5),
    BUILDABLE_WATER(6),
    DESERT_2(7),
    MEADOW_1(8),
    MEADOW_2(9),
    MEADOW_3(10),
    MOUNTAIN_2(11),
    MOUNTAIN_3(12),
    MOUNTAIN_4(13),
    STEPPE(14),
    FLOWER_MEADOW(15),
    LAVA(16),
    MAGENTA(17),
    MOUNTAIN_MEADOW(18),
    WATER_2(19),
    LAVA_2(20),
    LAVA_3(21),
    LAVA_4(22),
    BUILDABLE_MOUNTAIN_2(23);

    private int id;

    Texture(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }

    public static Texture textureFromInt(int i) {
        switch (i) {
            case 0:
                return SAVANNAH;
            case 1:
                return MOUNTAIN;
            case 2:
                return SNOW;
            case 3:
                return SWAMP;
            case 4:
                return DESERT_1;
            case 5:
                return WATER;
            case 6:
                return BUILDABLE_WATER;
            case 7:
                return DESERT_2;
            case 8:
                return MEADOW_1;
            case 9:
                return MEADOW_2;
            case 10:
                return MEADOW_3;
            case 11:
                return MOUNTAIN_2;
            case 12:
                return MOUNTAIN_3;
            case 13:
                return MOUNTAIN_4;
            case 14:
                return STEPPE;
            case 15:
                return FLOWER_MEADOW;
            case 16:
                return LAVA;
            case 17:
                return MAGENTA;
            case 18:
                return MOUNTAIN_MEADOW;
            case 19:
                return WATER_2;
            case 20:
                return LAVA_2;
            case 21:
                return LAVA_3;
            case 22:
                return LAVA_4;
            case 23:
                return BUILDABLE_MOUNTAIN_2;
            default:
                return null;
        }
    }
}
