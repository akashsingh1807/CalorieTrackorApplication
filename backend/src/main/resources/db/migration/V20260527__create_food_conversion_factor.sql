CREATE TABLE food_conversion_factor (
    id BIGSERIAL PRIMARY KEY,
    fdc_id VARCHAR(64),
    name VARCHAR(255) NOT NULL,
    protein NUMERIC(10,4),
    fat NUMERIC(10,4),
    carbohydrate NUMERIC(10,4),
    calories NUMERIC(10,4)
);
