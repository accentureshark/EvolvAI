package org.shark.evolvai.inference.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.inference.controller.EmbeddingMatchDto;

public class EnrichmentUtil {

    public static String smartEnrichQuery(String originalQuery, List<EmbeddingMatchDto> matches) {
        String normalized = originalQuery.toLowerCase(Locale.ROOT);

        String nivel = inferFromText(normalized, "level\\s*(\\d+)");
        String area = matches.stream()
                .flatMap(m -> m.getMetadata().entrySet().stream())
                .filter(e -> e.getKey().equalsIgnoreCase("area"))
                .map(Map.Entry::getValue)
                .map(Object::toString)
                .distinct()
                .filter(a -> normalized.contains(a.toLowerCase()))
                .findFirst()
                .orElse(null);

        Map<String, Object> variables = new HashMap<>();
        variables.put("texto", originalQuery);
        if (nivel != null) variables.put("nivel", nivel);
        if (area != null) variables.put("area", area);

        Optional<String> enrichPattern = matches.stream()
                .map(EmbeddingMatchDto::getMetadata)
                .map(m -> safeCast(m.get("estructuraContent"), Map.class))
                .filter(Objects::nonNull)
                .map(m -> m.get("enrichText"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .findFirst();

        return enrichPattern.map(
            pattern -> replaceVariables(pattern, variables))
            .orElse(originalQuery
            );
    }

    public static String rebuildContextFromMatches(List<EmbeddingMatchDto> matches) {
        return matches.stream()
                .map(match -> {
                    Map<String, Object> metadata = match.getMetadata();
                    Map<String, Object> estructura = safeCast(
                        metadata.get("estructuraContent"),
                        Map.class
                    );
                    String enrich = estructura != null
                        ? Objects.toString(estructura.get("enrichText"), "{texto}")
                        : "{texto}";
                    return replaceVariables(enrich, buildVariablesMap(metadata, match.getText()));
                })
                .collect(Collectors.joining("\n"));
    }

    private static Map<String, Object> buildVariablesMap(
        Map<String, Object> metadata, String texto
    ) {
        Map<String, Object> variables = new HashMap<>(metadata);
        variables.put("texto", texto);
        return variables;
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    public static Map<String, Object> extractMetadata(EmbeddingMatch<String> match) {
        try {
            Field field = match.getClass().getDeclaredField("metadata");
            field.setAccessible(true);
            Object rawMetadata = field.get(match);
            if (rawMetadata instanceof Map<?, ?>) {
                return (Map<String, Object>) rawMetadata;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Log o manejo si es necesario
        }
        return Collections.emptyMap();
    }

    private static String inferFromText(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String replaceVariables(String pattern, Map<String, Object> variables) {
        String result = pattern;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return result;
    }

    private static <T> T safeCast(Object obj, Class<T> clazz) {
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }
        return null;
    }
}
