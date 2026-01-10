import javax.swing.*;

abstract class AbstractGame extends JPanel {
    protected ClientGamePanel gamePanel;
    
    public AbstractGame(ClientGamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    public abstract void cleanup();
}