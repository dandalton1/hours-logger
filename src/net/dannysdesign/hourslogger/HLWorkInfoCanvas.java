package net.dannysdesign.hourslogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Currency;
import java.util.Locale;

class HLWorkInfoCanvas extends JScrollPane {
    private HLClientObject client;
    private DefaultTableModel tableModel = new DefaultTableModel(new String[]{
            "Start Time",
            "End Time",
            "Description",
            "Payment"
    }, 0);
    private JTable table;

    HLWorkInfoCanvas() {
        init();
    }

    private void init() {
        setOpaque(true);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setEnabled(false);

        getViewport().add(table);
    }

    void reinit(HLClientObject c) {
        this.client = c;

        tableModel = new DefaultTableModel(new String[]{
                "Start Time",
                "End Time",
                "Description",
                "Payment"
        }, 0);

        for (HLWorkObject w : client.workObjects) {
            addRow(w);
        }
        table.setModel(tableModel);
    }

    private void addRow(HLWorkObject workObject) {
        tableModel.addRow(new Object[]{
                workObject.startTime,
                workObject.endTime,
                workObject.description,
                Currency.getInstance(Locale.getDefault()).getSymbol() + workObject.payment
        });
    }
}
