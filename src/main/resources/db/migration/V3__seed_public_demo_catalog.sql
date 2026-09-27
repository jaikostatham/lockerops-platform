-- Give the portfolio a small, fictional public catalog on a fresh database.
-- Existing environments with stations keep their current sample data.
DO $$
DECLARE
    madrid_station_id BIGINT;
    valencia_station_id BIGINT;
    sevilla_station_id BIGINT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM public.locker_stations) THEN
        INSERT INTO public.locker_stations
            (name, model, manufacturer, status, location, image_url)
        VALUES
            ('Locker Station Madrid Centro', 'LOCKER STANDARD', 'Jaico Systems', 'ACTIVE', 'Madrid Centro', NULL)
        RETURNING id INTO madrid_station_id;

        INSERT INTO public.locker_stations
            (name, model, manufacturer, status, location, image_url)
        VALUES
            ('Locker Station Valencia Norte', 'LOCKER PRO', 'Jaico Systems', 'MAINTENANCE', 'Valencia Norte', NULL)
        RETURNING id INTO valencia_station_id;

        INSERT INTO public.locker_stations
            (name, model, manufacturer, status, location, image_url)
        VALUES
            ('Locker Station Sevilla Este', 'LOCKER OUTDOOR', 'Jaico Systems', 'INACTIVE', 'Sevilla Este', NULL)
        RETURNING id INTO sevilla_station_id;

        INSERT INTO public.locker_compartments
            (compartment_number, size, status, locker_station_id)
        VALUES
            (1, 'SMALL', 'AVAILABLE', madrid_station_id),
            (2, 'MEDIUM', 'AVAILABLE', madrid_station_id),
            (3, 'LARGE', 'AVAILABLE', madrid_station_id),
            (4, 'EXTRA_LARGE', 'AVAILABLE', madrid_station_id),
            (1, 'SMALL', 'OUT_OF_SERVICE', valencia_station_id),
            (2, 'MEDIUM', 'OUT_OF_SERVICE', valencia_station_id),
            (3, 'LARGE', 'OUT_OF_SERVICE', valencia_station_id),
            (4, 'EXTRA_LARGE', 'OUT_OF_SERVICE', valencia_station_id),
            (1, 'SMALL', 'OUT_OF_SERVICE', sevilla_station_id),
            (2, 'MEDIUM', 'OUT_OF_SERVICE', sevilla_station_id),
            (3, 'LARGE', 'OUT_OF_SERVICE', sevilla_station_id),
            (4, 'EXTRA_LARGE', 'OUT_OF_SERVICE', sevilla_station_id);
    END IF;
END $$;
