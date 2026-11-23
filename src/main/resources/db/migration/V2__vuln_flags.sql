-- 취약점 토글 플래그 (인시큐어 코딩 실습용).
-- lab 프로필에서만 사용하며, prod(배포)에서는 VulnToggle이 이 테이블을 읽지 않는다.
CREATE TABLE vuln_flags (
    flag_id    VARCHAR(10) NOT NULL,
    enabled    TINYINT(1)  NOT NULL DEFAULT 0,
    updated_at DATETIME     NULL,
    PRIMARY KEY (flag_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 진단 가이드 28개 항목 (1-6 제외). 기본값 전부 off(0).
INSERT INTO vuln_flags (flag_id, enabled) VALUES
('1-1',0),('1-2',0),('1-3',0),('1-4',0),('1-5',0),
('2-1',0),('2-2',0),
('3-1',0),('3-2',0),('3-3',0),('3-4',0),('3-5',0),('3-6',0),
('4-1',0),('4-2',0),('4-3',0),('4-4',0),('4-5',0),
('5-1',0),('5-2',0),
('6-1',0),('6-2',0),
('7-1',0),('7-2',0),('7-3',0),('7-4',0),
('8-1',0);
