package org.shark.evolvai.integration;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class McpEvolvaiIntegrationTest {

    static final String MCP_SERVER_URL = "http://localhost:8090";
    static final String EVOLVAI_URL = "http://localhost:8081/api/inference/query";

    @Test
    void preguntaNaturalALaIA_yRespondeDesdeMelian() {
        // 1. Traer metadata real de Melian
        Response mcpMeta = RestAssured
                .given()
                .when()
                .get(MCP_SERVER_URL + "/mcp/metadata/short")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // ¡Hamcrest Matcher para chequear que hay tablas!
        assertThat(mcpMeta.jsonPath().getList("tables"), is(not(empty())));

        // 2. Elegir tabla de prueba
        String tabla = "actor";
        int limit = 3;

        // 3. Armar el request natural a EvolvAI con source MCP_SERVER
        Map<String, Object> payload = Map.of(
                "query", "Dame todos los actores",
                "conversationId", "test-integration-123",
                "customPrompt", "",
                "source", Map.of(
                        "type", "MCP_SERVER",
                        "id", MCP_SERVER_URL,
                        "params", Map.of(
                                "endpoint", "/mcp/chunks",
                                "table", tabla,
                                "limit", limit
                        )
                )
        );

        // 4. Ejecutar POST a EvolvAI y validar la respuesta
        Response evoResp = RestAssured
                .given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post(EVOLVAI_URL)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String answer = evoResp.jsonPath().getString("answer");
        System.out.println("Respuesta IA:\n" + answer);

        // Validación: la respuesta no debe estar vacía
        assertThat(answer, not(is(emptyOrNullString())));
        // Si querés buscar algún nombre:
        // assertThat(answer.toLowerCase(), containsString("penelope"));
    }
}
