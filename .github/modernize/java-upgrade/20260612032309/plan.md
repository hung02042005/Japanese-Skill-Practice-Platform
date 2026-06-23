# Upgrade Plan: Japanese-Skill-Practice-Platform (20260612032309)

- **Generated**: 2026-06-12 03:23:09 UTC
- **HEAD Branch**: main
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**

- JDK 17.0.1: C:\Program Files\Java\jdk-17.0.1\bin
- JDK 21.0.11: C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin (current target)
- JDK 25.0.3: C:\Users\DELL\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\25\bin
- JDK 26.0.1: C:\Users\DELL\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\latest\bin

**Build Tools**

- Maven 3.9.16: C:\apache-maven-3.9.16\bin
- Maven Wrapper: not found in repository root; using system Maven 3.9.16

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260612032309
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java runtime support to the latest LTS version.

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java | 21 | 21 | Project already targets Java 21, the current LTS runtime. |
| Spring Boot | 3.3.3 | 3.3.3 | Compatible with Java 21. |
| Maven | system 3.9.16 | 3.9.16 | Compatible with Java 21. |
| maven-compiler-plugin | default from parent | 3.11.0+ recommended | Java 21 support is best validated with modern plugin versions. |
| MapStruct | 1.6.3 | 1.6.3 | Compatible with Java 21. |
| jjwt | 0.12.6 | 0.12.6 | Compatible with current dependencies. |

## Derived Upgrades

- No version-mandated Java upgrade is required because the project already targets Java 21.
- Validate existing Java 21 support by confirming build and tests with JDK 21.
- Because no wrapper is present, ensure the system Maven 3.9.16 environment is used consistently.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|------------|---------|--------|--------|--------|
| apps/backend/pom.xml | java.version | 21 | verify | 21 | Project already specifies Java 21. |
| apps/backend/pom.xml | maven-compiler-plugin | inherited | add/explicit | 3.11.0 | Explicit plugin version improves build reproducibility for Java 21. |
| apps/backend/pom.xml | spotless-maven-plugin | 2.43.0 | verify | 2.43.0 | No upgrade required for Java 21 support. |
| apps/backend/pom.xml | jacoco-maven-plugin | 0.8.12 | verify | 0.8.12 | Java 21 compatible. |

### Source Code Changes

No source code changes are required strictly for Java 21 support because the project already targets Java 21 and uses Spring Boot 3.3.3.

### Configuration Changes

| File | Property/Setting | Current | Required Change | Reason |
|------|------------------|---------|-----------------|--------|
| apps/backend/pom.xml | maven-compiler-plugin version | not explicitly set | add explicit version 3.11.0 | Ensures a supported compiler plugin is used for Java 21. |

### CI/CD Changes

No CI/CD files detected in repository root that require Java version updates for this target.

### Risks & Warnings

- **Missing Maven Wrapper**: The repository has no `mvnw`, so build reproducibility depends on the system Maven installation. Mitigation: use Maven 3.9.16 consistently and add explicit plugin versions.
- **Uncommitted workspace changes**: Changes were stashed before plan generation; ensure no unrelated modifications are included in the upgrade branch.

## Upgrade Steps

- Step 1: Initialize branch and verify JDK tooling
  - Rationale: Establish a clean working branch and confirm the available Java/Maven environment for validation.
  - Changes to Make: none in source code; use system Maven 3.9.16 and JDK 21.0.11.
  - Verification: `mvn -version` and `java -version`.

- Step 2: Baseline compile and test with Java 21
  - Rationale: Confirm current project state under the requested target runtime.
  - Changes to Make: none; run baseline build on existing Java 21 target.
  - Verification: `mvn clean test-compile -q && mvn test -q`.

- Step 3: Add explicit maven-compiler-plugin version for Java 21 stability
  - Rationale: Improve build reproducibility and ensure the compiler plugin is explicitly compatible with Java 21.
  - Changes to Make: add `maven-compiler-plugin` version `3.11.0` to `apps/backend/pom.xml`.
  - Verification: `mvn clean test-compile -q`.

- Step 4: Run full test suite and finalize validation
  - Rationale: Verify the upgrade goal is satisfied with 100% test pass rate and no regressions.
  - Changes to Make: none beyond Step 3, unless test fixes are required.
  - Verification: `mvn clean test -q`.
