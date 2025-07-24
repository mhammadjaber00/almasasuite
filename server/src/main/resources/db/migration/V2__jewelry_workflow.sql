-- Migration for jewelry workflow

-- Create a temporary table with the new structure
CREATE TABLE IF NOT EXISTS products_new (
    id VARCHAR(36) PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    image_url VARCHAR(255),
    type VARCHAR(20) NOT NULL CHECK (type IN ('ring', 'bracelet', 'necklace', 'earring', 'other')),
    karat INTEGER NOT NULL,
    weight_grams DECIMAL(10, 2) NOT NULL,
    design_fee DECIMAL(10, 2) NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes on the new table
CREATE INDEX IF NOT EXISTS idx_products_new_sku ON products_new(sku);
CREATE INDEX IF NOT EXISTS idx_products_new_type ON products_new(type);
CREATE INDEX IF NOT EXISTS idx_products_new_karat ON products_new(karat);

-- Update SaleItems table to add profit calculation fields
ALTER TABLE sale_items ADD COLUMN IF NOT EXISTS purchase_price DECIMAL(10, 2);
ALTER TABLE sale_items ADD COLUMN IF NOT EXISTS design_fee DECIMAL(10, 2);
ALTER TABLE sale_items ADD COLUMN IF NOT EXISTS profit DECIMAL(10, 2);

-- Create a view for inventory reporting
CREATE OR REPLACE VIEW inventory_report AS
SELECT 
    p.id,
    p.sku,
    p.type,
    p.karat,
    p.weight_grams,
    p.design_fee,
    p.purchase_price,
    p.quantity_in_stock,
    p.created_at,
    p.updated_at
FROM 
    products_new p
ORDER BY
    p.type, p.karat, p.weight_grams;

-- Create a view for sales reporting including profit
CREATE OR REPLACE VIEW sales_report AS
SELECT 
    s.id AS sale_id,
    s.date AS sale_date,
    s.total,
    s.payment_method,
    si.id AS item_id,
    p.sku,
    p.type,
    p.karat,
    p.weight_grams,
    si.quantity,
    si.unit_price,
    si.subtotal,
    si.purchase_price,
    si.design_fee,
    si.profit
FROM 
    sales s
JOIN 
    sale_items si ON s.id = si.sale_id
JOIN 
    products_new p ON si.product_id = p.id
ORDER BY
    s.date DESC;

-- After the application is updated, you'll need to run the following manually:
-- 1. Migrate data from products to products_new
-- 2. Drop the old products table
-- 3. Rename products_new to products
-- These steps are not included in this migration as they require application changes first