import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MovieMcpClient {

    static final String MCP_URL = "http://localhost:8090";
    static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[BOOT] Starting MovieMcpClient...");

        System.out.print("Choose data source [sql/rest] (default: sql): ");
        String source = scanner.nextLine();
        if (source.isBlank()) source = "sql";
        System.out.println("[INFO] Using data source: " + source);

        System.out.println("[INFO] Fetching schema metadata from MCP server...");
        String schemaJson = new RestTemplate().getForObject(MCP_URL + "/mcp/metadata/short?source=" + source, String.class);
        System.out.println("[DEBUG] /schema HTTP status: 200");
        System.out.println("[INFO] Schema metadata obtained. Tables: " + countTables(schemaJson));

        while (true) {
            System.out.println("\n[READY] Type your NQL query (natural language):");
            System.out.print("> ");
            String pregunta = scanner.nextLine();
            if (pregunta.isBlank()) continue;

            String prompt = "You are an expert in SQL-like reasoning and table relationships.\n\n" +
                    "Given the following database schema:\n" + schemaJson + "\n\n" +
                    "Based on the user's question: " + pregunta + ", respond with JSON like: {\"table\":\"film\", \"filter\":\"title LIKE '%Flashdance%'\"}\n\n" +
                    "Only output one single JSON object. Not a list. Not an array. Not wrapped in brackets.";

            String llmResponse = LlmCaller.real(prompt);
            Map<String, String> responseMap = MAPPER.readValue(llmResponse, new TypeReference<>() {});
            String table = responseMap.get("table");
            String filter = responseMap.get("filter");

            String url = MCP_URL + "/mcp/chunks?table=" + table + "&filter=" + encode(filter) + "&limit=100&source=" + source;
            System.out.println("[INFO] Fetching chunks from: " + url);

            String json = new RestTemplate().getForObject(url, String.class);
            List<Map<String, Object>> allChunks = MAPPER.readValue(json, new TypeReference<>() {});

            if (allChunks.isEmpty()) {
                System.out.println("[ANSWER] No relevant information found.");
            } else {
                allChunks.forEach(chunk -> System.out.println("[ANSWER] " + chunk.get("text")));

                String contexto = allChunks.stream()
                        .map(c -> (String) c.get("text"))
                        .collect(Collectors.joining("\n"));

                String promptFinal = "Respondé en español a la siguiente pregunta utilizando los datos:\n" +
                        contexto + "\n\nPregunta: " + pregunta + "\n\nRespuesta:";

                String respuestaFinal = LlmCaller.real(promptFinal);
                System.out.println("\n[FINAL ANSWER] " + respuestaFinal);
            }
        }
    }

    private static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    private static int countTables(String json) {
        return json.split("\\\\\"name\\\\\":").length - 1;
    }

    public static class LlmCaller {
        public static String real(String prompt) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Map<String, Object> payload = new HashMap<>();
                payload.put("model", "llama3");
                payload.put("prompt", prompt);
                payload.put("stream", false);

                String json = MAPPER.writeValueAsString(payload);

                String response = restTemplate.postForObject("http://localhost:11434/api/generate", json, String.class);
                Map<String, Object> map = MAPPER.readValue(response, new TypeReference<>() {});
                return (String) map.get("response");
            } catch (Exception e) {
                throw new RuntimeException("Failed to call LLM", e);
            }
        }
    }
}
