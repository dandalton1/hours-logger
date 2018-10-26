package net.dannysdesign.hourslogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
                "Duration",
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
                workObject.humanReadableStartTime(),
                workObject.humanReadableEndTime(),
                workObject.humanReadableDuration(),
                workObject.description,
                Currency.getInstance(Locale.getDefault()).getSymbol() + String.format("%.02f",workObject.payment)
        });
    }
}
