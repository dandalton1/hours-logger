package net.dannysdesign.hourslogger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;

class HLWindow extends JFrame {
    private DefaultComboBoxModel<HLClientObject> clientModel = new DefaultComboBoxModel<>();
    private JComboBox<HLClientObject> clientChooser = new JComboBox<>(clientModel);
    private JLabel timeLabel = new JLabel("");
    private HLWorkObject workBuffer;
    private HLClientObject clientBuffer;
    private HLWorkInfoCanvas workInfo = new HLWorkInfoCanvas();

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
            timeLabel.setText(String.format("%02d:%02d:%02d (%s%s)", millis / 3600000,
                    (millis / 60000) % 60, (millis / 1000) % 60, Currency.getInstance(Locale.getDefault()).getSymbol(),
                    workBuffer.humanReadablePayment()));
            repaint();
        }
    });

    HLWindow() {
        super();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
    }

    void init() {

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        loadFile();

        setTitle("Hours Logger");

        JButton startStopButton = new JButton("Start");
        startStopButton.addActionListener(e -> {
            switch (e.getActionCommand()) {
                case "start": {
                    workBuffer = new HLWorkObject();
                    workBuffer.startTime = Instant.now();
                    workBuffer.parentObject = clientBuffer;

                    startStopButton.setActionCommand("stop");
                    startStopButton.setText("Stop");

                    timeThread.start();

                    break;
                }
                case "stop": {
                    timeLabel.setText("");

                    workBuffer.endTime = Instant.now();
                    workBuffer.calculate();

                    timeThread.interrupt();

                    workBuffer.description = JOptionPane.showInputDialog(null,
                            "Please describe the work you did. Do not use commas (,).",
                            "Enter description", JOptionPane.QUESTION_MESSAGE).replaceAll(",", "");

                    if (clientBuffer != null) {
                        clientBuffer.workObjects.add(workBuffer);

                        workInfo.refresh(clientBuffer);
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
        startStopButton.setEnabled(false);

        JButton addClient = new JButton("Add Client");
        addClient.addActionListener(e -> {
            if (e.getActionCommand().equals("add-client")) {
                HLClientObject client = new HLClientObject();
                client.name = JOptionPane.showInputDialog(null,
                        "Please enter the name of the client. Do not use commas (,).", "Please enter name",
                        JOptionPane.INFORMATION_MESSAGE);
                if (client.name != null) {
                    client.name = client.name.replaceAll(",", "");

                    try {
                        client.rate = Double.parseDouble(JOptionPane.showInputDialog(null,
                                "Enter an hourly rate for the client. Do not use currency signs.",
                                "Enter hourly wage", JOptionPane.QUESTION_MESSAGE).replaceAll(HLIOManager.
                                getCurrencySymbolAsRegex(), ""));

                        clientModel.addElement(client);
                        clientModel.setSelectedItem(client);
                        HLIOManager.data.clients.add(client);

                        save();
                    } catch (Exception ex) {
                        System.out.println("Unable to parse rate: " + ex.getMessage());
                    }
                }
            } else {
                System.err.println("Unknown action command on add client button: " + e.getActionCommand());
            }
        });
        addClient.setActionCommand("add-client");

        JButton forkButton = new JButton("Fork me on GitHub!");
        forkButton.addActionListener(e -> {
            if (e.getActionCommand().equals("fork")) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://github.com/dandalton1/hours-logger"));
                    } catch (Exception ex) {
                        System.err.println("Exception while opening GitHub: " + ex.getMessage());
                    }
                }
            }
        });
        forkButton.setActionCommand("fork");

        JButton removeWork = new JButton("Remove Selected Work");
        removeWork.addActionListener(e -> {
            if (e.getActionCommand().equals("rm-work")) {
                if (workInfo.getSelectedWork() != null) {
                    removeWork();
                }
            } else {
                System.err.println("Invalid action command on Remove Work button: " + e.getActionCommand());
            }
        });
        removeWork.setActionCommand("rm-work");
        removeWork.setBackground(new Color(0xA63446));
        removeWork.setForeground(Color.WHITE);
        removeWork.setEnabled(false);

        JButton removeClient = new JButton("Remove Client");
        removeClient.addActionListener(e -> {
            if (e.getActionCommand().equals("rm-client")) {
                if (clientChooser.getSelectedItem() != null) {
                    removeClient();
                }
            } else {
                System.err.println("Invalid action command on Remove Client button: " + e.getActionCommand());
            }
        });
        removeClient.setActionCommand("rm-client");
        removeClient.setBackground(new Color(0xA63446));
        removeClient.setForeground(Color.WHITE);
        removeClient.setEnabled(false);

        JButton export = new JButton("Export...");
        export.addActionListener(e -> {
            if (e.getActionCommand().equals("export")) {
                if (clientBuffer != null) export();
            } else {
                System.err.println("Invalid action command on export button: " + e.getActionCommand());
            }
        });
        export.setActionCommand("export");
        export.setEnabled(false);

        JLabel clientLabel = new JLabel("Clients:");
        clientLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        clientChooser.setMaximumSize(new Dimension(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds().width, 50));
        clientChooser.addItemListener(a -> {
            if (a.getItem() != null) {
                if (a.getItem() instanceof HLClientObject) {
                    clientBuffer = (HLClientObject) a.getItem();
                    workInfo.refresh(clientBuffer);
                    startStopButton.setEnabled(true);
                    removeClient.setEnabled(true);
                    export.setEnabled(true);
                    System.out.println("Changed selection to " + clientBuffer);
                } else {
                    workInfo = new HLWorkInfoCanvas();
                    startStopButton.setEnabled(false);
                    removeClient.setEnabled(false);
                    export.setEnabled(false);
                }
            } else {
                workInfo = new HLWorkInfoCanvas();
                startStopButton.setEnabled(false);
                removeClient.setEnabled(false);
                export.setEnabled(false);
            }
            repaint();
        });

        workInfo.addListener(o -> {
            if (o.getIndex() >= 0) {
                removeWork.setEnabled(true);
            } else {
                removeWork.setEnabled(false);
            }
        });

        for (HLClientObject c : HLIOManager.data.clients) {
            System.out.println("Adding " + c);
            clientModel.addElement(c);
        }
        if (HLIOManager.data.clients.size() > 0) {
            clientChooser.setSelectedItem(HLIOManager.data.clients.toArray()[HLIOManager.data.clients.size() - 1]);
        }

        buttonPanel.add(export);
        buttonPanel.add(addClient);
        buttonPanel.add(startStopButton);
        buttonPanel.add(forkButton);
        buttonPanel.add(removeWork);
        buttonPanel.add(removeClient);
        this.getContentPane().add(workInfo);
        this.getContentPane().add(timeLabel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.CENTER);
        this.getContentPane().add(clientLabel, BorderLayout.CENTER);
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

    private void removeWork() {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to get rid of " +
                "the current work? This is " + workInfo.getSelectedWork().humanReadableDuration() + " of work, " +
                "covering " + Currency.getInstance(Locale.getDefault()).getSymbol() +
                workInfo.getSelectedWork().humanReadablePayment() + ". There is no going back if you decide to go" +
                " through with this.", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            clientBuffer.workObjects.remove(workInfo.getSelectedWork());
            workInfo.refresh(clientBuffer);
            repaint();
            save();
            JOptionPane.showMessageDialog(this, "Work removed.", "Work removed.",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void removeClient() {
        if (clientChooser.getSelectedItem() instanceof HLClientObject) {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to get rid " +
                            "of the client " + clientBuffer + "? There is no going back if you decide to do this!",
                    "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                HLIOManager.data.clients.remove(clientBuffer);
                clientModel.removeElement(clientBuffer);
                if (HLIOManager.data.clients.size() > 0) {
                    clientChooser.setSelectedItem(HLIOManager.data.clients.toArray()[HLIOManager.data.clients.size() - 1]);
                } else {
                    workInfo.removeAll();
                    clientChooser.setSelectedItem(null);
                    clientBuffer = null;
                }
                repaint();
                save();
                JOptionPane.showMessageDialog(this, "Client data removed.", "Client removed.",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (clientChooser.getSelectedItem() != null) {
            System.err.println("Currently selected client is a " + clientChooser.getSelectedItem().getClass().getName());
        }
    }

    private void export() {
        JFileChooser f = new JFileChooser();
        f.setAcceptAllFileFilterUsed(false);
        f.setFileFilter(new FileNameExtensionFilter("LaTeX Files", "tex"));
        f.showSaveDialog(this);
        if (f.getSelectedFile() != null) {
            if (clientBuffer.workObjects.toArray().length > 0) {
                File file;
                if (!f.getSelectedFile().getName().contains(".tex")) {
                    file = new File(f.getSelectedFile().getAbsolutePath() + ".tex");
                } else {
                    file = f.getSelectedFile();
                }
                try {
                    HLWorkObject earliestWorkObject = (HLWorkObject) JOptionPane.showInputDialog(this,
                            "Choose the earliest job you want to include.", "Choose earliest job",
                            JOptionPane.QUESTION_MESSAGE, null, clientBuffer.workObjects.toArray(),
                            clientBuffer.workObjects.toArray()[0]);
                    HLIOManager.exportLaTeX(file, clientBuffer, earliestWorkObject);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Exported log could not be saved.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "You need work to export.");
            }
        }
    }
}
