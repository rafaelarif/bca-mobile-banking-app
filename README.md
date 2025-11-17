# BCA Mobile Banking App

Mobile banking application for La Banque Canadienne de l'Agriculture (BCA).

## Project Structure

- `mobile/` - Flutter mobile application
- `backend/` - Spring Boot backend (WAR for Apache Tomcat)

## Getting Started

### Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the WAR file:
```bash
./mvnw clean package
```

3. Deploy the WAR file to Apache Tomcat:
   - Copy `target/bca-banking-backend-1.0.0.war` to Tomcat's `webapps/` directory
   - Start Tomcat server

4. The API will be available at: `http://localhost:8080/bca-banking-backend/api`

### Mobile App Setup

1. Navigate to the mobile directory:
```bash
cd mobile
```

2. Install dependencies:
```bash
flutter pub get
```

3. Run the app:
```bash
flutter run
```

## Features

- User authentication
- Account overview
- Transaction history
- Account balance inquiry
- Secure API communication

## Technology Stack

- **Mobile**: Flutter (Dart)
- **Backend**: Java Spring Boot (WAR)
- **Server**: Apache Tomcat

