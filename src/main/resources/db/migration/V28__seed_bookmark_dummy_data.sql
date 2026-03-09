-- =========================================================
-- V28: 북마크 API Swagger 테스트용 더미 데이터 적재
-- manuals 10건
-- customers 10건
-- consultation_results 10건
-- =========================================================

-- =========================================================
-- 1. manuals 더미 10건
-- =========================================================
INSERT INTO manuals (
    title,
    content,
    is_active,
    category,
    target_customer_type,
    status
) VALUES
('[북마크 테스트] 운영정책 01', '북마크 테스트용 운영정책 내용입니다. 01', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 02', '북마크 테스트용 운영정책 내용입니다. 02', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 03', '북마크 테스트용 운영정책 내용입니다. 03', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 04', '북마크 테스트용 운영정책 내용입니다. 04', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 05', '북마크 테스트용 운영정책 내용입니다. 05', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 06', '북마크 테스트용 운영정책 내용입니다. 06', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 07', '북마크 테스트용 운영정책 내용입니다. 07', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 08', '북마크 테스트용 운영정책 내용입니다. 08', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 09', '북마크 테스트용 운영정책 내용입니다. 09', 1, '미분류', 'ALL', 'ACTIVE'),
('[북마크 테스트] 운영정책 10', '북마크 테스트용 운영정책 내용입니다. 10', 1, '미분류', 'ALL', 'ACTIVE');

-- =========================================================
-- 2. customers 더미 10건
-- =========================================================
INSERT INTO customers (
    identification_num,
    name,
    customer_type,
    gender,
    birth_date,
    grade_code,
    preferred_contact,
    email,
    phone
) VALUES
(UUID(), '북마크테스트고객01', '개인', 'M', '1990-01-01', NULL, 'CALL', NULL, '01000000001'),
(UUID(), '북마크테스트고객02', '개인', 'F', '1991-02-02', NULL, 'CALL', NULL, '01000000002'),
(UUID(), '북마크테스트고객03', '개인', 'M', '1992-03-03', NULL, 'CALL', NULL, '01000000003'),
(UUID(), '북마크테스트고객04', '개인', 'F', '1993-04-04', NULL, 'CALL', NULL, '01000000004'),
(UUID(), '북마크테스트고객05', '개인', 'M', '1994-05-05', NULL, 'CALL', NULL, '01000000005'),
(UUID(), '북마크테스트고객06', '개인', 'F', '1995-06-06', NULL, 'CALL', NULL, '01000000006'),
(UUID(), '북마크테스트고객07', '개인', 'M', '1996-07-07', NULL, 'CALL', NULL, '01000000007'),
(UUID(), '북마크테스트고객08', '개인', 'F', '1997-08-08', NULL, 'CALL', NULL, '01000000008'),
(UUID(), '북마크테스트고객09', '개인', 'M', '1998-09-09', NULL, 'CALL', NULL, '01000000009'),
(UUID(), '북마크테스트고객10', '개인', 'F', '1999-10-10', NULL, 'CALL', NULL, '01000000010');

-- =========================================================
-- 3. consultation_results 더미 10건
-- emp_id = 1 사용
-- customer는 방금 insert한 이름으로 조회
-- category_code는 consultation_category_policy에 존재하는 값 사용
-- =========================================================
INSERT INTO consultation_results (
    emp_id,
    customer_id,
    channel,
    category_code,
    duration_sec,
    iam_issue,
    iam_action,
    iam_memo
)
SELECT 1, c.customer_id, 'CALL', 'M_FEE_02', 60,  '테스트 이슈 01', '테스트 조치 01', '테스트 메모 01'
FROM customers c
WHERE c.name = '북마크테스트고객01'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_FEE_03', 90,  '테스트 이슈 02', '테스트 조치 02', '테스트 메모 02'
FROM customers c
WHERE c.name = '북마크테스트고객02'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_FEE_04', 120, '테스트 이슈 03', '테스트 조치 03', '테스트 메모 03'
FROM customers c
WHERE c.name = '북마크테스트고객03'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_FEE_05', 150, '테스트 이슈 04', '테스트 조치 04', '테스트 메모 04'
FROM customers c
WHERE c.name = '북마크테스트고객04'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_FEE_06', 180, '테스트 이슈 05', '테스트 조치 05', '테스트 메모 05'
FROM customers c
WHERE c.name = '북마크테스트고객05'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_FEE_07', 210, '테스트 이슈 06', '테스트 조치 06', '테스트 메모 06'
FROM customers c
WHERE c.name = '북마크테스트고객06'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_FEE_08', 240, '테스트 이슈 07', '테스트 조치 07', '테스트 메모 07'
FROM customers c
WHERE c.name = '북마크테스트고객07'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_FEE_09', 270, '테스트 이슈 08', '테스트 조치 08', '테스트 메모 08'
FROM customers c
WHERE c.name = '북마크테스트고객08'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_FEE_10', 300, '테스트 이슈 09', '테스트 조치 09', '테스트 메모 09'
FROM customers c
WHERE c.name = '북마크테스트고객09'
UNION ALL
SELECT 1, c.customer_id, 'CALL', 'M_TRB_01', 330, '테스트 이슈 10', '테스트 조치 10', '테스트 메모 10'
FROM customers c
WHERE c.name = '북마크테스트고객10';