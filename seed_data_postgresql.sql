-- ============================================
-- BOOKVERSE BACKEND - SEED DATA (PostgreSQL/Supabase)
-- Generated: 2026-01-05
-- ============================================

-- ============================================
-- 1. USERS & AUTHENTICATION
-- ============================================

-- Insert Users (password: "password123" - bcrypt hashed)
INSERT INTO "user" (id, username, email, password_hash, enabled, auth_provider, created_at, updated_at) VALUES
(1, 'admin', 'admin@bookverse.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 'LOCAL', NOW(), NOW()),
(2, 'tinvo', 'tinvo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 'LOCAL', NOW(), NOW()),
(3, 'nguyenvan', 'nguyen@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 'LOCAL', NOW(), NOW()),
(4, 'lethithu', 'lethu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 'LOCAL', NOW(), NOW()),
(5, 'phamhoang', 'phamhoang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, 'LOCAL', NOW(), NOW());

-- Insert Roles
INSERT INTO role (id, name, description) VALUES
(1, 'ADMIN', 'Administrator role'),
(2, 'USER', 'Regular user role'),
(3, 'SELLER', 'Seller role');

-- Assign Roles to Users
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1), -- admin is ADMIN
(1, 2), -- admin is also USER
(2, 2), -- tinvo is USER
(2, 3), -- tinvo is SELLER
(3, 2), -- nguyenvan is USER
(3, 3), -- nguyenvan is SELLER
(4, 2), -- lethithu is USER
(5, 2), -- phamhoang is USER
(5, 3); -- phamhoang is SELLER

-- Insert User Profiles
INSERT INTO user_profile (id, user_id, display_name, avatar_url, bio, phone, date_of_birth, gender, account_type, is_pro_seller, response_time, fulfillment_rate, rating_avg, rating_count, seller_since, created_at, updated_at) VALUES
(1, 1, 'Admin User', 'https://i.pravatar.cc/150?u=admin', 'System Administrator', '+84901234567', '1990-01-01', 'OTHER', 'BUSINESS', false, NULL, NULL, 5.0, 0, NULL, NOW(), NOW()),
(2, 2, 'Tin Vo', 'https://i.pravatar.cc/150?u=tinvo', 'Passionate book collector and seller', '+84912345678', '1995-05-15', 'MALE', 'BUSINESS', true, '2 hours', 98.5, 4.8, 45, '2024-01-01', NOW(), NOW()),
(3, 3, 'Nguyen Van A', 'https://i.pravatar.cc/150?u=nguyen', 'Love reading and sharing books', '+84923456789', '1992-08-20', 'MALE', 'PERSONAL', false, '5 hours', 95.0, 4.5, 12, '2024-06-15', NOW(), NOW()),
(4, 4, 'Le Thi Thu', 'https://i.pravatar.cc/150?u=lethu', 'Book enthusiast', '+84934567890', '1998-03-10', 'FEMALE', 'PERSONAL', false, NULL, NULL, 4.7, 8, NULL, NOW(), NOW()),
(5, 5, 'Pham Hoang', 'https://i.pravatar.cc/150?u=pham', 'Second-hand book specialist', '+84945678901', '1993-11-25', 'MALE', 'BUSINESS', true, '1 hour', 99.0, 4.9, 120, '2023-03-01', NOW(), NOW());

-- Insert Shipping Addresses
INSERT INTO shipping_address (id, user_id, recipient_name, phone, address_line_1, ward, district, city, country, postal_code, is_default, created_at, updated_at) VALUES
(1, 2, 'Tin Vo', '+84912345678', '123 Nguyen Hue', 'Ben Nghe', 'District 1', 'Ho Chi Minh City', 'Vietnam', '700000', true, NOW(), NOW()),
(2, 3, 'Nguyen Van A', '+84923456789', '456 Le Loi', 'Ben Thanh', 'District 1', 'Ho Chi Minh City', 'Vietnam', '700000', true, NOW(), NOW()),
(3, 4, 'Le Thi Thu', '+84934567890', '789 Tran Hung Dao', 'Cau Ong Lanh', 'District 1', 'Ho Chi Minh City', 'Vietnam', '700000', true, NOW(), NOW()),
(4, 5, 'Pham Hoang', '+84945678901', '321 Hai Ba Trung', 'Da Kao', 'District 1', 'Ho Chi Minh City', 'Vietnam', '700000', true, NOW(), NOW());

-- ============================================
-- 2. AUTHORS
-- ============================================

INSERT INTO author (id, openlibrary_id, name, personal_name, avatar, bio, position, books_count, main_genre, nationality, dob, website, created_at, updated_at) VALUES
(1, 'OL23919A', 'Joshua Bloch', 'Joshua J. Bloch', 'https://covers.openlibrary.org/a/olid/OL23919A-M.jpg', 'Joshua Bloch is a software engineer and author, known for his work on Java.', 'Software Engineer & Author', 15, 'Technology', 'American', '1961-08-28', 'https://www.joshbloch.com', NOW(), NOW()),
(2, 'OL34221A', 'Robert C. Martin', 'Robert Cecil Martin', 'https://covers.openlibrary.org/a/olid/OL34221A-M.jpg', 'Robert C. Martin (Uncle Bob) is a software engineer and author, known for writing Clean Code.', 'Software Consultant', 20, 'Technology', 'American', '1952-12-05', 'https://cleancoder.com', NOW(), NOW()),
(3, 'OL2622837A', 'Yuval Noah Harari', 'Yuval Noah Harari', 'https://covers.openlibrary.org/a/olid/OL2622837A-M.jpg', 'Israeli historian and professor, author of Sapiens and Homo Deus.', 'Historian & Professor', 8, 'History', 'Israeli', '1976-02-24', 'https://www.ynharari.com', NOW(), NOW()),
(4, 'OL2638051A', 'J.K. Rowling', 'Joanne Kathleen Rowling', 'https://covers.openlibrary.org/a/olid/OL2638051A-M.jpg', 'British author, best known for the Harry Potter series.', 'Novelist', 25, 'Fantasy', 'British', '1965-07-31', 'https://www.jkrowling.com', NOW(), NOW()),
(5, 'OL26320A', 'George Orwell', 'Eric Arthur Blair', 'https://covers.openlibrary.org/a/olid/OL26320A-M.jpg', 'English novelist and essayist, journalist and critic.', 'Novelist & Journalist', 30, 'Fiction', 'British', '1903-06-25', NULL, NOW(), NOW()),
(6, 'OL1432069A', 'Paulo Coelho', 'Paulo Coelho de Souza', 'https://covers.openlibrary.org/a/olid/OL1432069A-M.jpg', 'Brazilian lyricist and novelist, best known for The Alchemist.', 'Novelist', 35, 'Fiction', 'Brazilian', '1947-08-24', 'https://paulocoelhoblog.com', NOW(), NOW()),
(7, 'OL28125A', 'F. Scott Fitzgerald', 'Francis Scott Key Fitzgerald', 'https://covers.openlibrary.org/a/olid/OL28125A-M.jpg', 'American novelist and short story writer, known for The Great Gatsby.', 'Novelist', 40, 'Fiction', 'American', '1896-09-24', NULL, NOW(), NOW()),
(8, 'OL34184A', 'Harper Lee', 'Nelle Harper Lee', 'https://covers.openlibrary.org/a/olid/OL34184A-M.jpg', 'American novelist, known for To Kill a Mockingbird.', 'Novelist', 12, 'Fiction', 'American', '1926-04-28', NULL, NOW(), NOW());

-- ============================================
-- 3. CATEGORIES
-- ============================================

INSERT INTO category (id, name, slug, description, parent_id, created_at, updated_at) VALUES
(1, 'Technology', 'technology', 'Books about technology and programming', NULL, NOW(), NOW()),
(2, 'Fiction', 'fiction', 'Fictional stories and novels', NULL, NOW(), NOW()),
(3, 'Non-Fiction', 'non-fiction', 'Non-fictional books', NULL, NOW(), NOW()),
(4, 'Science', 'science', 'Scientific books and research', NULL, NOW(), NOW()),
(5, 'History', 'history', 'Historical books and biographies', NULL, NOW(), NOW()),
(6, 'Fantasy', 'fantasy', 'Fantasy novels and stories', 2, NOW(), NOW()),
(7, 'Programming', 'programming', 'Programming and software development', 1, NOW(), NOW()),
(8, 'Self-Help', 'self-help', 'Self-improvement and motivation', 3, NOW(), NOW()),
(9, 'Biography', 'biography', 'Biographies and memoirs', 3, NOW(), NOW()),
(10, 'Classic', 'classic', 'Classic literature', 2, NOW(), NOW());

-- ============================================
-- 4. BOOK META (Book Information)
-- ============================================

INSERT INTO book_meta (id, title, isbn, isbn13, publisher, publication_date, edition, language, pages, description, cover_image_url, average_rating, total_reviews, created_at, updated_at) VALUES
(1, 'Effective Java', '9780134685991', '978-0134685991', 'Addison-Wesley', '2018-01-06', '3rd Edition', 'en', 416, 'The definitive guide to Java programming language best practices. Updated for Java 7, 8, and 9.', 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg', 4.7, 2500, NOW(), NOW()),
(2, 'Clean Code', '9780132350884', '978-0132350884', 'Prentice Hall', '2008-08-01', '1st Edition', 'en', 464, 'A Handbook of Agile Software Craftsmanship. Learn to write clean, maintainable code.', 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg', 4.6, 3200, NOW(), NOW()),
(3, 'Sapiens: A Brief History of Humankind', '9780062316110', '978-0062316110', 'Harper', '2015-02-10', '1st Edition', 'en', 443, 'A brief history of humankind from the Stone Age to the modern age.', 'https://covers.openlibrary.org/b/isbn/9780062316110-L.jpg', 4.5, 15000, NOW(), NOW()),
(4, 'Harry Potter and the Philosopher''s Stone', '9780439708180', '978-0439708180', 'Scholastic', '1998-09-01', '1st Edition', 'en', 309, 'The first book in the Harry Potter series. Follow Harry as he discovers he is a wizard.', 'https://covers.openlibrary.org/b/isbn/9780439708180-L.jpg', 4.8, 50000, NOW(), NOW()),
(5, '1984', '9780451524935', '978-0451524935', 'Signet Classic', '1961-01-01', 'Mass Market', 'en', 328, 'A dystopian social science fiction novel by George Orwell.', 'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg', 4.7, 40000, NOW(), NOW()),
(6, 'The Alchemist', '9780062315007', '978-0062315007', 'HarperOne', '2014-04-15', 'Anniversary Edition', 'en', 208, 'A philosophical novel about following your dreams.', 'https://covers.openlibrary.org/b/isbn/9780062315007-L.jpg', 4.3, 25000, NOW(), NOW()),
(7, 'The Great Gatsby', '9780743273565', '978-0743273565', 'Scribner', '2004-09-30', 'Reprint', 'en', 180, 'The story of the mysteriously wealthy Jay Gatsby and his love for Daisy Buchanan.', 'https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg', 4.4, 35000, NOW(), NOW()),
(8, 'To Kill a Mockingbird', '9780061120084', '978-0061120084', 'Harper Perennial', '2006-05-23', '50th Anniversary', 'en', 324, 'A gripping tale of racial injustice and childhood innocence.', 'https://covers.openlibrary.org/b/isbn/9780061120084-L.jpg', 4.6, 45000, NOW(), NOW());

-- ============================================
-- 5. BOOK AUTHORS (Many-to-Many Relationship)
-- ============================================

INSERT INTO book_meta_authors (book_meta_id, authors_id) VALUES
(1, 1), -- Effective Java - Joshua Bloch
(2, 2), -- Clean Code - Robert C. Martin
(3, 3), -- Sapiens - Yuval Noah Harari
(4, 4), -- Harry Potter - J.K. Rowling
(5, 5), -- 1984 - George Orwell
(6, 6), -- The Alchemist - Paulo Coelho
(7, 7), -- The Great Gatsby - F. Scott Fitzgerald
(8, 8); -- To Kill a Mockingbird - Harper Lee

-- ============================================
-- 6. BOOK CATEGORIES (Many-to-Many Relationship)
-- ============================================

INSERT INTO book_meta_categories (book_meta_id, categories_id) VALUES
(1, 1), -- Effective Java - Technology
(1, 7), -- Effective Java - Programming
(2, 1), -- Clean Code - Technology
(2, 7), -- Clean Code - Programming
(3, 3), -- Sapiens - Non-Fiction
(3, 5), -- Sapiens - History
(4, 2), -- Harry Potter - Fiction
(4, 6), -- Harry Potter - Fantasy
(5, 2), -- 1984 - Fiction
(5, 10), -- 1984 - Classic
(6, 2), -- The Alchemist - Fiction
(6, 8), -- The Alchemist - Self-Help
(7, 2), -- The Great Gatsby - Fiction
(7, 10), -- The Great Gatsby - Classic
(8, 2), -- To Kill a Mockingbird - Fiction
(8, 10); -- To Kill a Mockingbird - Classic

-- ============================================
-- 7. LISTINGS
-- ============================================

INSERT INTO listing (id, book_id, seller_id, title_override, price, original_price, currency, condition, quantity, description, location, status, visibility, platform_fee_percent, suggested_price_low, suggested_price_high, views, likes, sold_count, free_shipping, estimated_shipping_days, ships_from, created_at, updated_at) VALUES
(1, 1, 2, NULL, 35.00, 45.00, 'USD', 'LIKE_NEW', 3, 'Excellent condition, barely used. Perfect for Java developers!', 'Ho Chi Minh City, Vietnam', 'ACTIVE', true, 10.00, 30.00, 40.00, 125, 15, 2, true, '2-3 days', 'Ho Chi Minh City', NOW(), NOW()),
(2, 1, 3, 'Effective Java - Great Condition', 30.00, 45.00, 'USD', 'GOOD', 2, 'Good condition with minor wear. All pages intact.', 'Hanoi, Vietnam', 'ACTIVE', true, 10.00, 25.00, 40.00, 80, 8, 1, false, '3-5 days', 'Hanoi', NOW(), NOW()),
(3, 2, 2, NULL, 28.00, 40.00, 'USD', 'LIKE_NEW', 5, 'Almost new condition. A must-have for software engineers!', 'Ho Chi Minh City, Vietnam', 'ACTIVE', true, 10.00, 25.00, 35.00, 200, 32, 5, true, '2-3 days', 'Ho Chi Minh City', NOW(), NOW()),
(4, 2, 5, 'Clean Code - Professional Edition', 32.00, 40.00, 'USD', 'NEW', 10, 'Brand new, sealed copy. Perfect gift for developers!', 'Da Nang, Vietnam', 'ACTIVE', true, 10.00, 28.00, 38.00, 150, 20, 3, true, '1-2 days', 'Da Nang', NOW(), NOW()),
(5, 3, 3, NULL, 18.00, 25.00, 'USD', 'GOOD', 4, 'Good reading condition. Fascinating historical perspective.', 'Hanoi, Vietnam', 'ACTIVE', true, 10.00, 15.00, 22.00, 90, 12, 2, false, '3-5 days', 'Hanoi', NOW(), NOW()),
(6, 4, 5, NULL, 15.00, 20.00, 'USD', 'ACCEPTABLE', 2, 'Well-loved copy, some wear on cover. Great story!', 'Da Nang, Vietnam', 'ACTIVE', true, 10.00, 10.00, 18.00, 300, 45, 8, true, '2-3 days', 'Da Nang', NOW(), NOW()),
(7, 5, 2, NULL, 12.00, 15.00, 'USD', 'GOOD', 3, 'Classic dystopian novel in good condition.', 'Ho Chi Minh City, Vietnam', 'ACTIVE', true, 10.00, 10.00, 14.00, 180, 28, 4, true, '2-3 days', 'Ho Chi Minh City', NOW(), NOW()),
(8, 6, 3, 'The Alchemist - Inspirational', 14.00, 18.00, 'USD', 'LIKE_NEW', 6, 'Beautiful edition, like new. Inspirational read!', 'Hanoi, Vietnam', 'ACTIVE', true, 10.00, 12.00, 16.00, 110, 18, 3, false, '3-5 days', 'Hanoi', NOW(), NOW()),
(9, 7, 5, NULL, 13.00, 16.00, 'USD', 'GOOD', 4, 'The Great American Novel in good condition.', 'Da Nang, Vietnam', 'ACTIVE', true, 10.00, 11.00, 15.00, 95, 14, 2, true, '2-3 days', 'Da Nang', NOW(), NOW()),
(10, 8, 2, NULL, 16.00, 20.00, 'USD', 'LIKE_NEW', 3, 'Powerful story, excellent condition.', 'Ho Chi Minh City, Vietnam', 'ACTIVE', true, 10.00, 14.00, 18.00, 140, 22, 3, true, '2-3 days', 'Ho Chi Minh City', NOW(), NOW());

-- ============================================
-- 8. LISTING PHOTOS
-- ============================================

INSERT INTO listing_photo (id, listing_id, url, position, created_at) VALUES
-- Listing 1 photos
(1, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800', 0, NOW()),
(2, 1, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=800', 1, NOW()),
-- Listing 2 photos
(3, 2, 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=800', 0, NOW()),
-- Listing 3 photos
(4, 3, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800', 0, NOW()),
(5, 3, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800', 1, NOW()),
-- Listing 4 photos
(6, 4, 'https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=800', 0, NOW()),
-- Listing 5 photos
(7, 5, 'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=800', 0, NOW()),
-- Listing 6 photos
(8, 6, 'https://images.unsplash.com/photo-1551847812-f815db5b52e3?w=800', 0, NOW()),
-- Listing 7 photos
(9, 7, 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=800', 0, NOW()),
-- Listing 8 photos
(10, 8, 'https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=800', 0, NOW()),
-- Listing 9 photos
(11, 9, 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=800', 0, NOW()),
-- Listing 10 photos
(12, 10, 'https://images.unsplash.com/photo-1485988412941-77a35537dae4?w=800', 0, NOW());

-- ============================================
-- 9. REVIEWS & RATINGS
-- ============================================

INSERT INTO review (id, book_meta_id, user_id, rating, title, comment, is_verified_purchase, helpful_count, created_at, updated_at) VALUES
(1, 1, 3, 5, 'Excellent Java Resource', 'This book is a must-read for any serious Java developer. Clear explanations and best practices.', true, 25, NOW(), NOW()),
(2, 1, 4, 4, 'Very Helpful', 'Great book, learned a lot about Java patterns and practices.', false, 12, NOW(), NOW()),
(3, 2, 3, 5, 'Clean Code Changed My Career', 'This book transformed how I write code. Highly recommended!', true, 45, NOW(), NOW()),
(4, 2, 5, 5, 'Must-Have for Developers', 'Every developer should read this book. Game changer!', true, 38, NOW(), NOW()),
(5, 3, 2, 5, 'Mind-Blowing', 'Changed my perspective on human history. Absolutely brilliant!', true, 120, NOW(), NOW()),
(6, 4, 3, 5, 'Magical', 'The book that started it all. Timeless classic!', true, 200, NOW(), NOW()),
(7, 5, 4, 5, 'Prophetic and Disturbing', 'Still relevant today. Orwell was a visionary.', false, 85, NOW(), NOW()),
(8, 6, 2, 4, 'Inspiring Journey', 'Beautiful story about following your dreams.', true, 65, NOW(), NOW());

-- ============================================
-- 10. WISHLISTS
-- ============================================

INSERT INTO wishlist (id, user_id, listing_id, price_at_addition, added_at) VALUES
(1, 3, 1, 35.00, NOW()),
(2, 3, 4, 32.00, NOW()),
(3, 4, 3, 28.00, NOW()),
(4, 4, 6, 15.00, NOW()),
(5, 4, 10, 16.00, NOW()),
(6, 5, 7, 12.00, NOW());

-- ============================================
-- 11. LIKES
-- ============================================

INSERT INTO likes (id, user_id, listing_id, created_at) VALUES
(1, 3, 1, NOW()),
(2, 3, 3, NOW()),
(3, 4, 1, NOW()),
(4, 4, 3, NOW()),
(5, 4, 6, NOW()),
(6, 5, 3, NOW()),
(7, 5, 4, NOW());

-- ============================================
-- 12. CARTS & CART ITEMS
-- ============================================

INSERT INTO cart (id, user_id, created_at, updated_at) VALUES
(1, 3, NOW(), NOW()),
(2, 4, NOW(), NOW()),
(3, 5, NOW(), NOW());

INSERT INTO cart_item (id, cart_id, listing_id, quantity, added_at) VALUES
(1, 1, 1, 1, NOW()),
(2, 1, 3, 2, NOW()),
(3, 2, 6, 1, NOW()),
(4, 3, 4, 1, NOW());

-- ============================================
-- 13. ORDERS
-- ============================================

INSERT INTO "order" (id, order_number, user_id, status, subtotal, shipping_fee, tax, discount, total, payment_method, payment_status, shipping_address_id, billing_address_id, created_at, updated_at) VALUES
(1, 'ORD-2026-0001', 3, 'DELIVERED', 70.00, 5.00, 7.50, 0.00, 82.50, 'CREDIT_CARD', 'PAID', 2, 2, NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days'),
(2, 'ORD-2026-0002', 4, 'SHIPPED', 43.00, 5.00, 4.80, 0.00, 52.80, 'PAYPAL', 'PAID', 3, 3, NOW() - INTERVAL '5 days', NOW() - INTERVAL '3 days'),
(3, 'ORD-2026-0003', 5, 'PROCESSING', 32.00, 0.00, 3.20, 0.00, 35.20, 'CREDIT_CARD', 'PAID', 4, 4, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

-- ============================================
-- 14. ORDER ITEMS
-- ============================================

INSERT INTO order_item (id, order_id, listing_id, seller_id, book_title, author_name, image_url, quantity, unit_price, subtotal, status, created_at) VALUES
(1, 1, 1, 2, 'Effective Java', 'Joshua Bloch', 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800', 1, 35.00, 35.00, 'DELIVERED', NOW() - INTERVAL '30 days'),
(2, 1, 3, 2, 'Clean Code', 'Robert C. Martin', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800', 1, 28.00, 28.00, 'DELIVERED', NOW() - INTERVAL '30 days'),
(3, 2, 6, 5, 'Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 'https://images.unsplash.com/photo-1551847812-f815db5b52e3?w=800', 1, 15.00, 15.00, 'SHIPPED', NOW() - INTERVAL '5 days'),
(4, 2, 10, 2, 'To Kill a Mockingbird', 'Harper Lee', 'https://images.unsplash.com/photo-1485988412941-77a35537dae4?w=800', 1, 16.00, 16.00, 'SHIPPED', NOW() - INTERVAL '5 days'),
(5, 3, 4, 5, 'Clean Code - Professional Edition', 'Robert C. Martin', 'https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=800', 1, 32.00, 32.00, 'PROCESSING', NOW() - INTERVAL '1 day');

-- ============================================
-- 15. ORDER TIMELINE
-- ============================================

INSERT INTO order_timeline (id, order_id, status, description, created_at) VALUES
-- Order 1 timeline (Completed)
(1, 1, 'PENDING', 'Order placed', NOW() - INTERVAL '30 days'),
(2, 1, 'CONFIRMED', 'Order confirmed by seller', NOW() - INTERVAL '29 days'),
(3, 1, 'PROCESSING', 'Order is being prepared', NOW() - INTERVAL '28 days'),
(4, 1, 'SHIPPED', 'Order has been shipped', NOW() - INTERVAL '27 days'),
(5, 1, 'DELIVERED', 'Order delivered successfully', NOW() - INTERVAL '25 days'),
-- Order 2 timeline (In transit)
(6, 2, 'PENDING', 'Order placed', NOW() - INTERVAL '5 days'),
(7, 2, 'CONFIRMED', 'Order confirmed by seller', NOW() - INTERVAL '4 days'),
(8, 2, 'SHIPPED', 'Order has been shipped', NOW() - INTERVAL '3 days'),
-- Order 3 timeline (Processing)
(9, 3, 'PENDING', 'Order placed', NOW() - INTERVAL '1 day'),
(10, 3, 'PROCESSING', 'Order is being prepared', NOW());

-- ============================================
-- 16. VOUCHERS & PROMOTIONS
-- ============================================

INSERT INTO voucher (id, code, description, discount_type, discount_value, min_order_value, max_discount, usage_limit, used_count, is_active, valid_from, valid_to, created_at, updated_at) VALUES
(1, 'WELCOME10', 'Welcome discount for new users', 'PERCENT', 10.00, 20.00, 10.00, 100, 15, true, NOW(), NOW() + INTERVAL '30 days', NOW(), NOW()),
(2, 'NEWYEAR2026', 'New Year 2026 special discount', 'PERCENT', 15.00, 50.00, 20.00, 200, 45, true, NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days', NOW(), NOW()),
(3, 'FREESHIP', 'Free shipping voucher', 'FIXED', 5.00, 30.00, 5.00, 50, 8, true, NOW(), NOW() + INTERVAL '15 days', NOW(), NOW());

-- ============================================
-- 17. NOTIFICATIONS
-- ============================================

INSERT INTO notification (id, user_id, type, title, message, is_read, reference_id, created_at) VALUES
(1, 3, 'ORDER', 'Order Delivered', 'Your order #ORD-2026-0001 has been delivered successfully.', true, 1, NOW() - INTERVAL '25 days'),
(2, 3, 'PROMOTION', 'New Year Sale!', 'Get 15% off on all books with code NEWYEAR2026', true, NULL, NOW() - INTERVAL '5 days'),
(3, 4, 'ORDER', 'Order Shipped', 'Your order #ORD-2026-0002 has been shipped.', false, 2, NOW() - INTERVAL '3 days'),
(4, 5, 'ORDER', 'Order Confirmed', 'Your order #ORD-2026-0003 has been confirmed.', false, 3, NOW() - INTERVAL '1 day'),
(5, 2, 'LISTING', 'New Review', 'Someone left a 5-star review on your listing.', false, 1, NOW());

-- ============================================
-- FINAL STATISTICS UPDATE
-- ============================================

-- Update book average ratings
UPDATE book_meta bm
SET average_rating = (
    SELECT AVG(r.rating)
    FROM review r
    WHERE r.book_meta_id = bm.id
),
total_reviews = (
    SELECT COUNT(*)
    FROM review r
    WHERE r.book_meta_id = bm.id
);

-- Update user profile ratings
UPDATE user_profile up
SET rating_count = (
    SELECT COUNT(*)
    FROM review r
    INNER JOIN order_item oi ON r.book_meta_id = oi.listing_id
    INNER JOIN listing l ON oi.listing_id = l.id
    WHERE l.seller_id = up.user_id
);

-- ============================================
-- RESET SEQUENCES
-- ============================================

-- Reset auto increment sequences
SELECT setval(pg_get_serial_sequence('"user"', 'id'), COALESCE((SELECT MAX(id) FROM "user"), 1), true);
SELECT setval(pg_get_serial_sequence('author', 'id'), COALESCE((SELECT MAX(id) FROM author), 1), true);
SELECT setval(pg_get_serial_sequence('book_meta', 'id'), COALESCE((SELECT MAX(id) FROM book_meta), 1), true);
SELECT setval(pg_get_serial_sequence('listing', 'id'), COALESCE((SELECT MAX(id) FROM listing), 1), true);
SELECT setval(pg_get_serial_sequence('"order"', 'id'), COALESCE((SELECT MAX(id) FROM "order"), 1), true);

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

SELECT COUNT(*) as total_users FROM "user";
SELECT COUNT(*) as total_books FROM book_meta;
SELECT COUNT(*) as total_listings FROM listing;
SELECT COUNT(*) as total_orders FROM "order";
SELECT COUNT(*) as total_reviews FROM review;

-- ============================================
-- END OF SEED DATA
-- ============================================

SELECT 'Seed data imported successfully!' as status;
