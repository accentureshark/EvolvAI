package org.shark.evolvai.inference.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceInfo {
    private SourceType type;            // VECTOR_DB o MCP_SERVER
    private String id;                  // documentId, connectorId, etc.
    private Map<String, Object> params; // cualquier parámetro extra
}