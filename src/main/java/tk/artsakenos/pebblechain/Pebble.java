/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * First case study of a "social repositories" based chain. A pebble of the
 * PebbleChain.
 *
 * @author Andrea
 */
public class Pebble {

    private static final String P_ALGORITHM = "SHA-256";
    public static final String P_HASHSTART = "#THROWN@GENESIS";
    public static final String[] P_LINKSTART = new String[]{};
    public static final int P_PREFIX_MINLENGTH = 4;
    public static final int P_DATA_MAXSIZE = 10 * 1024 * 1024;

    private static final int VERSION_20191105 = 191105;

    private final String hash_current;
    private final String hash_previous;
    private final long created_epoch;
    private final String[] links_previous;
    private final String prefix;
    private final String data;
    private int nonce = 0;
    private final int version;
    /**
     * The owner could be an OpenId, plus PGP credentials.
     */
    private final String owner;
    private final int depth = 0;

    // -------------------------------------------------------------------------
    private final String fixedHashData;

    private String retrieveFixedHashData() {
        String links = "";
        for (String link : links_previous) {
            links += link + "\n";
        }
        if (getVersion() == VERSION_20191105) {
            return hash_previous + "\n"
                    + getPrefix() + "\n"
                    + getOwner() + "\n"
                    + Long.toString(getCreated_epoch()) + "\n"
                    + links
                    + getData() + "\n";
        }
        return null;
    }

    /**
     * Retrieves the HashData. TODO: Separate fixed chunk to inmprove mining
     * speed. HashData is multilined for pretty printing.
     *
     * @return the HashData of this Pebble
     */
    private String retrieveHashData() {
        return fixedHashData + Integer.toString(getNonce());
    }

    // -------------------------------------------------------------------------
    /**
     * Creates a new Peeble
     *
     * @param hash_previous The Hash of the previous Pebble in the Chain
     * @param link_previous The link URI where to find the previous Pebble
     * @param prefix The chosen prefix. Its length must be > P_PREFIX_MINLENGTH,
     * and will be a proof of work giving a value to the Peeble according to the
     * prefix length and nonce value.
     * @param data The data of the Pebble.
     */
    public Pebble(String hash_previous, String[] links_previous, String prefix, String owner, String data) throws PebbleException {
        if (!prefix.matches("-?[0-9a-f]+")) {
            throw new PebbleException(PebbleException.ET_INVALID_HEX, prefix + " is not exadecimal.");
        }
        this.created_epoch = Calendar.getInstance(Locale.getDefault()).getTimeInMillis();
        this.hash_previous = hash_previous;
        this.links_previous = links_previous;
        this.data = data;
        this.prefix = prefix;
        this.version = VERSION_20191105;
        this.owner = owner;
        this.fixedHashData = retrieveFixedHashData();
        this.hash_current = mineBlock();
    }

    /**
     * Loads a Peeble
     *
     * @param hash_previous The Hash of the previous Pebble in the Chain
     * @param link_previous The link URI where to find the previous Pebble
     * @param prefix The chosen prefix. Lower cased hex. Its length must be >
     * P_PREFIX_MINLENGTH, and will be a proof of work giving a value to the
     * Peeble according to the prefix length and nonce value.
     * @param data The data of the Pebble.
     * @param hash_current The hash
     * @param created_epoch The epoch
     * @param nonce The nonce
     */
    public Pebble(String hash_previous, String[] links_previous, String prefix, String owner, String data,
            String hash_current, long created_epoch, int nonce) throws PebbleException {
        if (!prefix.matches("-?[0-9a-f]+")) {
            throw new PebbleException(PebbleException.ET_INVALID_HEX, prefix + " is not exadecimal.");
        }
        this.created_epoch = created_epoch;
        this.hash_previous = hash_previous;
        this.links_previous = links_previous;
        this.data = data;
        this.prefix = prefix;
        this.hash_current = hash_current;
        this.nonce = nonce;
        this.version = VERSION_20191105;
        this.owner = owner;
        this.fixedHashData = retrieveFixedHashData();
    }

    /**
     * Compute Hash of the Data according to the seleced Algorithm, i.e.,
     * SHA256.
     *
     * @param dataToHash
     * @return Hash of the data
     */
    private String computeHash(String dataToHash) {
        MessageDigest digest;
        byte[] bytes;
        try {
            digest = MessageDigest.getInstance(P_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Pebble.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        bytes = digest.digest(dataToHash.getBytes(UTF_8));
        StringBuilder buffer = new StringBuilder();
        for (byte b : bytes) {
            buffer.append(String.format("%02x", b));
        }
        return buffer.toString();
    }

    private String mineBlock() {
        String hash = "";
        while (!hash.startsWith(prefix)) {
            nonce++;
            hash = computeHash(retrieveHashData());
            verboseMining(); // comment if not needed.
        }
        return hash;
    }

    private void verboseMining() {
        if (nonce % 10_000 == 0) {
            System.out.print(".");
        }
        if (nonce % 1_000_000 == 0) {
            String nf = NumberFormat.getNumberInstance(Locale.US).format(nonce);
            // todo: switch to unsigned.
            double percentage = (double) nonce / (double) Integer.MAX_VALUE * 100.0;
            System.out.println(String.format(" %s; %,.2f%%", nf, percentage));
        }
    }

    /**
     * Check some rules.
     *
     * @return
     */
    public boolean isValid() {
        // Check Hash
        String hashData = retrieveHashData();
        String computeHash = computeHash(hashData);
        String message = "";

        if (!computeHash.equals(hash_current)) {
            message += "Hash Signature doesn't match; ";
        }

        // Check Prefix Length
        if (prefix.length() < P_PREFIX_MINLENGTH) {
            message += "Short Prefix " + prefix.length() + "<" + P_PREFIX_MINLENGTH + "; ";
        }

        // Check Previous Links
        if (links_previous == null) {
            message += "Previous_link can't be null, can be empty though; ";
        }

        // Check Data Length, 10MB maximum right now.
        if (getData().length() > P_DATA_MAXSIZE) {
            message += "Data packet too big, >" + P_DATA_MAXSIZE + "; ";
        }

        if (message.isEmpty()) {
            return true;
        } else {
            Logger.getLogger(Pebble.class.getName()).log(Level.WARNING, message.trim());
            return false;
        }

    }

    @Override
    public String toString() {
        return getId() + ", " + getNonce();
    }

    // -------------------------------------------------------------------------
    /**
     * @return the hash_current
     */
    public String getHash_current() {
        return hash_current;
    }

    /**
     * @return the hash_current
     */
    public String getHash_previous() {
        return hash_previous;
    }

    /**
     * @return the created_epoch
     */
    public long getCreated_epoch() {
        return created_epoch;
    }

    /**
     * @return the link_previous
     */
    public String[] getLinks_previous() {
        return links_previous;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return the nonce
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * The value of the Pebble. The price is calculated according to some
     * variables which more or less represent the calculation time.
     *
     * @return
     */
    public double getValue() {
        return Math.pow(10, prefix.length() - 15) * nonce * retrieveHashData().length();
    }

    /**
     * Returns a Pebble Identifier
     *
     * @return a Pebble identifier
     */
    public String getId() {
        return "💎" + prefix + "#" + hash_current;
    }

    /**
     * Creation date in IS8601
     *
     * @return The date created
     */
    public String getCreatedISO8601() {
        return Instant.ofEpochMilli(created_epoch).toString();
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

}
