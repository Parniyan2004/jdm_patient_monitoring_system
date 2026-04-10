package jdm;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        
        PatientMonitor monitor = new PatientMonitor("data/");

        
        System.out.println("Loading data...");
        try {
            monitor.loadData();
        } catch (IOException e) {
            System.out.println("Error: could not load data files.");
            System.out.println("Make sure the 'data' folder is in your working directory.");
            System.out.println("Details: " + e.getMessage());
            return;
        }
        System.out.println("Data loaded successfully!");

        
        showMenu(monitor);
    }

    
    private static void showMenu(PatientMonitor monitor) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=".repeat(65));
            System.out.println("  JDM PATIENT MONITORING SYSTEM");
            System.out.println("=".repeat(65));
            System.out.println("  1. Patient Information");
            System.out.println("  2. Lab Results");
            System.out.println("  3. Biomarkers  (CXCL10, Galectin-9)");
            System.out.println("  4. CMAS Score History");
            System.out.println("  5. Disease Trend Overview");
            System.out.println("  6. Patient Summary Report");
            System.out.println("  0. Exit");
            System.out.println("=".repeat(65));
            System.out.print("  Your choice: ");

            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                monitor.showPatientInfo();
                waitForEnter(scanner);
            } else if (input.equals("2")) {
                monitor.showLabResults();
                waitForEnter(scanner);
            } else if (input.equals("3")) {
                monitor.showBiomarkers();
                waitForEnter(scanner);
            } else if (input.equals("4")) {
                monitor.showCmasHistory();
                waitForEnter(scanner);
            } else if (input.equals("5")) {
                monitor.showDiseaseTrend();
                waitForEnter(scanner);
            } else if (input.equals("6")) {
                monitor.generateReport();
                waitForEnter(scanner);
            } else if (input.equals("0")) {
                System.out.println();
                System.out.println("  Goodbye!");
                running = false;
            } else {
                System.out.println("  Please enter a number from 0 to 6.");
            }
        }

        scanner.close();
    }

    
    private static void waitForEnter(Scanner scanner) {
        System.out.println();
        System.out.print("Press ENTER to return to the main menu.");
        scanner.nextLine();
    }
}
