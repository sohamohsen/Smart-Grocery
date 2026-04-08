# 🛒 Smart Grocery

A full-stack grocery catalog application. It includes separate admin and user dashboards, JWT-based authentication, Open Food Facts product suggestion lookup, product and category management, wishlist support, pagination, and bulk-add from queued suggestions.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 16 |
| Backend | Spring Boot 3 |
| Language | Java 17 |
| Database | MySQL 8+ |
| Security | Spring Security + JWT |
| External API | OpenFeign → Open Food Facts |

## Project Structure

```
├── Smart Grocery BE/                              # Spring Boot backend
└── smart-grocery-fe-master/smart-grocery-fe-master/  # Angular frontend
```

## Prerequisites

- Java 17+
- Node.js 18+ and npm
- MySQL 8+

---

## Backend Setup

1. Create or start a MySQL server on port `3306`.

2. Update credentials in `Smart Grocery BE/src/main/resources/application.properties` if your local MySQL username or password differ from `root / root`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/smart_grocery?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=root
   ```

3. From `Smart Grocery BE/`, run:

   ```bash
   .\mvnw.cmd spring-boot:run
   ```

   The backend runs on `http://localhost:8080/api`.

---

## Frontend Setup

1. Open a second terminal and navigate to the frontend directory:

   ```bash
   cd smart-grocery-fe-master/smart-grocery-fe-master
   ```

2. Install dependencies (first run only):

   ```bash
   npm install
   ```

3. Start the Angular dev server:

   ```bash
   npm start
   ```

   The frontend runs on `http://localhost:4200`.

---

## Default Seeded Accounts

The backend seeds these accounts automatically on startup if they do not already exist:

| Role | Username | Password |
|------|----------|----------|
| Super Admin | `superadmin` | `123456` |
| Admin | `admin` | `123456` |
| User | `user` | `123456` |

A few sample categories and one approved demo product are also seeded when the catalog is empty, so the user dashboard has data immediately after first run.

---

## How to Review the App

1. Sign in as `admin` / `123456`.
2. Open **Admin → Categories** to add categories beyond the seeded demo data.
3. Open **Admin → Suggestions** to fetch product suggestions from Open Food Facts by barcode.
4. Approve a single suggestion, or queue several and bulk-add them all at once.
5. Open **Admin → Products** to add, edit, search, paginate, and delete products.
6. Sign out and sign in as `user` / `123456`, or register a new user account.
7. Open **User → Products** to browse approved products, search, filter by category, paginate, and add items to the wishlist.
8. Open **User → Wishlist** to review saved items.

---

## Feature 

- [x] Admin dashboard
- [x] Admin login
- [x] User dashboard
- [x] User login
- [x] Admin fetches product suggestions from a public API (Open Food Facts)
- [x] Admin adds products to the internal database
- [x] Admin removes products from the internal database
- [x] Users view approved products only
- [x] Product details shown to users: name, category, price, brand, image
- [x] Product search for users
- [x] Users can mark items as "Want to Buy" via the wishlist
- [x] Admin bulk-add products from queued suggestions
- [x] Pagination on product lists

---

## Helpful Notes

- The public product suggestion source is [Open Food Facts](https://world.openfoodfacts.org/).
- The backend uses `spring.jpa.hibernate.ddl-auto=update` — schema changes are applied automatically on startup.
- If `npm run build` fails because the existing `dist/` folder is locked by another process, use an alternate output path:


  ```bash
  npm run build -- --output-path build-check
  ```

---