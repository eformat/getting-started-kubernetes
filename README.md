# Quarkus Getting Started Kubernetes Pipeline

## Build Graal Maven Jenkins Slave in OpenShift

- https://github.com/redhat-cop/containers-quickstarts/tree/master/jenkins-slaves

```
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
