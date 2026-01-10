import org.json.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

class ContestData {
    private String contestName;
    private String gameType;
    private long startTime;
    private Map<String, ParticipantData> participants;
    
    public ContestData(String contestName, String gameType) {
        this.contestName = contestName;
        this.gameType = gameType;
        this.startTime = System.currentTimeMillis();
        this.participants = new HashMap<>();
    }
    
    public void updateParticipant(String name, ParticipantData data) {
        participants.put(name, data);
        saveToFile();
    }
    
    public List<ParticipantData> getLeaderboard() {
        List<ParticipantData> list = new ArrayList<>(participants.values());
        Collections.sort(list, (a, b) -> {
            if (b.getScore() != a.getScore()) {
                return b.getScore() - a.getScore();
            }
            return Long.compare(a.getCompletionTime(), b.getCompletionTime());
        });
        return list;
    }
    
    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("contestName", contestName);
            json.put("gameType", gameType);
            json.put("startTime", startTime);
            json.put("startTimeFormatted", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime)));
            
            JSONArray participantsArray = new JSONArray();
            for (ParticipantData p : participants.values()) {
                participantsArray.put(p.toJSON());
            }
            json.put("participants", participantsArray);
            json.put("totalParticipants", participants.size());
            
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
    
    public void saveToFile() {
        try {
            File dir = new File("contest_data");
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            String filename = "contest_data/" + contestName.replaceAll("[^a-zA-Z0-9]", "_") + 
                             "_" + System.currentTimeMillis() + ".json";
            
            FileWriter writer = new FileWriter(filename);
            writer.write(toJSON().toString(4));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void exportToFile(String filepath) {
        try {
            FileWriter writer = new FileWriter(filepath);
            writer.write(toJSON().toString(4));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getContestName() {
        return contestName;
    }
    
    public Map<String, ParticipantData> getParticipants() {
        return participants;
    }
}