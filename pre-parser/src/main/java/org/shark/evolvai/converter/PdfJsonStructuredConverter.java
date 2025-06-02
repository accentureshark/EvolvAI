package org.shark.evolvai.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

public class PdfJsonStructuredConverter {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Uso: java PdfJsonStructuredConverter <archivo.pdf|.txt> <estructura.json> <salida.json>");
            System.exit(1);
        }

        String inputPath = args[0];
        String schemaPath = args[1];
        String outputPath = args[2];

        String documentId = Paths.get(outputPath).getFileName().toString();

        String text = extractText(inputPath);
        System.out.println("Texto extraído (primeros 500 caracteres):\n" + text.substring(0, Math.min(500, text.length())));

        Map<String, Object> output = parseStructuredJson(text, schemaPath, documentId, inputPath);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), output);

        System.out.println("JSON generado en: " + outputPath);
    }

    private static String extractText(String path) throws IOException {
        if (path.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(new File(path))) {
                return new PDFTextStripper().getText(doc);
            }
        } else {
            return Files.readString(Paths.get(path));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseStructuredJson(String text, String schemaPath, String documentId, String originalFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> schema = mapper.readValue(new File(schemaPath), new TypeReference<>() {});

        Map<String, Object> nivelConfig = (Map<String, Object>) schema.get("nivelRegex");
        Pattern nivelPattern = Pattern.compile("^\\s*LEVEL\\s+(\\d+)\\s*:?\\s*$", Pattern.CASE_INSENSITIVE);
        String nivelKey = (String) nivelConfig.get("key");

        Map<String, Object> itemConfig = (Map<String, Object>) schema.get("itemRegex");
        Pattern itemPattern = Pattern.compile((String) itemConfig.get("pattern"));
        String itemKey = (String) itemConfig.get("key");

        List<Map<String, Object>> secciones = (List<Map<String, Object>>) schema.get("secciones");

        String enrichTemplate = (String) schema.getOrDefault("enrichText", "{texto}");

        List<Map<String, Object>> chunks = new ArrayList<>();
        String currentNivel = null;
        Map<String, String> currentMetadata = null;

        int charCount = 0;
        int chunkIndex = 0;

        for (String line : text.split("\\R")) {
            // Normalizar y eliminar caracteres invisibles
            line = Normalizer.normalize(line, Normalizer.Form.NFKC).replaceAll("\\p{C}", "").trim();
            if (line.isEmpty()) continue;

            System.out.println("Procesando línea: '" + line + "'");

            Matcher nivelMatcher = nivelPattern.matcher(line);
            if (nivelMatcher.matches()) {
                currentNivel = nivelMatcher.group(1);
                // No reseteamos currentMetadata para mantener la sección activa
                charCount = 0; // reset contador por nivel
                System.out.println("Detectado nivel: " + currentNivel);
                continue;
            }

            boolean matchedSection = false;
            for (Map<String, Object> sec : secciones) {
                Pattern sectionPattern = Pattern.compile((String) sec.get("regex"), Pattern.CASE_INSENSITIVE);
                if (sectionPattern.matcher(line).find()) {
                    currentMetadata = new HashMap<>((Map<String, String>) sec.get("metadata"));
                    matchedSection = true;
                    break;
                }
            }
            // Si no encuentra nueva sección, mantiene currentMetadata igual

            Matcher itemMatcher = itemPattern.matcher(line);
            if (itemMatcher.find() && currentNivel != null && currentMetadata != null) {
                String originalText = itemMatcher.group(1).trim();
                int charStart = charCount;
                int charEnd = charStart + originalText.length();

                Map<String, String> context = new HashMap<>(currentMetadata);
                context.put(nivelKey, currentNivel);
                context.put("texto", originalText);
                context.put("chunkIndex", String.valueOf(chunkIndex));

                String enrichedText = enrichTemplate;
                for (Map.Entry<String, String> entry : context.entrySet()) {
                    enrichedText = enrichedText.replace("{" + entry.getKey() + "}", entry.getValue());
                }

                Map<String, Object> chunk = new HashMap<>(currentMetadata);
                chunk.put("documentId", documentId);
                chunk.put("section", "fragment-" + chunkIndex);
                chunk.put("chunkIndex", chunkIndex);
                chunk.put("charStart", charStart);
                chunk.put("charEnd", charEnd);
                chunk.put(nivelKey, currentNivel);
                chunk.put(itemKey, enrichedText);
                chunk.put("estructuraId", Paths.get(schemaPath).getFileName().toString());
                chunk.put("timestamp", LocalDateTime.now().toString());

                chunks.add(chunk);

                System.out.println("Chunk agregado: nivel=" + currentNivel + ", area=" + currentMetadata.get("area") + ", texto='" + originalText + "'");

                charCount = charEnd + 1;
                chunkIndex++;
            }
        }

        String estructuraRaw = Files.readString(Paths.get(schemaPath));

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", documentId);
        metadata.put("estructuraId", Paths.get(schemaPath).getFileName().toString());
        metadata.put("timestamp", LocalDateTime.now().toString());
        metadata.put("originalFile", originalFilePath);
        metadata.put("uuid", UUID.randomUUID().toString());
        metadata.put("estructuraContent", estructuraRaw);

        result.put("metadata", metadata);
        result.put("data", chunks);

        return result;
    }
}
