CREATE USER IF NOT EXISTS 'productaddaAppServiceAcc'@'%'
IDENTIFIED BY 'codeaxisFuison1';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE
ON product_adda_db.*
TO 'productaddaAppServiceAcc'@'%';

FLUSH PRIVILEGES;

SHOW GRANTS FOR 'productaddaAppServiceAcc'@'%';

SELECT 
   *
FROM mysql.user;

