-- 1. code_name 단독 UNIQUE 제약 제거 (없으면 무시)
ALTER TABLE analysis_code DROP INDEX IF EXISTS uq_analysis_code_name;

-- 2. V41에서 누락된 ('OTHER', 'defense_category') 삽입
INSERT IGNORE INTO analysis_code (code_name, classification) VALUES
    ('OTHER', 'defense_category');
