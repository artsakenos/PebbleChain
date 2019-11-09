/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain;

import com.besaba.revonline.pastebinapi.paste.PasteExpire;
import com.besaba.revonline.pastebinapi.paste.PasteVisiblity;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dean.jraw.models.Submission;
import tk.artsakenos.ultraanalysis.CREDENTIALS_INSTANCES;
import tk.artsakenos.ultraanalysis.UltraSocial.UltraPasteBin;
import tk.artsakenos.ultraanalysis.UltraSocial.UltraReddit;
import tk.artsakenos.ultraanalysis.UltraSocial.UltraTwitter;
import twitter4j.TwitterException;

/**
 * Contains few facades for some sample repositories. complete and add more. If
 * a pebble resides let's say on Reddit, GDrive, Twitter, Facebook, it is
 * already safe enough. Note that most of these social apps nowadays track
 * revisions.
 *
 * @author Andrea
 */
public class PebbleRepositories {

    private static final UltraPasteBin PASTEBIN = new UltraPasteBin(UltraPasteBin.UPB_DEVELOPER_KEY_02);
    private static final UltraReddit REDDIT = CREDENTIALS_INSTANCES.getUltraReddit(1);
    private static final UltraTwitter TWITTER = CREDENTIALS_INSTANCES.getUltraTwitter();
    // private static final UltraFacebook FACEBOOK = CREDENTIALS_INSTANCES.getUltraFacebook();
    // private static final UltraSms SMS = CREDENTIALS_INSTANCES.getUltraSMS();

    public static String brothers = "";

    // -------------------------------------------------------------------------
    /**
     * Pastebin posts can be set to expire leading to the loss of a chain block.
     *
     * @param pebble
     * @return The pastebin id, e.g., https://pastebin.com/{id}
     */
    public static String pastebin_post(Pebble pebble) {
        String pastebinId = PASTEBIN.paste(pebble.getId(), Pebble.toJson(pebble), "json", PasteVisiblity.Public, PasteExpire.Never);
        Logger.getLogger(Pebble.class.getName()).log(Level.INFO, "Posted on PasteBin with id >{0}<.", pastebinId);
        return pastebinId;
    }

    public static Pebble pastebin_get(String code) throws PebbleException {
        String jsonPebble = PASTEBIN.getRawPaste(code);
        // Pebble pebble = PebbleUtils.fromJson(jsonPebble);
        return null;
    }

    /**
     * Reddit submission has a limit of 40k charachters, to post large amount of
     * data, image posting can be exploited through steganography.
     *
     * @param subreddit
     * @param title
     * @param body
     * @return
     */
    public static Submission reddit_post(String subreddit, String title, String body) {
        if (body.length() > 40_000) {
            return null;
        }
        return REDDIT.submit(subreddit, title, body);
    }

    public static Pebble reddit_get() {
        // to bind.
        return null;
    }

    /**
     * The google drive doc id, e.g., https://docs.google.com/document/d/{id}
     *
     * @param subreddit
     * @param title
     * @param body
     * @return
     */
    public static String gdrive_post(String subreddit, String title, String body) {
        // to bind.
        return null;
    }

    public Pebble gdrive_get() {
        // to bind.
        return null;
    }

    public static void twitter_post(Pebble pebble) {
        String twit = pebble.getId() + "\n"
                + "from: " + pebble.getHash_previous() + "\n"
                + brothers;
        try {
            long twitId = TWITTER.post(twit);
            System.out.println("Twitted: " + twitId);
        } catch (TwitterException ex) {
            Logger.getLogger(PebbleRepositories.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}