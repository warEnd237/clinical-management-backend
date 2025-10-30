-- Option 1: Delete the V2 migration record (Flyway will re-apply it)
DELETE FROM flyway_schema_history WHERE version = '2';

-- Option 2: Update the checksum to match the new file
UPDATE flyway_schema_history 
SET checksum = 656756261 
WHERE version = '2';
