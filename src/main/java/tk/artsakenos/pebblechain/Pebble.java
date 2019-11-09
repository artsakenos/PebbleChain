/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pebble implements Serializable {

    public static final String P_ALGORITHM = "SHA-256";
    public static final String P_HASHSTART = "💎THROWN#GENESIS";
    public static final String[] P_LINKSTART = new String[]{};
    public static final int P_PREFIX_MINLENGTH = 4;
    public static final int P_DATA_MAXSIZE = 10 * 1024 * 1024;

    public static final int VERSION_20191105 = 191105;

    private String hash_previous = P_HASHSTART;
    private String[] links_previous = P_LINKSTART;
    private String target = "0000";
    private int version = VERSION_20191105;
    private int depth = -1;
    private boolean verbose = true;

    private String owner; // Switch to OpenId, plus PGP.
    private String data;
    private long created_epoch;
    private String hash_current;
    private String merkel_root;
    private int nonce = 0;

    // -------------------------------------------------------------------------
    public Pebble() {
    }

    /**
     * Creates a new Pebble.
     *
     * @param owner
     * @param data
     * @throws PebbleException
     */
    public Pebble(String owner, String data) throws PebbleException {
        this(P_HASHSTART, P_LINKSTART, "0000", owner, data, null, Calendar.getInstance(Locale.getDefault()).getTimeInMillis(), VERSION_20191105, 0);
    }

    /**
     * Loads an existing Peeble
     *
     * @param hash_previous The Hash of the previous Pebble in the Chain
     * @param link_previous The link URI where to find the previous Pebble
     * @param prefix The chosen target. Lower cased hex. Its length must be >
     * P_PREFIX_MINLENGTH, and will be a proof of work giving a value to the
     * Peeble according to the target length and nonce value.
     * @param data The data of the Pebble.
     * @param hash_current The hash_current
     * @param created_epoch The epoch
     * @param nonce The nonce
     */
    public Pebble(String hash_previous, String[] links_previous, String prefix, String owner, String data,
            String hash_current, long created_epoch, int version, int nonce) throws PebbleException {
        if (!prefix.matches("-?[0-9a-f]+")) {
            throw new PebbleException(PebbleException.ET_INVALID_HEX, prefix + " is not exadecimal.");
        }
        this.created_epoch = created_epoch;
        this.hash_previous = hash_previous;
        this.links_previous = links_previous;
        this.data = data;
        this.target = prefix;
        this.hash_current = hash_current;
        this.nonce = nonce;
        this.version = version;
        this.owner = owner;
    }

    /**
     * The Header is a 264 bit (33 byte) string used to sign the block. Two
     * sub-block apart from the Markle Root, because once the version is chosen
     * they're the ones who knock the door.
     *
     * @return the block header
     */
    private String retrieveHeader() {
        return getMerkel_root() + getCreated_epoch() + getNonce();
    }

    public void mineBlock() {
        this.merkel_root = retrieveMerkelRoot(this, getVersion());
        this.setCreated_epoch(Calendar.getInstance().getTimeInMillis());
        String hashGuess = "";
        while (!hashGuess.startsWith(target)) {
            ++nonce;
            hashGuess = computeHash(P_ALGORITHM, retrieveHeader());
            if (verbose) {
                verboseMining();
            }
            // Updates timestamp time to time
            if (nonce % 1_000_000 == 0) {
                this.setCreated_epoch(Calendar.getInstance().getTimeInMillis());
            }
        }
        setHash_current(hashGuess);
    }

    private void verboseMining() {
        if (getNonce() % 10_000 == 0) {
            System.out.print(".");
        }
        if (getNonce() % 1_000_000 == 0) {
            String nf = NumberFormat.getNumberInstance(Locale.US).format(getNonce());
            // Gives an idea of how much of the integer (half) spectrum has passed
            double percentage = (double) getNonce() / (double) Integer.MAX_VALUE * 100.0;
            System.out.println(String.format(" %s; %,.2f%%", nf, percentage));
        }
    }

    /**
     * Check given rules.
     *
     * @return true if the Pebble is considered valid (rules can vary according
     * to the version).
     */
    public boolean isValid() {
        this.merkel_root = retrieveMerkelRoot(this, getVersion());
        String hash_computed = computeHash(P_ALGORITHM, retrieveHeader());
        String message = "";

        if (!hash_computed.equals(this.hash_current)) {
            message += "Hash Signature doesn't match; ";
        }

        // Check Prefix Length
        if (getTarget().length() < P_PREFIX_MINLENGTH) {
            message += "Short Prefix " + getTarget().length() + "<" + P_PREFIX_MINLENGTH + "; ";
        }

        // Check Previous Links
        if (getLinks_previous() == null) {
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

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(Pebble.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Pebble fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Pebble pebble = mapper.readValue(json, Pebble.class);
            return pebble;
        } catch (IOException ex) {
            Logger.getLogger(Pebble.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    /**
     * Compute Hash of the Data according to the seleced Algorithm, e.g.,
     * SHA256.
     *
     * @param dataToHash
     * @return Hash of the data
     */
    public final String computeHash(String algorithm, String dataToHash) {
        MessageDigest digest;
        byte[] bytes;
        try {
            digest = MessageDigest.getInstance(algorithm);
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

    /**
     * Retrieve the Merkel Root Hash to build the header, according to the
     * pebble version.
     *
     * @param pebble A pabble.
     * @param version The version.
     * @return The merkel root.
     */
    public String retrieveMerkelRoot(Pebble pebble, int version) {
        String merkel_data = null;

        if (version == VERSION_20191105) {
            String links = "";
            for (String link : pebble.getLinks_previous()) {
                links += link + "\n";
            }
            merkel_data = version + "\n"
                    + pebble.getHash_previous() + "\n"
                    /**
                     * The target is included to be "declared" before mining.
                     */
                    + pebble.getTarget() + "\n"
                    + pebble.getOwner() + "\n"
                    + links
                    /**
                     * In other version could be a transaction encrypted with
                     * the owner private key.
                     */
                    + pebble.getData() + "\n";
        }
        return computeHash(P_ALGORITHM, merkel_data);
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
     * @return the mining target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return the nonce
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * @return the data. In a secret transaction data could be encrypted
     * according to the recipient private key.
     */
    public String getData() {
        return data;
    }

    /**
     * The value of the Pebble. The price is calculated according to some
     * variables which more or less represent the calculation time. To review.
     * The nonce should be the minimum allowing the target to be reached.
     *
     * @return a block pseudo-value
     */
    public double getValue() {
        return Math.pow(10, getTarget().length() - 20) * getNonce() * retrieveHeader().length();
    }

    /**
     * Returns a Pebble Identifier
     *
     * @return a Pebble identifier
     */
    public String getId() {
        return "💎" + getTarget() + "#" + getHash_current();
    }

    /**
     * Creation date in IS8601
     *
     * @return The date created
     */
    public String getCreatedISO8601() {
        return Instant.ofEpochMilli(getCreated_epoch()).toString();
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

    /**
     * @param hash_previous the hash_previous to set
     */
    public void setHash_previous(String hash_previous) {
        this.hash_previous = hash_previous;
    }

    /**
     * @param links_previous the links_previous to set
     */
    public void setLinks_previous(String[] links_previous) {
        this.links_previous = links_previous;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the depth
     */
    public Integer getDepth() {
        if (depth < 0) {
            // Not set.
            return null;
        }
        return depth;
    }

    /**
     * @param depth the depth to set
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @param created_epoch the created_epoch to set
     */
    public void setCreated_epoch(long created_epoch) {
        this.created_epoch = created_epoch;
    }

    /**
     * @param hash_current the hash_current to set
     */
    public void setHash_current(String hash_current) {
        this.hash_current = hash_current;
    }

    /**
     * @return the merkel_root
     */
    public String getMerkel_root() {
        return merkel_root;
    }

    /**
     * @param nonce the nonce to set
     */
    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    // -------------------------------------------------------------------------
}
