package org.appland.settlers.maps;


import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Terrain;
import org.appland.settlers.model.Tile;
import org.appland.settlers.model.Tile.Vegetation;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Hello world!
 *
 */
public class MapLoader {

    @Option(name="--file", usage="Map file to load")
    String filename;

    public static void main(String[] args) {

        /* Parse command line and start */
        MapLoader mapLoader = new MapLoader();

        CmdLineParser parser = new CmdLineParser(mapLoader);

        try {
            parser.parseArgument(args);

            MapFile mf = mapLoader.loadMapFromFile(mapLoader.filename);
            GameMap gm = mapLoader.convertMapFileToGameMap(mf);
        } catch (Exception ex) {
            Logger.getLogger(MapLoader.class.getName()).log(Level.SEVERE, null, ex);

            System.exit(1);
        }
    }

    public MapFile loadMapFromFile(String mapFilename) {

        byte[] header = new byte[10];
        byte[] blockHeader1 = new byte[16];
        byte[] blockHeader2 = new byte[16];
        byte[] reusedArray = new byte[36864];

        MapFile mf = new MapFile();

        int width = -1;
        int height = -1;
        int actualWidth = -1;
        int actualHeight = -1;
        TerrainType terrainType;
        int numberOfPlayers = -1;
        String author;
        List<Point> tmpStartingPositions = new ArrayList<>();
        UnlimitedPlayValidity unlimitedPlay;
        List<PlayerFace> playerFaces = new ArrayList<>();
        List<UniqueMass> masses = new ArrayList<>();
        int fileId;
        List<SpotData> spotList = new ArrayList<>();

        System.out.println("Loading " + mapFilename);

        File file = new File(mapFilename);
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);

            /* Read header */
            fis.read(header);

            /* Read title */
            fis.read(reusedArray, 0, 19);
            String title = new String(reusedArray);
            System.out.println(" -- Title: " + title);
            mf.setTitle(title);

            /* Skip null terminator for title */
            fis.skip(1);

            /* Read width and height */
            byte[] printArray = new byte[4];
            fis.read(printArray, 0, 4);

            width = Utils.getUnsignedByteInArray(printArray, 0); // Only first byte seems to be significant
            height = Utils.getUnsignedByteInArray(printArray, 2); // Only first byte seems to be significant

            System.out.println(" -- Dimensions: " + width + " x " + height);

            /* Read the terrain type */
            fis.read(reusedArray, 0 ,1);
            terrainType = TerrainType.fromShort(Utils.getUnsignedByteInArray(reusedArray, 0));

            mf.setTerrainType(terrainType);

            System.out.println(" -- Terrain type: " + terrainType);

            /* Read number of players */
            fis.read(reusedArray, 0, 1);
            numberOfPlayers = Utils.getUnsignedByteInArray(reusedArray, 0);

            mf.setMaxNumberOfPlayers(numberOfPlayers);

            System.out.println(" -- Number of players: " + numberOfPlayers);

            /* Read the author */
            fis.read(reusedArray, 0, 19);
            author = new String(reusedArray);

            mf.setAuthor(author);

            System.out.println(" -- Author: " + author);

            /* Skip null terminator for the author */
            fis.skip(1);

            /* Go through x coordinates for starting positions */
            fis.read(reusedArray, 0, 14);
            for (int i = 0; i < 7; i++) {
                int x = Utils.getUnsignedByteInArray(reusedArray, i * 2); // Only the first byte seems to be significant

                if (i < numberOfPlayers) {
                    tmpStartingPositions.add(new Point(x, 0));
                }
            }

            /* Go through y coordinates for starting positions */
            fis.read(reusedArray, 0, 14);
            for (int i = 0; i < 7; i++) {
                int y = Utils.getUnsignedByteInArray(reusedArray, i * 2); // Only first byte seems to be significant

                if (i < numberOfPlayers) {
                    tmpStartingPositions.get(i).y = y;

                    mf.addStartingPosition(tmpStartingPositions.get(i));
                }
            }

            System.out.println(" -- Starting positions: " + tmpStartingPositions);

