package Window;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class MainWindow extends JFrame {
    String title = "Diplomacy";
    
    public MainWindow() {}

    public MainWindow(String title) {
        this.title = title;
    }

    public void initialize() {
        setTitle(title);
        
        
        setSize(600, 500);
        setMinimumSize(new Dimension(400, 300));

        
        setIconImage(new ImageIcon(getClass().getResource("favicon.png")).getImage());


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void create() {
        setTitle(title);

        setSize(600, 500);
        setMinimumSize(new Dimension(400, 300));

        
        setIconImage(new ImageIcon(getClass().getResource("favicon.png")).getImage());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}

