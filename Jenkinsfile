pipeline {
    environment {
        APP_NAME = "quarkus-quickstart"
        DEV_NAMESPACE = "quarkus-hello"
        GIT_SSL_NO_VERIFY = 'true'
    }

    agent {
        label "master"
    }

    stages {
        stage("Pipeline Start") {
            steps {
                notifyBuild('STARTED')
            }
            post {
                failure {
                    notifyBuild('FAIL')
                }
            }
        }

        stage('Build and Deploy Dev') {
            agent {
              kubernetes {
                label 'ansible-slave'
                cloud 'openshift'
                serviceAccount 'jenkins'
                containerTemplate {
                  name 'jnlp'
                  image "docker-registry.default.svc:5000/openshift/jenkins-slave-mvn-graal:v3.11"
                  alwaysPullImage false
                  workingDir '/tmp'
                  args '${computer.jnlpmac} ${computer.name}'
                  command ''
                  ttyEnabled false
                }
              }
            }
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withCredentials() {
                            openshift.withProject("${DEV_NAMESPACE}") {
                                // maven cache configuration (change mirror host)
                                sh "sed -i \"s|<!-- ### configured mirrors ### -->|<mirror><id>mirror.default</id><url>http://nexus.nexus.svc.cluster.local:8081/repository/maven-public/</url><mirrorOf>external:*</mirrorOf></mirror>|\" /home/jenkins/.m2/settings.xml"
                                sh "mvn clean package -Pnative -DskipTests"
                                sh  '''
                                cat <<EOF > Dockerfile
FROM registry.fedoraproject.org/fedora-minimal
WORKDIR /work/
COPY target/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dshamrock.http.host=0.0.0.0"]
EOF
                                '''
                                sh "cat Dockerfile | oc new-build -D - --name ${APP_NAME} --follow"
                                sh "oc new-app --image-stream=${APP_NAME}:latest"
                                sh "oc expose service ${APP_NAME}"
                            }
                        }
                    }
                }
            }
        }
    }
}        
