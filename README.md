# HVAC Quotation Management System

A full-stack quotation management platform developed to automate quotation preparation processes for HVAC products.

Many companies still prepare quotations manually using Excel and manual calculations.  
This system digitalizes the process by enabling dynamic product configuration, automated pricing calculations and quotation generation.

---

## Technologies

### Backend
- Java
- Spring Boot
- REST API
- Apache POI
- SQLite

### Frontend
- React
- JavaScript
- Axios
- CSS

---

## Features

- Dynamic HVAC product configuration
- Measurement-based pricing engine
- Automated quotation calculation
- Excel price list import
- Excel quotation export
- REST API architecture
- Modular backend structure

---

## System Architecture

```
React Frontend (localhost:3000)
            ↓
Spring Boot REST API (localhost:8080)
            ↓
SQLite Database
```

---

## Project Structure

```
hvac-quotation-system
│
├── backend
│   ├── src
│   ├── resources
│   └── pom.xml
│
├── frontend
│   ├── src
│   ├── public
│   └── package.json
│
└── README.md
```

---

## Installation

### Clone the repository

```
git clone https://github.com/cfkaplann/hvac-quotation-system.git
```

---

### Backend Setup

Open the backend project in Eclipse as a Maven project.

Run the application:

```
Run AppStarter.java
```

Backend will run at:

```
http://localhost:8080
```

---

### Frontend Setup

Navigate to the frontend folder:

```
cd frontend
```

Install dependencies:

```
npm install
```

Start the application:

```
npm start
```

Frontend will run at:

```
http://localhost:3000
```

---

## Screenshots

Add application screenshots here to demonstrate the UI.

Example:

- Product configuration screen
- Quotation creation screen
- Generated quotation table

---

## Purpose of the Project

This project was developed to replace manual Excel-based quotation preparation with a modern web-based application.

The system provides automated pricing calculations, product configuration capabilities and structured quotation generation.

---

## Author

Developed by **Cebrail Kaplan**

GitHub:  
https://github.com/cfkaplann

LinkedIn:  
https://www.linkedin.com/in/cebrailkap
