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

#### 01. Run Consumer

- Create a consumer pod.
```shell
kubectl run kafka-client --restart='Never' --image docker.io/bitnami/kafka:3.9.0-debian-12-r1 --command -- sleep infinity
```

- Execute the consumer.
```shell
KAFKA_POD_IP=$(kubectl get pod -l app.kubernetes.io/name=kafka -o jsonpath='{.items[0].status.podIP}')

kubectl exec -it kafka-client -- kafka-console-consumer.sh \
    --bootstrap-server $KAFKA_POD_IP:9092 \
    --topic output-topic \
    --from-beginning
```


# Reference

- [Tour of Beam](https://tour.beam.apache.org/tour/java/introduction/guide)
- [Kubernetes | Apache Flink](https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/resource-providers/standalone/kubernetes/)
- [kafka 31.1.0 · bitnami/bitnami](https://artifacthub.io/packages/helm/bitnami/kafka)
