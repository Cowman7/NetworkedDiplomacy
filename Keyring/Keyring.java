package Keyring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class Keyring {
    private KeyStore store = null;
    private String keyring_name = "";
    private String password = "";
    private String base_filename = "";

    public Keyring(String keyring_name, String keyring_password) {
        this.keyring_name = keyring_name;
        this.password = keyring_password;

        JFileChooser fc = new JFileChooser();
        FileSystemView fw = fc.getFileSystemView();
        String documents_path = fw.getDefaultDirectory().toString();
        File storage_directory = new File(documents_path + "\\Diplomacy");
        storage_directory.mkdirs();
        base_filename = storage_directory.getPath() + "\\";

        if (!new File(base_filename + keyring_name + ".jks").isFile()) {
            createStore(keyring_name);
        }
        
        loadStore(keyring_name);
    }

    private void loadStore(String store_name)  {
        try {
            store = KeyStore.getInstance("JKS");

            String filename = base_filename + store_name + ".jks";
            char[] password = this.password.toCharArray();
            store.load(new FileInputStream(filename), password);
        } catch (KeyStoreException kse) {
            System.err.println("Key Store Exception on KeyStore load: " + kse);
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println("No Such Algorithm Exception on KeyStore load: " + nsae);
        } catch (CertificateException ce) {
            System.err.println("Certificate Exception on KeyStore load: " + ce);
        } catch (FileNotFoundException fnfo) {
            System.err.println("File " + store_name + ".jks not found on KeyStore load");
        } catch (IOException ie) {
            System.err.println("IOException on KeyStore load: " + ie);
        }
    }

    /* Creates the Key Store */
    private void createStore(String store_name) {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

            ks.load(null, password.toCharArray());

            String filename = base_filename + store_name + ".jks";

            FileOutputStream fos = new FileOutputStream(filename);
            ks.store(fos, password.toCharArray());
        } catch (KeyStoreException kse) {
            System.err.println("Key Store Exception on KeyStore creation: " + kse);
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println("No Such Algorithm Exception on KeyStore creation: " + nsae);
        } catch (CertificateException ce) {
            System.err.println("Certificate Exception on KeyStore creation: " + ce);
        } catch (FileNotFoundException fnfo) {
            System.err.println("File " + store_name + ".jks not found on KeyStore Creation");
        } catch (IOException ie) {
            System.err.println("IOException on KeyStore creation: " + ie);
        }
    }

    private void saveStore() {
        try {
            String filename = base_filename + keyring_name + ".jks";
            FileOutputStream fos = new FileOutputStream(filename, false);
            store.store(fos, password.toCharArray());
        } catch (KeyStoreException kse) {
            System.err.println("Key Store Exception on KeyStore save: " + kse);
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println("No Such Algorithm Exception on KeyStore save: " + nsae);
        } catch (CertificateException ce) {
            System.err.println("Certificate Exception on KeyStore save: " + ce);
        } catch (FileNotFoundException fnfo) {
            System.err.println("File " + keyring_name + ".jks not found on KeyStore save");
        } catch (IOException ie) {
            System.err.println("IOException on KeyStore save: " + ie);
        }
    }

    public Certificate getCertificate(String cert_name) {
        try {
            Certificate cert = store.getCertificate(cert_name);
            if (cert == null) { System.err.println("Certificate " + cert_name + " not found"); }
            return cert;
        } catch (KeyStoreException kse) {
            System.err.println("Error in fetching certificate " + cert_name + ": " + kse);
        }

        return null;
    }

    public Key getKey(String key_name, String password_string) {
        try {
            Key key = store.getKey(key_name, password_string.toCharArray());
            if (key == null) { System.err.println("Key " + key_name + " not found"); }
            return key;
        } catch (KeyStoreException kse) {
            System.err.println("Error in fetching key " + key_name + ": " + kse);
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println("No Such Algorithm Exception while fetching key " + key_name + ": " + nsae);
        } catch (UnrecoverableKeyException uke) {
            System.err.println("Password " + password_string + " incorrect for key " + key_name + ": " + uke);
        }
        return null;
    }

    /*
    public KeyPair get_rsa_keypair(String password_string) {
        Key public_key_data = get_key("public_key", password_string);
        Key private_key_data = get_key("private_key", password_string);

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(public_key_data.getEncoded(), "RSA");
            PublicKey pubkey = kf.generatePublic(keySpec);

            PKCS8EncodedKeySpec private_key_spec = new PKCS8EncodedKeySpec(private_key_data.getEncoded(), "RSA");
            PrivateKey privkey = kf.generatePrivate(private_key_spec);

            return new KeyPair(pubkey, privkey);
        } catch (InvalidKeySpecException ikse) {
            System.err.println("Issue in finding server keys: " + ikse);
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println("No Such Algorithm Exception while fetching server keys: " + nsae);
        }

        return null;
    }
    */

    public KeyPair getClientKeys(String password_string) {
        Key public_key_data = getKey("client_public", password_string);
        Key private_key_data = getKey("client_private", password_string);
        
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(public_key_data.getEncoded(), "RSA");
            PublicKey pubkey = kf.generatePublic(keySpec);

            PKCS8EncodedKeySpec private_key_spec = new PKCS8EncodedKeySpec(private_key_data.getEncoded(), "RSA");
            PrivateKey privkey = kf.generatePrivate(private_key_spec);

            return new KeyPair(pubkey, privkey);
        } catch (InvalidKeySpecException ikse) {
            System.err.println("Issue in finding client RSA keys: " + ikse);
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println("No Such Algorithm Exception while fetching client RSA keys: " + nsae);
        }

        return null;
    }


    public boolean findEntry(String alias) {
        try {
            return store.containsAlias(alias);
        } catch (KeyStoreException kse) {
            System.err.println("Error in fetching entry " + alias + ": " + kse);
        }
        return false;
    }

    /* Return Codes:
     * 0 - Key Stored Correctly
     * 1 - Key already exists
     * 2 - Error while attempting to access the KeyStore
    */
    public int saveKey(String key_name, SecretKey secret_key, String password_string) {
        try {
            if (store.containsAlias(key_name)) { 
                System.err.println("Error: Entry " + key_name + " already exists");
                return 1;
            }
        } catch (KeyStoreException kse) {
            System.err.println("Error accessing KeyStore: " + kse);
            return 2;
        }

        KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(secret_key);
        KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(password_string.toCharArray());
        try {
            store.setEntry(key_name, secret, password);
        } catch (KeyStoreException kse) {
            System.err.println("Error in storing key " + key_name + ": " + kse);
            return 2;
        }

        saveStore();
        return 0;
    }

    public int saveServerPubkey(PublicKey pubkey, String password_string) {
        SecretKeySpec public_key = new SecretKeySpec(pubkey.getEncoded(), "RSA");
        
        int public_result = saveKey("server_public_key", (SecretKey)public_key, password_string);
        if (public_result > 0) {
            System.err.println("Code " + public_result + " in pubkey save");
        }
        return public_result;
    }


    /* Return Codes:
     * 0 - Key Stored Correctly
     * 1 - Key already exists
     * 2 - Error while attempting to access the KeyStore
    */
    public int saveKey(String key_name, String password_string, SecretKey secret_key, Certificate[] cert_chain) {
        try {
            if (store.containsAlias(key_name)) { 
                System.err.println("Error: Entry " + key_name + " already exists");
                return 1;
            }
        } catch (KeyStoreException kse) {
            System.err.println("Error accessing KeyStore: " + kse);
            return 2;
        }
        
        try {
            store.setKeyEntry(key_name, secret_key, password_string.toCharArray(), cert_chain);
        } catch (KeyStoreException kse) {
            System.err.println("Error in storing key " + key_name + ": " + kse);
            return 2;
        }

        saveStore();
        return 0;
    }


    public int saveKeys(String key_name, String password_string, PrivateKey private_key, PublicKey public_key) {
        System.out.println("This is not finished");
        
        saveStore();
        return -1;
    }


    /* Return Codes:
     * 0 - Key Stored Correctly
     * 1 - Key already exists
     * 2 - Error while attempting to access the KeyStore
    */
    public int saveCertificate(String cert_name, Certificate cert) {
        try {
            if (store.containsAlias(cert_name)) { 
                System.err.println("Error: Entry " + cert_name + " already exists");
                return 1;
            }
        } catch (KeyStoreException kse) {
            System.err.println("Error accessing KeyStore: " + kse);
            return 2;
        }
        
        try {
            store.setCertificateEntry(cert_name, cert);
        } catch (KeyStoreException kse) {
            System.err.println("Error in storing certificate " + cert_name + ": " + kse);
            return 2;
        }
        
        saveStore();
        return 0;
    }
    
    public String getUuid() {
        Key uuid_key = getKey("uuid", "player");
        byte[] uuid_data = uuid_key.getEncoded();
        return new String(uuid_data);
    }

    public int saveUuid(String uuid) {
        SecretKeySpec password_key = new SecretKeySpec(uuid.getBytes(), "PBEWithMD5AndDES");
        return saveKey("uuid", password_key, "player");
    }

    /*
     * deleteEntry removes the entry regardless of whether it exists or not
     */
    public void removeEntry(String alias) {
        try {
            store.deleteEntry(alias);
        } catch (KeyStoreException kse) {
            System.err.println("Error in deleting entry " + alias + ": " + kse);
        }
        saveStore();
    }
}
