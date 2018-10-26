package net.dannysdesign.hourslogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class HLWorkInfoWindow extends JFrame {
    private HLClientObject client;
    private DefaultTableModel tableModel;

    HLWorkInfoWindow(HLClientObject c) {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
        this.client = c;
    }

    void init() {
        tableModel = new DefaultTableModel();
        tableModel.setColumnCount(4);

        for (HLWorkObject w : client.workObjects) {
            addRow(w);
        }

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setEnabled(false);

        this.getContentPane().add(table);
    }

    private void addRow(HLWorkObject workObject) {
        tableModel.addRow(new Object[]{
                workObject.startTime,
                workObject.endTime,
                workObject.description,
                workObject.payment
        });
    }

    void showWindow() {
        this.pack();
        this.setVisible(true);
    }
}
