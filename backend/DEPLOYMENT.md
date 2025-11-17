# Deployment Guide

## Building the WAR File

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the WAR file using Maven:
```bash
./mvnw clean package
```

3. The WAR file will be generated at:
   `target/bca-banking-backend-1.0.0.war`

## Deploying to Apache Tomcat

### Option 1: Manual Deployment

1. Stop Tomcat (if running):
```bash
# On Linux/Mac
$CATALINA_HOME/bin/shutdown.sh

# On Windows
%CATALINA_HOME%\bin\shutdown.bat
```

2. Copy the WAR file to Tomcat's webapps directory:
```bash
cp target/bca-banking-backend-1.0.0.war $CATALINA_HOME/webapps/
```

3. Start Tomcat:
```bash
# On Linux/Mac
$CATALINA_HOME/bin/startup.sh

# On Windows
%CATALINA_HOME%\bin\startup.bat
```

4. The application will be available at:
   `http://localhost:8080/bca-banking-backend/api`

### Option 2: Using Tomcat Manager

1. Access Tomcat Manager:
   `http://localhost:8080/manager/html`

2. Upload the WAR file through the web interface

3. Deploy the application

## Configuration

### Database Configuration

For production, update `application.properties` to use a production database:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bca_banking
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### JWT Secret

Change the JWT secret in `application.properties` for production:

```properties
jwt.secret=YourSecureSecretKeyHere
```

## Testing the Deployment

1. Test the login endpoint:
```bash
curl -X POST http://localhost:8080/bca-banking-backend/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}'
```

2. Use the returned token to access protected endpoints:
```bash
curl -X GET http://localhost:8080/bca-banking-backend/api/accounts \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Troubleshooting

- **Port conflicts**: Ensure port 8080 is available or change it in `application.properties`
- **Context path**: The application uses `/bca-banking-backend` as context path
- **CORS**: CORS is configured to allow all origins. Restrict this in production
- **Database**: H2 is used for development. Use a production database for deployment

