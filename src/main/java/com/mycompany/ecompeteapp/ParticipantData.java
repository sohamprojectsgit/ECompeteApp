package com.mycompany.ecompeteapp;

import org.json.*;
import java.util.*;

class ParticipantData {
    private String name;
    private int score;
    private long completionTime;
    private long startTime;
    private List<AnswerRecord> answers;
    
    public ParticipantData() {
        this.startTime = System.currentTimeMillis();
        this.answers = new ArrayList<>();
        this.score = 0;
        this.completionTime = 0;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public void setCompletionTime(long time) {
        this.completionTime = time;
    }
    
    public void addAnswer(JSONObject answerData) {
        try {
            AnswerRecord record = new AnswerRecord(
                answerData.getInt("questionId"),
                answerData.getString("answer"),
                answerData.getBoolean("correct"),
                answerData.getLong("timestamp")
            );
            answers.add(record);
            if (record.isCorrect()) {
                score += 10;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getName() {
        return name;
    }
    
    public int getScore() {
        return score;
    }
    
    public long getCompletionTime() {
        return completionTime;
    }
    
    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("score", score);
            json.put("completionTime", completionTime);
            json.put("completionTimeFormatted", formatTime(completionTime));
            json.put("totalAnswers", answers.size());
            
            int correct = 0;
            for (AnswerRecord a : answers) {
                if (a.isCorrect()) correct++;
            }
            json.put("correctAnswers", correct);
            
            JSONArray answersArray = new JSONArray();
            for (AnswerRecord a : answers) {
                answersArray.put(a.toJSON());
            }
            json.put("answers", answersArray);
            
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
    
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

