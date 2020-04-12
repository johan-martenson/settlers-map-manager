package org.appland.settlers.maps;


import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Material;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Terrain;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    @Option(name="--mode", usage="Set swd or wld file format explicitly")
    String mode = "swd";

    public static void main(String[] args) {

        /* Parse command line and start */
        MapLoader mapLoader = new MapLoader();

        CmdLineParser parser = new CmdLineParser(mapLoader);

        try {
            parser.parseArgument(args);

            MapFile mapFile = mapLoader.loadMapFromFile(mapLoader.filename);
            GameMap gameMap = mapLoader.convertMapFileToGameMap(mapFile);
        } catch (Exception | InvalidMapException ex) {
            Logger.getLogger(MapLoader.class.getName()).log(Level.SEVERE, null, ex);

            System.exit(1);
        }
    }

    public MapFile loadMapFromFile(String mapFilename) throws SettlersMapLoadingException, IOException, InvalidMapException {
        if (debug) {
            System.out.print("Loading " + mapFilename);
        }

        FileInputStream fis = new FileInputStream(new File(mapFilename));

        return loadMapFromStream(fis);
    }

    public MapFile loadMapFromStream(InputStream inputStream) throws SettlersMapLoadingException, IOException, InvalidMapException {

        byte[] fileHeader       = new byte[10];
        byte[] firstBlockHeader = new byte[16];
        byte[] newBlockHeader   = new byte[16];
        byte[] reusedArray      = new byte[65536];

        MapFile mapFile = new MapFile();

        if (isWldMode() && debug) {
            System.out.println(" in WLD mode");
        } else if (debug) {
            System.out.println(" in SWD mode");
        }

        /* Read file header */
        inputStream.read(fileHeader);

        if (debug) {
            System.out.println(" -- File header: " + Utils.getHex(fileHeader));
        }

        /* Read title */
        String title;

        if (isWldMode()) {
            title = Utils.readString(inputStream, 23);
        } else {
            title = Utils.readString(inputStream, 19);
        }

        mapFile.setTitle(title);

        if (debug) {
            System.out.println(" -- Title: " + title);
        }

        /* Skip null terminator for title */
        inputStream.skip(1);

        /* Read width and height for SWD maps*/
        if (!isWldMode()) {
            mapFile.setWidth(Utils.getNextUnsignedShort(inputStream));

            mapFile.setHeight(Utils.getNextUnsignedShort(inputStream));
        }

        if (debug) {
            System.out.println(" -- Dimensions: " + mapFile.getWidth() + " x " + mapFile.getHeight());
        }

        /* Read the terrain type */
        mapFile.setTerrainType(TerrainType.fromShort(Utils.readUnsignedByte(inputStream)));

        if (debug) {
            System.out.println(" -- Terrain type: " + mapFile.getTerrainType());
        }

        /* Read number of players */
        mapFile.setMaxNumberOfPlayers(Utils.readUnsignedByte(inputStream));

        if (debug) {
            System.out.println(" -- Number of players: " + mapFile.getMaxNumberOfPlayers());
        }

        if (mapFile.getMaxNumberOfPlayers() < 1) {
            throw new InvalidMapException("The map must contain at least one player");
        }

        /* Read the author */
        mapFile.setAuthor(Utils.readString(inputStream, 19));

        if (debug) {
            System.out.println(" -- Author: " + mapFile.getAuthor());
        }

        /* Skip null terminator for the author */
        inputStream.skip(1);

        /* Go through x coordinates for starting positions */
        List<java.awt.Point> tmpStartingPositions = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            int x = Utils.readUnsignedByte(inputStream); //WRONG?

            inputStream.skip(1);

            if (i < mapFile.getMaxNumberOfPlayers()) {
                tmpStartingPositions.add(new Point(x, 0));
            }
        }

        /* Go through y coordinates for starting positions */
        for (int i = 0; i < 7; i++) {
            int y = Utils.readUnsignedByte(inputStream);

            inputStream.skip(1);

            if (i < mapFile.getMaxNumberOfPlayers()) {
                tmpStartingPositions.get(i).y = y;

                mapFile.addStartingPosition(tmpStartingPositions.get(i));
            }
        }

        if (debug) {
            System.out.println();
        }

        /* Determine if the map is intended for unlimited play */
        if (Utils.readUnsignedByte(inputStream) == 0) {
            mapFile.enableUnlimitedPlay();
        } else {
            mapFile.disableUnlimitedPlay();
        }

        if (debug) {
            System.out.println(" -- Unlimited play: " + mapFile.isPlayUnlimited());
        }

        /* Read player face */
        List<PlayerFace> playerFaces = new ArrayList<>();
        for (int i = 0; i < 7; i++) {

            short faceByte = Utils.readUnsignedByte(inputStream);

            if (i < mapFile.getMaxNumberOfPlayers()) {
                PlayerFace pf = PlayerFace.playerFaceFromShort(faceByte);

                playerFaces.add(pf);
            }
        }

        mapFile.setPlayerFaces(playerFaces);

        for (PlayerFace face : mapFile.getPlayerFaces()) {
            if (debug) {
                System.out.println(" -- Player: " + face.name());
            }
        }

        /* Read starting points for each unique water and land mass */
        inputStream.read(reusedArray, 0, 2250);

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
        byte[] fileIdBytes = new byte[2];
        inputStream.read(fileIdBytes);

        /* Verify file id */
        if (fileIdBytes[0] != 0x11 || fileIdBytes[1] != 0x27) {
            System.out.println("Warning: Invalid file id " + Utils.getHex(fileIdBytes) + " (must be 0x1127). Exiting.");

            //throw new SettlersMapLoadingException("Invalid file id " + Utils.getHex(fileIdBytes) + " (must be 0x1127). Exiting.");
        }

        /* Skip four un-used bytes */
        inputStream.read(reusedArray, 0, 4);

        if (reusedArray[0] != 0 || reusedArray[1] != 0 ||
            reusedArray[2] != 0 || reusedArray[3] != 0) {
            System.out.println("Warning: Not zeros although mandatory. Are instead " +
                                reusedArray[0] + " " +
                    reusedArray[0] + " " +
                    reusedArray[0] + " " +
                    reusedArray[0] + " ");
            /*throw new SettlersMapLoadingException("Not zeros although mandatory. Are instead " +
                    reusedArray[0] + " " +
                    reusedArray[0] + " " +
                    reusedArray[0] + " " +
                    reusedArray[0] + " ");*/
        }

        /* Extra 01 00 bytes may appear here but no files seen so far have this */
        byte[] maybe = new byte[2];

        inputStream.read(maybe);

        if (debug) {
            System.out.println("Potential filler: " + Utils.getHex(maybe));
        }

        if (maybe[0] == 1 && maybe[1] == 0) {
            if (debug) {
                System.out.println("Saw 01 00 filler. Skipping two bytes");
            }

            inputStream.read(maybe);
        }

        /* Read actual width and height, as used by map loaders */
        int newWidth = ByteBuffer.wrap(maybe).order(ByteOrder.LITTLE_ENDIAN).getChar();

        inputStream.read(maybe);
        int newHeight = ByteBuffer.wrap(maybe).order(ByteOrder.LITTLE_ENDIAN).getChar();

        if (debug) {
            System.out.println("Old width: " + mapFile.getWidth() + ", new width: " + newWidth);
            System.out.println("Old height: " + mapFile.getHeight() + ", new height: " + newHeight);
        }

        if (newWidth != mapFile.getWidth() || newHeight != mapFile.getHeight()) {
            System.out.println("Warning: Dimensions don't match. Were "
                    + mapFile.getWidth() + " x " + mapFile.getHeight() + "but now saw "
                    + newWidth + " x " + newHeight);
        }

        mapFile.setWidth(newWidth);
        mapFile.setHeight(newHeight);

        /* Read first sub block fileHeader with data about heights */
        inputStream.read(firstBlockHeader, 0, 16);

        if (debug) {
            System.out.println();
            System.out.println("Height block fileHeader");
        }

        /* Verify that the coming six bytes are: 0x 10 27 00 00 00 00 */
        byte[] MANDATORY = new byte[] {0x10, 0x27, 0, 0, 0, 0};
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
        if (mapFile.getWidth()  != Utils.getUnsignedShortInArray(firstBlockHeader, 6 + extraOffset) ||
            mapFile.getHeight() != Utils.getUnsignedShortInArray(firstBlockHeader, 8 + extraOffset)) {
            System.out.println("Mismatch in dimensions. Was "
                               + mapFile.getWidth() + " x " + mapFile.getHeight() + " but saw "
                               + Utils.getUnsignedShortInArray(firstBlockHeader, 6 + extraOffset) + " x "
                               + Utils.getUnsignedShortInArray(firstBlockHeader, 8 + extraOffset));
            throw new SettlersMapLoadingException("Mismatch in dimensions. Was "
                    + mapFile.getWidth() + " x " + mapFile.getHeight() + " but saw "
                    + Utils.getUnsignedShortInArray(firstBlockHeader, 6 + extraOffset) + " x "
                    + Utils.getUnsignedShortInArray(firstBlockHeader, 8 + extraOffset));
        }

        /* Handle fixed 01 00 if they appear */
        if (Utils.getUnsignedByteInArray(firstBlockHeader, 10 + extraOffset) == 1 &&
            Utils.getUnsignedByteInArray(firstBlockHeader, 11 + extraOffset) == 0) {

            if (debug) {
                System.out.println(" -- Ignoring fixed 01 00 (second place).");
            }

            extraOffset += 2;
        }

        if (debug) {
            System.out.println("Extra offset " + extraOffset);
        }

        long subBlockSize = Utils.getUnsignedIntInArray(firstBlockHeader, 10 + extraOffset);

        if (debug) {
            System.out.println(" -- Data size: " + (int)subBlockSize);
        }

        /* Read height map */
        inputStream.read(reusedArray, 0, (int)subBlockSize);
        ByteBuffer heightsBuffer = ByteBuffer.wrap(reusedArray).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < subBlockSize; i++) {
            SpotData spot = new SpotData();

            spot.setHeight((short)(heightsBuffer.get() & 0xff));

            mapFile.addSpot(spot);
        }

        if (debug) {
            System.out.println(" -- Loaded heights");
        }

        /* Read the second sub block with textures for up-pointing triangles */
        if (debug) {
            System.out.println();
            System.out.print("Texture block 1: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block fileHeader doesn't match the first fileHeader */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Header of block for upward triangles doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Header of block for upward triangles doesn't match. Exiting.");
        }

        inputStream.read(reusedArray, 0, (int)subBlockSize);
        for (int i = 0; i < subBlockSize; i++) {
            Texture tex = Texture.textureFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

            mapFile.getSpot(i).setTextureTriangleBelow(tex);
        }

        /* Read the third sub block fileHeader with textures for down-pointing triangles */
        if (debug) {
            System.out.println();
            System.out.print("Texture block 2: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block fileHeader doesn't match the first fileHeader */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Header of block for downward triangles doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Header of block for downward triangles doesn't match. Exiting.");
        }

        /* Read textures */
        inputStream.read(reusedArray, 0, (int)subBlockSize);
        for (int i = 0; i < subBlockSize; i++) {
            Texture tex = Texture.textureFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

            mapFile.getSpot(i).setTextureTriangleDownRight(tex);
        }

        /* Read the fourth sub block fileHeader with roads */
        if (debug) {
            System.out.println();
            System.out.print("Road block: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block fileHeader doesn't match the first fileHeader */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Header for road section doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Header for road section doesn't match. Exiting.");
        }

        /* Read roads */
        inputStream.read(reusedArray, 0, (int)subBlockSize);

        // Ignore roads for now

        /* Read the fifth sub block fileHeader with object properties */
        if (debug) {
            System.out.println();
            System.out.print("Object property block: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block fileHeader doesn't match the first fileHeader */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Object properties fileHeader doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Object properties fileHeader doesn't match. Exiting.");
        }

        /* Object properties */
        inputStream.read(reusedArray, 0, (int)subBlockSize);

        for (int i = 0; i < subBlockSize; i++) {
            mapFile.getSpot(i).setObjectProperties(Utils.getUnsignedByteInArray(reusedArray, i));
        }

        /*  Read sixth sub block header with object types */
        if (debug) {
            System.out.println();
            System.out.print("Object type block: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block header doesn't match the first header */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Object types fileHeader doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Object types fileHeader doesn't match. Exiting.");
        }

        /* Read object types*/
        inputStream.read(reusedArray, 0, (int)subBlockSize);

        for (int i = 0; i < subBlockSize; i++) {
            mapFile.getSpot(i).setObjectType(Utils.getUnsignedByteInArray(reusedArray, i));
        }

        /* Read seventh sub block header with animals */
        if (debug) {
            System.out.println();
            System.out.println("Animals block: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block header doesn't match the first header */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Animals block fileHeader doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Animals block fileHeader doesn't match. Exiting.");
        }

        /* Read animals */
        inputStream.read(reusedArray, 0, (int)subBlockSize);

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

        /* Skip eighth block with unknown data */
        if (debug) {
            System.out.println();
            System.out.print("Unknown block: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block header doesn't match the first header */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Unknown block fileHeader doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Unknown block fileHeader doesn't match. Exiting.");
        }

        /* Skip the block */
        inputStream.skip(subBlockSize);

        /* Read ninth block with buildable sites */
        if (debug) {
            System.out.println();
            System.out.print("Buildable sites block: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block fileHeader doesn't match the first fileHeader */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Buildable sites fileHeader doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Buildable sites fileHeader doesn't match. Exiting.");
        }

        /* Read the buildable sites */
        inputStream.read(reusedArray, 0, (int)subBlockSize);
        for (int i = 0; i < subBlockSize; i++) {
            BuildableSite site = BuildableSite.buildableSiteFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

            mapFile.getSpot(i).setBuildableSite(site);
        }

        /* Skip tenth block with unknown data */
        if (debug) {
            System.out.println();
            System.out.print("Second unknown block: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block header doesn't match the first header */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Second unknown block fileHeader doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Second unknown block fileHeader doesn't match. Exiting.");
        }

        /* Skip the block */
        inputStream.skip(subBlockSize);

        /* Skip eleventh block with map editor cursor position */
        if (debug) {
            System.out.println();
            System.out.print("Map editor cursor position: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block header doesn't match the first header */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Map editor cursor position block fileHeader doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Map editor cursor position block fileHeader doesn't match. Exiting.");
        }

        /* Skip the block */
        inputStream.skip(subBlockSize);

        /* Read the 12th block with resources */
        if (debug) {
            System.out.println();
            System.out.print("Resource block: ");
        }

        inputStream.read(newBlockHeader, 0, 16);

        /* Exit if the block fileHeader doesn't match the first fileHeader */
        if (!Utils.byteArraysMatch(firstBlockHeader, newBlockHeader, 16)) {
            System.out.println("Resource block fileHeader doesn't match. Exiting.");
            System.out.println("First header: " + Utils.getHex(firstBlockHeader));
            System.out.println("Current header: " + Utils.getHex(newBlockHeader));

            throw new SettlersMapLoadingException("Resource block fileHeader doesn't match. Exiting.");
        }

        /* Read the resources block */
        inputStream.read(reusedArray, 0, (int)subBlockSize);

        for (int i = 0; i < subBlockSize; i++) {
            Resource resource = Resource.resourceFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

            mapFile.getSpot(i).setResource(resource);
        }

        /* Ignore gouraud shading block */
        inputStream.skip(newBlockHeader.length);
        inputStream.skip(subBlockSize);

        /* Ignore passable areas block */
        inputStream.skip(newBlockHeader.length);
        inputStream.skip(subBlockSize);

        /* Footer, always 0xFF */

        /* Post process the map file */
        mapFile.assignPositionsToSpots();
        mapFile.adjustPointsToGameCoordinates();
        mapFile.translateFileStartingPointsToGamePoints();

        return mapFile;
    }

    private boolean isWldMode() {
        return mode.equals("wld");
    }

    public GameMap convertMapFileToGameMap(MapFile mapFile) throws Exception {

        /* Generate list of players */
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < mapFile.maxNumberOfPlayers; i++) {
            players.add(new Player("Player " + i, new Color(i*20, i*20, i*20)));
        }

        /* Create initial game map with correct dimensions */
        GameMap gameMap = new GameMap(players, mapFile.getWidth() * 2 + 2, mapFile.getHeight() + 3);

        /* Set up the terrain */
        Terrain terrain = gameMap.getTerrain();

        for (SpotData spot : mapFile.getSpots()) {

            org.appland.settlers.model.Point point = spot.getPosition();

            /* Assign textures */
            terrain.getTileBelow(point).setVegetationType(Utils.convertTextureToVegetation(spot.getTextureBelow()));
            terrain.getTileDownRight(point).setVegetationType(Utils.convertTextureToVegetation(spot.getTextureDownRight()));

            /* Set mineral quantities */
            if (spot.hasMineral()) {
                Material mineral = Utils.resourceTypeToMaterial(spot.getMineralType());

                terrain.getTileAbove(point).setAmountMineral(mineral, spot.getMineralQuantity());
            }

            /* Place stones */
            if (spot.hasStone()) {
                gameMap.placeStone(point);
            }

            /* Place trees */
            if (spot.hasTree()) {
                gameMap.placeTree(point);
            }

            /* Place wild animals */
            if (spot.hasWildAnimal()) {
                gameMap.placeWildAnimal(point);
            }

            /* Set the height */
            gameMap.setHeightAtPoint(point, spot.getHeight());
        }

        /* Set starting points */
        gameMap.setStartingPoints(mapFile.getStartingPoints());

        if (debug) {
            System.out.print(" -- Starting positions: ");
        }

        if (debug) {
            for (Point point : mapFile.getStartingPoints()) {
                System.out.print("(" + point.x + ", " + point.y + ") ");
            }
        }

        return gameMap;
    }
}
