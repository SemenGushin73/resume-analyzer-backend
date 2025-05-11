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
