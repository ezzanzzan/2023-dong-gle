package org.donggle.backend.infrastructure.client.notion;

import com.fasterxml.jackson.databind.JsonNode;
import org.donggle.backend.domain.parser.notion.NotionBlockType;
import org.donggle.backend.infrastructure.client.exception.ClientException;
import org.donggle.backend.infrastructure.client.notion.dto.response.NotionBlockNodeResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class NotionApiClient {
    public static final String CHILD_BLOCK_URI = "/blocks/{blockId}/children";
    public static final String BLOCK_URI = "/blocks/{blockId}";
    public static final String AUTHORIZATION = "Authorization";
    public static final String NOTION_VERSION = "Notion-Version";
    public static final String BEARER = "Bearer ";
    public static final String API_VERSION = "2022-06-28";
    public static final String PLATFORM_NAME = "Notion";
    private static final String NOTION_URL = "https://api.notion.com/v1";

    private final WebClient webClient;

    public NotionApiClient() {
        this.webClient = WebClient.create(NOTION_URL);
    }

    public NotionBlockNodeResponse retrieveParentBlockNode(final String parentBlockId, final String notionToken) {
        return new NotionBlockNodeResponse(retrieveBlock(parentBlockId, notionToken), -1);
    }

    private JsonNode retrieveBlock(final String parentBlockId, final String notionToken) {
        return retrieveData(parentBlockId, BLOCK_URI, notionToken);
    }

    private JsonNode retrieveData(final String blockId, final String childBlockUri, final String notionToken) {
        return webClient.get()
                .uri(childBlockUri, blockId)
                .header(AUTHORIZATION, BEARER + notionToken)
                .header(NOTION_VERSION, API_VERSION)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> ClientException.handle4xxException(clientResponse.statusCode().value(), PLATFORM_NAME))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException(clientResponse.toString())))
                .bodyToMono(JsonNode.class)
                .block();
    }

    public List<NotionBlockNodeResponse> retrieveBodyBlockNodes(final NotionBlockNodeResponse parentNotionBlock, final String notionToken) {
        final List<NotionBlockNodeResponse> bodyBlockNodes = new ArrayList<>();
        final Deque<NotionBlockNodeResponse> notionBlockNodeResponseDeque = new ArrayDeque<>();
        notionBlockNodeResponseDeque.addFirst(parentNotionBlock);

        while (!notionBlockNodeResponseDeque.isEmpty()) {
            final NotionBlockNodeResponse notionBlockNodeResponse = notionBlockNodeResponseDeque.removeFirst();
            processNotionBlockNode(notionBlockNodeResponse, bodyBlockNodes, notionBlockNodeResponseDeque, notionToken);
        }

        return bodyBlockNodes;
    }

    public String findTitle(final NotionBlockNodeResponse parentBlockNode) {
        return parentBlockNode.getBlockProperties().get("title").asText();
    }

    private void processNotionBlockNode(
            final NotionBlockNodeResponse notionBlockNodeResponse,
            final List<NotionBlockNodeResponse> bodyBlockNodes,
            final Deque<NotionBlockNodeResponse> notionBlockNodeResponseDeque,
            final String notionToken
    ) {
        bodyBlockNodes.add(notionBlockNodeResponse);

        if (notionBlockNodeResponse.hasChildren()) {
            final List<JsonNode> childrenBlocks = retrieveChildrenBlocks(notionBlockNodeResponse.getId(), notionToken);
            processChildrenBlocks(childrenBlocks, notionBlockNodeResponse, notionBlockNodeResponseDeque);
        }
    }

    private List<JsonNode> retrieveChildrenBlocks(final String blockId, final String notionToken) {
        final List<JsonNode> childrenBlocks = new ArrayList<>();
        retrieveData(blockId, CHILD_BLOCK_URI, notionToken).withArray("results").elements().forEachRemaining(childrenBlocks::add);
        return childrenBlocks;
    }

    private void processChildrenBlocks(
            final List<JsonNode> childrenBlocks,
            final NotionBlockNodeResponse notionBlockNodeResponse,
            final Deque<NotionBlockNodeResponse> notionBlockNodeResponseDeque
    ) {
        final int lastIndex = childrenBlocks.size() - 1;
        for (int i = lastIndex; i >= 0; i--) {
            final JsonNode childBlock = childrenBlocks.get(i);
            if (notionBlockNodeResponse.getBlockType() == NotionBlockType.COLUMN || notionBlockNodeResponse.getBlockType() == NotionBlockType.COLUMN_LIST) {
                notionBlockNodeResponseDeque.addFirst(new NotionBlockNodeResponse(childBlock, 0));
                continue;
            }
            notionBlockNodeResponseDeque.addFirst(new NotionBlockNodeResponse(childBlock, notionBlockNodeResponse.depth() + 1));
        }
    }
}
