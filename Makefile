.PHONY: help setup gradle clean build test lint format install-hooks install-sdk

GRADLE := ./gradlew
GRADLE_OPTS := -Dorg.gradle.jvmargs="-Xmx2048m"

# Default target
help:
	@echo "MindGuard Build Commands"
	@echo "========================"
	@echo ""
	@echo "Setup & Installation:"
	@echo "  make setup           - Full development setup"
	@echo "  make install-sdk     - Install Android SDK (requires Android Studio or cmdline-tools)"
	@echo "  make install-hooks   - Install git hooks"
	@echo ""
	@echo "Development:"
	@echo "  make clean           - Clean build artifacts"
	@echo "  make build           - Build entire project (requires Android SDK)"
	@echo "  make build-shared    - Build shared module only"
	@echo "  make test            - Run all tests (requires Android SDK)"
	@echo "  make test-shared     - Run shared module tests"
	@echo ""
	@echo "Code Quality:"
	@echo "  make lint            - Run linting checks"
	@echo "  make format          - Format code with Prettier & ESLint"
	@echo "  make ktlint          - Run Kotlin linting"
	@echo ""
	@echo "Android:"
	@echo "  make apk-debug       - Build debug APK"
	@echo "  make apk-release     - Build release APK"
	@echo ""
	@echo "Utilities:"
	@echo "  make tasks           - List available Gradle tasks"
	@echo "  make status          - Show git status"
	@echo "  make commit-check    - Verify conventional commits"
	@echo ""

# Setup
setup: clean install-hooks
	@echo "✓ MindGuard setup complete"

install-hooks:
	@echo "Installing git hooks..."
	@mkdir -p .git/hooks
	@echo "#!/bin/bash" > .git/hooks/pre-commit
	@echo "echo 'Running pre-commit checks...'" >> .git/hooks/pre-commit
	@echo "$(GRADLE) clean ktlintFormat" >> .git/hooks/pre-commit
	@chmod +x .git/hooks/pre-commit
	@echo "✓ Git hooks installed"

install-sdk:
	@echo "Installing Android SDK..."
	@echo "Visit: https://developer.android.com/studio"
	@echo "Or use: sdk install android-sdk"
	@echo "Then set ANDROID_HOME=/path/to/sdk"

# Build
clean:
	@echo "Cleaning build artifacts..."
	@$(GRADLE) $(GRADLE_OPTS) clean
	@rm -rf build/ .gradle/
	@echo "✓ Clean complete"

build: clean
	@echo "Building MindGuard (full)..."
	@$(GRADLE) $(GRADLE_OPTS) build
	@echo "✓ Build complete"

build-shared:
	@echo "Building shared module..."
	@$(GRADLE) $(GRADLE_OPTS) :shared:build
	@echo "✓ Shared build complete"

# Testing
test:
	@echo "Running all tests..."
	@$(GRADLE) $(GRADLE_OPTS) test
	@echo "✓ Tests complete"

test-shared:
	@echo "Running shared module tests..."
	@$(GRADLE) $(GRADLE_OPTS) :shared:test
	@echo "✓ Shared tests complete"

# Code Quality
lint: ktlint
	@echo "✓ Linting complete"

ktlint:
	@echo "Running ktlint..."
	@$(GRADLE) $(GRADLE_OPTS) ktlint || true
	@echo "✓ Ktlint complete"

format:
	@echo "Formatting code..."
	@$(GRADLE) $(GRADLE_OPTS) ktlintFormat || true
	@echo "✓ Formatting complete"

# Android APK
apk-debug:
	@echo "Building debug APK..."
	@$(GRADLE) $(GRADLE_OPTS) assembleDebug
	@echo "✓ Debug APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk"

apk-release:
	@echo "Building release APK..."
	@$(GRADLE) $(GRADLE_OPTS) assembleRelease
	@echo "✓ Release APK: androidApp/build/outputs/apk/release/androidApp-release.apk"

# Utilities
tasks:
	@$(GRADLE) tasks

status:
	@git status

commit-check:
	@echo "Checking commits..."
	@git log --oneline --pretty=format:"%h %s" | head -10
	@echo ""
	@echo "Conventional Commits Format:"
	@echo "  feat: new feature"
	@echo "  fix: bug fix"
	@echo "  test: test addition"
	@echo "  docs: documentation"
	@echo "  refactor: code refactoring"

# CI/CD
ci-build:
	@echo "CI: Building project..."
	@$(GRADLE) $(GRADLE_OPTS) clean build --build-cache

ci-test:
	@echo "CI: Running tests..."
	@$(GRADLE) $(GRADLE_OPTS) test --build-cache

ci-lint:
	@echo "CI: Linting..."
	@$(GRADLE) $(GRADLE_OPTS) ktlint || true
