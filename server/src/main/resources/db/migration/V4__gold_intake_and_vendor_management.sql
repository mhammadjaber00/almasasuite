-- Migration for Gold Intake and Vendor Management functionality

-- Create vendors table for tracking sellers and their liability balances
CREATE TABLE IF NOT EXISTS vendors (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_info TEXT,
    total_liability_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_paid DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_intake_value DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    synced BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create indexes for vendors table
CREATE INDEX IF NOT EXISTS idx_vendors_name ON vendors(name);
CREATE INDEX IF NOT EXISTS idx_vendors_active ON vendors(is_active);
CREATE INDEX IF NOT EXISTS idx_vendors_liability ON vendors(total_liability_balance);
CREATE INDEX IF NOT EXISTS idx_vendors_created_by ON vendors(created_by);

-- Create gold_intakes table for recording gold purchases from sellers/customers
CREATE TABLE IF NOT EXISTS gold_intakes (
    id VARCHAR(36) PRIMARY KEY,
    vendor_id VARCHAR(36), -- NULL for customers, references vendors(id) for sellers
    party_type VARCHAR(20) NOT NULL CHECK (party_type IN ('seller', 'customer')),
    party_name VARCHAR(255) NOT NULL,
    karat INTEGER NOT NULL CHECK (karat > 0),
    grams DECIMAL(10, 3) NOT NULL CHECK (grams > 0),
    design_fee_per_gram DECIMAL(10, 2) NOT NULL CHECK (design_fee_per_gram >= 0),
    metal_value_per_gram DECIMAL(10, 2) NOT NULL DEFAULT 0.00 CHECK (metal_value_per_gram >= 0),
    total_design_fee_paid DECIMAL(12, 2) NOT NULL CHECK (total_design_fee_paid >= 0),
    total_metal_value_owed DECIMAL(12, 2) NOT NULL DEFAULT 0.00 CHECK (total_metal_value_owed >= 0),
    notes TEXT,
    created_at BIGINT NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    synced BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- Create indexes for gold_intakes table
CREATE INDEX IF NOT EXISTS idx_gold_intakes_vendor_id ON gold_intakes(vendor_id);
CREATE INDEX IF NOT EXISTS idx_gold_intakes_party_type ON gold_intakes(party_type);
CREATE INDEX IF NOT EXISTS idx_gold_intakes_party_name ON gold_intakes(party_name);
CREATE INDEX IF NOT EXISTS idx_gold_intakes_karat ON gold_intakes(karat);
CREATE INDEX IF NOT EXISTS idx_gold_intakes_created_at ON gold_intakes(created_at);
CREATE INDEX IF NOT EXISTS idx_gold_intakes_created_by ON gold_intakes(created_by);

-- Create vendor_payments table for recording liability payouts
CREATE TABLE IF NOT EXISTS vendor_payments (
    id VARCHAR(36) PRIMARY KEY,
    vendor_id VARCHAR(36) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('cash', 'check', 'bank_transfer', 'other')),
    payment_reference VARCHAR(255),
    notes TEXT,
    paid_at BIGINT NOT NULL,
    recorded_at BIGINT NOT NULL,
    recorded_by VARCHAR(36) NOT NULL,
    synced BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE RESTRICT,
    FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- Create indexes for vendor_payments table
CREATE INDEX IF NOT EXISTS idx_vendor_payments_vendor_id ON vendor_payments(vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_payments_paid_at ON vendor_payments(paid_at);
CREATE INDEX IF NOT EXISTS idx_vendor_payments_recorded_at ON vendor_payments(recorded_at);
CREATE INDEX IF NOT EXISTS idx_vendor_payments_recorded_by ON vendor_payments(recorded_by);
CREATE INDEX IF NOT EXISTS idx_vendor_payments_payment_method ON vendor_payments(payment_method);

-- Create a view for vendor liability summary reporting
CREATE OR REPLACE VIEW vendor_liability_summary AS
SELECT 
    v.id,
    v.name,
    v.contact_info,
    v.total_liability_balance,
    v.total_paid,
    v.total_intake_value,
    v.is_active,
    CASE 
        WHEN v.total_intake_value > 0 THEN (v.total_paid / v.total_intake_value) * 100.0
        ELSE 0.0
    END as payment_percentage,
    (SELECT MAX(gi.created_at) FROM gold_intakes gi WHERE gi.vendor_id = v.id) as last_intake_date,
    (SELECT MAX(vp.paid_at) FROM vendor_payments vp WHERE vp.vendor_id = v.id) as last_payment_date,
    (SELECT COUNT(*) FROM gold_intakes gi WHERE gi.vendor_id = v.id) as intake_count,
    (SELECT COUNT(*) FROM vendor_payments vp WHERE vp.vendor_id = v.id) as payment_count
FROM 
    vendors v
WHERE 
    v.is_active = TRUE
ORDER BY
    v.total_liability_balance DESC, v.name ASC;

-- Create a view for gold intake history with vendor information
CREATE OR REPLACE VIEW gold_intake_history AS
SELECT 
    gi.id,
    gi.vendor_id,
    v.name as vendor_name,
    gi.party_type,
    gi.party_name,
    gi.karat,
    gi.grams,
    gi.design_fee_per_gram,
    gi.metal_value_per_gram,
    gi.total_design_fee_paid,
    gi.total_metal_value_owed,
    gi.notes,
    gi.created_at,
    gi.created_by,
    u.name as created_by_name
FROM 
    gold_intakes gi
LEFT JOIN 
    vendors v ON gi.vendor_id = v.id
LEFT JOIN 
    users u ON gi.created_by = u.id
ORDER BY
    gi.created_at DESC;

-- Create a view for vendor payment history
CREATE OR REPLACE VIEW vendor_payment_history AS
SELECT 
    vp.id,
    vp.vendor_id,
    v.name as vendor_name,
    vp.amount,
    vp.payment_method,
    vp.payment_reference,
    vp.notes,
    vp.paid_at,
    vp.recorded_at,
    vp.recorded_by,
    u.name as recorded_by_name
FROM 
    vendor_payments vp
JOIN 
    vendors v ON vp.vendor_id = v.id
LEFT JOIN 
    users u ON vp.recorded_by = u.id
ORDER BY
    vp.paid_at DESC;

-- Add triggers to automatically update vendor balances when intakes or payments are added

-- Function to update vendor balances after gold intake
CREATE OR REPLACE FUNCTION update_vendor_balance_after_intake()
RETURNS TRIGGER AS $$
BEGIN
    -- Only update if this is for a vendor (seller)
    IF NEW.vendor_id IS NOT NULL THEN
        UPDATE vendors 
        SET 
            total_liability_balance = total_liability_balance + NEW.total_metal_value_owed,
            total_intake_value = total_intake_value + NEW.total_metal_value_owed + NEW.total_design_fee_paid,
            updated_at = EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000
        WHERE id = NEW.vendor_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to update vendor balances after payment
CREATE OR REPLACE FUNCTION update_vendor_balance_after_payment()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE vendors 
    SET 
        total_liability_balance = total_liability_balance - NEW.amount,
        total_paid = total_paid + NEW.amount,
        updated_at = EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000
    WHERE id = NEW.vendor_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers
DROP TRIGGER IF EXISTS trigger_update_vendor_after_intake ON gold_intakes;
CREATE TRIGGER trigger_update_vendor_after_intake
    AFTER INSERT ON gold_intakes
    FOR EACH ROW
    EXECUTE FUNCTION update_vendor_balance_after_intake();

DROP TRIGGER IF EXISTS trigger_update_vendor_after_payment ON vendor_payments;
CREATE TRIGGER trigger_update_vendor_after_payment
    AFTER INSERT ON vendor_payments
    FOR EACH ROW
    EXECUTE FUNCTION update_vendor_balance_after_payment();

-- Insert some sample data for testing (optional - can be removed in production)
-- Sample vendor
INSERT INTO vendors (id, name, contact_info, created_at, updated_at, created_by)
VALUES (
    'sample-vendor-id',
    'Sample Gold Seller',
    'Phone: (555) 123-4567, Email: seller@example.com',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
    'admin-user-id'
) ON CONFLICT (id) DO NOTHING;