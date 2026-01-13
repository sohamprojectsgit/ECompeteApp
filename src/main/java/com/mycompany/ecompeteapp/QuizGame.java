package com.mycompany.ecompeteapp;

import javax.swing.*;
import java.awt.*;
import java.util.*;

class QuizGame extends AbstractGame {
    private java.util.List<QuizQuestion> questions;
    private int currentQuestionIndex;
    private JLabel questionLabel;
    private JLabel progressLabel;
    private ButtonGroup answerGroup;
    private JRadioButton[] answerButtons;
    private JButton submitBtn;
    private JButton nextBtn;
    private volatile boolean isCleanedUp = false;
    
    public QuizGame(ClientGamePanel gamePanel) {
        super(gamePanel);
        setLayout(new BorderLayout());
        
        initializeQuestions();
        currentQuestionIndex = 0;
        
        createUI();
        loadQuestion();
    }
    
    private void createUI() {
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Progress label
        progressLabel = new JLabel("Question 1 of " + questions.size());
        progressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Question panel
        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBorder(BorderFactory.createTitledBorder("Question"));
        
        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        
        // Answer panel
        JPanel answerPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        answerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Options"),
            BorderFactory.createEmptyBorder(5, 10, 10, 10)
        ));
        
        answerGroup = new ButtonGroup();
        answerButtons = new JRadioButton[4];
        
        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new JRadioButton();
            answerButtons[i].setFont(new Font("Arial", Font.PLAIN, 14));
            answerGroup.add(answerButtons[i]);
            answerPanel.add(answerButtons[i]);
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        submitBtn = new JButton("Submit Answer");
        submitBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        submitBtn.addActionListener(e -> checkAnswer());
        
        nextBtn = new JButton("Next Question");
        nextBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> loadNextQuestion());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(nextBtn);
        
        // Add components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(progressLabel, BorderLayout.NORTH);
        topPanel.add(questionPanel, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(answerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void initializeQuestions() {
        questions = new ArrayList<>();
        
        questions.add(new QuizQuestion(
            "What is the time complexity of binary search?",
            new String[]{"O(n)", "O(log n)", "O(n²)", "O(1)"},
            1
        ));
        
        questions.add(new QuizQuestion(
            "Which data structure uses LIFO (Last In First Out)?",
            new String[]{"Queue", "Stack", "Tree", "Graph"},
            1
        ));
        
        questions.add(new QuizQuestion(
            "What does OOP stand for?",
            new String[]{"Object Oriented Programming", "Object Oriented Process", 
                        "Only One Program", "Open Operation Protocol"},
            0
        ));
        
        questions.add(new QuizQuestion(
            "Which sorting algorithm has the best average case performance?",
            new String[]{"Bubble Sort", "Quick Sort", "Selection Sort", "Insertion Sort"},
            1
        ));
        
        questions.add(new QuizQuestion(
            "What is the default port for HTTP?",
            new String[]{"443", "8080", "80", "22"},
            2
        ));
        
        questions.add(new QuizQuestion(
            "Which keyword is used for inheritance in Java?",
            new String[]{"implements", "extends", "inherits", "super"},
            1
        ));
        
        questions.add(new QuizQuestion(
            "What does SQL stand for?",
            new String[]{"Structured Query Language", "Simple Query Language",
                        "Standard Question Language", "System Query Logic"},
            0
        ));
        
        questions.add(new QuizQuestion(
            "Which is NOT a primitive data type in Java?",
            new String[]{"int", "boolean", "String", "char"},
            2
        ));
        
        questions.add(new QuizQuestion(
            "What is polymorphism in OOP?",
            new String[]{"Multiple forms of a single entity", "Single form of multiple entities",
                        "Data hiding", "Code reusability"},
            0
        ));
        
        questions.add(new QuizQuestion(
            "Which collection maintains insertion order in Java?",
            new String[]{"HashSet", "TreeSet", "LinkedHashSet", "Set"},
            2
        ));
        
        Collections.shuffle(questions);
    }
    
    private void loadQuestion() {
        if (isCleanedUp || currentQuestionIndex >= questions.size()) return;
        
        QuizQuestion q = questions.get(currentQuestionIndex);
        
        progressLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
        questionLabel.setText("<html>" + q.getQuestion() + "</html>");
        
        String[] options = q.getOptions();
        for (int i = 0; i < 4; i++) {
            answerButtons[i].setText(options[i]);
            answerButtons[i].setEnabled(true);
            answerButtons[i].setBackground(null);
        }
        
        answerGroup.clearSelection();
        submitBtn.setEnabled(true);
        nextBtn.setEnabled(false);
    }
    
    private void checkAnswer() {
        if (isCleanedUp) return;
        
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (answerButtons[i].isSelected()) {
                selected = i;
                break;
            }
        }
        
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an answer!", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        QuizQuestion q = questions.get(currentQuestionIndex);
        boolean correct = (selected == q.getCorrectIndex());
        
        // Visual feedback
        answerButtons[selected].setBackground(correct ? new Color(144, 238, 144) : new Color(255, 182, 193));
        if (!correct) {
            answerButtons[q.getCorrectIndex()].setBackground(new Color(144, 238, 144));
        }
        
        // Show result
        if (correct) {
            JOptionPane.showMessageDialog(this, 
                "Correct! +10 points", 
                "Correct Answer", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Incorrect!\n\nCorrect answer: " + q.getOptions()[q.getCorrectIndex()], 
                "Wrong Answer", 
                JOptionPane.ERROR_MESSAGE);
        }
        
        // Submit to server
        gamePanel.submitAnswer(currentQuestionIndex, answerButtons[selected].getText(), correct);
        
        // Disable interaction
        for (JRadioButton btn : answerButtons) {
            btn.setEnabled(false);
        }
        submitBtn.setEnabled(false);
        nextBtn.setEnabled(true);
        nextBtn.requestFocus();
    }
    
    private void loadNextQuestion() {
        if (isCleanedUp) return;
        
        currentQuestionIndex++;
        
        if (currentQuestionIndex < questions.size()) {
            loadQuestion();
        } else {
            // All questions completed
            JOptionPane.showMessageDialog(this, 
                "All questions completed!\n\nGreat job!", 
                "Quiz Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            gamePanel.completeGame();
        }
    }
    
    @Override
    public void cleanup() {
        if (isCleanedUp) return;
        isCleanedUp = true;
        
        // Disable all UI components
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
        
        if (answerButtons != null) {
            for (JRadioButton btn : answerButtons) {
                if (btn != null) {
                    btn.setEnabled(false);
                }
            }
        }
        
        // Clear data
        questions = null;
        answerGroup = null;
        
        System.out.println("QuizGame cleaned up");
    }
}

class QuizQuestion {
    private String question;
    private String[] options;
    private int correctIndex;
    
    public QuizQuestion(String question, String[] options, int correctIndex) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }
    
    public String getQuestion() { return question; }
    public String[] getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
}