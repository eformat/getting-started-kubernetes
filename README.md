# Quarkus Getting Started Kubernetes Pipeline

## Build Graal Maven Jenkins Slave in OpenShift

- https://github.com/redhat-cop/containers-quickstarts/tree/master/jenkins-slaves

```
cd ~/git/containers-quickstarts/jenkins-slaves/jenkins-slave-mvn-graal

oc process -f ../.openshift/templates/jenkins-slave-generic-template.yml \
     -p NAME=jenkins-slave-mvn-graal \
     -p SOURCE_CONTEXT_DIR=jenkins-slaves/jenkins-slave-mvn-graal \
     -p DOCKERFILE_PATH=Dockerfile \
     -p SOURCE_REPOSITORY_URL=https://github.com/eformat/containers-quickstarts \
     | oc create -f -
```

## Jenkins pipeline

Run the `Jenkinsfile` as pipeline build job in OpenShift.

```
cat <<EOF | oc create -f -
apiVersion: v1
kind: BuildConfig
metadata:
  annotations:
  creationTimestamp: null
  labels:
    app: quarkus-quickstart
    name: quarkus-quickstart
  name: quarkus-quickstart
spec:
  output: {}
  postCommit: {}
  resources: {}
  runPolicy: Serial
  source:
    secrets: null
    type: "Git"
    git:
      uri: "https://github.com/eformat/getting-started-kubernetes.git"
  strategy:
    jenkinsPipelineStrategy:
      jenkinsfilePath: Jenkinsfile
    type: JenkinsPipeline
  triggers:
  - github:
      secret: secret101
    type: GitHub
  - generic:
      secret: secret101
    type: Generic
status:
  lastVersion: 0
EOF
```

## Testing

```
mvn quarkus:dev
mvn compile shamrock:dev -Ddebug=true
```

Then, attach your debugger to localhost:5005.

```
while true; do curl localhost:8080/hello/greeting/mike && echo; sleep 1; done
```

```
curl localhost:8080/hello
curl localhost:8080/hello/greeting/mike
curl localhost:8080/hello/async
curl localhost:8080/hello/async-rx
curl localhost:8080/hello/stream
--
curl localhost:8080/health
curl localhost:8080/metrics
```

```
export GRAALVM_HOME=/opt/graalvm-ce-1.0.0-rc16
mvn package -Pnative -DskipTests
```

### Extensions

```
mvn quarkus:list-extensions
-- mvn quarkus:add-extension -Dextensions="groupId:artifactId"

mvn quarkus:add-extension -Dextensions="io.quarkus:quarkus-smallrye-health"
mvn quarkus:add-extension -Dextensions="io.quarkus:quarkus-smallrye-metrics"
```

### Deploy OpenShift

```
mvn package -Pnative -DskipTests
oc new-build --binary --name=quarkus-quickstart -l app=quarkus-quickstart
oc start-build quarkus-quickstart --from-dir=. --follow
export URL="http://$(oc get route | grep quarkus-quickstart | awk '{print $2}')"
curl $URL/health

oc set probe dc/quarkus-quickstart --liveness --get-url=http://:8080/health --initial-delay-seconds=1 --timeout-seconds=1
oc set probe dc/quarkus-quickstart --readiness --get-url=http://:8080/health --initial-delay-seconds=1 --timeout-seconds=1
```

### Use FMP

```
# build a local docker image using jar
mvn package
mvn fabric8:build -Dfabric8.mode=kubernetes

# test
docker run -m 200m -td -p 8080:8080 acme/quarkus-quickstart-kubernetes

curl localhost:8080/health

# build a local docker image using native
mvn package -Pnative
mvn fabric8:build -Pnative -Dfabric8.mode=kubernetes

# test
docker run -m 200m -td -p 8080:8080 acme/quarkus-quickstart-kubernetes

curl localhost:8080/health

# have two images
docker images | grep acme/quarkus-quickstart-kubernetes

# binary build - s2i not covered yet
oc new-build --binary --name=quarkus-quickstart-kubernetes -l app=quarkus-quickstart-kubernetes
oc start-build quarkus-quickstart-kubernetes --from-dir=. --follow

# run native image on openshift
oc new-project quarkus-hello
oc policy add-role-to-user view --serviceaccount=default -n $(oc project -q)
mvn fabric8:deploy -Pnative -Dfabric8.mode=openshift

# test
host=$(oc get route quarkus-quickstart-kubernetes --template='{{ .spec.host }}')
curl $host/health

# just create resources 
mvn fabric8:resource
mvn fabric8:apply -Pnative -Dfabric8.mode=openshift

# undeploy
mvn fabric8:undeploy
```
