package org.appland.settlers.maps;


import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Material;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Terrain;
import org.appland.settlers.model.Tile;
import org.appland.settlers.model.Tile.Vegetation;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads a map binary file into a MapFile instance
 *
 */
public class MapLoader {

    @Option(name="--file", usage="Map file to load")
    String filename;

    @Option(name="--debug", usage="Print debug information")
    boolean debug = false;

    public static void main(String[] args) {

        /* Parse command line and start */
        MapLoader mapLoader = new MapLoader();

        CmdLineParser parser = new CmdLineParser(mapLoader);

        try {
            parser.parseArgument(args);

            MapFile mapFile = mapLoader.loadMapFromFile(mapLoader.filename);
            GameMap gameMap = mapLoader.convertMapFileToGameMap(mapFile);
        } catch (Exception ex) {
            Logger.getLogger(MapLoader.class.getName()).log(Level.SEVERE, null, ex);

            System.exit(1);
        }
    }

    public MapFile loadMapFromFile(String mapFilename) {

        byte[] fileHeader       = new byte[10];
        byte[] firstBlockHeader = new byte[16];
        byte[] newBlockHeader   = new byte[16];
        byte[] reusedArray      = new byte[36864];

        MapFile mapFile = new MapFile();

        System.out.println("Loading " + mapFilename);

        try {
            FileInputStream fis = new FileInputStream(new File(mapFilename));

            /* Read file header */
            fis.read(fileHeader);

            /* Read title */
            mapFile.setTitle(Utils.readString(fis, 19));
            System.out.println(" -- Title: " + mapFile.getTitle());

            /* Skip null terminator for title */
            fis.skip(1);

            /* Read width and height */
            mapFile.setWidth(Utils.readUnsignedByte(fis));

            fis.skip(1);

            mapFile.setHeight(Utils.readUnsignedByte(fis));

            fis.skip(1);

            System.out.println(" -- Dimensions: " + mapFile.getWidth() + " x " + mapFile.getHeight());

            /* Read the terrain type */
            mapFile.setTerrainType(TerrainType.fromShort(Utils.readUnsignedByte(fis)));

            System.out.println(" -- Terrain type: " + mapFile.getTerrainType());

            /* Read number of players */
            mapFile.setMaxNumberOfPlayers(Utils.readUnsignedByte(fis));

            System.out.println(" -- Number of players: " + mapFile.getMaxNumberOfPlayers());

            /* Read the author */
            mapFile.setAuthor(Utils.readString(fis, 19));

            System.out.println(" -- Author: " + mapFile.getAuthor());

            /* Skip null terminator for the author */
            fis.skip(1);

            /* Go through x coordinates for starting positions */
            List<Point> tmpStartingPositions = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                int x = Utils.readUnsignedByte(fis);

                fis.skip(1);

                if (i < mapFile.getMaxNumberOfPlayers()) {
                    tmpStartingPositions.add(new Point(x, 0));
                }
            }

            /* Go through y coordinates for starting positions */
            for (int i = 0; i < 7; i++) {
                int y = Utils.readUnsignedByte(fis);

                fis.skip(1);

                if (i < mapFile.getMaxNumberOfPlayers()) {
                    tmpStartingPositions.get(i).y = y;

                    mapFile.addStartingPosition(tmpStartingPositions.get(i));
                }
            }

            System.out.print(" -- Starting positions: ");

            for (Point point : mapFile.getStartingPoints()) {
                System.out.print("(" + point.x + ", " + point.y + ") ");
            }

            System.out.println("");

            /* Determine if the map is intended for unlimited play */
            if (Utils.readUnsignedByte(fis) == 0) {
                mapFile.enableUnlimitedPlay();
            } else {
                mapFile.disableUnlimitedPlay();
            }

            System.out.println(" -- Unlimited play: " + mapFile.isPlayUnlimited());

            /* Read player face */
            List<PlayerFace> playerFaces = new ArrayList<>();
            for (int i = 0; i < 7; i++) {

                short faceByte = Utils.readUnsignedByte(fis);

                if (i < mapFile.getMaxNumberOfPlayers()) {
                    PlayerFace pf = PlayerFace.playerFaceFromShort(faceByte);

                    playerFaces.add(pf);
                }
            }

            mapFile.setPlayerFaces(playerFaces);

            for (PlayerFace face : mapFile.getPlayerFaces()) {
                System.out.println(" -- Player: " + face.name());
            }

            /* Read starting points for each unique water and land mass */
            fis.read(reusedArray, 0, 2250);

            List<UniqueMass> masses = new ArrayList<>();

            for (int i = 0; i < 250; i++) {

                MassType type = MassType.massTypeFromInt(Utils.getUnsignedByteInArray(reusedArray, (i * 9)));

                int x = Utils.getUnsignedByteInArray(reusedArray, (i * 9) + 1);
                int y = Utils.getUnsignedByteInArray(reusedArray, (i * 9) + 3);

                java.awt.Point position = new java.awt.Point(x, y);

                long totalMass = Utils.getUnsignedShortInArray(reusedArray, (i * 9) + 5);
                UniqueMass mass = new UniqueMass(type, position, totalMass);

                masses.add(mass);

                if (!position.equals(new java.awt.Point(0, 0))) {
                    mapFile.addMassStartingPoint(position);
                }
            }

            if (debug) {
                System.out.println(" -- Loaded starting points for water and land masses");
            }

            /* Read map file identification */
            int fileId = Utils.readUnsignedShort(fis);

            /* Verify file id */
            if (fileId != 0x1127) {
                System.out.println("Invalid file id " + fileId + " (must be 0x1127). Exiting.");
                System.exit(1);
            }

            /* Skip four un-used bytes */
            fis.read(reusedArray, 0, 4);

            if (reusedArray[0] != 0 || reusedArray[1] != 0 ||
                reusedArray[2] != 0 || reusedArray[3] != 0) {
                System.out.println("Not zeros although mandatory. Are instead " +
                                    reusedArray[0] + " " + 
                        reusedArray[0] + " " + 
                        reusedArray[0] + " " + 
                        reusedArray[0] + " ");
                System.exit(1);
            }

            /* Extra 01 00 bytes may appear here but no files seen so far have this */

            /* Read actual width and height, as used by map loaders */
            int newWidth = Utils.readUnsignedByte(fis);

            fis.skip(1);

            int newHeight = Utils.readUnsignedByte(fis);

            fis.skip(1);

            if (newWidth != mapFile.getWidth() || newHeight != mapFile.getHeight()) {
                System.out.println("Dimensions don't match. Were "
                        + mapFile.getWidth() + " x " + mapFile.getHeight() + "but now saw "
                        + newWidth + " x " + newHeight);

                System.exit(1);
            }

            /* Read first sub block fileHeader with data about heights */
            fis.read(firstBlockHeader, 0, 16);

            if (debug) {
                System.out.println("");
                System.out.println("Height block fileHeader");
            }

            /* Verify that the coming six bytes are: 0x 10 27 00 00 00 00 */
            byte[] MANDATORY = new byte[] {0x10, 0x27, 00, 00, 00, 00};
            if (!Utils.byteArraysMatch(firstBlockHeader, MANDATORY, 6)) {
                System.out.println("Mandatory bytes don't match.");
                System.out.println(Utils.getHex(firstBlockHeader));
                System.out.println("Should be " + Utils.getHex(MANDATORY));
            }

            /* Handle fixed 01 00 if they appear */
            int extraOffset = 0;
            if (Utils.getUnsignedByteInArray(firstBlockHeader, 6) == 1 &&
                Utils.getUnsignedByteInArray(firstBlockHeader, 7) == 0) {
                System.out.println(" -- Ignoring fixed 01 00 (first place).");
                extraOffset = 2;
            }

            /* Verify that the dimensions remain */
            if (mapFile.getWidth()  != Utils.getUnsignedByteInArray(firstBlockHeader, 6 + extraOffset) ||
                mapFile.getHeight() != Utils.getUnsignedByteInArray(firstBlockHeader, 8 + extraOffset)) {
                System.out.println("Mismatch in dimensions. Was "
                                   + mapFile.getWidth() + " x " + mapFile.getHeight() + " but saw "
                                   + Utils.getUnsignedByteInArray(firstBlockHeader, 6 + extraOffset) + " x "
                                   + Utils.getUnsignedByteInArray(firstBlockHeader, 8 + extraOffset));
                System.exit(1);
            }

            /* Handle fixed 01 00 if they appear */
            if (Utils.getUnsignedByteInArray(firstBlockHeader, 10 + extraOffset) == 1 &&
                Utils.getUnsignedByteInArray(firstBlockHeader, 11 + extraOffset) == 0) {

                if (debug) {
                    System.out.println(" -- Ignoring fixed 01 00 (second place).");
                }

                extraOffset += 2;
            }

            int subBlockSize = Utils.getUnsignedShortInArray(firstBlockHeader, 11 + extraOffset);

            if (debug) {
                System.out.println(" -- Data size: " + subBlockSize);
            }

            /* Read height map */
            fis.read(reusedArray, 0, subBlockSize);
            for (int i = 0; i < subBlockSize; i++) {
                int heightAtPoint = Utils.getUnsignedByteInArray(reusedArray, i);

                SpotData spot = new SpotData();

                spot.setHeight(heightAtPoint);

                mapFile.addSpot(spot);
            }

            if (debug) {
                System.out.println(" -- Loaded heights");
            }

            /* Read the second sub block with textures for up-pointing triangles */
            if (debug) {
                System.out.println("");
                System.out.print("Texture block 1: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block fileHeader doesn't match the first fileHeader */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Header of block for upward triangles doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            fis.read(reusedArray, 0, subBlockSize);
            for (int i = 0; i < subBlockSize; i++) {
                Texture tex = Texture.textureFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                mapFile.getSpot(i).setTextureTriangleBelow(tex);
            }

            /* Read the third sub block fileHeader with textures for down-pointing triangles */
            if (debug) {
                System.out.println("");
                System.out.print("Texture block 2: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block fileHeader doesn't match the first fileHeader */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Header of block for downward triangles doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Read textures */
            fis.read(reusedArray, 0, subBlockSize);
            for (int i = 0; i < subBlockSize; i++) {
                Texture tex = Texture.textureFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                mapFile.getSpot(i).setTextureTriangleDownRight(tex);
            }

            /* Read the fourth sub block fileHeader with roads */
            if (debug) {
                System.out.println("");
                System.out.print("Road block: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block fileHeader doesn't match the first fileHeader */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Header for road section doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Read roads */
            fis.read(reusedArray, 0, subBlockSize);

            // Ignore roads for now

            /* Read the fifth sub block fileHeader with object properties */
            if (debug) {
                System.out.println("");
                System.out.print("Object property block: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block fileHeader doesn't match the first fileHeader */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Object properties fileHeader doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Object properties */
            fis.read(reusedArray, 0, subBlockSize);

            for (int i = 0; i < subBlockSize; i++) {
                mapFile.getSpot(i).setObjectProperties(Utils.getUnsignedByteInArray(reusedArray, i));
            }

            /*  Read sixth sub block header with object types */
            if (debug) {
                System.out.println("");
                System.out.print("Object type block: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Object types fileHeader doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Read object types*/
            fis.read(reusedArray, 0, subBlockSize);

            for (int i = 0; i < subBlockSize; i++) {
                mapFile.getSpot(i).setObjectType(Utils.getUnsignedByteInArray(reusedArray, i));
            }

            /* Read seventh sub block header with animals */
            if (debug) {
                System.out.println("");
                System.out.println("Animals block: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Animals block fileHeader doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Read animals */
            fis.read(reusedArray, 0, subBlockSize);

            if (debug) {
                System.out.print(" -- Wild animals: ");
            }

            for (int i = 0; i < subBlockSize; i++) {
                Animal animal = Animal.animalFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                if (animal.isWildAnimal()) {
                    mapFile.getSpot(i).setAnimal(animal);

                    if (debug) {
                        System.out.print(i + " ");
                    }

                }
            }

            System.out.println("");

            /* Skip eighth block with unknown data */
            if (debug) {
                System.out.println("");
                System.out.print("Unknown block: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Unknown block fileHeader doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Skip the block */
            fis.skip(subBlockSize);

            /* Read ninth block with buildable sites */
            if (debug) {
                System.out.println("");
                System.out.print("Buildable sites block: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block fileHeader doesn't match the first fileHeader */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Buildable sites fileHeader doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Read the buildable sites */
            fis.read(reusedArray, 0, subBlockSize);
            for (int i = 0; i < subBlockSize; i++) {
                BuildableSite site = BuildableSite.buildableSiteFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                mapFile.getSpot(i).setBuildableSite(site);
            }

            /* Skip tenth block with unknown data */
            if (debug) {
                System.out.println("");
                System.out.print("Second unknown block: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Second unknown block fileHeader doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Skip the block */
            fis.skip(subBlockSize);

            /* Skip eleventh block with map editor cursor position */
            if (debug) {
                System.out.println("");
                System.out.print("Map editor cursor position: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Map editor cursor position block fileHeader doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Skip the block */
            fis.skip(subBlockSize);

            /* Read the 12th block with resources */
            if (debug) {
                System.out.println("");
                System.out.print("Resource block: ");
            }

            fis.read(newBlockHeader, 0, 16);

            /* Exit if the block fileHeader doesn't match the first fileHeader */
            if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
                System.out.println("Resource block fileHeader doesn't match. Exiting.");
                System.out.println("First header: " + Utils.getHex(firstBlockHeader));
                System.out.println("Current header: " + Utils.getHex(newBlockHeader));
                System.exit(1);
            }

            /* Read the resources block */
            fis.read(reusedArray, 0, subBlockSize);

            for (int i = 0; i < subBlockSize; i++) {
                Resource resource = Resource.resourceFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                mapFile.getSpot(i).setResource(resource);
            }

            /* Ignore gouraud shading block */
            fis.skip(newBlockHeader.length);
            fis.skip(subBlockSize);

            /* Ignore passable areas block */
            fis.skip(newBlockHeader.length);
            fis.skip(subBlockSize);

            /* Footer, always 0xFF */
        } catch (IOException ex) {
            Logger.getLogger(MapLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return mapFile;
    }

    public GameMap convertMapFileToGameMap(MapFile mapFile) throws Exception {

        /* Generate list of players */
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < mapFile.maxNumberOfPlayers; i++) {
            players.add(new Player("Player " + i, new Color(i*20, i*20, i*20)));
        }

        /* Create initial game map with correct dimensions */
        GameMap gameMap = new GameMap(players, mapFile.getWidth() * 2 + 1, mapFile.getHeight() + 2);

        /* Set up the terrain */
        Terrain terrain = gameMap.getTerrain();
        int row = 1;
        int index = 1;

        for (SpotData spot : mapFile.getSpots()) {

            /* Set triangles above, instead of below because the iteration starts
               from the bottom and runs upwards
            
               This will look good but the maps are rendered upside down
            */
            org.appland.settlers.model.Point p0 = new org.appland.settlers.model.Point(index - 1, row + 1);
            org.appland.settlers.model.Point p1 = new org.appland.settlers.model.Point(index, row);
            org.appland.settlers.model.Point p2 = new org.appland.settlers.model.Point(index + 1, row + 1);
            org.appland.settlers.model.Point p3 = new org.appland.settlers.model.Point(index + 2, row);

            Tile tileUp = terrain.getTile(p0, p1, p2);
            Tile tileUpRight = terrain.getTile(p1, p2, p3);

            Vegetation vegetationUp = Utils.convertTextureToVegetation(spot.getTextureBelow());
            Vegetation vegetationUpRight = Utils.convertTextureToVegetation(spot.getTextureDownRight());

            tileUp.setVegetationType(vegetationUp);
            tileUpRight.setVegetationType(vegetationUpRight);

            /* Set mineral quantities */
            if (spot.hasMineral()) {
                Material mineral = Utils.resourceTypeToMaterial(spot.getMineralType());

                tileUp.setAmountMineral(mineral, spot.getMineralQuantity());
            }

            /* Place stones */
            if (spot.hasStone()) {
                gameMap.placeStone(p1);
            }

            /* Place trees */
            if (spot.hasTree()) {
                gameMap.placeTree(p1);
            }

            /* Place wild animals */
            if (spot.hasWildAnimal()) {
                gameMap.placeWildAnimal(p1);
            }

            if (index == gameMap.getWidth() - 2) {
                index = 2;
                row++;
            } else if (index >= gameMap.getWidth() - 1) {
                index = 1;
                row++;
            } else {
                index += 2;
            }
        }

        List<org.appland.settlers.model.Point> startingPoints = new ArrayList<>();

        for (java.awt.Point p : mapFile.getStartingPoints()) {
            try {
                startingPoints.add(new org.appland.settlers.model.Point(p.x*2, p.y));
            } catch (Exception e) {
                startingPoints.add(new org.appland.settlers.model.Point(p.x*2, p.y + 1));
            }
        }

        gameMap.setStartingPoints(startingPoints);


        return gameMap;
    }
}
