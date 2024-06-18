import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import Keyring.Keyring;

public class EncryptionHandler {
    private Keyring keyring;
    private Key arc4Key;
    

    public EncryptionHandler() {
        keyring = new Keyring("keys", "security");
        checkKeyExistenceAndOrGenerate();
    }

    public boolean performRSAHandshake(DataInputStream server_in, DataOutputStream out) {
        try {
            PublicKey client_pubkey = keyring.getClientKeys("networking").getPublic();
            boolean server_pubkey_exists = keyring.findEntry("server_public");

            if (!server_pubkey_exists) {
                initiateKeyRetrieveHandshake(client_pubkey, server_in, out);
            } else {
                initiateKeyPassHandshake(client_pubkey, server_in, out);
            }

            return completeKeyExchange(server_in, out);
        } catch (Exception e) {
            System.err.println("Error during handshake: " + e);
            return false;
        }
    }

    private void checkKeyExistenceAndOrGenerate() {
        boolean client_pub = keyring.findEntry("client_public");
        boolean client_priv = keyring.findEntry("client_private");

        if (!client_pub || !client_priv) {
            try {
                // Generates 2048 size Key Pair
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair pair = generator.generateKeyPair();

                // Creates SecretKeySpec objects of public and private keys by getting the respective key's bytes (.getEncoded()) for the object
                SecretKeySpec privateKey = new SecretKeySpec(pair.getPrivate().getEncoded(), "RSA");
                SecretKeySpec publicKey = new SecretKeySpec(pair.getPublic().getEncoded(), "RSA");

                keyring.saveKey("client_public", publicKey, "networking");
                keyring.saveKey("client_private", privateKey, "networking");
            } catch (NoSuchAlgorithmException nsae) {
                System.err.println("Error in generating client RSA keys: " + nsae);
            }
        }
        System.out.println("Keys Clear");
    }

    private void initiateKeyRetrieveHandshake(PublicKey client_pubkey, DataInputStream server_in, DataOutputStream out) throws Exception {
        sendData("key_retrieve_handshake_initiate".getBytes(), out);

        String incoming_message = new String(readData(server_in));
        if (!"sending_receiving_keys".equals(incoming_message.trim())) {
            throw new Exception("Invalid handshake response: " + incoming_message);
        }
        System.out.println("Established Connection");

        byte[] server_pubkey_data = readData(server_in);
        SecretKeySpec server_pubkey = new SecretKeySpec(server_pubkey_data, "RSA");
        keyring.saveKey("server_public", (SecretKey)server_pubkey, "networking");

        byte[] client_pubkey_data = client_pubkey.getEncoded();
        sendData(client_pubkey_data, out);
    }
    
    private void initiateKeyPassHandshake(PublicKey client_pubkey, DataInputStream server_in, DataOutputStream out) throws Exception {
        sendData("key_pass_handshake_initiate".getBytes(), out);

        String incoming_message = new String(readData(server_in));
        if (!"receiving_sending_keys".equals(incoming_message.trim())) {
            throw new Exception("Invalid handshake response: " + incoming_message);
        }
        System.out.println("Established Connection");

        byte[] server_pubkey_data = keyring.getKey("server_public", "networking").getEncoded();
        sendData(server_pubkey_data, out);

        String confirmation = new String(readData(server_in));
        if ("incorrect_key".equals(confirmation)) {
            handleIncorrectKey(server_in, out);
        } else if (!"correct_key".equals(confirmation)) {
            throw new Exception("Unexpected response when checking key data");
        }

        byte[] client_pubkeyData = client_pubkey.getEncoded();
        sendData(client_pubkeyData, out);
    }

    private void handleIncorrectKey(DataInputStream server_in, DataOutputStream out) throws Exception {
        sendData("send_replacement_key".getBytes(), out);

        byte[] new_server_pubkey_data = readData(server_in);
        SecretKeySpec new_server_pubkey = new SecretKeySpec(new_server_pubkey_data, "RSA");
        keyring.removeEntry("server_public");
        keyring.saveKey("server_public", (SecretKey)new_server_pubkey, "networking");
    }

