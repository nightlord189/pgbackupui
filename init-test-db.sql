-- Create test database schema with sample data

-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Orders table
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    shipping_address TEXT
);

-- Insert test users
INSERT INTO users (username, email, full_name, is_active) VALUES
    ('john_doe', 'john@example.com', 'John Doe', true),
    ('jane_smith', 'jane@example.com', 'Jane Smith', true),
    ('bob_jones', 'bob@example.com', 'Bob Jones', true);

-- Insert test orders
INSERT INTO orders (user_id, total_amount, status, shipping_address) VALUES
    (1, 1329.98, 'completed', '123 Main St, New York, NY 10001'),
    (2, 89.99, 'completed', '456 Oak Ave, Los Angeles, CA 90001'),
    (3, 649.98, 'pending', '789 Pine Rd, Chicago, IL 60601'),
    (1, 74.98, 'shipped', '123 Main St, New York, NY 10001');

-- Create indexes for better query performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
