-- ============================================
-- BOOKVERSE DEMO SEED DATA v2
-- Compatible with Hibernate-generated schema
-- ============================================

-- ============================================
-- 1. CREATE DEMO USERS (password: password123)
-- ============================================

INSERT INTO "user" (id, username, email, password_hash, enabled, failed_login_count, auth_provider, created_at, updated_at)
VALUES 
    (2, 'tinvo', 'tinvo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 0, 'LOCAL', NOW(), NOW()),
    (3, 'seller1', 'seller1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 0, 'LOCAL', NOW(), NOW()),
    (4, 'buyer1', 'buyer1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 0, 'LOCAL', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Assign roles
INSERT INTO user_role (user_id, role_id) VALUES
    (2, 1), (2, 2),  -- tinvo is USER + SELLER
    (3, 1), (3, 2),  -- seller1 is USER + SELLER
    (4, 1)           -- buyer1 is USER
ON CONFLICT DO NOTHING;

-- User profiles (phone_number instead of phone)
INSERT INTO user_profile (id, user_id, display_name, avatar_url, bio, phone_number, account_type, is_pro_seller, created_at, updated_at)
VALUES
    (2, 2, 'Tin Vo', 'https://i.pravatar.cc/150?u=tinvo', 'Passionate book collector and seller', '+84912345678', 'BUSINESS', true, NOW(), NOW()),
    (3, 3, 'Book Seller Pro', 'https://i.pravatar.cc/150?u=seller1', 'Quality used books at great prices', '+84923456789', 'BUSINESS', false, NOW(), NOW()),
    (4, 4, 'Happy Reader', 'https://i.pravatar.cc/150?u=buyer1', 'Love reading sci-fi and fantasy', '+84934567890', 'PERSONAL', false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 2. AUTHORS
-- ============================================

INSERT INTO author (id, name, bio, created_at, updated_at) VALUES
    (1, 'Joshua Bloch', 'Software engineer and author, known for his work on Java.', NOW(), NOW()),
    (2, 'Robert C. Martin', 'Software consultant and author, known as Uncle Bob.', NOW(), NOW()),
    (3, 'Yuval Noah Harari', 'Israeli historian and professor, author of Sapiens.', NOW(), NOW()),
    (4, 'J.K. Rowling', 'British author, best known for the Harry Potter series.', NOW(), NOW()),
    (5, 'George Orwell', 'English novelist and essayist, author of 1984.', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 3. BOOKS (BookMeta) - no cover_image_url
-- ============================================

INSERT INTO book_meta (id, title, isbn, publisher, pages, description, language, average_rating, total_reviews, created_at, updated_at) VALUES
    (1, 'Effective Java', '9780134685991', 'Addison-Wesley', 416, 'The definitive guide to Java programming language best practices.', 'en', 4.7, 0, NOW(), NOW()),
    (2, 'Clean Code', '9780132350884', 'Prentice Hall', 464, 'A Handbook of Agile Software Craftsmanship.', 'en', 4.6, 0, NOW(), NOW()),
    (3, 'Sapiens', '9780062316110', 'Harper', 443, 'A brief history of humankind from the Stone Age to modern age.', 'en', 4.5, 0, NOW(), NOW()),
    (4, 'Harry Potter', '9780439708180', 'Scholastic', 309, 'The first book in the Harry Potter series.', 'en', 4.8, 0, NOW(), NOW()),
    (5, '1984', '9780451524935', 'Signet Classic', 328, 'A dystopian social science fiction novel.', 'en', 4.7, 0, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Book images (separate table)
INSERT INTO book_image (id, url, book_id, is_primary, created_at) VALUES
    (1, 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg', 1, true, NOW()),
    (2, 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg', 2, true, NOW()),
    (3, 'https://covers.openlibrary.org/b/isbn/9780062316110-L.jpg', 3, true, NOW()),
    (4, 'https://covers.openlibrary.org/b/isbn/9780439708180-L.jpg', 4, true, NOW()),
    (5, 'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg', 5, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 4. BOOK-AUTHOR & BOOK-CATEGORY (book_id not book_meta_id)
-- ============================================

INSERT INTO book_author (book_id, author_id) VALUES
    (1, 1), (2, 2), (3, 3), (4, 4), (5, 5)
ON CONFLICT DO NOTHING;

INSERT INTO book_category (book_id, category_id) VALUES
    (1, 1), (1, 7),  -- Effective Java: Technology, Programming
    (2, 1), (2, 7),  -- Clean Code: Technology, Programming
    (3, 3), (3, 5),  -- Sapiens: Non-Fiction, History
    (4, 2), (4, 6),  -- Harry Potter: Fiction, Fantasy
    (5, 2)           -- 1984: Fiction
ON CONFLICT DO NOTHING;

-- ============================================
-- 5. LISTINGS (Books for Sale)
-- ============================================

INSERT INTO listing (id, book_id, seller_id, price, original_price, currency, condition, quantity, description, location, status, visibility, views, likes, sold_count, free_shipping, created_at, updated_at) VALUES
    (1, 1, 2, 35.00, 45.00, 'USD', 'LIKE_NEW', 3, 'Excellent condition, barely used. Perfect for Java developers!', 'Ho Chi Minh City', 'ACTIVE', true, 125, 15, 2, true, NOW(), NOW()),
    (2, 2, 2, 28.00, 40.00, 'USD', 'LIKE_NEW', 5, 'Almost new condition. A must-have for software engineers!', 'Ho Chi Minh City', 'ACTIVE', true, 200, 32, 5, true, NOW(), NOW()),
    (3, 3, 3, 18.00, 25.00, 'USD', 'GOOD', 4, 'Good reading condition. Fascinating historical perspective.', 'Hanoi', 'ACTIVE', true, 90, 12, 2, false, NOW(), NOW()),
    (4, 4, 3, 15.00, 20.00, 'USD', 'GOOD', 2, 'Well-loved copy, some wear on cover. Great story!', 'Hanoi', 'ACTIVE', true, 300, 45, 8, true, NOW(), NOW()),
    (5, 5, 2, 12.00, 15.00, 'USD', 'GOOD', 3, 'Classic dystopian novel in good condition.', 'Ho Chi Minh City', 'ACTIVE', true, 180, 28, 4, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 6. LISTING PHOTOS
-- ============================================

INSERT INTO listing_photo (id, listing_id, url, position, created_at) VALUES
    (1, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800', 0, NOW()),
    (2, 1, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=800', 1, NOW()),
    (3, 2, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800', 0, NOW()),
    (4, 3, 'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=800', 0, NOW()),
    (5, 4, 'https://images.unsplash.com/photo-1551847812-f815db5b52e3?w=800', 0, NOW()),
    (6, 5, 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=800', 0, NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 7. VOUCHERS (check actual columns)
-- ============================================

INSERT INTO voucher (id, code, description, discount_type, discount_value, min_order_value, max_usage_per_user, usage_limit, used_count, is_active, valid_from, valid_to, created_at) VALUES
    (1, 'WELCOME10', 'Welcome discount - 10% off', 'PERCENT', 10.00, 20.00, 1, 100, 0, true, NOW(), NOW() + INTERVAL '30 days', NOW()),
    (2, 'DEMO2026', 'Demo special - 15% off', 'PERCENT', 15.00, 30.00, 2, 50, 0, true, NOW(), NOW() + INTERVAL '7 days', NOW()),
    (3, 'FREESHIP', 'Free shipping over $25', 'FIXED', 5.00, 25.00, 3, 200, 0, true, NOW(), NOW() + INTERVAL '60 days', NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- RESET SEQUENCES
-- ============================================

SELECT setval(pg_get_serial_sequence('"user"', 'id'), COALESCE((SELECT MAX(id) FROM "user"), 1), true);
SELECT setval(pg_get_serial_sequence('author', 'id'), COALESCE((SELECT MAX(id) FROM author), 1), true);
SELECT setval(pg_get_serial_sequence('category', 'id'), COALESCE((SELECT MAX(id) FROM category), 1), true);
SELECT setval(pg_get_serial_sequence('book_meta', 'id'), COALESCE((SELECT MAX(id) FROM book_meta), 1), true);
SELECT setval(pg_get_serial_sequence('listing', 'id'), COALESCE((SELECT MAX(id) FROM listing), 1), true);
SELECT setval(pg_get_serial_sequence('listing_photo', 'id'), COALESCE((SELECT MAX(id) FROM listing_photo), 1), true);
SELECT setval(pg_get_serial_sequence('voucher', 'id'), COALESCE((SELECT MAX(id) FROM voucher), 1), true);
SELECT setval(pg_get_serial_sequence('user_profile', 'id'), COALESCE((SELECT MAX(id) FROM user_profile), 1), true);
SELECT setval(pg_get_serial_sequence('book_image', 'id'), COALESCE((SELECT MAX(id) FROM book_image), 1), true);

-- ============================================
-- VERIFICATION
-- ============================================

SELECT 'Users: ' || COUNT(*) FROM "user";
SELECT 'Books: ' || COUNT(*) FROM book_meta;
SELECT 'Listings: ' || COUNT(*) FROM listing;
SELECT 'Authors: ' || COUNT(*) FROM author;

SELECT '✅ Demo seed v2 completed!' as status;
