#!/usr/bin/env groovy

def sonarBranchName = env.BRANCH_NAME.contains('master') ? '-Dsonar.branch.name=master' : '-Dsonar.branch.name=' + env.BRANCH_NAME
def sonarBranchTarget = env.BRANCH_NAME.contains('master') ? '' : env.BRANCH_NAME == 'develop' ? '-Dsonar.branch.target=master' : '-Dsonar.branch.target=develop'

pipeline {

    agent { label '!master' }

    environment {
        JDK_VERSION = 'jdk-8-oracle'
        tesseractDir = tool name: 'Tesseract', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    }

    options {
        ansiColor('xterm')
        buildDiscarder(logRotator(artifactNumToKeepStr: '1'))
        parallelsAlwaysFailFast()
        retry(1)
        skipStagesAfterUnstable()
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
    }

    triggers {
        cron(env.BRANCH_NAME == 'develop' ? '@midnight' : '')
    }

    tools {
        maven 'M3'
        jdk "${JDK_VERSION}"
    }

    stages {
        stage('Clean workspace') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                withMaven(jdk: "${JDK_VERSION}", maven: 'M3') {
                    sh 'mvn clean'
                    sh 'mvn dependency:purge-local-repository -Dinclude=com.itextpdf -DresolutionFuzziness=groupId -DreResolve=false'
                }
            }
        }
        stage('Compile') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                withMaven(jdk: "${JDK_VERSION}", maven: 'M3') {
                    sh 'mvn compile test-compile package -Dmaven.test.skip=true -Dmaven.javadoc.failOnError=false'
                }
            }
        }
        stage('Run Tests') {
            options {
                timeout(time: 30, unit: 'MINUTES')
            }
            steps {
                withMaven(jdk: "${JDK_VERSION}", maven: 'M3', mavenLocalRepo: '.repository') {
                    withSonarQubeEnv('Sonar') {
                        sh 'mvn --activate-profiles test -DgsExec="${gsExec}"' +
                                ' -DcompareExec="${compareExec}" -DtesseractDir="${tesseractDir}" -Dmaven.test.skip=false -Dmaven.test.failure.ignore=false -Dmaven.javadoc.skip=true org.jacoco:jacoco-maven-plugin:prepare-agent verify org.jacoco:jacoco-maven-plugin:report -Dsonar.java.spotbugs.reportPaths="target/spotbugs.xml" sonar:sonar '
                    }
                }
            }
        }
        stage('Static Code Analysis') {
            options {
                timeout(time: 30, unit: 'MINUTES')
            }
            steps {
                withMaven(jdk: "${JDK_VERSION}", maven: 'M3', mavenLocalRepo: '.repository') {
                    sh 'mvn --activate-profiles qa verify -Dpmd.analysisCache=true -Dmaven.javadoc.skip=true'
                }
            }
        }
        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Artifactory Deploy') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            when {
                anyOf {
                    branch "master"
                    branch "develop"
                }
            }
            steps {
                script {
                    def server = Artifactory.server('itext-artifactory')
                    def rtMaven = Artifactory.newMavenBuild()
                    rtMaven.deployer server: server, snapshotRepo: 'snapshot'
                    rtMaven.tool = 'M3'
                    def buildInfo = rtMaven.run pom: 'pom.xml', goals: 'install -Dmaven.test.skip=true -Dspotbugs.skip=true -Dmaven.javadoc.failOnError=false'
                    server.publishBuildInfo buildInfo
                }
            }
        }
        stage('Archive Artifacts') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                archiveArtifacts allowEmptyArchive: true, artifacts: '**/*.jar'
            }
        }
    }

    post {
        always {
            echo 'One way or another, I have finished \uD83E\uDD16'
        }
        success {
            echo 'I succeeeded! \u263A'
        }
        unstable {
            echo 'I am unstable \uD83D\uDE2E'
        }
        failure {
            echo 'I failed \uD83D\uDCA9'
        }
        changed {
            echo 'Things were different before... \uD83E\uDD14'
        }
    }

}