    private boolean completeKeyExchange(DataInputStream server_in, DataOutputStream out) throws Exception {
        byte[] encodedKeyBytes = readData(server_in);

        Cipher decryptRsaCipher = Cipher.getInstance("RSA");
        decryptRsaCipher.init(Cipher.DECRYPT_MODE, keyring.getClientKeys("networking").getPrivate());
        byte[] arc4KeyBytes = decryptRsaCipher.doFinal(encodedKeyBytes);

        arc4Key = new SecretKeySpec(arc4KeyBytes, "ARCFOUR");
        sendMessage("Key Received", out);

        String message = receiveMessage(server_in);

        if (!"Key Exchange Complete".equals(message.trim())) {
            throw new Exception("Unexpected response when completing key exhange");
        }

        return true;
    }

    public void checkUserId(DataInputStream server_in, DataOutputStream out) {
        String message = receiveMessage(server_in);
        if (!"uuid_check".equals(message.trim())) {
            System.err.println("Incorrect incoming message when trying to check UUID");
            return;
        }
        
        boolean uuid_existence = keyring.findEntry("uuid");

        if (uuid_existence) {
            uuidExists(server_in, out);
        } else {
            uuidNeeded(server_in, out);
        }
        System.out.println("UUID Checked");
    }

    private void uuidExists(DataInputStream server_in, DataOutputStream out) {
        sendMessage("yes_uuid", out);

        String message = receiveMessage(server_in);
        
        if (!"send_uuid".equals(message.trim())) {
            System.err.println("Unexpected message when trying to send UUID");
            return;
        }

        sendMessage(keyring.getUuid(), out);
        message = receiveMessage(server_in);

        String user_id = receiveMessage(server_in);
        if ("incorrect_uuid".equals(message.trim())) {
            keyring.removeEntry("uuid");
            keyring.saveUuid(user_id);
        } else if (!"correct_uuid".equals(message.trim())) {
            System.err.println("Unexpected message when receiving UUID confirmation");
            return;
        }
        sendMessage("uuid_recv", out);
    }

    private void uuidNeeded(DataInputStream server_in, DataOutputStream out) {
        sendMessage("no_uuid", out);
        String user_id = receiveMessage(server_in);

        keyring.removeEntry("uuid");
        keyring.saveUuid(user_id);

        sendMessage("uuid_recv", out);
    }

    public void sendData(byte[] data, DataOutputStream out) throws IOException {
        int length = data.length;
        if (length <= 0) {
            System.err.println("Data null or zero length");
            return;
        }
        while (length > 127) {
            out.writeByte(127);
            length -= 127;
        }
        out.writeByte(length);
        out.write(data);
    }

    public byte[] readData(DataInputStream server_in) throws IOException {
        int data_length = 0;
        byte length_byte = server_in.readByte();
        while (length_byte == 127) {
            data_length += length_byte;
            length_byte = server_in.readByte();
        }
        data_length += length_byte;
        return server_in.readNBytes(data_length);
    }

    public int sendMessage(String message, DataOutputStream out) {
        try {
            Cipher arc4Cipher = Cipher.getInstance("ARCFOUR");
            arc4Cipher.init(Cipher.ENCRYPT_MODE, arc4Key);
            byte[] encryptedMessage = arc4Cipher.doFinal(message.getBytes());
            sendData(encryptedMessage, out);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e);
            return 1;
        }
        return 0;
    }

    public byte[] receiveBytes(DataInputStream server_in) {
        try {
            byte[] encryptedData = readData(server_in);

            Cipher arc4Cipher = Cipher.getInstance("ARCFOUR");
            arc4Cipher.init(Cipher.DECRYPT_MODE, arc4Key);
            return arc4Cipher.doFinal(encryptedData);
        } catch (Exception e) {
            System.err.println("Error receiving bytes: " + e);
            return null;
        }
    }

    public String receiveMessage(DataInputStream server_in) {
        return new String(receiveBytes(server_in));
    }
}