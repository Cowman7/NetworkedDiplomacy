import java.io.*;
import java.net.*;

import Window.Lobby;
import Window.Tuple;

public class Peer {
    private Socket              socket = null;
    private BufferedReader      input = null;
    private DataInputStream     server_in = null;
    private DataOutputStream    out = null;
    private Lobby               lobby = null;
    private EncryptionHandler encryptionHandler;
    
    public Peer(String address, int port) {
        encryptionHandler = new EncryptionHandler();

        try {
            connectToServer(address, port);
            performHandshake();
            processMessages();
        } catch (Exception e) {
            System.err.println("An error occured: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void connectToServer(String address, int port) throws IOException {
        System.out.println("Connecting to " + address + " on port " + port);
        socket = new Socket(address, port);
        System.out.println("Connected to " + address + " on port " + port);

        input = new BufferedReader(new InputStreamReader(System.in));
        out = new DataOutputStream(socket.getOutputStream());
        server_in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    private void performHandshake() throws Exception {
        if (!encryptionHandler.performRSAHandshake(server_in, out)) {
            throw new Exception("Handshake Failed");
        }
        System.out.println("Handshake Success");
        encryptionHandler.checkUserId(server_in, out);
    }

    private void processMessages() throws IOException {
        if (!assembleLobby()) {
            throw new IOException("Error in fetching user information");
        }
        displayLobby();

        String message;
        while ((message = encryptionHandler.receiveMessage(server_in)) != null) {
            handleMessage(message);
        }

        String line;
        while (!(line = input.readLine()).equals("Over")) {
            encryptionHandler.sendMessage(line, out);
        }
    }

    private void handleMessage(String message) {
        switch (message) {
            case "add_lobby_member":
                addLobbyMember();
                break;
            case "remove_lobby_member":
                removeLobbyMember();
                break;
            case "start_game":
                encryptionHandler.sendMessage("", out);
                break;
            default:
                System.out.println("Unknown message: " + message);
        }
    }

    private boolean assembleLobby() {
        if (!"sending_lobby".equals(encryptionHandler.receiveMessage(server_in))) {
            System.err.println("Expected lobby assembly");
            return false;
        }
        encryptionHandler.sendMessage("retrieving_lobby", out);

        lobby = new Lobby();
        retrieveLobbyMembers();

        encryptionHandler.sendMessage("get_current_username", out);

        String current_username = encryptionHandler.receiveMessage(server_in);
        encryptionHandler.sendMessage("received_username", out);

        
        Tuple<String, byte[]> user_data = lobby.promptUsername(current_username);

        if (current_username.equals(user_data.x) || "".equals(user_data.x)) {
            encryptionHandler.sendMessage("no_username_change", out);
        } else {
            encryptionHandler.sendMessage("sending_username", out);
            encryptionHandler.sendMessage(user_data.x, out);
        }

        if (!"message_received".equals(encryptionHandler.receiveMessage(server_in))) {
            System.err.println("Incorrect message response");
            return false;
        }

        if (user_data.y == null) {
            encryptionHandler.sendMessage("no_image_change", out);
        } else {
            encryptionHandler.sendMessage("sending_image", out);
            encryptionHandler.sendBytes(user_data.y, out);
        }

        if (!"image_received".equals(encryptionHandler.receiveMessage(server_in))) {
            System.err.println("Incorrect message response");
            return false;
        }
        return true;
    }

    private void retrieveLobbyMembers() {
        String message = encryptionHandler.receiveMessage(server_in);
        while (!"all_members_sent".equals(message)) {
            String incoming_username = message;
            encryptionHandler.sendMessage("received_playername", out);

            byte[] incoming_image = encryptionHandler.receiveBytes(server_in);
            encryptionHandler.sendMessage("received_image", out);

            lobby.memberAssembly(incoming_username, incoming_image);

            message = encryptionHandler.receiveMessage(server_in);
        }
    }

    private void displayLobby() {
        lobby.buildWindow();
    }

    private void addLobbyMember() {
        System.out.println("Adding Lobby Member");
        encryptionHandler.sendMessage("send_player_data", out);
        
        String incomingPlayerName = encryptionHandler.receiveMessage(server_in);
        encryptionHandler.sendMessage("received_playername", out);
        
        byte[] incomingImage = encryptionHandler.receiveBytes(server_in);
        encryptionHandler.sendMessage("all_elements_received", out);

        lobby.addLobbyMember(incomingPlayerName, incomingImage);
    }

    private void removeLobbyMember() {
        System.out.println("Removing Lobby Member");
        encryptionHandler.sendMessage("ready_to_receive", out);
        
        String playerName = encryptionHandler.receiveMessage(server_in);
        lobby.removeLobbyMember(playerName);
    }

    private void cleanup() {
        try {
            if (input != null) input.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e);
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Peer peer = new Peer("direct.ws-studios.com", 1855);
    }
}