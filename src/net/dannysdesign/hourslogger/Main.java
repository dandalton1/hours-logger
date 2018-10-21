package net.dannysdesign.hourslogger;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HLWindow h = new HLWindow();
            h.init();
            h.showWindow();
        });
    }
}
