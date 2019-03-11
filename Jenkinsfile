pipeline {
    environment {
        APP_NAME = "quarkus-quickstart"
        DEV_NAMESPACE = "quarkus-hello"
        GIT_SSL_NO_VERIFY = 'true'
        GIT_URL='https://github.com/eformat/getting-started-kubernetes.git'
        BRANCH_NAME='master'
    }

    agent {
        label "master"
    }

    stages {
        stage('Build and Deploy Dev') {
            agent {
              kubernetes {
                label 'jenkins-slave-mvn-graal'
                cloud 'openshift'
                serviceAccount 'jenkins'
                containerTemplate {
                  name 'jnlp'
                  image "docker-registry.default.svc:5000/openshift/jenkins-slave-mvn-graal:v3.11"
                  alwaysPullImage true
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
                            openshift.withProject("${env.DEV_NAMESPACE}") {
                                checkout([$class           : 'GitSCM',
                                          branches         : [[name: "*/${env.BRANCH_NAME}"]],
                                          userRemoteConfigs: [[url: "${env.GIT_URL}", refspec: "+refs/pull/*:refs/remotes/origin/pr/*"]], 
                                ]);                                
                                // maven cache configuration (change mirror host)
                                sh "sed -i \"s|<!-- ### configured mirrors ### -->|<mirror><id>mirror.default</id><url>http://nexus.nexus.svc.cluster.local:8081/repository/maven-public/</url><mirrorOf>external:*</mirrorOf></mirror>|\" /home/jenkins/.m2/settings.xml"
                                sh '''
                                export PATH=/opt/rh/rh-maven35/root/usr/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
                                mvn clean package -Pnative -DskipTests
                                '''
                                sh  '''
                                cat <<EOF > Dockerfile
FROM registry.fedoraproject.org/fedora-minimal
WORKDIR /work/
COPY target/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
EOF
                                '''
                                def build = openshift.selector("bc", "${env.APP_NAME}")
                                if (build.count() == 1) {
                                    // existing bc
                                    sh "oc start-build -n ${env.DEV_NAMESPACE} ${env.APP_NAME} --from-dir=."
                                    def pipelineBuild = build.related('builds')
                                    pipelineBuild.untilEach(1) {
                                        return it.object().status.phase == "Complete"
                                    }                                    
                                } else {
                                    // create new build
                                    sh "oc new-build -n ${env.DEV_NAMESPACE} --binary --name=${env.APP_NAME} -l app=${env.APP_NAME}"
                                    sh "oc start-build -n ${env.DEV_NAMESPACE} ${env.APP_NAME} --from-dir=."
                                    build = openshift.selector("bc", "${env.APP_NAME}")
                                    def pipelineBuild = build.related('builds')
                                    pipelineBuild.untilEach(1) {
                                        return it.object().status.phase == "Complete"
                                    }
                                    sh "oc new-app -n ${env.DEV_NAMESPACE} --image-stream=${env.APP_NAME}:latest"
                                    sh "oc expose service ${env.APP_NAME} -n ${env.DEV_NAMESPACE}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}        
