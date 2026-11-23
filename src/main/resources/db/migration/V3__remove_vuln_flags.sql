-- V2는 이미 적용된 DB의 Flyway 체크섬을 위해 보존한다.
-- 런타임 토글을 폐기했으므로 기존 플래그 상태 테이블만 제거한다.
DROP TABLE IF EXISTS vuln_flags;
