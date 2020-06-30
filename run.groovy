pipeline {
    agent{node('master')}
    stages {
        stage('Clean workspace & download dist') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop arhipov"
                            sh "echo '${password}' | sudo -S docker container rm arhipov"
                        } catch (Exception e) {
                            print 'Epic Fail'
                        }
                    }
                }
                script {
                    echo 'Update from repository'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'ArhipovAAGit', url: 'https://github.com/impuppy/jenkins_docker.git']]])
                }
            }
        }
        stage ('Build & run docker image'){
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t arhipovaa_nginx"
                        currentBuild.result = 'FAILURE'
                        sh "echo '${password}' | sudo -S docker run -d -p 8220:80 --name arhipov -v /home/adminci/arhipovaa:/stat arhipovaa_nginx"
                    }
                }
            }
        }
        stage ('Get stats & write to file'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker exec -t arhipov bash -c 'df -h > /stat/stats.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t arhipov bash -c 'top -n 1 -b >> /stat/stats.txt'"
                    }
                }
            }
        }
        stage ('STOP'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) { 
                            sh "echo '${password}' | sudo -S docker stop arhipov"
                       }
                    }
                }
            }
        }
    }
}
