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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A block of a "social targeted" BlockChain. A pebble of the PebbleChain.
 *
 * @author artsakenos
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pebble implements Serializable {

    public static final String P_ALGORITHM = "SHA-256";
    public static final String P_START_HASH = "💎THROWN#GENESIS";
    public static final String[] P_START_LINKS = new String[]{};
    public static final String P_START_TARGET = "bc00";
    public static final int P_PREFIX_MINLENGTH = 4;
    public static final int P_DATA_MAXSIZE = 10 * 1024 * 1024;

    // Pebble Properties
    public static final int VERSION_20191105 = 191105;
    public static final int VERSION_20191210 = 191210;

    private String previousHash = P_START_HASH;
    private String[] previousLinks = P_START_LINKS;
    private String target = P_START_TARGET;
    private int version = VERSION_20191105;
    private int depth = -1;
    private boolean verbose = true;

    private String owner;   // Can exploit OpenId, plus PGP.
    private String data;    // Can be a BASE64 representation of an object.
    private long created;
    private String hash;
    private String merkle;
    private int nonce = 0;

    // -------------------------------------------------------------------------
    public Pebble() {
    }

    /**
     * Creates a new Pebble.
     *
     * @param data The Data. Can be a BASE64 representation of an Object.
     * @throws PebbleException The PebbleException.
     */
    public Pebble(String data) throws PebbleException {
        this(P_START_HASH, P_START_LINKS, P_START_TARGET, P_START_TARGET, data, null, Calendar.getInstance(Locale.getDefault()).getTimeInMillis(), VERSION_20191210, 0);
    }

    /**
     * Loads an existing Pebble.
     *
     * @param previousHash The Hash of the previous Pebble in the Chain (null if
     * genesis)
     * @param previousLinks The link URI where to find the previous Pebbles
     * @param prefix The chosen target. Lower cased hex. Its length must be >
     * P_PREFIX_MINLENGTH, and will be a proof of work giving a value to the
     * Pebble according to the target length and nonce value.
     * @param data The data of the Pebble.
     * @param hash The hash.
     * @param created_epoch The epoch.
     * @param nonce The nonce.
     */
    public Pebble(String previousHash, String[] previousLinks, String prefix, String owner, String data,
            String hash, long created_epoch, int version, int nonce) throws PebbleException {
        if (!prefix.matches("-?[0-9a-f]+")) {
            throw new PebbleException(PebbleException.ET_INVALID_HEX, prefix + " is not exadecimal.");
        }
        this.created = created_epoch;
        this.previousHash = previousHash;
        this.previousLinks = previousLinks;
        this.data = data;
        this.target = prefix;
        this.hash = hash;
        this.nonce = nonce;
        this.version = version;
        this.owner = owner;

        if (hash != null && !isValid()) {
            throw new PebbleException(PebbleException.ET_INVALID_HEX, "Hash is not valid.");
        }
    }

    /**
     * The Header is a 320 bit (40 byte) string used to sign the block. Two
     * sub-block apart from the Merkle Root, because once the version is chosen
     * they're the ones who knock the door.
     *
     * @return the block header
     */
    private String retrieveHeader() {
        return getMerkle() + getCreated() + getNonce();
    }

    /**
     * Mines the block, making it valid. Sets its Merkle.
     */
    public void mineBlock() {
        this.merkle = retrieveMerkleRoot();
        this.setCreated(Calendar.getInstance().getTimeInMillis());
        String hashGuess = "";
        while (!hashGuess.startsWith(target)) {
            ++nonce;
            hashGuess = computeHash(P_ALGORITHM, retrieveHeader());
            if (verbose) {
                verboseMining();
            }
            // Updates timestamp time to time
            if (nonce % 1_000_000 == 0) {
                this.setCreated(Calendar.getInstance().getTimeInMillis());
            }
        }
        setHash(hashGuess);
    }

    /**
     * Shows the progress of the mining process.
     */
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
     * Check if a Pebble is valid according to its version.
     *
     * @return true if the Pebble is considered valid (rules can vary according
     * to the version).
     */
    public final boolean isValid() {
        this.merkle = retrieveMerkleRoot();
        String hash_computed = computeHash(P_ALGORITHM, retrieveHeader());
        String message = "";

        if (!hash_computed.equals(this.hash)) {
            message += "Hash Signature doesn't match; ";
        }

        // Check Prefix Length
        if (getTarget().length() < P_PREFIX_MINLENGTH) {
            message += "Short Prefix " + getTarget().length() + "<" + P_PREFIX_MINLENGTH + "; ";
        }

        // Check Previous Links
        if (getPreviousLinks() == null) {
            message += "Previous_link can't be null, can be empty though; ";
        }

        if (retrieveMerkleRoot() == null) {
            message += "Merkle Root is null; ";
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
        return String.format("[%s] Lev:%d; PrevHash:%s; PrevLink:%s; [%s]",
                getId(),
                getDepth(),
                getPreviousHash(),
                Arrays.toString(getPreviousLinks()),
                isValid() ? "valid" : "invalid");
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
     * Compute Hash of the Data according to the selected Algorithm, e.g.,
     * SHA256.
     *
     * @param dataToHash The Data
     * @return Hash of the data The Corresponding Hash
     */
    public final String computeHash(String algorithm, String dataToHash) {

        if (algorithm == null || dataToHash == null) {
            Logger.getLogger(Pebble.class.getName()).log(Level.SEVERE, "Algorithm and Data can't be null in computeHash(...)");
            return null;
        }

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
     * Retrieve the Merkle Root Hash to build the header, according to the
     * pebble version.
     *
     * @param pebble A pebble.
     * @return The Merkle root.
     */
    private String retrieveMerkleRoot() {
        String merkleData = null;

        if (version == VERSION_20191105 || version == VERSION_20191210) {
            String links = "";
            for (String link : getPreviousLinks()) {
                links += link + "\n";
            }
            merkleData
                    = getVersion() + "\n"
                    + getPreviousHash() + "\n"
                    /**
                     * The target is included to be "declared" before mining.
                     */
                    + getTarget() + "\n"
                    + getOwner() + "\n"
                    + links
                    /**
                     * In other version could be a transaction encrypted with
                     * the owner private key.
                     */
                    + getData() + "\n";
        }

        if (merkleData == null) {
            Logger.getLogger(Pebble.class.getName()).log(Level.SEVERE, "Error: no Merkle with version {0}.", version);
            return null;
        }

        return computeHash(P_ALGORITHM, merkleData);
    }

    // -------------------------------------------------------------------------
    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @return the hash
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /**
     * @return the created
     */
    public long getCreated() {
        return created;
    }

    /**
     * @return the link_previous
     */
    public String[] getPreviousLinks() {
        return previousLinks;
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
     * The value of the Pebble. The price is calculated according to an
     * opinionated view taking into account the calculation time.
     *
     * In this case, The nonce should be the minimum allowing the target to be
     * reached. Which is not if you selected it randomly, or if you reset it
     * during the computation.
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
        return "💎" + getTarget() + "#" + getHash();
    }

    /**
     * Creation date in IS8601
     *
     * @return the date created in Milliseconds Epoch Time
     */
    public String getCreatedISO8601() {
        return Instant.ofEpochMilli(getCreated()).toString();
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @return the owner of the Pebble
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param previousHash the previousHash to set
     */
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    /**
     * @param previousLinks the previousLinks to set
     */
    public void setPreviousLinks(String[] previousLinks) {
        this.previousLinks = previousLinks;
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
     * @param created the created to set
     */
    public void setCreated(long created) {
        this.created = created;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * @return the Merkle
     */
    public String getMerkle() {
        return merkle;
    }

    /**
     * @param nonce the nonce to set
     */
    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    // -------------------------------------------------------------------------
    /**
     * A PebbleException.
     *
     * @author artsakenos
     */
    public class PebbleException extends IOException {

        public static final String ET_INVALID_HEX = "Invalid Hex";

        public PebbleException(String type, String message) {
            super("[" + type + "] " + message);
        }

    }
}
