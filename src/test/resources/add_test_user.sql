ALTER SESSION SET CONTAINER=XEPDB1;

-- 1. Create User A
CREATE USER testuser IDENTIFIED BY testpassword;
GRANT CREATE SESSION, CREATE TABLE TO testuser;