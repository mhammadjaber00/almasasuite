-- Initial database schema for Almasa Suite

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    stone VARCHAR(100) NOT NULL,
    carat DECIMAL(10, 2) NOT NULL,
    weight DECIMAL(10, 2) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on product SKU for faster lookups
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);

-- Create index on product name for search functionality
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- Sales table
CREATE TABLE IF NOT EXISTS sales (
    id VARCHAR(36) PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    synced BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create index on sale date for faster lookups
CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(date);

-- Sale items table
CREATE TABLE IF NOT EXISTS sale_items (
    id VARCHAR(36) PRIMARY KEY,
    sale_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- Create index on sale_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_sale_items_sale_id ON sale_items(sale_id);

-- Create index on product_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_sale_items_product_id ON sale_items(product_id);

-- Stock mutations table
CREATE TABLE IF NOT EXISTS stock_mutations (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    delta INTEGER NOT NULL,
    reason VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    synced BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- Create index on product_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_stock_mutations_product_id ON stock_mutations(product_id);

-- Create index on timestamp for faster lookups
CREATE INDEX IF NOT EXISTS idx_stock_mutations_timestamp ON stock_mutations(timestamp);

-- Create index on reason for faster lookups
CREATE INDEX IF NOT EXISTS idx_stock_mutations_reason ON stock_mutations(reason);

-- Insert a default admin user (password: admin123)
INSERT INTO users (id, email, password_hash, name, created_at, updated_at)
VALUES (
    'admin-user-id',
    'admin@almasa.io',
    '$2a$12$k8Y6Ri8AYMQkBCqhZxXyT.6JVNiQGQjlV7rqagcbGLYnvHXUQlGAe',
    'Admin User',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;