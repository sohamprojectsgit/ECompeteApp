import javax.swing.*;
import java.awt.*;
import java.util.*;

class DebugGame extends AbstractGame {
    private java.util.List<BuggyCode> bugs;
    private int currentBugIndex;
    private JTextArea codeArea;
    private JComboBox<String> errorTypeBox;
    private JTextField lineNumberField;
    private JButton submitBtn;
    private JButton nextBtn;
    private JLabel progressLabel;
    private volatile boolean isCleanedUp = false;
    
    public DebugGame(ClientGamePanel gamePanel) {
        super(gamePanel);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initializeBugs();
        currentBugIndex = 0;
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        
        progressLabel = new JLabel("Bug 1 of " + bugs.size());
        progressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel instrLabel = new JLabel("Find and identify the bug in the code:");
        
        headerPanel.add(progressLabel, BorderLayout.NORTH);
        headerPanel.add(instrLabel, BorderLayout.CENTER);
        
        // Code display
        codeArea = new JTextArea();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        codeArea.setEditable(false);
        codeArea.setTabSize(4);
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createTitledBorder("Code"));
        
        // Answer panel
        JPanel answerPanel = new JPanel(new GridBagLayout());
        answerPanel.setBorder(BorderFactory.createTitledBorder("Your Answer"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        answerPanel.add(new JLabel("Bug Type:"), gbc);
        gbc.gridx = 1;
        String[] errorTypes = {"Syntax Error", "Logic Error", "Runtime Error", "Null Pointer"};
        errorTypeBox = new JComboBox<>(errorTypes);
        answerPanel.add(errorTypeBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        answerPanel.add(new JLabel("Line Number:"), gbc);
        gbc.gridx = 1;
        lineNumberField = new JTextField(10);
        answerPanel.add(lineNumberField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        submitBtn = new JButton("Submit");
        nextBtn = new JButton("Next Bug");
        nextBtn.setEnabled(false);
        
        submitBtn.addActionListener(e -> checkBug());
        nextBtn.addActionListener(e -> loadNextBug());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(nextBtn);
        
        add(headerPanel, BorderLayout.NORTH);
        add(codeScroll, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(answerPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
        
        loadBug();
    }
    
    private void initializeBugs() {
        bugs = new ArrayList<>();
        
        bugs.add(new BuggyCode(
            "public class Test {\n" +
            "    public static void main(String[] args) {\n" +
            "        int x = 10;\n" +
            "        int y = 0;\n" +
            "        int result = x / y;\n" +
            "        System.out.println(result);\n" +
            "    }\n" +
            "}",
            "Runtime Error", 5
        ));
        
        bugs.add(new BuggyCode(
            "public class Loop {\n" +
            "    public static void main(String[] args) {\n" +
            "        for (int i = 0; i < 10; i++) {\n" +
            "            System.out.println(i)\n" +
            "        }\n" +
            "    }\n" +
            "}",
            "Syntax Error", 4
        ));
        
        bugs.add(new BuggyCode(
            "public class Sum {\n" +
            "    public static void main(String[] args) {\n" +
            "        int sum = 0;\n" +
            "        for (int i = 1; i <= 10; i++) {\n" +
            "            sum = i;\n" +
            "        }\n" +
            "        System.out.println(sum);\n" +
            "    }\n" +
            "}",
            "Logic Error", 5
        ));
        
        bugs.add(new BuggyCode(
            "public class NullTest {\n" +
            "    public static void main(String[] args) {\n" +
            "        String str = null;\n" +
            "        int length = str.length();\n" +
            "        System.out.println(length);\n" +
            "    }\n" +
            "}",
            "Null Pointer", 4
        ));
        
        Collections.shuffle(bugs);
    }
    
    private void loadBug() {
        if (isCleanedUp) return;
        
        if (currentBugIndex < bugs.size()) {
            BuggyCode bug = bugs.get(currentBugIndex);
            progressLabel.setText("Bug " + (currentBugIndex + 1) + " of " + bugs.size());
            codeArea.setText(bug.getCode());
            lineNumberField.setText("");
            submitBtn.setEnabled(true);
            nextBtn.setEnabled(false);
        }
    }
    
    private void checkBug() {
        if (isCleanedUp) return;
        
        try {
            int line = Integer.parseInt(lineNumberField.getText());
            String errorType = (String) errorTypeBox.getSelectedItem();
            
            BuggyCode bug = bugs.get(currentBugIndex);
            boolean correct = (line == bug.getBugLine() && errorType.equals(bug.getErrorType()));
            
            if (correct) {
                JOptionPane.showMessageDialog(this, "Correct! Bug found! +10 points");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Incorrect!\nCorrect answer: " + bug.getErrorType() + " at line " + bug.getBugLine());
            }
            
            gamePanel.submitAnswer(currentBugIndex, errorType + ":" + line, correct);
            
            submitBtn.setEnabled(false);
            nextBtn.setEnabled(true);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid line number!");
        }
    }
    
    private void loadNextBug() {
        if (isCleanedUp) return;
        
        currentBugIndex++;
        if (currentBugIndex < bugs.size()) {
            loadBug();
        } else {
            JOptionPane.showMessageDialog(this, "Debug Hunt completed!");
            gamePanel.completeGame();
        }
    }
    
    @Override
    public void cleanup() {
        if (isCleanedUp) return;
        isCleanedUp = true;
        
        // Disable all interactive components
        if (submitBtn != null) {
            submitBtn.setEnabled(false);
            for (var listener : submitBtn.getActionListeners()) {
                submitBtn.removeActionListener(listener);
            }
        }
        
        if (nextBtn != null) {
            nextBtn.setEnabled(false);
            for (var listener : nextBtn.getActionListeners()) {
                nextBtn.removeActionListener(listener);
            }
        }
        
        if (lineNumberField != null) {
            lineNumberField.setEnabled(false);
        }
        
        if (errorTypeBox != null) {
            errorTypeBox.setEnabled(false);
        }
        
        // Clear references
        bugs = null;
        
        System.out.println("DebugGame cleaned up successfully");
    }
}

class BuggyCode {
    private String code;
    private String errorType;
    private int bugLine;
    
    public BuggyCode(String code, String errorType, int bugLine) {
        this.code = code;
        this.errorType = errorType;
        this.bugLine = bugLine;
    }
    
    public String getCode() { return code; }
    public String getErrorType() { return errorType; }
    public int getBugLine() { return bugLine; }
}