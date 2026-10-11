-- ===================================================================
-- Trust Circle Database Initialization Script
-- ===================================================================
-- This script creates the complete database schema and seed data
-- for testing the Trust Circle platform
-- ===================================================================

-- Drop existing database if it exists (caution in production!)
DROP DATABASE IF EXISTS trust_circle;

-- Create database
CREATE DATABASE trust_circle CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE trust_circle;

-- ===================================================================
-- TABLE 1: USERS
-- ===================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    bio VARCHAR(1000),

    -- Location & Geo Verification
    claimed_city VARCHAR(100),
    verified_latitude DOUBLE,
    verified_longitude DOUBLE,
    location_accuracy_meters DOUBLE,
    is_location_verified BOOLEAN DEFAULT FALSE,
    location_verification_notes VARCHAR(500),

    -- Identity Verification
    is_identity_verified BOOLEAN DEFAULT FALSE,
    verification_status VARCHAR(50) DEFAULT 'PENDING', -- VERIFIED, PENDING, REJECTED
    verified_at DATETIME,

    -- Dynamic Trust Score
    trust_score INT DEFAULT 50, -- 0-100
    trust_tier VARCHAR(50) DEFAULT 'BRONZE', -- BRONZE, SILVER, GOLD, PLATINUM

    -- Activity & Reputation
    activities_attended INT DEFAULT 0,
    activities_hosted INT DEFAULT 0,
    peer_rating_avg DOUBLE DEFAULT 5.0, -- 0.0-5.0
    ratings_count INT DEFAULT 0,

    -- Preferences
    interests VARCHAR(500), -- Comma-separated
    social_vibe VARCHAR(100),
    budget_preference VARCHAR(50), -- BUDGET_FRIENDLY, MODERATE, FLEXIBLE
    typical_availability VARCHAR(100),
    safety_pact_agreed VARCHAR(10),

    -- Badges & Safety
    badges VARCHAR(500),
    is_flagged BOOLEAN DEFAULT FALSE,
    flag_reason VARCHAR(500),
    role VARCHAR(50) DEFAULT 'ROLE_USER', -- ROLE_USER, ROLE_ADMIN

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_active_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_trust_score (trust_score),
    INDEX idx_is_flagged (is_flagged)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- TABLE 2: ACTIVITIES
