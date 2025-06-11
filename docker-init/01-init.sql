-- Initialize database with pg_warden extension and demo data

-- Create the extension
CREATE EXTENSION pg_warden;

-- Create users (warden_admin role already exists from extension)
-- Create a login user that will have the warden_admin role
CREATE USER warden_admin_user WITH PASSWORD 'warden_admin_pass' LOGIN;
CREATE USER regular_user WITH PASSWORD 'regular_user_pass' LOGIN;

-- Grant the warden_admin role to the warden_admin_user
GRANT warden_admin TO warden_admin_user;

-- Create tables
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    price DECIMAL(10,2),
    stock_quantity INTEGER,
    description TEXT
);

CREATE TABLE customer_payments (
    id SERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    card_last_four_digits VARCHAR(4),
    card_type VARCHAR(50),
    amount DECIMAL(10,2),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_id VARCHAR(100) UNIQUE
);

-- Grant permissions
GRANT ALL ON SCHEMA public TO warden_admin_user, regular_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO warden_admin_user, regular_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO warden_admin_user, regular_user;

-- Unprotect regular data table (warden is opt-out, all tables are protected by default)
SELECT warden_unprotect('products');

-- Insert sample data for products
INSERT INTO products (name, category, price, stock_quantity, description) VALUES 
    ('Laptop', 'Electronics', 999.99, 10, 'High-performance laptop'),
    ('Mouse', 'Electronics', 29.99, 50, 'Wireless mouse'),
    ('Keyboard', 'Electronics', 79.99, 30, 'Mechanical keyboard'),
    ('Monitor', 'Electronics', 299.99, 15, '27-inch LED monitor'),
    ('Desk', 'Furniture', 199.99, 5, 'Adjustable standing desk');

-- Insert sample data for customer payments
INSERT INTO customer_payments (customer_name, card_last_four_digits, card_type, amount, transaction_id) VALUES
    ('John Doe', '1234', 'VISA', 999.99, 'TXN001'),
    ('Jane Smith', '5678', 'MasterCard', 329.98, 'TXN002'),
    ('Bob Wilson', '9012', 'AMEX', 79.99, 'TXN003'),
    ('Alice Brown', '3456', 'VISA', 299.99, 'TXN004');

-- Show initial state
\echo 'Protected tables (empty means all tables are protected):'
SELECT * FROM warden_unprotected_tables;