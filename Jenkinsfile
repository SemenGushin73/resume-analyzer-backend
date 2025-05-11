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
      	DOCKER_IMAGE_ANALYZER = "${params.DOCKER_REPO}-resume-analyzer:${env.BUILD_NUMBER}"
        DOCKER_IMAGE_GENERATOR = "${params.DOCKER_REPO}-resume-generator:${env.BUILD_NUMBER}"
        DOCKER_IMAGE_GATEWAY = "${params.DOCKER_REPO}-api-gateway:${env.BUILD_NUMBER}"
        DOCKER_IMAGE_CONFIG = "${params.DOCKER_REPO}-config-server:${env.BUILD_NUMBER}"
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
      
      	stage('Build Docker Images') {
        	steps {
                script {
                    dir('resume-analyzer-backend') {
                        sh '''
                            docker build -t $DOCKER_IMAGE_ANALYZER -f resume-analyzer/Dockerfile .
                            docker build -t $DOCKER_IMAGE_GENERATOR -f resume-generator/Dockerfile resume-generator/
                            docker build -t $DOCKER_IMAGE_GATEWAY -f api-gateway/Dockerfile api-gateway/
                            docker build -t $DOCKER_IMAGE_CONFIG -f bitbucket-config-server/Dockerfile bitbucket-config-server
                        '''
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                        docker push $DOCKER_IMAGE_ANALYZER
                        docker push $DOCKER_IMAGE_GENERATOR
                        docker push $DOCKER_IMAGE_GATEWAY
                        docker push $DOCKER_IMAGE_CONFIG
                    '''
                }
            }
        }
      
      	stage('Deploy with Docker Compose') {
        	steps {
            	dir('resume-analyzer-backend') {
                	sh '''
                    	docker compose -f docker-compose.yml down
                        docker compose -f docker-compose.yml up -d --build
                    '''
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
