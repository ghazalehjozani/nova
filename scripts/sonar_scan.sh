#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

PROJECT_ROOT="$SCRIPT_DIR/.."

cd "$PROJECT_ROOT"

source "$SCRIPT_DIR/config.cfg"

if [[ -z "${SONAR_TOKEN}" ]]; then
  echo "Error: SONAR_TOKEN environment variable is not set."
  exit 1
fi

current_branch=$(git rev-parse --abbrev-ref HEAD)

run_pull_request_scan() {
  available_branches=$(git branch -r | grep 'origin/' | grep -vE 'origin/(develop|master|stage|[0-9]+$)' | sed 's/origin\///')

  branches_array=($available_branches)

  echo "Available branches:"
  for i in "${!branches_array[@]}"; do
    echo "$((i+1)). ${branches_array[$i]}"
  done

  read -p "Please choose the number of the branch for the pull request (press Enter to use '$current_branch'): " branch_number

  if [[ -z "$branch_number" ]]; then
    pullrequest_branch="$current_branch"
  else
    pullrequest_branch="${branches_array[$((branch_number-1))]}"
  fi

  new_pullrequest_key=$((pullrequest_key + 1))
  sed -i "s/^pullrequest_key=.*/pullrequest_key=$new_pullrequest_key/" $SCRIPT_DIR/config.cfg

  git stash push -m "Temporary stash for pull request scan"

  git checkout "$pullrequest_branch"
  git -c core.quotepath=false -c log.showSignature=false fetch origin --recurse-submodules=no --progress --prune


  echo "Running SonarQube scan for a pull request on branch '$pullrequest_branch'..."
  mvn clean test verify sonar:sonar -Pcoverage \
    -Dsonar.projectKey=$project_key \
    -Dsonar.projectName="$project_name" \
    -Dsonar.host.url=$sonar_host_url \
    -Dsonar.token=$MORABEHE_LOAN_SONAR_TOKEN \
    -Dsonar.pullrequest.key=$new_pullrequest_key \
    -Dsonar.pullrequest.base=$pullrequest_base \
    -Dsonar.pullrequest.branch=$pullrequest_branch

  git checkout "$current_branch"

  git stash pop --index
}

if [[ "$current_branch" == "develop" || "$current_branch" == "master" || "$current_branch" == "stage" ]]; then
  read -p "You are on the '$current_branch' branch. Do you want to check a pull request (y/n)? " user_input

  if [[ "$user_input" == "y" ]]; then
    run_pull_request_scan
  elif [[ "$user_input" == "n" ]]; then
    echo "Running SonarQube scan for the '$current_branch' branch..."
    mvn clean test verify sonar:sonar -Pcoverage \
      -Dsonar.projectKey=$project_key \
      -Dsonar.projectName="$project_name" \
      -Dsonar.host.url=$sonar_host_url \
      -Dsonar.token=$MORABEHE_LOAN_SONAR_TOKEN
  else
    echo "Invalid input. Please enter 'y' or 'n'."
    exit 1
  fi
else
  run_pull_request_scan
fi