            /* Determine if the map is intended for unlimited play */
            fis.read(reusedArray, 0, 1);
            if (Utils.getUnsignedByteInArray(reusedArray, 0) == 0) {
                unlimitedPlay = UnlimitedPlayValidity.VALID;

                mf.enableUnlimitedPlay();
            } else {
                unlimitedPlay = UnlimitedPlayValidity.INVALID;

                mf.disableUnlimitedPlay();
            }

            System.out.println(" -- Unlimited play: " + unlimitedPlay.name());

            /* Read player face */
            fis.read(reusedArray, 0, 7);

            for (int i = 0; i < 7; i++) {
                if (i < numberOfPlayers) {
                    PlayerFace pf = PlayerFace.playerFaceFromShort(Utils.getUnsignedByteInArray(reusedArray, i));

                    System.out.println(" -- Player " + i + ": " + pf.name());

                    playerFaces.add(pf);
                }
            }

            mf.setPlayerFaces(playerFaces);

            /* Read starting points for each unique water and land mass */
            fis.read(reusedArray, 0, 2250);
            for (int i = 0; i < 250; i++) {

                MassType type = MassType.massTypeFromInt(Utils.getUnsignedByteInArray(reusedArray, (i * 9)));

                int x = Utils.getUnsignedByteInArray(reusedArray, (i * 9) + 1);
                int y = Utils.getUnsignedByteInArray(reusedArray, (i * 9) + 3);

                java.awt.Point position = new java.awt.Point(x, y);

                long totalMass = Utils.getUnsignedShortInArray(reusedArray, (i * 9) + 5);
                UniqueMass mass = new UniqueMass(type, position, totalMass);

                masses.add(mass);

                if (!position.equals(new java.awt.Point(0, 0))) {
                    mf.addMassStartingPoint(position);
                }
            }

            System.out.println(" -- Loaded starting points for water and land masses");

            /* Read map file identification */
            fis.read(reusedArray, 0, 2);
            fileId = Utils.getUnsignedShortInArray(reusedArray, 0);

