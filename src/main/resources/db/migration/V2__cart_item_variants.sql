ALTER TABLE cart_item
    ADD COLUMN IF NOT EXISTS variant_key VARCHAR(128) NOT NULL DEFAULT '';

ALTER TABLE cart_item
    ADD COLUMN IF NOT EXISTS variant_display_name VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_cart_item_variant_scope ON cart_item(cart_id, product_id, variant_key);
