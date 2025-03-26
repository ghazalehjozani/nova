# GPG-Signed Commits: A Complete Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Guide](#step-by-step-guide)
   - [Install GPG](#install-gpg)
   - [Generate a GPG Key](#generate-a-gpg-key)
   - [Configure Git to Use the GPG Key](#configure-git)
   - [Add GPG Key to GitHub/GitLab](#add-gpg-key-to-githubgitlab)
   - [Sign Commits](#sign-commits)
   - [Verify Signatures](#verify-signatures)
4. [Troubleshooting](#troubleshooting)
5. [Best Practices](#best-practices)
6. [Conclusion](#conclusion)

---

## Introduction <a name="introduction"></a>
GPG (GNU Privacy Guard)-signed commits allow developers to cryptographically sign Git commits to prove their **authenticity** and **integrity**. This ensures:
- **Identity Verification**: Confirm that the commit author is who they claim to be.
- **Tamper Detection**: Detect unauthorized changes to commits after signing.
- **Trust in Collaboration**: Critical for open-source projects and teams to prevent impersonation attacks.

---

## Prerequisites <a name="prerequisites"></a>
- **Git** installed on your machine.
- **GPG** (GnuPG) installed (version 2.1+ recommended).
- A GitHub/GitLab account (optional but recommended for cloud repo integration).

---

## Step-by-Step Guide <a name="step-by-step-guide"></a>

### 1. Install GPG <a name="install-gpg"></a>
Install GPG based on your OS:

#### Linux (Debian/Ubuntu)
```bash
sudo apt update && sudo apt install gnupg
```

#### Windows
Download [Gpg4win](https://www.gpg4win.org/) and follow the installer prompts.

---

### 2. Generate a GPG Key <a name="generate-a-gpg-key"></a>
Run the following command to generate a GPG key pair:
```bash
gpg --full-generate-key
```
- **Key Type**: Choose `RSA and RSA` (default).
- **Key Size**: Enter `4096` for stronger security.
- **Expiration**: Set a reasonable expiration date (e.g., `2y` for 2 years).
- **Name/Email**: Use the **exact name and email** associated with your Git account.

---

### 3. Configure Git to Use the GPG Key <a name="configure-git"></a>

#### List Your GPG Keys
```bash
gpg --list-secret-keys --keyid-format LONG
```
Copy the **GPG Key ID** (e.g., `3AA5C34371567BD2`).

#### Set the Key in Git
```bash
git config --global user.signingkey YOUR_KEY_ID
git config --global commit.gpgsign true  # Auto-sign all commits
```

#### (Optional) Set GPG Program Path (if errors occur)
```bash
git config --global gpg.program $(which gpg)
```

---

### 4. Add GPG Key to GitHub/GitLab <a name="add-gpg-key-to-githubgitlab"></a>

#### Export Your Public Key
```bash
gpg --armor --export YOUR_KEY_ID
```
Copy the output starting with `-----BEGIN PGP PUBLIC KEY BLOCK-----`.

#### GitHub
1. Go to **Settings → SSH and GPG Keys**.
2. Click **New GPG Key** and paste the exported key.

#### GitLab
1. Go to **Settings → GPG Keys**.
2. Paste the exported key and click **Add Key**.

---

### 5. Sign Commits <a name="sign-commits"></a>

#### Manually Sign a Commit
```bash
git commit -S -m "feat: add security patch"
```
The `-S` flag enables GPG signing.

#### Auto-Sign All Commits (if not already configured)
```bash
git config --global commit.gpgsign true
```

---

### 6. Verify Signatures <a name="verify-signatures"></a>

#### Locally
```bash
git log --show-signature
```
Look for `Good signature` and the committer’s email.

#### On GitHub/GitLab
- GitHub: Commits show a **Verified** badge.
- GitLab: Navigate to **Repository → Commits**; signed commits show a green checkmark.

---

## Troubleshooting <a name="troubleshooting"></a>

### Issue: `gpg failed to sign the data`
**Fix**: Ensure GPG is in your system’s `PATH` and the `pinentry` program is installed:
- macOS: `brew install pinentry-mac`
- Linux: `sudo apt install pinentry-gtk2`
- Windows: Use `gpg --full-generate-key` to trigger pinentry setup.

### Issue: GitHub/GitLab Doesn’t Recognize Signature
- Ensure the email in your GPG key matches your Git email.
- Re-export and re-add the public key to your account.

---

## Best Practices <a name="best-practices"></a>
1. **Key Expiration**: Rotate keys periodically (e.g., every 1-2 years).
2. **Backup Keys**: Securely store your private key offline.
3. **Passphrase**: Use a strong passphrase for your GPG key.
4. **CI/CD**: Enforce signed commits in pipelines (e.g., GitHub Actions checks).

---

## Conclusion <a name="conclusion"></a>
GPG-signed commits are a critical security practice for verifying authorship and preventing code tampering. By following this guide, you can ensure your contributions to Git repositories are trusted and secure.

**Next Steps**:
- Explore [Git’s signing documentation](https://git-scm.com/book/en/v2/Git-Tools-Signing-Your-Work).
- Contribute to projects requiring signed commits (e.g., Linux kernel).
