package shareit.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import shareit.booking.dto.BookingDto;
import shareit.client.BaseClient;
import shareit.common.Common;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    public BookingClient(@Value("${shareit-server.url}") String url,
                         RestTemplateBuilder restBuilder) {
        super(restBuilder.uriTemplateHandler(new DefaultUriBuilderFactory(url + Common.BOOKING_API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> createBooking(long userId, BookingDto bookingDto) {
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> updateBooking(long userId, long bookingId, Boolean isApprove) {
        return patch("/" + bookingId + "?approved=" + isApprove, userId);
    }

    public ResponseEntity<Object> getBookingById(long bookingId, long userId) {
        return get("/" + bookingId, userId);
    }


    public ResponseEntity<Object> getUsersBooking(long userId, String state) {
        Map<String, Object> parameters = Map.of(
                "state", state
        );
        return get("?state={state}", userId, parameters);
    }


    public ResponseEntity<Object> getBookingsForOwner(long userId, String state) {
        Map<String, Object> parameters = Map.of(
                "state", state
        );
        return get("/owner?state={state}", userId, parameters);
    }
}
