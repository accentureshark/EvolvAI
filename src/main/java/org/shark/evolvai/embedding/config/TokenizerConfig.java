package org.shark.evolvai.embedding.config;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(TokenizerProperties.class)
@Data
public class TokenizerConfig {

    private static final Logger logger = LoggerFactory.getLogger(TokenizerConfig.class);
    private String loadedPath;
    private int maxLength;

    @Bean
    public HuggingFaceTokenizer huggingFaceTokenizer(TokenizerProperties properties,
                                                     ResourceLoader resourceLoader) throws IOException {
        String rawPath = properties.getPath();
        logger.info("Loading tokenizer from path: {}", rawPath);

        Path tokenizerPath;
        if (rawPath.startsWith("classpath:")) {
            Resource resource = resourceLoader.getResource(rawPath);
            if (!resource.exists()) {
                logger.error("Tokenizer resource not found at classpath: {}", rawPath);
                throw new FileNotFoundException("Tokenizer resource not found at path: " + rawPath);
            }

            File tempFile = File.createTempFile("tokenizer", ".json");
            tempFile.deleteOnExit();
            try (InputStream in = resource.getInputStream(); OutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            tokenizerPath = tempFile.toPath();
        } else {
            tokenizerPath = Paths.get(rawPath);
        }

        logger.info("Resolved tokenizer.json absolute path: {}", tokenizerPath.toAbsolutePath());

        // Agregado: validación de contenido antes de crear el tokenizer
        String content = Files.readString(tokenizerPath);
        if (content == null || content.trim().isEmpty()) {
            throw new IOException("Tokenizer file is empty or unreadable at path: " + tokenizerPath);
        }

        logger.info("Tokenizer file content (first 200 chars): {}", content.substring(0, Math.min(content.length(), 200)));

        try {
            return HuggingFaceTokenizer.newInstance(tokenizerPath);
        } catch (Exception e) {
            logger.error("Error creating HuggingFaceTokenizer from path: {}", tokenizerPath, e);
            throw e;
        }
    }
}