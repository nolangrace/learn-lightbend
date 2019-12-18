helm install --namespace learn-lightbend-cloudflow --set persistence.enabled=false,securityContext.enabled=false,dbUser.forcePassword=false --name example-cassandra bitnami/cassandra

export CASSANDRA_PASSWORD=$(kubectl get secret --namespace learn-lightbend-cloudflow example-cassandra -o jsonpath="{.data.cassandra-password}" | base64 --decode)

kubectl port-forward --namespace learn-lightbend-cloudflow svc/example-cassandra 9042:9042 & cqlsh -u cassandra -p $CASSANDRA_PASSWORD 127.0.0.1 9042


kubectl pipelines deploy gcr.io/gsa-pipeliners/learn-lightbend-cloudflow:4-2126c59-dirty \
kafka-ingress.kafka-hostname="pipelines-strimzi-kafka-bootstrap.lightbend" kafka-ingress.kafka-port=9092 \
cassandra-write.cassandra-host="example-cassandra.learn-lightbend-cloudflow.svc.cluster.local" \
cassandra-write.cassandra-password="$CASSANDRA_PASSWORD"