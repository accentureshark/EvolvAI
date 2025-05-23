#!/bin/bash

echo "🛑 Deteniendo y limpiando todos los contenedores..."
podman-compose -f docker-compose.base.yml -f docker-compose.prod.yml down
