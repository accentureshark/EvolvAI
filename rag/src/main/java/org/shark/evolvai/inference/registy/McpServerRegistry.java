package org.shark.evolvai.inference.registy;

import org.shark.evolvai.config.RagProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry to resolve MCP server configuration by ID (sourceId).
 * Allows dynamic lookup of multiple MCP backends.
 */
@Component
public class McpServerRegistry {

    private final Map<String, RagProperties.Mcp> mcpMap;

    public McpServerRegistry(RagProperties ragProperties) {
        // Index MCP servers by their unique ID
        this.mcpMap = ragProperties.getMcps().stream()
                .collect(java.util.stream.Collectors.toMap(
                        RagProperties.Mcp::getId,
                        mcp -> mcp
                ));
    }

    /**
     * Resolve the MCP configuration for a given ID.
     * @param id sourceId (e.g., "mcp-tecnologia")
     * @return Optional MCP config
     */
    public Optional<RagProperties.Mcp> resolve(String id) {
        return Optional.ofNullable(mcpMap.get(id));
    }

    /**
     * Returns all configured MCP servers.
     */
    public List<RagProperties.Mcp> all() {
        return List.copyOf(mcpMap.values());
    }
}
