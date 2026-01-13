-- ============================================================
-- RICH CATEGORY HIERARCHY MIGRATION
-- 50+ categories with emojis, colors, hierarchy, and featured flags
-- ============================================================

-- Add new columns to category table (if not exists)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'category' AND column_name = 'parent_id') THEN
        ALTER TABLE category ADD COLUMN parent_id BIGINT REFERENCES category(id) ON DELETE SET NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'category' AND column_name = 'emoji') THEN
        ALTER TABLE category ADD COLUMN emoji VARCHAR(10);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'category' AND column_name = 'sort_order') THEN
        ALTER TABLE category ADD COLUMN sort_order INTEGER DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'category' AND column_name = 'color') THEN
        ALTER TABLE category ADD COLUMN color VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'category' AND column_name = 'featured') THEN
        ALTER TABLE category ADD COLUMN featured BOOLEAN DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'category' AND column_name = 'book_count') THEN
        ALTER TABLE category ADD COLUMN book_count INTEGER DEFAULT 0;
    END IF;
END $$;

-- Create index for hierarchical queries
CREATE INDEX IF NOT EXISTS idx_category_parent_id ON category(parent_id);
CREATE INDEX IF NOT EXISTS idx_category_featured ON category(featured) WHERE featured = TRUE;
CREATE INDEX IF NOT EXISTS idx_category_sort_order ON category(sort_order);

-- ============================================================
-- CLEAR EXISTING CATEGORIES (for fresh hierarchy)
-- Comment out this section if you want to preserve existing data
-- ============================================================
-- DELETE FROM book_meta_categories;
-- DELETE FROM category;

-- ============================================================
-- ROOT CATEGORIES (Level 0)
-- ============================================================
INSERT INTO category (name, slug, description, emoji, color, sort_order, featured, book_count) VALUES
-- FICTION UMBRELLA
('Fiction', 'fiction', 'Imaginative narratives that transport you to other worlds', '📚', '#8B5CF6', 1, TRUE, 0),
-- NON-FICTION UMBRELLA
('Non-Fiction', 'non-fiction', 'Real stories, knowledge, and insights from the world around us', '📖', '#3B82F6', 2, TRUE, 0),
-- CHILDREN & YA
('Children & Young Adult', 'children-young-adult', 'Stories for young readers of all ages', '🧸', '#EC4899', 3, TRUE, 0),
-- ACADEMIC & PROFESSIONAL
('Academic & Professional', 'academic-professional', 'Educational and career-focused resources', '🎓', '#10B981', 4, TRUE, 0),
-- COMICS & GRAPHIC
('Comics & Graphic Novels', 'comics-graphic-novels', 'Visual storytelling in all its forms', '💥', '#F59E0B', 5, TRUE, 0),
-- LIFESTYLE
('Lifestyle & Hobbies', 'lifestyle-hobbies', 'Enhance your daily life and passions', '🌟', '#06B6D4', 6, TRUE, 0)
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    featured = EXCLUDED.featured;

