/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.maps;

import org.appland.settlers.model.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author johan
 */
public class MapFile {

    /* Map properties */
    private final List<Point> startingPositions;
    private final List<PlayerFace> playerFaces;
    private final List<UniqueMass> masses;
    private final List<MapFilePoint> pointList;

    int         width;
    int         height;
    int         maxNumberOfPlayers;
    TerrainType terrainType;
    String      author;
    boolean     unlimitedPlay;

    private     String title;
    private     Map<Point, MapFilePoint> gamePointToMapFilePointMap;
    private     Map<java.awt.Point, MapFilePoint> mapFilePointToGamePointMap;
    private final List<java.awt.Point> fileStartingPoints;
    private     MapTitleType mapTitleType;

    public MapFile() {
        width              = -1;
        height             = -1;
        maxNumberOfPlayers = -1;
        startingPositions  = new ArrayList<>();
        playerFaces        = new ArrayList<>();
        masses             = new ArrayList<>();
        pointList = new ArrayList<>();
        fileStartingPoints = new ArrayList<>();
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    void setMaxNumberOfPlayers(int numberOfPlayers) {
        this.maxNumberOfPlayers = numberOfPlayers;
    }

    void setAuthor(String author) {
        this.author = author;
    }

    void addStartingPosition(java.awt.Point startingPoint) {
        fileStartingPoints.add(startingPoint);
    }

    void enableUnlimitedPlay() {
        unlimitedPlay = true;
    }

    void disableUnlimitedPlay() {
        unlimitedPlay = false;
    }

    void setPlayerFaces(List<PlayerFace> playerFaces) {
        this.playerFaces.clear();

        this.playerFaces.addAll(playerFaces);
    }

    void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    void addSpot(MapFilePoint spot) {
        pointList.add(spot);
    }

    void addMassStartingPoint(java.awt.Point position) {
        // Ignore for now
    }

    Iterable<MapFilePoint> getMapFilePoints() {
        return pointList;
    }

    public List<Point> getStartingPoints() {
        return startingPositions;
    }

    public int getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }

    TerrainType getTerrainType() {
        return terrainType;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isPlayUnlimited() {
        return unlimitedPlay;
    }

    public List<PlayerFace> getPlayerFaces() {
        return playerFaces;
    }

    public MapFilePoint getSpot(int i) {
        return pointList.get(i);
    }

    /**
     * Assign positions to the spots
     *
     * The spots in the map file are saved according to the pattern:
     *
     *   00  01  02  03
     * 04  05  06  07
     *   08  09  0A  0B
     * 0C  0D  0E  0F
     *
     * While points in the game are structured as:
     *
     *
     * 0,2       2, 2
     *      1,1       3,1
     * 0,0       2,0
     *
     * The starting points read from the file start with y as 0 on the top row and then increases downwards. X is the
     * number of data points in the file on the current row. This means that y is increasing while the in-game y is
     * ascending and x is half the in-game x.
     *
     * Unknown: does y and x start at 0 or 1?
     *
     * Starting point in file: 3, 4
     *
     *   00  01  02  03
     * 04  05  06  07
     *   08  09  0A  **   <----- Starting point
     * 0C  0D  0E  0F
     *
     *
     */
    void assignPositionsToSpots() {

        int y = height + 1;
        int yInFile = 0;
        int x = 1;

        int xInFile = 1;
        boolean nextIsInset = true;

        mapFilePointToGamePointMap = new HashMap<>();

        for (MapFilePoint spot : pointList) {

            spot.setPosition(x, y);
            mapFilePointToGamePointMap.put(new java.awt.Point(xInFile, yInFile), spot);

            if (xInFile == width) {

                if (nextIsInset) {
                    x = 2;
                } else {
                    x = 1;
                }

                y--;
                yInFile++;

                nextIsInset = !nextIsInset;
                xInFile = 1;
            } else {
                x += 2;
                xInFile++;
            }
        }
    }

    void translateFileStartingPointsToGamePoints() throws InvalidMapException {

        // filePointToSpots.get(point) == null (?)

        for (java.awt.Point point : fileStartingPoints) {

            /* Filter invalid starting points - this can exist e.g. on mission maps */
            if (!mapFilePointToGamePointMap.containsKey(point)) {
                continue;
            }

            if (point == null || mapFilePointToGamePointMap == null || mapFilePointToGamePointMap.get(point) == null) {
                System.out.println(point);
                System.out.println(startingPositions);
                System.out.println(mapFilePointToGamePointMap);
                System.out.println(mapFilePointToGamePointMap.get(point));
            }

            MapFilePoint spot = mapFilePointToGamePointMap.get(point);

            if (spot == null) {
                throw new InvalidMapException("The starting point " + point + " is outside of the map.");
            }

            startingPositions.add(new org.appland.settlers.model.Point(spot.getPosition()));
        }
    }

    public void adjustPointsToGameCoordinates() {

        gamePointToMapFilePointMap = new HashMap<>();

        for (MapFilePoint spot : pointList) {
            gamePointToMapFilePointMap.put(new org.appland.settlers.model.Point(spot.getPosition()), spot);
        }
    }

    public MapFilePoint getMapFilePoint(Point point) {
        return gamePointToMapFilePointMap.get(point);
    }

    public void setTitleType(MapTitleType titleType) {
        this.mapTitleType = titleType;
    }

    public String getTitleType() {
        return mapTitleType.name();
    }
}
