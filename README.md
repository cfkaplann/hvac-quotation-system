# HVAC Quotation Management System

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![React](https://img.shields.io/badge/React-18-blue)

A full-stack quotation management system built with **Spring Boot** and **React** to manage HVAC product configurations, dynamic pricing calculations and quotation generation.

This application digitizes quotation preparation workflows that are traditionally handled with Excel and manual calculations.

---

## 🚀 Technologies

### Backend
- Java 21
- Spring Boot
- REST API
- Apache POI (Excel operations)
- Maven

### Frontend
- React
- JavaScript
- Axios
- CSS

### Database
- SQLite

### Tools
- Git
- Maven
- npm

---

## 🏗 System Architecture

React Frontend  
↓  
REST API  
↓  
Spring Boot Backend  
↓  
Database  

Frontend communicates with the backend using REST APIs.

---

## 📦 Core Features

### Product Configuration
Users can configure HVAC products with different dimensions and parameters.

### Dynamic Pricing Engine
The pricing engine calculates product prices dynamically based on selected parameters.

### Customer Management
Customers can be created and managed within the system.

### Quotation Creation
Users can generate quotations by selecting products and configurations.

### Excel Price Import
Product price lists can be imported directly from Excel files.

### Excel Quotation Export
Quotations can be exported as Excel documents.

### Currency Support
The system includes exchange rate functionality for price calculations.

---

## 📂 Project Structure

```
hvac-quotation-system

backend
├── src
│   ├── api
│   ├── pricing
│   ├── repository
│   ├── importer
│   ├── dependency
│   └── bootstrap
├── resources
└── pom.xml

frontend
├── src
│   ├── components
│   ├── pages
│   ├── services
│   └── styles
├── public
└── package.json
```

---

## ⚙ Installation

### Clone Repository

```
git clone https://github.com/yourusername/hvac-quotation-system.git
```

---

### Backend Setup

```
cd backend
mvn spring-boot:run
```

Backend runs on:

```
http://localhost:8080
```

---

### Frontend Setup

```
cd frontend
npm install
npm start
```

Frontend runs on:

```
http://localhost:3000
```

---

## 📸 Screenshots

Add application screenshots here.

Example:

- Quotation creation screen
- Product selection modal
- Customer management page
- Generated quotation table

---

## 🧠 Business Logic

The application includes a pricing engine that calculates product prices based on configurable parameters such as:

- Width
- Height
- Diameter
- Slot count
- Product type

The pricing data can be imported from Excel files, allowing price updates without modifying the application code.

---

## 📌 Purpose of the Project

The goal of this project is to build a digital quotation management system for HVAC manufacturers and distributors.

Many companies still prepare quotations manually using Excel.  
This system replaces that workflow with a modern web-based application.

---

## 🚧 Future Improvements

Possible future features:

- User authentication
- PDF quotation export
- Online quotation sharing
- Cloud database support
- Multi-user support
- Admin panel

---

## 👨‍💻 Author

Developed by **Cebrail Kaplan**