-- ============================================================
-- FICTION SUBCATEGORIES (Level 1)
-- ============================================================
INSERT INTO category (name, slug, description, emoji, color, sort_order, featured, parent_id) VALUES
-- Under Fiction
('Literary Fiction', 'literary-fiction', 'Character-driven stories with depth and artistry', '✨', '#A855F7', 1, FALSE, (SELECT id FROM category WHERE slug = 'fiction')),
('Science Fiction', 'science-fiction', 'Explore futures, technology, and beyond', '🚀', '#6366F1', 2, TRUE, (SELECT id FROM category WHERE slug = 'fiction')),
('Fantasy', 'fantasy', 'Magic, mythical creatures, and epic adventures', '🐉', '#8B5CF6', 3, TRUE, (SELECT id FROM category WHERE slug = 'fiction')),
('Mystery & Thriller', 'mystery-thriller', 'Suspense, crime, and page-turning plots', '🔍', '#EF4444', 4, TRUE, (SELECT id FROM category WHERE slug = 'fiction')),
('Romance', 'romance', 'Love stories that warm the heart', '💕', '#F472B6', 5, TRUE, (SELECT id FROM category WHERE slug = 'fiction')),
('Horror', 'horror', 'Tales of terror and the supernatural', '👻', '#1F2937', 6, FALSE, (SELECT id FROM category WHERE slug = 'fiction')),
('Historical Fiction', 'historical-fiction', 'Stories set in fascinating historical periods', '⚔️', '#B45309', 7, FALSE, (SELECT id FROM category WHERE slug = 'fiction')),
('Contemporary Fiction', 'contemporary-fiction', 'Modern stories reflecting today''s world', '🌆', '#64748B', 8, FALSE, (SELECT id FROM category WHERE slug = 'fiction')),
('Adventure', 'adventure', 'Action-packed journeys and explorations', '🏔️', '#059669', 9, FALSE, (SELECT id FROM category WHERE slug = 'fiction')),
('Classics', 'classics', 'Timeless literature that shaped our culture', '📜', '#78350F', 10, FALSE, (SELECT id FROM category WHERE slug = 'fiction'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- ============================================================
-- NON-FICTION SUBCATEGORIES (Level 1)
-- ============================================================
INSERT INTO category (name, slug, description, emoji, color, sort_order, featured, parent_id) VALUES
-- Under Non-Fiction
('Biography & Memoir', 'biography-memoir', 'True stories of remarkable lives', '👤', '#4F46E5', 1, TRUE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Self-Help & Personal Development', 'self-help', 'Transform your life and mindset', '🧠', '#22C55E', 2, TRUE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Business & Economics', 'business-economics', 'Master money, markets, and management', '💼', '#0EA5E9', 3, TRUE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('History', 'history', 'Explore humanity''s past', '🏛️', '#B45309', 4, TRUE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Science & Nature', 'science-nature', 'Understand our world and universe', '🔬', '#14B8A6', 5, TRUE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Health & Wellness', 'health-wellness', 'Nurture body and mind', '🏃', '#84CC16', 6, FALSE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Philosophy', 'philosophy', 'Big questions and deep thinking', '🤔', '#A78BFA', 7, FALSE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Psychology', 'psychology', 'Understanding the human mind', '🧩', '#F97316', 8, FALSE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('True Crime', 'true-crime', 'Real criminal cases and investigations', '🔎', '#DC2626', 9, FALSE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Travel', 'travel', 'Journeys and destinations around the globe', '✈️', '#0D9488', 10, FALSE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Religion & Spirituality', 'religion-spirituality', 'Faith, belief, and inner peace', '🕊️', '#7C3AED', 11, FALSE, (SELECT id FROM category WHERE slug = 'non-fiction')),
('Politics & Current Affairs', 'politics-current-affairs', 'Understanding today''s world', '🗳️', '#64748B', 12, FALSE, (SELECT id FROM category WHERE slug = 'non-fiction'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- ============================================================
-- CHILDREN & YA SUBCATEGORIES (Level 1)
-- ============================================================
INSERT INTO category (name, slug, description, emoji, color, sort_order, featured, parent_id) VALUES
('Picture Books', 'picture-books', 'Illustrated stories for the youngest readers', '🎨', '#F472B6', 1, FALSE, (SELECT id FROM category WHERE slug = 'children-young-adult')),
('Middle Grade', 'middle-grade', 'Adventures for tweens (ages 8-12)', '📕', '#8B5CF6', 2, FALSE, (SELECT id FROM category WHERE slug = 'children-young-adult')),
('Young Adult Fiction', 'young-adult-fiction', 'Coming-of-age stories for teens', '🎭', '#EC4899', 3, TRUE, (SELECT id FROM category WHERE slug = 'children-young-adult')),
('Young Adult Fantasy', 'young-adult-fantasy', 'Magical worlds for teen readers', '🦋', '#A855F7', 4, TRUE, (SELECT id FROM category WHERE slug = 'children-young-adult')),
('Children''s Classics', 'childrens-classics', 'Timeless tales for young minds', '🌈', '#F59E0B', 5, FALSE, (SELECT id FROM category WHERE slug = 'children-young-adult'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- ============================================================
-- ACADEMIC & PROFESSIONAL SUBCATEGORIES (Level 1)
-- ============================================================
INSERT INTO category (name, slug, description, emoji, color, sort_order, featured, parent_id) VALUES
('Technology & Programming', 'technology-programming', 'Code, software, and digital innovation', '💻', '#3B82F6', 1, TRUE, (SELECT id FROM category WHERE slug = 'academic-professional')),
('Mathematics', 'mathematics', 'Numbers, equations, and logic', '📐', '#6366F1', 2, FALSE, (SELECT id FROM category WHERE slug = 'academic-professional')),
('Engineering', 'engineering', 'Building and designing our world', '⚙️', '#64748B', 3, FALSE, (SELECT id FROM category WHERE slug = 'academic-professional')),
('Medical & Healthcare', 'medical-healthcare', 'Medicine and health sciences', '🩺', '#EF4444', 4, FALSE, (SELECT id FROM category WHERE slug = 'academic-professional')),
('Law', 'law', 'Legal principles and practice', '⚖️', '#78350F', 5, FALSE, (SELECT id FROM category WHERE slug = 'academic-professional')),
('Education & Teaching', 'education-teaching', 'Learning and pedagogy', '📝', '#22C55E', 6, FALSE, (SELECT id FROM category WHERE slug = 'academic-professional')),
('Reference', 'reference', 'Dictionaries, encyclopedias, and guides', '📚', '#71717A', 7, FALSE, (SELECT id FROM category WHERE slug = 'academic-professional'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- ============================================================
-- COMICS & GRAPHIC NOVELS SUBCATEGORIES (Level 1)
-- ============================================================
INSERT INTO category (name, slug, description, emoji, color, sort_order, featured, parent_id) VALUES
('Manga', 'manga', 'Japanese comics and graphic storytelling', '🎌', '#EF4444', 1, TRUE, (SELECT id FROM category WHERE slug = 'comics-graphic-novels')),
('Superhero Comics', 'superhero-comics', 'Marvel, DC, and beyond', '🦸', '#3B82F6', 2, TRUE, (SELECT id FROM category WHERE slug = 'comics-graphic-novels')),
('Graphic Memoirs', 'graphic-memoirs', 'True stories in visual form', '📓', '#8B5CF6', 3, FALSE, (SELECT id FROM category WHERE slug = 'comics-graphic-novels')),
('Webcomics', 'webcomics', 'Digital-first visual stories', '🌐', '#06B6D4', 4, FALSE, (SELECT id FROM category WHERE slug = 'comics-graphic-novels')),
('Art Books', 'art-books', 'Visual art collections and illustrations', '🖼️', '#F59E0B', 5, FALSE, (SELECT id FROM category WHERE slug = 'comics-graphic-novels'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- ============================================================
-- LIFESTYLE & HOBBIES SUBCATEGORIES (Level 1)
-- ============================================================
INSERT INTO category (name, slug, description, emoji, color, sort_order, featured, parent_id) VALUES
('Cooking & Food', 'cooking-food', 'Recipes, cuisine, and culinary arts', '🍳', '#F59E0B', 1, TRUE, (SELECT id FROM category WHERE slug = 'lifestyle-hobbies')),
('Crafts & DIY', 'crafts-diy', 'Make, create, and build', '🛠️', '#10B981', 2, FALSE, (SELECT id FROM category WHERE slug = 'lifestyle-hobbies')),
('Gardening', 'gardening', 'Grow and nurture your green space', '🌱', '#22C55E', 3, FALSE, (SELECT id FROM category WHERE slug = 'lifestyle-hobbies')),
('Sports & Outdoors', 'sports-outdoors', 'Active living and athletic pursuits', '⚽', '#3B82F6', 4, FALSE, (SELECT id FROM category WHERE slug = 'lifestyle-hobbies')),
('Home & Interior Design', 'home-interior-design', 'Create beautiful living spaces', '🏠', '#A78BFA', 5, FALSE, (SELECT id FROM category WHERE slug = 'lifestyle-hobbies')),
('Photography', 'photography', 'Capture moments and master the lens', '📷', '#64748B', 6, FALSE, (SELECT id FROM category WHERE slug = 'lifestyle-hobbies')),
('Music', 'music', 'Learn instruments and music theory', '🎵', '#EC4899', 7, FALSE, (SELECT id FROM category WHERE slug = 'lifestyle-hobbies')),
('Pet Care', 'pet-care', 'Love and care for animal companions', '🐾', '#F472B6', 8, FALSE, (SELECT id FROM category WHERE slug = 'lifestyle-hobbies'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- ============================================================
-- LEVEL 2 SUBCATEGORIES (Deeper hierarchy for popular categories)
-- ============================================================

-- Science Fiction sub-subcategories
INSERT INTO category (name, slug, description, emoji, color, sort_order, parent_id) VALUES
('Space Opera', 'space-opera', 'Epic galactic adventures', '🌌', '#6366F1', 1, (SELECT id FROM category WHERE slug = 'science-fiction')),
('Cyberpunk', 'cyberpunk', 'High-tech, low-life futures', '🤖', '#22D3EE', 2, (SELECT id FROM category WHERE slug = 'science-fiction')),
('Hard Science Fiction', 'hard-science-fiction', 'Scientifically rigorous speculation', '🔭', '#3B82F6', 3, (SELECT id FROM category WHERE slug = 'science-fiction')),
('Dystopian', 'dystopian', 'Dark futures and oppressive societies', '🏚️', '#374151', 4, (SELECT id FROM category WHERE slug = 'science-fiction'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- Fantasy sub-subcategories
INSERT INTO category (name, slug, description, emoji, color, sort_order, parent_id) VALUES
('Epic Fantasy', 'epic-fantasy', 'Grand quests in vast magical worlds', '⚔️', '#8B5CF6', 1, (SELECT id FROM category WHERE slug = 'fantasy')),
('Urban Fantasy', 'urban-fantasy', 'Magic in modern cities', '🌃', '#A855F7', 2, (SELECT id FROM category WHERE slug = 'fantasy')),
('Dark Fantasy', 'dark-fantasy', 'Grim tales of magic and horror', '🌑', '#1F2937', 3, (SELECT id FROM category WHERE slug = 'fantasy')),
('Fairy Tales & Folklore', 'fairy-tales-folklore', 'Myths, legends, and retellings', '🧚', '#F472B6', 4, (SELECT id FROM category WHERE slug = 'fantasy'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- Romance sub-subcategories
INSERT INTO category (name, slug, description, emoji, color, sort_order, parent_id) VALUES
('Contemporary Romance', 'contemporary-romance', 'Modern love stories', '💑', '#F472B6', 1, (SELECT id FROM category WHERE slug = 'romance')),
('Historical Romance', 'historical-romance', 'Love across the ages', '👗', '#B45309', 2, (SELECT id FROM category WHERE slug = 'romance')),
('Paranormal Romance', 'paranormal-romance', 'Supernatural love affairs', '🧛', '#7C3AED', 3, (SELECT id FROM category WHERE slug = 'romance')),
('Romantic Comedy', 'romantic-comedy', 'Laughs and love combined', '😂', '#FACC15', 4, (SELECT id FROM category WHERE slug = 'romance'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- Technology sub-subcategories
INSERT INTO category (name, slug, description, emoji, color, sort_order, parent_id) VALUES
('Web Development', 'web-development', 'Build the modern web', '🌐', '#3B82F6', 1, (SELECT id FROM category WHERE slug = 'technology-programming')),
('Data Science & AI', 'data-science-ai', 'Machine learning and analytics', '🤖', '#8B5CF6', 2, (SELECT id FROM category WHERE slug = 'technology-programming')),
('Mobile Development', 'mobile-development', 'iOS, Android, and cross-platform', '📱', '#22C55E', 3, (SELECT id FROM category WHERE slug = 'technology-programming')),
('Cloud & DevOps', 'cloud-devops', 'Infrastructure and deployment', '☁️', '#0EA5E9', 4, (SELECT id FROM category WHERE slug = 'technology-programming')),
('Cybersecurity', 'cybersecurity', 'Protect and defend digital systems', '🔐', '#EF4444', 5, (SELECT id FROM category WHERE slug = 'technology-programming'))
ON CONFLICT (slug) DO UPDATE SET
    emoji = EXCLUDED.emoji,
    color = EXCLUDED.color,
    sort_order = EXCLUDED.sort_order,
    parent_id = EXCLUDED.parent_id;

-- ============================================================
-- VERIFY COUNT
-- ============================================================
SELECT COUNT(*) as total_categories, 
       COUNT(CASE WHEN parent_id IS NULL THEN 1 END) as root_categories,
       COUNT(CASE WHEN parent_id IS NOT NULL THEN 1 END) as subcategories,
       COUNT(CASE WHEN featured = TRUE THEN 1 END) as featured_categories
FROM category;
