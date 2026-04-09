# ReJunk — Frontend

A Spring Boot MVC web application that serves as the user-facing layer for the ReJunk platform. ReJunk is a circular economy service that collects second-hand furniture from customers, evaluates it, lists it on a managed marketplace, and pays customers 50% of each sale.

---

## Overview

The frontend is a **thin client** — it handles routing, session management, and rendering. All business logic lives in the backend REST API. The frontend communicates with the backend exclusively through `BackendClient`, which attaches JWT tokens automatically on every request.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0 (MVC) |
| Templating | Thymeleaf |
| UI | Bootstrap 5.3, Bootstrap Icons |
| Fonts | DM Sans (Google Fonts) |
| Security | Spring Security (session-based) |
| HTTP Client | Spring `RestClient` |
| Build | Maven |
| Java | Java 17 |

---

## Project Structure

```
src/main/
├── java/com/example/rejunkfrontend/
│   ├── ReJunkFrontendApplication.java
│   ├── SecurityConfig.java             # Spring Security — permits all routes, disables form login
│   ├── client/
│   │   └── BackendClient.java          # All HTTP calls to the backend API
│   ├── controller/
│   │   └── PageController.java         # All route handlers (MVC controller)
│   ├── dto/                            # Request/response data transfer objects
│   └── security/
│       └── TokenContextFilter.java     # Detects and clears stale sessions
│
└── resources/
    ├── application.properties
    ├── static/
    │   ├── css/rejunk.css              # Custom design system
    │   └── images/
    └── templates/
        ├── fragments/
        │   ├── navbar.html             # Public, customer, and admin navbar fragments
        │   ├── footer.html
        │   └── helpers.html            # Reusable UI components (e.g. status badges)
        ├── admin/                      # Admin pages
        ├── customer/                   # Customer pages
        ├── marketplace/                # Marketplace pages
        ├── landing.html
        ├── login.html
        └── register.html
```

---

## Configuration

`src/main/resources/application.properties`

```properties
server.port=8081
backend.base-url=http://localhost:8080
```

The backend must be running on port `8080` before starting the frontend.

---

## Running the App

```bash
./mvnw spring-boot:run
```

Then open: `http://localhost:8081`

---

## User Roles

| Role | Landing Page | Access |
|---|---|---|
| Guest | `/` | Landing page, login, register |
| Customer | `/dashboard` | Dashboard, marketplace, collections, my items, notifications, profile |
| Admin | `/admin` | Dashboard, add items, listings, orders, customers |

Role is determined from the backend's login response. Admins are automatically redirected to `/admin` on login. The correct navbar is rendered based on the session role throughout the entire session.

---

## Key Pages

### Customer
| Route | Description |
|---|---|
| `/dashboard` | Overview of recent collection requests and notifications |
| `/collections` | All pickup requests with status tracking |
| `/collections/new` | Submit a new pickup request |
| `/collections/{id}` | Timeline view of a single request |
| `/my-items` | Items belonging to the customer that are listed |
| `/marketplace` | Browse all active listings |
| `/marketplace/{id}` | Item detail and purchase |
| `/notifications` | In-app notifications |
| `/profile` | Profile and avatar management |

### Admin
| Route | Description |
|---|---|
| `/admin` | Dashboard with stats and user management |
| `/admin/add-items` | Evaluate collection requests and add/price items |
| `/admin/listings` | View and remove marketplace listings |
| `/admin/orders` | View and update order statuses |
| `/admin/customers` | View, suspend, activate, or delete customers |

---

## Authentication Flow

1. User submits credentials via `POST /login`
2. Frontend calls the backend `POST /auth/login` and receives a JWT token
3. Token and user details are stored in the **server-side HTTP session**
4. `BackendClient` reads the token from the session on every request and attaches it as a `Bearer` token header
5. `TokenContextFilter` runs on every incoming request to invalidate stale or corrupted sessions
6. Logout invalidates the session via `GET /logout`

---

## Collection Request Status Pipeline

```
SUBMITTED → PAID → SCHEDULED → COLLECTED → EVALUATED → CLOSED
```

Admins move requests through this pipeline via the Add Items page. Customers track progress on the collection detail page.

---

## Listing Status

| Status | Meaning |
|---|---|
| `ACTIVE` | Visible on the marketplace |
| `INACTIVE` | Removed by admin — hidden from buyers |
| `SOLD` | Purchased by a customer |

---

## Design System

All styles live in `rejunk.css` using CSS custom properties:

```css
--rj-green: #2B6E3F      /* Primary brand colour */
--rj-orange: #E68A2E     /* Accent / step numbers */
--rj-cream: #F5F3EE      /* Page background */
--rj-navy: #1A3245       /* Dark text alternative */
```

Reusable components include `.rj-card`, `.rj-stat-card`, `.rj-table`, `.rj-badge-*`, and `.rj-request-card`.