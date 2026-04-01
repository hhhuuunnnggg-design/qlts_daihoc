# TUYEN SINH DAI HOC 2026 — Architecture & Code Flow

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Project Structure](#2-project-structure)
3. [Database Schema](#3-database-schema)
4. [ORM Architecture (Hibernate)](#4-orm-architecture-hibernate)
5. [Data Access Layer (DAO)](#5-data-access-layer-dao)
6. [Service Layer](#6-service-layer)
7. [Web Application Flow (web-app)](#7-web-application-flow-web-app)
8. [Admin Desktop Application Flow (admin-app)](#8-admin-desktop-application-flow-admin-app)
9. [Main Business Flows](#9-main-business-flows)
10. [Technology Stack](#10-technology-stack)

---

## 1. Project Overview

**TuyenSinhDaiHoc_2026** is a university admissions management system for Vietnam's 2026 recruitment cycle. It is a Maven multi-module project with three distinct deployment units:

| Module | Type | Technology | Users |
|--------|------|------------|-------|
| `core` | Library/JAR | Hibernate + MySQL | Shared by all modules |
| `web-app` | Web Application | JSP/Servlet + Jetty | Candidates (Thí sinh) |
| `admin-app` | Desktop Application | Swing | Administrators |

The system manages the complete admission lifecycle: candidate registration, score input, preference submission, and automated selection processing.

---

## 2. Project Structure

```
TuyenSinhDaiHoc_2026/
│
├── pom.xml                          # Parent POM — defines all module versions
│
├── core/                            # ── CORE MODULE ──
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/tuyensinh/
│       │   ├── entity/             # 18 JPA Entities (ORM models)
│       │   ├── dao/                 # 13 DAO classes (data access)
│       │   ├── service/             # 14 Service classes (business logic)
│       │   └── util/               # Utility classes
│       └── resources/
│           ├── hibernate.cfg.xml    # Hibernate configuration
│           └── application.properties
│
├── web-app/                        # ── WEB MODULE ──
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/tuyensinh/web/
│       │   ├── servlet/            # 9 Servlet classes
│       │   └── JettyLauncher.java  # Embedded Jetty launcher
│       └── webapp/
│           ├── index.jsp           # Entry point → redirect to /login
│           └── WEB-INF/
│               ├── web.xml          # Servlet mappings
│               └── views/           # 10 JSP templates
│
└── admin-app/                      # ── ADMIN MODULE ──
    ├── pom.xml
    └── src/main/java/com/tuyensinh/admin/
        ├── MainApp.java            # Entry point
        └── ui/
            ├── LoginFrame.java     # Admin login window
            ├── MainFrame.java     # Main frame (CardLayout)
            └── panels/             # 12 management panels
```

---

## 3. Database Schema

**Database**: `xettuyen2026` (MySQL 8.0)
**Hibernate Mode**: `validate` (schema must exist beforehand)

### 3.1 Entity-Relationship Diagram (Conceptual)

```
VaiTro (1)──────< NguoiDung (1:Many) >───────(1:1)──────< ThiSinh (1) >───────(1:Many)──< NguyenVong (1)
   │                   │                                   │                                    │
   │                   │                                   │                                    │
   │               18 tables                           (1:Many)──< DiemThi (1)                 │
   │               total                            │         │                                 │
   │                                      (1:Many)──< DiemCong (1) >───────(N:1)──< NganhToHop (1)
   │                                      │                                            │
   │                                      └──(1:Many)──< DiemThiChiTiet (1) >───(N:1)──< Mon (1)
   │                                                                              │
   │                                                               ────────────────────────────────
   │                                                                    │
   │            (N:1)──< NganhPhuongThuc (1) >───────(N:1)──< PhuongThuc (1)
   │                 │
   │                 └──(N:1)──< Nganh (1) >───────(1:Many)──< NganhToHop (1) >───(N:1)──< ToHop (1)
   │                                   │                       │                        │
   │                                   │                       └──(1:Many)──< NganhToHopMon (1) >───(N:1)── Mon
   │                                   │
   │                                   └──(1:Many)──< NguyenVong (1)
   │
   └──(1:Many)──< DoiTuongUutien (1)
   │
   └──(1:Many)──< KhuVucUutien (1)
   │
   └──(1:Many)──< BangQuyDoi (1)
```

### 3.2 Table Reference

| Table | Description | Key Columns |
|-------|-------------|-------------|
| `xt_vaitro` | User roles | `vaitro_id`, `ma_vaitro` (ADMIN/USER), `ten_vaitro` |
| `xt_nguoidung` | User accounts | `nguoidung_id`, `username`, `password_hash`, `vaitro_id`, `email`, `is_active` |
| `xt_thisinh` | Candidate profile | `thisinh_id`, `nguoidung_id` (1:1), `cccd`, `sobaodanh`, `ho`, `ten`, `ngay_sinh`, `gioi_tinh`, `dien_thoai`, `email`, `doituong_id`, `khuvuc_id` |
| `xt_phuongthuc` | Admission methods | `phuongthuc_id`, `ma_phuongthuc` (XTT/VHAT/DGNL/THPT/NK), `ten_phuongthuc`, `thang_diem` |
| `xt_mon` | Subjects | `mon_id`, `ma_mon`, `ten_mon`, `loai_mon` (MON_HOC/DANH_GIA_NANG_LUC/NANG_KHIEU) |
| `xt_tohop` | Subject combinations | `tohop_id`, `ma_tohop`, `ten_tohop` |
| `xt_tohop_mon` | Combination ↔ Subject mapping | `tohop_id`, `mon_id`, `thu_tu` |
| `xt_nganh` | Majors | `nganh_id`, `ma_nganh`, `ten_nganh`, `chi_tieu`, `diem_san`, `diem_trung_tuyen`, `is_active` |
| `xt_nganh_phuongthuc` | Major ↔ Method mapping | `nganh_id`, `phuongthuc_id`, `chi_tieu`, `so_luong_hien_tai` |
| `xt_nganh_tohop` | Major ↔ Combination mapping | `nganh_tohop_id`, `nganh_id`, `tohop_id`, `do_lech` (adjustment score) |
| `xt_nganh_tohop_mon` | Major ↔ Combination ↔ Subject | `nganh_tohop_id`, `mon_id`, `he_so` (coefficient), `is_mon_chinh` (main subject) |
| `xt_diemthi` | Exam score records | `diemthi_id`, `thisinh_id`, `phuongthuc_id`, `sobaodanh`, `nam_tuyensinh` |
| `xt_diemthi_chitiet` | Per-subject scores | `diemthi_ct_id`, `diemthi_id`, `mon_id`, `diem_goc`, `diem_quydoi`, `diem_sudung` |
| `xt_nguyenvong` | Admission preferences | `nguyenvong_id`, `thisinh_id`, `nganh_id`, `nganh_tohop_id`, `phuongthuc_id`, `thu_tu`, `diem_xettuyen`, `ket_qua` |
| `xt_diemcong` | Bonus scores | `diemcong_id`, `thisinh_id`, `nganh_tohop_id`, `phuongthuc_id`, `diem_chungchi`, `diem_uutien_xt`, `diem_tong` |
| `xt_doituong_uutien` | Priority target groups | `doituong_id`, `ma_doituong`, `ten_doituong`, `muc_diem` |
| `xt_khuvuc_uutien` | Priority zones | `khuvuc_id`, `ma_khuvuc`, `ten_khuvuc`, `muc_diem` |
| `xt_bangquydoi` | Score conversion table | `bangquydoi_id`, `phuongthuc_id`, `tohop_id`, `mon_id`, `diem_tu`, `diem_den`, `diem_quydoi_tu`, `diem_quydoi_den`, `phan_vi`, `ma_quydoi` |

---

## 4. ORM Architecture (Hibernate)

### 4.1 Hibernate Configuration (`hibernate.cfg.xml`)

```
Driver      : com.mysql.cj.jdbc.Driver
URL         : jdbc:mysql://localhost:3306/xettuyen2026
Credentials : root / root
Dialect     : org.hibernate.dialect.MySQL8Dialect
Mode        : validate  (does NOT auto-create schema)
Batch Size  : 25
```

### 4.2 Entity Mapping Strategy

All 18 entities use JPA annotations (`javax.persistence`). Key patterns:

```java
@Entity
@Table(name = "xt_thisinh")
@Data  // Lombok — generates getters/setters/constructors
@NoArgsConstructor
@AllArgsConstructor
public class ThiSinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thisinh_id")
    private Integer thisinhId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoidung_id", unique = true)
    private NguoiDung nguoiDung;     // 1:1 relationship with NguoiDung

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doituong_id")
    private DoiTuongUutien doiTuongUutien;  // N:1 reference

    @OneToMany(mappedBy = "thiSinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiemThi> danhSachDiemThi;  // 1:Many collection
}
```

### 4.3 Key Design Decisions

- **Lazy Loading**: All `@ManyToOne` and `@OneToMany` use `FetchType.LAZY` to avoid N+1 queries
- **Cascade ALL**: Collections use `CascadeType.ALL` so deleting a `ThiSinh` cascades to `DiemThi`, `NguyenVong`, `DiemCong`
- **No JPA Repository**: The project uses custom DAO classes extending `BaseDao<T>`, not Spring Data JPA
- **Session per operation**: Each DAO method opens a new session, performs one operation, and closes

### 4.4 Session Management (`HibernateUtil.java`)

```java
public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        Configuration cfg = new Configuration();
        cfg.configure("hibernate.cfg.xml");
        sessionFactory = cfg.buildSessionFactory();
    }

    public static Session getSession() {
        return sessionFactory.openSession();  // Opens NEW session each call
    }
}
```

> **Important**: Each DAO method calls `getSession()` in a try-with-resources block, so every operation gets its own dedicated session. This is the **Repository pattern** implemented manually.

---

## 5. Data Access Layer (DAO)

### 5.1 BaseDao Pattern

```java
public abstract class BaseDao<T> {

    protected abstract Class<T> getEntityClass();   // Subclasses return their Entity class

    protected Session getSession() {
        return HibernateUtil.getSession();
    }

    // ── Basic CRUD ──────────────────────────────────────────
    public T findById(Integer id) {
        try (Session session = getSession()) {
            return session.get(getEntityClass(), id);
        }
    }

    public List<T> findAll() {
        try (Session session = getSession()) {
            return session.createQuery(
                "FROM " + getEntityClass().getSimpleName(), getEntityClass()
            ).getResultList();
        }
    }

    public T save(T entity) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.persist(entity);
            session.getTransaction().commit();
            return entity;
        }
    }

    public void update(T entity) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.update(entity);
            session.getTransaction().commit();
        }
    }

    public void delete(T entity) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.delete(entity);
            session.getTransaction().commit();
        }
    }

    // ── Pagination ──────────────────────────────────────────
    public List<T> findByPage(int page, int pageSize) {
        try (Session session = getSession()) {
            return session.createQuery("FROM " + getEntityClass().getSimpleName(), getEntityClass())
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        }
    }

    public long count() { /* ... */ }

    // ── Template methods for HQL queries ──────────────────
    protected List<T> executeQuery(String hql, List<Object> params) { /* ... */ }
    protected T executeSingleResult(String hql, List<Object> params) { /* ... */ }
}
```

### 5.2 NguyenVongDao — Example of Custom Queries

```java
public class NguyenVongDao extends BaseDao<NguyenVong> {

    // Find all preferences for one candidate, ordered by priority
    public List<NguyenVong> findByThiSinhId(Integer thisinhId) {
        return session.createQuery(
            "FROM NguyenVong nv WHERE nv.thiSinh.thisinhId = :tsid ORDER BY nv.thuTu"
        ).setParameter("tsid", thisinhId).getResultList();
    }

    // Find all preferences for one major, ordered by admission score DESC
    public List<NguyenVong> findByNganhId(Integer nganhId) {
        return session.createQuery(
            "FROM NguyenVong nv WHERE nv.nganh.nganhId = :nid ORDER BY nv.diemsxettuyen DESC"
        ).setParameter("nid", nganhId).getResultList();
    }

    // Find preferences for one major + one method, ordered by score
    public List<NguyenVong> findByNganhIdAndPhuongThuc(Integer nganhId, Short phuongthucId) {
        return session.createQuery(
            "FROM NguyenVong nv WHERE nv.nganh.nganhId = :nid " +
            "AND nv.phuongThuc.phuongthucId = :ptid ORDER BY nv.diemsxettuyen DESC"
        ).setParameter("nid", nganhId).setParameter("ptid", phuongthucId).getResultList();
    }

    // Check if a preference already exists (to prevent duplicates)
    public Optional<NguyenVong> findByThiSinhNganhToHopPhuongThuc(
            Integer thisinhId, Integer nganhId, Integer nganhToHopId, Short phuongthucId) { /* ... */ }

    // Count how many candidates passed for a major+method
    public int countByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId, String ketQua) { /* ... */ }
}
```

---

## 6. Service Layer

Services wrap DAO operations with business logic. They are simple **delegation + transaction** wrappers.

### 6.1 Service Diagram

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  AuthService    │     │ ThiSinhService  │     │ NguyenVongSvc   │
│                 │     │                 │     │                 │
│ login()         │     │ findByCccd()    │     │ findAll()       │
│ register()     │     │ save()          │     │ findByThiSinh() │
│ changePassword()│     │ update()       │     │ save()          │
│ updatePassword()│     │ generateSBD()  │     │ update()        │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                      │                      │
         ▼                      ▼                      ▼
┌──────────────────────────────────────────────────────────────────┐
│                        XetTuyenService (Aggregate)               │
│  findActiveNganh()  findActivePhuongThuc()  findAllNguyenVong()   │
│  findNguyenVongByNganhPhuongThuc()   countTrungTuyen()         │
│  findDiemThiByThiSinh()  saveDiemThi()  updateDiemThi()        │
└──────────────────────────────────────────────────────────────────┘
```

### 6.2 AuthService — Authentication Logic

```java
public class AuthService {

    private final NguoiDungDao nguoiDungDao = new NguoiDungDao();

    public Optional<NguoiDung> login(String username, String password) {
        Optional<NguoiDung> optNd = nguoiDungDao.findByUsername(username);
        if (optNd.isPresent()) {
            NguoiDung nd = optNd.get();
            // Check: account active AND password matches (SHA-256 with salt)
            if (nd.getIsActive() && PasswordUtil.checkPassword(password, nd.getPasswordHash())) {
                return Optional.of(nd);
            }
        }
        return Optional.empty();
    }

    public NguoiDung register(NguoiDung nguoiDung, String rawPassword) {
        // Hash password before saving
        String hash = PasswordUtil.hashPassword(rawPassword);
        nguoiDung.setPasswordHash(hash);
        return nguoiDungDao.save(nguoiDung);
    }

    public void changePassword(NguoiDung nd, String oldPassword, String newPassword) {
        if (!PasswordUtil.checkPassword(oldPassword, nd.getPasswordHash())) {
            throw new IllegalArgumentException("Mat khau cu khong dung");
        }
        updatePassword(nd, newPassword);
    }
}
```

### 6.3 PasswordUtil — Security

```java
public class PasswordUtil {
    public static String hashPassword(String rawPassword) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        md.update(salt);
        byte[] hashed = md.digest(rawPassword.getBytes("UTF-8"));
        // Stored as: $2a$10$<salt_base64>.<$hash_base64>
        return "$2a$10$" + saltStr + "." + hashStr;
    }

    public static boolean checkPassword(String raw, String storedHash) {
        // Extract salt, re-hash, compare
        // Falls back to plain SHA-256 comparison
    }
}
```

---

## 7. Web Application Flow (web-app)

### 7.1 Request Lifecycle

```
User Browser
    │
    │ HTTP Request (e.g. GET /dashboard)
    ▼
Jetty Web Server (port 8080)
    │
    ▼
web.xml Servlet Mapping
    │
    ├─ /login          → LoginServlet
    ├─ /register       → RegisterServlet
    ├─ /logout         → LogoutServlet
    ├─ /dashboard      → DashboardServlet
    ├─ /profile        → ProfileServlet
    ├─ /scores         → ScoresServlet
    ├─ /nguyenvong     → NguyenVongServlet
    └─ /add-nguyenvong  → AddNguyenVongServlet
    │
    ▼
BaseServlet (abstract parent)
    │
    ├─ doGet() / doPost() → delegates to handleGet() / handlePost()
    ├─ requireLogin()     → checks session["nguoidung"]
    ├─ getLoggedInUser() → returns NguoiDung from session
    ├─ forward()         → JSP forward
    └─ redirect()        → HTTP 302 redirect
    │
    ▼
Specific Servlet.handleGet/Post()
    │
    ├─ Validate input
    ├─ Call Service layer
    ├─ Call DAO layer → Hibernate → MySQL
    │
    ▼
Set request/session attributes
    │
    ▼
forward() to /WEB-INF/views/xxx.jsp
    │
    ▼
JSP renders HTML
    │
    ▼
HTTP Response → Browser
```

### 7.2 Session Management

- **Login** stores `NguoiDung` object in `session.setAttribute("nguoidung", nguoiDung)`
- **Session timeout**: 30 minutes (`session-config` in `web.xml`)
- **Authentication check**: Every Servlet calls `requireLogin()` first
- **Message flash**: `setMessage(request, "text", "success")` stores message in session, displayed in JSP, then removed

### 7.3 Servlet Routing Map

```
┌──────────────────────┬────────────────────────────────────────────────┐
│ URL Pattern          │ Servlet & Handler                               │
├──────────────────────┼────────────────────────────────────────────────┤
│ /login    (GET)      │ LoginServlet.handleGet()                       │
│                      │ → If logged in → redirect /dashboard           │
│                      │ → Else → forward to login.jsp                  │
│                      │                                                │
│ /login    (POST)     │ LoginServlet.handlePost()                       │
│                      │ → AuthService.login(username, password)        │
│                      │ → Success → set session → redirect /dashboard    │
│                      │ → Fail → forward back to login.jsp with msg    │
│                      │                                                │
│ /register  (GET)      │ RegisterServlet.handleGet()                     │
│                      │ → Load DoiTuong + KhuVuc lists → forward      │
│                      │                                                │
│ /register  (POST)    │ RegisterServlet.handlePost()                   │
│                      │ → Validate all fields                           │
│                      │ → Check CCCD uniqueness                        │
│                      │ → AuthService.register() → save NguoiDung       │
│                      │ → ThiSinhService.save() → save ThiSinh       │
│                      │ → Set session → redirect /dashboard            │
│                      │                                                │
│ /dashboard (GET)     │ DashboardServlet.handleGet()                   │
│                      │ → getLoggedInUser() → find ThiSinh by userId  │
│                      │ → Load NguyenVong list                          │
│                      │ → Load DiemThi list                           │
│                      │ → Count TRUNG_TUYEN results                   │
│                      │ → Set attributes → forward dashboard.jsp       │
│                      │                                                │
│ /profile   (GET)     │ ProfileServlet.handleGet()                     │
│                      │ → Load ThiSinh + DoiTuong list + KhuVuc list │
│                      │ → forward profile.jsp                          │
│                      │                                                │
│ /profile   (POST)    │ ProfileServlet.handlePost()                    │
│                      │ → Update ThiSinh fields                        │
│                      │ → ThiSinhService.update()                      │
│                      │ → redirect /dashboard                          │
│                      │                                                │
│ /scores    (GET)     │ ScoresServlet.handleGet()                      │
│                      │ → Load all DiemThi for this ThiSinh           │
│                      │ → Load all PhuongThuc and Mon                  │
│                      │ → forward scores.jsp                           │
│                      │                                                │
│ /scores    (POST)    │ ScoresServlet.handlePost()                     │
│                      │ → Create DiemThi → save                       │
│                      │ → For each mon with a score:                  │
│                      │     → Create DiemThiChiTiet (diemGoc=diemQuydoi=diemSudung) │
│                      │     → add to DiemThi.danhSachDiemChiTiet      │
│                      │ → DiemThiService.update() to cascade save     │
│                      │                                                │
│ /nguyenvong (GET)    │ NguyenVongServlet.handleGet()                  │
│                      │ → Load all NguyenVong for this ThiSinh        │
│                      │ → forward nguyenvong.jsp                       │
│                      │                                                │
│ /nguyenvong (POST)  │ NguyenVongServlet.handlePost()                │
│                      │ → action=delete → find and delete NguyenVong   │
│                      │ → else → handleGet()                           │
│                      │                                                │
│ /add-nguyenvong(GET) │ AddNguyenVongServlet.handleGet()               │
│                      │ → Load Nganh + PhuongThuc + NganhToHop       │
│                      │ → Count existing NguyenVong (must < 5)        │
│                      │ → forward add-nguyenvong-form.jsp              │
│                      │                                                │
│ /add-nguyenvong(POST)│ AddNguyenVongServlet.handlePost()            │
│                      │ → Validate limit (max 5)                       │
│                      │ → Validate no duplicate (same nganh+tohop+phuongthuc) │
│                      │ → Set thuTu = existingCount + 1                │
│                      │ → Set ketQua = CHO_XET                         │
│                      │ → NguyenVongService.save()                     │
│                      │ → redirect /nguyenvong                        │
│                      │                                                │
│ /logout   (GET)      │ LogoutServlet.handleGet()                     │
│                      │ → session.invalidate()                          │
│                      │ → redirect /login                             │
└──────────────────────┴────────────────────────────────────────────────┘
```

### 7.4 JSP Template Structure

All pages extend a consistent layout:

```
header.jsp  ── Bootstrap 5 navbar with navigation links
            ── Shows current username, logout button
            ── Active state on current page link

<jsp:include page="header.jsp"/>

<!-- Page-specific content -->

<jsp:include page="footer.jsp"/>
footer.jsp  ── Copyright, tech stack info
```

### 7.5 Key JSP Flows

#### Register Flow (register.jsp → RegisterServlet)

```jsp
<!-- register.jsp renders form with sections: -->
<!-- 1. Account: username, password, confirmPassword -->
<!-- 2. Personal: ho, ten, cccd, ngaySinh, gioiTinh, noiSinh -->
<!-- 3. Contact: dienThoai, email -->
<!-- 4. Priority: doituongId (select), khuvucId (select) -->

<form action="/register" method="post">
    <!-- Submits to RegisterServlet.handlePost() -->
</form>
```

#### Scores Flow (scores.jsp ↔ ScoresServlet)

```jsp
<!-- scores.jsp shows all DiemThi in a table -->
<!-- "Nhap diem moi" button opens Bootstrap modal -->

<!-- Modal form submits to /scores (POST) -->
<!-- Each subject field: name="diem_<monId>" → e.g. diem_1, diem_2 -->

<!-- On POST, ScoresServlet: -->
DiemThi diemThi = new DiemThi();
diemThi.setThiSinh(thiSinh);
diemThi.setPhuongThuc(phuongThuc);
DiemThi saved = xetTuyenService.saveDiemThi(diemThi);  // persist first

// Then add child DiemThiChiTiet one by one
for (Mon mon : allMon) {
    String score = request.getParameter("diem_" + mon.getMonId());
    if (score != null) {
        DiemThiChiTiet ct = new DiemThiChiTiet();
        ct.setDiemThi(saved);
        ct.setMon(mon);
        ct.setDiemGoc(new BigDecimal(score));
        ct.setDiemQuydoi(new BigDecimal(score));
        ct.setDiemSudung(new BigDecimal(score));
        saved.getDanhSachDiemChiTiet().add(ct);
    }
}
xetTuyenService.updateDiemThi(saved);  // cascade persist children
```

#### Add NguyenVong Flow (add-nguyenvong-form.jsp)

```javascript
// Client-side cascading selects:
// 1. User selects Nganh → JavaScript populates NganhToHop dropdown
// 2. Data embedded in JSP via JSTL loops:
//    nganhToHopData[nganhId] = [{id, ma, ten}, ...]

// On form submit (POST to /add-nguyenvong):
AddNguyenVongServlet.handlePost():
    1. Check existingNguyenVong.size() < 5
    2. Validate nganhId, nganhTohopId, phuongthucId
    3. Check no duplicate: findByThiSinhNganhToHopPhuongThuc()
    4. Set thuTu = existingCount + 1
    5. Set ketQua = CHO_XET (default, updated later by admin)
    6. saveNguyenVong()
```

---

## 8. Admin Desktop Application Flow (admin-app)

### 8.1 Application Startup Flow

```
MainApp.main()
    │
    ├─ setLookAndFeel()  [System L&F]
    │
    └─ SwingUtilities.invokeLater()
            │
            ▼
        LoginFrame.setVisible(true)
            │
            ▼
        User enters admin/admin123
            │
            ▼
        LoginFrame.doLogin()
            ├─ SwingWorker.doInBackground():
            │      AuthService.login("admin", "admin123")
            │              ↓
            │          NguoiDungDao.findByUsername("admin")
            │              ↓
            │          PasswordUtil.checkPassword()
            │              ↓
            │          return Optional<NguoiDung>
            │
            └─ SwingWorker.done():
                   if (user != null AND user.isAdmin()):
                       MainApp.currentUser = user
                       dispose LoginFrame
                       MainApp.openMainFrame()
                              ↓
                          MainFrame.setVisible(true)
```

### 8.2 MainFrame Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Menu Bar                                                       │
│  [He thong] [Nguoi dung] [Thi sinh] [Nganh & To hop] [Diem]... │
├─────────────────────────────────────────────────────────────────┤
│  Toolbar                                                        │
│  [Home] [Nguoi dung] [Thi sinh] [Nganh] [Diem] [Nguyen Vong]..│
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│                    CardLayout contentPanel                       │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  showPanel("home")      → HomePanel                  │    │
│  │  showPanel("nguoidung")  → NguoiDungPanel             │    │
│  │  showPanel("thisinh")    → ThiSinhPanel              │    │
│  │  showPanel("nganh")      → NganhPanel                │    │
│  │  showPanel("diemthi")    → DiemThiPanel              │    │
│  │  showPanel("nguyenvong")  → NguyenVongPanel           │    │
│  │  showPanel("xettuyen")    → XetTuyenPanel             │    │
│  │  ...                                                    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│  Status Bar: [Ready]                    [User: admin (Admin)]   │
└─────────────────────────────────────────────────────────────────┘
```

### 8.3 Panel Patterns

All management panels follow the same CRUD pattern:

```
┌────────────────────────────────────────────────┐
│ Toolbar: [Search ____] [Search] [Add][Edit][Del]│
├────────────────────────────────────────────────┤
│ JTable                                         │
│ ┌──┬────────┬─────────┬────────┬──────────┐│
│ │ID│ Col1   │  Col2    │  Col3   │   ...     ││
│ ├──┼────────┼─────────┼────────┼──────────┤│
│ │ 1│        │          │         │           ││
│ │ 2│        │          │         │           ││
│ └──┴────────┴─────────┴────────┴──────────┘│
├────────────────────────────────────────────────┤
│ Total: 120 records          Page: [<<][>>] [1] │
└────────────────────────────────────────────────┘
```

**CRUD Operations:**

```java
// 1. LOAD DATA (called on init and after any operation)
private void loadData() {
    model.setRowCount(0);
    List<Entity> list = service.findByPage(currentPage, pageSize);
    for (Entity e : list) {
        model.addRow(new Object[]{ e.getId(), e.getField1(), ... });
    }
}

// 2. ADD
private void showAddDialog() {
    // JOptionPane.showInputDialog() or showConfirmDialog() with fields
    Entity e = new Entity();
    e.setField1(value1);
    service.save(e);
    loadData();
}

// 3. EDIT
private void showEditDialog() {
    Entity e = getSelectedFromTable();  // get row → ID → service.findById()
    // JOptionPane with pre-filled fields
    e.setField1(newValue);
    service.update(e);
    loadData();
}

// 4. DELETE
private void deleteEntity() {
    Entity e = getSelectedFromTable();
    int confirm = JOptionPane.showConfirmDialog("Delete?");
    if (confirm == YES) {
        service.delete(e);
        loadData();
    }
}
```

### 8.4 XetTuyenPanel — Admission Processing Flow

This is the most critical business logic panel:

```
┌────────────────────────────────────────────────────────────────┐
│ Phuong thuc: [XTT ▼]   Nganh: [CNTT ▼]   [Bat dau xet tuyen]  │
├────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ QUY TAC XET TUYEN:                                       │  │
│  │ 1. So sanh diem xet tuyen voi diem san cua nganh        │  │
│  │ 2. Diem xet tuyen = diem_thxt + diem_cong              │  │
│  │ 3. Sap xep theo diem giam dan                            │  │
│  │ 4. Lay dau danh theo chi tieu nganh                     │  │
│  │ 5. Cap nhat: TRUNG_TUYEN / CHO_XET / TRUOT              │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                │
│  Ket qua xu ly:                                               │
│  === XET TUYEN ===                                            │
│  Phuong thuc: Xet tuyen thang diem 30.00 (XTT)               │
│  Nganh: Cong nghe thong tin (CN01)                           │
│  Chi tieu: 100                                                │
│  Diem san: 15.00                                             │
│  -----------------------                                      │
│  TS: Nguyen Van A | Diem: 25.50 | KQ: TRUNG_TUYEN           │
│  TS: Tran Thi B | Diem: 22.30 | KQ: CHO_XET                 │
│  ...                                                          │
│  -----------------------                                      │
│  Tong so trung tuyen: 45 / 100                               │
│  Xet tuyen hoan tat!                                         │
└────────────────────────────────────────────────────────────────┘
```

```java
private void xetTuyen() {
    SwingWorker<Void, String> worker = new SwingWorker<>() {
        protected Void doInBackground() {
            publish("=== XET TUYEN ===\n");

            List<NguyenVong> nvList = nguyenVongService.findAll();
            int countTrungTuyen = 0;

            for (NguyenVong nv : nvList) {
                // Filter: match current nganh AND phuongthuc
                if (nv.getNganh().equals(selectedNganh)
                    && nv.getPhuongThuc().equals(selectedPhuongThuc)) {

                    boolean trungTuyen = false;

                    // Rule: diem_xettuyen >= diem_san AND quota not exceeded
                    if (nv.getDiemsxettuyen() != null && nganh.getDiemSan() != null) {
                        if (nv.getDiemsxettuyen().compareTo(nganh.getDiemSan()) >= 0
                            && countTrungTuyen < nganh.getChiTieu()) {
                            trungTuyen = true;
                            countTrungTuyen++;
                        }
                    }

                    String ketQua = trungTuyen ? "TRUNG_TUYEN" : "CHO_XET";
                    publish("TS: " + nv.getThiSinh().getHoVaTen()
                        + " | Diem: " + nv.getDiemsxettuyen()
                        + " | KQ: " + ketQua + "\n");

                    nv.setKetQua(ketQua);
                    nguyenVongService.update(nv);
                }
            }
            publish("Tong so trung tuyen: " + countTrungTuyen + " / " + nganh.getChiTieu() + "\n");
            return null;
        }

        protected void process(List<String> chunks) {
            // Append each chunk to JTextArea in real-time
        }

        protected void done() {
            JOptionPane.showMessageDialog("Xet tuyen hoan tat!");
        }
    };
    worker.execute();
}
```

### 8.5 Import Panels

#### ThiSinhImportPanel

- Template format: `sobaodanh,cccd,ho,ten,ngaySinh,gioiTinh,dienThoai,email,noiSinh,doituong,khuvuc`
- Note: Currently shows a placeholder message; requires Apache POI library for actual Excel parsing

#### DiemImportPanel

```
CSV Template:
sobaodanh,phuongthuc,TO,LI,HO,SI,VA,SU,DI,N1,NL1,NK1,NK2
TS0001,XTT,25.50,24.00,23.75,22.00,24.25,23.00,22.50,23.00,25.00,22.00,24.00
TS0001,VHAT,24.00,23.50,22.00,21.00,23.00,22.50,22.00,22.50,24.00,,
TS0001,DGNL,780.0,750.0,,720.0,,680.0,,,

Supported columns:
- TO, LI, HO, SI, VA, SU, DI  : Regular subjects
- N1, NL1                      : Aptitude test subjects
- NK1, NK2                      : Talent/Art subjects
```

---

## 9. Main Business Flows

### 9.1 Candidate Registration Flow

```
1. User opens /register (GET)
   └─ RegisterServlet.handleGet()
      ├─ Load DoiTuongUutien list → request attribute
      └─ Load KhuVucUutien list  → request attribute
      └─ forward register.jsp

2. User fills form and submits (POST) → /register
   └─ RegisterServlet.handlePost()
      ├─ Validate: username, password, confirmPassword, ho, ten, cccd
      ├─ Check password == confirmPassword
      ├─ Check password.length >= 6
      ├─ Check CCCD uniqueness (ThiSinhService.findByCccd)
      │
      ├─ Create NguoiDung:
      │   vaiTro = new VaiTro(vaitro_id=2)   ← USER role
      │   passwordHash = PasswordUtil.hashPassword(rawPassword)
      │   nguoiDungDao.save(nguoiDung)
      │
      ├─ Create ThiSinh:
      │   nguoiDung = savedNguoiDung
      │   cccd, ho, ten, ngaySinh, gioiTinh, dienThoai, email, noiSinh
      │   doiTuongUutien = findById(doituongId)   (nullable)
      │   khuVucUutien  = findById(khuvucId)     (nullable)
      │   thiSinhDao.save(thiSinh)
      │
      ├─ Set session["nguoidung"] = savedNguoiDung
      └─ redirect /dashboard
```

### 9.2 Score Entry Flow

```
1. Candidate logs in → Dashboard
   └─ Goes to /scores (GET)
      └─ ScoresServlet.handleGet()
         └─ ThiSinhService.findById(nguoidung_id) → get ThiSinh
         └─ DiemThiDao.findByThiSinhId(thisinhId) → list
         └─ forward scores.jsp

2. Candidate clicks "Nhap diem moi" → Bootstrap modal opens
   └─ Selects PhuongThuc (XTT/VHAT/DGNL/THPT/NK)
   └─ Enters subject scores
   └─ Submits (POST /scores)

3. ScoresServlet.handlePost()
   └─ Find ThiSinh by logged-in user
   └─ Find PhuongThuc by selected ID
   └─ Create DiemThi:
       diemThi.thiSinh = thiSinh
       diemThi.phuongThuc = phuongThuc
       diemThi.namTuyensinh = 2026
       DiemThi saved = diemThiService.save(diemThi)  // persist to get ID
   └─ For each Mon with a score:
       DiemThiChiTiet ct = new DiemThiChiTiet()
       ct.diemThi = saved
       ct.mon = mon
       ct.diemGoc = score
       ct.diemQuydoi = score          // Not yet converted
       ct.diemSudung = score           // Not yet converted
       saved.danhSachDiemChiTiet.add(ct)
   └─ diemThiService.update(saved)   // cascade persist children
   └─ redirect /scores
```

### 9.3 Preference Registration Flow (Candidate)

```
1. Candidate clicks "Dang ky nguyen vong" → /add-nguyenvong (GET)
   └─ AddNguyenVongServlet.handleGet()
      ├─ Load all active Nganh list
      ├─ Load all active PhuongThuc list
      ├─ Load all NganhToHop list
      ├─ Count existing NguyenVong for this candidate
      ├─ If count >= 5 → show warning
      └─ forward add-nguyenvong-form.jsp

2. add-nguyenvong-form.jsp
   └─ User selects Nganh → JS populates NganhToHop dropdown
   └─ User selects NganhToHop → JS shows info
   └─ User selects PhuongThuc
   └─ Form submits (POST /add-nguyenvong)

3. AddNguyenVongServlet.handlePost()
   ├─ Validate count < 5
   ├─ Validate nganhId, nganhToHopId, phuongThucId
   ├─ Check duplicate: findByThiSinhNganhToHopPhuongThuc()
   │   └─ If found → error "Nguyen vong nay da ton tai"
   ├─ nextOrder = existingCount + 1
   ├─ Create NguyenVong:
   │   thiSinh = current thiSinh
   │   nganh = findById(nganhId)
   │   nganhToHop = findById(nganhToHopId)
   │   phuongThuc = findById(phuongThucId)
   │   thuTu = nextOrder
   │   ketQua = CHO_XET
   │   (diemXettuyen = null — calculated later by admin)
   ├─ nguyenVongService.save(nguyenVong)
   └─ redirect /nguyenvong
```

### 9.4 Admission Processing Flow (Admin)

```
1. Admin opens admin-app → LoginFrame → MainFrame

2. Menu: Nguyen vong → Xet tuyen → XetTuyenPanel

3. Admin selects PhuongThuc + Nganh

4. Admin clicks "Bat dau xet tuyen"

5. XetTuyenPanel.xetTuyen() → SwingWorker
   ├─ nguyenVongService.findAll()
   ├─ For each NguyenVong:
   │   ├─ Filter: matches selected Nganh AND selected PhuongThuc
   │   ├─ Get diemXettuyen from NguyenVong (pre-calculated by admin earlier)
   │   ├─ Get diemSan from Nganh
   │   ├─ Rule:
   │   │     if (diemXettuyen >= diemSan AND countTrungTuyen < chiTieu):
   │   │         ketQua = "TRUNG_TUYEN"
   │   │         countTrungTuyen++
   │   │     else:
   │   │         ketQua = "CHO_XET"
   │   └─ nguyenVongService.update(nv)  → save ketQua
   └─ Display real-time log in JTextArea

6. Admin can then view NguyenVongPanel to see all results
```

---

## 10. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 11+ |
| ORM | Hibernate | 5.4.7.Final |
| Database | MySQL | 8.0 |
| Web Server | Jetty | 9.4.44 |
| Web UI | JSP + Bootstrap | 5.3.0 |
| Desktop UI | Swing (JavaFX-style CardLayout) | built-in |
| JSON | Gson | (included with web container) |
| Data | Lombok | 1.18.30 |
| Build | Maven | 3.x |
| JPA | javax.persistence-api | 2.2 |

---

## Appendix A: NguoiDung Entity

```java
@Entity
@Table(name = "xt_nguoidung")
public class NguoiDung {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nguoidung_id")
    private Integer nguoidungId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "email", unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaitro_id", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public boolean isAdmin() {
        return vaiTro != null && VaiTro.ADMIN.equals(vaiTro.getMaVaitro());
    }
}
```

## Appendix B: NguyenVong Entity — The Core Admission Entity

```java
@Entity
@Table(name = "xt_nguyenvong")
public class NguyenVong {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer nguyenvongId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thisinh_id", nullable = false)
    private ThiSinh thiSinh;           // Candidate

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_id", nullable = false)
    private Nganh nganh;               // Target major

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_tohop_id", nullable = false)
    private NganhToHop nganhToHop;     // Major+Combination

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuongthuc_id", nullable = false)
    private PhuongThuc phuongThuc;     // Admission method

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu;              // Priority order (1–5)

    @Column(name = "diem_thxt", precision = 10, scale = 5)
    private BigDecimal diemThxt;         // Subject score (total)

    @Column(name = "diem_thgxt", precision = 10, scale = 5)
    private BigDecimal diemThgxt;        // Aptitude/talent score

    @Column(name = "diem_cong", precision = 6, scale = 2)
    private BigDecimal diemCong;         // Bonus score

    @Column(name = "diem_uutien", precision = 6, scale = 2)
    private BigDecimal diemUutien;       // Priority zone score

    @Column(name = "diem_xettuyen", precision = 10, scale = 5)
    private BigDecimal diemXettuyen;     // Final admission score

    @Column(name = "ket_qua", length = 45)
    private String ketQua;               // CHO_XET / TRUNG_TUYEN / TRUOT / PHOI_DU_KIEN

    public static final class KetQua {
        public static final String CHO_XET     = "CHO_XET";
        public static final String TRUNG_TUYEN  = "TRUNG_TUYEN";
        public static final String TRUOT       = "TRUOT";
        public static final String PHOI_DU_KIEN = "PHOI_DU_KIEN";
    }
}
```

## Appendix C: Admin User Credentials

```
Username: admin
Password: admin123
Role: ADMIN (vaitro_id = 1)

(This account must exist in xt_nguoidung table with vaiTro_id = 1)
```

---

*Document generated for codebase: TuyenSinhDaiHoc_2026*
*Technology: Java 11 + Hibernate 5.4.7 + MySQL 8.0 + JSP/Servlet + Swing*
