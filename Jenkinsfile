pipeline {
    agent any

    parameters {
        booleanParam(name: 'RUN_INTEGRATION_TESTS', defaultValue: false, description: 'Run integration tests')
        booleanParam(name: 'RUN_ARCHITECTURE_TESTS', defaultValue: true, description: 'Run architecture tests')
        booleanParam(name: 'RUN_SPOTBUGS', defaultValue: false, description: 'Run SpotBugs static analysis')
        booleanParam(name: 'RUN_SECURITY_SCAN', defaultValue: false, description: 'Run OWASP security scanning')
        booleanParam(name: 'RUN_AI_CODE_REVIEW', defaultValue: true, description: 'Run AI code review with Claude')
        choice(name: 'AI_REVIEW_TYPE', choices: ['full', 'security', 'performance', 'quick'], description: 'Type of AI code review to perform')
        booleanParam(name: 'FORCE_DEPLOY', defaultValue: false, description: 'Force deployment even if quality gates fail')
        booleanParam(name: 'SHIP_IT_MODE', defaultValue: true, description: '🚀 Deadline mode: Quality stages run but never fail the pipeline')
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

        CLAUDE_REVIEW_SCRIPT = 'claude-code-review.sh'
        AI_REVIEW_OUTPUT_DIR = 'ai-review-reports'
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
                        echo "🚀 SHIP IT MODE ACTIVATED! Quality stages will run but won't block the pipeline"
                    }
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
                        script {
                            def buildResult = params.SHIP_IT_MODE ? 'SUCCESS' : 'UNSTABLE'
                            def stageResult = params.SHIP_IT_MODE ? 'SUCCESS' : 'UNSTABLE'

                            catchError(buildResult: buildResult, stageResult: stageResult) {
                                sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P!dev,error-prone compile"
                            }
                        }
                    }
                }
                stage('Checkstyle') {
                    steps {
                        script {
                            def buildResult = params.SHIP_IT_MODE ? 'SUCCESS' : 'UNSTABLE'
                            def stageResult = params.SHIP_IT_MODE ? 'SUCCESS' : 'UNSTABLE'

                            catchError(buildResult: buildResult, stageResult: stageResult) {
                                sh "${env.MVN_CMD} ${MAVEN_CLI_OPTS} -P!dev,quality-gate checkstyle:check"
                            }
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

                    if (params.SHIP_IT_MODE) {
                        echo "🚀 SHIP IT MODE: SonarQube quality gate will be checked but won't fail the pipeline"
                        timeout(time: 15, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                echo "⚠️ SonarQube quality gate failed: ${qg.status} (ignored due to SHIP IT MODE)"
                            } else {
                                echo "✅ SonarQube quality gate passed: ${qg.status}"
                            }
                        }
                    } else {
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
        }

        stage('AI Code Review') {
            when {
                allOf {
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
                    echo "🤖 Starting AI Code Review with Claude..."

                    sh "mkdir -p ${env.AI_REVIEW_OUTPUT_DIR}"

                    def reviewEnv = [
                        "REVIEW_TYPE=${params.AI_REVIEW_TYPE}",
                        "OUTPUT_FORMAT=json",
                        "MAX_TURNS=5"
                    ]

                    if (env.CHANGE_ID) {
                        reviewEnv.add("CHANGE_ID=${env.CHANGE_ID}")
                        reviewEnv.add("CHANGE_TARGET=${env.CHANGE_TARGET}")
                    }

                    def reviewResult = sh(
                        script: "${env.CLAUDE_REVIEW_SCRIPT}",
                        returnStatus: true,
                        env: reviewEnv
                    )

                    if (params.SHIP_IT_MODE) {
                        echo "🚀 SHIP IT MODE: AI Code Review will report but won't fail the pipeline"

                        switch(reviewResult) {
                            case 0:
                                echo "✅ AI Code Review completed successfully - No critical issues found"
                                break
                            case 1:
                                echo "⚠️ AI Code Review found warnings (ignored due to SHIP IT MODE)"
                                break
                            case 2:
                                echo "🔴 AI Code Review found critical issues (ignored due to SHIP IT MODE)"
                                echo "⚠️ Warning: Critical issues detected but ignored for deadline"
                                break
                            default:
                                echo "❌ AI Code Review script execution failed (ignored due to SHIP IT MODE)"
                        }
                    } else {
                        switch(reviewResult) {
                            case 0:
                                echo "✅ AI Code Review completed successfully - No critical issues found"
                                currentBuild.result = 'SUCCESS'
                                break
                            case 1:
                                echo "⚠️ AI Code Review found warnings - Failing pipeline"
                                error "AI Code Review failed: Warnings detected that require attention"
                                break
                            case 2:
                                echo "🔴 AI Code Review found critical issues - Failing pipeline"
                                error "AI Code Review failed: Critical issues detected that must be fixed"
                                break
                            default:
                                echo "❌ AI Code Review script execution failed"
                                error "AI Code Review failed: Script execution error (exit code: ${reviewResult})"
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        if (fileExists('claude-review-report.json')) {
                            archiveArtifacts artifacts: 'claude-review-report.json', allowEmptyArchive: true
                        }
                        if (fileExists('claude-review-report.md')) {
                            archiveArtifacts artifacts: 'claude-review-report.md', allowEmptyArchive: true
                        }

                        if (fileExists(env.AI_REVIEW_OUTPUT_DIR)) {
                            archiveArtifacts artifacts: "${env.AI_REVIEW_OUTPUT_DIR}/**/*", allowEmptyArchive: true
                        }
                    }
                }
                success {
                    script {
                        if (params.SHIP_IT_MODE) {
                            echo "🎉 AI Code Review stage completed - Issues ignored due to SHIP IT MODE"
                        } else {
                            echo "🎉 AI Code Review stage completed successfully"
                        }
                    }
                }
                failure {
                    script {
                        if (params.SHIP_IT_MODE) {
                            echo "🎉 AI Code Review stage completed - Issues ignored due to SHIP IT MODE"
                        } else {
                            echo "💥 AI Code Review stage failed - Check the reports for details"
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
        failure {
            script {
                def failureText = params.SHIP_IT_MODE ? "Build Failed (🚀 SHIP IT MODE)" : "Build Failed"
                if (currentBuild.description) {
                    currentBuild.description += " | ${failureText}"
                } else {
                    currentBuild.description = failureText
                }
            }
        }
        success {
            script {
                def successText = params.SHIP_IT_MODE ? "Build Successful (🚀 SHIP IT MODE)" : "Build Successful"
                if (currentBuild.description) {
                    currentBuild.description += " | ${successText}"
                } else {
                    currentBuild.description = successText
                }
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
