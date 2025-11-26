pipeline {
    agent { label 'minikube' }

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
        // Maven Configuration
        MAVEN_OPTS = '-Xmx2048m -XX:+TieredCompilation -XX:TieredStopAtLevel=1'
        MAVEN_CLI_OPTS = '--errors --show-version --batch-mode --no-transfer-progress -U'
        MVN_CMD = 'mvn'

        // Project Configuration
        PROJECT_NAME = 'trade-loan'
        PROJECT_GROUP = 'ir.dotin.loan'

        // Docker & Registry
        DOCKER_IMAGE_NAME = 'trade-loan-service'
        NEXUS_REPOSITORY_NAME = 'expenditures'

        // Kubernetes
        K8S_NAMESPACE = 'default'

        // AI Review
        CLAUDE_REVIEW_SCRIPT = 'claude-code-review.sh'
        AI_REVIEW_OUTPUT_DIR = 'ai-review-reports'

        // Health Check
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
                sh "${MVN_CMD} ${MAVEN_CLI_OPTS} clean verify -P !dev -DskipITs=true"
            }
            post {
                always {
                    junit allowEmptyResults: false, testResults: '**/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Static Analysis') {
            when { expression { shouldRunQualityStage() } }
            parallel {
                stage('Error-Prone') {
                    steps {
                        runMavenWithCatch('-P !dev,error-prone compile')
                    }
                }
                stage('Checkstyle') {
                    steps {
                        runMavenWithCatch('-P !dev,quality-gate checkstyle:check')
                    }
                }
                stage('SpotBugs') {
                    when { expression { params.RUN_SPOTBUGS } }
                    steps {
                        runMavenWithCatch('-P !dev,quality-gate spotbugs:check')
                    }
                }
            }
        }

        stage('Advanced Tests') {
            when { expression { !params.SHIP_IT_MODE } }
            parallel {
                stage('Integration Tests') {
                    when { expression { params.RUN_INTEGRATION_TESTS } }
                    steps {
                        sh "${MVN_CMD} ${MAVEN_CLI_OPTS} verify -P !dev,integration-test -DskipUTs=true"
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
                        sh "${MVN_CMD} ${MAVEN_CLI_OPTS} test -P !dev,architecture-test"
                    }
                }
            }
        }

        stage('Security Scan') {
            when {
                expression { !params.SHIP_IT_MODE && params.RUN_SECURITY_SCAN }
            }
            steps {
                sh "${MVN_CMD} ${MAVEN_CLI_OPTS} -P security verify"
            }
        }

        stage('SonarQube Analysis') {
            when { expression { shouldRunQualityStage() } }
            environment {
                SONAR_TOKEN = credentials('SONAR_TOKEN_TRADE_LOAN')
            }
            steps {
                script {
                    runSonarAnalysis()
                    validateQualityGate()
                }
            }
        }

        stage('AI Code Review') {
            when {
                expression { shouldRunQualityStage() && params.RUN_AI_CODE_REVIEW && currentBuild.result != 'FAILURE' }
            }
            steps {
                script {
                    runAICodeReview()
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'claude-review-report.*', allowEmptyArchive: true
                    archiveArtifacts artifacts: "${AI_REVIEW_OUTPUT_DIR}/**/*", allowEmptyArchive: true
                }
            }
        }

        stage('Prepare Deployment') {
            when { expression { shouldDeploy() } }
            steps {
                dir('container') {
                    sh 'cp $HOME/nova/.env .'
                }
            }
        }

        stage('Build Docker Image') {
            when { expression { shouldDeploy() } }
            steps {
                dir('container') {
                    sh """
                        mkdir -p ca-certificates
                        cp -r \$HOME/nova/ca-certificates/* ca-certificates/ 2>/dev/null || true
                        docker build \
                            -t ${DOCKER_IMAGE_NAME}:${CALCULATED_VERSION} \
                            -t ${DOCKER_IMAGE_NAME}:latest \
                            -f Dockerfile .
                    """
                }
            }
            post {
                success { echo "✅ Docker image built: ${DOCKER_IMAGE_NAME}:${CALCULATED_VERSION}" }
            }
        }

        stage('Publish Docker Image to Nexus') {
            when {
                expression { shouldDeploy() && params.PUBLISH_TO_NEXUS }
            }
            steps {
                script {
                    publishToNexus()
                }
            }
            post {
                success { echo "✅ Docker image published: ${CALCULATED_VERSION}" }
            }
        }

        stage('Load Image to Minikube') {
            when { expression { shouldDeploy() } }
            steps {
                sh """
                    minikube image load ${DOCKER_IMAGE_NAME}:${CALCULATED_VERSION}
                    minikube image load ${DOCKER_IMAGE_NAME}:latest
                """
            }
            post {
                success { echo "✅ Images loaded to Minikube" }
            }
        }

        stage('Sync K8s Configs') {
            when { expression { shouldDeploy() } }
            steps {
                dir('container') { sh './scripts/sync-configs.sh' }
            }
        }

        stage('Create K8s Secrets') {
            when { expression { shouldDeploy() } }
            steps {
                dir('container') { sh './scripts/create-secrets.sh' }
            }
        }

        stage('Deploy to K8s') {
            when { expression { shouldDeploy() } }
            steps {
                dir('container') {
                    script { deployToKubernetes() }
                }
            }
            post {
                failure {
                    dir('container') {
                        script { handleDeploymentFailure() }
                    }
                }
            }
        }

        stage('Verify Deployment') {
            when { expression { shouldDeploy() } }
            steps {
                script {
                    timeout(time: 10, unit: 'MINUTES') {
                        verifyDeployment()
                    }
                }
            }
            post {
                success { echo "✅ Deployment verified - Version ${CALCULATED_VERSION} is running" }
            }
        }

        stage('Deploy Artifacts') {
            when { expression { shouldDeploy() } }
            steps {
                script {
                    sh "${MVN_CMD} ${MAVEN_CLI_OPTS} deploy -P !dev,release -DskipTests=true"
                    tagReleaseIfApplicable()
                }
            }
        }
    }

    post {
        always { cleanWs() }
        failure {
            script {
                def suffix = params.SHIP_IT_MODE ? "❌ Failed (🚀 SHIP IT)" : "❌ Failed"
                currentBuild.description = "${currentBuild.description ?: ''} | ${suffix}"
            }
        }
        success {
            script {
                def suffix = params.SHIP_IT_MODE ? "✅ Success (🚀 SHIP IT)" : "✅ Success"
                currentBuild.description = "${currentBuild.description ?: ''} | ${suffix}"
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONDITION HELPERS
// ═══════════════════════════════════════════════════════════════════════════════

def shouldRunQualityStage() {
    return !params.SHIP_IT_MODE && isQualityBranch()
}

def isQualityBranch() {
    return env.BRANCH_NAME in ['main', 'master', 'develop'] || env.CHANGE_ID != null
}

def shouldDeploy() {
    return env.BRANCH_NAME == 'develop' &&
           env.CHANGE_ID == null &&
           currentBuild.result != 'FAILURE'
}

// ═══════════════════════════════════════════════════════════════════════════════
// BUILD SETUP
// ═══════════════════════════════════════════════════════════════════════════════

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
        script: "${MVN_CMD} help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null | grep -E '^[0-9]' | head -1",
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
    ['groupId': PROJECT_GROUP, 'artifactId': PROJECT_NAME].each { tag, expected ->
        if (!pomContent.contains("<${tag}>${expected}</${tag}>")) {
            error "Invalid ${tag} - expected ${expected}"
        }
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
    ║ Maven       : ${MVN_CMD}
    ║ User        : ${env.BUILD_USER ?: 'System'}
    ╚════════════════════════════════════════╝
    """
}

// ═══════════════════════════════════════════════════════════════════════════════
// MAVEN HELPERS
// ═══════════════════════════════════════════════════════════════════════════════

def runMavenWithCatch(String goals) {
    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
        sh "${MVN_CMD} ${MAVEN_CLI_OPTS} ${goals}"
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// QUALITY GATES
// ═══════════════════════════════════════════════════════════════════════════════

def runSonarAnalysis() {
    withSonarQubeEnv('SonarQube') {
        def sonarParams = "-Dsonar.token=${SONAR_TOKEN}"
        if (env.CHANGE_ID) {
            sonarParams += " -Dsonar.pullrequest.key=${env.CHANGE_ID}"
            sonarParams += " -Dsonar.pullrequest.branch=${env.CHANGE_BRANCH}"
            sonarParams += " -Dsonar.pullrequest.base=${env.CHANGE_TARGET}"
        }
        sh "${MVN_CMD} ${MAVEN_CLI_OPTS} -P !dev,quality-gate sonar:sonar ${sonarParams}"
    }
}

def validateQualityGate() {
    timeout(time: 15, unit: 'MINUTES') {
        def qg = waitForQualityGate()
        if (qg.status != 'OK') {
            def message = "Quality gate failure: ${qg.status}"
            params.FORCE_DEPLOY ? unstable("${message} (FORCE_DEPLOY enabled)") : error("Pipeline aborted: ${message}")
        }
    }
}

def runAICodeReview() {
    sh "mkdir -p ${AI_REVIEW_OUTPUT_DIR}"

    if (!fileExists(CLAUDE_REVIEW_SCRIPT)) {
        error "Claude review script not found: ${CLAUDE_REVIEW_SCRIPT}"
    }

    def reviewResult = sh(
        script: """
            export REVIEW_TYPE="${params.AI_REVIEW_TYPE}"
            export OUTPUT_DIR="${AI_REVIEW_OUTPUT_DIR}"
            export GIT_REF="${GIT_COMMIT_SHORT}"
            export PROJECT_NAME="${PROJECT_NAME}"
            bash ${CLAUDE_REVIEW_SCRIPT}
        """,
        returnStatus: true
    )

    def resultMessages = [
        0: "✅ AI Code Review completed successfully",
        1: "⚠️ AI Code Review found warnings",
        2: "🔴 AI Code Review found critical issues"
    ]

    def message = resultMessages.get(reviewResult, "❌ AI Code Review script failed (exit: ${reviewResult})")
    reviewResult == 0 ? echo(message) : error(message)
}

// ═══════════════════════════════════════════════════════════════════════════════
// DOCKER & NEXUS
// ═══════════════════════════════════════════════════════════════════════════════

def publishToNexus() {
    withCredentials([
        string(credentialsId: 'NEXUS_REGISTRY_URL', variable: 'NEXUS_URL'),
        string(credentialsId: 'NEXUS_USERNAME', variable: 'NEXUS_USER'),
        string(credentialsId: 'NEXUS_PASSWORD', variable: 'NEXUS_PASS')
    ]) {
        def fullImagePath = "\${NEXUS_URL}/${NEXUS_REPOSITORY_NAME}/${DOCKER_IMAGE_NAME}"

        sh """
            echo "\${NEXUS_PASS}" | docker login \${NEXUS_URL} -u "\${NEXUS_USER}" --password-stdin

            docker tag ${DOCKER_IMAGE_NAME}:${CALCULATED_VERSION} ${fullImagePath}:${CALCULATED_VERSION}
            docker tag ${DOCKER_IMAGE_NAME}:latest ${fullImagePath}:latest

            docker push ${fullImagePath}:${CALCULATED_VERSION}
            docker push ${fullImagePath}:latest

            docker logout \${NEXUS_URL}
        """

        cleanupOldNexusImages(params.NEXUS_RETENTION_COUNT)
    }
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
                curl -sf -u "\${NEXUS_USER}:\${NEXUS_PASS}" \
                    "\${NEXUS_URL}/v2/${NEXUS_REPOSITORY_NAME}/${DOCKER_IMAGE_NAME}/tags/list" | \
                    jq -r '.tags[]' | grep -v latest | sort -V | head -n -${retentionCount} | \
                while read tag; do
                    echo "Deleting old tag: \$tag"
                    curl -X DELETE -u "\${NEXUS_USER}:\${NEXUS_PASS}" \
                        "\${NEXUS_URL}/v2/${NEXUS_REPOSITORY_NAME}/${DOCKER_IMAGE_NAME}/manifests/\$tag" || true
                done
            """
        }
        echo "✅ Cleanup completed"
    } catch (Exception e) {
        echo "⚠️ Cleanup failed: ${e.message}"
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KUBERNETES DEPLOYMENT
// ═══════════════════════════════════════════════════════════════════════════════

def deployToKubernetes() {
    sh """
        kubectl apply -f k8s/base/rbac.yml -n ${K8S_NAMESPACE}
        kubectl apply -f k8s/base/configmap-trade-loan.yml -n ${K8S_NAMESPACE}
        kubectl apply -f k8s/base/service.yml -n ${K8S_NAMESPACE}

        # Configure for Minikube local image
        sed -i 's|image:.*trade-loan-service:.*|image: trade-loan-service:${CALCULATED_VERSION}|g' k8s/base/deployment.yml
        sed -i 's|imagePullPolicy:.*|imagePullPolicy: Never|g' k8s/base/deployment.yml

        kubectl apply -f k8s/base/deployment.yml -n ${K8S_NAMESPACE}

        timeout 30m kubectl rollout status deployment/${DOCKER_IMAGE_NAME} -n ${K8S_NAMESPACE} || {
            echo "❌ Deployment timeout - Gathering diagnostics..."
            kubectl describe deployment/${DOCKER_IMAGE_NAME} -n ${K8S_NAMESPACE}
            kubectl get pods -n ${K8S_NAMESPACE} -l app=${DOCKER_IMAGE_NAME}
            kubectl logs -n ${K8S_NAMESPACE} -l app=${DOCKER_IMAGE_NAME} --tail=500 || true
            exit 1
        }
    """
}

def handleDeploymentFailure() {
    sh """
        echo "❌ Deployment failed, capturing final state..."
        kubectl describe deployment/${DOCKER_IMAGE_NAME} -n ${K8S_NAMESPACE} || true
        kubectl describe pods -n ${K8S_NAMESPACE} -l app=${DOCKER_IMAGE_NAME} || true
        kubectl logs -n ${K8S_NAMESPACE} -l app=${DOCKER_IMAGE_NAME} --tail=1000 || true

        echo "Rolling back..."
        kubectl rollout undo deployment/${DOCKER_IMAGE_NAME} -n ${K8S_NAMESPACE} || true
    """
}

def verifyDeployment() {
    sh """
        kubectl wait --for=condition=ready pod \
            -l app=${DOCKER_IMAGE_NAME} \
            -n ${K8S_NAMESPACE} \
            --timeout=600s
    """

    def maxRetries = 20
    def healthCheckPassed = false

    for (int i = 1; i <= maxRetries && !healthCheckPassed; i++) {
        sleep 15
        def status = sh(script: "curl -sf ${HEALTH_CHECK_ENDPOINT} | grep -q 'UP'", returnStatus: true)

        if (status == 0) {
            healthCheckPassed = true
            echo "✅ Health check passed after ${i} attempts"
        } else {
            echo "⏳ Health check attempt ${i}/${maxRetries} failed, retrying..."
        }
    }

    if (!healthCheckPassed) {
        error "❌ Health check failed after ${maxRetries} retries"
    }

    sh """
        echo "📊 Final deployment status:"
        kubectl get pods -n ${K8S_NAMESPACE} -l app=${DOCKER_IMAGE_NAME}
        kubectl describe deployment/${DOCKER_IMAGE_NAME} -n ${K8S_NAMESPACE} | grep Image:
    """
}

def tagReleaseIfApplicable() {
    if (env.IS_SNAPSHOT == 'false') {
        sh """
            git tag -a v${CALCULATED_VERSION} -m "Release version ${CALCULATED_VERSION}"
            git push origin v${CALCULATED_VERSION}
        """
    }
}