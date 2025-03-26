# CONTRIBUTING

Welcome to our project! This document outlines how to contribute code in a way that **maintains high quality**, **enforces best practices**, and **aligns with our Git Flow**. Please read thoroughly before opening a pull request or committing code.

---

## Table of Contents

1. [Overview](#overview)  
2. [Branching Strategy](#branching-strategy)  
3. [Commit Message Guidelines](#commit-message-guidelines)  
   - [Commit Message Format](#commit-message-format)  
   - [Allowed Commit Types](#allowed-commit-types)  
   - [Task References](#task-references)  
   - [Minimum/Maximum Length](#minimummaximum-length)  
   - [WIP/TODO Disallowed](#wiptodo-disallowed)  
   - [GPG-Signed Commits](#gpg-signed-commits)  
4. [Local Git Hooks](#local-git-hooks)  
   - [Installing the Hooks](#installing-the-hooks)  
   - [Hooks Overview](#hooks-overview)  
5. [Breaking Changes & Version Bumps](#breaking-changes--version-bumps)  
   - [Semantic Versioning](#semantic-versioning)  
   - [When to Declare a Breaking Change](#when-to-declare-a-breaking-change)  
   - [Automatic Version Bumps](#automatic-version-bumps)  
6. [Pull Requests & Code Reviews](#pull-requests--code-reviews)  
7. [Further Resources](#further-resources)

---

## 1. Overview

This project follows **Domain-Driven Design (DDD)** principles and a **Hexagonal Architecture**. We enforce a **Git Flow** style branching model and semantic versioning. Our commit messages, branch names, and local checks are designed to maintain a clean, traceable history and reduce errors in a regulated banking environment.

---

## 2. Branching Strategy

We use three main persistent branches:

- `master`  
- `stage`  
- `develop`  

> **No direct commits** are allowed on these branches. Instead, create feature/hotfix/release branches.

**Branch naming** must match:

```
(feature|hotfix|release)/<kebab-case-name>-<digits>[optional-extra-kebab-segments]
```

Examples:

- `feature/customer-service-1234`
- `hotfix/fix-memory-leak-532`
- `release/v1-2-0`

If you attempt to commit on `master`, `stage`, or `develop` directly, our **commit-msg** hook will block the commit.

---

## 3. Commit Message Guidelines

### Commit Message Format

We follow a format inspired by [Conventional Commits](https://www.conventionalcommits.org/) and further constrained by our internal requirements.

Example header:

```
<type>(FOO-12345[:BAR-67890[:XYZ-1122]]): Short description (15–100 chars)
```

Where:

1. **`<type>`** is one of:
   - `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
2. **Task references** in parentheses:  
   - At least **1** and at most **3** tasks, separated by colons (e.g., `FOO-1234:BAR-5678`).
   - Each task ID is uppercase letters, a dash, and digits: `[A-Z]{2,}-[0-9]+`.
3. **Short description**:  
   - A short, imperative summary of the changes (min 15 chars, max 100 chars).

### Allowed Commit Types

- **feat**: A new feature.  
- **fix**: A bug fix.  
- **docs**: Documentation-only changes.  
- **style**: Code style changes (formatting, no logic).  
- **refactor**: Changes that neither fix a bug nor add a feature, but improve structure/quality.  
- **test**: Adding or updating tests.  
- **chore**: Other tasks like build scripts, config changes, etc.

### Task References

Each commit must reference **1-3** valid task IDs in parentheses. This ensures clear traceability to tickets or stories (e.g. `FOO-12345`). Example:

```
feat(FOO-12345:BAR-67890): Implement new domain service
```

### Minimum/Maximum Length

- The first (header) line of the commit must be **at least 15** characters and **no more than 100** characters.  
- This ensures concise yet descriptive messages.

### WIP/TODO Disallowed

We disallow commits with **“WIP”, “TODO”, or “FIXME”** in the first line. Any partial work or placeholders must be resolved before committing.

### GPG-Signed Commits

All commits must be **GPG-signed**. This is crucial in regulated environments to prove authenticity. If you need help setting up GPG signing, please see our [docs/gpg-signed-commits-guide.md](docs/gpg-signed-commits-guide.md).

---

## 4. Local Git Hooks

We use **three** local hooks:

1. **`commit-msg`**  
   
   - Checks your branch name, disallows commits on `master`/`stage`/`develop`.  
   - Validates the commit message format, length, task references, disallows “WIP”/“TODO”.  
   - Ensures the commit is GPG-signed.  
   - Checks for “breaking change” flags if `feat!` or `fix!`.

2. **`pre-commit`**  
   
   - Runs `mvn clean compile -P dev`.  
   - Fails if there are any errors or warnings.

3. **`pre-push`**  
   
   - Checks if your local branch is behind `develop`. If so, you must rebase/merge.  
   - Analyzes commit messages to auto-determine a version bump.  
   - Updates `pom.xml`, commits the new version, and tags it.  
   - Runs a full test/verify (`mvn clean compile test verify -P dev,mutation-test,architecture-test`).  
   - If the build fails, the push is blocked, and the version bump is reverted.

### Installing the Hooks

1. **Clone** the repository.

2. If our hooks are not already copied into `.git/hooks/`, run:
   
   ```bash
   cp .githooks/commit-msg .git/hooks/commit-msg
   cp .githooks/pre-commit .git/hooks/pre-commit
   cp .githooks/pre-push .git/hooks/pre-push
   
   chmod +x .git/hooks/commit-msg
   chmod +x .git/hooks/pre-commit
   chmod +x .git/hooks/pre-push
   ```

3. Verify you have **GPG** configured (`git config user.signingkey`), and that you are committing with the `-S` flag or have `commit.gpgsign = true` in your `.gitconfig`.

### Hooks Overview

If you attempt to commit or push something that violates the rules, you’ll see an **ERROR** message explaining which check failed and how to fix it.

---

## 5. Breaking Changes & Version Bumps

### Semantic Versioning

We maintain a version in `pom.xml` using **SemVer**: `MAJOR.MINOR.PATCH`. Our hooks enforce:

- **Breaking changes**: Major bump (`+1.0.0`).  
- **Feature (`feat(...)`)**: Minor bump (`+0.1.0`).  
- **Other commits**: Patch bump (`+0.0.1`).

### When to Declare a Breaking Change

If your commit introduces an incompatible API or behavior change, add a `!` after the commit type, e.g. `feat!`, or include a `BREAKING CHANGE:` line in the commit body. The pre-push hook will then **major**-bump.

Example commit:

```
feat!(FOO-9999): Remove old aggregator method

- Old aggregator method is removed; code referencing it must be updated
- BREAKING CHANGE: aggregator method is no longer available
```

### Automatic Version Bumps

The **pre-push** hook:

1. Scans commits since `origin/develop`.  
2. Determines if there’s a “breaking” or “feat” commit.  
3. Updates `pom.xml` from, say, `1.2.3` → `2.0.0` (breaking) or `1.3.0` (minor) or `1.2.4` (patch).  
4. Commits the updated `pom.xml` and creates a tag like `v2.0.0`.

---

## 6. Pull Requests & Code Reviews

1. **Open a Pull Request** from your feature/hotfix/release branch to `develop`.  
2. **Ensure your commits pass** all local hooks (you must push successfully).  
3. Our CI pipeline will run the same checks plus additional integration tests.  
4. **Code reviews** are mandatory. At least one reviewer must approve before merging.

---

## 7. Further Resources

- **DDD & Hexagonal Architecture**:  
  - [Tactical DDD Patterns](https://www.domainlanguage.com/ddd/reference/)  
  - [Hexagonal Architecture Overview (Alistair Cockburn)](https://alistair.cockburn.us/hexagonal-architecture/)
- **Git Flow**:  
  - [Git Flow Cheatsheet](https://danielkummer.github.io/git-flow-cheatsheet/)
- **Semantic Versioning**:  
  - [semver.org](https://semver.org/)
- **GPG-Signed Commits**:  
  - [docs/gpg-signed-commits-guide.md](docs/gpg-signed-commits-guide.md) (local doc)  
- **Conventional Commits**:  
  - [Conventional Commits Specification](https://www.conventionalcommits.org/)

---

**Thank you for reading!** By following these guidelines, we keep our repository safe, compliant, and maintainable. If you have any questions or concerns, feel free to open an issue or reach out to the core maintainers.

