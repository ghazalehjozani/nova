pipeline {
    agent any

    options {
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
        timestamps()
        skipDefaultCheckout()
    }

    environment {
        MAVEN_OPTS = '-Xmx2048m -XX:+TieredCompilation -XX:TieredStopAtLevel=1'
        MAVEN_CLI_OPTS = '--errors --show-version --batch-mode --no-transfer-progress'
        MVN_CMD = 'mvn'
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    // Check for mvnd first with proper returnStdout
                    def mvndCheck = sh(script: 'command -v mvnd 2>/dev/null', returnStatus: true)
                    if (mvndCheck == 0) {
                        env.MVN_CMD = 'mvnd'
                        echo "Using Maven Daemon (mvnd)"
                    } else {
                        // Check for mvn
                        def mvnCheck = sh(script: 'command -v mvn 2>/dev/null', returnStatus: true)
                        if (mvnCheck != 0) {
                            error "Neither mvn nor mvnd found in PATH"
                        }
                        env.MVN_CMD = 'mvn'
                        echo "Using standard Maven (mvn)"
                    }

                    sh "${env.MVN_CMD} --version"
                }
            }
        }

        stage('Checkout & Setup') {
            steps {
                checkout scm
                script {
                    validateProject()

                    env.CALCULATED_VERSION = getProjectVersion()
                    env.IS_SNAPSHOT = env.CALCULATED_VERSION.contains('SNAPSHOT')

                    echo "Project version: ${env.CALCULATED_VERSION}"
                    echo "Branch: ${env.BRANCH_NAME ?: 'unknown'}"
                    echo "Build type: ${env.CHANGE_ID ? 'Pull Request' : 'Branch build'}"
                }
            }
        }

        stage('Build & Test') {
            steps {
                stage('Compile & Unit Tests') {
                    steps {
                        sh """
                            ${env.MVN_CMD} ${MAVEN_CLI_OPTS} \\
                                clean verify \\
                                -DskipITs=true
                        """
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('Integration Tests') {
                    when {
                        anyOf {
                            branch 'develop'
                            branch 'master'
                            expression { env.CHANGE_ID != null && params.RUN_INTEGRATION_TESTS }
                        }
                    }
                    steps {
                        sh """
                            ${env.MVN_CMD} ${MAVEN_CLI_OPTS} \\
                                verify \\
                                -Pintegration-test
                        """
                    }
                    post {
                        always {
                            junit '**/target/failsafe-reports/*.xml'
                        }
                    }
                }

                stage('Architecture Tests') {
                    steps {
                        sh """
                            ${env.MVN_CMD} ${MAVEN_CLI_OPTS} \\
                                test \\
                                -Parchitecture-test
                        """
                    }
                }
            }
        }

        stage('Code Quality & Analysis') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'master'
                    expression { env.CHANGE_ID != null }
                }
            }
            steps {
                stage('Error Prone Analysis') {
                    steps {
                        sh """
                            ${env.MVN_CMD} ${MAVEN_CLI_OPTS} \\
                                -Perror-prone compile
                        """
                    }
                }
                stage('Checkstyle') {
                    steps {
                        sh """
                            ${env.MVN_CMD} ${MAVEN_CLI_OPTS} \\
                                checkstyle:check
                        """
                    }
                }
                stage('SonarQube Analysis') {
                    environment {
                        SONAR_TOKEN = credentials('SONAR_TOKEN_TRADE_LOAN')
                    }
                    steps {
                        sh """
                            ${env.MVN_CMD} ${MAVEN_CLI_OPTS} \\
                                sonar:sonar \\
                                -Dsonar.token=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                allOf {
                    anyOf {
                        branch 'main'
                        branch 'master'
                        branch 'develop'
                    }
                    expression { env.CHANGE_ID == null }
                    expression { env.CALCULATED_VERSION != null }
                }
            }
            steps {
                script {
                    def deployProfile = env.IS_SNAPSHOT ? 'snapshots' : 'release'

                    sh """
                        ${env.MVN_CMD} ${MAVEN_CLI_OPTS} \\
                            deploy \\
                            -P${deployProfile} \\
                            -DskipTests=true \\
                            -Dmaven.install.skip=true
                    """

                }
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: false
                }
            }
        }
    }

    post {
        failure {
            emailext(
                subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Build failed for ${env.JOB_NAME}. Check console output at ${env.BUILD_URL}",
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
        always {
            cleanWs()
        }
    }
}

def getProjectVersion() {
    try {
        def version = sh(
            script: """
                ${env.MVN_CMD} help:evaluate -Dexpression=project.version -q -DforceStdout | \\
                grep -E '^[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?$' | \\
                head -1
            """,
            returnStdout: true
        ).trim()

        if (!version) {
            error "Could not extract valid version from pom.xml"
        }

        return version
    } catch (Exception e) {
        error "Failed to retrieve project version: ${e.message}"
    }
}

def validateProject() {
    if (!fileExists('pom.xml')) {
        error "pom.xml not found - invalid Maven project structure"
    }

    def pomContent = readFile('pom.xml')
    if (!pomContent.contains('<groupId>ir.dotin.loan</groupId>')) {
        error "Invalid project groupId - expected ir.dotin.loan"
    }
}