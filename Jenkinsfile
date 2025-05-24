pipeline {
    agent any

    environment {
        GIT_USER_EMAIL = 'm.amirabdollahi@dotin.ir'  // 'core.jenkins@dotin.ir'
        GIT_USER_NAME = 'Mahdi Amirabdollahi'  // 'Jenkins CI'
        GIT_REPO_URL = 'https://bitbucket.dotin.ir/scm/core/trade-loan.git'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
                script {
                    setupGitUser()
                    if (!fileExists('pom.xml')) {
                        error "Critical error: pom.xml not found in workspace!"
                    }
                    def retrievedVersion = getProjectVersion()

                    echo "Retrieved version: ${retrievedVersion}"

                    env.CALCULATED_VERSION = retrievedVersion.toString()

                    echo "CALCULATED_VERSION set to: ${env.CALCULATED_VERSION}"

                    if (!env.CALCULATED_VERSION) {
                        error "Failed to set CALCULATED_VERSION environment variable"
                    }
                }
                echo "Branch name: ${env.BRANCH_NAME}"
                echo "Is PR build: ${env.CHANGE_ID ? 'Yes' : 'No'}"
            }
        }

        stage('Build & Analysis') {
            steps {
                parallel(
                    "Core Build": { sh 'mvn --errors --show-version --batch-mode --no-transfer-progress -Ddoclint=all compile -Pcore-build,code-quality' },
                    failFast: true
                )
            }
        }

        stage('Testing') {
            steps {
                parallel(
                    "Unit Tests": { sh 'mvn --errors --show-version --batch-mode --no-transfer-progress -Ddoclint=all -Pcore-build,code-quality test' },
                    "Integration Tests": { sh 'mvn --errors --show-version --batch-mode --no-transfer-progress -Ddoclint=all -Pcore-build,code-quality verify' },
                    "Architecture Tests": { sh 'mvn --errors --show-version --batch-mode --no-transfer-progress -Ddoclint=all clean test -Parchitecture-test' },
                    failFast: true
                )
            }
        }

        stage('Quality Gates') {
            environment {
                SONAR_TOKEN = credentials('SONAR_TOKEN_TRADE_LOAN')
            }
            steps {
                sh """
                    mvn --errors --show-version --batch-mode --no-transfer-progress -Ddoclint=all -Pquality-gate sonar:sonar clean verify -Dsonar.token=${SONAR_TOKEN}
                """
            }
        }

        stage('Deploy') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { env.CALCULATED_VERSION }
                }
            }
            steps {
                sh 'mvn --errors --show-version --batch-mode --no-transfer-progress -Ddoclint=all -Prelease deploy'
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}

def getProjectVersion() {
    try {
        String rawOutput = sh(
            script: 'mvn help:evaluate -Dexpression=project.version -DforceStdout',
            returnStdout: true
        ).trim()

        echo "Raw Maven output: '${rawOutput}'"

        def version = rawOutput.readLines()
                               .find { it ==~ /^\d+\.\d+\.\d+(-SNAPSHOT)?$/ }

        if (!version) {
            error "Could not find a valid version line in the Maven output."
        }

        echo "Validated project version: ${version}"
        return version

    } catch (ex) {
        error "Failed to retrieve project version: ${ex.getMessage()}"
    }
}

def setupGitUser() {
    sh """
        git config user.email "${env.GIT_USER_EMAIL}"
        git config user.name "${env.GIT_USER_NAME}"
    """
}
