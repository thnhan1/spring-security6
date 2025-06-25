# Spring Security JWT, Refresh token login, register with Credentials, OAuth2

## Prerequisite 
- Spring Boot: `3.4.x`
- Java 21
- Google Auth Platform/Clients
## Models

![sql_schema](./REFRESH_TOKEN.png)

## Authentication Flow
### a. Credentials (username/password)
```mermaid
sequenceDiagram
    participant FE as Frontend (React)
    participant BE as Backend (Spring Boot)
    participant DB as Database

    FE->>BE: POST /api/auth/login (email, password)
    BE->>BE: Validate credentials
    BE->>BE: Generate access token and refresh token
    BE->>DB: Store refresh token linked to user
    DB-->>BE: OK
    BE-->>FE: Return access token and refresh token

    FE->>FE: Save access token to cookie [not HttpOnly]
    FE->>FE: Save refresh token to cookie [HttpOnly]

    Note over FE: Future requests:\nAuthorization: Bearer <access token>
    FE->>BE: GET /api/protected\nAuthorization: Bearer <token>
```
### b. OAuth2 (Google Login)

```mermaid
sequenceDiagram
    autonumber
    actor User as User
    participant Frontend as Frontend
    participant Backend as Backend (API)
    participant Google as Google Auth
    participant DB as Database

    User->>Frontend: Clicks "Login with Google"
    Frontend->>Backend: GET /api/auth/google/url
    Backend->>Frontend: Google Auth URL
    
    Frontend->>User: Redirect to Google
    User->>Google: Access Google Auth
    Google->>User: Shows consent screen
    User->>Google: Grants permission
    
    Google->>Backend: GET /api/auth/google/callback?code={code}
    
    Backend->>Google: POST /oauth2/v4/token (code + secret)
    Google->>Backend: access_token
    
    Backend->>Google: GET /oauth2/v3/userinfo
    Google->>Backend: {email, name, ...}
    
    Backend->>DB: Find user by email
    alt User exists
        DB->>Backend: User data
    else New user
        Backend->>DB: Create user
        DB->>Backend: New user
    end
    
    Backend->>Frontend: HTTP 302 with Set-Cookie
    Note right of Backend: Set-Cookie: jwt=...\nRedirect: /dashboard
    
    Frontend->>User: Load dashboard (with cookie)
```

### c. Refresh token flow

```mermaid
sequenceDiagram
participant FE as Frontend (React)
participant BE as Backend (Spring Boot)
participant DB as Database

    FE->>BE: Request protected resource (with access token)
    BE-->>FE: 401 Unauthorized (access token expired)

    FE->>BE: POST /api/auth/refresh (with refresh token cookie)
    BE->>DB: Validate refresh token
    DB-->>BE: Token is valid

    BE->>BE: Generate new access token
    BE-->>FE: Return new access token

    FE->>FE: Update access token in cookie or memory

```

## Enpoints

| API Endpoint           | Method | Description                 | Body / Parameters                                                                 |
|------------------------|--------|-----------------------------|-----------------------------------------------------------------------------------|
| `/api/auth/register`   | POST   | Register with username/password | JSON:<br>`{ "username": "abc", "email": "email@gmail.com", "password": "123456" }` |
| `/api/auth/login`      | POST   | Login with username/password | JSON:<br>`{ "username": "abc", "password": "123456" }`                             |
| `/api/auth/refresh`    | POST   | Get new access token         | Query Param:<br>`refreshToken=...`                                                |
| `/secured`             | GET    | Secured API (requires auth)  | Header:<br>`Authorization: Bearer <access_token>`                                 |
| `/api/auth/google`     | GET    | Redirect to Google Login     | None                                                                               |

---


