-- MySQL Initialization Script
-- 이유: 루트 사용자 권한 설정 및 데이터베이스 초기화를 위해

ALTER USER 'root'@'localhost' IDENTIFIED BY '1234';
ALTER USER 'root'@'%' IDENTIFIED BY '1234';
FLUSH PRIVILEGES;