import java.util.ArrayList;
import java.util.List;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import java.util.Base64;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.security.cert.CertificateException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.KeyStoreException;

public class RecordLayerProtocol {

    private static final int FRAGMENT_SIZE = 256;
    static String workingDir = "C:\\Users\\visha\\Desktop\\Project";

    private static final String RST = "\u001B[0m";
    private static final String YLW = "\u001B[33m";
    private static final String BLU = "\u001B[36m";

    public List<String> encrypt(String data) {
        List<String> fragments = new ArrayList<String>();

        try {
            // ----------------------------------------------------------------
            // FRAGMENT
            // ----------------------------------------------------------------
            for (int i = 0; i < data.length(); i += FRAGMENT_SIZE) {
                // get fragment
                String fragment = data.substring(i, Math.min(data.length(), i + FRAGMENT_SIZE));

                // add padding if shorter than FRAGMENT_SIZE
                while (fragment.length() < FRAGMENT_SIZE) {
                    fragment += " ";
                }
                fragments.add(fragment);
            }

            // for each fragment
            for (int i = 0; i < fragments.size(); i++) {
                String fragment = fragments.get(i);
                System.out.println(YLW + "Fragment of Raw Data: \n" + RST + fragment);
                
                // ----------------------------------------------------------------
                // ADD HEADER
                // ----------------------------------------------------------------
                String contentType = "survey_data";
                int length = fragment.length();
                String header = contentType + "|" + length + "|";
                fragment = header + fragment;

                // ----------------------------------------------------------------
                // ENCRYPT
                // ----------------------------------------------------------------
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

                // get public key from server's certificate
                try (FileInputStream fis = new FileInputStream(workingDir + "\\server.jks")) {
                    ks.load(fis, "password".toCharArray());

                    Certificate cert = ks.getCertificate("server");
                    PublicKey publicKey = cert.getPublicKey();

                    // initialize rsa cipher with public key
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);

                    // encrypt fragment
                    byte[] encryptedData = cipher.doFinal(fragment.getBytes(StandardCharsets.UTF_8));
                    String encryptedFragment = Base64.getEncoder().encodeToString(encryptedData);

                    // ----------------------------------------------------------------
                    // ADD MESSAGE AUTHENTICATION CODE
                    // ----------------------------------------------------------------
                    String hmacKeyString = "password";
                    byte[] hmacKeyBytes = hmacKeyString.getBytes(StandardCharsets.UTF_8);
                    SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA256");

                    // initalize mac with secret key
                    Mac mac = Mac.getInstance("HmacSHA256");
                    mac.init(secretKey);
                
                    // generate mac and convert to string
                    byte[] macBytes = mac.doFinal(encryptedFragment.getBytes(StandardCharsets.UTF_8));
                    String macStr = Base64.getEncoder().encodeToString(macBytes);
                    System.out.println(YLW + "Generated MAC: \n" + RST + macStr);
                
                    // Append the MAC to the encrypted fragment
                    encryptedFragment += "|" + macStr;
                    fragments.set(i, encryptedFragment);
                    System.out.println(BLU + "Sending Encrypted Fragment: \n" + RST + encryptedFragment + "\n");
                }
            }
        } catch (CertificateException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException | KeyStoreException e) {
            e.printStackTrace();
        }

        return fragments;
    }

    public String decrypt(List<String> encryptedFragments) {
        StringBuilder decryptedData = new StringBuilder();

        try {
            // get private key from server's keystore
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream fis = new FileInputStream(workingDir + "\\server.jks")) {
                ks.load(fis, "password".toCharArray());

                KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection("password".toCharArray());
                KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("server", protParam);
                PrivateKey privateKey = pkEntry.getPrivateKey();

                // initialize rsa cipher with private key
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);

                for (String encryptedFragment : encryptedFragments) {
                    // split the fragment into parts
                    String[] parts = encryptedFragment.split("\\|", 2);
                    String encryptedData = parts[0];
                    String receivedMac = parts[1];

                    System.out.println(YLW + "Encrypted Data: \n" + RST + encryptedData);
                    System.out.println(YLW + "Received MAC: \n" + RST + receivedMac);

                    // ----------------------------------------------------------------
                    // VERIFY MAC
                    // ----------------------------------------------------------------
                    String hmacKeyString = "password";
                    byte[] hmacKeyBytes = hmacKeyString.getBytes(StandardCharsets.UTF_8);
                    SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA256");
                
                    Mac mac = Mac.getInstance("HmacSHA256");
                    mac.init(secretKey);
                
                    byte[] computedMacBytes = mac.doFinal((encryptedData).getBytes(StandardCharsets.UTF_8));
                    String computedMac = Base64.getEncoder().encodeToString(computedMacBytes);
                    System.out.println(YLW + "Computed MAC: \n" + RST + computedMac);
                
                    // compare the computed MAC with the received MAC
                    if (!computedMac.equals(receivedMac)) {
                        throw new SecurityException("MAC verification failed");
                    }
                    else {
                        //System.out.println("MAC verification successful");
                    }
                
                    // ----------------------------------------------------------------
                    // DECRYPT
                    // ----------------------------------------------------------------
                    byte[] decodedData = Base64.getDecoder().decode(encryptedData);
                    byte[] fragmentBytes = cipher.doFinal(decodedData);
                    String fragment = new String(fragmentBytes, StandardCharsets.UTF_8);

                    // remove header
                    int headerEnd = fragment.indexOf("|", fragment.indexOf("|") + 1);
                    fragment = fragment.substring(headerEnd + 1);

                    // remove padding
                    fragment = fragment.trim();
                    decryptedData.append(fragment);
                    System.out.println(BLU + "Decrypted Data: \n" + RST + fragment + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // rebuild the original data
        return decryptedData.toString();
    }

    /*
    public static void main(String[] args) {
        List<String> encryptedFragments = encrypt("HELLO HELLO HELLO HELLO HELLO HELLO HELLO");

        // print each encrypted fragment
        for (String fragment : encryptedFragments) {
            System.out.println(fragment);
            System.out.println();
        }

        System.out.println(decrypt(encryptedFragments));
    }
    */
}