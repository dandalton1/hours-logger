package net.dannysdesign.hourslogger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;

class HLWindow extends JFrame {
    private DefaultComboBoxModel<HLClientObject> clientModel = new DefaultComboBoxModel<>();
    private JComboBox<HLClientObject> clientChooser = new JComboBox<>(clientModel);
    private JLabel timeLabel = new JLabel("");
    private HLWorkObject workBuffer;
    private HLClientObject clientBuffer;

    private final Thread timeThread = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                break;
            }
            workBuffer.endTime = Instant.now();
            workBuffer.calculate();
            long millis = workBuffer.endTime.toEpochMilli() - workBuffer.startTime.toEpochMilli();
            timeLabel.setText(String.format("%02d:%02d:%02d (%s%.02f)", millis / 3600000,
                    (millis / 60000) % 60, (millis / 1000) % 60, Currency.getInstance(Locale.getDefault()).getSymbol(),
                    workBuffer.payment));
            repaint();
        }
    });

    HLWindow() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
    }

    void init() {
        loadFile();

        getContentPane().setPreferredSize(new Dimension(500,150));

        JButton showWorkDone = new JButton("Show work done for client");
        showWorkDone.setEnabled(false);
        showWorkDone.addActionListener(e -> {
            if (e.getActionCommand().equals("show-work")) {
                HLWorkInfoWindow workInfoWindow = new HLWorkInfoWindow(clientBuffer);
                workInfoWindow.init();
                workInfoWindow.showWindow();
            } else {
                System.err.println("Invalid action command on show work button: ");
            }
        });
        showWorkDone.setActionCommand("show-work");

        clientChooser.addItemListener(a -> {
            if (a.getItem() != null) {
                if (a.getItem() instanceof HLClientObject) {
                    clientBuffer = (HLClientObject) a.getItem();
                    showWorkDone.setEnabled(true);
                    System.out.println("Changed selection to " + clientBuffer);
                } else {
                    showWorkDone.setEnabled(false);
                }
            } else {
                showWorkDone.setEnabled(false);
            }
            repaint();
        });

        for (HLClientObject c : HLIOManager.data.clients) {
            clientModel.addElement(c);
            System.out.println("Adding " + c);
        }

        JButton startStopButton = new JButton("Start");
        startStopButton.addActionListener(e -> {
            switch (e.getActionCommand()) {
                case "start": {
                    workBuffer = new HLWorkObject();
                    workBuffer.startTime = Instant.now();

                    startStopButton.setActionCommand("stop");
                    startStopButton.setText("Stop");

                    timeThread.start();

                    break;
                }
                case "stop": {
                    workBuffer.endTime = Instant.now();
                    workBuffer.calculate();

                    timeThread.interrupt();

                    workBuffer.description = JOptionPane.showInputDialog(null,
                            "Please describe the work you did. Do not use commas (,).",
                            "Enter description", JOptionPane.QUESTION_MESSAGE).replaceAll(",", "");

                    if (clientBuffer != null) {
                        clientBuffer.workObjects.add(workBuffer);
                    }

                    save();

                    workBuffer = null;

                    startStopButton.setActionCommand("start");
                    startStopButton.setText("Start");

                    break;
                }
                default: {
                    System.err.println("Invalid action command on start/stop button: " + e.getActionCommand());
                }
            }
        });
        startStopButton.setActionCommand("start");

        JButton addClient = new JButton("Add Client");
        addClient.addActionListener(e -> {
            if (e.getActionCommand().equals("add-client")) {
                HLClientObject client = new HLClientObject();
                client.name = JOptionPane.showInputDialog(null,
                        "Please enter the name of the client. Do not use commas (,).", "Please enter name",
                        JOptionPane.INFORMATION_MESSAGE);
                client.name = client.name.replaceAll(",","");

                client.rate = Double.parseDouble(JOptionPane.showInputDialog(null,
                        "Enter an hourly rate for the client. Do not use currency signs.",
                        "Enter hourly wage", JOptionPane.QUESTION_MESSAGE).replaceAll(HLIOManager.
                        getCurrencySymbolAsRegex(), ""));

                clientModel.addElement(client);
                clientModel.setSelectedItem(client);
                HLIOManager.data.clients.add(client);

                save();
            } else {
                System.err.println("Unknown action command on add client button: " + e.getActionCommand());
            }
        });
        addClient.setActionCommand("add-client");

        JLabel clientLabel = new JLabel("Clients:");

        this.getContentPane().add(addClient);
        this.getContentPane().add(timeLabel);
        this.getContentPane().add(startStopButton);
        this.getContentPane().add(showWorkDone);
        this.getContentPane().add(clientLabel);
        this.getContentPane().add(clientChooser);
    }

    void showWindow() {
        this.pack();
        this.setVisible(true);
    }

    private void loadFile() {
        JFileChooser f = new JFileChooser();
        f.setAcceptAllFileFilterUsed(false);
        f.setFileFilter(new FileNameExtensionFilter("Comma Separated Values", "csv"));
        f.showDialog(null, "OK");
        if (f.getSelectedFile() != null) {
            if (f.getSelectedFile().exists()) {
                try {
                    HLIOManager.load(f.getSelectedFile());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error loading file: " + e.getMessage(),
                            "Error loading file", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                } catch (HLParseException h) {
                    JOptionPane.showMessageDialog(null, "File could not be parsed",
                            "Error loading file", JOptionPane.ERROR_MESSAGE);
                    System.exit(2);
                }
            } else {
                File file;
                if (!f.getSelectedFile().getName().contains(".csv")) {
                    file = new File(f.getSelectedFile().getAbsolutePath() + ".csv");
                } else {
                    file = f.getSelectedFile();
                }
                HLIOManager.file = file;
            }
        } else {
            System.exit(0);
        }
    }

    private void save() {
        try {
            System.out.println("saving");
            HLIOManager.save();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving file: " + e.getMessage(),
                    "Error saving file", JOptionPane.ERROR_MESSAGE);
        }
    }
}
