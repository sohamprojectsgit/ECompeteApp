package com.mycompany.ecompeteapp;

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
        setSize(900, 500);
        setLocationRelativeTo(null);
        
        String[] columns = {"Contest Name", "Game Type", "Date", "Participants", "View", "Delete"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5;
            }
        };
        
        statsTable = new JTable(tableModel);
        statsTable.getColumn("View").setCellRenderer(new ButtonRenderer());
        statsTable.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox(), "View"));
        statsTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        statsTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), "Delete"));
        
        loadStatistics();
        
        JScrollPane scrollPane = new JScrollPane(statsTable);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton refreshBtn = new JButton("Refresh");
        JButton deleteAllBtn = new JButton("Delete All Data");
        JButton closeBtn = new JButton("Close");
        
        deleteAllBtn.setForeground(Color.RED);
        
        refreshBtn.addActionListener(e -> loadStatistics());
        deleteAllBtn.addActionListener(e -> deleteAllData());
        closeBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(deleteAllBtn);
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
                        "View",
                        "Delete"
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void deleteContest(int row) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this contest data?\nThis action cannot be undone!",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            File dir = new File("contest_data");
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            
            if (files != null && row < files.length) {
                if (files[row].delete()) {
                    JOptionPane.showMessageDialog(this, "Contest deleted successfully!");
                    loadStatistics();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete contest!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "WARNING: This will delete ALL contest data permanently!\n\n" +
            "Are you absolutely sure you want to continue?\n" +
            "This action CANNOT be undone!",
            "Delete All Data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Second confirmation
            int secondConfirm = JOptionPane.showConfirmDialog(
                this,
                "FINAL CONFIRMATION:\n\n" +
                "Delete " + tableModel.getRowCount() + " contest(s)?\n\n" +
                "Click YES to permanently delete all data.",
                "Final Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (secondConfirm == JOptionPane.YES_OPTION) {
                File dir = new File("contest_data");
                if (dir.exists()) {
                    File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
                    int deletedCount = 0;
                    int failedCount = 0;
                    
                    if (files != null) {
                        for (File file : files) {
                            if (file.delete()) {
                                deletedCount++;
                            } else {
                                failedCount++;
                            }
                        }
                    }
                    
                    String message = "Deleted " + deletedCount + " contest(s)";
                    if (failedCount > 0) {
                        message += "\nFailed to delete " + failedCount + " contest(s)";
                    }
                    
                    JOptionPane.showMessageDialog(this, message, "Delete Complete", 
                        failedCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
                    
                    loadStatistics();
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
            setText((value == null) ? "" : value.toString());
            
            // Color the Delete button red
            if (value != null && value.toString().equals("Delete")) {
                setForeground(Color.RED);
            } else {
                setForeground(Color.BLACK);
            }
            
            return this;
        }
    }
    
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private String buttonType;
        
        public ButtonEditor(JCheckBox checkBox, String buttonType) {
            super(checkBox);
            this.buttonType = buttonType;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            
            // Color the Delete button red
            if (label.equals("Delete")) {
                button.setForeground(Color.RED);
            } else {
                button.setForeground(Color.BLACK);
            }
            
            isPushed = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = statsTable.getSelectedRow();
                
                if (buttonType.equals("View")) {
                    showContestDetails(row);
                } else if (buttonType.equals("Delete")) {
                    deleteContest(row);
                }
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
            File dir = new File("contest_data");
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            
            if (files != null && row < files.length) {
                File sourceFile = files[row];
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
}