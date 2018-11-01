package net.dannysdesign.hourslogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

class HLWorkInfoCanvas extends JScrollPane {
    private HLClientObject client;
    private DefaultTableModel tableModel = new DefaultTableModel(new String[]{
            "Start Time",
            "End Time",
            "Duration",
            "Description",
            "Payment"
    }, 0);
    private JTable table;
    private ArrayList<HLWorkInfoCanvasEventListener> eventListeners = new ArrayList<>();

    HLWorkObject getSelectedWork() {
        if (table.getSelectedRowCount() > 0) return (HLWorkObject) client.workObjects.toArray()[table.getSelectedRow()];
        else return null;
    }

    HLWorkInfoCanvas() {
        init();
    }

    private void init() {
        setOpaque(true);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            for (HLWorkInfoCanvasEventListener l : eventListeners) {
                l.onChange(new HLWorkInfoCanvasEventObject(e.getFirstIndex()));
            }
        });

        getViewport().add(table);
    }

    void refresh(HLClientObject c) {
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

    void addListener(HLWorkInfoCanvasEventListener l) {
        eventListeners.add(l);
    }
}

interface HLWorkInfoCanvasEventListener {
    void onChange(HLWorkInfoCanvasEventObject o);
}

class HLWorkInfoCanvasEventObject {
    private int index;

    HLWorkInfoCanvasEventObject(int index) {
        this.index = index;
    }

    int getIndex() { return index; }
}