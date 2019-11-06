/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain;

import java.io.IOException;

/**
 *
 * @author Andrea
 */
public class PebbleException extends IOException {

    public static final String ET_INVALID_HEX = "Invalid Hex";

    public PebbleException(String type, String message) {
        super("[" + type + "] " + message);
    }

}
