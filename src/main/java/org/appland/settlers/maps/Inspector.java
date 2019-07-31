package org.appland.settlers.maps;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Size;
import org.jline.terminal.TerminalBuilder;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inspector {

    /* Define command line options */
    @Option(name="--info", usage ="Print information about the map")
    boolean printInfo = false;

    @Option(name="--debug", usage="Print debug information")
    boolean debug = false;

    @Option(name="--file", usage="Map file to load")
    static String mapFilename;

    @Option(name="--mode", usage="Set swd or wld file format explicitly")
    String mode = "swd";

    @Option(name="--override-width", usage="Override the detected width of the console")
    int widthOverride = -1;

    @Option(name="--override-height", usage="Override the detected height of the console")
    int heightOverride = -1;

    @Option(name="--compare-available-buildings", usage="Compares the available buildings between the map file and the game map")
    boolean compareAvailableBuildings;

    @Option(name="--point-info", handler=SettlersPointHandler.class, usage="Prints detailed information about the given point from the file and from the game map")
    java.awt.Point infoPoint = null;

    @Option(name="--print-starting-points", usage="Prints a list of the starting points")
    private boolean printStartingPoints = false;

    @Option(name="--filter-unreliable-comparisons", usage="When selected points that are close to the border or the headquarter are hidden")
    private boolean filterUnreliableComparisons = false;

    @Option(name="--render-map-file", usage="Renders an ascii representation of the map file")
    private boolean renderMapFile = false;

    @Option(name="--dump-spots", usage="Prints a list of all the spots in the map file")
    private boolean dumpSpots = false;

    /* Regular fields */
    private final MapLoader mapLoader;
    private MapFile   mapFile;
    private int       consoleHeight;
    private int       consoleWidth;
    private GameMap   map;

    /**
     * Run the inspector
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Inspector inspector = new Inspector();
        CmdLineParser parser = new CmdLineParser(inspector);

        parser.parseArgument(args);

        inspector.loadMapFile(mapFilename);

        /* Print map info if selected */
        if (inspector.isPrintInfoSelected()) {
            inspector.printMapInfo();
        }

        /* Print the map from the file */
        if (inspector.isPrintMapFromFileChosen()) {
            inspector.printMapFile();
        }

        /* Compare the available buildings */
        if (inspector.isCompareAvailableBuildingsChosen()) {
            inspector.compareAvailableBuildingPoints();
        }

        /* Print the starting points if selected */
        if (inspector.isPrintStartingPointsChosen()) {
            inspector.printStartingPointsFromGameMap();
        }

        /* Print detailed information about a given point if selected */
        if (inspector.isPrintPointInformationChosen()) {
            inspector.printPointInformation(new Point(inspector.infoPoint));
        }

        /* Dump spots */
        if (inspector.isDumpSpotsChosen()) {
            inspector.printSpotList();
        }
    }

    private void printMapInfo() {
        System.out.println();
        System.out.println("About the map:");
        System.out.println(" - Title: " + mapFile.getTitle());
        System.out.println(" - Author: " + mapFile.getAuthor());
        System.out.println(" - Width: " + mapFile.getWidth());
        System.out.println(" - Height: " + mapFile.getHeight());
        System.out.println(" - Max number of players: " + mapFile.getMaxNumberOfPlayers());
    }

    private boolean isPrintInfoSelected() {
        return printInfo;
    }

    private void printSpotList() {

        System.out.println();
        System.out.println("All spots in the map file");
        for (SpotData spot : mapFile.getSpots()) {
            Point point = spot.getPosition();

            SpotData spotLeft = mapFile.getSpotAtPoint(point.left());
            SpotData spotUpLeft = mapFile.getSpotAtPoint(point.upLeft());
            SpotData spotDownLeft = mapFile.getSpotAtPoint(point.downLeft());
            SpotData spotRight = mapFile.getSpotAtPoint(point.right());
            SpotData spotUpRight = mapFile.getSpotAtPoint(point.upRight());
            SpotData spotDownRight = mapFile.getSpotAtPoint(point.downRight());

            System.out.print(" - " + point + " available: " + spot.getBuildableSite() +
                    ", height: " + spot.getHeight() +
                    ", height differences:");

            if (spotLeft != null) {
                System.out.print(" " + (spot.getHeight() - spotLeft.getHeight()));
            }

            if (spotUpLeft != null) {
                System.out.print(" " + (spot.getHeight() - spotUpLeft.getHeight()));
            }

            if (spotDownLeft!= null) {
                System.out.print(" " + (spot.getHeight() - spotDownLeft.getHeight()));
            }

            if (spotRight != null) {
                System.out.print(" " + (spot.getHeight() - spotRight.getHeight()));
            }

            if (spotUpRight != null) {
                System.out.print(" " + (spot.getHeight() - spotUpRight.getHeight()));
            }

            if (spotDownRight!= null) {
                System.out.print(" " + (spot.getHeight() - spotDownRight.getHeight()));
            }

            System.out.println();
        }
    }

    private boolean isDumpSpotsChosen() {
        return dumpSpots;
    }

    private boolean isPrintMapFromFileChosen() {
        return renderMapFile;
    }

    /**
     * Returns true if the user has chosen to compare available buildings
     *
     * @return
     */
    private boolean isCompareAvailableBuildingsChosen() {
        return compareAvailableBuildings;
    }

    /**
     * Returns true if the user has chosen to print the starting points
     *
     * @return
     */
    private boolean isPrintStartingPointsChosen() {
        return printStartingPoints;
    }

    /**
     * Returns true if the user has chosen to print detailed information about a selected point
     *
     * @return
     */
    private boolean isPrintPointInformationChosen() {
        return infoPoint != null;
    }

    /**
     * Creates a new Inspector instance
     *
     * @throws IOException
     */
    public Inspector() throws IOException {
        mapLoader = new MapLoader();

        mapLoader.debug = debug;
        mapLoader.mode = mode;

        consoleWidth = TerminalBuilder.terminal().getWidth();
        consoleHeight = TerminalBuilder.terminal().getHeight();

        if (debug) {
            System.out.println("Detected console dimensions: " + consoleWidth + "x" + consoleHeight);
        }

        if (widthOverride != -1) {
            consoleWidth = widthOverride;
        }

        if (heightOverride != -1) {
            consoleHeight = heightOverride;
        }

        if (debug) {
            System.out.println("Using dimensions: " + consoleWidth + "x" + consoleHeight);
        }
    }

    /**
     * Loads the given file, creates a MapFile instance based on it, and converts it to a GameMap instance
     *
     * @param mapFilename
     * @throws Exception
     */
    private void loadMapFile(String mapFilename) throws Exception {
        mapFile = mapLoader.loadMapFromFile(mapFilename);
        map = mapLoader.convertMapFileToGameMap(mapFile);
    }

    /**
     * Renders the map file to stdout with the starting points highlighted
     */
    private void printMapFile() {

        String[][] mapFileRender = renderMapFileToStringArray(mapFile, mapFile.getStartingPoints());

        /* Print the render of the map file */
        StringBuilder sb = new StringBuilder();
        for (String[] row : mapFileRender) {

            int index = 0;
            for (String character : row) {

                if (index == consoleWidth) {
                    break;
                }

                index++;

                if (character == null) {
                    sb.append(" ");
                } else {
                    sb.append(character);
                }
            }
        }

        System.out.println(sb.toString());
    }

    /**
     * Prints the starting points from the GameMap instance
     */
    private void printStartingPointsFromGameMap() {

        System.out.println();
        System.out.println("Starting points: ");

        for (Point point : map.getStartingPoints()) {
            System.out.println(" - " + point);
        }
    }

    /**
     * Prints detailed information about the given point
     *
     * @param infoPoint
     */
    private void printPointInformation(Point infoPoint) {

        System.out.println();
        System.out.println("Detailed information about " + infoPoint);

        /* Print information about the point read from the MapFile */
        SpotData spot = mapFile.getSpotAtPoint(infoPoint);
        System.out.println();
        System.out.println("Map file");

        if (spot.hasTree()) {
            System.out.println(" - Tree");
        }

        if (spot.hasStone()) {
            System.out.println(" - Stone");
        }

        if (spot.getBuildableSite() != null) {
            System.out.println(" - Can build: " + spot.getBuildableSite());
        }

        System.out.println(" - Surrounding terrain:");
        System.out.println("   -- Above 1: " +
                mapFile.getSpotAtPoint(infoPoint.upLeft().upLeft()).getTextureBelow() + " " +
                mapFile.getSpotAtPoint(infoPoint.upLeft().upLeft()).getTextureDownRight() + " " +
                mapFile.getSpotAtPoint(infoPoint.up()).getTextureBelow() + " " +
                mapFile.getSpotAtPoint(infoPoint.up()).getTextureDownRight() + " " +
                mapFile.getSpotAtPoint(infoPoint.upRight().upRight()).getTextureBelow());
        System.out.println("   -- Above 2: " +
                mapFile.getSpotAtPoint(infoPoint.upLeft().left()).getTextureDownRight() + " " +
                mapFile.getSpotAtPoint(infoPoint.upLeft()).getTextureBelow() + " " +
                mapFile.getSpotAtPoint(infoPoint.upLeft()).getTextureDownRight() + " " +
                mapFile.getSpotAtPoint(infoPoint.upRight()).getTextureBelow() + " " +
                mapFile.getSpotAtPoint(infoPoint.upRight()).getTextureDownRight());
        System.out.println("   -- Below 1: " +
                mapFile.getSpotAtPoint(infoPoint.left()).getTextureBelow() + " " +
                mapFile.getSpotAtPoint(infoPoint.left()).getTextureDownRight() + " " +
                mapFile.getSpotAtPoint(infoPoint).getTextureBelow() + " " +
                mapFile.getSpotAtPoint(infoPoint).getTextureDownRight() + " " +
                mapFile.getSpotAtPoint(infoPoint.right()).getTextureBelow());
        System.out.println("   -- Below 2: " +
                mapFile.getSpotAtPoint(infoPoint.left().downLeft()).getTextureDownRight() + " " +
                mapFile.getSpotAtPoint(infoPoint.downLeft()).getTextureBelow() + " " +
                mapFile.getSpotAtPoint(infoPoint.downLeft()).getTextureDownRight() + " " +
                mapFile.getSpotAtPoint(infoPoint.downRight()).getTextureBelow() + " " +
                mapFile.getSpotAtPoint(infoPoint.downRight()).getTextureDownRight());

        System.out.println(" - Surrounding available buildings:");
        System.out.println("   -- Above 1: " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.upLeft().upLeft()).getBuildableSite()) + " " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.up()).getBuildableSite()) + " " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.upRight().upRight()).getBuildableSite()));
        System.out.println("   -- Above 2:           " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.upLeft()).getBuildableSite()) + " " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.upRight()).getBuildableSite()));
        System.out.println("   -- Same:    " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.left()).getBuildableSite()) + " " +
                String.format("%-20s", "POINT") + " " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.right()).getBuildableSite()));
        System.out.println("   -- Below 1:           " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.downLeft()).getBuildableSite()) + " " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.downRight()).getBuildableSite()));
        System.out.println("   -- Below 2: " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.downLeft().downLeft()).getBuildableSite()) + " " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.down()).getBuildableSite()) + " " +
                String.format("%-20s", mapFile.getSpotAtPoint(infoPoint.downRight().downRight()).getBuildableSite()));

        System.out.println(" - Surrounding stones and trees:");
        System.out.println("   -- Above 1: " +
                treeOrStoneOrNoneString(mapFile, infoPoint.upLeft().upLeft()) + " " +
                treeOrStoneOrNoneString(mapFile, infoPoint.up()) + " " +
                treeOrStoneOrNoneString(mapFile, (infoPoint.upRight().upRight())));
        System.out.println("   -- Above 2:    " +
                treeOrStoneOrNoneString(mapFile, infoPoint.upLeft()) + " " +
                treeOrStoneOrNoneString(mapFile, infoPoint.upRight()));
        System.out.println("   -- Same:    " +
                treeOrStoneOrNoneString(mapFile, infoPoint.left()) + " " +
                "POINT" + " " +
                treeOrStoneOrNoneString(mapFile, infoPoint.right()));
        System.out.println("   -- Below 1:    " +
                treeOrStoneOrNoneString(mapFile, infoPoint.downLeft()) + " " +
                treeOrStoneOrNoneString(mapFile, infoPoint.downRight()));
        System.out.println("   -- Below 2: " +
                treeOrStoneOrNoneString(mapFile, infoPoint.downLeft().downLeft()) + " " +
                treeOrStoneOrNoneString(mapFile, infoPoint.down()) + " " +
                treeOrStoneOrNoneString(mapFile, infoPoint.downRight().downRight()));

        System.out.println(" - Surrounding heights:");
        System.out.println("   -- Above 1: " +
                mapFile.getSpotAtPoint(infoPoint.upLeft().upLeft()).getHeight() + " " +
                        mapFile.getSpotAtPoint(infoPoint.up()).getHeight() + " " +
                        mapFile.getSpotAtPoint(infoPoint.upRight().upRight()).getHeight());
        System.out.println("   -- Above 2:   " +
                mapFile.getSpotAtPoint(infoPoint.upLeft()).getHeight() + " " +
                mapFile.getSpotAtPoint(infoPoint.upRight()).getHeight());
        System.out.println("   -- Same:    " +
                mapFile.getSpotAtPoint(infoPoint.left()).getHeight() + " " +
                mapFile.getSpotAtPoint(infoPoint).getHeight() + " " +
                mapFile.getSpotAtPoint(infoPoint.right()).getHeight());
        System.out.println("   -- Below 1:   " +
                mapFile.getSpotAtPoint(infoPoint.downLeft()).getHeight() + " " +
                mapFile.getSpotAtPoint(infoPoint.downRight()).getHeight());
        System.out.println("   -- Below 2: " +
                mapFile.getSpotAtPoint(infoPoint.downLeft().downLeft()).getHeight() + " " +
                mapFile.getSpotAtPoint(infoPoint.down()).getHeight() + " " +
                mapFile.getSpotAtPoint(infoPoint.downRight().downRight()).getHeight());

        System.out.println();


        /* Print the information about the point read from the GameMap */
        System.out.println();
        System.out.println("Game map");
        if (map.isTreeAtPoint(infoPoint)) {
            System.out.println(" - Tree");
        }

        if (spot.hasStone()) {
            System.out.println(" - Stone");
        }

        Player player = null;

        for (Player p : map.getPlayers()) {
            if (player.getLandInPoints().contains(infoPoint)) {
                player = p;

                break;
            }
        }

        Map<Point, Size> availableHousePoints = map.getAvailableHousePoints(player);
        if (player != null) {
            System.out.println(" - Can build: " + map.getAvailableHousePoints(player).get(infoPoint));
        }

        System.out.println(" - Surrounding terrain:");
        System.out.println("   -- Above 1: " +
                map.getTerrain().getTileUpLeft(infoPoint.up()) + " " +
                map.getTerrain().getTileAbove(infoPoint.up()) + " " +
                map.getTerrain().getTileUpRight(infoPoint.up()));
        System.out.println("   -- Above 2: " +
                map.getTerrain().getTileDownLeft(infoPoint.up()) + " " +
                map.getTerrain().getTileBelow(infoPoint.up()) + " " +
                map.getTerrain().getTileDownRight(infoPoint.up()));
        System.out.println("   -- Above 3: " +
                map.getTerrain().getTileUpLeft(infoPoint) + " " +
                map.getTerrain().getTileAbove(infoPoint) + " " +
                map.getTerrain().getTileUpRight(infoPoint));
        System.out.println("   -- Below 1: " +
                map.getTerrain().getTileDownLeft(infoPoint) + " " +
                map.getTerrain().getTileBelow(infoPoint) + " " +
                map.getTerrain().getTileDownRight(infoPoint));
        System.out.println("   -- Below 2: " +
                map.getTerrain().getTileUpLeft(infoPoint.down()) + " " +
                map.getTerrain().getTileAbove(infoPoint.down()) + " " +
                map.getTerrain().getTileUpRight(infoPoint.down()));
        System.out.println("   -- Below 3: " +
                map.getTerrain().getTileDownLeft(infoPoint.down()) + " " +
                map.getTerrain().getTileBelow(infoPoint.down()) + " " +
                map.getTerrain().getTileDownRight(infoPoint.down()));

        System.out.println(" - Surrounding available buildings:");
        System.out.println("   -- Above 1: " +
                String.format("%-20s", availableHousePoints.get(infoPoint.upLeft().upLeft())) + " " +
                String.format("%-20s", availableHousePoints.get(infoPoint.up())) + " " +
                String.format("%-20s", availableHousePoints.get(infoPoint.upRight().upRight())));
        System.out.println("   -- Above 2:           " +
                String.format("%-20s", availableHousePoints.get(infoPoint.upLeft())) + " " +
                String.format("%-20s", availableHousePoints.get(infoPoint.upRight())));
        System.out.println("   -- Same:    " +
                String.format("%-20s", availableHousePoints.get(infoPoint.left())) + " " +
                String.format("%-20s", "POINT") + " " +
                String.format("%-20s", availableHousePoints.get(infoPoint.right())));
        System.out.println("   -- Below 1:           " +
                String.format("%-20s", availableHousePoints.get(infoPoint.downLeft())) + " " +
                String.format("%-20s", availableHousePoints.get(infoPoint.downRight())));
        System.out.println("   -- Below 2: " +
                String.format("%-20s", availableHousePoints.get(infoPoint.downLeft().downLeft())) + " " +
                String.format("%-20s", availableHousePoints.get(infoPoint.down())) + " " +
                String.format("%-20s", availableHousePoints.get(infoPoint.downRight().downRight())));

        /* Print the closest border point */
        int distance = getDistanceToBorder(infoPoint, player);

        System.out.println(" - Distance to border: " + distance);

        /* Print the distance to the headquarter */
        System.out.println(" - Distance headquarter: " + distanceInGame(infoPoint, getHeadquarterForPlayer(player).getPosition()));

        System.out.println();
    }

    private String treeOrStoneOrNoneString(MapFile mapFile, Point point) {
        SpotData spot = mapFile.getSpotAtPoint(point);

        if (spot.hasTree()) {
            return "tree ";
        } else if (spot.hasStone()) {
            return "stone";
        } else {
            return "  x  ";
        }
    }

    /**
     * Returns the distance to the closest border point for the given point
     *
     * @param infoPoint
     * @param player
     * @return
     */
    private int getDistanceToBorder(Point infoPoint, Player player) {
        int distance = Integer.MAX_VALUE;
        for (Collection<Point> border : player.getBorders()) {
            for (Point point : border) {

                int tmpDistance = distanceInGame(point, infoPoint);

                if (tmpDistance < distance) {
                    distance = tmpDistance;
                }
            }
        }
        return distance;
    }

    /**
     * Returns the headquarter for the given player
     *
     * @param player
     * @return
     */
    private Headquarter getHeadquarterForPlayer(Player player) {
        for (Building building : player.getBuildings()) {
            if (building instanceof Headquarter) {
                return (Headquarter) building;
            }
        }

        return null;
    }

    /**
     * Returns the distance when traveling between the two points following in-game rules
     *
     * @param point
     * @param infoPoint
     * @return
     */
    private int distanceInGame(Point point, Point infoPoint) {
        int distanceY = Math.abs(infoPoint.y - point.y);
        int distanceX = Math.abs(infoPoint.x - point.x);

        int distance = distanceY;

        if (distanceX > distanceY) {
            distance += distanceX - distanceY;
        }

        return distance;
    }

    /**
     * Compared the available points to build on in a map file and what the game calculates
     *
     * @return
     * @throws Exception
     */
    private void compareAvailableBuildingPoints() throws Exception {

        System.out.println("Available starting points in MapFile and in GameMap");
        System.out.println();

        /* Use the first player to compare building points for */
        Player player = map.getPlayers().get(0);

        if (debug) {
            System.out.println("Starting point for player from MapFile: " + new Point(mapFile.getStartingPoints().get(0)));
            System.out.println("Starting point for player from GameMap: " + map.getStartingPoints().get(0));
        }

        /* Place a headquarter for the player to get the game to calculate available buildings points within the
        * border
        * */
        map.placeBuilding(new Headquarter(player), map.getStartingPoints().get(0));

        /* Compare the available building points calculated in the game with the corresponding points in the MapFile */
        Map<Point, Size> availablePoints = player.getAvailableHousePoints();

        Map<Point, AvailableBuildingComparison> matched    = new HashMap<>();
        Map<Point, AvailableBuildingComparison> mismatched = new HashMap<>();
        for (Point point : availablePoints.keySet()) {
            SpotData spot = mapFile.getSpotAtPoint(point);

            if (!availablePoints.containsKey(point)) {
                continue;
            }

            AvailableBuildingComparison comparison = new AvailableBuildingComparison(availablePoints.get(point),
                    map.isAvailableFlagPoint(player, point),
                    spot.getBuildableSite());

            /* Collect matches and mismatches */
            if (comparison.matches()) {
                matched.put(point, comparison);
            } else {
                mismatched.put(point, comparison);
            }

        }

        /* Print the matches */
        System.out.println();
        System.out.println("Matches: ");

        for (Map.Entry<Point, AvailableBuildingComparison> pointAndComparison : matched.entrySet()) {
            Point point = pointAndComparison.getKey();
            AvailableBuildingComparison comparison = pointAndComparison.getValue();

            System.out.println(" - " + point + ": " + comparison.getAvailableInMap() + ", " + comparison.getAvailableInFile());
        }

        /* Print the point that didn't match */
        Headquarter headquarter = getHeadquarterForPlayer(player);

        System.out.println();
        System.out.println("Mismatched: ");

        int filtered = 0;

        for (Map.Entry<Point, AvailableBuildingComparison> pointAndComparison : mismatched.entrySet()) {
            Point point = pointAndComparison.getKey();
            AvailableBuildingComparison comparison = pointAndComparison.getValue();

            int distanceToBorder = getDistanceToBorder(point, player);
            int distanceToHeadquarter = distanceInGame(point, headquarter.getPosition());

            /* Filter comparisons where the point is too close to the border or the headquarter */
            if (filterUnreliableComparisons && isComparisonUnreliable(distanceToBorder, distanceToHeadquarter)) {
                filtered++;

                continue;
            }

            SpotData spot = mapFile.getSpotAtPoint(point);
            SpotData spotLeft = mapFile.getSpotAtPoint(point.left());
            SpotData spotUpLeft = mapFile.getSpotAtPoint(point.upLeft());
            SpotData spotDownLeft = mapFile.getSpotAtPoint(point.downLeft());
            SpotData spotRight = mapFile.getSpotAtPoint(point.right());
            SpotData spotUpRight = mapFile.getSpotAtPoint(point.upRight());
            SpotData spotDownRight = mapFile.getSpotAtPoint(point.downRight());

            System.out.println(" - " + point + " - game: " + comparison.availableInGame + ", file: " + comparison.getAvailableInFile() +
                    ", distance to border: " + distanceToBorder +
                    ", distance to headquarter: " + distanceToHeadquarter +
                    ", height differences: " + (spot.getHeight() - spotLeft.getHeight()) +
                    ", " + (spot.getHeight() - spotUpLeft.getHeight()) +
                    ", " + (spot.getHeight() - spotUpRight.getHeight()) +
                    ", " + (spot.getHeight() - spotRight.getHeight()) +
                    ", " + (spot.getHeight() - spotDownRight.getHeight()) +
                    ", " + (spot.getHeight() - spotDownLeft.getHeight()));

            if (comparison.getAvailableInFile() == BuildableSite.OCCUPIED_BY_TREE) {

                if (!map.isTreeAtPoint(point)) {
                    System.out.println("   -- Tree in file but not on map: " + point);
                } else {
                    System.out.println("   -- Tree availableInGame on map: " /*+ map.getTreeAtPoint(point)*/);
                }
            }
        }

        System.out.println();
        System.out.println("Comparison summary:");
        System.out.println(" - Matching: " + matched.size());
        System.out.println(" - Mismatched: " + mismatched.size());
        System.out.println(" - Filtered: " + filtered);
    }

    private boolean isComparisonUnreliable(int distanceToBorder, int distanceToHeadquarter) {
        return distanceToBorder < 4 || distanceToHeadquarter < 4;
    }

    /**
     * Renders the MapFile instance to a String array. If highlights are included they will be shown on top of the
     * rendered map
     *
     * @param mapFile
     * @param highlights
     * @return
     */
    private String[][] renderMapFileToStringArray(MapFile mapFile, List<Point> highlights) {
        int maxWidth = mapFile.getWidth() * 2 + 2;
        int maxHeight = mapFile.getHeight() + 1;

        if (debug) {
            System.out.println("Width: " + maxWidth + ", height: " + maxHeight);
        }

        String[][] bfr = new String[maxHeight][maxWidth * 2];

        for (SpotData spot : mapFile.getSpots()) {

            int x = spot.getPosition().x;
            int y = spot.getPosition().y;

            /* Skip points that will not appear on screen */
            if (x >= maxWidth || y >= maxHeight) {
                continue;
            }

            /* Draw water */
            if (Texture.isWater(spot.getTextureBelow()) && y > 0) {
                bfr[y - 1][x] = " ";
            } else if (y > 0 && bfr[y - 1][x] == null) {
                bfr[y - 1][x] = ".";
            }

            if (y > 0 && x < maxWidth - 2 && Texture.isWater(spot.getTextureDownRight())) {
                bfr[y - 1][x + 1] = " ";
            } else if (y > 0 && x < maxWidth - 2 && bfr[y - 1][x + 1] == null) {
                bfr[y - 1][x + 1] = ".";
            }

            /* Place stones */
            if (spot.hasStone()) {
                bfr[y][x] = "O";
            }

            /* Place trees */
            if (spot.hasTree()) {
                bfr[y][x] = "T";
            }
        }

        if (highlights != null) {
            for (java.awt.Point highlight : highlights) {
                bfr[highlight.y - 1][highlight.x - 1] = "*";
                bfr[highlight.y - 1][highlight.x + 1] = "*";
                bfr[highlight.y + 1][highlight.x - 1] = "*";
                bfr[highlight.y + 1][highlight.x + 1] = "*";
                bfr[highlight.y + 1][highlight.x] = "*";
                bfr[highlight.y - 1][highlight.x] = "*";
                bfr[highlight.y][highlight.x - 1] = "*";
                bfr[highlight.y][highlight.x + 1] = "*";

                bfr[highlight.y][highlight.x] = "X";
            }
        }

        return bfr;
    }

    private class AvailableBuildingComparison {
        private final BuildableSite availableInFile;
        private final Size          availableInGame;
        private final boolean       availableFlagInGame;

        public AvailableBuildingComparison(Size availableInGame, boolean availableFlagInGame, BuildableSite availableInFile) {
            this.availableInGame     = availableInGame;
            this.availableFlagInGame = availableFlagInGame;
            this.availableInFile     = availableInFile;
        }

        public boolean matches() {
            return ((availableInGame == Size.LARGE  && availableInFile == BuildableSite.CASTLE) ||
                    (availableInGame == Size.MEDIUM && availableInFile == BuildableSite.HOUSE)  ||
                    (availableInGame == Size.SMALL  && availableInFile == BuildableSite.HUT)    ||
                    (availableFlagInGame && availableInGame == null &&
                            (availableInFile == BuildableSite.FLAG ||
                             availableInFile == BuildableSite.FLAG_NEXT_TO_INACCESSIBLE_TERRAIN)));
        }

        public BuildableSite getAvailableInFile() {
            return availableInFile;
        }

        public Size getAvailableInMap() {
            return availableInGame;
        }
    }
}
