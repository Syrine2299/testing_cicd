pipeline {
    agent any

    environment {
        ANDROID_HOME = "/usr/lib/android-sdk"
        PATH = "${ANDROID_HOME}/cmdline-tools/tools/bin:${ANDROID_HOME}/platform-tools:${PATH}"
        GIT_CREDENTIALS_ID = '1db4425d-852e-4237-8252-098b032f13a3' // Replace with your GitHub credentials ID
        APP_CENTER_TOKEN = '862c7f40e8a1c8698754f56a41b499f4e9b032b3' // Replace with your App Center API token
        APP_CENTER_OWNER = 'SIRINE RABHI' // Replace with App Center owner
        APP_CENTER_APP = 'AndroidApp' // Replace with App Center app name
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo "Cloning the GitHub repository..."
                git credentialsId: "${GIT_CREDENTIALS_ID}", url: 'https://github.com/Syrine2299/testing_cicd.git'
            }
        }

        stage('Setup Environment') {
            steps {
                echo "Setting up the Android environment..."
                sh '''
                    sdkmanager --licenses
                    sdkmanager "platform-tools" "build-tools;33.0.0" "platforms;android-33"
                '''
            }
        }

        stage('Build APK') {
            steps {
                echo "Building the Android application..."
                sh './gradlew assembleDebug'
            }
        }

        stage('Run Tests') {
            steps {
                echo "Running unit tests..."
                sh './gradlew testDebugUnitTest'
            }
        }

        stage('Archive APK') {
            steps {
                echo "Archiving the APK file..."
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
            }
        }

        stage('Upload to App Center') {
            steps {
             retry(3) {
              echo "Uploading APK to App Center..."
              sh '''
                npx appcenter distribute release \
                    --file app/build/outputs/apk/debug/app-debug.apk \
                    --app ${APP_CENTER_OWNER}/${APP_CENTER_APP} \
                    --group Collaborators \
                    --token ${APP_CENTER_TOKEN}
            '''
        }
    }
    }

    post {
        always {
            echo "Cleaning up workspace..."
            cleanWs()
        }
        success {
            echo "Build and upload succeeded!"
        }
        failure {
            echo "Build failed. Check logs for details."
        }
    }
}
