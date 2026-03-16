package com.teklif.ui;

import javax.swing.*;

import com.teklif.ui.workspace.TeklifWorkspace;

public class MainFrame extends JFrame {

    public MainFrame(){

        setTitle("Teklif Programı ERP");
        setSize(1200,720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setContentPane(new TeklifWorkspace());
    }
}
