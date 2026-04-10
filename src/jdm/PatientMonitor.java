package jdm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PatientMonitor {

    private Patient patient;
    private ArrayList<LabResultGroup> groups;
    private ArrayList<LabResult> labResults;
    private ArrayList<Measurement> measurements;
    private ArrayList<CmasScore> cmasScores;

    private String dataFolder;
    private String cxcl10Id;
    private String galectin9Id;

    public PatientMonitor(String dataFolder) {
        this.dataFolder = dataFolder;
        this.groups = new ArrayList<LabResultGroup>();
        this.labResults = new ArrayList<LabResult>();
        this.measurements = new ArrayList<Measurement>();
        this.cmasScores = new ArrayList<CmasScore>();
    }

    public void loadData() throws IOException {
        patient = loadPatient(dataFolder + "Patient.csv");
        groups = loadGroups(dataFolder + "LabResultGroup.csv");
        labResults = loadLabResults(dataFolder + "LabResults(EN).csv");
        measurements = loadMeasurements(dataFolder + "Measurement.csv");
        cmasScores = loadCmas(dataFolder + "CMAS.csv");

        cxcl10Id = findBiomarkerId("cxcl10");
        galectin9Id = findBiomarkerId("galectin");
    }

    public void showPatientInfo() {
        printHeader("PATIENT INFORMATION");

        if (patient == null) {
            System.out.println("  No patient data found.");
            return;
        }

        System.out.printf("  %-11s: %s%n", "Patient ID", patient.getPatientId());
        System.out.printf("  %-11s: %s%n", "Name", patient.getName());
        System.out.printf("  %-11s: %s%n", "Diagnosis", "Juvenile Dermatomyositis (JDM)");
    }

    public void showLabResults() {
        printHeader("LAB RESULTS (latest value per test)");

        for (int g = 0; g < groups.size(); g++) {
            LabResultGroup group = groups.get(g);
            ArrayList<LabResult> inGroup = new ArrayList<LabResult>();

            for (int i = 0; i < labResults.size(); i++) {
                if (labResults.get(i).getGroupId().equals(group.getGroupId())) {
                    inGroup.add(labResults.get(i));
                }
            }

            if (inGroup.size() == 0) {
                continue;
            }

            boolean hasData = false;
            for (int i = 0; i < inGroup.size(); i++) {
                ArrayList<Measurement> ms = getMeasurementsFor(inGroup.get(i).getLabResultId());
                if (ms.size() > 0) {
                    hasData = true;
                    break;
                }
            }

            if (!hasData) {
                continue;
            }

            printSection(group.getGroupName());
            System.out.printf("  %-40s %-10s %-18s %s%n",
        "Test Name", "Unit", "Value", "Date");
            System.out.println("  " + "-".repeat(62));

            for (int i = 0; i < inGroup.size(); i++) {
                LabResult lr = inGroup.get(i);
                ArrayList<Measurement> ms = getMeasurementsFor(lr.getLabResultId());

                if (ms.size() == 0) {
                    continue;
                }

                Measurement latest = ms.get(ms.size() - 1);
                String unit = lr.getUnit();

                if (unit.equals("")) {
                    unit = "-";
                }

                System.out.printf("  %-40s %-10s %-18s %s%n",
                shorten(lr.getResultNameEn(), 40),
                shorten(unit, 10),
                shorten(latest.getValue(), 18),
                formatDateTime(latest.getDateTime()));
            }
        }
    }

    public void showBiomarkers() {
        printHeader("JDM BIOMARKERS");
        System.out.println("  These biomarkers help monitor how active the disease is.");
        System.out.println("  Higher values usually indicate more active disease.");
        System.out.println();
        System.out.println("  Note: TNFR2 is mentioned in the project case,");
        System.out.println("  but it is not present in this dataset.");
        System.out.println("  Only CXCL10 and Galectin-9 are available.");

        showOneBiomarker("CXCL10", cxcl10Id);
        showOneBiomarker("Galectin-9", galectin9Id);
    }

    public void showCmasHistory() {
        printHeader("CMAS SCORE HISTORY");
        System.out.println("  CMAS ranges from 0 to 52.");
        System.out.println("  A higher score means the patient is doing better.");
        System.out.println();

        if (cmasScores.size() == 0) {
            System.out.println("  No CMAS data found.");
            return;
        }

        System.out.printf("  %-20s  %-6s  %s%n", "Date", "Score", "Bar");
        System.out.println("  " + "-".repeat(62));

        for (int i = cmasScores.size() - 1; i >= 0; i--) {
            CmasScore cs = cmasScores.get(i);
            StringBuilder bar = new StringBuilder();

            for (int b = 0; b < cs.getScore() / 2; b++) {
                bar.append("=");
            }

            System.out.printf("  %-20s  %-6d  [%s]%n", cs.getDate(), cs.getScore(), bar.toString());
        }

        CmasScore oldest = cmasScores.get(0);
        CmasScore newest = cmasScores.get(cmasScores.size() - 1);
        int change = newest.getScore() - oldest.getScore();

        System.out.println();
        System.out.println("  Earliest : " + oldest.getScore() + "  (" + oldest.getDate() + ")");
        System.out.println("  Latest   : " + newest.getScore() + "  (" + newest.getDate() + ")");

        if (change >= 0) {
            System.out.println("  Change   : +" + change + " points across " + cmasScores.size() + " visits");
        } else {
            System.out.println("  Change   : " + change + " points across " + cmasScores.size() + " visits");
        }
    }

    public void showDiseaseTrend() {
        printHeader("DISEASE TREND OVERVIEW");
        System.out.println("  This overview compares early and recent CMAS scores.");
        System.out.println("  Latest biomarker values are shown for context.");

        printSection("CMAS Trend");

        if (cmasScores.size() < 4) {
            System.out.println("  Not enough CMAS data to calculate a trend.");
        } else {
            int half = cmasScores.size() / 2;

            int totalEarly = 0;
            for (int i = 0; i < half; i++) {
                totalEarly += cmasScores.get(i).getScore();
            }
            double avgEarly = (double) totalEarly / half;

            int totalLate = 0;
            for (int i = half; i < cmasScores.size(); i++) {
                totalLate += cmasScores.get(i).getScore();
            }
            double avgLate = (double) totalLate / (cmasScores.size() - half);

            double change = avgLate - avgEarly;

            if (change > 3) {
                System.out.println("  >> Improving - average CMAS increased by "
                        + formatDecimal(change) + " points");
            } else if (change < -3) {
                System.out.println("  >> Declining - average CMAS decreased by "
                        + formatDecimal(Math.abs(change)) + " points");
            } else {
                System.out.println("  >> Stable - average CMAS changed by only "
                        + formatDecimal(change) + " points");
            }
        }

        printSection("Latest Biomarker Values");
        System.out.println("  " + getLatestBiomarkerLine("CXCL10", cxcl10Id));
        System.out.println("  " + getLatestBiomarkerLine("Galectin-9", galectin9Id));

        printSection("How to Read This");
        System.out.println("  Rising CMAS + lower biomarkers  --> possible improvement");
        System.out.println("  Falling CMAS + higher biomarkers --> possible disease activity");
        System.out.println("  This is only a simple data summary.");
    }

    public void generateReport() {
        printHeader("PATIENT SUMMARY REPORT");

        String latestScore = "N/A";
        String latestDate = "";

        if (cmasScores.size() > 0) {
            CmasScore last = cmasScores.get(cmasScores.size() - 1);
            latestScore = String.valueOf(last.getScore());
            latestDate = last.getDate();
        }

        String conclusion = getConclusion();
        String icon;

        if (conclusion.equals("Improving")) {
            icon = "[+]";
        } else if (conclusion.equals("Needs Attention")) {
            icon = "[!]";
        } else {
            icon = "[=]";
        }

        System.out.println();
        System.out.println("  Patient    : " + patient.getName());
        System.out.println("  Diagnosis  : Juvenile Dermatomyositis (JDM)");
        System.out.println();
        System.out.println("  Latest CMAS score : " + latestScore + " / 52   (recorded: " + latestDate + ")");
        System.out.println("  Total measurements in dataset : " + measurements.size());
        System.out.println();
        System.out.println("  Key Biomarkers (most recent values)");
        System.out.println("    " + getLatestBiomarkerLine("CXCL10", cxcl10Id));
        System.out.println("    " + getLatestBiomarkerLine("Galectin-9", galectin9Id));
        System.out.println();
        System.out.println("=".repeat(65));
        System.out.println("  STATUS :  " + icon + "  " + conclusion.toUpperCase());
        System.out.println("=".repeat(65));
        System.out.println();
        System.out.println("  This report is based on the dataset only.");
        System.out.println("  It does not replace evaluation by a medical professional.");
    }

    private Patient loadPatient(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        reader.readLine();
        String line = reader.readLine();
        reader.close();

        if (line == null || line.trim().equals("")) {
            return null;
        }

        String[] parts = line.split(",", 2);
        if (parts.length < 2) {
            return null;
        }

        return new Patient(parts[0].trim(), parts[1].trim());
    }

    private ArrayList<LabResultGroup> loadGroups(String path) throws IOException {
        ArrayList<LabResultGroup> result = new ArrayList<LabResultGroup>();

        BufferedReader reader = new BufferedReader(new FileReader(path));
        reader.readLine();

        String line = reader.readLine();
        while (line != null) {
            if (!line.trim().equals("")) {
                String[] parts = line.split(",", 2);
                if (parts.length >= 2) {
                    result.add(new LabResultGroup(parts[0].trim(), parts[1].trim()));
                }
            }
            line = reader.readLine();
        }

        reader.close();
        return result;
    }

    private ArrayList<LabResult> loadLabResults(String path) throws IOException {
        ArrayList<LabResult> result = new ArrayList<LabResult>();

        BufferedReader reader = new BufferedReader(new FileReader(path));
        reader.readLine();

        String line = reader.readLine();
        while (line != null) {
            if (!line.trim().equals("")) {
                String[] parts = line.split(",", 6);

                if (parts.length >= 6) {
                    String labResultId = parts[0].trim();
                    String groupId = parts[1].trim();
                    String unit = parts[4].trim();
                    String nameEn = parts[5].trim();

                    if (nameEn.equals("")) {
                        nameEn = parts[3].trim();
                    }

                    result.add(new LabResult(labResultId, groupId, nameEn, unit));
                }
            }
            line = reader.readLine();
        }

        reader.close();
        return result;
    }

    private ArrayList<Measurement> loadMeasurements(String path) throws IOException {
        ArrayList<Measurement> result = new ArrayList<Measurement>();

        BufferedReader reader = new BufferedReader(new FileReader(path));
        reader.readLine();

        String line = reader.readLine();
        while (line != null) {
            if (!line.trim().equals("")) {
                String[] parts = line.split(",", 4);

                if (parts.length >= 4) {
                    result.add(new Measurement(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim()
                    ));
                }
            }
            line = reader.readLine();
        }

        reader.close();
        return result;
    }

    private ArrayList<CmasScore> loadCmas(String path) throws IOException {
        ArrayList<CmasScore> result = new ArrayList<CmasScore>();

        BufferedReader reader = new BufferedReader(new FileReader(path));
        String headerLine = reader.readLine();
        String rowHigh = reader.readLine();
        String rowLow = reader.readLine();
        reader.close();

        if (headerLine == null || rowHigh == null || rowLow == null) {
            return result;
        }

        String[] dates = headerLine.split(",", -1);
        String[] highVals = rowHigh.split(",", -1);
        String[] lowVals = rowLow.split(",", -1);

        for (int col = 1; col < dates.length; col++) {
            String date = dates[col].trim();
            date = formatCmasDate(date);

            if (date.equals("") || date.equals("points")) {
                continue;
            }

            if (col < highVals.length && !highVals[col].trim().equals("")) {
                int score = Integer.parseInt(highVals[col].trim());
                result.add(new CmasScore(date, score, "CMAS Score > 10"));
            } else if (col < lowVals.length && !lowVals[col].trim().equals("")) {
                int score = Integer.parseInt(lowVals[col].trim());
                result.add(new CmasScore(date, score, "CMAS Score 4-9"));
            }
        }

        Collections.sort(result, new Comparator<CmasScore>() {
            @Override
            public int compare(CmasScore a, CmasScore b) {
                return a.getDate().compareTo(b.getDate());
            }
        });

        return result;
    }

    private ArrayList<Measurement> getMeasurementsFor(String labResultId) {
        ArrayList<Measurement> result = new ArrayList<Measurement>();

        for (int i = 0; i < measurements.size(); i++) {
            if (measurements.get(i).getLabResultId().equals(labResultId)) {
                result.add(measurements.get(i));
            }
        }

        return result;
    }

    private String findBiomarkerId(String keyword) {
        for (int i = 0; i < labResults.size(); i++) {
            String name = labResults.get(i).getResultNameEn().toLowerCase();
            if (name.contains(keyword.toLowerCase())) {
                return labResults.get(i).getLabResultId();
            }
        }
        return null;
    }

    private String getUnit(String labResultId) {
        for (int i = 0; i < labResults.size(); i++) {
            if (labResults.get(i).getLabResultId().equals(labResultId)) {
                return labResults.get(i).getUnit();
            }
        }
        return "";
    }

    private void showOneBiomarker(String name, String id) {
        printSection(name);

        if (id == null) {
            System.out.println("  Not found in this dataset.");
            return;
        }

        ArrayList<Measurement> ms = getMeasurementsFor(id);

        if (ms.size() == 0) {
            System.out.println("  No measurements recorded.");
            return;
        }

        String unit = getUnit(id);

        System.out.printf("  %-25s %-15s %s%n", "Date / Time", "Value", "Unit");
        System.out.println("  " + "-".repeat(50));

        for (int i = 0; i < ms.size(); i++) {
            System.out.printf(
                    "  %-25s %-15s %s%n",
                    formatDateTime(ms.get(i).getDateTime()),
                    ms.get(i).getValue(),
                    unit
            );
        }
    }

    private String getLatestBiomarkerLine(String name, String id) {
        if (id == null) {
            return name + ": not found in this dataset";
        }

        Measurement last = getLatestMeasurement(id);

        if (last == null) {
            return name + ": no measurements recorded";
        }

        String unit = getUnit(id);

        if (unit.equals("")) {
            unit = "-";
        }

        return name + ": " + last.getValue() + " " + unit
            + "  (on " + formatDateTime(last.getDateTime()) + ")";
    }

    private String getConclusion() {
        if (cmasScores.size() < 4) {
            return "Insufficient data";
        }

        int latestScore = cmasScores.get(cmasScores.size() - 1).getScore();
        int quarter = cmasScores.size() / 4;

        if (quarter == 0) {
            return "Insufficient data";
        }

        int total = 0;
        for (int i = 0; i < quarter; i++) {
            total += cmasScores.get(i).getScore();
        }

        double baseline = (double) total / quarter;
        double improvement = latestScore - baseline;

        if (improvement > 5) {
            return "Improving";
        } else if (improvement < -5) {
            return "Needs Attention";
        } else {
            return "Stable";
        }
    }

    private String shorten(String text, int maxLength) {
        if (text == null) {
            text = "";
        }

        if (text.length() > maxLength) {
            return text.substring(0, maxLength);
        }

        while (text.length() < maxLength) {
            text = text + " ";
        }

        return text;
    }

    private String formatDateTime(String dateTime) {
        if (dateTime == null) {
            return "";
        }

        dateTime = dateTime.trim();

        if (dateTime.length() == 15) {
            return dateTime.substring(0, 10) + " " + dateTime.substring(10);
        }

        return dateTime;
    }

    private String formatCmasDate(String date) {
        if (date == null) {
            return "";
        }

        date = date.trim();

        if (date.contains("-")) {
            String[] parts = date.split("-");

            if (parts.length == 3) {
                String first = parts[0];
                String second = parts[1];
                String third = parts[2];

            if (first.length() == 4) {
                return first + "-" + padTwo(second) + "-" + padTwo(third);
            } else if (third.length() == 4) {
                return third + "-" + padTwo(second) + "-" + padTwo(first);
            }
        }
    }

    return date;
}

private String padTwo(String text) {
    if (text == null) {
        return "";
    }

    text = text.trim();

    if (text.length() == 1) {
        return "0" + text;
    }

    return text;
}

    private String formatDecimal(double value) {
        return String.format("%.1f", value).replace(',', '.');
    }

    private Measurement getLatestMeasurement(String labResultId) {
        ArrayList<Measurement> ms = getMeasurementsFor(labResultId);

        if (ms.size() == 0) {
           return null;
        }

        Measurement latest = ms.get(0);
        LocalDateTime latestDate = parseMeasurementDateTime(latest.getDateTime());

        for (int i = 1; i < ms.size(); i++) {
            Measurement current = ms.get(i);
            LocalDateTime currentDate = parseMeasurementDateTime(current.getDateTime());

            if (currentDate != null && latestDate != null) {
                if (currentDate.isAfter(latestDate)) {
                    latest = current;
                    latestDate = currentDate;
                }
            }
        }

        return latest;
    }

    private LocalDateTime parseMeasurementDateTime(String dateTime) {
       if (dateTime == null) {
           return null;
       }

        String formatted = formatDateTime(dateTime);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            return LocalDateTime.parse(formatted, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    private void printHeader(String title) {
        System.out.println();
        System.out.println("=".repeat(65));
        System.out.println("  " + title);
        System.out.println("=".repeat(65));
    }

    private void printSection(String title) {
        System.out.println();
        System.out.println("-".repeat(65));
        System.out.println("  " + title);
        System.out.println("-".repeat(65));
    }
}

