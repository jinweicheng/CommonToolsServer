-- Migration: make front_errors.error_message a TEXT column to allow long messages
ALTER TABLE `front_errors` MODIFY COLUMN `error_message` TEXT NULL;