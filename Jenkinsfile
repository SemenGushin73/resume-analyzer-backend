pipeline {
	agent {
		label 'agent-docker'
    }

    stages {
		stage('SCM Checkout') {
			steps {
				script {
					sh 'git init'
					checkout([
						$class: 'GitSCM',
						branches: [[name: '*/main']],
						extensions: [[$class: 'CleanCheckout']],
						userRemoteConfigs: [[
							credentialsId: 'bitbucket-credentials',
							url: 'https://gitpacket.kata.academy/scm/fit/resume-analyzer-backend.git'
						]]
					])
				}
			}
		}

        stage('Build') {
			steps {
				script {
					dir('resume-analyzer-backend') {
						sh 'chmod +x gradlew'
						sh './gradlew clean build'
                    }
                }
            }
        }

        stage('Test') {
			steps {
				script {
					dir('resume-analyzer-backend') {
						sh 'chmod +x gradlew'
                        sh './gradlew test'
                    }
                }
            }
        }

        stage('Docker Build & Push') {
			steps {
				script {
					withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
						sh "echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin"
                        dir('resume-analyzer-backend') {
							sh "docker build -t ${env.DOCKER_IMAGE_NAME} ."
                            sh "docker push ${env.DOCKER_IMAGE_NAME}"
                        }
                    }
                }
            }
        }

        stage('Deploy') {
			steps {
				script {
					withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
						sh "echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin"
                        sh """
                            docker pull ${env.DOCKER_IMAGE_NAME}
                            docker stop resume-analyzer || true
                            docker rm resume-analyzer || true
                            docker run -d --name resume-analyzer -p 8080:8080 ${env.DOCKER_IMAGE_NAME}
                        """
                    }
                }
            }
        }
    }

    post {
		success {
			echo 'Build & Deployment Success!'
        }
        failure {
			echo 'Build Failed'
        }
    }
}
