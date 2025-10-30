package shareit.item.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import shareit.client.BaseClient;
import shareit.common.Common;
import shareit.item.dto.CommentDto;
import shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    public ItemClient(@Value("${shareit-server.url}") String url,
                      RestTemplateBuilder restBuilder) {
        super(restBuilder.uriTemplateHandler(new DefaultUriBuilderFactory(url + Common.ITEM_API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> createItem(ItemDto itemDto, long userId) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, ItemDto itemDto) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItemById(long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getUsersItems(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getItemsByContext(long userId, String context) {
        Map<String, Object> parameters = Map.of(
                "text", context
        );
        return get("/search?text={text}", userId, parameters);
    }

    public ResponseEntity<Object> addCommentToItem(long userId, long itemId, CommentDto commentDto) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }

}
