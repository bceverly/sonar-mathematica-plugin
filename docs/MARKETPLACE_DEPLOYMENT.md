# SonarQube Marketplace Deployment Guide

This guide explains how to use the automated marketplace deployment workflow to publish new versions of wolfralyze to the SonarQube Marketplace.

## Prerequisites

Before using the deployment workflow, you need to complete these one-time setup steps:

### 1. Fork Required Repositories

You need to fork both SonarSource repositories to your GitHub account:

1. **Fork sonar-update-center-properties**
   - Go to: https://github.com/SonarSource/sonar-update-center-properties
   - Click "Fork" button
   - Fork to your account: `bceverly/sonar-update-center-properties`

2. **Fork sonarqube-community-branch-plugin-builds**
   - Go to: https://github.com/SonarSource/sonarqube-community-branch-plugin-builds
   - Click "Fork" button
   - Fork to your account: `bceverly/sonarqube-community-branch-plugin-builds`

### 2. Create GitHub Personal Access Token (PAT)

1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Configure the token:
   - **Name:** `SONAR_MARKETPLACE_PAT`
   - **Expiration:** Set to your preference (90 days or longer)
   - **Scopes:** Check the following:
     - ✅ `repo` (Full control of private repositories)
     - ✅ `workflow` (Update GitHub Action workflows)
     - ✅ `write:packages` (Upload packages)

