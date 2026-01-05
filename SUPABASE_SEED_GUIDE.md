# 📚 Hướng Dẫn Seed Data vào Supabase

## 🎯 Tổng Quan

File này hướng dẫn các cách để import seed data vào Supabase database cho dự án Bookverse.

---

## ✅ Cách 1: Sử dụng Supabase Dashboard (Đơn giản nhất)

### Bước 1: Đăng nhập Supabase
1. Truy cập: https://supabase.com
2. Đăng nhập và chọn project của bạn

### Bước 2: Mở SQL Editor
1. Click **SQL Editor** ở sidebar bên trái
2. Click nút **New Query**

### Bước 3: Copy & Paste SQL
1. Mở file `seed_data.sql`
2. Copy toàn bộ nội dung (Ctrl+A, Ctrl+C)
3. Paste vào SQL Editor (Ctrl+V)
4. Click **Run** hoặc nhấn `Ctrl + Enter`

### Bước 4: Xác Nhận
Chạy các query sau để verify:
```sql
SELECT COUNT(*) as total_users FROM "user";
SELECT COUNT(*) as total_books FROM book_meta;
SELECT COUNT(*) as total_listings FROM listing;
SELECT COUNT(*) as total_orders FROM "order";
```

**✅ Ưu điểm:** Đơn giản, trực quan, không cần cài đặt gì thêm  
**❌ Nhược điểm:** Thủ công, không tự động hóa được

---

## 🔧 Cách 2: Sử dụng psql (PostgreSQL CLI)

### Bước 1: Cài đặt PostgreSQL Client
```bash
# Windows (Chocolatey)
choco install postgresql

# macOS
brew install postgresql

# Linux (Ubuntu/Debian)
sudo apt-get install postgresql-client
```

### Bước 2: Lấy Database Connection String
1. Vào Supabase Dashboard
2. Vào **Settings** > **Database**
3. Scroll xuống **Connection String**
4. Chọn tab **URI** và copy connection string
5. Format: `postgresql://postgres:[YOUR-PASSWORD]@[HOST]:5432/postgres`

### Bước 3: Chạy Script
```bash
# Windows PowerShell
$env:DATABASE_URL="postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres"
psql $env:DATABASE_URL -f seed_data.sql

# macOS/Linux
export DATABASE_URL="postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres"
psql $DATABASE_URL -f seed_data.sql
```

**✅ Ưu điểm:** Nhanh, có thể tự động hóa  
**❌ Nhược điểm:** Cần cài đặt psql

---

## 🚀 Cách 3: Sử dụng Supabase CLI

### Bước 1: Cài đặt Supabase CLI
```bash
npm install -g supabase
```

### Bước 2: Login
```bash
supabase login
```

### Bước 3: Link Project
```bash
# Replace YOUR_PROJECT_REF with your actual project reference
supabase link --project-ref YOUR_PROJECT_REF
```

### Bước 4: Run Seed Script
```bash
# Windows
.\supabase_seed.ps1

# macOS/Linux
chmod +x supabase_seed.sh
./supabase_seed.sh
```

**✅ Ưu điểm:** Professional, tích hợp tốt với workflow  
**❌ Nhược điểm:** Cần setup ban đầu

---

## 🔐 Cách 4: Sử dụng Backend API (Từ Spring Boot)

### Tạo Seeder Service trong Spring Boot

```java
@Service
@RequiredArgsConstructor
public class DatabaseSeederService {
    
    @Value("${seed.enabled:false}")
    private boolean seedEnabled;
    
    private final JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void init() {
        if (seedEnabled) {
            seedDatabase();
        }
    }
    
    public void seedDatabase() {
        try {
            Resource resource = new ClassPathResource("seed_data.sql");
            String sql = new String(Files.readAllBytes(Paths.get(resource.getURI())));
            jdbcTemplate.execute(sql);
            log.info("✅ Database seeded successfully!");
        } catch (Exception e) {
            log.error("❌ Error seeding database", e);
        }
    }
}
```

### Cấu hình trong application.properties
```properties
# Enable seeding (set to true only once!)
seed.enabled=false
```

**✅ Ưu điểm:** Tự động, không cần manual intervention  
**❌ Nhược điểm:** Nguy hiểm nếu chạy nhầm trên production

---

