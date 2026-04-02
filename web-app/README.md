# Tuyen Sinh Dai Hoc 2026 - Web Application

Ung dung web cho thi sinh dang ky xet tuyen dai hoc.

## Cau truc du an

```
web-app/
├── src/main/
│   ├── java/com/tuyensinh/web/
│   │   ├── JettyLauncher.java              # Khoi dong server Jetty
│   │   └── servlet/
│   │       ├── BaseServlet.java            # Servlet co so
│   │       ├── LoginServlet.java           # Dang nhap
│   │       ├── RegisterServlet.java         # Dang ky
│   │       ├── LogoutServlet.java          # Dang xuat
│   │       ├── DashboardServlet.java       # Trang chu
│   │       ├── ProfileServlet.java        # Ho so ca nhan
│   │       ├── ScoresServlet.java         # Quan ly diem thi
│   │       ├── NguyenVongServlet.java      # Quan ly nguyen vong
│   │       └── AddNguyenVongServlet.java   # Them nguyen vong
│   └── webapp/
│       ├── index.jsp                       # Trang dang nhap
│       └── WEB-INF/
│           ├── web.xml                     # Cau hinh Servlet
│           └── views/
│               ├── header.jsp              # Navigation bar
│               ├── footer.jsp              # Footer
│               ├── login.jsp               # Trang dang nhap
│               ├── register.jsp            # Trang dang ky
│               ├── dashboard.jsp           # Trang chu sau dang nhap
│               ├── profile.jsp             # Chinh sua ho so
│               ├── scores.jsp              # Quan ly diem thi
│               ├── nguyenvong.jsp          # Danh sach nguyen vong
│               └── add-nguyenvong-form.jsp # Form them nguyen vong
└── pom.xml
```

## Chay ung dung

### Cách 1: Su dung Maven Jetty Plugin

```bash
cd web-app
mvn jetty:run
```

Ung dung se chay tai: http://localhost:8080

### Cách 2: Su dung JettyLauncher

```bash
cd web-app
mvn compile
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFilterFile=true -DincludeScope=compile) com.tuyensinh.web.JettyLauncher
```

## Tinh nang

- **Dang ky/Dang nhap**: Thi sinh co the tao tai khoan va dang nhap vao he thong
- **Trang chu**: Hien thi thong tin tong quat, thong ke so luong nguyen vong va diem thi
- **Ho so ca nhan**: Cho phep thi sinh xem va cap nhat thong tin ca nhan
- **Diem thi**: Quan ly thong tin diem thi theo phuong thuc xet tuyen
- **Nguyen vong**: Dang ky va quan ly danh sach nguyen vong xet tuyen (toi da 5 nguyen vong)

## Cau hinh

### Database

Dam bao MySQL database da duoc cau hinh voi cac bang can thiet:
- `xt_nguoidung` - Tai khoan nguoi dung
- `xt_thisinh` - Thong tin thi sinh
- `xt_diemthi` - Bang diem thi
- `xt_diemthi_chitiet` - Chi tiet diem thi
- `xt_nguyenvong` - Nguyen vong xet tuyen
- `xt_nganh` - Danh sach nganh
- `xt_phuongthuc` - Phuong thuc xet tuyen
- `xt_tohop` - To hop mon
- `xt_doituong_uutien` - Doi tuong uu tien
- `xt_khuvuc_uutien` - Khu vuc uu tien

### Hibernate

File cau hinh: `core/src/main/resources/hibernate.cfg.xml`

## Phan quyen

- **ADMIN** (vaitro_id=1): Quan tri vien
- **USER** (vaitro_id=2): Thi sinh

## Giao dien

Su dung Bootstrap 5 voi cac tinh nang:
- Thiet ke responsive
- Card-based layout
- Navigation bar voi Bootstrap Icons
- Flash messages cho thong bao

## Phat trien them

De them trang moi:

1. Tao Servlet ke thua `BaseServlet`
2. Them cau hinh servlet-mapping trong `web.xml`
3. Tao JSP view trong `WEB-INF/views/`
4. Them link trong `header.jsp` neu can


chạy web   
cd web-app
mvn jetty:run