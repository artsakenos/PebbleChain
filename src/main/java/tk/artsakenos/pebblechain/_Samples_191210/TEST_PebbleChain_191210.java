/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain._Samples_191210;

import java.util.Base64;
import tk.artsakenos.iperunits.file.FileManager;
import tk.artsakenos.pebblechain.Pebble;

/**
 * @version Sep 28, 2020
 * @author Andrea
 */
public class TEST_PebbleChain_191210 {

    public void test_01_createAndPostGenesis() throws Pebble.PebbleException {
        Pebble pebble01 = new Pebble("Pebble 01 Genesis");
        byte[] assetBytes = FileManager.getAssetBytes(this, "/u_analisys_lib/pebblechain/icoArtsakenos256.jpeg");
        String encodeToString = Base64.getEncoder().encodeToString(assetBytes);
        pebble01.setOwner("artsakenos");
        pebble01.setData(encodeToString);
        pebble01.mineBlock(); // Not valid until I don't mine it.
        System.out.println(pebble01.toJson());
        // To show the image in html src, prepend: data:image/jpeg;base64,
        // You can check the image here: https://onlinejpgtools.com/convert-base64-to-jpg

        if (pebble01.isValid()) {
            // String pastebinId = PebbleChain.pastebin_post(pebble01);
            // System.out.println("The pebble has been successfully posted with ID:" + pastebinId + ";");
        }
    }

    public void test_02_createNextPebble() {
        // Pebble pebble02 = new Pebble("");
    }

    public static void main(String[] args) throws Pebble.PebbleException {
        TEST_PebbleChain_191210 test = new TEST_PebbleChain_191210();
        test.test_01_createAndPostGenesis();

    }

}
