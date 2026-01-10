// AnswerRecord.java - Individual answer record
import org.json.*;

class AnswerRecord {
    private int questionId;
    private String answer;
    private boolean correct;
    private long timestamp;
    
    public AnswerRecord(int questionId, String answer, boolean correct, long timestamp) {
        this.questionId = questionId;
        this.answer = answer;
        this.correct = correct;
        this.timestamp = timestamp;
    }
    
    public boolean isCorrect() {
        return correct;
    }
    
    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("questionId", questionId);
            json.put("answer", answer);
            json.put("correct", correct);
            json.put("timestamp", timestamp);
            return json;
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
