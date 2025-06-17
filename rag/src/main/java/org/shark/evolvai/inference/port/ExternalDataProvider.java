package org.shark.evolvai.inference.port;

import dev.langchain4j.data.segment.TextSegment;
import java.util.List;
import java.util.Map;

public interface ExternalDataProvider {
    List<TextSegment> fetch(String query, String mcpServerUrl, Map<String, Object> params);

    // Para metadata: que sea genérico, sin DTO, por ejemplo:
    Map<String, Object> fetchMetadata(String mcpServerUrl);
}
