package acdc.Core.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <b>Hash is a class with 2 different way of hashing with the MD5 algorithm.</b>
 *
 * <ul>
 * <li>sampleHashFile</li>
 * <p>
 * This hash has a constant complexity.
 * It takes samples at the beginning, in the middle and at the end and hash it.
 * It's quick but can output some errors.
 *
 * <li>md5ofFile</li>
 * <p>
 * This hash complexity depends on the file size.
 * It hashes all the file.
 * It's very slow but can't output errors.
 * </ul>
 *
 * @author Baptiste
 * @version 1.0
 */
public class Hash {
    private static final int SAMPLE_SIZE = 4000;

    /**
     * Hashing only three samples of the file
     *
     * @param path the path of the file to hash.
     * @return the hash of the file as a string.
     * @throws IOException              when the file is protected for example
     * @throws NoSuchAlgorithmException when the algo is not available in the environnment
     */
    public static String sampleHashFile(String path) throws IOException, NoSuchAlgorithmException {

        final long totalBytes = new java.io.File(path).length();

        try (InputStream inputStream = new FileInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);

            // if the file is too short to take 3 samples, hash the entire file
            if (totalBytes < SAMPLE_SIZE * 3) {
                byte[] bytes = new byte[(int) totalBytes];
                digestInputStream.read(bytes);
            } else {
                byte[] bytes = new byte[SAMPLE_SIZE * 3];
                long numBytesBetweenSamples = (totalBytes - SAMPLE_SIZE * 3) / 2;

                // read first, middle and last bytes
                for (int n = 0; n < 3; n++) {
                    digestInputStream.read(bytes, n * SAMPLE_SIZE, SAMPLE_SIZE);
                    digestInputStream.skip(numBytesBetweenSamples);
                }
            }
            return new BigInteger(1, digest.digest()).toString(16);
        }
    }

    /**
     * @param file the path of the file to hash.
     * @return the hash of the file as a string.
     * @throws Exception when the algo is not available in the environment.
     */
    public static String md5OfFile(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fs = new FileInputStream(file);
        BufferedInputStream bs = new BufferedInputStream(fs);
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = bs.read(buffer, 0, buffer.length)) != -1) {
            md.update(buffer, 0, bytesRead);
        }
        byte[] digest = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte bite : digest) {
            sb.append(String.format("%02x", bite & 0xff));
        }
        return sb.toString();
    }
}
