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
        System.out.println(Pebble.toJson(pebble));
        SuperFileText.setText("GenesisPebble.json", Pebble.toJson(pebble));
    }

    public Pebble test_GenesisBlock() throws PebbleException {
        final String JSON = FileManager.getAssetString(this, "/u_analisys_lib/pebblechain/GenesisPebble.json");
        Pebble pebble = Pebble.fromJson(JSON);
        System.out.println(Pebble.toJson(pebble));
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

        System.out.println(Pebble.toJson(pebble));
        SuperFileText.setText("NextPebble_" + prefix + ".json", Pebble.toJson(pebble));

        String pastebin_post = PebbleChain.pastebin_post(pebble);
        Submission reddit_post = PebbleChain.reddit_post("bricioledipane", pebble.getId(), Pebble.toJson(pebble));
        // UltraFFMpeg.subs(Pebble.toJson(pebble)); // Video with subs.
        PebbleChain.twitter_post(pebble, pastebin_post, reddit_post.getId());
    }

    public void test_CreatePebble03(String prefix) throws PebbleException {

        String json = SuperFileText.getText("NextPebble_caffe2.json");
        Pebble pebble = Pebble.fromJson(json);

        String[] links_previous = new String[]{
            "https://pastebin.com/MWuqVru2",
            "https://reddit.com/r/BricioleDiPane/comments/dtarkx"};
        String owner = "andrea";
        String data = "In order to corrupt this block you need to hack:\n"
                + "* Block 0 on GDrive\n"
                + "* Block 0 on PasteBin\n"
                + "* Block 2 on PasteBin\n"
                + "* Block 2 on Reddit\n"
                + "* Block 2 on Youtube\n"
                + "And this block.\n"
                + "See more:\n"
                + "GithubPage: ...\n"
                + "InfoDev: ...\n";
        System.out.println("Starting with prefix, " + prefix);

        String hash_previous = pebble.getHash();
        pebble = new Pebble(owner, data);
        pebble.setHash_previous(hash_previous);
        pebble.setLinks_previous(links_previous);
        pebble.setTarget("caffe3");
        pebble.mineBlock();
        System.out.println(Pebble.toJson(pebble));
        SuperFileText.setText("NextPebble_" + prefix + ".json", Pebble.toJson(pebble));
    }

    public static void main(String[] args) throws PebbleException {
        final TEST_PebbleChain test = new TEST_PebbleChain();
        String prefix = args.length < 1 ? "caffe0" : args[0];
        // test.test_CreatePebble02();
        // test.postPebble();
    }

}
