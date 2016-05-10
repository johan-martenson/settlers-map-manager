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

    int width = -1;
    int height = -1;
    int actualWidth = -1;
    int actualHeight = -1;
    TerrainType terrainType;
    int maxNumberOfPlayers = -1;
    String author;
    List<Point> startingPositions = new ArrayList<>();
    boolean unlimitedPlay;
    List<PlayerFace> playerFaces = new ArrayList<>();
    List<UniqueMass> masses = new ArrayList<>();
    int fileId;
    List<SpotData> spotList = new ArrayList<>();    
    private String title;

    void setTitle(String title) {
        this.title = title;
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
        this.playerFaces = playerFaces;
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
}
