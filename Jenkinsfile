pipeline {
    agent any

    environment {
        NETWORK_NAME = 'blog-network'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Ensure Network Exists') {
            steps {
                script {
                    sh "docker network create ${NETWORK_NAME} || true"
                }
            }
        }

        stage('Ensure Nginx') {
            steps {
                script {
                    sh "docker compose -f docker-compose.yml up -d nginx postgres"
                }
            }
        }

        stage('Blue-Green Deploy') {
            steps {
                script {
                    sh "chmod +x scripts/deploy.sh && ./scripts/deploy.sh"
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
