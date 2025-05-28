.PHONY: start build logs down ps

start:
	podman-compose up --build

build:
	podman-compose build

logs:
	podman-compose logs -f

down:
	podman-compose down -v

ps:
	podman-compose ps
