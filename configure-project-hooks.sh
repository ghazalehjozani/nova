#!/bin/sh
set -eu

HOOKS_CORE_REPO_URL="https://bitbucket.dotin.ir/scm/~m.amirabdollahi/hooks-core.git"
HOOKS_CORE_DEFAULT_BRANCH="master"
HOOKS_SUBDIRECTORY_IN_REPO="hooks"
HOOK_FILES_TO_MANAGE="commit-msg pre-commit pre-push maven-helper.sh"
VERSION_FILE_NAME="hooks.version"

TARGET_HOOKS_BRANCH_OR_TAG="${1:-$HOOKS_CORE_DEFAULT_BRANCH}"

TARGET_PROJECT_ROOT=$(pwd)
TARGET_PROJECT_GIT_DIR="${TARGET_PROJECT_ROOT}/.git"
TARGET_PROJECT_HOOKS_DIR="${TARGET_PROJECT_GIT_DIR}/hooks"

TEMP_HOOKS_REPO_DIR=""

echo_info() {
  printf "INFO: %s\n" "$1"
}
echo_warn() {
  printf "WARN: %s\n" "$1" >&2
}
echo_error() {
  printf "ERROR: %s\n" "$1" >&2
  exit 1
}

cleanup_temp_dir() {
  if [ -n "$TEMP_HOOKS_REPO_DIR" ] && [ -d "$TEMP_HOOKS_REPO_DIR" ]; then
    echo_info "Cleaning up temporary directory: $TEMP_HOOKS_REPO_DIR"
    rm -rf "$TEMP_HOOKS_REPO_DIR"
  fi
}

if ! command -v git >/dev/null 2>&1; then
  echo_error "Git command not found. Please install Git and ensure it's in your PATH."
fi

if [ ! -d "$TARGET_PROJECT_GIT_DIR" ]; then
  echo_error "This script must be run from the root of a Git repository ('.git' directory not found in '$TARGET_PROJECT_ROOT')."
fi

echo_info "Starting Git hooks configuration for this project..."
echo_info "Fetching hooks from: $HOOKS_CORE_REPO_URL (Branch/Tag: $TARGET_HOOKS_BRANCH_OR_TAG)"

mkdir -p "$TARGET_PROJECT_HOOKS_DIR"

TEMP_HOOKS_REPO_DIR=$(mktemp -d "${TMPDIR:-/tmp}/project-hooks.XXXXXX")
if [ $? -ne 0 ] || [ -z "$TEMP_HOOKS_REPO_DIR" ] || [ ! -d "$TEMP_HOOKS_REPO_DIR" ]; then
    echo_error "Failed to create temporary directory."
fi
trap cleanup_temp_dir EXIT HUP INT QUIT TERM

echo_info "Cloning repository into temporary directory: $TEMP_HOOKS_REPO_DIR"
if ! git clone --quiet --depth 1 --branch "$TARGET_HOOKS_BRANCH_OR_TAG" "$HOOKS_CORE_REPO_URL" "$TEMP_HOOKS_REPO_DIR" ; then
    echo_error "Failed to clone branch/tag '$TARGET_HOOKS_BRANCH_OR_TAG' from $HOOKS_CORE_REPO_URL."
fi

SOURCE_HOOKS_PATH="${TEMP_HOOKS_REPO_DIR}/${HOOKS_SUBDIRECTORY_IN_REPO}"
if [ ! -d "$SOURCE_HOOKS_PATH" ]; then
  echo_error "Hooks subdirectory ('$HOOKS_SUBDIRECTORY_IN_REPO') not found in cloned repository."
fi

echo_info "Installing/Updating Git hooks from '$SOURCE_HOOKS_PATH' to '$TARGET_PROJECT_HOOKS_DIR'..."

for hook_name in $HOOK_FILES_TO_MANAGE; do
  source_hook_file="${SOURCE_HOOKS_PATH}/${hook_name}"
  target_hook_file="${TARGET_PROJECT_HOOKS_DIR}/${hook_name}"

  if [ ! -f "$source_hook_file" ]; then
    echo_warn "Source script '$source_hook_file' for '$hook_name' not found. Skipping."
    continue
  fi

  echo_info "Processing file: '$hook_name'"
  should_copy=1
  if [ -f "$target_hook_file" ]; then
    if cmp -s "$source_hook_file" "$target_hook_file"; then
      echo_info "  File '$hook_name' is already up-to-date."
      should_copy=0
    else
      echo_info "  File '$hook_name' differs. Updating."
      backup_file="${target_hook_file}.$(date +%Y%m%d-%H%M%S).bak"
      echo_info "  Backing up existing '$hook_name' to '$backup_file'"
      mv "$target_hook_file" "$backup_file"
    fi
  else
    echo_info "  File '$hook_name' not present. Installing."
  fi

  if [ "$should_copy" -eq 1 ]; then
    cp "$source_hook_file" "$target_hook_file"
    echo_info "  Copied '$hook_name'."
  fi

  if [ -f "$target_hook_file" ]; then
    if [ -x "$source_hook_file" ]; then
      chmod +x "$target_hook_file"
      echo_info "  Ensured '$hook_name' is executable."
    else
      chmod -x "$target_hook_file"
      echo_info "  Ensured '$hook_name' is NOT executable (matching source)."
    fi
  fi
done

source_version_file="${SOURCE_HOOKS_PATH}/${VERSION_FILE_NAME}"
target_version_file="${TARGET_PROJECT_HOOKS_DIR}/${VERSION_FILE_NAME}"
if [ -f "$source_version_file" ]; then
  cp "$source_version_file" "$target_version_file"
  INSTALLED_VERSION=$(cat "$target_version_file")
  echo_info "Git hooks version '$INSTALLED_VERSION' (from '$TARGET_HOOKS_BRANCH_OR_TAG') successfully installed/updated."
else
  echo_warn "Source version file ('$VERSION_FILE_NAME') not found in hooks repository."
  if [ -f "$target_version_file" ]; then
    rm "$target_version_file"
    echo_info "  Removed stale local version file '$target_version_file'."
  fi
fi

echo_info "---------------------------------------------------------------------"
echo_info "Git hooks configuration complete."
echo_info "---------------------------------------------------------------------"
exit 0