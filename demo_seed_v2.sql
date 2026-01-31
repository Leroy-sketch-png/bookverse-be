-- ============================================
-- BOOKVERSE DEMO SEED DATA v2
-- Professional Demo Data - January 2026
-- ALL PASSWORDS: Bookverse2026!
-- ============================================

-- ============================================
-- 1. CREATE DEMO USERS
-- Password hash for: Bookverse2026!
-- ============================================

INSERT INTO "user" (id, username, email, password_hash, enabled, failed_login_count, auth_provider, created_at, updated_at)
VALUES 
    (2, 'tinvo', 'tinvo@bookverse.vn', '$2a$10$EqKcp1WFKVQISheBxkXpUO.CpWkE.4Fo5rPWYzBs/B5TBbFQNY7GS', true, 0, 'LOCAL', NOW(), NOW()),
    (3, 'sarahchen', 'sarah.chen@bookverse.vn', '$2a$10$EqKcp1WFKVQISheBxkXpUO.CpWkE.4Fo5rPWYzBs/B5TBbFQNY7GS', true, 0, 'LOCAL', NOW(), NOW()),
    (4, 'alexnguyen', 'alex.nguyen@bookverse.vn', '$2a$10$EqKcp1WFKVQISheBxkXpUO.CpWkE.4Fo5rPWYzBs/B5TBbFQNY7GS', true, 0, 'LOCAL', NOW(), NOW()),
    (5, 'minhpham', 'minh.pham@bookverse.vn', '$2a$10$EqKcp1WFKVQISheBxkXpUO.CpWkE.4Fo5rPWYzBs/B5TBbFQNY7GS', true, 0, 'LOCAL', NOW(), NOW()),
    (6, 'emilytran', 'emily.tran@bookverse.vn', '$2a$10$EqKcp1WFKVQISheBxkXpUO.CpWkE.4Fo5rPWYzBs/B5TBbFQNY7GS', true, 0, 'LOCAL', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Assign roles (role_id: 1=USER, 2=SELLER, 3=PRO_SELLER, 4=MODERATOR, 5=ADMIN)
INSERT INTO user_role (user_id, role_id) VALUES
    (2, 1), (2, 2), (2, 3),  -- tinvo: PRO Seller
    (3, 1), (3, 2),          -- sarahchen: Seller
    (4, 1),                  -- alexnguyen: Buyer
    (5, 1), (5, 2),          -- minhpham: Seller
    (6, 1)                   -- emilytran: Buyer
ON CONFLICT DO NOTHING;

-- User profiles
INSERT INTO user_profile (id, user_id, display_name, avatar_url, bio, phone_number, account_type, is_pro_seller, created_at, updated_at)
VALUES
    (2, 2, 'Tin Vo Books', 'https://i.pravatar.cc/150?u=tinvo', 'Curated collection of programming and technology books. Fast shipping, excellent condition guaranteed.', '0912345678', 'PRO_SELLER', true, NOW(), NOW()),
    (3, 3, 'Sarah Chen', 'https://i.pravatar.cc/150?u=sarahchen', 'History and non-fiction enthusiast. Building a community of thoughtful readers.', '0923456789', 'SELLER', false, NOW(), NOW()),
    (4, 4, 'Alex Nguyen', 'https://i.pravatar.cc/150?u=alexnguyen', 'Avid reader and book collector since 2018.', '0934567890', 'BUYER', false, NOW(), NOW()),
    (5, 5, 'Minh Pham', 'https://i.pravatar.cc/150?u=minhpham', 'Sharing beloved books with new readers. Specializing in fiction and fantasy.', '0945678901', 'SELLER', false, NOW(), NOW()),
    (6, 6, 'Emily Tran', 'https://i.pravatar.cc/150?u=emilytran', 'Software engineer who loves reading about design and creativity.', '0956789012', 'BUYER', false, NOW(), NOW())
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
-- 7. SHIPPING ADDRESSES (for orders)
-- ============================================

INSERT INTO shipping_address (id, user_id, full_name, phone, address_line1, address_line2, ward, district, city, postal_code, country, is_default, created_at, updated_at) VALUES
    (1, 4, 'Alex Nguyen', '0934567890', '123 Nguyen Hue Street', 'Apartment 5A', 'Ben Nghe Ward', 'District 1', 'Ho Chi Minh City', '70000', 'Vietnam', true, NOW(), NOW()),
    (2, 6, 'Emily Tran', '0956789012', '456 Le Loi Boulevard', NULL, 'Ben Thanh Ward', 'District 1', 'Ho Chi Minh City', '70000', 'Vietnam', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 8. ORDERS (DELIVERED for balance calculation)
-- ============================================

INSERT INTO orders (id, user_id, order_number, status, subtotal, total_amount, tax, shipping, discount, total, shipping_address_id, tracking_number, carrier, shipped_at, delivered_at, created_at, updated_at) VALUES
    -- Orders for tinvo (PRO seller)
    (1, 4, 'BV-2026-0001', 'DELIVERED', 150.00, 150.00, 0.00, 0.00, 0.00, 150.00, 1, 'GHN7823456789', 'GHN Express', NOW() - INTERVAL '10 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '14 days', NOW()),
    (2, 4, 'BV-2026-0002', 'DELIVERED', 200.00, 200.00, 0.00, 0.00, 0.00, 200.00, 1, 'GHN7834567890', 'GHN Express', NOW() - INTERVAL '8 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '12 days', NOW()),
    (3, 6, 'BV-2026-0003', 'DELIVERED', 100.00, 100.00, 0.00, 0.00, 0.00, 100.00, 2, 'GHN7845678901', 'GHN Express', NOW() - INTERVAL '6 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '10 days', NOW()),
    -- Orders for sarahchen (casual seller)
    (4, 4, 'BV-2026-0004', 'DELIVERED', 75.00, 75.00, 0.00, 5.00, 0.00, 80.00, 1, 'GHN7856789012', 'GHN Express', NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '8 days', NOW()),
    (5, 6, 'BV-2026-0005', 'DELIVERED', 50.00, 50.00, 0.00, 5.00, 0.00, 55.00, 2, 'GHN7867890123', 'GHN Express', NOW() - INTERVAL '4 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '6 days', NOW()),
    -- Pending order (for demo flow)
    (6, 6, 'BV-2026-0006', 'PENDING', 35.00, 35.00, 0.00, 0.00, 0.00, 35.00, 2, NULL, NULL, NULL, NULL, NOW() - INTERVAL '1 day', NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 9. ORDER ITEMS (link orders to listings and sellers)
-- ============================================

INSERT INTO order_items (id, order_id, listing_id, book_id, seller_id, title, author, cover_image, quantity, price, subtotal, created_at) VALUES
    -- Order 1: tinvo's listings
    (1, 1, 1, 1, 2, 'Effective Java', 'Joshua Bloch', 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg', 2, 35.00, 70.00, NOW()),
    (2, 1, 2, 2, 2, 'Clean Code', 'Robert C. Martin', 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg', 1, 28.00, 28.00, NOW()),
    (3, 1, 5, 5, 2, '1984', 'George Orwell', 'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg', 1, 12.00, 12.00, NOW()),
    -- Order 2: tinvo's listings
    (4, 2, 2, 2, 2, 'Clean Code', 'Robert C. Martin', 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg', 3, 28.00, 84.00, NOW()),
    (5, 2, 1, 1, 2, 'Effective Java', 'Joshua Bloch', 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg', 1, 35.00, 35.00, NOW()),
    -- Order 3: tinvo's listings
    (6, 3, 5, 5, 2, '1984', 'George Orwell', 'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg', 3, 12.00, 36.00, NOW()),
    (7, 3, 2, 2, 2, 'Clean Code', 'Robert C. Martin', 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg', 1, 28.00, 28.00, NOW()),
    -- Order 4: sarahchen's listings
    (8, 4, 3, 3, 3, 'Sapiens', 'Yuval Noah Harari', 'https://covers.openlibrary.org/b/isbn/9780062316110-L.jpg', 2, 18.00, 36.00, NOW()),
    (9, 4, 4, 4, 3, 'Harry Potter', 'J.K. Rowling', 'https://covers.openlibrary.org/b/isbn/9780439708180-L.jpg', 1, 15.00, 15.00, NOW()),
    -- Order 5: sarahchen's listings
    (10, 5, 4, 4, 3, 'Harry Potter', 'J.K. Rowling', 'https://covers.openlibrary.org/b/isbn/9780439708180-L.jpg', 2, 15.00, 30.00, NOW()),
    -- Order 6: pending order (tinvo)
    (11, 6, 1, 1, 2, 'Effective Java', 'Joshua Bloch', 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg', 1, 35.00, 35.00, NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 10. REVIEWS (for seller reputation & AI summary)
-- ============================================

INSERT INTO review (id, order_item_id, listing_id, seller_id, user_id, rating, comment, helpful_count, verified_purchase, created_at, updated_at) VALUES
    -- Reviews for tinvo (PRO seller)
    (1, 1, 1, 2, 4, 5, 'Excellent condition, exactly as described. Fast shipping and professional packaging. Highly recommend!', 12, true, NOW() - INTERVAL '6 days', NOW()),
    (2, 2, 2, 2, 4, 5, 'Book arrived in pristine condition. Great communication throughout. Will definitely buy again.', 8, true, NOW() - INTERVAL '6 days', NOW()),
    (3, 3, 5, 2, 4, 4, 'Good book in accurate condition. Shipping took a bit longer than expected but seller kept me updated.', 3, true, NOW() - INTERVAL '6 days', NOW()),
    (4, 4, 2, 2, 4, 5, 'Another fantastic purchase from this seller. Books are always carefully packaged.', 5, true, NOW() - INTERVAL '4 days', NOW()),
    (5, 6, 5, 2, 6, 5, 'Outstanding seller! Lightning fast shipping and the book condition exceeded my expectations.', 7, true, NOW() - INTERVAL '2 days', NOW()),
    (6, 7, 2, 2, 6, 4, 'Smooth transaction. There was a minor shipping delay but the seller was proactive in communication.', 2, true, NOW() - INTERVAL '2 days', NOW()),
    -- Reviews for sarahchen (casual seller)
    (7, 8, 3, 3, 4, 4, 'Book was in good readable condition with some minor highlighting. Fair price for the quality.', 4, true, NOW() - INTERVAL '1 day', NOW()),
    (8, 9, 4, 3, 4, 5, 'Bought this as a gift and it was perfect. Great seller with quick responses!', 6, true, NOW() - INTERVAL '1 day', NOW()),
    (9, 10, 4, 3, 6, 4, 'Book condition was accurate. Would appreciate more protective packaging next time.', 1, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- 11. VOUCHERS (check actual columns)
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
SELECT setval(pg_get_serial_sequence('shipping_address', 'id'), COALESCE((SELECT MAX(id) FROM shipping_address), 1), true);
SELECT setval(pg_get_serial_sequence('orders', 'id'), COALESCE((SELECT MAX(id) FROM orders), 1), true);
SELECT setval(pg_get_serial_sequence('order_items', 'id'), COALESCE((SELECT MAX(id) FROM order_items), 1), true);
SELECT setval(pg_get_serial_sequence('review', 'id'), COALESCE((SELECT MAX(id) FROM review), 1), true);

-- ============================================
-- VERIFICATION
-- ============================================

SELECT 'Users: ' || COUNT(*) FROM "user";
SELECT 'Books: ' || COUNT(*) FROM book_meta;
SELECT 'Listings: ' || COUNT(*) FROM listing;
SELECT 'Orders: ' || COUNT(*) FROM orders;
SELECT 'Reviews: ' || COUNT(*) FROM review;
SELECT 'Authors: ' || COUNT(*) FROM author;

SELECT '✅ Demo seed v2 completed!' as status;
