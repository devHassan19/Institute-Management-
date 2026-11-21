pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {
        AWS_DEFAULT_REGION = 'eu-north-1'
    }

    stages {
        stage('Hello') {
            steps {
                echo 'Hello Institute Microservices'
            }
        }

        stage('Pull from GitHub') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/devHassan19/Institute-Management-.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh '''
                    mvn -f auth-service/pom.xml clean package -DskipTests
                    mvn -f institute-management/pom.xml clean package -DskipTests
                    mvn -f notification-service/pom.xml clean package -DskipTests
                '''
            }
        }

        stage('Prepare Deployment') {
            steps {
                sh '''
                    cp auth-service/target/auth-service-0.0.1-SNAPSHOT.jar aws-deployment/auth-service/
                    cp institute-management/target/institute-management-0.0.1-SNAPSHOT.jar aws-deployment/institute-management/
                    cp notification-service/target/notification-service-0.0.1-SNAPSHOT.jar aws-deployment/notification-service/

                    rm -f deployment.zip
                    cd aws-deployment
                    jar --create --file ../deployment.zip -C . .
                    cd ..
                    jar tf deployment.zip
                '''
            }
        }

        stage('Deploy to AWS') {
            steps {
                withCredentials([
                    string(credentialsId: 'AWS_ACCESS_KEY_ID', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'AWS_SECRET_ACCESS_KEY', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    sh '''
                        BUCKET=$(aws elasticbeanstalk create-storage-location --query S3Bucket --output text)
                        VERSION="jenkins-${BUILD_NUMBER}"

                        aws s3 cp deployment.zip "s3://${BUCKET}/deployments/${VERSION}.zip"

                        aws elasticbeanstalk create-application-version \
                          --application-name Institute-Microservices \
                          --version-label "$VERSION" \
                          --source-bundle S3Bucket="$BUCKET",S3Key="deployments/${VERSION}.zip"

                        aws elasticbeanstalk update-environment \
                          --environment-name Institute-Microservices-env \
                          --version-label "$VERSION"
                    '''
                }
            }
        }
    }
}
