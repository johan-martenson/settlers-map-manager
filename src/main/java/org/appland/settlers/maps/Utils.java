/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.maps;

import org.appland.settlers.model.DetailedVegetation;
import org.appland.settlers.model.Material;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author johan
 */
class Utils {

    public static short getUint8InArray(byte[] arr, int i) {
        ByteBuffer bb = ByteBuffer.wrap(arr);
        return ((short)(bb.get(i) & 0xff));
    }

    public static char getNextUnsignedShort(InputStream is) throws IOException {
        byte[] bytes = new byte[2];

        is.read(bytes);

        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getChar();
    }

    public static char getUint16InArray(byte[] arr, int i) {
        ByteBuffer bb = ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN);

        return bb.getChar(i);
    }

    public static long getUint32InArray(byte[] arr, int i) {
        ByteBuffer bb = ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN);
        return ((long)bb.getInt(i) & 0xffffffff);
    }

    static String getHex(byte[] blockHeader1) {
        StringBuilder hex = new StringBuilder();

        for (byte b : blockHeader1) {
            hex.append(Integer.toHexString((int)(b & 0xff)));
        }

        return hex.toString();
    }

    static boolean byteArraysMatch(byte[] blockHeader1, byte[] blockHeader2, int len) {
        for (int i = 0; i < len; i++) {
            if (blockHeader1[i] != blockHeader2[i]) {
                return false;
            }
        }

        return true;
    }

    static DetailedVegetation convertTextureToVegetation(Texture texture) {

        switch (texture) {
            case SAVANNAH:             return DetailedVegetation.SAVANNAH;           // Savannah - can build houses
            case MOUNTAIN_1:           return DetailedVegetation.MOUNTAIN_1;         // Mountain 1 - mining
            case SNOW:                 return DetailedVegetation.SNOW;               // Snow - can't walk on the snow
            case SWAMP:                return DetailedVegetation.SWAMP;              // Swamp - can't walk on swamp?
            case DESERT_1:             return DetailedVegetation.DESERT_1;           // Desert 1 - flags
            case WATER:                return DetailedVegetation.WATER;              // Water - no walking, sailing
            case BUILDABLE_WATER:      return DetailedVegetation.BUILDABLE_WATER;    // Buildable water - can build houses
            case DESERT_2:             return DetailedVegetation.DESERT_2;           // Desert 2 - flags
            case MEADOW_1:             return DetailedVegetation.MEADOW_1;           // Meadow 1 - can build houses
            case MEADOW_2:             return DetailedVegetation.MEADOW_2;           // Meadow 2 - can build houses
            case MEADOW_3:             return DetailedVegetation.MEADOW_3;           // Meadow 3 - can build houses
            case MOUNTAIN_2:           return DetailedVegetation.MOUNTAIN_2;         // Mountain 2 - mining
            case MOUNTAIN_3:           return DetailedVegetation.MOUNTAIN_3;         // Mountain 3 - mining
            case MOUNTAIN_4:           return DetailedVegetation.MOUNTAIN_4;         // Mountain 4 - mining
            case STEPPE:               return DetailedVegetation.STEPPE;             // Steppe - can build houses
            case FLOWER_MEADOW:        return DetailedVegetation.FLOWER_MEADOW;      // Flower meadow - can build houses
            case LAVA:                 return DetailedVegetation.LAVA;               // Lava - no walking
            case MAGENTA:              return DetailedVegetation.MAGENTA;            // Magenta - build flags
            case MOUNTAIN_MEADOW:      return DetailedVegetation.MOUNTAIN_MEADOW;    // Mountain meadow - can build houses
            case WATER_2:              return DetailedVegetation.WATER_2;            // Water - no walking, no building, no sailing
            case LAVA_2:               return DetailedVegetation.LAVA_2;             // Lava 2 - no walking, building
            case LAVA_3:               return DetailedVegetation.LAVA_3;             // Lava 3 - no walking, building
            case LAVA_4:               return DetailedVegetation.LAVA_4;             // Lava 4 - no walking, building
            case BUILDABLE_MOUNTAIN:   return DetailedVegetation.BUILDABLE_MOUNTAIN; // Buildable mountain can build houses, walking, no mining

            default:
                System.out.println("Can't handle texture " + texture);
                System.exit(1);

                return null;
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

    static String readString(InputStream fis, int length) throws IOException {
        byte[] array = new byte[length];

        fis.read(array, 0, length);

        int indexOfZero = length;

        for (int i = 0; i < length; i++) {
            if (array[i] == 0) {
                indexOfZero = i;

                break;
            }
        }

        return new String(array, 0, indexOfZero);
    }

    static short readUnsignedByte(InputStream fis) throws IOException {

        byte[] bytes = new byte[2];

        fis.read(bytes, 0, 1);

        return Utils.getUint8InArray(bytes, 0);
    }
}
