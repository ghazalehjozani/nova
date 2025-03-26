cp .githooks/commit-msg .git/hooks/commit-msg
cp .githooks/pre-commit .git/hooks/pre-commit
cp .githooks/pre-push .git/hooks/pre-push

chmod +x .git/hooks/commit-msg
chmod +x .git/hooks/pre-commit
chmod +x .git/hooks/pre-push
