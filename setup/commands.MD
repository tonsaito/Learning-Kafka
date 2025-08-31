## Docker commands
```
docker compose -f docker-compose-kafka.yaml up -d
docker compose -f docker-compose-kafka.yaml down
docker compose -f docker-compose-kafka.yaml ps
```

## Kafka Commands - host machine - from command line
```
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic test-topic --bootstrap-server host.docker.internal:9092
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-topics.sh --list --bootstrap-server host.docker.internal:9092
```

## Kafka Commands - inside docker container
Go to inside the docker container using Docker Desktop or use the command line

# Go to the following directory
``` cd /opt/bitnami/kafka/bin ```
# List all topics
``` ./kafka-topics.sh --create --topic test-topic --bootstrap-server host.docker.internal:9092 ```
# List topics
``` ./kafka-topics.sh --list --bootstrap-server host.docker.internal:9092 ```