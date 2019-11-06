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
import tk.artsakenos.pebblechain.PebbleException;
import tk.artsakenos.pebblechain.PebbleRepos;
import tk.artsakenos.pebblechain.PebbleUtils;
import static tk.artsakenos.pebblechain.PebbleUtils.toJson;
import tk.artsakenos.ultraanalysis.UltraSocial.UltraReddit;

/**
 *
 * @author Andrea
 */
public class TEST_PebbleChain {

    public void test_CreatePebble01_GenesisBlock() throws PebbleException {
        String data = FileManager.getAssetString(this, "u_analysis_lib/pebblechain/helloworld.txt");
        Pebble pebbleNew = new Pebble(Pebble.P_HASHSTART, Pebble.P_LINKSTART, "caffe", "andrea", data);
        System.out.println(toJson(pebbleNew));
        SuperFileText.setText("GenesisPebble.json", toJson(pebbleNew));
    }

    public Pebble test_GetGenesisPebble() throws PebbleException {
        final String JSON = FileManager.getAssetString(this, "u_analysis_lib/pebblechain/GenesisPebble.json");
        Pebble pebble = PebbleUtils.fromJson(JSON);
        System.out.println(PebbleUtils.toJson(pebble));
        return pebble;
    }

    public Pebble test_CreatePebble02(String prefix) throws PebbleException {
        String hash_previous = "caffe58c4e0e81f1b90d40dc74e186be6744f2009eb12fbb0daddaa0863e459b";
        String[] links_previous = new String[]{
            "https://pastebin.com/mUiUB2dL",
            "https://docs.google.com/document/d/12vLMWE1PlJQiEpYz599o6OqZ4NCOO12isqGvlBtUu00"};
        String owner = "andrea";
        String data = SuperFileText.getText("./src/main/java/tk/artsakenos/pebblechain/Pebble.java");
        System.out.println("Starting with prefix, " + prefix);
        Pebble pebble = new Pebble(hash_previous, links_previous, prefix, owner, data);
        System.out.println(PebbleUtils.toJson(pebble));
        SuperFileText.setText("NextPebble_" + prefix + ".json", toJson(pebble));
        return pebble;
    }

    public void test_PostReddit() {
        Submission reddit_post = PebbleRepos.reddit_post("bricioledipane", "Titolo", "Contenuto");
        System.out.println(UltraReddit.toStringSubmission(reddit_post));
    }

    public static void main(String[] args) throws PebbleException {
        final TEST_PebbleChain test = new TEST_PebbleChain();
        String prefix = args.length < 1 ? "facade000" : args[0];
        // test.test_CreatePebble02(prefix);
        test.test_PostReddit();
    }

}
