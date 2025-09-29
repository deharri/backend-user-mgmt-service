# User Management Service

A Spring Boot microservice for managing user registration, authentication, and user data operations with email notifications and event-driven architecture.

## Features

- User registration and authentication
- Email verification and notifications
- Password reset functionality
- User profile management
- Event-driven architecture with Kafka
- RESTful API with Swagger documentation
- PostgreSQL database integration

## Tech Stack

- **Java** (17+)
- **Spring Boot** (3.x)
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **Apache Kafka**
- **Spring Mail**
- **Springdoc OpenAPI** (Swagger)
- **Maven/Gradle**

## Prerequisites

Before running this application, ensure you have:

- Java 17 or higher
- PostgreSQL 12 or higher
- Apache Kafka 3.x
- Maven 3.8+ or Gradle 7+
- SMTP server access (Gmail configured by default)

## Environment Setup

### 1. PostgreSQL Database

Create a database for the application:

```sql
CREATE DATABASE "deharri-user-mgmt-db";
```

### 2. Environment Variables

Set the following environment variables or check default values in properties file:

```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export DB_USER=your-db-username
export DB_PASSWORD=your-db-password
```

**Note:** For Gmail, you need to generate an [App Password](https://support.google.com/accounts/answer/185833) instead of using your regular password.

Optional environment variables with defaults:

```bash
export SERVER_PORT=8088                    # Default: 8088
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092  # Default: localhost:9092
```

### 3. Kafka Setup

Start Kafka and Zookeeper:

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

## Running the Application

### Using Maven

```bash
# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run
```

### Using Gradle

```bash
# Clean and build
./gradlew clean build

# Run the application
./gradlew bootRun
```

### Using JAR

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/user-management-service-0.0.1-SNAPSHOT.jar
```

## Configuration

The application can be configured through `application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/deharri-user-mgmt-db
    username: haris
    password: 123456
```

### Profile-based Configuration

You can use different profiles for different environments:

```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

API documentation is also available at:

```
http://localhost:8080/v3/api-docs
```

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/verify-email` | Verify email address | No |
| POST | `/api/auth/forgot-password` | Request password reset | No |
| POST | `/api/auth/reset-password` | Reset password with token | No |

### User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/me` | Get current user profile | Yes |
| PUT | `/api/users/me` | Update current user profile | Yes |
| DELETE | `/api/users/me` | Delete current user account | Yes |
| GET | `/api/users/{id}` | Get user by ID | Yes (Admin) |
| GET | `/api/users` | Get all users | Yes (Admin) |

## Request/Response Examples

### Register User

**Request:**
```bash
curl -X POST http://localhost:8088/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response:**
```json
{
  "message": "User registered successfully. Please check your email for verification.",
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

### Login

**Request:**
```bash
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600
}
```

## Kafka Events

The service publishes events to Kafka topics:

- `user.registered` - When a new user registers
- `user.updated` - When user profile is updated
- `user.deleted` - When a user account is deleted

## Database Schema

The application uses the following main entities:

- **User**: Stores user credentials and profile information
- **Role**: User roles (USER, ADMIN, etc.)
- **VerificationToken**: Email verification tokens
- **PasswordResetToken**: Password reset tokens

**Note:** The application is configured with `ddl-auto: create-drop` for development. Change this to `validate` or `update` for production.

## Security

- Passwords are encrypted using BCrypt
- JWT tokens for stateless authentication
- Email verification for new accounts
- Secure password reset flow
- CSRF protection enabled
- HTTPS recommended for production

## Error Handling

The API returns standard HTTP status codes:

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run with coverage
mvn test jacoco:report
```

## Monitoring

Health check endpoint:

```
GET http://localhost:8088/actuator/health
```

## Troubleshooting

### Database Connection Issues

- Ensure PostgreSQL is running on port 5432
- Verify database credentials in `application.yml`
- Check if the database `deharri-user-mgmt-db` exists

### Email Not Sending

- Verify `MAIL_USERNAME` and `MAIL_PASSWORD` environment variables
- For Gmail, ensure "Less secure app access" is enabled or use App Password
- Check SMTP settings and firewall rules

### Kafka Connection Issues

- Ensure Kafka is running on `localhost:9092`
- Verify Zookeeper is running
- Check Kafka logs for errors

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

**Developer:** Haris
**Project:** Deharri User Management Service

For questions or support, please open an issue in the repository.

## Roadmap

- [ ] OAuth2 integration (Google, Facebook)
- [ ] Two-factor authentication (2FA)
- [ ] User activity logging
- [ ] Rate limiting
- [ ] API versioning
- [ ] Internationalization (i18n)
- [ ] Docker containerization
- [ ] Kubernetes deployment configs