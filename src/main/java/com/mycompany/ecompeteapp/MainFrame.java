import javax.swing.*;
import java.awt.*;
import java.net.*;

class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private HomePanel homePanel;
    private HostPanel hostPanel;
    private JoinPanel joinPanel;
    
    public MainFrame() {
        setTitle("E-Compete - Contest Hosting Platform");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        homePanel = new HomePanel(this);
        hostPanel = new HostPanel(this);
        joinPanel = new JoinPanel(this);
        
        mainPanel.add(homePanel, "HOME");
        mainPanel.add(hostPanel, "HOST");
        mainPanel.add(joinPanel, "JOIN");
        
        add(mainPanel);
        setVisible(true);
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    public void showGamePanel(GamePanel gamePanel) {
        // Both HostControlPanel and ClientGamePanel create their own fullscreen windows
    }
}