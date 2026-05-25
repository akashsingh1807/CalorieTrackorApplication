package com.calorie.tracker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseSchemaUpdater {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaUpdater.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void updateDatabaseSchema() {
        try {
            logger.info("Checking and updating database schema for Google Sign-In columns...");
            
            // Add auth_provider column if it doesn't exist
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(255) DEFAULT 'LOCAL'");
            
            // Add role column if it doesn't exist
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(255) DEFAULT 'ROLE_USER'");
            
            // Add google_id column if it doesn't exist
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS google_id VARCHAR(255) UNIQUE");

            // Add new onboarding columns
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS age INTEGER");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS lifestyle VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS daily_protein_goal INTEGER");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS daily_carbs_goal INTEGER");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS daily_fat_goal INTEGER");
            
            // Add AI & Gamification columns (Phase 1)
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS persona_preference VARCHAR(255) DEFAULT 'STRICT_TRAINER'");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS subscription_tier VARCHAR(255) DEFAULT 'FREE'");
            
            jdbcTemplate.execute("ALTER TABLE meals ADD COLUMN IF NOT EXISTS raw_text_input TEXT");
            jdbcTemplate.execute("ALTER TABLE meals ADD COLUMN IF NOT EXISTS meal_image_url VARCHAR(1024)");
            jdbcTemplate.execute("ALTER TABLE meals ADD COLUMN IF NOT EXISTS confidence_score FLOAT");
            jdbcTemplate.execute("ALTER TABLE meals ADD COLUMN IF NOT EXISTS is_ai_logged BOOLEAN DEFAULT FALSE");
            
            logger.info("Database schema update completed successfully!");
        } catch (Exception e) {
            logger.error("Failed to update database schema. This might be fine if columns already exist.", e);
        }
    }
}
