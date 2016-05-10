/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.maps;

/**
 *
 * @author johan
 */
class SpotData {

    private int height;
    private Texture textureDown;
    private Texture textureDownRight;
    private short objectProperties;
    private short objectType;
    private Animal animal;
    private BuildableSite buildableSite;
    private Resource resource;

    void setHeight(int heightAtPoint) {
        height = heightAtPoint;
    }

    void setTextureTriangleBelow(Texture tex) {
        textureDown = tex;
    }
    Texture getTextureBelow() {
        return textureDown;
    }

    void setTextureTriangleDownRight(Texture tex) {
        textureDownRight = tex;
    }

    void setObjectProperties(short unsignedByteInArray) {
        objectProperties = unsignedByteInArray;
    }

    void setObjectType(short unsignedByteInArray) {
        objectType = unsignedByteInArray;
    }

    void setAnimal(Animal animal) {
        this.animal = animal;
    }

    void setBuildableSite(BuildableSite site) {
        buildableSite = site;
    }

    void setResource(Resource resource) {
        this.resource = resource;
    }

    Texture getTextureDownRight() {
        return textureDownRight;
    }
}
