# GUTS Proxy

## Overview

GUTS Proxy is a lightweight reverse proxy that acts as a secure gateway between clients and backend services. It integrates with an external IAM service to handle authentication and authorization, ensuring centralized access control across distributed services.

The proxy is designed to demonstrate core API gateway concepts used in microservice-based architectures.

---

## Architecture

### Request Flow

1. Client sends request to proxy
2. Proxy extracts authentication token from request headers
3. Token is validated via IAM service
4. Based on validation result, request is either:
   - Forwarded to backend service
   - Rejected with appropriate response
5. Backend response is returned to client

---

## Responsibilities

### Proxy Layer
- Request interception
- Routing to backend services
- Header/token extraction
- Response forwarding

### IAM Integration
- Authentication validation
- Authorization checks
- Identity verification via token validation endpoint

---

## Tech Stack

- Java
- Spring Boot
- REST APIs
- WebClient / RestTemplate
- External IAM Service

---

## Design Principles

- Centralized authentication through IAM
- Stateless request processing
- Separation of authentication and business logic
- Service decoupling for scalability

---

## Project Structure

---

## External Dependency

- Guts-IAM Service (authentication and authorization provider)

---

## Future Improvements

- Role-based routing rules
- Rate limiting per client
- Circuit breaker for IAM failures
- Centralized logging and tracing
- Request audit trail

---

## License

This project is licensed under the Apache License 2.0.
