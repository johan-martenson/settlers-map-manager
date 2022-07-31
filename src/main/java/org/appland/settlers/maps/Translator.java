package org.appland.settlers.maps;

import org.appland.settlers.model.DecorationType;

import java.util.HashMap;
import java.util.Map;

import static org.appland.settlers.model.DecorationType.BROWN_MUSHROOM;
import static org.appland.settlers.model.DecorationType.BUSH;
import static org.appland.settlers.model.DecorationType.CACTUS_1;
import static org.appland.settlers.model.DecorationType.CACTUS_2;
import static org.appland.settlers.model.DecorationType.CATTAIL;
import static org.appland.settlers.model.DecorationType.DEAD_TREE;
import static org.appland.settlers.model.DecorationType.DEAD_TREE_LYING_DOWN;
import static org.appland.settlers.model.DecorationType.FEW_SMALL_STONES;
import static org.appland.settlers.model.DecorationType.FLOWERS;
import static org.appland.settlers.model.DecorationType.GRASS_1;
import static org.appland.settlers.model.DecorationType.GRASS_2;
import static org.appland.settlers.model.DecorationType.LARGE_BUSH;
import static org.appland.settlers.model.DecorationType.LITTLE_GRASS;
import static org.appland.settlers.model.DecorationType.MINI_BROWN_MUSHROOM;
import static org.appland.settlers.model.DecorationType.MINI_BUSH;
import static org.appland.settlers.model.DecorationType.MINI_GRASS;
import static org.appland.settlers.model.DecorationType.MINI_STONE;
import static org.appland.settlers.model.DecorationType.MINI_STONE_WITH_GRASS;
import static org.appland.settlers.model.DecorationType.PILE_OF_STONES;
import static org.appland.settlers.model.DecorationType.PORTAL;
import static org.appland.settlers.model.DecorationType.SHINING_PORTAL;
import static org.appland.settlers.model.DecorationType.SKELETON;
import static org.appland.settlers.model.DecorationType.SMALL_BUSH;
import static org.appland.settlers.model.DecorationType.SMALL_SKELETON;
import static org.appland.settlers.model.DecorationType.SMALL_STONE;
import static org.appland.settlers.model.DecorationType.SMALL_STONE_WITH_GRASS;
import static org.appland.settlers.model.DecorationType.SNOWMAN;
import static org.appland.settlers.model.DecorationType.SOME_SMALLER_STONES;
import static org.appland.settlers.model.DecorationType.SOME_SMALL_STONES;
import static org.appland.settlers.model.DecorationType.SOME_WATER;
import static org.appland.settlers.model.DecorationType.SPARSE_BUSH;
import static org.appland.settlers.model.DecorationType.STONE;
import static org.appland.settlers.model.DecorationType.TOADSTOOL;

public class Translator {

    public static final Map<Integer, DecorationType> DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP;

    static {
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP = new HashMap<>();

        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x00, MINI_BROWN_MUSHROOM);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x01, TOADSTOOL);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x02, MINI_STONE);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x03, SMALL_STONE);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x04, STONE);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x05, DEAD_TREE_LYING_DOWN);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x06, DEAD_TREE);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x07, SKELETON);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x08, SMALL_SKELETON);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x09, FLOWERS);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x0A, LARGE_BUSH);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x0B, PILE_OF_STONES);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x0C, CACTUS_1);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x0D, CACTUS_2);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x0E, CATTAIL);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x0F, GRASS_1);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x10, BUSH);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x11, SMALL_BUSH);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x12, MINI_BUSH);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x13, GRASS_2);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x14, MINI_GRASS);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x15, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x16, PORTAL);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x17, SHINING_PORTAL);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x18, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x19, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x1A, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x1B, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x1C, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x1D, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x1E, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x1F, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x20, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x21, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x22, BROWN_MUSHROOM);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x23, MINI_STONE_WITH_GRASS);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x24, SMALL_STONE_WITH_GRASS);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x25, SOME_SMALL_STONES);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x26, SOME_SMALLER_STONES);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x27, FEW_SMALL_STONES);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x28, SPARSE_BUSH);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x29, SOME_WATER);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x2A, LITTLE_GRASS); // VERIFY
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x2B, SNOWMAN);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x2C, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x2D, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x2E, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x2F, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x30, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x31, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x32, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x33, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x34, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x35, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x36, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x37, null);
        DEFAULT_OBJECT_PROPERTY_TO_DECORATION_MAP.put(0x38, null);
    }
}