            System.out.println(" -- File id: " + fileId + "(must be 0x 11 27)");
            /* Verify file id */

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
            } else {
                System.out.println(" -- Mandatory zeros are correct");
            }

            /* Read actual width and height, as used by map loaders */
            fis.read(reusedArray, 0, 4);

            // Skip meaningless 01 00 bytes if they are present
            if (Utils.getUnsignedByteInArray(reusedArray, 0) == 1 &&
                Utils.getUnsignedByteInArray(reusedArray, 1) == 0) {
                System.out.println(" -- Ignoring fixed 01 00 bytes (early in the file!)");
                fis.read(reusedArray, 0, 4);
            }

            actualWidth = Utils.getUnsignedByteInArray(reusedArray, 0);
            actualHeight = Utils.getUnsignedByteInArray(reusedArray, 2);

            System.out.println(" -- Actual dimensions: " + actualWidth + " x " + actualHeight);

            mf.setWidth(actualWidth);
            mf.setHeight(actualHeight);

            /* 
            
            
                Read first sub block header
            
            
            */
            fis.read(blockHeader1, 0, 16);

            System.out.println("\nHeight block header");

            System.out.println(" -- Map data id: " + Utils.getUnsignedShortInArray(blockHeader1, 0) + " (must be 10 27)");
            System.out.println(" -- Mandatory zeros: " + 
                    Utils.getUnsignedByteInArray(blockHeader1, 2) +
                    Utils.getUnsignedByteInArray(blockHeader1, 3) +
                    Utils.getUnsignedByteInArray(blockHeader1, 4) +
                    Utils.getUnsignedByteInArray(blockHeader1, 5));

            /* Handle fixed 01 00 if they appear */
            int extraOffset = 0;
            if (Utils.getUnsignedByteInArray(blockHeader1, 6) == 1 &&
                Utils.getUnsignedByteInArray(blockHeader1, 7) == 0) {
                System.out.println(" -- Ignoring fixed 01 00.");
                extraOffset = 2;
            }

            /* Verify that the dimensions remain */
            if (actualWidth != Utils.getUnsignedByteInArray(blockHeader1, 6 + extraOffset) ||
                actualHeight != Utils.getUnsignedByteInArray(blockHeader1, 8 + extraOffset)) {
                System.out.println(" -- Mismatch in dimensions. Exiting");
                System.out.println("\n\n\n\n" + Utils.getHex(blockHeader1) + "\n\n\n\n");
                System.exit(1);
            }

            /* Handle fixed 01 00 if they appear */
            if (Utils.getUnsignedByteInArray(blockHeader1, 10 + extraOffset) == 1 &&
                Utils.getUnsignedByteInArray(blockHeader1, 11 + extraOffset) == 0) {
                System.out.println(" -- Ignoring fixed 01 00.");
                extraOffset += 2;
            }

            int subBlockSize = Utils.getUnsignedShortInArray(blockHeader1, 11 + extraOffset);
            System.out.println(" -- Data size: " + subBlockSize);
        
            /* Read height map */
            fis.read(reusedArray, 0, subBlockSize);
            for (int i = 0; i < subBlockSize; i++) {
                int heightAtPoint = Utils.getUnsignedByteInArray(reusedArray, i);

                SpotData spot = new SpotData();

                spot.setHeight(heightAtPoint);

                spotList.add(spot);

                mf.addSpot(spot);
            }

            System.out.println(" -- Loaded heights");

            /*
            
            
                Read second sub block header with up-pointing triangles
            
            
            */
            System.out.print("\nTexture block 1: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Read textures */
            fis.read(reusedArray, 0, subBlockSize);
            for (int i = 0; i < subBlockSize; i++) {
                Texture tex = Texture.textureFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                spotList.get(i).setTextureTriangleBelow(tex);
            }

            /*
            
            
                Read third sub block header with down-pointing triangles
            
            
            */
            System.out.print("\nTexture block 2: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Read textures */
            fis.read(reusedArray, 0, subBlockSize);
            for (int i = 0; i < subBlockSize; i++) {
                Texture tex = Texture.textureFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                spotList.get(i).setTextureTriangleDownRight(tex);
            }

            /*
            
            
                Read fourth sub block header with roads
            
            
            */
            System.out.print("\nRoad block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Read roads */
            fis.read(reusedArray, 0, subBlockSize);

            // Ignore roads for now

            System.out.println(" -- Ignored roads.");

            /*
            
            
                Read fifth sub block header with object properties
            
            
            */
            System.out.print("\nObject property block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Object properties */
            fis.read(reusedArray, 0, subBlockSize);

            for (int i = 0; i < subBlockSize; i++) {
                spotList.get(i).setObjectProperties(Utils.getUnsignedByteInArray(reusedArray, i));
            }

            System.out.println(" -- Ignored object properties");

            /*
            
            
                Read sixth sub block header with object types
            
            
            */
            System.out.print("\nObject type block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Read object types*/
            fis.read(reusedArray, 0, subBlockSize);

            for (int i = 0; i < subBlockSize; i++) {
                spotList.get(i).setObjectType(Utils.getUnsignedByteInArray(reusedArray, i));
            }

            /*
            
            
                Read seventh sub block header with animals
            
            
            */
            System.out.print("\nAnimals block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Read animals */
            fis.read(reusedArray, 0, subBlockSize);

            for (int i = 0; i < subBlockSize; i++) {
                Animal animal = Animal.animalFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                if (animal != null) {
                    spotList.get(i).setAnimal(animal);
                }
            }

            /*
            
            
                Skip eighth block with unknown data
            
            
            */
            System.out.print("\nUnknown block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Skip the block */
            fis.skip(subBlockSize);

            /*
            
            
                Read ninth block with buildable sites
            
            
            */
            System.out.print("\nBuildable sites block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Read the buildable sites */
            fis.read(reusedArray, 0, subBlockSize);
            for (int i = 0; i < subBlockSize; i++) {
                BuildableSite site = BuildableSite.buildableSiteFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                spotList.get(i).setBuildableSite(site);
            }

            
            /*
            
            
                Skip tenth block with unknown data
            
            
            */
            System.out.print("\nUnknown block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Skip the block */
            fis.skip(subBlockSize);

            /*
            
            
                Skip eleventh block with map editor cursor position
            
            
            */
            System.out.print("\nUnknown block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Skip the block */
            fis.skip(subBlockSize);

            /*
            
            
                Read the twelveth block with resources
            
            
            */
            System.out.print("\nResource block: ");
            fis.read(blockHeader2, 0, 16);

            /* Exit if the block header doesn't match the first header */
            if (!Utils.byteArraysMatch(blockHeader1, blockHeader2, 16)) {
                System.out.println("Failed. Exiting.");
                System.out.println("\n\n\n\n\n" + Utils.getHex(blockHeader2) + "\n\n\n");
                System.exit(1);
            } else {
                System.out.println("OK");
            }

            /* Read the resources block */
            fis.read(reusedArray, 0, subBlockSize);

            for (int i = 0; i < subBlockSize; i++) {
                Resource resource = Resource.resourceFromInt(Utils.getUnsignedByteInArray(reusedArray, i));

                spotList.get(i).setResource(resource);
            }

            /* Ignore gouraud shading block */
            fis.skip(blockHeader2.length);
            fis.skip(subBlockSize);

            /* Ignore passable areas block */
            fis.skip(blockHeader2.length);
            fis.skip(subBlockSize);

            /* Footer, always FF */
        } catch (IOException ex) {
            Logger.getLogger(MapLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return mf;
    }

    public GameMap convertMapFileToGameMap(MapFile mf) throws Exception {

        /* Generate list of players */
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < mf.maxNumberOfPlayers; i++) {
            players.add(new Player("Player " + i, new Color(i*20, i*20, i*20)));
        }

        /* Create initial game map with correct dimensions */
        GameMap gm = new GameMap(players, mf.getWidth() * 2 + 1, mf.getHeight() + 2);

        /* Set up the terrain */
        Terrain terrain = gm.getTerrain();
        int row = 1;
        int index = 2;
        int nr = 1;
        for (SpotData spot : mf.getSpots()) {

/*            org.appland.settlers.model.Point p0 = new org.appland.settlers.model.Point(index - 1, row);
            org.appland.settlers.model.Point p1 = new org.appland.settlers.model.Point(index, row + 1);
            org.appland.settlers.model.Point p2 = new org.appland.settlers.model.Point(index + 1, row);
            org.appland.settlers.model.Point p3 = new org.appland.settlers.model.Point(index + 2, row + 1);
*/

            org.appland.settlers.model.Point p0 = new org.appland.settlers.model.Point(index - 1, row);
            org.appland.settlers.model.Point p1 = new org.appland.settlers.model.Point(index, row - 1);
            org.appland.settlers.model.Point p2 = new org.appland.settlers.model.Point(index + 1, row);
            org.appland.settlers.model.Point p3 = new org.appland.settlers.model.Point(index + 2, row - 1);


            Tile tileDown = terrain.getTile(p0, p1, p2);

            Vegetation vegetationDown = Utils.convertTextureToVegetation(spot.getTextureBelow());

            tileDown.setVegetationType(vegetationDown);
//            System.out.println("Set " + p0 + " " + p1 + " " + p2 + " to " + vegetationDown.name());

            Tile tileDownRight = terrain.getTile(p1, p2, p3);

            Vegetation vegetationDownRight = Utils.convertTextureToVegetation(spot.getTextureDownRight());
//            System.out.println("Set " + p1 + " " + p2 + " " + p3 + " to " + vegetationDown.name());

            tileDownRight.setVegetationType(vegetationDownRight);

            if (index == gm.getWidth() - 2) {
                index = 2;
                System.out.println("Changing at a " + nr);
                row++;
            } else if (index >= gm.getWidth() - 1) {
                index = 1;
                System.out.println("Changing at b " + nr);
                row++;
            } else {
                index += 2;
            }

            nr++;
        }

        List<org.appland.settlers.model.Point> startingPoints = new ArrayList<>();

        for (java.awt.Point p : mf.getStartingPoints()) {
            try {
                startingPoints.add(new org.appland.settlers.model.Point(p.x*2, p.y));
            } catch (Exception e) {
                startingPoints.add(new org.appland.settlers.model.Point(p.x*2, p.y + 1));
            }
        }

        gm.setStartingPoints(startingPoints);


        return gm;
    }
}
