import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class ClientGamePanel extends GamePanel implements ClientMessageListener {
    private ContestClient client;
    private JPanel gameContentPanel;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private Timer countdownTimer;
    private long startTime;
    private int timeLimit;
    private int currentScore;
    private AbstractGame currentGame;
    private JFrame gameFrame;
    private volatile boolean isExiting = false;
    
    public ClientGamePanel(MainFrame parent, ContestClient client) {
        super(parent);
        this.client = client;
        this.client.setMessageListener(this);
        this.timeLimit = client.getTimeLimit() * 60;
        this.startTime = System.currentTimeMillis();
        this.currentScore = 0;
        
        createFullscreenWindow();
    }
    
    private void createFullscreenWindow() {
        gameFrame = new JFrame("E-Compete - Contest");
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.setUndecorated(true);
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        
        // Alt+F4 support
        gameFrame.getRootPane().registerKeyboardAction(
            e -> confirmExit(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        timerLabel = new JLabel("Time: " + formatTime(timeLimit), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        JButton exitBtn = new JButton("Exit Contest");
        exitBtn.addActionListener(e -> confirmExit());
        
        headerPanel.add(scoreLabel, BorderLayout.WEST);
        headerPanel.add(timerLabel, BorderLayout.CENTER);
        headerPanel.add(exitBtn, BorderLayout.EAST);
        
        // Game content area
        gameContentPanel = new JPanel(new BorderLayout());
        loadGame(client.getGameType());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(gameContentPanel, BorderLayout.CENTER);
        
        gameFrame.add(mainPanel);
        gameFrame.setVisible(true);
        
        startCountdown();
    }
    
    private void confirmExit() {
        if (isExiting) return;
        
        gameFrame.setAlwaysOnTop(false);
        
        int result = JOptionPane.showConfirmDialog(
            gameFrame,
            "Are you sure you want to exit the contest?\nYour progress will be saved.",
            "Exit Contest",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            exitContest();
        } else {
            gameFrame.setAlwaysOnTop(true);
        }
    }
    
    private void exitContest() {
        if (isExiting) return;
        isExiting = true;
        
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        
        long completionTime = System.currentTimeMillis() - startTime;
        
        try {
            if (client != null) {
                client.sendCompletion(currentScore, completionTime);
            }
        } catch (Exception e) {
            System.err.println("Error sending completion: " + e.getMessage());
        }
        
        cleanup();
        
        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null) {
                gameFrame.setVisible(false);
                gameFrame.dispose();
            }
            parent.setVisible(true);
            parent.toFront();
            parent.requestFocus();
            parent.showPanel("HOME");
        });
    }
    
    private void loadGame(String gameType) {
        switch (gameType) {
            case "Quiz Challenge":
                currentGame = new QuizGame(this);
                break;
            case "Debug Hunt":
                currentGame = new DebugGame(this);
                break;
            case "Problem Solver":
                currentGame = new ProblemSolverGame(this);
                break;
            case "Code World":
                currentGame = new CodeWorldGame(this);
                break;
        }
        
        if (currentGame != null) {
            gameContentPanel.add(currentGame, BorderLayout.CENTER);
        }
    }
    
    private void startCountdown() {
        countdownTimer = new Timer(1000, e -> {
            if (isExiting) {
                ((Timer)e.getSource()).stop();
                return;
            }
            
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long remaining = timeLimit - elapsed;
            
            if (remaining <= 0) {
                endGame();
            } else {
                timerLabel.setText("Time: " + formatTime((int)remaining));
            }
        });
        countdownTimer.start();
    }
    
    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
    
    public void updateScore(int points) {
        if (!isExiting) {
            currentScore += points;
            scoreLabel.setText("Score: " + currentScore);
        }
    }
    
    public void submitAnswer(int questionId, String answer, boolean correct) {
        if (!isExiting && client != null) {
            try {
                client.sendAnswer(questionId, answer, correct);
            } catch (Exception e) {
                System.err.println("Error submitting answer: " + e.getMessage());
            }
            if (correct) {
                updateScore(10);
            }
        }
    }
    
    private void endGame() {
        if (isExiting) return;
        isExiting = true;
        
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        
        long completionTime = System.currentTimeMillis() - startTime;
        
        try {
            if (client != null) {
                client.sendCompletion(currentScore, completionTime);
            }
        } catch (Exception e) {
            System.err.println("Error sending completion: " + e.getMessage());
        }
        
        gameFrame.setAlwaysOnTop(false);
        
        JOptionPane.showMessageDialog(gameFrame,
            "Contest Completed!\nYour Score: " + currentScore +
            "\nTime: " + formatTime((int)(completionTime / 1000)),
            "Results",
            JOptionPane.INFORMATION_MESSAGE);
        
        cleanup();
        
        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null) {
                gameFrame.setVisible(false);
                gameFrame.dispose();
            }
            parent.setVisible(true);
            parent.toFront();
            parent.requestFocus();
            parent.showPanel("HOME");
        });
    }
    
    public void completeGame() {
        if (!isExiting) {
            endGame();
        }
    }
    
    @Override
    public void onMessageReceived(String message) {
        System.out.println("Server message: " + message);
    }
    
    @Override
    public void cleanup() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        if (currentGame != null) {
            try {
                currentGame.cleanup();
            } catch (Exception e) {
                System.err.println("Error cleaning up game: " + e.getMessage());
            }
        }
        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception e) {
                System.err.println("Error disconnecting client: " + e.getMessage());
            }
        }
    }
}