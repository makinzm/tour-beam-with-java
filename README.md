# tour-beam-with-java


## SetUp

```shell
kubectl create namespace flink

kubectl config set-context --current --namespace=flink
```

### Flink

```shell
pushd k8s/flink

kubectl apply -f flink-configuration-configmap.yaml
kubectl apply -f jobmanager-service.yaml
kubectl apply -f jobmanager-session-deployment.yaml
kubectl apply -f taskmanager-session-deployment.yaml

popd
```

- Check UI

```shell
kill -9 $(lsof -i:8081)
kubectl port-forward deployment/flink-jobmanager 8081:8081
```

```shell
open http://localhost:8081
```

### Kafka

```shell
helm repo add bitnami https://charts.bitnami.com/bitnami

helm install kafka oci://registry-1.docker.io/bitnamicharts/kafka
```
- Decrease the number of replicas to make it easier to test in local environment.
```shell
helm upgrade kafka oci://registry-1.docker.io/bitnamicharts/kafka \
    --set controller.replicaCount=2
```

### Beam

```shell
pushd beam-app

docker run --rm -v $(pwd):/workspace -w /workspace gradle:7.6-jdk11 gradle build

popd
```

- Copy the jar file to the flink job manager pod.

```shell
FLINK_JOB_MANAGER_POD=$(kubectl get pod | grep flink-jobmanager | awk '{print $1}')
kubectl cp beam-app/build/libs/beam-app.jar $FLINK_JOB_MANAGER_POD:/opt/flink/lib/
```

### Run ( IN PROGRESS )

#### 00. Create topic

- Create a topic.
```shell
KAFKA_POD_IP=kafka.default.svc.cluster.local:9092

kubectl exec -it kafka-client -- kafka-topics.sh \
    --bootstrap-server $KAFKA_POD_IP \
    --create \
    --topic test-topic \
    --partitions 1 \
    --replication-factor 1

kubectl exec -it kafka-client -- kafka-topics.sh \
    --bootstrap-server $KAFKA_POD_IP \
    --create \
    --topic output-topic \
    --partitions 1 \
    --replication-factor 1
```
- TIMEOUT ERROR => Why...?

```shell

❯ KAFKA_POD_IP=kafka.default.svc.cluster.local:9092

kubectl exec -it kafka-client -- kafka-topics.sh \
    --bootstrap-server $KAFKA_POD_IP \
    --create \
    --topic test-topic \
    --partitions 1 \
    --replication-factor 1

kubectl exec -it kafka-client -- kafka-topics.sh \
    --bootstrap-server $KAFKA_POD_IP \
    --create \
    --topic output-topic \
    --partitions 1 \
    --replication-factor 1
Error while executing topic command : Call(callName=createTopics, deadlineMs=1734017108908, tries=50, nextAllowedTryMs=1734017109909) timed out at 1734017108909 after 50 attempt(s)
[2024-12-12 15:25:08,914] ERROR org.apache.kafka.common.errors.TimeoutException: Call(callName=createTopics, deadlineMs=1734017108908, tries=50, nextAllowedTryMs=1734017109909) timed out at 1734017108909 after 50 attempt(s)
Caused by: org.apache.kafka.common.errors.DisconnectException: Cancelled createTopics request with correlation id 203 due to node 0 being disconnected
 (org.apache.kafka.tools.TopicCommand)
command terminated with exit code 1
```

#### 01. Run Consumer

- Create a consumer pod.
```shell
kubectl run kafka-client --restart='Never' --image docker.io/bitnami/kafka:3.9.0-debian-12-r1 --command -- sleep infinity
```

- Execute the consumer.
```shell
KAFKA_POD_IP=kafka.default.svc.cluster.local:9092

kubectl exec -it kafka-client -- kafka-console-consumer.sh \
    --bootstrap-server $KAFKA_POD_IP:9092 \
    --topic output-topic \
    --from-beginnine
```


# Reference

- [Tour of Beam](https://tour.beam.apache.org/tour/java/introduction/guide)
- [Kubernetes | Apache Flink](https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/resource-providers/standalone/kubernetes/)
- [kafka 31.1.0 · bitnami/bitnami](https://artifacthub.io/packages/helm/bitnami/kafka)
