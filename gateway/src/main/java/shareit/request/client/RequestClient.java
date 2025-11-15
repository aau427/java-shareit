package shareit.request.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import shareit.client.BaseClient;
import shareit.common.Common;
import shareit.request.dto.ItemRequestInDto;

@Service
public class RequestClient extends BaseClient {

    public RequestClient(@Value("${shareit-server.url}") String url,
                         RestTemplateBuilder restBuilder) {
        super(restBuilder.uriTemplateHandler(new DefaultUriBuilderFactory(url + Common.REQUEST_API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> createRequest(long userId, ItemRequestInDto itemRequestDto) {
        return post("", userId, itemRequestDto);
    }

    public ResponseEntity<Object> getRequestsByUser(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getItemRequestDtoById(long requestId, long userId) {
        return get("/" + requestId, userId);
    }

    public ResponseEntity<Object> getAll(long userId) {
        return get("/all", userId);
    }
}
