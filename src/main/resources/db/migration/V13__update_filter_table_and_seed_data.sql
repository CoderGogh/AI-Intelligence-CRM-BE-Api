-- V13__update_filter_table_and_seed_data.sql
-- 주의: alt_code 컬럼 제거는 이전 실행에서 이미 완료됨.
--       filter_custom, filter_groups의 FK 제약을 위해 FK 체크 비활성화 후 데이터 재적재.

-- FK 체크 비활성화 (filter_custom -> filter FK 충돌 방지)
SET FOREIGN_KEY_CHECKS = 0;

-- 데이터 삭제
DELETE FROM filter_custom;
DELETE FROM filter_groups;
DELETE FROM filter;

-- AUTO_INCREMENT 초기화
ALTER TABLE filter AUTO_INCREMENT = 1;

-- FK 체크 재활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 재적재
INSERT INTO filter (filter_id, filter_key, filter_name) VALUES
(1,  'keyword',         '키워드'),
(2,  'consult_from',    '상담 시작일'),
(3,  'consult_to',      '상담 종료일'),
(4,  'consultant_id',   '담당 상담사'),
(5,  'category_code',   '상담 카테고리'),
(6,  'channel',         '상담 채널'),
(7,  'issue_keyword',   '상담 메모'),
(8,  'action_keyword',  '상담 조치사항'),
(9,  'memo_keyword',    '상담 특이사항'),
(10, 'customer_name',   '고객 이름'),
(11, 'customer_phone',  '고객 연락처'),
(12, 'customer_type',   '고객 유형'),
(13, 'customer_grade',  '고객 등급'),
(14, 'risk_type',       '위험 유형'),
(15, 'product_code',    '상품 코드'),
(16, 'contract_type',   '계약 종류');
