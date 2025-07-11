
# Oracle SpringBoot Angular Project

A comprehensive database management system integrating Oracle Database, Spring Boot, and Angular.

## Core Features

- User Management (CRUD, Roles, Privileges)
- DATA Backup and Recovery
- Security
- Query Optimization

## Technical Architecture

### Backend (Spring Boot)
- RESTful API endpoints with OpenAPI documentation
- Database transaction management with JPA/Hibernate
- Business logic implementation with service layer pattern
- Security middleware using Spring Security
- File handling services with multipart support
- Caching with Redis for improved performance

### Database (Oracle)
- Efficient data storage and retrieval
- Complex query optimization
- Data integrity and consistency
- Backup and recovery mechanisms
- Connection pooling with HikariCP

## Prerequisites

- Java 17+
- Oracle Database 23ai
- Maven 3.8+
- Docker (optional, for Oracle container)

## Getting Started

1. Configure Oracle Database:
   - Install Oracle Database 23ai or use Docker: `docker run -d -p 1521:1521 gvenzl/oracle-free:23-slim`
   - Update `application.properties` with your database credentials

2. Start Spring Boot backend:
   ```bash
   cd spring-oracle
   mvn spring-boot:run
   ```

3. Access the application at `http://localhost:8080`

## Testing

Run integration tests with Oracle database:
```bash
cd spring-oracle
mvn test
```

## CI/CD Pipeline

The project includes GitHub Actions workflow that:
- Sets up Oracle Database service
- Runs integration tests
- Builds the application
- Uploads test artifacts

## Environment Variables

For production, set these environment variables:

```env
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@//your-host:1521/your-service
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-password
```

## API Documentation

- Backend API documentation is available at `http://localhost:8080/swagger-ui.html`
- Detailed API documentation can be found in the `/docs` directory

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add feature'`)
4. Push to the branch (`git push origin feature/cc-feature`)
5. Open a Pull Request



## Acknowledgments
- Spring Boot team for the robust backend framework
- Oracle for the reliable database system
=======
# oracle-DBA_SpringBoot

