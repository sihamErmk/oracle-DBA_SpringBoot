# Oracle SpringBoot NextJS Project

A comprehensive database management system integrating Oracle Database, Spring Boot, and Next.js.

## Core Features

- User Management (CRUD, Roles, Privileges)
- DATA Backup and Recovery
- Security
- Query Optimization

## Technical Architecture

### Frontend (Next.js 13.5)
- Server-side rendering for optimal performance
- Responsive dashboard with real-time updates using Recharts
- Interactive data visualization components with Tailwind CSS
- Secure authentication and authorization with NextAuth.js
- Form validation using React Hook Form
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

- Node.js 18+
- Java 17+
- Oracle Database 19c+
- Maven 3.8+

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/project-name.git
   ```

2. Configure Oracle Database:
    - Create a new database schema
    - Update `application.properties` with your database credentials

3. Start Spring Boot backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. Install frontend dependencies and start Next.js:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. Access the application at `http://localhost:3000`

## Environment Variables

Create a `.env` file in the frontend directory:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your-secret-key
```

## API Documentation

- Backend API documentation is available at `http://localhost:8080/swagger-ui.html`
- Detailed API documentation can be found in the `/docs` directory

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Contributors

- [zachary013](https://github.com/zachary013) 
- [Sam-jab](https://github.com/Sam-jab) 

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Next.js team for the amazing framework
- Spring Boot team for the robust backend framework
- Oracle for the reliable database system