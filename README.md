# Backend

Spring Boot 3.5 (Java 21), Spring Security, JPA, MySQL. REST APIs under `/api/...`.

Run locally (MySQL must be running):
    cd backend
    mvn spring-boot:run
Open http://localhost:8080

The build bundles the `../frontend` folder into the jar, so one process serves both the pages and the APIs.

Environment variables (all optional locally): MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD, ADMIN_USERNAME, ADMIN_PASSWORD, PORT.
