# Spring Boot + AWS RDS MySQL Integration

This project connects a Spring Boot REST API to an AWS RDS MySQL
database and provides endpoints to create and retrieve employee records.

## 1. Technology Stack

-   Java 25
-   Spring Boot 4.2.0
-   Spring Data JPA
-   Hibernate ORM
-   MySQL
-   AWS RDS for MySQL
-   Maven
-   Postman (API testing)

## 2. AWS RDS MySQL Configuration

  Setting             Value
  ------------------- ---------------------------------------------------
  Database engine     MySQL
  AWS Region          `ap-south-2`
  Endpoint            `mysql.czgcmu8y8c75.ap-south-2.rds.amazonaws.com`
  Port                `3306`
  Database name       `employeedb`
  Database username   `admin`

**Security note:** Do not commit your database password, AWS access
keys, or other credentials to Git. The database password should be
supplied through an environment variable or a secrets manager.

### RDS security group

The RDS security group must allow inbound MySQL traffic on TCP port
`3306` from the application host.

-   For local development, allow your current public IP address only.
-   If the Spring Boot application runs on EC2, preferably make RDS
    private and allow TCP `3306` from the EC2 instance's security group.
-   Avoid allowing `3306` from `0.0.0.0/0`.

A private RDS instance does not require an IAM access key and secret key
just because the application connects to it. Network access is
controlled by VPC routing and security groups; database authentication
is handled by the MySQL username/password (unless IAM database
authentication is deliberately configured).

## 3. Connect to RDS from MySQL CLI

Download the AWS RDS CA certificate bundle:

``` bash
curl -o global-bundle.pem https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem
```

Connect to the database:

``` bash
mysql -h mysql.czgcmu8y8c75.ap-south-2.rds.amazonaws.com \
  -P 3306 \
  -u admin \
  -p \
  --ssl-mode=VERIFY_IDENTITY \
  --ssl-ca=./global-bundle.pem
```

Enter the RDS database password when prompted.

## 4. Create the Database and Employee Table

Run these SQL commands after connecting to MySQL:

``` sql
CREATE DATABASE IF NOT EXISTS employeedb;

USE employeedb;

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(150) UNIQUE NOT NULL,
    department VARCHAR(100),
    salary DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Check the table:

``` sql
DESCRIBE employees;
SELECT * FROM employees;
```

## 5. Spring Boot Database Configuration

Configure the following properties in
`src/main/resources/application.properties`:

``` properties
spring.datasource.url=jdbc:mysql://mysql.czgcmu8y8c75.ap-south-2.rds.amazonaws.com:3306/employeedb
spring.datasource.username=admin
spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Set the database password in the terminal before starting the
application:

``` bash
export DB_PASSWORD='your-rds-password'
```

Then start the application:

``` bash
./mvnw spring-boot:run
```

If your project does not contain the Maven wrapper, use:

``` bash
mvn spring-boot:run
```

Do not put the real password in this README or commit it in
`application.properties`.

## 6. Employee Entity and Database Column Mapping

The Java entity must map to the existing MySQL column names. In
particular, Java's `firstName` property maps to the database column
`first_name`, and `lastName` maps to `last_name`.

Example:

``` java
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    private double salary;

    // Add a no-argument constructor, getters and setters.
}
```

The `email` field is required and unique in the database. Send a
different email address for each new employee.

## 7. Employee Repository

``` java
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
```

`JpaRepository` provides methods such as `save()`, `findAll()` and
`findById()`.

## 8. REST API Endpoints

Base URL:

``` text
http://localhost:8080/api/employees
```

### Create an employee

  Property       Value
  -------------- ---------------------------------------
  HTTP method    `POST`
  URL            `http://localhost:8080/api/employees`
  Content-Type   `application/json`

Request body:

``` json
{
  "firstName": "Madhuri",
  "lastName": "Reddy",
  "email": "madhuri@example.com",
  "salary": 60000.00
}
```

### Get all employees

  Property      Value
  ------------- ---------------------------------------
  HTTP method   `GET`
  URL           `http://localhost:8080/api/employees`

Example response:

``` json
[
  {
    "id": 1,
    "firstName": "Madhuri",
    "lastName": "Reddy",
    "email": "madhuri@example.com",
    "salary": 60000.0
  }
]
```

If no employees exist, the endpoint returns an empty JSON array:

``` json
[]
```

### Get an employee by ID

  Property      Value
  ------------- -----------------------------------------
  HTTP method   `GET`
  URL           `http://localhost:8080/api/employees/1`

Example response:

``` json
{
  "id": 1,
  "firstName": "Madhuri",
  "lastName": "Reddy",
  "email": "madhuri@example.com",
  "salary": 60000.0
}
```

The example controller throws a runtime exception if the requested ID
does not exist. You can later replace this with a proper `404 Not Found`
response.

## 9. Test with Postman

1.  Start the Spring Boot application.
2.  Open Postman.
3.  To insert a record, select `POST`, enter
    `http://localhost:8080/api/employees`, select **Body → raw → JSON**,
    and paste the create-employee JSON.
4.  To retrieve all records, select `GET` and enter
    `http://localhost:8080/api/employees`.
5.  To retrieve one record, select `GET` and enter
    `http://localhost:8080/api/employees/1`.

## 10. Verify Data in RDS

Connect to the RDS MySQL instance and run:

``` sql
USE employeedb;

SELECT * FROM employees;
```

The employee inserted through the POST endpoint should appear in the
result.

## 11. Request Flow

``` text
Postman
   |
   | HTTP request
   v
Spring Boot EmployeeController
   |
   v
EmployeeRepository (Spring Data JPA)
   |
   v
Hibernate ORM
   |
   | JDBC connection over TCP 3306
   v
AWS RDS MySQL (employeedb)
   |
   v
employees table
```

## 12. Common Troubleshooting

  ---------------------------------------------------------------------------------------
  Error / symptom                                     What to check
  --------------------------------------------------- -----------------------------------
  `Communications link failure` or connection timeout RDS endpoint and port, RDS status,
                                                      VPC routing, public/private
                                                      accessibility, and inbound
                                                      security-group rule

  `Access denied for user`                            MySQL username/password and
                                                      database user permissions

  `Unknown database 'employeedb'`                     Create the database and verify the
                                                      database name in the JDBC URL

  `Field 'first_name' doesn't have a default value`   Map the entity property using
                                                      `@Column(name = "first_name")` and
                                                      provide `firstName` in the POST
                                                      JSON

  Duplicate email error                               Use an email address that is not
                                                      already present

  `Connection refused`                                Confirm the database is available
                                                      and that port `3306` is reachable
                                                      from the application host
  ---------------------------------------------------------------------------------------

## 13. Important Security Practices

-   Keep the RDS database private when it is accessed by an application
    running inside the same VPC.
-   Allow database traffic only from trusted source IPs or the
    application's security group.
-   Use TLS/SSL for database connections.
-   Store passwords in environment variables or AWS Secrets Manager.
-   Never commit passwords, access keys, private keys, or certificate
    files containing private material to source control.
-   Do not use broad inbound rules such as `0.0.0.0/0` for MySQL port
    `3306`.
-   An EC2 instance role is useful for accessing AWS services such as
    Secrets Manager. It is not automatically required for ordinary MySQL
    username/password authentication.
