-- ============================================================
-- Migration: V22__alter_customer_contracts_add_customer_id
-- 작성일: 2026-03-02
-- 대상 테이블: customer_contracts
-- 변경 목적: customer_id 컬럼 신규 추가 및 customers 테이블 FK 설정
-- ⚠️ 주의: 기존 데이터가 존재할 경우 NOT NULL 컬럼 추가 전
--           데이터 백필(backfill) 처리가 필요합니다.
--           데이터가 없는 경우 또는 백필 완료 후 실행하세요.
-- ============================================================

ALTER TABLE customer_contracts
    ADD COLUMN customer_id BIGINT NOT NULL
        COMMENT 'customers.customer_id 참조'
        AFTER contract_id,
    ADD CONSTRAINT fk_cc_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers (customer_id);
