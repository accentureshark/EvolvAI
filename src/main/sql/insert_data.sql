-- Personas jurídicas (organizaciones)
INSERT INTO admin.person (id, display_name, type, business_name, tax_id, registration_number)
VALUES ('229eea43-39b8-49d5-88c0-7dfb31d874c1', 'SharkTech', 'LEGAL', 'SharkTech Inc', '30-12345678-9', 'ST123'),
       ('8f657a3d-5a0b-43f5-b2c8-60876385e3c3', 'AgueroCorp', 'LEGAL', 'Aguero Corp', '30-98765432-1', 'AC987');

INSERT INTO admin.organization (id, name, business_name, tax_id, registration_number)
VALUES ('229eea43-39b8-49d5-88c0-7dfb31d874c1', 'SharkTech', 'SharkTech Inc', '30-12345678-9', 'ST123'),
       ('8f657a3d-5a0b-43f5-b2c8-60876385e3c3', 'AgueroCorp', 'Aguero Corp', '30-98765432-1', 'AC987');

-- Personas físicas
INSERT INTO admin.person (id, display_name, type, first_name, last_name, national_id, birth_date)
VALUES ('7adf9146-2bc2-4756-b8a4-fa2704b4a5b5', 'Fabián Agüero', 'NATURAL', 'Fabián', 'Agüero', '12345678',
        '1985-06-15'),
       ('d4f56b31-32dd-45c4-9208-61023e4a38e3', 'Lucía Pérez', 'NATURAL', 'Lucía', 'Pérez', '87654321', '1990-12-05');

-- Áreas
INSERT INTO admin.area (id, name, organization_id)
VALUES ('04ae9a81-6474-46b8-a5d3-3a2685d7be34', 'AI Research', '229eea43-39b8-49d5-88c0-7dfb31d874c1'),
       ('a9d2c91b-67ed-4ca8-8adb-352ab6d4629f', 'Blockchain', '229eea43-39b8-49d5-88c0-7dfb31d874c1'),
       ('1f35d42a-975c-4d33-9c5b-825cf319ac27', 'Innovation Lab', '8f657a3d-5a0b-43f5-b2c8-60876385e3c3');

-- Roles
INSERT INTO admin.role (id, name)
VALUES ('421c79bc-8568-4a18-8045-8f8dc71960ab', 'Developer'),
       ('79d00a63-2b07-41e5-b22a-7e940377a0f8', 'Data Scientist'),
       ('c7ac928d-ad29-412c-860a-e456b530946f', 'Team Lead');

-- Usuarios
INSERT INTO admin.user (id, email, password, active, role_id, person_id)
VALUES ('cf4f0881-c521-468f-a016-9a9c65e16f09', 'fabian@example.com', 'pass123', TRUE,
        '421c79bc-8568-4a18-8045-8f8dc71960ab', '7adf9146-2bc2-4756-b8a4-fa2704b4a5b5'),
       ('fb14bb2b-53fa-4169-b23d-e14df48204ce', 'lucia@example.com', 'pass456', TRUE,
        '79d00a63-2b07-41e5-b22a-7e940377a0f8', 'd4f56b31-32dd-45c4-9208-61023e4a38e3');

-- Asignaciones de área
INSERT INTO admin.area_assignment (id, assigned_at, local_role, area_id, person_id)
VALUES ('8644db4a-2fd4-49f2-b91e-0539a1d9075d', '2023-01-10', 'Lead', '04ae9a81-6474-46b8-a5d3-3a2685d7be34',
        '7adf9146-2bc2-4756-b8a4-fa2704b4a5b5'),
       ('40c51055-c864-40d3-b80f-ff1ca4afa721', '2024-03-22', 'Researcher', '1f35d42a-975c-4d33-9c5b-825cf319ac27',
        'd4f56b31-32dd-45c4-9208-61023e4a38e3');

-- Fuentes de embedding
INSERT INTO admin.embedding_source (id, name, description, path)
VALUES ('ca7f35af-9da9-40ac-a172-d855d7fab433', 'AI Whitepaper', 'Core documentation for AI use', '/docs/ai.pdf'),
       ('3df56e5d-dac8-4aa2-8faf-27ce32264f17', 'Blockchain Spec', 'Technical spec', '/docs/blockchain.pdf');

-- Prompts
INSERT INTO admin.prompt (id, title)
VALUES ('ca93a320-9c20-4f38-b502-7b2970c9232b', 'Summarize');

-- Mensajes de prompt
INSERT INTO admin.prompt_message (id, role, content, prompt_id)
VALUES ('b6f8c185-8fe2-4adb-93b0-93efea85c06d', 'SYSTEM', 'You are a helpful assistant',
        'ca93a320-9c20-4f38-b502-7b2970c9232b'),
       ('41fbd8aa-0652-4c6d-a837-86a7a320680c', 'USER', 'Summarize the following text: {text}',
        'ca93a320-9c20-4f38-b502-7b2970c9232b');

-- Configuraciones de inferencia
INSERT INTO admin.inference_configuration (id, context_type, context_id, ai_role)
VALUES ('996c3ed6-d13d-4bb8-88c7-d3b4195329b0', 'USER', 'cf4f0881-c521-468f-a016-9a9c65e16f09', 'ASSISTANT'),
       ('3684ee68-6c06-4a07-9c25-96b19b208dc8', 'AREA', '1f35d42a-975c-4d33-9c5b-825cf319ac27', 'AUDITOR');

-- Relación muchos a muchos: configuración <-> prompt
INSERT INTO admin.inference_configuration_prompt (config_id, prompt_id)
VALUES ('996c3ed6-d13d-4bb8-88c7-d3b4195329b0', 'ca93a320-9c20-4f38-b502-7b2970c9232b'),
       ('3684ee68-6c06-4a07-9c25-96b19b208dc8', 'ca93a320-9c20-4f38-b502-7b2970c9232b');

-- Relación muchos a muchos: configuración <-> embedding_source
INSERT INTO admin.inference_configuration_embedding_source (config_id, source_id)
VALUES ('996c3ed6-d13d-4bb8-88c7-d3b4195329b0', 'ca7f35af-9da9-40ac-a172-d855d7fab433'),
       ('3684ee68-6c06-4a07-9c25-96b19b208dc8', '3df56e5d-dac8-4aa2-8faf-27ce32264f17');