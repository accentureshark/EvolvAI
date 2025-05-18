DO $$
    BEGIN
        RAISE NOTICE 'Creando esquema y tablas del módulo admin...';
    END
$$;

DROP SCHEMA IF EXISTS admin CASCADE;

CREATE SCHEMA IF NOT EXISTS admin;

CREATE TABLE IF NOT EXISTS admin.person (
                                            id UUID PRIMARY KEY,
                                            display_name TEXT NOT NULL,
                                            type TEXT NOT NULL,
                                            first_name TEXT,
                                            last_name TEXT,
                                            national_id TEXT,
                                            birth_date DATE,
                                            business_name TEXT,
                                            tax_id TEXT,
                                            registration_number TEXT
);

CREATE TABLE IF NOT EXISTS admin.organization (
                                                  id UUID PRIMARY KEY,
                                                  name TEXT NOT NULL,
                                                  business_name TEXT,
                                                  tax_id TEXT,
                                                  registration_number TEXT,
                                                  CONSTRAINT fk_org_person FOREIGN KEY (id) REFERENCES admin.person(id)
);

CREATE TABLE IF NOT EXISTS admin.area (
                                          id UUID PRIMARY KEY,
                                          name TEXT NOT NULL,
                                          organization_id UUID NOT NULL,
                                          CONSTRAINT fk_area_org FOREIGN KEY (organization_id) REFERENCES admin.organization(id)
);

CREATE TABLE IF NOT EXISTS admin.role (
                                          id UUID PRIMARY KEY,
                                          name TEXT NOT NULL,
                                          area_id UUID,
                                          CONSTRAINT fk_role_area FOREIGN KEY (area_id) REFERENCES admin.area(id)
);

CREATE TABLE IF NOT EXISTS admin.user (
                                          id UUID PRIMARY KEY,
                                          email TEXT NOT NULL UNIQUE,
                                          password TEXT NOT NULL,
                                          active BOOLEAN,
                                          role_id UUID,
                                          person_id UUID UNIQUE,
                                          CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES admin.role(id),
                                          CONSTRAINT fk_user_person FOREIGN KEY (person_id) REFERENCES admin.person(id)
);

CREATE TABLE IF NOT EXISTS admin.area_assignment (
                                                     id UUID PRIMARY KEY,
                                                     area_id UUID NOT NULL,
                                                     person_id UUID NOT NULL,
                                                     assigned_at DATE NOT NULL,
                                                     local_role TEXT,
                                                     CONSTRAINT fk_assignment_area FOREIGN KEY (area_id) REFERENCES admin.area(id),
                                                     CONSTRAINT fk_assignment_person FOREIGN KEY (person_id) REFERENCES admin.person(id),
                                                     CONSTRAINT uc_area_person UNIQUE (area_id, person_id)
);

CREATE TABLE IF NOT EXISTS admin.embedding_source (
                                                      id UUID PRIMARY KEY,
                                                      name TEXT NOT NULL,
                                                      description TEXT,
                                                      path TEXT
);

CREATE TABLE IF NOT EXISTS admin.prompt (
                                            id UUID PRIMARY KEY,
                                            title TEXT NOT NULL,
                                            description TEXT,
                                            version TEXT
);

CREATE TABLE IF NOT EXISTS admin.prompt_message (
                                                    id UUID PRIMARY KEY,
                                                    role TEXT NOT NULL,
                                                    content TEXT NOT NULL,
                                                    prompt_id UUID NOT NULL,
                                                    CONSTRAINT fk_prompt_message_prompt FOREIGN KEY (prompt_id) REFERENCES admin.prompt(id)
);

CREATE TABLE IF NOT EXISTS admin.inference_configuration (
                                                             id UUID PRIMARY KEY,
                                                             context_type TEXT NOT NULL,
                                                             context_id UUID NOT NULL,
                                                             ai_role TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS admin.inference_configuration_prompt (
                                                                    config_id UUID NOT NULL,
                                                                    prompt_id UUID NOT NULL,
                                                                    PRIMARY KEY (config_id, prompt_id),
                                                                    CONSTRAINT fk_icp_config FOREIGN KEY (config_id) REFERENCES admin.inference_configuration(id),
                                                                    CONSTRAINT fk_icp_prompt FOREIGN KEY (prompt_id) REFERENCES admin.prompt(id)
);

CREATE TABLE IF NOT EXISTS admin.inference_configuration_embedding_source (
                                                                              config_id UUID NOT NULL,
                                                                              source_id UUID NOT NULL,
                                                                              PRIMARY KEY (config_id, source_id),
                                                                              CONSTRAINT fk_ice_config FOREIGN KEY (config_id) REFERENCES admin.inference_configuration(id),
                                                                              CONSTRAINT fk_ice_source FOREIGN KEY (source_id) REFERENCES admin.embedding_source(id)
);