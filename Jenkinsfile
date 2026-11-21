pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        AWS_DEFAULT_REGION = 'us-east-1'
        S3_BUCKET          = 'your-eb-deploy-bucket'
        EB_APPLICATION     = 'institute-management'
        EB_ENVIRONMENT     = 'institute-management-env'
        VERSION_LABEL      = "build-${env.BUILD_NUMBER}"
    }

    stages {
        stage('Pull from GitHub') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/<your-user>/Institute-Management-.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh '''
                    chmod +x institute-management/mvnw auth-service/mvnw notification-service/mvnw
                    (cd institute-management && ./mvnw -B clean package -DskipTests)
                    (cd auth-service         && ./mvnw -B clean package -DskipTests)
                    (cd notification-service && ./mvnw -B clean package -DskipTests)
                '''
            }
        }

        stage('Prepare Deployment') {
            steps {
                sh '''
                    cp institute-management/target/institute-management-0.0.1-SNAPSHOT.jar aws-deployment/institute-management/
                    cp auth-service/target/auth-service-0.0.1-SNAPSHOT.jar aws-deployment/auth-service/
                    cp notification-service/target/notification-service-0.0.1-SNAPSHOT.jar aws-deployment/notification-service/
                    cd aws-deployment
                    rm -f ../deployment.zip
                    zip -r ../deployment.zip . -x "*.git*"
                '''
            }
        }

        stage('Deploy to AWS') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'aws-credentials',
                                                  usernameVariable: 'AWS_ACCESS_KEY_ID',
                                                  passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh '''
                        aws s3 cp deployment.zip s3://$S3_BUCKET/$VERSION_LABEL.zip
                        aws elasticbeanstalk create-application-version \
                            --application-name "$EB_APPLICATION" \
                            --version-label "$VERSION_LABEL" \
                            --source-bundle S3Bucket="$S3_BUCKET",S3Key="$VERSION_LABEL.zip"
                        aws elasticbeanstalk update-environment \
                            --environment-name "$EB_ENVIRONMENT" \
                            --version-label "$VERSION_LABEL"
                    '''
                }
            }
        }
    }
}
