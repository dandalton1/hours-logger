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
        HLIOManager.file = file;
        Scanner s = new Scanner(file);
        HLClientObject clientBuffer = new HLClientObject();
        while (s.hasNext()) {
            String buffer = s.nextLine();
            if (!buffer.contains(headerRow)) {
                if (clientBuffer.name == null) {
                    clientBuffer.name = buffer;
                    System.out.println("Client name: " + clientBuffer.name);
                } else if (buffer.split(",").length >= 4) {
                    try {
                        String[] workInfo = buffer.split(",");
                        HLWorkObject w = new HLWorkObject();
                        w.startTime = Instant.parse(workInfo[0]);
                        w.endTime = Instant.parse(workInfo[1]);
                        w.description = workInfo[2];
                        w.payment = Double.parseDouble(workInfo[3].replaceAll(getCurrencySymbolAsRegex(),
                                ""));
                        System.out.println("Added work valuing " + w.payment + " for " + w.description);
                        clientBuffer.workObjects.add(w);
                    } catch (Exception e) {
                        System.err.println("Exception in array parsing: " + e.getMessage());
                        e.printStackTrace();
                        throw new HLParseException();
                    }
                } else if (buffer.split(",").length >= 2) {
                    try {
                        clientBuffer.rate = Double.parseDouble(buffer.split(",")[1]
                                .replaceAll(getCurrencySymbolAsRegex(), "")); // "Rate, $..."
                        System.out.println("Rate of client: " + clientBuffer.rate);
                    } catch (NumberFormatException n) {
                        System.err.println("NumberFormatException: " + n.getMessage());
                        n.printStackTrace();
                        throw new HLParseException();
                    }
                } else {
                    System.out.println("Client being added: " + clientBuffer.name + "; " + clientBuffer.rate);
                    data.clients.add(clientBuffer);
                    clientBuffer = new HLClientObject();
                }
            }
            else {
                // advance a line
                if (s.hasNext()) s.nextLine();
            }
        }
    }

    static void save() throws IOException {
        FileWriter w = new FileWriter(file, false);
        w.write(headerRow + "\n");
        for (HLClientObject c : data.clients) {
            w.write("\n");
            System.out.println("Saving work for " + c);
            w.write(c.name + "\n");
            w.write("Rate," + Currency.getInstance(Locale.getDefault()).getSymbol() + String.format("%.02f",c.rate) + "\n");
            for (HLWorkObject workObject : c.workObjects) {
                System.out.println("Saving work done with description: " + workObject.description);
                w.write(workObject.startTime.toString() + "," +
                        workObject.endTime.toString() + "," + workObject.description + "," +
                        Currency.getInstance(Locale.getDefault()).getSymbol() + String.format("%.02f",workObject.payment)
                        + "\n");
            }
            w.write("--\n");
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