DOCKER_COMPOSE = docker compose

.PHONY: up
up:
	$(DOCKER_COMPOSE) up -d

.PHONY: down
down:
	$(DOCKER_COMPOSE) down

.PHONY: build
build:
	$(DOCKER_COMPOSE) build

.PHONY: logs
logs:
	$(DOCKER_COMPOSE) logs -f

.PHONY: ps
ps:
	$(DOCKER_COMPOSE) ps

.PHONY: restart
restart:
	$(DOCKER_COMPOSE) restart

.PHONY: stop
stop:
	$(DOCKER_COMPOSE) stop

.PHONY: rm
rm:
	$(DOCKER_COMPOSE) rm

# ========== CLOUDFLARE TUNNEL ==========

CLOUDFLARE_PORT ?= 8080
CLOUDFLARED_BIN ?= cloudflared


.PHONY: cloudflared
cloudflared:
	cloudflared tunnel run shark-tunnel



