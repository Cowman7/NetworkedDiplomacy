package Window;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.PlainDocument;

public class ProfileEditor implements ActionListener {
    
    private MainWindow window;

    final private String current_playername;
    final private byte[] current_image;

    String new_playername;
    byte[] new_image_bytes;
    boolean finished;

    ProfileEditor(String current_playername, byte[] current_image) {
        window = new MainWindow("Diplomacy: User Profile");
        window.setLayout(null);
        
        this.current_playername = current_playername;
        this.current_image = current_image;

        this.new_playername = current_playername;
        this.new_image_bytes = current_image;

        buildWindow();

        window.create();
        window.setSize(400, 300);
        window.setResizable(false);
    }

    public Tuple<String, byte[]> render() {
        window.setVisible(true);

        finished = false;

        while (!finished) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                System.err.println("Error in username window loop: " + ie);
                break;
            }
        }

        window.dispose();

        if ("nN".equals(new_playername)) new_playername = "";
        
        return new Tuple<String,byte[]>(new_playername, new_image_bytes);
    }

    private void buildWindow() {
        String playername = current_playername;
        if ("nN".equals(current_playername)) playername = "";
        
        addPlayernameSection(playername);
        
        JButton cont = new JButton("Continue to Lobby");
        window.add(cont);
        cont.setBounds(15, 225, 150, 25);
        cont.addActionListener(this);
        cont.setActionCommand("end_profile_change");
        
        addImageSection();
    }

    private void addPlayernameSection(String playername) {
        String display_name = "Current name: " + playername;
        if ("".equals(playername)) display_name = "Name not set";

        JLabel current_name_label = new JLabel(display_name);
        window.add(current_name_label, 0);
        current_name_label.setBounds(25, 10, 200, 25);
        
        JTextField text = new JTextField(playername, 20);
        window.add(text, 1);
        text.setBounds(25, 35, 200, 25);

        PlainDocument doc = (PlainDocument) text.getDocument();
        doc.setDocumentFilter(new LengthRestrictedDocumentFilter(20));

        JButton submit = new JButton("Change Username");
        window.add(submit);
        submit.setBounds(50, 70, 150, 25);

        JLabel error = new JLabel();
        window.add(error, 2);
        error.setBounds(25, 80, 200, 60);

        submit.addActionListener(this);
        submit.setActionCommand("update_username");
        
    }

    private void addImageSection() {
        JLabel current_image_label = new JLabel("Current image");
        window.add(current_image_label);
        current_image_label.setBounds(275, 10, 125, 25);

        Image img = new ImageIcon(current_image).getImage().getScaledInstance(125, 125, java.awt.Image.SCALE_SMOOTH);
        JLabel image_label = new JLabel(new ImageIcon(img));
        window.add(image_label, 4);
        image_label.setBounds(250, 35, 125, 125);

        JButton upload_button = new JButton("Upload New Image");
        window.add(upload_button);
        upload_button.setBounds(225, 175, 150, 25);

        upload_button.addActionListener(this);
        upload_button.setActionCommand("upload_image");
    }

    public void actionPerformed(ActionEvent event) {
        switch(event.getActionCommand()) {
            case "update_username":
                updateUsername();
            break;
            case "upload_image":
                updateImage();
            break;
            case "end_profile_change":
                finished = true;
            break;
        }
    }

    private void updateUsername() {
        JTextField text = (JTextField)window.getContentPane().getComponent(1);
        String input = text.getText();
        
        JLabel error = (JLabel)window.getContentPane().getComponent(2);
        if (!inputCheck(input, error)) return;
        error.setText("");
        
        JLabel name_label = (JLabel)window.getContentPane().getComponent(0);
        name_label.setText("Current name: " + input);
        new_playername = input;

    }

    private boolean inputCheck(String input, JLabel error) {
        if (input.length() < 3) {
            error.setText("<html>Username must be <br>at least 3 characters</html>");
            return false;
        }

        for (char ch : input.toCharArray()) {
            if (ch <= 31 || ch == 124 || ch >= 127) {
                error.setText("Invalid Character in Username");
                return false;
            }
        }

        if (input.contains("(You)")) {
            error.setText("Username cannot contain \"(You)\"");
            return false;
        }

        return true;
    }

    private void updateImage() {
        JLabel image_label = (JLabel)window.getContentPane().getComponent(4);

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "bmp");

        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(window);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected_file = fileChooser.getSelectedFile();
            try {
                BufferedImage buffered_image = ImageIO.read(selected_file);
                Image scaledImage = buffered_image.getScaledInstance(125, 125, Image.SCALE_SMOOTH);
                image_label.setIcon(new ImageIcon(scaledImage));
                new_image_bytes = Files.readAllBytes(selected_file.toPath());
            } catch (IOException ioe) {
                System.err.println("Error in getting image file: " + ioe);
            }
        }
    }
}
