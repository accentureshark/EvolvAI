package org.shark.evolvai.inference.util;

import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.shark.evolvai.inference.controller.EmbeddingMatchDto;

public class EnrichmentUtil {

    public static String smartEnrichQuery(String query, List<EmbeddingMatchDto> matches) {
        // Implementación básica: concatena el contexto de los matches al query
        StringBuilder sb = new StringBuilder(query);
        for (EmbeddingMatchDto match : matches) {
            sb.append(" ").append(match.getText());
        }
        return sb.toString();
    }

    public static String rebuildContextFromMatches(List<EmbeddingMatchDto> matches) {
        return matches.stream()
                .map(match -> {
                    Map<String, Object> metadata = match.getMetadata();
                    Map<String, Object> estructura = safeCast(metadata.get("estructuraContent"), Map.class);
                    String enrich = estructura != null ? Objects.toString(estructura.get("enrichText"), "{texto}") : "{texto}";
                    return replaceVariables(enrich, buildVariablesMap(metadata, match.getText()));
                })
                .collect(Collectors.joining("\n"));
    }

    private static Map<String, Object> buildVariablesMap(Map<String, Object> metadata, String texto) {
        Map<String, Object> variables = new HashMap<>(metadata);
        variables.put("texto", texto);
        return variables;
    }

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