-- ===================================================================
CREATE TABLE activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    activity_type VARCHAR(100), -- Sports, Study, Travel, Events, Social

    -- Location
    latitude DOUBLE,
    longitude DOUBLE,
    location VARCHAR(255),

    -- Schedule
    scheduled_date DATE,
    scheduled_time TIME,
    duration_minutes INT,

    -- Constraints
    min_participants INT DEFAULT 2,
    max_participants INT DEFAULT 10,
    estimated_budget DOUBLE,

    -- Status & Verification
    status VARCHAR(50) DEFAULT 'CREATED', -- CREATED, ONGOING, COMPLETED, CANCELLED
    is_verified BOOLEAN DEFAULT FALSE,
    verification_notes VARCHAR(500),

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,

    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    INDEX idx_scheduled_date (scheduled_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- TABLE 3: ACTIVITY_PARTICIPANTS
-- ===================================================================
CREATE TABLE activity_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'JOINED', -- JOINED, COMPLETED, LEFT
    participant_rating INT, -- 1-5 stars from host
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_activity_user (activity_id, user_id),
    INDEX idx_activity_id (activity_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- TABLE 4: PEER_REVIEWS
-- ===================================================================
CREATE TABLE peer_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- Person being reviewed
    reviewed_by_id BIGINT NOT NULL, -- Person giving review
    rating INT NOT NULL, -- 1-5 stars
    comment VARCHAR(500),

    -- Review aspects
    punctuality INT,
    hygiene INT,
    communication INT,
    respectfulness INT,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_reviewed_by_id (reviewed_by_id),
    INDEX idx_activity_id (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- TABLE 5: SAFETY_ALERTS
-- ===================================================================
CREATE TABLE safety_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    suspected_user_id BIGINT NOT NULL,
    reported_by_id BIGINT,
    report_reason VARCHAR(500),
    severity_level VARCHAR(50), -- LOW, MEDIUM, HIGH, CRITICAL
    status VARCHAR(50) DEFAULT 'OPEN', -- OPEN, INVESTIGATING, RESOLVED

    resolution_notes VARCHAR(500),
    resolved_at DATETIME,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (suspected_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_by_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_suspected_user_id (suspected_user_id),
    INDEX idx_status (status),
    INDEX idx_severity_level (severity_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- SEED DATA FOR TESTING
-- ===================================================================

-- Insert test users
INSERT INTO users (
    email, password, full_name, phone, bio, claimed_city,
    is_identity_verified, verification_status, verified_at,
    trust_score, trust_tier, activities_attended, activities_hosted,
    peer_rating_avg, ratings_count, interests, social_vibe,
    budget_preference, typical_availability, safety_pact_agreed,
    role, created_at, last_active_at
) VALUES
-- User 1: Abhijeet (Admin)
('abhijeet@trustcircle.com',
 'hashed_password_1_bcrypt_here', -- In real app, this is hashed
 'Abhijeet Chandel',
 '+91-9876543210',
 'Python developer, love outdoor activities and travel',
 'Noida',
 TRUE,
 'VERIFIED',
 NOW(),
 85,
 'GOLD',
 12, 5, 4.8, 15,
 'Badminton,Travel,Photography,Coding',
 'Active & Outdoorsy',
 'FLEXIBLE',
 'WEEKENDS',
 'YES',
 'ROLE_ADMIN',
 NOW(),
 NOW()
),

-- User 2: Priya
('priya.sharma@trustcircle.com',
 'hashed_password_2_bcrypt_here',
 'Priya Sharma',
 '+91-9876543211',
 'Fitness enthusiast, yoga instructor',
 'Gurgaon',
 TRUE,
 'VERIFIED',
 NOW(),
 78,
 'SILVER',
 8, 3, 4.6, 12,
 'Yoga,Fitness,Hiking,Wellness',
 'Chill & Casual',
 'MODERATE',
 'WEEKDAY_EVENINGS',
 'YES',
 'ROLE_USER',
 NOW(),
 NOW()
),

-- User 3: Rahul
('rahul.verma@trustcircle.com',
 'hashed_password_3_bcrypt_here',
 'Rahul Verma',
 '+91-9876543212',
 'Student, loves cricket and movies',
 'Ghaziabad',
 TRUE,
 'VERIFIED',
 NOW(),
 65,
 'SILVER',
 6, 2, 4.4, 8,
 'Cricket,Movies,Gaming,Study',
 'Active & Outdoorsy',
 'BUDGET_FRIENDLY',
 'WEEKENDS',
 'YES',
 'ROLE_USER',
 NOW(),
 NOW()
),

-- User 4: Neha (Unverified)
('neha.patel@trustcircle.com',
 'hashed_password_4_bcrypt_here',
 'Neha Patel',
 '+91-9876543213',
 'Artist and photographer',
 'Delhi',
 FALSE,
 'PENDING',
 NULL,
 50,
 'BRONZE',
 0, 0, 5.0, 0,
 'Photography,Art,Music',
 'Chill & Casual',
 'MODERATE',
 'ANYTIME',
 'NO',
 'ROLE_USER',
 NOW(),
 NOW()
),

-- User 5: Sameer
('sameer.khan@trustcircle.com',
 'hashed_password_5_bcrypt_here',
 'Sameer Khan',
 '+91-9876543214',
 'Startup founder, tech enthusiast',
 'Noida',
 TRUE,
 'VERIFIED',
 NOW(),
 72,
 'SILVER',
 4, 3, 4.7, 10,
 'Startups,Technology,Networking,Travel',
 'Active & Outdoorsy',
 'FLEXIBLE',
 'WEEKENDS',
 'YES',
 'ROLE_USER',
 NOW(),
 NOW()
),

-- User 6: Flagged User (for safety testing)
('suspect.user@trustcircle.com',
 'hashed_password_6_bcrypt_here',
 'Suspect User',
 '+91-9876543215',
 'Test account for safety monitoring',
 'Mumbai',
 FALSE,
 'PENDING',
 NULL,
 25,
 'BRONZE',
 1, 0, 2.0, 3,
 'Unknown',
 'Unknown',
 'BUDGET_FRIENDLY',
 'WEEKENDS',
 'NO',
 'ROLE_USER',
 NOW(),
 NOW()
);

-- Insert test activities
INSERT INTO activities (
    creator_id, title, description, activity_type,
    latitude, longitude, location,
    scheduled_date, scheduled_time, duration_minutes,
    min_participants, max_participants, estimated_budget,
    status, is_verified, created_at
) VALUES
-- Activity 1: Badminton Session
(1,
 'Evening Badminton at Delhi Club',
 'Casual badminton session at the club. All skill levels welcome!',
 'Sports',
 28.6139, 77.0249, 'Delhi Club, New Delhi',
 DATE_ADD(CURDATE(), INTERVAL 3 DAY), '18:00:00', 90,
 4, 8, 200,
 'CREATED', TRUE, NOW()
),

-- Activity 2: Study Group
(3,
 'DSA & Competitive Programming Study Group',
 'Prepare for placements together. Focus on arrays, strings, trees',
 'Study',
 28.4089, 77.1066, 'Coffee House, Ghaziabad',
 DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00', 120,
 3, 6, 150,
 'CREATED', TRUE, NOW()
),

-- Activity 3: Yoga Session
(2,
 'Morning Yoga & Meditation',
 'Beginner-friendly yoga session. Bring your own mat.',
 'Wellness',
 28.5244, 77.1855, 'Community Park, Gurgaon',
 DATE_ADD(CURDATE(), INTERVAL 1 DAY), '06:30:00', 60,
 5, 15, 100,
 'CREATED', TRUE, NOW()
),

-- Activity 4: Travel Planning
(5,
 'Monsoon Hiking Trip to Mandi Himalayas',
 '3-day trip to explore Mandi region. Budget-friendly accommodation arranged.',
 'Travel',
 32.2390, 76.9000, 'Mandi, Himachal Pradesh',
 DATE_ADD(CURDATE(), INTERVAL 10 DAY), '06:00:00', 2880,
 4, 10, 5000,
 'CREATED', TRUE, NOW()
),

-- Activity 5: Movie Night
(2,
 'Movie Night - Bollywood Thriller',
 'Casual movie night followed by dinner discussion',
 'Social',
 28.6139, 77.0249, 'PVR Cinemas, Delhi',
 DATE_ADD(CURDATE(), INTERVAL 5 DAY), '19:30:00', 180,
 3, 5, 800,
 'CREATED', FALSE, NOW()
),

-- Activity 6: Completed Activity (for testing reviews)
(1,
 'Cricket Match - Noida Legends',
 'Friendly cricket match between local teams',
 'Sports',
 28.5921, 77.3722, 'Sector 18, Noida',
 DATE_SUB(CURDATE(), INTERVAL 7 DAY), '17:00:00', 180,
 11, 22, 1000,
 'COMPLETED', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY)
);

-- Insert test activity participants
INSERT INTO activity_participants (activity_id, user_id, status, participant_rating, joined_at) VALUES
-- Activity 1 (Badminton) participants
(1, 2, 'JOINED', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 3, 'JOINED', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 5, 'JOINED', NULL, NOW()),

-- Activity 2 (Study Group) participants
(2, 1, 'JOINED', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 4, 'JOINED', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 5, 'JOINED', NULL, NOW()),

-- Activity 3 (Yoga) participants
(3, 1, 'JOINED', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 4, 'JOINED', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Activity 6 (Completed - Cricket) participants with ratings
(6, 2, 'COMPLETED', 5, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(6, 3, 'COMPLETED', 4, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(6, 5, 'COMPLETED', 5, DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Insert test peer reviews (for completed activity)
INSERT INTO peer_reviews (activity_id, user_id, reviewed_by_id, rating, comment, punctuality, hygiene, communication, respectfulness, created_at) VALUES
(6, 2, 1, 5, 'Excellent player! Very cooperative and friendly', 5, 5, 5, 5, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6, 3, 1, 4, 'Good performance. Could improve communication', 4, 4, 3, 4, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6, 5, 1, 5, 'Outstanding! Great team player', 5, 5, 5, 5, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6, 1, 2, 5, 'Excellent organizer! Well-coordinated match', 5, 5, 5, 5, DATE_SUB(NOW(), INTERVAL 6 DAY));

-- Insert test safety alerts
INSERT INTO safety_alerts (suspected_user_id, reported_by_id, report_reason, severity_level, status, created_at) VALUES
(6, 1, 'Suspicious behavior pattern and incomplete profile', 'MEDIUM', 'OPEN', NOW()),
(6, 3, 'Failed to show up for 2 activities without notice', 'LOW', 'OPEN', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ===================================================================
-- CREATE INDEXES FOR PERFORMANCE
-- ===================================================================
CREATE INDEX idx_activities_creator ON activities(creator_id);
CREATE INDEX idx_activities_type ON activities(activity_type);
CREATE INDEX idx_participants_user ON activity_participants(user_id);
CREATE INDEX idx_reviews_user ON peer_reviews(user_id);
CREATE INDEX idx_safety_status ON safety_alerts(status);

-- ===================================================================
-- VERIFY DATABASE SETUP
-- ===================================================================
SELECT 'Database setup complete!' AS status;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_activities FROM activities;
SELECT COUNT(*) as total_participants FROM activity_participants;
SELECT COUNT(*) as total_reviews FROM peer_reviews;
SELECT COUNT(*) as total_alerts FROM safety_alerts;
