/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.maps;

import org.appland.settlers.model.Material;
import org.appland.settlers.model.Tile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author johan
 */
class Utils {

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

        switch (textureBelow.getValue()) {
            case 0x00: return Tile.Vegetation.SAVANNAH; // Savannah - can build houses
            case 0x01: return Tile.Vegetation.MOUNTAIN; // Mountain 1 - mining
            case 0x02: return Tile.Vegetation.SNOW;     // Snow - can't walk on the snow
            case 0x03: return Tile.Vegetation.SWAMP;    // Swamp - can't walk on swamp?
            case 0x04: return Tile.Vegetation.DESERT;   // Desert 1 - flags
            case 0x05: return Tile.Vegetation.DEEP_WATER;    // Water - no walking, sailing
            case 0x06: return Tile.Vegetation.SHALLOW_WATER; // Buildable water - can build houses
            case 0x07: return Tile.Vegetation.DESERT;   // Desert 2 - flags
            case 0x08: return Tile.Vegetation.GRASS;    // Meadow 1 - can build houses
            case 0x09: return Tile.Vegetation.GRASS;    // Meadow 2 - can build houses
            case 0x0A: return Tile.Vegetation.GRASS;    // Meadow 3 - can build houses
            case 0x0B: return Tile.Vegetation.MOUNTAIN; // Mountain 2 - mining
            case 0x0C: return Tile.Vegetation.MOUNTAIN; // Mountain 3 - mining
            case 0x0D: return Tile.Vegetation.MOUNTAIN; // Mountain 4 - mining
            case 0x0E: return Tile.Vegetation.STEPPE;   // Steppe - can build houses
            case 0x0F: return Tile.Vegetation.GRASS;    // Flower meadow - can build houses
            case 0x10: return Tile.Vegetation.LAVA;     // Lava - no walking
            case 0x11: return Tile.Vegetation.MAGENTA;  // MAGENTA - build flags
            case 0x12: return Tile.Vegetation.MOUNTAIN_MEADOW; // Mountain meadow - can build houses
            case 0x13: return Tile.Vegetation.WATER;    // Water - no walking, no building, no sailing
            case 0x14: return Tile.Vegetation.LAVA;     // Lava 2 - no walking, building
            case 0x15: return Tile.Vegetation.LAVA;     // Lava 3 - no walking, building
            case 0x16: return Tile.Vegetation.LAVA;     // Lava 4 - no walking, building
            case 0x22: return Tile.Vegetation.BUILDABLE_MOUNTAIN; // Buildable mountain can build houses, walking, no mining
            default:
                System.out.println("Can't handle texture " + textureBelow);
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

    static String readString(FileInputStream fis, int length) throws IOException {
        byte[] array = new byte[length];

        fis.read(array, 0, length);

        return new String(array);
    }

    static short readUnsignedByte(FileInputStream fis) throws IOException {

        byte[] bytes = new byte[2];

        fis.read(bytes, 0, 1);

        return Utils.getUnsignedByteInArray(bytes, 0);
    }

    public static int readUnsignedShort(FileInputStream fis) throws IOException {
        byte[] bytes = new byte[2];

        fis.read(bytes, 0,2);

        return getUnsignedShortInArray(bytes, 0);
    }
}
