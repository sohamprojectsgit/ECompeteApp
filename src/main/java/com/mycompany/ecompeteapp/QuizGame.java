import javax.swing.*;
import java.awt.*;
import java.util.*;

class QuizGame extends AbstractGame {
    private java.util.List<QuizQuestion> questions;
    private int currentQuestionIndex;
    private JLabel questionLabel;
    private ButtonGroup answerGroup;
    private JRadioButton[] answerButtons;
    private JButton submitBtn;
    private JButton nextBtn;
    private JLabel progressLabel;
    
    public QuizGame(ClientGamePanel gamePanel) {
        super(gamePanel);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initializeQuestions();
        currentQuestionIndex = 0;
        
        // Question display
        JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
        
        progressLabel = new JLabel("Question 1 of " + questions.size());
        progressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        questionPanel.add(progressLabel, BorderLayout.NORTH);
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        
        // Answer options
        JPanel answerPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        answerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        answerGroup = new ButtonGroup();
        answerButtons = new JRadioButton[4];
        
        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new JRadioButton();
            answerButtons[i].setFont(new Font("Arial", Font.PLAIN, 14));
            answerGroup.add(answerButtons[i]);
            answerPanel.add(answerButtons[i]);
        }
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        submitBtn = new JButton("Submit Answer");
        nextBtn = new JButton("Next Question");
        nextBtn.setEnabled(false);
        
        submitBtn.addActionListener(e -> checkAnswer());
        nextBtn.addActionListener(e -> loadNextQuestion());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(nextBtn);
        
        add(questionPanel, BorderLayout.NORTH);
        add(answerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadQuestion();
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
        
        Collections.shuffle(questions);
    }
    
    private void loadQuestion() {
        if (currentQuestionIndex < questions.size()) {
            QuizQuestion q = questions.get(currentQuestionIndex);
            progressLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
            questionLabel.setText("<html>" + q.getQuestion() + "</html>");
            
            String[] options = q.getOptions();
            for (int i = 0; i < 4; i++) {
                answerButtons[i].setText(options[i]);
                answerButtons[i].setEnabled(true);
            }
            
            answerGroup.clearSelection();
            submitBtn.setEnabled(true);
            nextBtn.setEnabled(false);
        }
    }
    
    private void checkAnswer() {
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (answerButtons[i].isSelected()) {
                selected = i;
                break;
            }
        }
        
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select an answer!");
            return;
        }
        
        QuizQuestion q = questions.get(currentQuestionIndex);
        boolean correct = (selected == q.getCorrectIndex());
        
        if (correct) {
            JOptionPane.showMessageDialog(this, "Correct! +10 points");
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect! Correct answer: " + 
                q.getOptions()[q.getCorrectIndex()]);
        }
        
        gamePanel.submitAnswer(currentQuestionIndex, answerButtons[selected].getText(), correct);
        
        for (JRadioButton btn : answerButtons) {
            btn.setEnabled(false);
        }
        submitBtn.setEnabled(false);
        nextBtn.setEnabled(true);
    }
    
    private void loadNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            loadQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "Quiz completed!");
            gamePanel.completeGame();
        }
    }
    
    @Override
    public void cleanup() {
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