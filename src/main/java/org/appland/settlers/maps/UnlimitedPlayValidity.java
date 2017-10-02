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
public enum UnlimitedPlayValidity {

    VALID(0), INVALID(1);

    private final int id;

    UnlimitedPlayValidity(int id) {
        if (id == 0) {
            this.id = id;
        } else {
            this.id = 1;
        }
    }


    public int getValue() {
        return id;
    }
}
