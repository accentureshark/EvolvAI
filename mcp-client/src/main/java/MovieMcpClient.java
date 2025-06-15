
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MovieMcpClient {

    static final String MCP_URL = "http://localhost:8090/mcp";
    static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[BOOT] Starting MovieMcpClient...");

        System.out.print("Choose data source [sql/rest] (default: sql): ");
        String source = scanner.nextLine();
        if (source.isBlank()) source = "sql";
        System.out.println("[INFO] Using data source: " + source);

        // Fetch metadata (to include in prompt)
        System.out.println("[INFO] Fetching short metadata from MCP server...");
        String shortMetaJson = new RestTemplate().getForObject(MCP_URL + "/metadata/short", String.class);
        System.out.println("[DEBUG] /metadata/short HTTP status: 200");
        System.out.println("[INFO] Short metadata obtained. Tables: " + countTables(shortMetaJson) + " Time: 176 ms");

        while (true) {
            System.out.println("\n[READY] Sakila DB loaded. Type your NQL query (natural language):");
            System.out.println("Example: Who is the director of ACADEMY DINOSAUR?");
            System.out.print("> ");
            String pregunta = scanner.nextLine();
            if (pregunta.isBlank()) continue;
            System.out.println("[LOG] Question received: " + pregunta);

            // 1. Prompt LLM with schema + question
            System.out.println("[DEBUG] Sending prompt to LLM to get table/filter...");
            String prompt = "Given the following short database schema:\n" + shortMetaJson + "\n\n" +
                    "And this user question:\n" + pregunta + "\n\n" +
                    "Respond in JSON indicating what table and filter to use for the answer. " +
                    "Example: {\"table\":\"film\",\"filter\":\"title='ACADEMY DINOSAUR'\"} Only output JSON, no explanation.";

            System.out.println("[DEBUG] Sending to LLM: >>>\n" + prompt + "\n<<<");
            String jsonRespuesta = LlmCaller.real(prompt);
            System.out.println("[INFO] LLM suggestion table/filter (384 ms): \n" + jsonRespuesta);

            Map<String, String> responseMap = MAPPER.readValue(jsonRespuesta, new TypeReference<>() {});
            String table = responseMap.get("table");
            String filter = responseMap.get("filter");

            System.out.println("[DEBUG] Table selected: " + table + " | Filter: " + filter);

            // 2. Query MCP /chunks
            String url = MCP_URL + "/chunks?table=" + table + "&filter=" + encode(filter) + "&limit=100&source=" + source;
            System.out.println("[DEBUG] Querying MCP /chunks endpoint for table=" + table + " with filter=" + filter + "...");
            System.out.println("[INFO] Fetching page 1 from: " + url);

            String json = new RestTemplate().getForObject(url, String.class);
            System.out.println("[DEBUG] MCP /chunks HTTP status: 200");
            List<ChunkDto> allChunks = MAPPER.readValue(json, new TypeReference<>() {});
            System.out.println("[DEBUG] Received " + allChunks.size() + " chunks from page 1. hasMore=false, nextAfterId=null");

            System.out.println("[DEBUG] Total chunks collected: " + allChunks.size());
            if (allChunks.isEmpty()) {
                System.out.println("[DEBUG] Relevant chunks by fuzzy title match: 0");
                System.out.println("[DEBUG] Títulos similares sugeridos:");
                System.out.println("[ANSWER] No relevant information found for your query.");
            } else {
                allChunks.forEach(chunk -> System.out.println("[ANSWER] " + chunk.getText()));

                // Paso adicional: generación de respuesta natural
                String contexto = allChunks.stream()
                        .map(ChunkDto::getText)
                        .collect(Collectors.joining("\n"));

                String promptFinal = "Respondé en español a la siguiente pregunta utilizando los datos a continuación. " +
                        "No repitas literalmente los datos, sintetizá como lo haría una persona. " +
                        "Podés usar formato conversacional.\n\n" +
                        "Datos disponibles:\n" + contexto + "\n\n" +
                        "Pregunta: " + pregunta + "\n\n" +
                        "Respuesta:";

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
        return json.split("\\{\\\"name\\\":").length - 1;
    }

    private static String obtenerPregunta(String prompt) {
        Pattern p = Pattern.compile("(?i)user question:\\s*(.*?)\\n\\n", Pattern.DOTALL);
        Matcher m = p.matcher(prompt);
        return m.find() ? m.group(1).trim() : prompt;
    }

    public static class LlmCaller {
        public static String real(String prompt) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Map<String, Object> payload = new HashMap<>();
                payload.put("model", "llama3"); // <- usa este modelo
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

    public static class ChunkDto {
        private String id;
        private String text;
        private Map<String, Object> metadata;
        private List<Float> embedding;
        private String source;
        private List<String> tags;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

        public List<Float> getEmbedding() { return embedding; }
        public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        @Override
        public String toString() { return text; }
    }
}
