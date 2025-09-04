## Docker commands
```
docker compose -f docker-compose-kafka.yaml up -d
docker compose -f docker-compose-kafka.yaml down
docker compose -f docker-compose-kafka.yaml ps
```

## Kafka Commands - host machine - from command line
```
// Create topic
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topic1 --partitions 3 --replication-factor 3 --bootstrap-server host.docker.internal:9092 --config min.insync.replicas=2

// List topics
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-topics.sh --list --bootstrap-server host.docker.internal:9092

// Describe topics
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-topics.sh --describe --bootstrap-server host.docker.internal:9092

// Update config
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-configs.sh --bootstrap-server host.docker.internal:9092 --alter --entity-type topics --entity-name topic1 --add-config min.insync.replicas=2

// Delete Topic
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-topics.sh --delete --topic topic1 --bootstrap-server host.docker.internal:9092

// Produce a message / plain string
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-console-producer.sh --bootstrap-server host.docker.internal:9092 --topic topic1

// Produce a message / Key:value
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-console-producer.sh --bootstrap-server host.docker.internal:9092 --topic topic1 --property "parse.key=true" --property "key.separator=:"

// Read messages from beginning
docker compose -f docker-compose-kafka.yaml exec kafka-server-1 /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server host.docker.internal:9092 --topic topic1 --from-beginning --property print.key=true
```

## Kafka Commands - inside docker container
Go to inside the docker container using Docker Desktop or use the command line

# Go to the following directory
``` cd /opt/bitnami/kafka/bin ```
# List all topics
``` ./kafka-topics.sh --create --topic topic1 --bootstrap-server host.docker.internal:9092 ```
# List topics
``` ./kafka-topics.sh --list --bootstrap-server host.docker.internal:9092 ```