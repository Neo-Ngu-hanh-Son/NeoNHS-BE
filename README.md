# NeoNHS-BE

RESTful API cho ứng dụng Du lịch Neo-NHS

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

---

## Mục lục

- [Giới thiệu](#giới-thiệu)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Cài đặt và chạy](#cài-đặt-và-chạy)
- [Các lệnh hữu ích](#các-lệnh-hữu-ích)
- [CI/CD](#cicd)

---

## Giới thiệu

NeoNHS-BE là backend API phục vụ ứng dụng du lịch, cung cấp các tính năng:

- Quản lý người dùng và xác thực
- Quản lý địa điểm du lịch và check-in
- Đặt vé và workshop
- Đánh giá và blog

---

## Công nghệ sử dụng

| Công nghệ | Phiên bản |
|-----------|-----------|
| Java | 21 |
| Spring Boot | 4.0 |
| Spring Data JPA | - |
| Spring Security | - |
| MySQL | 8.0 |
| Maven | 3.9+ |
| Docker | - |

---

## Cấu trúc dự án

```
src/main/java/fpt/project/NeoNHS/
├── controller/      # REST API endpoints
├── service/         # Business logic
├── repository/      # Data access (JPA)
├── entity/          # Database entities
├── enums/           # Enum types
├── dto/             # Request/Response objects
├── config/          # Configurations
└── exception/       # Exception handling
```

---

## Cài đặt và chạy

### Yêu cầu

- JDK 21+
- Maven 3.9+
- Docker & Docker Compose

### Bước 1: Clone repository

```bash
git clone <repository-url>
cd NeoNHS-BE
```

### Bước 2: Cấu hình database

Tạo file `.env` hoặc cập nhật `application.yaml` với thông tin database của bạn.

### Bước 3: Khởi động MySQL

```bash
docker-compose up -d
```

### Bước 4: Chạy ứng dụng

```bash
./mvnw spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

---

## Các lệnh hữu ích

| Lệnh | Mô tả |
|------|-------|
| `./mvnw clean compile` | Compile source code |
| `./mvnw test` | Chạy unit tests |
| `./mvnw package -DskipTests` | Build JAR file |
| `docker-compose up -d` | Khởi động MySQL container |
| `docker-compose down` | Dừng MySQL container |

---

## CI/CD

Dự án sử dụng **GitHub Actions** để tự động:

- Build và compile code
- Chạy unit tests
- Tạo artifact (JAR file)

Workflow được trigger khi push hoặc tạo PR vào branch `main`.

---

## License

FPT University Capstone Project © 2026
