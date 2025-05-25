package org.shark.evolvai.admin.model.inference;


public enum AiRole {

    ASSISTANT,       // Default helper or responder
    AUDITOR,         // Reviews or questions content
    GENERATOR,       // Creates reports, summaries, etc.
    REVIEWER,        // Validates or filters AI-generated output
    ANALYST,         // Interprets or contextualizes data
    OBSERVER         // Passive role, just consumes output
}

