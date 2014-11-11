CREATE TABLE `employee` (
    `id` BIGINT(10)   PRIMARY KEY AUTOINCREMENT,
    `firstname` VARCHAR(50) NULL DEFAULT NULL,
    `lastname` VARCHAR(50) NULL DEFAULT NULL,
    `birth_date` DATE NOT NULL,
    `cell_phone` VARCHAR(15) NOT NULL
)

drop table `employee` 