pipeline {
	agent {
		label 'agent-docker'
    }

    options {
		skipDefaultCheckout true
    }

    environment {
      	BITBUCKET_CREDS = credentials('bitbucket-credentials')
        DOCKER_CREDS = credentials('dockerhub-creds')
		DOCKER_IMAGE_NAME = "${params.DOCKER_REPO}:${env.BUILD_NUMBER}"
    }

    stages {
		stage('Checkout') {
            steps {
                sh """
                	rm -rf resume-analyzer-backend || true
                    git clone https://${BITBUCKET_CREDS_USR}:${BITBUCKET_CREDS_PSW}@gitpacket.kata.academy/scm/fit/resume-analyzer-backend.git
                    cd resume-analyzer-backend
                    git checkout main
                """
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
                        sh '''
                            echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                            docker build -t "$DOCKER_IMAGE_NAME" .
                            docker push "$DOCKER_IMAGE_NAME"
                        '''
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
