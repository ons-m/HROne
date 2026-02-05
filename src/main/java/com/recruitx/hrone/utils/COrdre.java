package com.recruitx.hrone.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class COrdre {

    /* ===============================
       NumOrdre reference (epoch)
       =============================== */

    // 01/01/2020 00:00:00 UTC
    private static final LocalDateTime DATE_PIVOT =
            LocalDateTime.of(2020, 1, 1, 0, 0, 0);

    private static final long DATE_PIVOT_EPOCH_SECONDS =
            DATE_PIVOT.toEpochSecond(ZoneOffset.UTC);

    /* ===============================
       Durations (seconds)
       =============================== */

    public static final int DAY_DURATION   = 86400;          // 86_400
    public static final int MONTH_DURATION = 2592000;    // fixed 30 days
    public static final int YEAR_DURATION  = 31536000;   // fixed 365 days

    private COrdre() {
        // Prevent instantiation
    }

    /* ===============================
       Now helpers
       =============================== */

    /**
     * Returns current time as NumOrdre (UTC).
     */
    public static long GetNumOrdreNow() {
        return Instant.now().getEpochSecond() - DATE_PIVOT_EPOCH_SECONDS;
    }

    /**
     * Returns current UTC datetime.
     */
    public static LocalDateTime GetNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /* ===============================
       Date -> NumOrdre
       =============================== */

    /**
     * Converts a LocalDateTime (UTC) to NumOrdre.
     */
    public static long GetNumOrdreFromDate(LocalDateTime dateTime) {
        long epochSeconds = dateTime.toEpochSecond(ZoneOffset.UTC);
        return epochSeconds - DATE_PIVOT_EPOCH_SECONDS;
    }

    /**
     * Converts explicit date components to NumOrdre.
     */
    public static long GetNumOrdreFromDate(
            int day,
            int month,
            int year,
            int hour,
            int minute,
            int second) {

        LocalDateTime dateTime =
                LocalDateTime.of(year, month, day, hour, minute, second);

        return GetNumOrdreFromDate(dateTime);
    }

    /* ===============================
       NumOrdre -> Date
       =============================== */

    /**
     * Converts NumOrdre back to LocalDateTime (UTC).
     */
    public static LocalDateTime GetDateFromNumOrdre(long numOrdre) {
        long epochSeconds = DATE_PIVOT_EPOCH_SECONDS + numOrdre;
        return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);
    }
}
