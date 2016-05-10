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
public enum ResourceType {
    WATER,
    FISH,
    COAL,
    IRON_ORE,
    GOLD,
    GRANITE;

    public static ResourceType resourceTypeFromInt(int type) {
        if (type == 33) {
            return WATER;
        } else if (type == 135) {
            return FISH;
        } else if (type >= 64 && type <= 71) {
            return COAL;
        } else if (type >= 72 && type <= 79) {
            return IRON_ORE;
        } else if (type >= 80 && type <= 87) {
            return GOLD;
        } else if (type >= 88 && type <= 95) {
            return GRANITE;
        } else {
            return null;
        }
    }
}
