import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import org.json.*;

class StatsViewer extends JFrame {
    private JTable statsTable;
    private DefaultTableModel tableModel;
    
    public StatsViewer() {
        setTitle("Contest Statistics");
        setSize(800, 500);
        setLocationRelativeTo(null);
        
        String[] columns = {"Contest Name", "Game Type", "Date", "Participants", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        
        statsTable = new JTable(tableModel);
        statsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        statsTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        loadStatistics();
        
        JScrollPane scrollPane = new JScrollPane(statsTable);
        
        JPanel buttonPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn = new JButton("Close");
        
        refreshBtn.addActionListener(e -> loadStatistics());
        closeBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);
        
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void loadStatistics() {
        tableModel.setRowCount(0);
        
        File dir = new File("contest_data");
        if (!dir.exists()) {
            return;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                    JSONObject json = new JSONObject(content);
                    
                    tableModel.addRow(new Object[]{
                        json.getString("contestName"),
                        json.getString("gameType"),
                        json.getString("startTimeFormatted"),
                        json.getInt("totalParticipants"),
                        "View"
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "View" : value.toString());
            return this;
        }
    }
    
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "View" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isPushed) {
                // Show detailed view
                int row = statsTable.getSelectedRow();
                showContestDetails(row);
            }
            isPushed = false;
            return label;
        }
        
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
    
    private void showContestDetails(int row) {
        // Create detailed view window
        JFrame detailFrame = new JFrame("Contest Details");
        detailFrame.setSize(700, 500);
        detailFrame.setLocationRelativeTo(this);
        
        String[] columns = {"Rank", "Name", "Score", "Time", "Correct"};
        DefaultTableModel detailModel = new DefaultTableModel(columns, 0);
        JTable detailTable = new JTable(detailModel);
        
        // Load data from file
        File dir = new File("contest_data");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        
        if (files != null && row < files.length) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(files[row].toPath()));
                JSONObject json = new JSONObject(content);
                JSONArray participants = json.getJSONArray("participants");
                
                for (int i = 0; i < participants.length(); i++) {
                    JSONObject p = participants.getJSONObject(i);
                    detailModel.addRow(new Object[]{
                        i + 1,
                        p.getString("name"),
                        p.getInt("score"),
                        p.getString("completionTimeFormatted"),
                        p.getInt("correctAnswers") + "/" + p.getInt("totalAnswers")
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        JPanel buttonPanel = new JPanel();
        JButton exportBtn = new JButton("Export");
        JButton closeBtn = new JButton("Close");
        
        exportBtn.addActionListener(e -> exportContest(row));
        closeBtn.addActionListener(e -> detailFrame.dispose());
        
        buttonPanel.add(exportBtn);
        buttonPanel.add(closeBtn);
        
        detailFrame.setLayout(new BorderLayout());
        detailFrame.add(new JScrollPane(detailTable), BorderLayout.CENTER);
        detailFrame.add(buttonPanel, BorderLayout.SOUTH);
        detailFrame.setVisible(true);
    }
    
    private void exportContest(int row) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("contest_export.json"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File sourceFile = new File("contest_data").listFiles()[row];
            File targetFile = fileChooser.getSelectedFile();
            
            try {
                java.nio.file.Files.copy(sourceFile.toPath(), targetFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Exported successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
            }
        }
    }
}