package org.appland.settlers.maps;

public enum DecorationType {
    WATER_STONE,
    UNKNOWN_1,
    UNKNOWN_2,
    STRANDED_SHIP,
    GATE,
    OPEN_GATE,
    STALAGMITE,
    DEAD_TREE,
    SKELETON,
    UNKNOWN_3,
    TENT,
    GUARDHOUSE_RUIN,
    TOWER_RUIN,
    CROSS,
    CASTLE_RUIN,
    SMALL_VIKING_WITH_BOAT,
    PILE_OF_WOOD,
    WHALE_SKELETON_HEAD_RIGHT,
    WHALE_SKELETON_HEAD_LEFT;

    static DecorationType getDecorationTypeForUint8(short type) {

        if (type == 0x0B) {
            return WATER_STONE;
        } else if (type <= 0x0F) {
            return UNKNOWN_1;
        } else if (type <= 0x14) {
            return UNKNOWN_2;
        } else if (type == 0x15) {
            return STRANDED_SHIP;
        } else if (type == 0x16) {
            return GATE;
        } else if (type == 0x17) {
            return OPEN_GATE;
        } else if (type <= 0x1E) {
            return STALAGMITE;
        } else if (type <= 0x20) {
            return DEAD_TREE;
        } else if (type == 0x21) {
            return SKELETON;
        } else if (type <= 0x2B) {
            return UNKNOWN_3;
        } else if (type == 0x2C) {
            return TENT; //?
        } else if (type == 0x2D) {
            return GUARDHOUSE_RUIN; //?
        } else if (type == 0x2E) {
            return TOWER_RUIN; //?
        } else if (type == 0x30) {
            return CROSS; //?
        } else if (type == 0x2F) {
            return CASTLE_RUIN;
        } else if (type == 0x31) {
            return SMALL_VIKING_WITH_BOAT;
        } else if (type == 0x32) {
            return PILE_OF_WOOD;
        } else if (type == 0x33) {
            return WHALE_SKELETON_HEAD_RIGHT;
        } else if (type == 0x34) {
            return WHALE_SKELETON_HEAD_LEFT;
        }

        return null;
    }
}
