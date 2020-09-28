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
import tk.artsakenos.ultraanalysis.UltraSocial.UltraTwitter;
import tk.artsakenos.ultraanalysis.UltraSocial.reddit.UltraReddit;
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
        String pastebinId = PASTEBIN.paste(pebble.getId(), pebble.toJson(), "json", PasteVisiblity.Public, PasteExpire.Never);
        Logger.getLogger(Pebble.class.getName()).log(Level.INFO, "Posted on PasteBin with id >{0}<.", pastebinId);
        return pastebinId;
    }

    /**
     *
     * @param pastebinId The pastebin id, e.g., https://pastebin.com/{id}
     * @return the corresponding pebble
     * @throws PebbleException
     */
    public static Pebble pastebin_get(String pastebinId) throws PebbleException {
        String jsonPebble = PASTEBIN.getRawPaste(pastebinId);
        Pebble pebble = Pebble.fromJson(jsonPebble);
        return pebble;
    }

    /**
     * Reddit submission has a limit of 40k charachters, to post larger amount
     * of data, image posting can be exploited through steganography.
     *
     * @param subreddit
     * @param title
     * @param body
     * @return
     */
    public static Submission reddit_post(String subreddit, Pebble pebble) {
        String title = pebble.getId();
        String body = pebble.toJson();

        if (body.length() > 40_000) {
            return null;
        }
        return REDDIT.submit(subreddit, title, body);
    }

    public static Pebble reddit_get(String submissionId) {
        Submission submission = REDDIT.getSubmissionById(submissionId);
        String author = submission.getAuthor();
        // String json = submission.getBody();
        String json = submission.getSelfText();
        long time = submission.getCreated().getTime();
        Pebble pebble = Pebble.fromJson(json);
        long timeDelta = (time - pebble.getCreated_epoch()) / 1000;
        Logger.getLogger(Pebble.class.getName()).log(Level.INFO, "Submission {0} from {1}, Time delta {2} seconds.", new Object[]{submissionId, author, timeDelta});
        return pebble;
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
            Logger.getLogger(Pebble.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    public Pebble loadFromUrl(String url) throws PebbleException {
        // https://pastebin.com/mUiUB2dL
        // https://reddit.com/r/BricioleDiPane/comments/dtarkx
        // https://www.reddit.com/dtarkx
        String id = url.substring(url.lastIndexOf("/") + 1);
        if (url.contains("pastebin.com/")) {
            return pastebin_get(id);
        }
        if (url.contains("reddit.com/")) {
            return reddit_get(id);
        }
        return null;
    }

    /**
     * Naviga all'indietro recuperando i dati in base al servizio. TODO: to be
     * implemented: si controlla il link, si fa una query al servizio. Tutte le
     * query sono in OR perciò il pirmo che risponde con un pebble valido
     * sicontinua all'indietro.
     *
     * @param pebble
     */
    public void validateChain(Pebble pebble, int depth) throws PebbleException {
        String[] links_previous = pebble.getLinks_previous();
        for (String link : links_previous) {
            Pebble pebble_next = loadFromUrl(link);
            if (pebble_next != null) {
                ++depth;
                Logger.getLogger(Pebble.class.getName()).log(Level.INFO, "Found a valid block at depth {0}: {1}.", new Object[]{depth, pebble});
                validateChain(pebble_next, ++depth);
                break;
            }
        }
        Logger.getLogger(Pebble.class.getName()).log(Level.INFO, "Last valid block at depth {0}: {1}.", new Object[]{depth, pebble});
    }

}
