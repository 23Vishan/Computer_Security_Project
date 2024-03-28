import java.io.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class SSLServer {
    static String workingDir = "C:\\Users\\visha\\Desktop\\Project";

    private static final String RST = "\u001B[0m";
    private static final String YLW = "\u001B[33m"; 

    private static SSLContext createContext() throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException, NoSuchProviderException {
        // load server keystore
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream ksData = new FileInputStream(workingDir + "\\server.jks");
        char[] ksPassword = "password".toCharArray();
        ks.load(ksData, ksPassword);

        // load key manager using server keystore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
        keyManagerFactory.init(ks, ksPassword);
        X509KeyManager x509KeyManager = null;
        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
            if (keyManager instanceof X509KeyManager) {
                x509KeyManager = (X509KeyManager) keyManager;
                break;
            }
        }

        // load client keystore
        KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream tsData = new FileInputStream(workingDir + "\\client.jks");
        char[] tsPassword = "password".toCharArray();
        ts.load(tsData, tsPassword);

        // load trust manager using client keystore
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
            // create a server socket
            SSLContext sslContext = createContext();
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(1234);
            System.out.println("Server started on port 1234.");

            // set ciphers
            String[] cipherSuites = {"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", 
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", 
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256"};
            serverSocket.setEnabledCipherSuites(cipherSuites);

            // set protocols
            String[] enabledProtocols = {"TLSv1.3", "TLSv1.2"};
            serverSocket.setEnabledProtocols(enabledProtocols);

            // accept a connection from the client
            SSLSocket socket = (SSLSocket) serverSocket.accept();

            // receive a message from the client until '\0' is received
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            List<String> encryptedFragments = new ArrayList<>();

            // read until '\0' is received
            String line;
            while ((line = in.readLine()) != null && !line.equals("\0")) {
                encryptedFragments.add(line);
            }

            // decrypt the fragments
            RecordLayerProtocol x = new RecordLayerProtocol();
            String decryptedData = x.decrypt(encryptedFragments);
            System.out.println(YLW + "\nReceived from client: \n" + RST + decryptedData);
            
            // send confirmation to the client
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.println("Survey Data Received!");
            out.flush();

            out.close();
            in.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}