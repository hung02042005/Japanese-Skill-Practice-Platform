# Upgrade Plan: JLPT E-Learning Platform (20260531101003)

- **Generated**: 2026-05-31 10:10:03
- **HEAD Branch**: main
- **HEAD Commit ID**: 880bb1c091a7144183b53844fe84c6f275

## Available Tools

**JDKs**

- JDK 17.0.1: C:\Program Files\Java\jdk-17.0.1\bin
- JDK 21.0.11: C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin

**Build Tools**

- Maven 3.9.16: C:\apache-maven-3.9.16\bin
- Maven Wrapper: not present in repository

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260531101003
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade the Java runtime to the latest LTS version (inferred target: Java 21)

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java | 21 | 21 | Project already targets the requested LTS version |
| Spring Boot | 3.3.3 | 3.3.3 | Compatible with Java 21 |
| Maven | (no wrapper) | 3.9.0 | Local Maven 3.9.16 is compatible with Java 21 |
| mapstruct.version | 1.5.5.Final | 1.5.5.Final | Compatible with Java 21 |
| jjwt.version | 0.12.6 | 0.12.6 | Compatible with Java 21 |

## Derived Upgrades

- None. The current project already targets Java 21, which is the latest LTS runtime for this upgrade.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|------------|---------|--------|--------|--------|
| apps/backend/pom.xml | java.version | 21 | none | 21 | Project already targets latest LTS |

### Source Code Changes

- None required. No source or package changes are needed because the project already targets Java 21.

### Configuration Changes

- None required. application.yml and other config files do not need Java runtime changes.

### CI/CD Changes

- None required. No Dockerfile or pipeline updates are needed for this verification-only upgrade.

### Risks & Warnings

- The project is already configured for Java 21. The main risk is stale build artifacts or uncommitted generated files; these have been stashed before plan generation.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Verify the local toolchain and ensure Java 21 is available for validation.
  - **Changes to Make**: None; environment verification only.
  - **Verification**: `mvn -version` and JDK availability check with Java 21 installed.

- Step 2: Validate Current Java 21 Runtime
  - **Rationale**: Confirm that the project builds and tests successfully on the current latest LTS runtime with no code changes required.
  - **Changes to Make**: None; execute verification on the existing configuration.
  - **Verification**: `mvn clean test -q` using JDK 21.