4. Click "Generate token"
5. **IMPORTANT:** Copy the token immediately (you won't be able to see it again)

### 3. Add Secrets to wolfralyze Repository

1. Go to your wolfralyze repository on GitHub
2. Navigate to: Settings → Secrets and variables → Actions
3. Click "New repository secret"
4. Add these two secrets:

#### Secret 1: SONAR_MARKETPLACE_PAT
- **Name:** `SONAR_MARKETPLACE_PAT`
- **Value:** Paste the Personal Access Token you created above
- **Purpose:** Allows workflow to create PRs on your behalf

#### Secret 2: USER_EMAIL
- **Name:** `USER_EMAIL`
- **Value:** Your GitHub email (e.g., `your-email@example.com`)
- **Purpose:** Used for git commit author information

## Release Process

### Step 1: Create and Push a Git Tag

Before running the deployment workflow, you must create and push a new version tag:

```bash
# Make sure you're on the main branch with latest changes
git checkout master
git pull origin master

# Create a new tag (e.g., v1.0.3)
git tag v1.0.3

# Push the tag to GitHub
git push origin v1.0.3
```

**Tag Naming Convention:** Use semantic versioning with a `v` prefix (e.g., `v1.0.3`, `v2.0.0`)

### Step 1.5: Generate and Commit Release Notes

After creating the tag, regenerate the comprehensive release notes file and commit it:

```bash
# Generate comprehensive release notes (all versions from v1.0.0 onwards)
make release-notes

# Review the generated RELEASE_NOTES.md file
cat RELEASE_NOTES.md

# Commit it to version control
git add RELEASE_NOTES.md
git commit -m "Update release notes for v1.0.3"
git push origin master
```

The `make release-notes` target automatically generates a comprehensive RELEASE_NOTES.md file containing:
- All versions from v1.0.0 onwards
- Newest version at the top
- Commits for each version
- Full changelog links between versions

**Note:** You need to run this after every new tag to keep RELEASE_NOTES.md up to date.

### Step 2: Create a GitHub Release

1. Go to your wolfralyze repository on GitHub
2. Click "Releases" → "Create a new release"
3. Select the tag you just created (e.g., `v1.0.3`)
4. Fill in release details:
   - **Title:** wolfralyze v1.0.3
   - **Description:** Describe the changes in this release
5. **IMPORTANT:** Upload the JAR file as a release asset
   - Build the plugin: `./gradlew clean build`
   - Upload: `build/libs/wolfralyze-1.0.3.jar`
6. Click "Publish release"

### Step 3: Run the Deployment Workflow

1. Go to your wolfralyze repository on GitHub
2. Click "Actions" tab
3. Select "Deploy New Release" workflow from the left sidebar
4. Click "Run workflow" button
5. Choose options:
   - **Branch:** master
   - **Dry run:**
     - ☐ `false` (default) - Creates actual PRs
     - ☑ `true` - Test run without creating PRs (recommended first time)
6. Click "Run workflow"

### Step 4: Monitor and Verify

The workflow will:

1. ✅ Detect the latest tag automatically
2. ✅ Generate release notes from commits since previous tag
3. ✅ Build and verify the plugin JAR
4. ✅ Update `wolfralyze.properties` in sonar-update-center-properties
5. ✅ Create PR #1 to SonarSource/sonar-update-center-properties
6. ✅ Create metadata file in sonarqube-community-branch-plugin-builds
7. ✅ Create PR #2 to SonarSource/sonarqube-community-branch-plugin-builds

### Step 5: Wait for PR Approval

After the PRs are created:

1. SonarSource team will review your PRs
2. They may request changes or approve directly
3. Once approved and merged, your plugin will appear in the marketplace
4. **Timeline:** Usually 1-3 business days for review

## Troubleshooting

### Error: "JAR not found"

**Cause:** The built JAR filename doesn't match the version tag.

**Fix:** Ensure your `build.gradle` version matches the git tag:
```gradle
version = '1.0.3'  // Must match tag v1.0.3
```

### Error: "Permission denied" when creating PR

**Cause:** PAT token doesn't have correct permissions or has expired.

**Fix:**
1. Verify `SONAR_MARKETPLACE_PAT` secret is set correctly
2. Check token hasn't expired
3. Regenerate token with correct scopes if needed

### Error: "Could not resolve host: github.com"

**Cause:** Network/authentication issue with Git.

**Fix:** Verify the forks exist at:
- `https://github.com/bceverly/sonar-update-center-properties`
- `https://github.com/bceverly/sonarqube-community-branch-plugin-builds`

### Dry Run First

**Recommendation:** Always run with "Dry run" enabled first to verify everything works before creating actual PRs.

## What Gets Updated

### In sonar-update-center-properties/wolfralyze.properties:

```properties
# Adds version to list
publicVersions=1.0.2,1.0.3

# Adds version-specific config
1.0.3.description=Bug fixes and improvements
1.0.3.sqs=[2025.1,LATEST]
1.0.3.sqcb=[24.12,LATEST]
1.0.3.downloadUrl=https://github.com/bceverly/wolfralyze/releases/download/v1.0.3/wolfralyze-1.0.3.jar
1.0.3.changelogUrl=https://github.com/bceverly/wolfralyze/releases/tag/v1.0.3
1.0.3.date=2025-01-15
```

### In sonarqube-community-branch-plugin-builds/metadata/wolfralyze/:

Creates a new file: `1.0.3.json`
```json
{
  "version": "1.0.3",
  "downloadUrl": "https://github.com/bceverly/wolfralyze/releases/download/v1.0.3/wolfralyze-1.0.3.jar",
  "changelogUrl": "https://github.com/bceverly/wolfralyze/releases/tag/v1.0.3",
  "compatibleSQVersions": ["2025.1", "LATEST"],
  "date": "2025-01-15"
}
```

## Security Notes

- ✅ Secrets are never exposed in logs
- ✅ PRs are created using your personal credentials
- ✅ No "Claude" or automated user accounts
- ✅ You maintain full control and attribution
- ⚠️ Keep your PAT secure - treat it like a password
- ⚠️ Rotate PAT if compromised

## Additional Resources

- [SonarSource Plugin Contribution Guide](https://www.sonarplugins.com/#contribution)
- [SonarQube Marketplace Deployment](https://community.sonarsource.com/t/deploying-to-the-marketplace/35236)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
