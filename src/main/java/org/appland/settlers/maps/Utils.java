/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.maps;

import java.nio.ByteBuffer;
import org.appland.settlers.model.Material;
import org.appland.settlers.model.Tile;

/**
 *
 * @author johan
 */
public class Utils {

    public static short getUnsignedByteInArray (byte[] arr, int i) {
        ByteBuffer bb = ByteBuffer.wrap(arr);
        return ((short)(bb.get(i) & 0xff));
    }

    public static int getUnsignedShortInArray (byte[] arr, int i) {
        ByteBuffer bb = ByteBuffer.wrap(arr);
        return (bb.getShort(i) & 0xffff);
    }

    public static long getUnsignedIntInArray (byte[] arr, int i) {
        ByteBuffer bb = ByteBuffer.wrap(arr);
        return ((long)bb.getInt(i) & 0xffffffff);
    }

    static String getHex(byte[] blockHeader1) {
        return javax.xml.bind.DatatypeConverter.printHexBinary(blockHeader1);
    }

    static boolean byteArraysMatch(byte[] blockHeader1, byte[] blockHeader2, int len) {
        for (int i = 0; i < len; i++) {
            if (blockHeader1[i] != blockHeader2[i]) {
                return false;
            }
        }

        return true;
    }

    static Tile.Vegetation convertTextureToVegetation(Texture textureBelow) {
        switch (textureBelow) {
            case MOUNTAIN:
            case MOUNTAIN_2:
            case MOUNTAIN_3:
            case MOUNTAIN_4:
                return Tile.Vegetation.MOUNTAIN;

            case SWAMP:
                return Tile.Vegetation.SWAMP;

            case WATER:
            case BUILDABLE_WATER:
            case WATER_2:
                return Tile.Vegetation.WATER;

            case DESERT_2:
            case MEADOW_1:
            case MEADOW_2:
            case MEADOW_3:
            case FLOWER_MEADOW:
            case DESERT_1:
            case STEPPE:
            case SAVANNAH:
            case SNOW:
            case MOUNTAIN_MEADOW:
            case BUILDABLE_MOUNTAIN_2:
                return Tile.Vegetation.GRASS;

            case LAVA:
            case MAGENTA:
            case LAVA_2:
            case LAVA_3:
            case LAVA_4:
            default:
                return Tile.Vegetation.MOUNTAIN;

        }
    }

    static Material resourceTypeToMaterial(ResourceType mineralType) {
        switch (mineralType) {
            case COAL:
                return Material.COAL;
            case IRON_ORE:
                return Material.IRON;
            case GOLD:
                return Material.GOLD;
            case GRANITE:
                return Material.STONE;
            default:
                return null;
        }
    }
}
