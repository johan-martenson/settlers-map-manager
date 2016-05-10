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
public enum Animal {
    NO_ANIMAL(0),
    RABBIT(1),
    FOX(2),
    STAG(3),
    DEER(4),
    DUCK(5),
    SHEEP(6),
    DEER_2(7),
    DUCK_2(8),
    PACK_DONKEY(9)
    ;

    private int id;

    private Animal(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }

    static Animal animalFromInt(short i) {
        switch (i) {
            case 0:
                return NO_ANIMAL;
            case 1:
                return RABBIT;
            case 2:
                return FOX;
            case 3:
                return STAG;
            case 4:
                return DEER;
            case 5:
                return DUCK;
            case 6:
                return SHEEP;
            case 7:
                return DEER_2;
            case 8:
                return DUCK_2;
            case 9:
                return PACK_DONKEY;
            default:
                return NO_ANIMAL;
        }
    }
}
