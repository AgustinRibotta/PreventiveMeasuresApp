# CosatCal

**A desktop application for estimating laser-cutting costs.**

CosatCal is a Java desktop application designed to estimate the cost of cutting a part using an industrial laser-cutting machine. It calculates material costs, machine operating costs, and gas consumption to provide a practical cost estimate before production.

## Background and Purpose

The idea for CosatCal came from a real-world need identified at a previous workplace where laser cutting was part of the production process. Estimating the cost of cutting a specific part required considering several variables, including the material, thickness, dimensions, cutting time, and operating costs.

The goal was to develop a tool that could bring these variables together and provide a quick, consistent estimate to support production planning and cost evaluation.

CosatCal is an updated version of an earlier application that was already in use. The original version was developed in Python using Tkinter and pandas. This new version was rebuilt in Java with JavaFX, improving the application structure, maintainability, user interface, and data-handling process while preserving the core calculation logic.

**This is not just a theoretical project.** CosatCal addresses an actual production-related problem, and the application is already being used to support cost estimation in a real-world context.

## Key Features

- **Cost estimation:** Calculates material, machine, and gas costs for a laser-cut part.
- **Partial calculations:** Supports material-only calculations and machine-and-gas-only calculations when only the relevant input data is available.
- **Excel-based configuration:** Loads materials, thicknesses, prices, and calculation parameters from an external Excel workbook.
- **Dynamic data management:** New materials and thicknesses can be added to the workbook without modifying the application code.
- **Editable parameters:** Allows users to adjust prices and calculation parameters directly in the interface for the current session.
- **Data validation:** Identifies missing information and reports loading or calculation issues instead of returning misleading cost values.
- **Improved results presentation:** Displays the estimated total alongside a breakdown of the individual cost components.
- **Data reload:** Allows users to reload the Excel configuration without restarting the application, preserving the previous data if the reload fails.

## How It Works

The user selects a material and its thickness, then enters the available part dimensions and cutting time. The application determines which calculations can be performed based on the supplied information.

Depending on the available inputs, CosatCal calculates:

- **Full cost estimate:** Material, machine, and gas costs.
- **Material-only estimate:** Based on the part dimensions.
- **Machine and gas estimate:** Based on the cutting time.

The results include the calculated costs and a breakdown of the main calculation components, making the estimate easier to understand and verify.

## Data Management

All materials, thicknesses, prices, and calculation parameters are loaded from an external Excel workbook, `CosatCal_dati.xlsx`.

The application reads the workbook dynamically, allowing users to maintain the configuration without changing the source code. Changes made to values in the interface apply only to the current session and do not modify the original Excel file.

The data-loading process is designed to be robust: if the workbook cannot be loaded correctly, the application reports the problem and retains the previously loaded data.

## Technology Stack

- **Java 21** — Core application logic.
- **JavaFX 21** — Desktop user interface.
- **Apache POI 5.3.0** — Excel workbook processing.
- **JUnit 5** — Unit and integration-oriented testing.
- **Maven** — Dependency management and build automation.
- **Maven Shade Plugin** — Executable JAR packaging.
- **jpackage** — Windows application packaging with a bundled Java runtime.
- **GitHub Actions** — Automated testing, building, and Windows packaging.

## Architecture

The application follows a layered structure that separates responsibilities:

- **View:** Displays the interface and exposes user inputs.
- **Controller:** Validates inputs, coordinates calculations, and formats results.
- **Service:** Contains the cost calculation logic independently of the user interface.
- **Repository:** Loads and validates data from the Excel workbook.
- **Model:** Represents materials, thicknesses, parameters, parts, and calculation results.

This separation makes the application easier to maintain, test, and extend.

## Testing and Deployment

Unit tests cover the main calculation scenarios, including known reference values, missing inputs, zero cutting time, and partial calculations. Repository tests verify Excel data loading, validation errors, and the preservation of previously loaded data when a reload fails.

A GitHub Actions workflow automatically runs the tests, builds the application on Windows, and packages it with its Java runtime. The resulting artifact can be downloaded and run on a compatible Windows machine without requiring a separate Java installation.

## Future Development

A planned improvement is to introduce a list of calculated parts and generate a consolidated report containing the estimated costs for multiple items. The next step is to define how calculation parameters should be applied across the list.

## Project Goal

CosatCal aims to make laser-cutting cost estimation faster, more consistent, and easier to manage by turning production-related variables into a clear, accessible cost breakdown.
