CREATE TABLE ordinary(id INTEGER PRIMARY KEY, value TEXT, payload BLOB);
INSERT INTO ordinary(value, payload) VALUES(json_array(1, 2, 3), randomblob(32));
CREATE INDEX ordinary_value ON ordinary(value);
SELECT json_extract(value, '$[1]'), length(payload) FROM ordinary WHERE id=1;
WITH RECURSIVE counter(value) AS (VALUES(0) UNION ALL SELECT value+1 FROM counter WHERE value<32)
SELECT sum(value) FROM counter;
