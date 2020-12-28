/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.appland.settlers.maps;

import org.appland.settlers.model.Point;
import org.appland.settlers.model.Size;
import org.appland.settlers.model.Tree.TreeType;

/**
 *
 * @author johan
 */
class MapFilePoint {

    private static final short DEAD_TREE = 0x1F;
    private static final short NATURE_DECORATION_1 = 0xC8;
    private static final short NATURE_DECORATION_2 = 0xC9;

    private int height;
    private Texture textureDown;
    private Texture textureDownRight;
    private short objectProperties;
    private short objectType;
    private Animal animal;
    private BuildableSite buildableSite;
    private Resource resource;
    private Point position;

    void setHeight(int heightAtPoint) {
        height = heightAtPoint;
    }

    void setVegetationBelow(Texture texture) {
        textureDown = texture;
    }

    Texture getVegetationBelow() {
        return textureDown;
    }

    void setVegetationDownRight(Texture texture) {
        textureDownRight = texture;
    }

    Texture getVegetationDownRight() {
        return textureDownRight;
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

    boolean hasMineral() {
        return resource != null && resource.type != null &&
               (resource.type == ResourceType.COAL     ||
                resource.type == ResourceType.GOLD     ||
                resource.type == ResourceType.IRON_ORE ||
                resource.type == ResourceType.GRANITE) &&
                resource.amount > 0;
    }

    ResourceType getMineralType() {
        return resource.type;
    }

    Size getMineralQuantity() {

        /* Amount goes from 0 to 7. 0 means there is no more minerals
        *
        * Map 1-2 to SMALL, 3-4 to MEDIUM, 5-7 to LARGE
        *
        * */

        if (resource.amount > 4) {
            return Size.LARGE;
        } else if (resource.amount > 2) {
            return Size.MEDIUM;
        } else {
            return Size.SMALL;
        }
    }

    boolean hasStone() {
        if (objectType == 0xCC || objectType == 0xCD) {
            return true;
        }

        return false;
    }

    StoneType getStoneType() {
        if (objectType == 0xCC) {
            return StoneType.STONE_1;
        } else if (objectType == 0xCD) {
            return StoneType.STONE_2;
        }

        return null;
    }

    int getStoneAmount() {
        return objectProperties;
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

    boolean isNatureDecoration() {
        return objectType == NATURE_DECORATION_1 || objectType == NATURE_DECORATION_2;
    }

    DecorationType getNatureDecorationType() {
        return DecorationType.getDecorationTypeForUint8(objectProperties);
    }

    TreeType getTreeType() {
        if (objectType == 0xC4) {
            if (objectProperties >= 0x30 && objectProperties <= 0x37) {
                return TreeType.PINE;
            } else if (objectProperties >= 0x70 && objectProperties <= 0x77) {
                return TreeType.BIRCH;
            } else if (objectProperties >= 0xB0 && objectProperties <= 0xB7) {
                return TreeType.OAK;
            } else if (objectProperties >= 0xF0 && objectProperties <= 0xF7) {
                return TreeType.PALM_1;
            }
        } else if (objectType == 0xC5) {
            if (objectProperties >= 0x30 && objectProperties <= 0x37) {
                return TreeType.PALM_2;
            } else if (objectProperties >= 0x70 && objectProperties <= 0x77) {
                return TreeType.PINE_APPLE;
            } else if (objectProperties >= 0xB0 && objectProperties <= 0xB7) {
                return TreeType.CYPRESS;
            } else if (objectProperties >= 0xF0 && objectProperties <= 0xF7) {
                return TreeType.CHERRY;
            }
        } else if (objectType == 0xC6) {
            if (objectProperties >= 0x30 && objectProperties <= 0x3D) {
                return TreeType.FIR;
            }
        }

        return null;
    }

    public boolean hasWildAnimal() {
        return animal != null;
    }

    public void setPosition(int index, int row) {
        position = new Point(index, row);
    }

    public org.appland.settlers.model.Point getPosition() {
        return position;
    }

    public BuildableSite getBuildableSite() {
        return buildableSite;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasDeadTree() {
        return (objectType == NATURE_DECORATION_1 || objectType == NATURE_DECORATION_2) && objectProperties == DEAD_TREE;
    }
}
