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
	@rm -f cloudflared.log
	@($(CLOUDFLARED_BIN) tunnel --url http://localhost:$(CLOUDFLARE_PORT) > cloudflared.log 2>&1 & \
	  CLOUDFLARED_PID=$$!; \
	  echo "Lanzando cloudflared en background (PID: $$CLOUDFLARED_PID)..."; \
	  for i in $$(seq 1 30); do \
	    url=$$(grep -Eo "https://[a-zA-Z0-9\-]+\.trycloudflare\.com" cloudflared.log | head -1); \
	    if [ -n "$$url" ]; then \
	      echo "URL capturada: $$url/chat.html"; \
	      if command -v xdg-open >/dev/null 2>&1; then \
	        xdg-open "$$url/chat.html"; \
	      elif command -v open >/dev/null 2>&1; then \
	        open "$$url/chat.html"; \
	      else \
	        echo "Por favor, abrí manualmente: $$url/chat.html"; \
	      fi; \
	      break; \
	    fi; \
	    sleep 1; \
	  done; \
	  echo "Cloudflared corriendo en segundo plano (PID: $$CLOUDFLARED_PID)"; \
	  echo "Podés cerrarlo ejecutando: kill $$CLOUDFLARED_PID"; \
	)

.PHONY: cloudflared-swagger
cloudflared-swagger:
	@rm -f cloudflared-swagger.log
	@($(CLOUDFLARED_BIN) tunnel --url http://localhost:8081 > cloudflared-swagger.log 2>&1 & \
	  CLOUDFLARED_PID=$$!; \
	  echo "Lanzando cloudflared para backend en background (PID: $$CLOUDFLARED_PID)..."; \
	  for i in $$(seq 1 30); do \
	    url=$$(grep -Eo "https://[a-zA-Z0-9\-]+\.trycloudflare\.com" cloudflared-swagger.log | head -1); \
	    if [ -n "$$url" ]; then \
	      echo "URL capturada: $$url/swagger-ui/index.html"; \
	      if command -v xdg-open >/dev/null 2>&1; then \
	        xdg-open "$$url/swagger-ui/index.html"; \
	      elif command -v open >/dev/null 2>&1; then \
	        open "$$url/swagger-ui/index.html"; \
	      else \
	        echo "Por favor, abrí manualmente: $$url/swagger-ui/index.html"; \
	      fi; \
	      break; \
	    fi; \
	    sleep 1; \
	  done; \
	  echo "Cloudflared corriendo en segundo plano (PID: $$CLOUDFLARED_PID)"; \
	  echo "Podés cerrarlo ejecutando: kill $$CLOUDFLARED_PID"; \
	)
