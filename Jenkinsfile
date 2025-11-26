pipeline {
    agent {
        label 'minikube'
    }

    parameters {
        booleanParam(name: 'SHIP_IT_MODE', defaultValue: true, description: '🚀 Shipping mode: Only Build & Unit Test runs, all other stages are skipped')
        booleanParam(name: 'RUN_INTEGRATION_TESTS', defaultValue: false, description: 'Run integration tests')
        booleanParam(name: 'RUN_ARCHITECTURE_TESTS', defaultValue: true, description: 'Run architecture tests')
        booleanParam(name: 'RUN_SPOTBUGS', defaultValue: false, description: 'Run SpotBugs static analysis')
        booleanParam(name: 'RUN_SECURITY_SCAN', defaultValue: false, description: 'Run OWASP security scanning')
        booleanParam(name: 'RUN_AI_CODE_REVIEW', defaultValue: true, description: 'Run AI code review with Claude')
        choice(name: 'AI_REVIEW_TYPE', choices: ['full', 'security', 'performance', 'quick'], description: 'Type of AI code review to perform')
        booleanParam(name: 'FORCE_DEPLOY', defaultValue: false, description: 'Force deployment even if quality gates fail')
        booleanParam(name: 'PUBLISH_TO_NEXUS', defaultValue: true, description: 'Publish Docker image to Nexus registry')
        string(name: 'NEXUS_RETENTION_COUNT', defaultValue: '3', description: 'Number of Docker image versions to retain in Nexus')
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
        MAVEN_CLI_OPTS = '--errors --show-version --batch-mode --no-transfer-progress -U'
        MVN_CMD = 'mvn'
        PROJECT_NAME = 'trade-loan'
        PROJECT_GROUP = 'ir.dotin.loan'
        DOCKER_IMAGE_NAME = 'trade-loan-service'
        NEXUS_REPOSITORY_NAME = 'expenditures'
        K8S_NAMESPACE = 'default'
        CLAUDE_REVIEW_SCRIPT = 'claude-code-review.sh'
        AI_REVIEW_OUTPUT_DIR = 'ai-review-reports'
        HEALTH_CHECK_ENDPOINT = 'http://localhost:8085/actuator/health'
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

                    if (params.SHIP_IT_MODE) {
                        echo "🚀 SHIP IT MODE ACTIVATED! Only Build & Unit Test will run"
                    }
                }
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} clean verify -P !dev -DskipITs=true"
            }
            post {
                always {
                    junit allowEmptyResults: false, testResults: '**/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Static Analysis') {
            when {
                allOf {
                    expression { !params.SHIP_IT_MODE }
                    anyOf {
                        branch 'main'
                        branch 'master'
                        branch 'develop'
                        expression { env.CHANGE_ID != null }
                    }
                }
            }
            parallel {
                stage('Error-Prone') {
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                            sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P !dev,error-prone compile"
                        }
                    }
                }
                stage('Checkstyle') {
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                            sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P !dev,quality-gate checkstyle:check"
                        }
                    }
                }
                stage('SpotBugs') {
                    when { expression { params.RUN_SPOTBUGS } }
                    steps {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                            sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P !dev,quality-gate spotbugs:check"
                        }
                    }
                }
            }
        }

        stage('Advanced Tests') {
            when {
                expression { !params.SHIP_IT_MODE }
            }
            parallel {
                stage('Integration Tests') {
                    when { expression { params.RUN_INTEGRATION_TESTS } }
                    steps {
                        sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} verify -P !dev,integration-test -DskipUTs=true"
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
                        sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} test -P !dev,architecture-test"
                    }
                }
            }
        }

        stage('Security Scan') {
            when {
                allOf {
                    expression { !params.SHIP_IT_MODE }
                    expression { params.RUN_SECURITY_SCAN }
                }
            }
            steps {
                sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P security verify"
            }
        }

        stage('SonarQube Analysis') {
            when {
                allOf {
                    expression { !params.SHIP_IT_MODE }
                    anyOf {
                        branch 'main'
                        branch 'master'
                        branch 'develop'
                        expression { env.CHANGE_ID != null }
                    }
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
                        sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P !dev,quality-gate sonar:sonar ${sonarParams}"
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

        stage('AI Code Review') {
            when {
                allOf {
                    expression { !params.SHIP_IT_MODE }
                    expression { params.RUN_AI_CODE_REVIEW }
                    anyOf {
                        branch 'main'
                        branch 'master'
                        branch 'develop'
                        expression { env.CHANGE_ID != null }
                    }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                script {
                    sh "mkdir -p ${env.AI_REVIEW_OUTPUT_DIR}"

                    if (!fileExists(env.CLAUDE_REVIEW_SCRIPT)) {
                        error "Claude review script not found: ${env.CLAUDE_REVIEW_SCRIPT}"
                    }

                    def reviewResult = sh(
                        script: """
                            export REVIEW_TYPE="${params.AI_REVIEW_TYPE}"
                            export OUTPUT_DIR="${env.AI_REVIEW_OUTPUT_DIR}"
                            export GIT_REF="${env.GIT_COMMIT_SHORT}"
                            export PROJECT_NAME="${env.PROJECT_NAME}"
                            bash ${env.CLAUDE_REVIEW_SCRIPT}
                        """,
                        returnStatus: true
                    )

                    switch(reviewResult) {
                        case 0:
                            echo "✅ AI Code Review completed successfully"
                            break
                        case 1:
                            error "⚠️ AI Code Review found warnings"
                            break
                        case 2:
                            error "🔴 AI Code Review found critical issues"
                            break
                        default:
                            error "❌ AI Code Review script failed (exit: ${reviewResult})"
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'claude-review-report.*', allowEmptyArchive: true
                    archiveArtifacts artifacts: "${env.AI_REVIEW_OUTPUT_DIR}/**/*", allowEmptyArchive: true
                }
            }
        }

        stage('Prepare Deployment') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                dir('container') {
                    sh 'cp $HOME/nova/.env .'
                }
            }
        }

        stage('Build Docker Image') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                dir('container') {
                    sh """
                        mkdir -p ca-certificates
                        cp -r \$HOME/nova/ca-certificates/* ca-certificates/ 2>/dev/null || true
                        docker build \
                            -t ${env.DOCKER_IMAGE_NAME}:${env.CALCULATED_VERSION} \
                            -t ${env.DOCKER_IMAGE_NAME}:latest \
                            -f Dockerfile .
                    """
                }
            }
            post {
                success {
                    echo "✅ Docker image built: ${env.DOCKER_IMAGE_NAME}:${env.CALCULATED_VERSION}"
                }
            }
        }

        stage('Publish Docker Image to Nexus') {
            when {
                allOf {
                    expression { params.PUBLISH_TO_NEXUS }
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                script {
                    withCredentials([
                        string(credentialsId: 'NEXUS_REGISTRY_URL', variable: 'NEXUS_URL'),
                        string(credentialsId: 'NEXUS_USERNAME', variable: 'NEXUS_USER'),
                        string(credentialsId: 'NEXUS_PASSWORD', variable: 'NEXUS_PASS')
                    ]) {
                        sh """
                            echo "\${NEXUS_PASS}" | docker login \${NEXUS_URL} -u "\${NEXUS_USER}" --password-stdin

                            docker tag ${env.DOCKER_IMAGE_NAME}:${env.CALCULATED_VERSION} \
                                \${NEXUS_URL}/${env.NEXUS_REPOSITORY_NAME}/${env.DOCKER_IMAGE_NAME}:${env.CALCULATED_VERSION}
                            docker tag ${env.DOCKER_IMAGE_NAME}:latest \
                                \${NEXUS_URL}/${env.NEXUS_REPOSITORY_NAME}/${env.DOCKER_IMAGE_NAME}:latest

                            docker push \${NEXUS_URL}/${env.NEXUS_REPOSITORY_NAME}/${env.DOCKER_IMAGE_NAME}:${env.CALCULATED_VERSION}
                            docker push \${NEXUS_URL}/${env.NEXUS_REPOSITORY_NAME}/${env.DOCKER_IMAGE_NAME}:latest

                            docker logout \${NEXUS_URL}
                        """

                        cleanupOldNexusImages(params.NEXUS_RETENTION_COUNT)
                    }
                }
            }
            post {
                success {
                    echo "✅ Docker image published: ${env.CALCULATED_VERSION}"
                }
            }
        }

        stage('Load Image to Minikube') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                sh """
                    minikube image load ${env.DOCKER_IMAGE_NAME}:${env.CALCULATED_VERSION}
                    minikube image load ${env.DOCKER_IMAGE_NAME}:latest
                """
            }
            post {
                success {
                    echo "✅ Images loaded to Minikube"
                }
            }
        }

        stage('Sync K8s Configs') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                dir('container') {
                    sh './scripts/sync-configs.sh'
                }
            }
        }

        stage('Create K8s Secrets') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                dir('container') {
                    sh './scripts/create-secrets.sh'
                }
            }
        }

        stage('Deploy to K8s') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                dir('container') {
                    sh """
                        kubectl apply -f k8s/base/rbac.yml -n ${env.K8S_NAMESPACE}
                        kubectl apply -f k8s/base/configmap-trade-loan.yml -n ${env.K8S_NAMESPACE}
                        kubectl apply -f k8s/base/service.yml -n ${env.K8S_NAMESPACE}

                        kubectl set image deployment/${env.DOCKER_IMAGE_NAME} \
                            ${env.DOCKER_IMAGE_NAME}=${env.DOCKER_IMAGE_NAME}:${env.CALCULATED_VERSION} \
                            -n ${env.K8S_NAMESPACE} || kubectl apply -f k8s/base/deployment.yml -n ${env.K8S_NAMESPACE}

                        kubectl rollout status deployment/${env.DOCKER_IMAGE_NAME} -n ${env.K8S_NAMESPACE} --timeout=15m
                    """
                }
            }
            post {
                failure {
                    dir('container') {
                        sh """
                            echo "❌ Deployment failed, rolling back..."
                            kubectl rollout undo deployment/${env.DOCKER_IMAGE_NAME} -n ${env.K8S_NAMESPACE} || true
                        """
                    }
                }
            }
        }

        stage('Verify Deployment') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                script {
                    timeout(time: 5, unit: 'MINUTES') {
                        sh """
                            kubectl wait --for=condition=ready pod \
                                -l app=${env.DOCKER_IMAGE_NAME} \
                                -n ${env.K8S_NAMESPACE} \
                                --timeout=300s
                        """

                        def healthCheckPassed = false
                        retry(10) {
                            sleep 10
                            def healthStatus = sh(
                                script: "curl -sf ${env.HEALTH_CHECK_ENDPOINT} | grep -q 'UP'",
                                returnStatus: true
                            )
                            if (healthStatus == 0) {
                                healthCheckPassed = true
                                echo "✅ Health check passed"
                            } else {
                                error "Health check failed, retrying..."
                            }
                        }

                        if (!healthCheckPassed) {
                            error "❌ Health check failed after retries"
                        }

                        sh """
                            kubectl get pods -n ${env.K8S_NAMESPACE} -l app=${env.DOCKER_IMAGE_NAME}
                            kubectl describe deployment/${env.DOCKER_IMAGE_NAME} -n ${env.K8S_NAMESPACE} | grep Image:
                        """
                    }
                }
            }
            post {
                success {
                    echo "✅ Deployment verified - Version ${env.CALCULATED_VERSION} is running"
                }
            }
        }

        stage('Deploy Artifacts') {
            when {
                allOf {
                    branch 'develop'
                    expression { env.CHANGE_ID == null }
                    expression { currentBuild.result != 'FAILURE' }
                }
            }
            steps {
                script {
                    sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} deploy -P !dev,release -DskipTests=true"

                    if (env.IS_SNAPSHOT == 'false') {
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
        failure {
            script {
                def failureText = params.SHIP_IT_MODE ? "❌ Failed (🚀 SHIP IT)" : "❌ Failed"
                currentBuild.description = "${currentBuild.description ?: ''} | ${failureText}"
            }
        }
        success {
            script {
                def successText = params.SHIP_IT_MODE ? "✅ Success (🚀 SHIP IT)" : "✅ Success"
                currentBuild.description = "${currentBuild.description ?: ''} | ${successText}"
            }
        }
    }
}

def setupBuildEnvironment() {
    env.CALCULATED_VERSION = getProjectVersion()
    env.IS_SNAPSHOT = env.CALCULATED_VERSION.contains('SNAPSHOT').toString()
    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    env.GIT_BRANCH = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()

    currentBuild.displayName = "#${BUILD_NUMBER} - ${env.CALCULATED_VERSION}"
    currentBuild.description = "Branch: ${env.BRANCH_NAME} | Commit: ${env.GIT_COMMIT_SHORT}"
}

def getProjectVersion() {
    def version = sh(
        script: "${env.MVN_CMD} help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null | grep -E '^[0-9]' | head -1",
        returnStdout: true
    ).trim()

    if (!version) {
        error "Failed to extract version from pom.xml"
    }

    return version
}

def validateProject() {
    if (!fileExists('pom.xml')) {
        error "pom.xml not found"
    }

    def pomContent = readFile('pom.xml')
    if (!pomContent.contains("<groupId>${PROJECT_GROUP}</groupId>")) {
        error "Invalid groupId - expected ${PROJECT_GROUP}"
    }
    if (!pomContent.contains("<artifactId>${PROJECT_NAME}</artifactId>")) {
        error "Invalid artifactId - expected ${PROJECT_NAME}"
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

def cleanupOldNexusImages(retentionCount) {
    try {
        echo "🧹 Cleaning up old Docker images in Nexus (keeping latest ${retentionCount})"
        withCredentials([
            string(credentialsId: 'NEXUS_USERNAME', variable: 'NEXUS_USER'),
            string(credentialsId: 'NEXUS_PASSWORD', variable: 'NEXUS_PASS'),
            string(credentialsId: 'NEXUS_REGISTRY_URL', variable: 'NEXUS_URL')
        ]) {
            sh """
                curl -s -u "\${NEXUS_USER}:\${NEXUS_PASS}" \
                    "\${NEXUS_URL}/v2/${env.NEXUS_REPOSITORY_NAME}/${env.DOCKER_IMAGE_NAME}/tags/list" | \
                    jq -r '.tags[]' | grep -v latest | sort -V | head -n -${retentionCount} | \
                while read tag; do
                    echo "Deleting old tag: \$tag"
                    curl -X DELETE -u "\${NEXUS_USER}:\${NEXUS_PASS}" \
                        "\${NEXUS_URL}/v2/${env.NEXUS_REPOSITORY_NAME}/${env.DOCKER_IMAGE_NAME}/manifests/\$tag" || true
                done
            """
        }
        echo "✅ Cleanup completed"
    } catch (Exception e) {
        echo "⚠️ Cleanup failed: ${e.message}"
    }
}