package net.dannysdesign.hourslogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;

class HLIOManager {
    final private static String headerRow = "Start time, End time, Description, Payment";

    static HLDataObject data = new HLDataObject();
    static File file;

    static void load(File file) throws IOException, HLParseException {
        if (!file.getAbsolutePath().contains(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
        HLIOManager.file = file;
        Scanner s = new Scanner(file);
        HLClientObject clientBuffer = new HLClientObject();
        while (s.hasNext()) {
            String buffer = s.nextLine();
            if (!buffer.contains(headerRow)) {
                if (buffer.equals("")) {
                    data.clients.add(clientBuffer);
                    clientBuffer = new HLClientObject();
                } else if (clientBuffer.name == null) {
                    clientBuffer.name = buffer;
                } else if (clientBuffer.rate == Double.MIN_VALUE) {
                    try {
                        clientBuffer.rate = Double.parseDouble(buffer.split(",")[1]
                                .replaceAll(getCurrencySymbolAsRegex(), "")); // "Rate, $..."
                    } catch (NumberFormatException n) {
                        System.err.println("NumberFormatException: " + n.getMessage());
                        n.printStackTrace();
                        throw new HLParseException();
                    }
                } else {
                    try {
                        String[] workInfo = buffer.split(",");
                        HLWorkObject w = new HLWorkObject();
                        w.startTime = Instant.parse(workInfo[0]);
                        w.endTime = Instant.parse(workInfo[1]);
                        w.description = workInfo[2];
                        w.payment = Double.parseDouble(workInfo[3].replaceAll(getCurrencySymbolAsRegex(),
                                ""));
                        clientBuffer.workObjects.add(w);
                    } catch (Exception e) {
                        System.err.println("Exception in array parsing: " + e.getMessage());
                        e.printStackTrace();
                        throw new HLParseException();
                    }
                }
            }
        }
    }

    static void save() throws IOException {
        FileWriter w = new FileWriter(file, false);
        w.write(headerRow + "\n");
        for (HLClientObject c : data.clients) {
            w.write("\n");
            w.write(c.name + "\n");
            w.write("Rate, " + Currency.getInstance(Locale.getDefault()).getSymbol() + String.format("%.02f",c.rate) + "\n");
            for (HLWorkObject workObject : c.workObjects) {
                w.write(workObject.startTime.toString() + "," +
                        workObject.endTime.toString() + "," + workObject.description + "," +
                        Currency.getInstance(Locale.getDefault()).getSymbol() + String.format("%.02f",workObject.payment)
                        + "\n");
            }
        }
        w.flush();
        w.close();
    }

    static String getCurrencySymbolAsRegex() {
        String s = Currency.getInstance(Locale.getDefault()).getSymbol();
        return Matcher.quoteReplacement(s);
    }
}

class HLParseException extends Exception {
    HLParseException() { super("Error parsing file"); }
}