pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvnw.cmd clean package'
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                    start "SpringBootApp" cmd /c "java -jar target\\WebsocketProject-0.0.1-SNAPSHOT.jar"
                '''
            }
        }
    }
}