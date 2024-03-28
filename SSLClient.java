import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.plaf.ColorUIResource;
import java.security.cert.CertificateException;

public class SSLClient {
    static String workingDir = "C:\\Users\\visha\\Desktop\\Project";

    private static final String RST = "\u001B[0m";
    private static final String YLW = "\u001B[33m"; 

    private static SSLContext createContext() throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException, NoSuchProviderException {
        // load client keystore
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream ksData = new FileInputStream(workingDir + "\\client.jks");
        char[] ksPassword = "password".toCharArray();
        ks.load(ksData, ksPassword);

        // load key manager using client keystore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
        keyManagerFactory.init(ks, ksPassword);
        X509KeyManager x509KeyManager = null;
        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
            if (keyManager instanceof X509KeyManager) {
                x509KeyManager = (X509KeyManager) keyManager;
                break;
            }
        }

        // load server keystore
        KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream tsData = new FileInputStream(workingDir + "\\server.jks");
        char[] tsPassword = "password".toCharArray();
        ts.load(tsData, tsPassword);

        // load trust manager using server keystore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
        trustManagerFactory.init(ts);
        X509TrustManager x509TrustManager = null;
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                x509TrustManager = (X509TrustManager) trustManager;
                break;
            }
        }

        // create SSLContext with key managers and trust managers
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(new KeyManager[]{x509KeyManager}, new TrustManager[]{x509TrustManager}, new SecureRandom());
        return sslContext;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException, NoSuchProviderException {
        try {
            // create a socket to connect to the server
            SSLContext sslContext = createContext();
            SSLSocketFactory factory = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 1234);

            // set ciphers
            String[] cipherSuites = {"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", 
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", 
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256"};
            socket.setEnabledCipherSuites(cipherSuites);

            // set protocols
            String[] enabledProtocols = {"TLSv1.3", "TLSv1.2"};
            socket.setEnabledProtocols(enabledProtocols);

            // send and receive
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // create survey UI
            SurveyUI ui = new SurveyUI();
            JFrame frame = ui.createUI();
            frame.setSize(700, 380);
            frame.setVisible(true);

            // submit button click event
            final String[] msg = new String[1];
            JButton button = ui.getSubmitButton();
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //System.out.println("Button clicked!");

                    msg[0] = ui.getSurveyData();
                    //System.out.println(msg[0]);
                }
            });

            while (true) {
                try {
                    if (msg[0] != null) {
                        System.out.println(YLW + "Survey Data: \n" + RST + msg[0]);

                        // encrypt data
                        RecordLayerProtocol x = new RecordLayerProtocol();
                        List<String> encryptedData = x.encrypt(msg[0]);

                        // send encrypted data to server
                        for (String fragment : encryptedData) {
                            if (fragment != null) {
                                //System.out.println(YLW + "Sending fragment: " + RST + fragment + "\n");
                                out.println(fragment);
                                out.flush();
                            }
                        }
        
                        // end of message
                        out.println("\0");
                        out.flush();

                        System.out.println(YLW + "Received from server: " + RST + in.readLine());
                        break;
                    }
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            out.close();
            in.close();
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}