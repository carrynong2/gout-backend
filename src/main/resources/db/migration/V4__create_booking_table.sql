CREATE TABLE IF NOT EXISTS "booking" (
                                         id SERIAL PRIMARY KEY,
                                         user_id INTEGER NOT NULL REFERENCES "user"(id),
    tour_id INTEGER NOT NULL REFERENCES "tour"(id),
    state VARCHAR(20) NOT NULL,
    booking_date TIMESTAMP NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );