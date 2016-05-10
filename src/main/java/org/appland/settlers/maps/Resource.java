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
class Resource {
    ResourceType type;
    int amount;

    public Resource(ResourceType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public static Resource resourceFromInt(int i) {
        ResourceType type = ResourceType.resourceTypeFromInt(i);

        int amount = 0;
        if (type == ResourceType.COAL || type == ResourceType.IRON_ORE ||
            type == ResourceType.GOLD || type == ResourceType.GRANITE) {
            amount = i;
        }

        if (type == null) {
            return null;
        }

        return new Resource(type, amount);
    }
}
