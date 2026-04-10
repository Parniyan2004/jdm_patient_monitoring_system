# JDM Patient Monitoring System

A Java console application built for the Introduction to Software Engineering course at Zuyd University of Applied Sciences. It reads clinical data from Juvenile Dermatomyositis (JDM) patients and displays it in a structured way, similar to an Electronic Patient Dossier (EPD).

## What it does

The application loads patient data from CSV files and lets you browse it through a text menu. You can view:

- **Patient Information** – ID, name, and diagnosis
- **Lab Results** – latest value per test, grouped by category (Blood Chemistry, Haematology, Cytokines, etc.)
- **Biomarkers** – all recorded CXCL10 and Galectin-9 values with dates
- **CMAS Score History** – muscle strength scores from 2017 to 2023 with a simple bar chart
- **Disease Trend Overview** – compares early vs recent CMAS averages to show if the patient is improving, stable, or declining
- **Patient Summary Report** – a one-page overview with the latest scores, biomarkers, and overall status

## Dataset

The application uses six CSV files stored in a `data/` folder:

| File | Description |
|------|-------------|
| `Patient.csv` | Patient ID and name |
| `CMAS.csv` | CMAS muscle strength scores over time |
| `LabResultGroup.csv` | 26 lab result categories |
| `LabResults(EN).csv` | 124 lab test definitions with English names |
| `Measurement.csv` | 480 individual measurements with dates and values |

The dataset contains one anonymised patient (Patient X) with data spanning from 2017 to 2024.

## Project structure

All Java classes are in the `jdm` package:

| Class | Purpose |
|-------|---------|
| `Main` | Entry point, shows the menu and handles user input |
| `PatientMonitor` | Loads all CSV files and contains the logic for each menu option |
| `Patient` | Stores patient ID and name |
| `CmasScore` | Stores one CMAS score with date and category |
| `LabResultGroup` | Stores a lab result category (e.g. Cytokines, Haematology) |
| `LabResult` | Stores the definition of one lab test (name, unit, group) |
| `Measurement` | Stores one recorded value with date and lab result ID |

## How to run

1. Make sure you have Java installed (Java 8 or higher).
2. Clone this repository.
3. Make sure the `data/` folder with the CSV files is in the working directory.
4. Compile and run:

```
javac jdm/*.java
java jdm.Main
```

## Built with

- Java (standard library only, no external dependencies)
- Git for version control

## Team

- Thijs Crienen
- Arya Bagheri Lengeh
- Zahra Amiri

## Course

Introduction to Software Engineering – Applied Data Science and Artificial Intelligence, Zuyd University of Applied Sciences
