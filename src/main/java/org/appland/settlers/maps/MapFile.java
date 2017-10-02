/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.maps;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author johan
 */
public class MapFile {

    /* Map properties */
    private final List<Point> startingPositions;
    private final List<PlayerFace> playerFaces;
    private final List<UniqueMass> masses;
    private final List<SpotData> spotList;

    int         width;
    int         height;
    int         actualWidth;
    int         actualHeight;
    int         maxNumberOfPlayers;
    TerrainType terrainType;
    String      author;
    boolean     unlimitedPlay;
    int         fileId;
    private     String title;

    public MapFile() {
        width              = -1;
        height             = -1;
        actualWidth        = -1;
        actualHeight       = -1;
        maxNumberOfPlayers = -1;
        startingPositions  = new ArrayList<>();
        playerFaces        = new ArrayList<>();
        masses             = new ArrayList<>();
        spotList           = new ArrayList<>();
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getTitle() {
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

    void addStartingPosition(Point startingPoint) {
        startingPositions.add(startingPoint);
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

    int getWidth() {
        return width;
    }

    void setHeight(int height) {
        this.height = height;
    }

    int getHeight() {
        return height;
    }

    void addSpot(SpotData spot) {
        spotList.add(spot);
    }

    void addMassStartingPoint(Point position) {
        // Ignore for now
    }

    Iterable<SpotData> getSpots() {
        return spotList;
    }

    List<java.awt.Point> getStartingPoints() {
        return startingPositions;
    }

    int getMaxNumberOfPlayers() {
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

    public SpotData getSpot(int i) {
        return spotList.get(i);
    }
}
