package Window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Lobby {
    // Array of strings to add to the title randomly
    private static String[] addedStrings = {
        "supported by We're Special Studios!",
        "Try out the Minecraft server!",
        "Gay Squid",
        "I just have more time in my time",
        "Not Handicapped, just slightly Retarded",
        "I once told TSA I had a gun",
        "sorr predicament lit rn",
        "Damn, I'm crinking",
        "We love propaganda",
        "Why is there so much Hitler stuff?",
        "Yeah we need to gas them",
        "Cigarettes r yummy",
        "That shit be lethal doses",
        "I got kicked out of a US embassy",
        "This is like George Orwells Book",
        "1984",
        "I'm not in over my head. My head is in over me",
        "Whatever the fuck they speak in Indonesia land",
        "The japanese are making shows about cowboys?"
    };

    // List of lobby members, each member is represented as a tuple of player name and image bytes
    private ArrayList<Tuple<String, byte[]>> lobby_members = new ArrayList<Tuple<String, byte[]>>(7);
    
    // Array of JLabels to display player usernames and country names
    private JLabel[] player_labels = new JLabel[7];
    private JLabel[] country_labels = new JLabel[7];
    
    // Main window for the lobby
    private MainWindow window;

    private boolean[] ready_state = {false};
    
    // Constructor for the Lobby class
    public Lobby() {
        
        // Set the title with a possible random addition from addedStrings
        String title = "Diplomacy Lobby";
        int rand = (int)(Math.random() * Math.ceil((double)(addedStrings.length * 1.5)));
        if (rand <= addedStrings.length) {
            title += ": " + addedStrings[(int)(Math.random() * addedStrings.length)];
        }

        // Initialize the main window
        window = new MainWindow(title);
        window.setLayout(null); // Use null layout to manually set component positions
        window.create();
    }

    public void buildWindow() {
        window.setVisible(true);
        // Create content pane with null layout
        JPanel contentPane = new JPanel(null);
        contentPane.setOpaque(false); // Make content pane transparent
        window.add(contentPane, Integer.valueOf(0));

        buildContentPane(contentPane);
        
        setWindowSize(window, contentPane);

        // Add the map to the window
        JLabel map = buildMap();
        window.add(map);
        map.setBounds(0, 0, 1047, 900); // Set the bounds for the map label
        updateDisplay();

        
    }

    private JLabel buildMap() {
        // Load and scale the map image
        ImageIcon map_icon = new ImageIcon(getClass().getResource("map.jpg"));
        Image image = map_icon.getImage();
        Image small_image = image.getScaledInstance(1047, 900, java.awt.Image.SCALE_SMOOTH);
        map_icon = new ImageIcon(small_image);

        // Create and return the map label
        return new JLabel(map_icon);
    }

    private void setWindowSize(MainWindow window, JPanel contentPane) {
        // Calculate window and content pane sizes
        Insets insets = window.getInsets();
        Dimension window_size = new Dimension(1047 + insets.left + insets.right, 900 + insets.top + insets.bottom);
        window.setSize(window_size);
        window.setMinimumSize(window_size);

        contentPane.setSize(window_size);
        contentPane.setMinimumSize(window_size);
        contentPane.setBounds(0, 0, window_size.width, window_size.height);
        window.setResizable(true);
    }

    private void buildContentPane(JPanel contentPane) {
        addImportantButtons(contentPane);

        // Add each country label as well as icon + username combo label to the content pane
        for (COUNTRIES country : COUNTRIES.values()) {
            JLabel country_name = new JLabel(country.toString());
            contentPane.add(country_name, Integer.valueOf(1)); // has to be added before logic or it doesn't render
            country_labels[country.getValue()] = country_name;
            
            // Set properties for country labels
            country_name.setOpaque(true); // Needs to be opaque for background to render
            country_name.setHorizontalAlignment(JLabel.CENTER);
            country_name.setFont(new Font(country_name.getFont().getName(), 0, 18)); // Sets font size to 18
            
            
            int i = country.getValue();
            // Creates new JLabel with AI (i + i) as the text and the image as the default AI image.
            player_labels[i] = new JLabel("AI " + (i + 1), new ImageIcon(getClass().getResource("AI_pic.png")), 0);
            contentPane.add(player_labels[i], Integer.valueOf(2));

            // Set properties for player labels
            player_labels[i].setOpaque(true);
            player_labels[i].setHorizontalTextPosition(JLabel.CENTER);
            player_labels[i].setVerticalTextPosition(JLabel.BOTTOM);

            // Set bounds and background color for each country and player label based on the country
            switch (country) {
                case AUSTRIA_HUNGARY:
                    country_name.setBounds(470, 510, 135, 25);
                    country_name.setBackground(new Color(225, 100 ,100));
                    
                    player_labels[i].setBackground(new Color(225, 100 ,100));
                break;
                case ENGLAND:
                    country_name.setBounds(250, 270, 70, 25);
                    country_name.setBackground(new Color(0, 150, 173));

                    player_labels[i].setBackground(new Color(0, 150, 173));
                break;
                case FRANCE:
                    country_name.setBounds(260, 515, 64, 25);
                    country_name.setBackground(new Color(128, 160, 207));

                    player_labels[i].setBackground(new Color(128, 160, 207));
                break;
                case GERMANY:
                    country_name.setBounds(390, 390, 75, 25);
                    country_name.setBackground(new Color(100, 100, 100));

                    player_labels[i].setBackground(new Color(100, 100, 100));
                break;
                case ITALY:
                    country_name.setBounds(370, 630, 64, 25);
                    country_name.setBackground(new Color(100, 186, 100));

                    player_labels[i].setBackground(new Color(100, 186, 100));
                break;
                case RUSSIA:
                    country_name.setBounds(675, 395, 64, 25);
                    country_name.setBackground(new Color(200, 200, 200));

                    player_labels[i].setBackground(new Color(200, 200, 200));
                break;
                case TURKEY:
                    country_name.setBounds(750, 670, 64, 25);
                    country_name.setBackground(new Color(221, 212, 50));

                    player_labels[i].setBackground(new Color(221, 212, 50));
                break;
            }

            int x = country_name.getBounds().x + ( country_name.getBounds().width / 2) - 32;
            int y = country_name.getBounds().y + 25;
            player_labels[i].setBounds(x, y, 64, 85);
            
        }
    }

    private void addImportantButtons(JPanel contentPane) {

        JButton ready_button = new JButton("Ready");
        contentPane.add(ready_button);

        ready_button.setBounds(25, 25, 125, 50);
        ready_button.setFont(new Font("Elephant", 0, 24));
        ready_button.setFocusable(false);

        final Color green = new Color(16, 128, 16);
        final Color red = new Color(192, 48, 16);
        ready_button.setBackground(red);
        
        

        ready_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ready_state[0] == false) {
                    ready_state[0] = true;
                    ready_button.setBackground(green);
                } else {
                    ready_state[0] = false;
                    ready_button.setBackground(red);
                }
            }
        });
    }

    // Prompts the user for a username, must be at least 3 characters up to a max of 20. Can only have Alphanumeric characters, "_" and "-"
    // Cannot contain the string "(You)"
    public Tuple<String, byte[]> promptUsername(String current_playername) {
        byte[] current_picture = null;
        for (Tuple<String, byte[]> iter : lobby_members) {
            if (iter.x.contains("(You)")) {
                current_picture = iter.y;
                break;
            }
        }

        ProfileEditor profileEditor = new ProfileEditor(current_playername, current_picture);
        Tuple<String, byte[]> new_profile = profileEditor.render();

        for (int i = 0; i < lobby_members.size(); i++) {
            Tuple<String, byte[]> iter = lobby_members.get(i);
            if (iter.x.contains("(You)")) {
                lobby_members.set(i, new Tuple<String,byte[]>(new_profile.x + " (You)", new_profile.y));
                break;
            }
        }

        return new_profile;
    }

    public void memberAssembly(String playername, byte[] image_bytes) {
        lobby_members.add(new Tuple<String, byte[]>(playername, image_bytes));
    }

    // Add a new lobby member
    // Called by the Peer class when the server sends us new player data
    public void addLobbyMember(String playername, byte[] image_bytes) {
        lobby_members.add(new Tuple<String, byte[]>(playername, image_bytes));
        updateDisplay();
    }


    // Remove a lobby member by name
    // Called by the Peer class when the server sends us a player to remove
    public void removeLobbyMember(String playername) {
        for (int i = 0; i < lobby_members.size(); i++) {
            Tuple<String, byte[]> member = lobby_members.get(i);
            if (member.x.equals(playername)) {
                lobby_members.remove(i);
                break;
            }
        }
        updateDisplay();
    }


    // Update display based on current lobby members
    private void updateDisplay() {
        for (int i = 0; i < 7; i++) {
            if (i >= lobby_members.size()) {
                // Update AI label if no player is available for this slot
                String text = player_labels[i].getText();
                player_labels[i].setText("AI " + (i - lobby_members.size() + 1));
                if (!text.startsWith("AI")) {
                    new ImageIcon(getClass().getResource("AI_pic.png"));
                }

                continue;
            }

            // Update the player label if it has changed
            if (!player_labels[i].getText().equals(lobby_members.get(i).x)) {
                Image scaled_image = new ImageIcon(lobby_members.get(i).y).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                player_labels[i].setIcon(new ImageIcon(scaled_image));

                String text = lobby_members.get(i).x;
                player_labels[i].setText(text);

                // Adjust label width if the text width exceeds the default width
                int font_width =  player_labels[i].getFontMetrics(player_labels[i].getFont()).stringWidth(player_labels[i].getText()) + 5;
                if (font_width > 64) {
                    Rectangle user_bounds = player_labels[i].getBounds();

                    player_labels[i].setBounds(user_bounds.x - (font_width / 4), user_bounds.y, font_width, user_bounds.height);

                    
                    int country_font_width =  country_labels[i].getFontMetrics(country_labels[i].getFont()).stringWidth(country_labels[i].getText()) + 5;
                    if (country_font_width < font_width) {
                        Rectangle country_bounds = country_labels[i].getBounds();
                        country_labels[i].setBounds(user_bounds.x - (font_width / 4), country_bounds.y, font_width, country_bounds.height);
                    } else {
                        Rectangle country_bounds = country_labels[i].getBounds();
                        
                        double middle = country_bounds.x + (country_bounds.width / 2) - 13;
                        
                        country_labels[i].setBounds((int)Math.floor(middle - (country_font_width / 2)), country_bounds.y, country_font_width, country_bounds.height);
                    }
                }
            }
        }
    }


    // Main method to run the lobby
    public static void main(String[] args) {
        // Schedule a job for the event dispatch thread: creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> new Lobby());
    }
}
