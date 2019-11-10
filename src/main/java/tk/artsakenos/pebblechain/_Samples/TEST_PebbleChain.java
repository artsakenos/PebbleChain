/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain._Samples;

import net.dean.jraw.models.Submission;
import tk.artsakenos.iperunits.file.FileManager;
import tk.artsakenos.iperunits.file.SuperFileText;
import tk.artsakenos.pebblechain.Pebble;
import tk.artsakenos.pebblechain.PebbleChain;
import tk.artsakenos.pebblechain.PebbleException;

/**
 *
 * @author Andrea
 */
public class TEST_PebbleChain {

    public void test_CreateGenesisBlock() throws PebbleException {
        String data = FileManager.getAssetString(this, "/u_analisys_lib/pebblechain/HelloWorld.txt");
        Pebble pebble = new Pebble("andrea", data);
        pebble.mineBlock();
        SuperFileText.setText("GenesisPebble.json", pebble.toJson());
    }

    public Pebble test_LoadGenesisBlock() throws PebbleException {
        final String JSON = FileManager.getAssetString(this, "/u_analisys_lib/pebblechain/GenesisPebble.json");
        Pebble pebble = Pebble.fromJson(JSON);
        System.out.println(pebble.toJson());
        return pebble;
    }

    public void test_CreatePebble02() throws PebbleException {
        String prefix = "caffe2";
        String hash_previous = "caffe58c4e0e81f1b90d40dc74e186be6744f2009eb12fbb0daddaa0863e459b";
        String[] links_previous = new String[]{
            "https://pastebin.com/mUiUB2dL",
            "https://docs.google.com/document/d/12vLMWE1PlJQiEpYz599o6OqZ4NCOO12isqGvlBtUu00"};
        String owner = "andrea";
        String data = SuperFileText.getText("./src/main/java/tk/artsakenos/pebblechain/Pebble.java");

        System.out.println("Mining block with prefix: " + prefix);
        Pebble pebble = new Pebble(owner, data);
        pebble.setHash_previous(hash_previous);
        pebble.setLinks_previous(links_previous);
        pebble.setTarget(prefix);
        pebble.mineBlock();

        System.out.println(pebble.toJson());
        SuperFileText.setText("NextPebble_" + prefix + ".json", pebble.toJson());

        String pastebin_post = PebbleChain.pastebin_post(pebble);
        Submission reddit_post = PebbleChain.reddit_post("bricioledipane", pebble);
        // UltraFFMpeg.subs(Pebble.toJson(pebble)); // Video with subs.
        PebbleChain.twitter_post(pebble, pastebin_post, reddit_post.getId());
    }

    public void test_CreatePebble03() throws PebbleException {

        String[] links_previous = new String[]{
            "https://pastebin.com/MWuqVru2",
            "https://reddit.com/dtarkx"};
        String owner = "andrea";
        String data = "In order to corrupt this block you need to hack:\n"
                + "* Block 0 on GDrive      - https://docs.google.com/document/d/12vLMWE1PlJQiEpYz599o6OqZ4NCOO12isqGvlBtUu00\n"
                + "* Block 0 on PasteBin    - https://pastebin.com/mUiUB2dL\n"
                + "* Block 2 on PasteBin    - https://pastebin.com/MWuqVru2\n"
                + "* Block 2 on Reddit      - https://www.reddit.com/dtarkx\n"
                + "* Block 2 on Youtube     - https://youtu.be/VusDvPl8OJo (experimental)\n"
                + "And this block, see the twit https://twitter.com/artsakenos/status/1192677802094428160.\n"
                + "See more:\n"
                + "Github: https://github.com/artsakenos/PebbleChain\n"
                + "InfoDev: https://infodev.wordpress.com/2019/10/15/pebble-chain\n";

        Pebble pebble = new Pebble(owner, data);
        pebble.setHash_previous("caffe2544a6d12722d04ecc24006d9b3339e424723e97bde783d32e7056146c7");
        pebble.setLinks_previous(links_previous);
        pebble.setTarget("caffe3");
        pebble.mineBlock();
        SuperFileText.setText("NextPebble_" + pebble.getTarget() + ".json", pebble.toJson());

        String idPastebin = PebbleChain.pastebin_post(pebble);
        String idReddit = PebbleChain.reddit_post("bricioledipane", pebble).getId();
        PebbleChain.twitter_post(pebble, idPastebin, idReddit);
    }

    public static void main(String[] args) throws PebbleException {
        final TEST_PebbleChain test = new TEST_PebbleChain();
        String prefix = args.length < 1 ? "caffe00" : args[0];
    }

}
