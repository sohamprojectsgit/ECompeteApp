import javax.swing.*;
import java.awt.*;

class CodeWorldGame extends AbstractGame {
    private int robotX, robotY;
    private int targetX, targetY;
    private int gridSize = 8;
    private JPanel gridPanel;
    private JTextArea codeArea;
    private JButton runBtn;
    private JLabel statusLabel;
    private boolean[][] obstacles;
    
    public CodeWorldGame(ClientGamePanel gamePanel) {
        super(gamePanel);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        robotX = 0;
        robotY = 0;
        targetX = gridSize - 1;
        targetY = gridSize - 1;
        
        obstacles = new boolean[gridSize][gridSize];
        setupObstacles();
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        
        JLabel titleLabel = new JLabel("Guide the robot to the target!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        statusLabel = new JLabel("Commands: moveRight(), moveDown(), moveLeft(), moveUp()");
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Grid
        gridPanel = new JPanel(new GridLayout(gridSize, gridSize, 2, 2));
        gridPanel.setPreferredSize(new Dimension(400, 400));
        gridPanel.setBorder(BorderFactory.createTitledBorder("World Grid"));
        updateGrid();
        
        // Code editor
        JPanel codePanel = new JPanel(new BorderLayout(5, 5));
        codePanel.setBorder(BorderFactory.createTitledBorder("Write Commands"));
        
        codeArea = new JTextArea(15, 30);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        codeArea.setText("// Write your commands here\n// Example:\n// moveRight();\n// moveDown();");
        JScrollPane codeScroll = new JScrollPane(codeArea);
        
        runBtn = new JButton("Run Code");
        runBtn.addActionListener(e -> executeCode());
        
        codePanel.add(codeScroll, BorderLayout.CENTER);
        codePanel.add(runBtn, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.WEST);
        add(codePanel, BorderLayout.CENTER);
    }
    
    private void setupObstacles() {
        obstacles[2][2] = true;
        obstacles[3][3] = true;
        obstacles[4][1] = true;
        obstacles[5][5] = true;
    }
    
    private void updateGrid() {
        gridPanel.removeAll();
        
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                
                if (x == robotX && y == robotY) {
                    cell.add(new JLabel("R"));
                } else if (x == targetX && y == targetY) {
                    cell.add(new JLabel("T"));
                } else if (obstacles[y][x]) {
                    cell.add(new JLabel("X"));
                }
                
                gridPanel.add(cell);
            }
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
    }
    
    private void executeCode() {
        String code = codeArea.getText();
        robotX = 0;
        robotY = 0;
        
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("//") || line.isEmpty()) continue;
            
            if (line.contains("moveRight")) {
                moveRobot(1, 0);
            } else if (line.contains("moveDown")) {
                moveRobot(0, 1);
            } else if (line.contains("moveLeft")) {
                moveRobot(-1, 0);
            } else if (line.contains("moveUp")) {
                moveRobot(0, -1);
            }
            
            updateGrid();
            
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        checkWin();
    }
    
    private void moveRobot(int dx, int dy) {
        int newX = robotX + dx;
        int newY = robotY + dy;
        
        if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize) {
            if (!obstacles[newY][newX]) {
                robotX = newX;
                robotY = newY;
            }
        }
    }
    
    private void checkWin() {
        if (robotX == targetX && robotY == targetY) {
            JOptionPane.showMessageDialog(this, "Success! Robot reached the target! +10 points");
            gamePanel.submitAnswer(0, "completed", true);
            gamePanel.completeGame();
        } else {
            JOptionPane.showMessageDialog(this, "Try again! Robot didn't reach the target.");
        }
    }
    
    @Override
    public void cleanup() {
    }
}