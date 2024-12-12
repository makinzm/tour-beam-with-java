# tour-beam-with-java


## SetUp

### Flink

```shell
kubectl create namespace flink

kubectl config set-context --current --namespace=flink
```

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
kubectl port-forward deployment/flink-jobmanager 8081:8081
```

```shell
open http://localhost:8081
```


# Reference

- [Tour of Beam](https://tour.beam.apache.org/tour/java/introduction/guide)
- [Kubernetes | Apache Flink](https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/resource-providers/standalone/kubernetes/)

