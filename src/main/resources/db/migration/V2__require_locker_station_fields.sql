-- Existing databases were baselined at V1. This brings their station nullability
-- in line with the JPA entity after the existing rows were checked for NULLs.
ALTER TABLE public.locker_stations
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN model SET NOT NULL,
    ALTER COLUMN manufacturer SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN location SET NOT NULL;
