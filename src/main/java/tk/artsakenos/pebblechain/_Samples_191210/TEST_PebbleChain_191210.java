/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain._Samples_191210;

import java.util.Base64;
import tk.artsakenos.iperunits.file.FileManager;
import tk.artsakenos.pebblechain.Pebble;
import tk.artsakenos.pebblechain.PebbleChain;

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
            String pastebinId = PebbleChain.pastebin_post(pebble01);
            System.out.println("The pebble has been successfully posted with ID:" + pastebinId + ";");
            // You can visit it here: https://pastebin.com/ktKHKD9p
        }
    }

    public void test_02_createNextPebble() throws Pebble.PebbleException {
        Pebble pebble02 = new Pebble(FileManager.getAssetString(this, "/u_analisys_lib/pebblechain/HelloWorld_BrainFuck.txt"));
        pebble02.setOwner("artsakenos");
        pebble02.setPreviousHash("bc0068b9179acae7737daaea0874fe498ff957ccba2f3c138b0c4cad95f26f00");
        pebble02.setPreviousLinks(new String[]{"https://pastebin.com/ktKHKD9p"});
        pebble02.mineBlock();

        if (pebble02.isValid()) {
            String pastebinId = PebbleChain.pastebin_post(pebble02);
            System.out.println("The pebble has been successfully posted with ID:" + pastebinId + ";");
            // You can visit it here: https://pastebin.com/9U9Z0R8b
        }
    }

    public void test_check_validity(String pasteBinId) throws Pebble.PebbleException {
        Pebble pebble = PebbleChain.pastebin_get(pasteBinId);
        System.out.println(pebble.toJson());
    }

    public void test_checkChainValidity() throws Pebble.PebbleException {
        int depth = 99;
        String pastebin = "https://pastebin.com/9U9Z0R8b";
        Pebble pebble = PebbleChain.loadFromUrl(pastebin);
        pebble.setDepth(depth);
        System.out.println(pebble);

        while (pebble.getPreviousLinks().length > 0) {
            pebble = PebbleChain.loadFromUrl(pebble.getPreviousLinks()[0]);
            pebble.setDepth(--depth);
            System.out.println(pebble);
        }
    }

    public static void main(String[] args) throws Pebble.PebbleException {
        TEST_PebbleChain_191210 test = new TEST_PebbleChain_191210();
        // test.test_01_createAndPostGenesis();
        // test.test_02_createNextPebble();
        test.test_checkChainValidity();
        // test.test_check_validity("ktKHKD9p");
    }

}
