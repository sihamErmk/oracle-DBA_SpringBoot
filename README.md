
# Oracle SpringBoot Angular Project

A comprehensive database management system integrating Oracle Database, Spring Boot, and Angular.

## Core Features

- User Management (CRUD, Roles, Privileges)
- DATA Backup and Recovery
- Security
- Query Optimization

## Technical Architecture

### Frontend (Angular)
- Server-side rendering for optimal performance
- Responsive dashboard with real-time updates using Ngcharts
- Interactive data visualization components with Tailwind CSS
- Secure authentication and authorization 
- Form validation 
- Type-safe development with TypeScript

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

- Angular 18+
- Java 17+
- Oracle Database 23ai
- Maven 3.8+

## Getting Started



1. Configure Oracle Database:
    - Create a new database schema
    - Update `application.properties` with your database credentials

2. Start Spring Boot backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. Install frontend dependencies and start Angular:
   ```bash
   cd frontend
   npm install
   ng serve
   ```

4. Access the application at `http://localhost:4200`

## Environment Variables

Create a `.env` file in the frontend directory:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXTAUTH_URL=http://localhost:4200
NEXTAUTH_SECRET=your-secret-key
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

