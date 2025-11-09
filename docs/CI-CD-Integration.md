# CI/CD Integration Guide for SonarQube Mathematica Plugin

**Complete setup examples for continuous code quality analysis**

This guide provides copy-paste ready configurations for integrating the SonarQube Mathematica Plugin into your CI/CD pipeline. Each example includes authentication setup, quality gate configuration, and badge setup.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [GitHub Actions](#github-actions)
3. [GitLab CI](#gitlab-ci)
4. [Jenkins](#jenkins)
5. [Azure Pipelines](#azure-pipelines)
6. [CircleCI](#circleci)
7. [Quality Gates](#quality-gates)
8. [Badges and Status](#badges-and-status)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### 1. SonarQube Server Setup

You need a running SonarQube server (self-hosted or SonarCloud).

**Self-hosted SonarQube:**
```bash
# Using Docker
docker run -d --name sonarqube \
  -p 9000:9000 \
  sonarqube:latest

# Access at http://localhost:9000
# Default credentials: admin/admin
```

**Or use SonarCloud:**
- Sign up at https://sonarcloud.io
- Free for open source projects

### 2. Generate Authentication Token

**SonarQube Server:**
1. Log in to SonarQube
2. Go to **My Account** → **Security**
3. Generate a token with name "CI/CD"
4. Copy and save the token securely

**SonarCloud:**
1. Go to https://sonarcloud.io/account/security
2. Generate a token
3. Save it securely

### 3. Install the Mathematica Plugin

**On SonarQube Server:**
1. Download the latest `wolfralyze-X.X.X.jar`
2. Copy to `$SONARQUBE_HOME/extensions/plugins/`
3. Restart SonarQube
4. Verify: **Administration** → **Marketplace** → **Installed**

**On SonarCloud:**
- Contact SonarCloud support to enable the plugin

### 4. Create sonar-project.properties

Create this file in your repository root:

```properties
# Project identification
sonar.projectKey=my-organization_my-mathematica-project
sonar.projectName=My Mathematica Project
sonar.projectVersion=1.0.0

# Source code location
sonar.sources=src
sonar.tests=tests

# Source encoding
sonar.sourceEncoding=UTF-8

# Language
sonar.language=mathematica

# File patterns
sonar.inclusions=**/*.m,**/*.wl,**/*.wlt

# Exclusions (optional)
sonar.exclusions=**/vendor/**,**/build/**,**/dist/**

# Coverage (if available)
# sonar.coverageReportPaths=coverage.xml

# Quality gate (optional)
# sonar.qualitygate.wait=true
sonar.qualitygate.timeout=300
```

---

## GitHub Actions

### Complete Workflow Configuration

Create `.github/workflows/sonarqube.yml`:

```yaml
name: SonarQube Analysis

on:
  push:
    branches:
      - main
      - master
      - develop
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  sonarqube:
    name: SonarQube Scan
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          # Shallow clones should be disabled for better analysis
          fetch-depth: 0

      - name: SonarQube Scan
        uses: sonarsource/sonarqube-scan-action@master
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
        with:
          # Optional: additional CLI arguments
          args: >
            -Dsonar.projectKey=${{ github.repository_owner }}_${{ github.event.repository.name }}
            -Dsonar.projectName=${{ github.event.repository.name }}

      # Optional: Check quality gate status
      - name: Check Quality Gate
        uses: sonarsource/sonarqube-quality-gate-action@master
        timeout-minutes: 5
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}

      # Optional: Upload results as artifacts
      - name: Upload SonarQube Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: sonarqube-results
          path: .scannerwork/

      # Optional: Comment PR with results
      - name: Comment PR with SonarQube Results
        if: github.event_name == 'pull_request'
        uses: actions/github-script@v7
        with:
          script: |
            const sonarUrl = process.env.SONAR_HOST_URL;
            const projectKey = '${{ github.repository_owner }}_${{ github.event.repository.name }}';
            const dashboardUrl = `${sonarUrl}/dashboard?id=${projectKey}`;

            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: `## SonarQube Analysis Complete\n\n[View Results](${dashboardUrl})`
            });
        env:
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
```

### Setup GitHub Secrets

1. Go to your repository → **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add these secrets:
   - `SONAR_TOKEN`: Your SonarQube authentication token
   - `SONAR_HOST_URL`: Your SonarQube server URL (e.g., `https://sonarqube.mycompany.com`)

### For SonarCloud

Use this simplified workflow for SonarCloud:

```yaml
name: SonarCloud Analysis

on:
  push:
    branches:
      - main
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  sonarcloud:
    name: SonarCloud Scan
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: SonarCloud Scan
        uses: SonarSource/sonarcloud-github-action@master
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        with:
          args: >
            -Dsonar.organization=${{ github.repository_owner }}
            -Dsonar.projectKey=${{ github.repository_owner }}_${{ github.event.repository.name }}
```

**SonarCloud Secrets:**
- `SONAR_TOKEN`: Generate at https://sonarcloud.io/account/security
- `GITHUB_TOKEN`: Automatically provided by GitHub Actions

---

## GitLab CI

### Complete Pipeline Configuration

Create `.gitlab-ci.yml`:

```yaml
stages:
  - test
  - quality

variables:
  SONAR_USER_HOME: "${CI_PROJECT_DIR}/.sonar"
  GIT_DEPTH: "0"  # Shallow clones disabled for better analysis

# Cache SonarQube scanner
cache:
  key: "${CI_JOB_NAME}"
  paths:
    - .sonar/cache

sonarqube-check:
  stage: quality
  image:
    name: sonarsource/sonar-scanner-cli:latest
    entrypoint: [""]

  script:
    - sonar-scanner
      -Dsonar.projectKey=${CI_PROJECT_PATH_SLUG}
      -Dsonar.projectName=${CI_PROJECT_NAME}
      -Dsonar.projectVersion=${CI_COMMIT_SHORT_SHA}
      -Dsonar.sources=src
      -Dsonar.tests=tests
      -Dsonar.host.url=${SONAR_HOST_URL}
      -Dsonar.token=${SONAR_TOKEN}
      -Dsonar.gitlab.project_id=${CI_PROJECT_ID}
      -Dsonar.gitlab.commit_sha=${CI_COMMIT_SHA}
      -Dsonar.gitlab.ref_name=${CI_COMMIT_REF_NAME}

  allow_failure: false

  only:
    - merge_requests
    - main
    - master
    - develop

  artifacts:
    paths:
      - .scannerwork/
    expire_in: 1 day

# Optional: Quality gate check
sonarqube-quality-gate:
  stage: quality
  image:
    name: sonarsource/sonar-scanner-cli:latest
    entrypoint: [""]

  script:
    - |
      # Wait for quality gate status
      TASK_URL=$(cat .scannerwork/report-task.txt | grep ceTaskUrl | cut -d'=' -f2)
      echo "Checking task status at: $TASK_URL"

      # Poll for task completion
      for i in {1..30}; do
        TASK_STATUS=$(curl -s -u "${SONAR_TOKEN}:" "${TASK_URL}" | jq -r '.task.status')
        echo "Task status: $TASK_STATUS"

        if [ "$TASK_STATUS" = "SUCCESS" ]; then
          break
        elif [ "$TASK_STATUS" = "FAILED" ] || [ "$TASK_STATUS" = "CANCELED" ]; then
          echo "Task failed or was canceled"
          exit 1
        fi

        sleep 10
      done

      # Get quality gate status
      ANALYSIS_ID=$(curl -s -u "${SONAR_TOKEN}:" "${TASK_URL}" | jq -r '.task.analysisId')
      QG_STATUS=$(curl -s -u "${SONAR_TOKEN}:" \
        "${SONAR_HOST_URL}/api/qualitygates/project_status?analysisId=${ANALYSIS_ID}" \
        | jq -r '.projectStatus.status')

      echo "Quality Gate Status: $QG_STATUS"

      if [ "$QG_STATUS" != "OK" ]; then
        echo "Quality gate failed!"
        exit 1
      fi

  dependencies:
    - sonarqube-check

  only:
    - merge_requests
    - main
    - master
```

### Setup GitLab CI/CD Variables

1. Go to your project → **Settings** → **CI/CD** → **Variables**
2. Click **Add variable**
3. Add these variables:
   - `SONAR_TOKEN`: Your SonarQube token (masked)
   - `SONAR_HOST_URL`: Your SonarQube URL (e.g., `https://sonarqube.mycompany.com`)

### With Docker Compose (Self-hosted SonarQube)

```yaml
stages:
  - test
  - quality

services:
  - docker:dind

sonarqube-check:
  stage: quality
  image: docker:latest

  services:
    - name: sonarqube:latest
      alias: sonarqube

  before_script:
    - apk add --no-cache curl jq
    - docker-compose up -d sonarqube
    - sleep 30  # Wait for SonarQube to start

  script:
    - docker run --rm
      --network host
      -v "${CI_PROJECT_DIR}:/usr/src"
      sonarsource/sonar-scanner-cli
      -Dsonar.projectKey=${CI_PROJECT_PATH_SLUG}
      -Dsonar.host.url=http://sonarqube:9000
      -Dsonar.token=${SONAR_TOKEN}

  only:
    - merge_requests
    - main
```

---

## Jenkins

### Complete Jenkinsfile

Create `Jenkinsfile` in your repository root:

```groovy
pipeline {
    agent any

    environment {
        // SonarQube server configuration (defined in Jenkins global config)
        SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
        SONAR_HOST_URL = credentials('sonar-host-url')
        SONAR_TOKEN = credentials('sonar-token')
    }

    options {
        // Keep only last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))

        // Disable concurrent builds
        disableConcurrentBuilds()

        // Timeout after 30 minutes
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from SCM
                checkout scm

                // Display git info
                sh '''
                    echo "Branch: ${GIT_BRANCH}"
                    echo "Commit: ${GIT_COMMIT}"
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    // Run SonarQube scanner
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            ${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                                -Dsonar.projectKey=${env.JOB_NAME} \
                                -Dsonar.projectName='${env.JOB_NAME}' \
                                -Dsonar.projectVersion=${env.BUILD_NUMBER} \
                                -Dsonar.sources=src \
                                -Dsonar.tests=tests \
                                -Dsonar.sourceEncoding=UTF-8 \
                                -Dsonar.language=mathematica \
                                -Dsonar.inclusions=**/*.m,**/*.wl,**/*.wlt \
                                -Dsonar.branch.name=${env.GIT_BRANCH}
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    // Wait for quality gate result
                    timeout(time: 5, unit: 'MINUTES') {
                        def qg = waitForQualityGate()

                        if (qg.status != 'OK') {
                            error "Quality Gate failed: ${qg.status}"
                        } else {
                            echo "Quality Gate passed!"
                        }
                    }
                }
            }
        }

        stage('Publish Results') {
            steps {
                script {
                    // Archive SonarQube results
                    archiveArtifacts artifacts: '.scannerwork/**/*', allowEmptyArchive: true

                    // Generate report URL
                    def projectKey = env.JOB_NAME
                    def dashboardUrl = "${SONAR_HOST_URL}/dashboard?id=${projectKey}"

                    echo "SonarQube Dashboard: ${dashboardUrl}"

                    // Set build description with link
                    currentBuild.description = "<a href='${dashboardUrl}'>SonarQube Report</a>"
                }
            }
        }
    }

    post {
        always {
            // Clean workspace
            cleanWs()
        }

        success {
            echo 'Pipeline succeeded!'

            // Optional: Send notification
            // emailext (
            //     subject: "Jenkins Build ${currentBuild.fullDisplayName} - Success",
            //     body: "Build successful. SonarQube analysis complete.",
            //     to: "team@example.com"
            // )
        }

        failure {
            echo 'Pipeline failed!'

            // Optional: Send notification
            // emailext (
            //     subject: "Jenkins Build ${currentBuild.fullDisplayName} - Failed",
            //     body: "Build failed. Check SonarQube results.",
            //     to: "team@example.com"
            // )
        }
    }
}
```

### Jenkins Setup

#### 1. Install Required Plugins

Go to **Manage Jenkins** → **Manage Plugins** → **Available**:
- SonarQube Scanner for Jenkins
- Pipeline
- Git Plugin
- Credentials Plugin

#### 2. Configure SonarQube Server

Go to **Manage Jenkins** → **Configure System** → **SonarQube servers**:
1. Click **Add SonarQube**
2. Name: `SonarQube`
3. Server URL: `https://sonarqube.mycompany.com`
4. Server authentication token: (select credential)

#### 3. Add Credentials

Go to **Manage Jenkins** → **Manage Credentials**:
1. Click **Global** → **Add Credentials**
2. Kind: **Secret text**
3. Secret: (paste your SonarQube token)
4. ID: `sonar-token`
5. Description: `SonarQube Authentication Token`

Add another credential:
1. Kind: **Secret text**
2. Secret: `https://sonarqube.mycompany.com`
3. ID: `sonar-host-url`
4. Description: `SonarQube Server URL`

#### 4. Install SonarQube Scanner

Go to **Manage Jenkins** → **Global Tool Configuration** → **SonarQube Scanner**:
1. Click **Add SonarQube Scanner**
2. Name: `SonarQubeScanner`
3. Install automatically: ✅
4. Version: Latest

### Multibranch Pipeline Configuration

For automatic branch analysis, create a Multibranch Pipeline:

```groovy
pipeline {
    agent any

    stages {
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        sonar-scanner \
                            -Dsonar.projectKey=${env.JOB_BASE_NAME} \
                            -Dsonar.branch.name=${env.BRANCH_NAME} \
                            -Dsonar.sources=src
                    """
                }
            }
        }

        stage('Quality Gate') {
            when {
                branch 'main'
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
```

---

## Azure Pipelines

### Complete Pipeline Configuration

Create `azure-pipelines.yml`:

```yaml
trigger:
  branches:
    include:
      - main
      - master
      - develop
  paths:
    include:
      - src/**
      - tests/**

pr:
  branches:
    include:
      - main
      - master

pool:
  vmImage: 'ubuntu-latest'

variables:
  - group: sonarqube-vars  # Variable group containing SONAR_TOKEN and SONAR_HOST_URL
  - name: SONAR_USER_HOME
    value: $(Pipeline.Workspace)/.sonar

stages:
  - stage: CodeQuality
    displayName: 'Code Quality Analysis'
    jobs:
      - job: SonarQube
        displayName: 'SonarQube Scan'

        steps:
          - checkout: self
            fetchDepth: 0  # Shallow clones disabled for better analysis
            clean: true

          - task: SonarQubePrepare@5
            displayName: 'Prepare SonarQube Analysis'
            inputs:
              SonarQube: 'SonarQube-Connection'  # Service connection name
              scannerMode: 'CLI'
              configMode: 'manual'
              cliProjectKey: '$(Build.Repository.Name)'
              cliProjectName: '$(Build.Repository.Name)'
              cliProjectVersion: '$(Build.BuildNumber)'
              cliSources: 'src'
              extraProperties: |
                sonar.sources=src
                sonar.tests=tests
                sonar.sourceEncoding=UTF-8
                sonar.language=mathematica
                sonar.inclusions=**/*.m,**/*.wl,**/*.wlt
                sonar.exclusions=**/vendor/**,**/build/**
                sonar.branch.name=$(Build.SourceBranchName)

          - task: SonarQubeAnalyze@5
            displayName: 'Run SonarQube Analysis'
            timeoutInMinutes: 10

          - task: SonarQubePublish@5
            displayName: 'Publish Quality Gate Result'
            inputs:
              pollingTimeoutSec: '300'

          # Optional: Display results
          - task: Bash@3
            displayName: 'Display SonarQube Results'
            inputs:
              targetType: 'inline'
              script: |
                echo "SonarQube Analysis Complete"
                echo "Dashboard URL: $(SONAR_HOST_URL)/dashboard?id=$(Build.Repository.Name)"

                if [ -f .scannerwork/report-task.txt ]; then
                  cat .scannerwork/report-task.txt
                fi

          # Optional: Break build on quality gate failure
          - task: PowerShell@2
            displayName: 'Check Quality Gate'
            condition: and(succeeded(), eq(variables['Build.Reason'], 'PullRequest'))
            inputs:
              targetType: 'inline'
              script: |
                $taskFile = Get-Content .scannerwork/report-task.txt
                $ceTaskUrl = ($taskFile | Select-String -Pattern "ceTaskUrl=(.*)").Matches.Groups[1].Value

                Write-Host "Task URL: $ceTaskUrl"

                # Wait for task to complete
                $maxAttempts = 30
                $attempt = 0
                $taskStatus = ""

                while ($attempt -lt $maxAttempts -and $taskStatus -ne "SUCCESS") {
                    Start-Sleep -Seconds 10
                    $attempt++

                    $response = Invoke-RestMethod -Uri $ceTaskUrl `
                        -Headers @{Authorization = "Bearer $(SONAR_TOKEN)"} `
                        -Method Get

                    $taskStatus = $response.task.status
                    Write-Host "Attempt $attempt : Task Status = $taskStatus"

                    if ($taskStatus -eq "FAILED" -or $taskStatus -eq "CANCELED") {
                        Write-Error "SonarQube task failed"
                        exit 1
                    }
                }

                # Get quality gate status
                $analysisId = $response.task.analysisId
                $qgUrl = "$(SONAR_HOST_URL)/api/qualitygates/project_status?analysisId=$analysisId"

                $qgResponse = Invoke-RestMethod -Uri $qgUrl `
                    -Headers @{Authorization = "Bearer $(SONAR_TOKEN)"} `
                    -Method Get

                $qgStatus = $qgResponse.projectStatus.status
                Write-Host "Quality Gate Status: $qgStatus"

                if ($qgStatus -ne "OK") {
                    Write-Error "Quality Gate Failed"
                    exit 1
                }

          # Optional: Publish artifacts
          - publish: $(System.DefaultWorkingDirectory)/.scannerwork
            artifact: sonarqube-results
            displayName: 'Publish SonarQube Results'
            condition: always()
```

### Azure DevOps Setup

#### 1. Install SonarQube Extension

1. Go to **Organization Settings** → **Extensions**
2. Browse Marketplace
3. Search for "SonarQube"
4. Install **SonarQube** extension

#### 2. Create Service Connection

1. Go to **Project Settings** → **Service connections**
2. Click **New service connection**
3. Select **SonarQube**
4. Fill in:
   - **Server URL**: `https://sonarqube.mycompany.com`
   - **Token**: (paste your SonarQube token)
   - **Service connection name**: `SonarQube-Connection`
5. Click **Save**

#### 3. Create Variable Group

1. Go to **Pipelines** → **Library**
2. Click **+ Variable group**
3. Name: `sonarqube-vars`
4. Add variables:
   - `SONAR_TOKEN`: (your token, mark as secret)
   - `SONAR_HOST_URL`: `https://sonarqube.mycompany.com`
5. Click **Save**

### Alternative: Using Docker

```yaml
stages:
  - stage: CodeQuality
    jobs:
      - job: SonarQube
        container:
          image: sonarsource/sonar-scanner-cli:latest

        steps:
          - checkout: self
            fetchDepth: 0

          - script: |
              sonar-scanner \
                -Dsonar.projectKey=$(Build.Repository.Name) \
                -Dsonar.sources=src \
                -Dsonar.host.url=$(SONAR_HOST_URL) \
                -Dsonar.token=$(SONAR_TOKEN)
            displayName: 'Run SonarQube Scanner'
```

---

## CircleCI

### Complete Configuration

Create `.circleci/config.yml`:

```yaml
version: 2.1

orbs:
  sonarcloud: sonarsource/sonarcloud@2.0.0

workflows:
  main:
    jobs:
      - sonarqube-analysis:
          context: sonarqube
          filters:
            branches:
              only:
                - main
                - master
                - develop

jobs:
  sonarqube-analysis:
    docker:
      - image: cimg/base:stable

    environment:
      SONAR_SCANNER_VERSION: 5.0.1.3006

    steps:
      - checkout

      # Install SonarQube Scanner
      - run:
          name: Install SonarQube Scanner
          command: |
            mkdir -p $HOME/sonar-scanner
            cd $HOME/sonar-scanner

            if [ ! -f sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip ]; then
              wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip
              unzip sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip
            fi

            export PATH=$HOME/sonar-scanner/sonar-scanner-${SONAR_SCANNER_VERSION}-linux/bin:$PATH
            echo 'export PATH=$HOME/sonar-scanner/sonar-scanner-'${SONAR_SCANNER_VERSION}'-linux/bin:$PATH' >> $BASH_ENV

      # Cache SonarQube scanner
      - save_cache:
          key: sonar-scanner-{{ .Environment.SONAR_SCANNER_VERSION }}
          paths:
            - ~/sonar-scanner

      - restore_cache:
          keys:
            - sonar-scanner-{{ .Environment.SONAR_SCANNER_VERSION }}

      # Run SonarQube analysis
      - run:
          name: Run SonarQube Analysis
          command: |
            sonar-scanner \
              -Dsonar.projectKey=${CIRCLE_PROJECT_USERNAME}_${CIRCLE_PROJECT_REPONAME} \
              -Dsonar.projectName=${CIRCLE_PROJECT_REPONAME} \
              -Dsonar.projectVersion=${CIRCLE_SHA1} \
              -Dsonar.sources=src \
              -Dsonar.tests=tests \
              -Dsonar.sourceEncoding=UTF-8 \
              -Dsonar.language=mathematica \
              -Dsonar.inclusions=**/*.m,**/*.wl,**/*.wlt \
              -Dsonar.host.url=${SONAR_HOST_URL} \
              -Dsonar.token=${SONAR_TOKEN} \
              -Dsonar.branch.name=${CIRCLE_BRANCH}

      # Check quality gate
      - run:
          name: Check Quality Gate
          command: |
            # Extract task URL
            TASK_URL=$(cat .scannerwork/report-task.txt | grep ceTaskUrl | cut -d'=' -f2)
            echo "Task URL: $TASK_URL"

            # Wait for task completion
            for i in {1..30}; do
              sleep 10

              TASK_STATUS=$(curl -s -u "${SONAR_TOKEN}:" "${TASK_URL}" | jq -r '.task.status')
              echo "Attempt $i: Task Status = $TASK_STATUS"

              if [ "$TASK_STATUS" = "SUCCESS" ]; then
                break
              elif [ "$TASK_STATUS" = "FAILED" ] || [ "$TASK_STATUS" = "CANCELED" ]; then
                echo "Task failed"
                exit 1
              fi
            done

            # Get quality gate status
            ANALYSIS_ID=$(curl -s -u "${SONAR_TOKEN}:" "${TASK_URL}" | jq -r '.task.analysisId')
            QG_URL="${SONAR_HOST_URL}/api/qualitygates/project_status?analysisId=${ANALYSIS_ID}"

            QG_STATUS=$(curl -s -u "${SONAR_TOKEN}:" "${QG_URL}" | jq -r '.projectStatus.status')
            echo "Quality Gate Status: $QG_STATUS"

            if [ "$QG_STATUS" != "OK" ]; then
              echo "Quality Gate Failed!"
              exit 1
            fi

            echo "Quality Gate Passed!"

      # Store artifacts
      - store_artifacts:
          path: .scannerwork
          destination: sonarqube-results

      # Display dashboard URL
      - run:
          name: Display Results
          command: |
            PROJECT_KEY="${CIRCLE_PROJECT_USERNAME}_${CIRCLE_PROJECT_REPONAME}"
            DASHBOARD_URL="${SONAR_HOST_URL}/dashboard?id=${PROJECT_KEY}"
            echo "SonarQube Dashboard: $DASHBOARD_URL"
```

### CircleCI Setup

#### 1. Create Context

1. Go to **Organization Settings** → **Contexts**
2. Click **Create Context**
3. Name: `sonarqube`
4. Add environment variables:
   - `SONAR_TOKEN`: (your token)
   - `SONAR_HOST_URL`: `https://sonarqube.mycompany.com`

#### 2. Alternative: Project Environment Variables

1. Go to project → **Project Settings** → **Environment Variables**
2. Add:
   - `SONAR_TOKEN`
   - `SONAR_HOST_URL`

### For SonarCloud

Simpler configuration using the SonarCloud orb:

```yaml
version: 2.1

orbs:
  sonarcloud: sonarsource/sonarcloud@2.0.0

workflows:
  main:
    jobs:
      - sonarcloud/scan:
          context: sonarcloud

# Or customize:
workflows:
  main:
    jobs:
      - build:
          context: sonarcloud

jobs:
  build:
    docker:
      - image: cimg/base:stable

    steps:
      - checkout
      - sonarcloud/scan
```

---

## Quality Gates

### Default Quality Gate

SonarQube includes a default quality gate. You can customize it for Mathematica projects.

### Creating a Custom Quality Gate

1. Go to **Quality Gates** in SonarQube
2. Click **Create**
3. Name: `Mathematica Quality Gate`
4. Add conditions:

**On New Code:**
- Coverage: ≥ 80%
- Duplicated Lines: ≤ 3%
- Maintainability Rating: ≥ A
- Reliability Rating: ≥ A
- Security Rating: ≥ A
- Security Hotspots Reviewed: = 100%

**On Overall Code:**
- Blocker Issues: = 0
- Critical Issues: = 0
- Code Smells: ≤ 100 (adjust based on project size)

### Applying Quality Gate to Project

**Via UI:**
1. Go to your project → **Project Settings** → **Quality Gate**
2. Select `Mathematica Quality Gate`
3. Click **Save**

**Via sonar-project.properties:**
```properties
sonar.qualitygate=Mathematica Quality Gate
sonar.qualitygate.wait=true
sonar.qualitygate.timeout=300
```

### Breaking Builds on Quality Gate Failure

Most CI/CD examples above include quality gate checks. Ensure:
- `sonar.qualitygate.wait=true` in your config
- CI pipeline has a step to check quality gate status
- Build fails if quality gate fails

---

## Badges and Status

### GitHub Badge

Add to your `README.md`:

```markdown
[![Quality Gate Status](https://sonarqube.mycompany.com/api/project_badges/measure?project=my-project-key&metric=alert_status)](https://sonarqube.mycompany.com/dashboard?id=my-project-key)

[![Bugs](https://sonarqube.mycompany.com/api/project_badges/measure?project=my-project-key&metric=bugs)](https://sonarqube.mycompany.com/dashboard?id=my-project-key)

[![Code Smells](https://sonarqube.mycompany.com/api/project_badges/measure?project=my-project-key&metric=code_smells)](https://sonarqube.mycompany.com/dashboard?id=my-project-key)

[![Coverage](https://sonarqube.mycompany.com/api/project_badges/measure?project=my-project-key&metric=coverage)](https://sonarqube.mycompany.com/dashboard?id=my-project-key)

[![Duplicated Lines (%)](https://sonarqube.mycompany.com/api/project_badges/measure?project=my-project-key&metric=duplicated_lines_density)](https://sonarqube.mycompany.com/dashboard?id=my-project-key)

[![Security Rating](https://sonarqube.mycompany.com/api/project_badges/measure?project=my-project-key&metric=security_rating)](https://sonarqube.mycompany.com/dashboard?id=my-project-key)

[![Vulnerabilities](https://sonarqube.mycompany.com/api/project_badges/measure?project=my-project-key&metric=vulnerabilities)](https://sonarqube.mycompany.com/dashboard?id=my-project-key)
```

Replace:
- `sonarqube.mycompany.com` with your SonarQube URL
- `my-project-key` with your project key

### SonarCloud Badge

```markdown
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my-org_my-repo&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my-org_my-repo)

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=my-org_my-repo&metric=bugs)](https://sonarcloud.io/summary/new_code?id=my-org_my-repo)

[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=my-org_my-repo&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=my-org_my-repo)
```

### All Available Metrics for Badges

- `alert_status` - Quality Gate Status
- `bugs` - Bugs
- `code_smells` - Code Smells
- `coverage` - Coverage
- `duplicated_lines_density` - Duplicated Lines (%)
- `ncloc` - Lines of Code
- `sqale_rating` - Maintainability Rating
- `reliability_rating` - Reliability Rating
- `security_rating` - Security Rating
- `security_hotspots_reviewed` - Security Hotspots Reviewed
- `sqale_index` - Technical Debt
- `vulnerabilities` - Vulnerabilities

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Authentication Failed

**Error:** `Not authorized. Please check the properties sonar.token`

**Solution:**
- Verify token is correct
- Check token hasn't expired
- Ensure token has correct permissions
- Verify `SONAR_TOKEN` environment variable is set

#### 2. Project Not Found

**Error:** `Project 'my-project-key' not found`

**Solution:**
- Create project in SonarQube first
- Or let automatic provisioning create it
- Check project key matches exactly

#### 3. Plugin Not Found

**Error:** `Language 'mathematica' not found`

**Solution:**
- Verify plugin is installed: **Administration** → **Marketplace**
- Restart SonarQube after installing plugin
- Check plugin version is compatible with SonarQube version

#### 4. Scanner Fails to Download

**Error:** `Unable to download SonarScanner`

**Solution:**
- Check network connectivity
- Use a specific version instead of `latest`
- Download scanner manually and cache it

#### 5. Quality Gate Timeout

**Error:** `Quality gate timeout after 300 seconds`

**Solution:**
- Increase timeout: `sonar.qualitygate.timeout=600`
- Check SonarQube server performance
- Verify background tasks are processing

#### 6. Out of Memory

**Error:** `java.lang.OutOfMemoryError`

**Solution:**
```bash
# Increase heap size
export SONAR_SCANNER_OPTS="-Xmx2048m"

# Or in CI config
env:
  SONAR_SCANNER_OPTS: "-Xmx2048m"
```

#### 7. Certificate Errors

**Error:** `PKIX path building failed`

**Solution:**
```bash
# Accept self-signed certificates (not recommended for production)
export SONAR_SCANNER_OPTS="-Djavax.net.ssl.trustStore=/path/to/truststore.jks"

# Or disable SSL verification (development only)
git config --global http.sslVerify false
```

#### 8. Branch Analysis Not Working

**Error:** Branches not showing up in SonarQube

**Solution:**
- Requires **Developer Edition** or higher (not Community Edition)
- Or use SonarCloud (supports branches on free plan)
- Check branch name is passed correctly: `-Dsonar.branch.name=develop`

---

## Best Practices

### 1. Run on Every Commit

Configure CI to run SonarQube analysis on every commit to catch issues early.

### 2. Enforce Quality Gates

Break builds when quality gate fails to prevent quality degradation.

### 3. Analyze Pull Requests

Enable PR decoration to see issues directly in your pull requests.

### 4. Monitor Trends

Track metrics over time to see if code quality is improving or declining.

### 5. Fix Issues Early

Address issues while the code is fresh in developers' minds.

### 6. Customize Rules

Adjust rules and quality gates to match your team's standards.

### 7. Educate Team

Ensure developers understand the rules and why they matter.

### 8. Cache Scanner

Cache the SonarQube scanner to speed up CI builds.

---

## Next Steps

1. Choose your CI/CD platform above
2. Copy the configuration to your repository
3. Set up authentication tokens as secrets
4. Push code and watch the analysis run
5. Configure quality gates for your project
6. Add status badges to your README
7. Review and fix issues found by SonarQube

For more information:
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [Mathematica Plugin Best Practices](./Best-Practices.md)
- [Plugin README](../README.md)

---

## Support

For issues specific to the Mathematica plugin:
- Check the [plugin repository](https://github.com/yourusername/wolfralyze)
- Review the [ROADMAP](../ROADMAP.md) for planned features
- Submit issues or feature requests

For general SonarQube questions:
- [SonarQube Community Forum](https://community.sonarsource.com/)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/sonarqube)
