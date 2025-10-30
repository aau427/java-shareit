package shareit.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Common {
    public static final String USER_API_PREFIX = "/users";

    public static final String USER_HEADER = "X-Sharer-User-Id";
    public static final String ITEM_API_PREFIX = "/items";
    public static final String REQUEST_API_PREFIX = "/requests";
    public static final String BOOKING_API_PREFIX = "/bookings";

    public static LocalDateTime getLocalDateTime() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow"));
    }
}
