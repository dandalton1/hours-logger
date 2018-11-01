package net.dannysdesign.hourslogger;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
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

    static void exportLaTeX(File file, HLClientObject clientObject, HLWorkObject earliestWorkObject) throws IOException {
        FileWriter w = new FileWriter(file, false);
        String clientStreetAddress = JOptionPane.showInputDialog(null, "Please enter the street address" +
                " of " + clientObject.name + ".");
        String clientState = JOptionPane.showInputDialog(null, "Please enter the State or Providence " +
                clientObject.name + " lives in.");
        String clientZip = JOptionPane.showInputDialog(null, "Please enter the ZIP Code of " +
                clientObject.name + ".");
        String clientCountry = JOptionPane.showInputDialog(null, "Please enter the Country of " +
                clientObject.name + ".");

        String name = JOptionPane.showInputDialog(null, "Please enter your name or company name.");
        String streetAddress = JOptionPane.showInputDialog(null, "Please enter your street address.");
        String state = JOptionPane.showInputDialog(null, "Please enter your State/Providence.");
        String zip = JOptionPane.showInputDialog(null, "Please enter your ZIP code.");
        String country = JOptionPane.showInputDialog(null, "Please enter your Country of residence.");

        w.write("\\documentclass{letter}\n" +
                "\\usepackage{hyperref}\n" +
                "\\signature{" + name + "}\n" +
                "\\address{" + streetAddress + " \\\\ " + state + " " + zip + " \\\\ }\n" +
                "\\begin{document}\n");

        w.write("\\begin{letter}{" + LaTeXify(clientObject.name) + " \\\\ " + LaTeXify(clientStreetAddress) + " \\\\ "
                + LaTeXify(clientState) + " " + LaTeXify(clientZip) + " \\\\ " + LaTeXify(clientCountry)  + "}\n");
        w.write("\\opening{Dear Sir or Madam,}\n");


        Date earliestWorkDate = (new Date(earliestWorkObject.startTime.toEpochMilli()));
        w.write("I am writing to you, your invoice for all work done since " + earliestWorkDate.toString() + ".\\\\" +
                "\n\\\\\n\\\\\n");

        w.write("\\begin{tabular}{l|l|l|l|l}\n");
        w.write("\\textit{Start Time} &\\textit{End Time} &\\textit{Duration} &\\textit{Description} " +
                "&\\textit{Price}\\\\\n\\hline\n");

        clientObject.workObjects.sort(HLWorkObject::compareTo);

        double payment = 0.;

        for (HLWorkObject workObject : clientObject.workObjects) {
            if (earliestWorkObject.compareTo(workObject) < 0) {
                continue;
            }
            w.write(workObject.humanReadableStartTime() + "&" + workObject.humanReadableEndTime() + "&" +
                    workObject.humanReadableDuration() + "&" + LaTeXify(workObject.description) + "&" +
                    getCurrencySymbolAsRegex() + workObject.payment + "\\\\\n");
            payment += workObject.payment;
        }

        w.write("\\end{tabular}\n\\\\\n");
        w.write("\\\\\n\\\\\nThe total amount owed for the work totals to " + getCurrencySymbolAsRegex() +
                payment + ". Please, get that to me as soon as you can.\\\\\n");

        w.write("\\\\\n");

        w.write("\\closing{Best Regards,}\n\n");

        w.write("\\ps\n\nThis invoice was generated by Danny's Website Design's own Hourly Logger tool, available on " +
                "GitHub, at the following URL: \\\\ https://github.com/dandalton1/hours-logger\\\\\n");

        w.write("\\end{letter}\n\\end{document}\n");

        w.flush();
        w.close();
    }

    private static String LaTeXify(String original) {
        return original.replaceAll("&", "\\\\&");
    }
}

class HLParseException extends Exception {
    HLParseException() { super("Error parsing file"); }
}