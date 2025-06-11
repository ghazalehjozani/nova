pipeline {
    agent any

    parameters {
        booleanParam(name: 'RUN_INTEGRATION_TESTS', defaultValue: false, description: 'Run integration tests')
        booleanParam(name: 'RUN_ARCHITECTURE_TESTS', defaultValue: true, description: 'Run architecture tests')
        booleanParam(name: 'RUN_SPOTBUGS', defaultValue: false, description: 'Run SpotBugs static analysis')
        booleanParam(name: 'RUN_SECURITY_SCAN', defaultValue: false, description: 'Run OWASP security scanning')
        booleanParam(name: 'FORCE_DEPLOY', defaultValue: false, description: 'Force deployment even if quality gates fail')
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        timestamps()
        skipDefaultCheckout()
        disableConcurrentBuilds()
        parallelsAlwaysFailFast()
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        MAVEN_OPTS = '-Xmx2048m -XX:+TieredCompilation -XX:TieredStopAtLevel=1'
        MAVEN_CLI_OPTS = '--errors --show-version --batch-mode --no-transfer-progress'
        MVN_CMD = 'mvn'
        PROJECT_NAME = 'trade-loan'
        PROJECT_GROUP = 'ir.dotin.loan'
    }

    stages {
        stage('Initialize & Checkout') {
            steps {
                deleteDir()
                checkout scm
                script {
                    printBuildInfo()
                    validateProject()
                    setupBuildEnvironment()
                }
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} clean verify -P!dev -DskipITs=true"
            }
            post {
                always {
                    junit allowEmptyResults: false, testResults: '**/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Static Analysis') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'develop'
                    expression { env.CHANGE_ID != null }
                }
            }
            parallel {
                stage('Error-Prone') {
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                            sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P!dev,error-prone compile"
                        }
                    }
                }
                stage('Checkstyle') {
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                            sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P!dev,quality-gate checkstyle:check"
                            recordIssues(
                                enabledForFailure: true,
                            )
                        }
                    }
                }
                stage('SpotBugs') {
                    when { expression { params.RUN_SPOTBUGS } }
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                            sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P!dev,quality-gate spotbugs:check"
                        }
                    }
                }
            }
        }

        stage('Advanced Tests') {
            parallel {
                stage('Integration Tests') {
                    when { expression { params.RUN_INTEGRATION_TESTS } }
                    steps {
                        sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} verify -P!dev,integration-test -DskipUTs=true"
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml'
                        }
                    }
                }
                stage('Architecture Tests') {
                    when { expression { params.RUN_ARCHITECTURE_TESTS } }
                    steps {
                        sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} test -P!dev,architecture-test"
                    }
                }
            }
        }

        stage('Security Scan') {
            when { expression { params.RUN_SECURITY_SCAN } }
            steps {
                sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -Psecurity verify"
            }
        }

        stage('SonarQube Analysis') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'develop'
                    expression { env.CHANGE_ID != null }
                }
            }
            environment {
                SONAR_TOKEN = credentials('SONAR_TOKEN_TRADE_LOAN')
            }
            steps {
                script {
                    withSonarQubeEnv('SonarQube') {
                        def sonarParams = "-Dsonar.token=${SONAR_TOKEN}"
                        if (env.CHANGE_ID) {
                            sonarParams += " -Dsonar.pullrequest.key=${env.CHANGE_ID} -Dsonar.pullrequest.branch=${env.CHANGE_BRANCH} -Dsonar.pullrequest.base=${env.CHANGE_TARGET}"
                        }
                        sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P!dev,quality-gate sonar:sonar ${sonarParams}"
                    }
                    timeout(time: 15, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK' && !params.FORCE_DEPLOY) {
                            error "Pipeline aborted due to SonarQube quality gate failure: ${qg.status}"
                        } else if (qg.status != 'OK') {
                            unstable "Quality gate failed but FORCE_DEPLOY is enabled. Status: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                allOf {
                    anyOf { branch 'develop'; branch 'master'; branch 'main' }
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                script {
                    sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} deploy -P!dev,release -DskipTests=true"

                    if (!env.IS_SNAPSHOT) {
                        sh """
                            git tag -a v${env.CALCULATED_VERSION} -m "Release version ${env.CALCULATED_VERSION}"
                            git push origin v${env.CALCULATED_VERSION}
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            updateBuildStatus('SUCCESS', 'Build completed successfully')
        }
        failure {
            updateBuildStatus('FAILURE', 'Build failed')
        }
        unstable {
            updateBuildStatus('UNSTABLE', 'Build is unstable')
        }
        aborted {
            updateBuildStatus('ABORTED', 'Build was aborted')
        }
    }

}


def setupBuildEnvironment() {
    env.CALCULATED_VERSION = getProjectVersion()
    env.IS_SNAPSHOT = env.CALCULATED_VERSION.contains('SNAPSHOT').toString()
    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    env.GIT_BRANCH = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()

    currentBuild.displayName = "#${BUILD_NUMBER} - ${env.CALCULATED_VERSION}"
    currentBuild.description = "Branch: ${env.BRANCH_NAME}, Commit: ${env.GIT_COMMIT_SHORT}"
}

def getProjectVersion() {
    try {
        def version = sh(
            script: """
                ${env.MVN_CMD} help:evaluate -Dexpression=project.version -q -DforceStdout | \\
                grep -E '^[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?\$' | \\
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
    if (!pomContent.contains("<groupId>${PROJECT_GROUP}</groupId>")) {
        error "Invalid project groupId - expected ${PROJECT_GROUP}"
    }

    if (!pomContent.contains("<artifactId>${PROJECT_NAME}</artifactId>")) {
        error "Invalid project artifactId - expected ${PROJECT_NAME}"
    }
}

def printBuildInfo() {
    echo """
    ╔════════════════════════════════════════╗
    ║          Build Information             ║
    ╠════════════════════════════════════════╣
    ║ Job Name    : ${env.JOB_NAME}
    ║ Build Number: ${env.BUILD_NUMBER}
    ║ Node Name   : ${env.NODE_NAME}
    ║ Workspace   : ${env.WORKSPACE}
    ║ Maven       : ${env.MVN_CMD}
    ║ User        : ${env.BUILD_USER ?: 'System'}
    ╚════════════════════════════════════════╝
    """
}
