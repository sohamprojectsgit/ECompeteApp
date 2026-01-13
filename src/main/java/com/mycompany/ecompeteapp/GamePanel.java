package com.mycompany.ecompeteapp;

import javax.swing.*;

abstract class GamePanel extends JPanel {
    protected MainFrame parent;
    
    public GamePanel(MainFrame parent) {
        this.parent = parent;
    }
    
    public abstract void cleanup();
}