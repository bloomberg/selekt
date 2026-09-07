SELECT vec1_from_json(NULL);
SELECT vec1_to_json(vec1_from_json('[0.0, 1.0, -2.5, 3.25]'));
CREATE VIRTUAL TABLE vectors USING vec1(vector, tag);
INSERT INTO vectors(vector, tag) VALUES(vec1_from_json('[0,0,0,0]'), 1.0);
SELECT rowid FROM vectors WHERE vector MATCH vec1_from_json('[0,0,0,0]') AND k=1;
PRAGMA integrity_check;
