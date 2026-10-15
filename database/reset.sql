-- Réinitialiser uniquement le jeu de données généré de cette étude, entre campagnes.
DO $$
DECLARE schema_name TEXT;
BEGIN
  FOREACH schema_name IN ARRAY ARRAY['variant_a','variant_c','variant_d'] LOOP
    EXECUTE format('TRUNCATE %I.item,%I.category RESTART IDENTITY CASCADE',schema_name,schema_name);
    EXECUTE format('INSERT INTO %I.category(code,name) SELECT ''CAT-''||n,''Catégorie ''||n FROM generate_series(1,2000) n',schema_name);
    EXECUTE format('INSERT INTO %I.item(sku,name,price,stock,description,category_id) SELECT ''SKU-''||n,''Produit ''||n,round((n%%10000)::numeric/100+1,2),n%%500,repeat(''x'',850),1+(n-1)%%2000 FROM generate_series(1,100000) n',schema_name);
  END LOOP;
END; $$;
ANALYZE variant_a.category;
ANALYZE variant_a.item;
ANALYZE variant_c.category;
ANALYZE variant_c.item;
ANALYZE variant_d.category;
ANALYZE variant_d.item;
