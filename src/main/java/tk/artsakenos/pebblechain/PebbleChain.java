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
            Pebble pastebin_get = pastebin_get(pb_code);
            if (pastebin_get != null) {
                ++depth;
                System.out.println("WE ARE AT DEPTH " + depth + " with " + pb_code);
                validateChain(pastebin_get, ++depth);
            }
        }
    }

    private static final UltraPasteBin PASTEBIN = new UltraPasteBin(UltraPasteBin.UPB_DEVELOPER_KEY_02);
    private static final UltraReddit REDDIT = CREDENTIALS_INSTANCES.getUltraReddit(1);
    // Twitter is useful for tracking purposes.
    private static final UltraTwitter TWITTER = CREDENTIALS_INSTANCES.getUltraTwitter();
    // Using Facebook API became unfeasible. Unless you're a big corporation you can not even post on your wall.
    // private static final UltraFacebook FACEBOOK = CREDENTIALS_INSTANCES.getUltraFacebook();
    // UltraSms, useful to warn on your mobile phone of a completed mining process
    // private static final UltraSms SMS = CREDENTIALS_INSTANCES.getUltraSMS();
    // Youtube, ok to store in come case, complex to read
    // Imgur like, good with steganography
    // Drive, ok, to complete

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

    public static void twitter_post(Pebble pebble, String idPastebin, String idReddit) {
        String twit = pebble.getId() + "\n"
                + "from: " + pebble.getHash_previous() + "\n"
                + "Pastebin - https://pastebin.com/" + idPastebin + ";\n"
                + "Reddit - https://www.reddit.com/" + idReddit + ";\n";
        try {
            long twitId = TWITTER.post(twit);
            System.out.println("Twitted: " + twitId);
        } catch (TwitterException ex) {
            Logger.getLogger(PebbleChain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
