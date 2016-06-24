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

    boolean hasMineral() {
        return resource != null && resource.type != null &&
               (resource.type == ResourceType.COAL     ||
                resource.type == ResourceType.GOLD     ||
                resource.type == ResourceType.IRON_ORE ||
                resource.type == ResourceType.GRANITE);
    }

    ResourceType getMineralType() {
        return resource.type;
    }

    int getMineralQuantity() {
        return resource.amount;
    }

    boolean hasStone() {
        if (objectProperties == 2 || objectProperties == 3 || objectProperties == 4) {
            return true;
        }

        return false;
    }

    boolean hasTree() {
        if (objectProperties >= 0x30 && objectProperties <= 0x37 ||
            objectProperties >= 0x70 && objectProperties <= 0x77 ||
            objectProperties >= 0xB0 && objectProperties <= 0xB7 ||
            objectProperties >= 0xF0 && objectProperties <= 0xF7) {
            return true;
        } else {
            return false;
        }
    }
}