## 🛠️ Cách 5: Sử dụng DBeaver/TablePlus (GUI Tools)

### Bước 1: Download Tool
- **DBeaver**: https://dbeaver.io/download/
- **TablePlus**: https://tableplus.com/

### Bước 2: Connect to Supabase
1. Tạo new connection (PostgreSQL)
2. Nhập connection details từ Supabase Dashboard:
   - Host: `db.[PROJECT-REF].supabase.co`
   - Port: `5432`
   - Database: `postgres`
   - User: `postgres`
   - Password: `[YOUR-PASSWORD]`

### Bước 3: Execute Script
1. Mở SQL Editor
2. Load file `seed_data.sql`
3. Execute (F5 hoặc Run button)

**✅ Ưu điểm:** GUI thân thiện, dễ debug  
**❌ Nhược điểm:** Cần cài đặt thêm software

---

## 📝 Lưu Ý Quan Trọng

### ⚠️ Trước Khi Seed:

1. **Backup database** (nếu có data quan trọng)
```sql
-- Export existing data
pg_dump $DATABASE_URL > backup_$(date +%Y%m%d).sql
```

2. **Kiểm tra schema** đã khớp với entities chưa
3. **Test trên local database trước**

### ⚠️ Sau Khi Seed:

1. **Verify data integrity:**
```sql
-- Check foreign key constraints
SELECT * FROM information_schema.table_constraints 
WHERE constraint_type = 'FOREIGN KEY';

-- Check data counts
SELECT 
    'Users' as table_name, COUNT(*) as count FROM "user"
UNION ALL
SELECT 'Books', COUNT(*) FROM book_meta
UNION ALL
SELECT 'Listings', COUNT(*) FROM listing;
```

2. **Reset sequences nếu cần:**
```sql
-- Reset auto increment sequences
SELECT setval('user_id_seq', (SELECT MAX(id) FROM "user"));
SELECT setval('book_meta_id_seq', (SELECT MAX(id) FROM book_meta));
SELECT setval('listing_id_seq', (SELECT MAX(id) FROM listing));
```

3. **Update statistics:**
```sql
ANALYZE;
```

---

## 🔄 Reset Database (Xóa Tất Cả Data)

**⚠️ NGUY HIỂM - CHỈ DÙNG TRONG DEVELOPMENT!**

```sql
-- Disable foreign key checks
SET session_replication_role = 'replica';

-- Truncate all tables
TRUNCATE TABLE 
    notification,
    order_timeline,
    order_item,
    "order",
    cart_item,
    cart,
    likes,
    wishlist,
    review,
    listing_photo,
    listing,
    book_meta_categories,
    book_meta_authors,
    book_meta,
    category,
    author,
    shipping_address,
    user_profile,
    user_role,
    "user",
    role,
    voucher
RESTART IDENTITY CASCADE;

-- Re-enable foreign key checks
SET session_replication_role = 'origin';
```

---

## 🎯 Khuyến Nghị

**Cho Development:**
- Dùng **Cách 1** (Dashboard) - Đơn giản nhất
- Hoặc **Cách 2** (psql) - Nhanh hơn

**Cho Production:**
- Dùng **Migration files** thay vì seed data
- Có backup plan
- Test kỹ trước

**Cho CI/CD:**
- Dùng **Cách 3** (Supabase CLI)
- Tự động hóa trong pipeline

---

## 📞 Hỗ Trợ

Nếu gặp lỗi:

1. **Error: "relation does not exist"**
   - Chạy migrations trước khi seed
   - Check table names đúng chưa

2. **Error: "duplicate key value"**
   - Database đã có data
   - Reset database trước khi seed

3. **Error: "permission denied"**
   - Check database permissions
   - Dùng đúng password

4. **Timeout**
   - Chia nhỏ SQL file
   - Tăng connection timeout

---

## ✨ Demo Data Included

Seed data bao gồm:
- ✅ 5 users (admin, sellers, buyers)
- ✅ 8 authors (famous authors)
- ✅ 10 categories
- ✅ 8 books (popular titles)
- ✅ 10 listings (various conditions)
- ✅ 8 reviews
- ✅ 3 orders (different statuses)
- ✅ Shopping carts, wishlists, likes
- ✅ Vouchers and notifications

Perfect for testing! 🎉
