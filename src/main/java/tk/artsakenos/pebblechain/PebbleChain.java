/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain;

/**
 * This class contains few example on how to store pebbles on social
 * repositories, and how to navigate and validate the chain.
 *
 * There is some example facade just to give an idea. To be further improved.
 *
 * @author Andrea
 */
public class PebbleChain {

    // -------------------------------------------------------------------------
    /**
     * Naviga all'indietro recuperando i dati in base al servizio. TODO: to be
     * implemented.
     *
     * @param pebble
     */
    public void validateChain(Pebble pebble, int depth) throws PebbleException {
        String[] links_previous = pebble.getLinks_previous();
        for (String link : links_previous) {
            String pb_code = link.substring(link.lastIndexOf("/") + 1);
            System.out.println("Searching for " + pb_code);
            Pebble pastebin_get = PebbleRepos.pastebin_get(pb_code);
            if (pastebin_get != null) {
                ++depth;
                System.out.println("WE ARE AT DEPTH " + depth + " with " + pb_code);
                validateChain(pastebin_get, ++depth);
            }
        }
    }

}
