package com.mycompany.ecompeteapp;

import javax.swing.*;
import java.awt.*;
import java.util.*;

class ProblemSolverGame extends AbstractGame {
    private java.util.List<Problem> problems;
    private int currentProblemIndex;
    private JTextArea problemArea;
    private JTextArea solutionArea;
    private JButton submitBtn;
    private JButton nextBtn;
    private JLabel progressLabel;
    
    public ProblemSolverGame(ClientGamePanel gamePanel) {
        super(gamePanel);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initializeProblems();
        currentProblemIndex = 0;
        
        // Header
        JPanel headerPanel = new JPanel();
        progressLabel = new JLabel("Problem 1 of " + problems.size());
        progressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(progressLabel);
        
        // Problem display
        problemArea = new JTextArea(8, 50);
        problemArea.setFont(new Font("Arial", Font.PLAIN, 13));
        problemArea.setEditable(false);
        problemArea.setLineWrap(true);
        problemArea.setWrapStyleWord(true);
        JScrollPane problemScroll = new JScrollPane(problemArea);
        problemScroll.setBorder(BorderFactory.createTitledBorder("Problem"));
        
        // Solution input
        solutionArea = new JTextArea(10, 50);
        solutionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        solutionArea.setTabSize(4);
        JScrollPane solutionScroll = new JScrollPane(solutionArea);
        solutionScroll.setBorder(BorderFactory.createTitledBorder("Your Solution (write code)"));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        submitBtn = new JButton("Submit Solution");
        nextBtn = new JButton("Next Problem");
        nextBtn.setEnabled(false);
        
        submitBtn.addActionListener(e -> checkSolution());
        nextBtn.addActionListener(e -> loadNextProblem());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(nextBtn);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, problemScroll, solutionScroll);
        splitPane.setDividerLocation(200);
        
        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadProblem();
    }
    
    private void initializeProblems() {
        problems = new ArrayList<>();
        
        problems.add(new Problem(
            "Write a function to find the sum of all even numbers from 1 to N.",
            new String[]{"sum", "even", "loop"}
        ));
        
        problems.add(new Problem(
            "Write a function to reverse a string.",
            new String[]{"reverse", "string", "char"}
        ));
        
        problems.add(new Problem(
            "Write a function to check if a number is prime.",
            new String[]{"prime", "if", "return"}
        ));
        
        Collections.shuffle(problems);
    }
    
    private void loadProblem() {
        if (currentProblemIndex < problems.size()) {
            Problem p = problems.get(currentProblemIndex);
            progressLabel.setText("Problem " + (currentProblemIndex + 1) + " of " + problems.size());
            problemArea.setText(p.getDescription());
            solutionArea.setText("");
            submitBtn.setEnabled(true);
            nextBtn.setEnabled(false);
        }
    }
    
    private void checkSolution() {
        String solution = solutionArea.getText().toLowerCase();
        
        if (solution.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write a solution!");
            return;
        }
        
        Problem p = problems.get(currentProblemIndex);
        boolean correct = p.checkSolution(solution);
        
        if (correct) {
            JOptionPane.showMessageDialog(this, "Good solution! +10 points");
        } else {
            JOptionPane.showMessageDialog(this, "Solution needs improvement. Partial credit given.");
        }
        
        gamePanel.submitAnswer(currentProblemIndex, solution, correct);
        
        submitBtn.setEnabled(false);
        nextBtn.setEnabled(true);
    }
    
    private void loadNextProblem() {
        currentProblemIndex++;
        if (currentProblemIndex < problems.size()) {
            loadProblem();
        } else {
            JOptionPane.showMessageDialog(this, "All problems completed!");
            gamePanel.completeGame();
        }
    }
    
    @Override
    public void cleanup() {
    }
}

class Problem {
    private String description;
    private String[] keywords;
    
    public Problem(String description, String[] keywords) {
        this.description = description;
        this.keywords = keywords;
    }
    
    public String getDescription() { return description; }
    
    public boolean checkSolution(String solution) {
        int matches = 0;
        for (String keyword : keywords) {
            if (solution.contains(keyword)) {
                matches++;
            }
        }
        return matches >= keywords.length / 2;
    }
}
