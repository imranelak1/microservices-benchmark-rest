CREATE SCHEMA variant_a;
CREATE SCHEMA variant_c;
CREATE SCHEMA variant_d;
CREATE FUNCTION pg_temp.seed_catalog(schema_name TEXT) RETURNS VOID AS $$
BEGIN
  EXECUTE format('CREATE TABLE %I.category (id BIGSERIAL PRIMARY KEY,code VARCHAR(32) UNIQUE NOT NULL,name VARCHAR(128) NOT NULL,updated_at TIMESTAMP NOT NULL DEFAULT NOW())',schema_name);
  EXECUTE format('CREATE TABLE %I.item (id BIGSERIAL PRIMARY KEY,sku VARCHAR(64) UNIQUE NOT NULL,name VARCHAR(128) NOT NULL,price NUMERIC(10,2) NOT NULL,stock INT NOT NULL,description VARCHAR(6000),category_id BIGINT NOT NULL REFERENCES %I.category(id),updated_at TIMESTAMP NOT NULL DEFAULT NOW())',schema_name,schema_name);
  EXECUTE format('CREATE INDEX idx_item_category ON %I.item(category_id)',schema_name);
  EXECUTE format('CREATE INDEX idx_item_updated_at ON %I.item(updated_at)',schema_name);
  EXECUTE format('INSERT INTO %I.category(code,name) SELECT ''CAT-''||n,''Catégorie ''||n FROM generate_series(1,2000) n',schema_name);
  EXECUTE format('INSERT INTO %I.item(sku,name,price,stock,description,category_id) SELECT ''SKU-''||n,''Produit ''||n,round((n%%10000)::numeric/100+1,2),n%%500,repeat(''x'',850),1+(n-1)%%2000 FROM generate_series(1,100000) n',schema_name);
END; $$ LANGUAGE plpgsql;
SELECT pg_temp.seed_catalog('variant_a');
SELECT pg_temp.seed_catalog('variant_c');
SELECT pg_temp.seed_catalog('variant_d');
