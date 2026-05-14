# DataEase 2.10.22 OB Oracle Fork Development Guide

This repository is a fork of `https://github.com/dataease/dataease.git` based on DataEase 2.10.22. The fork keeps the upstream GPLv3 license and adds OceanBase Oracle mode datasource support.

## Repository Layout

- `core/core-backend`: Spring Boot backend and packaged application.
- `core/core-frontend`: Vue frontend. Use the checked-in `package-lock.json`.
- `sdk/extensions/extensions-datasource`: datasource extension interfaces and JDBC datasource definitions.
- `drivers`: fork-managed JDBC driver jars. Only `oceanbase-client-2.4.17.jar` is intentionally tracked here.
- `third-party/maven`: small static Maven repository for required artifacts that are not available from public Maven mirrors.
- `installer`: deployment templates.
- `.github/workflows/docker-publish.yml`: GHCR image build and publish workflow.

## Required Toolchain

- JDK 21.
- Maven 3.9 or compatible.
- Node.js 22 for frontend build.
- Docker with Buildx when building images locally.

## Dependency Sources

Maven uses the repository-local `.mvn/settings.xml`, which mirrors Maven Central through Aliyun public Maven. Keep public dependencies in normal Maven coordinates. Only add files to `third-party/maven` when a required upstream artifact is not available from public repositories.

Frontend dependencies use `core/core-frontend/.npmrc` and `registry.npmmirror.com`. Keep `package-lock.json` updated whenever frontend dependencies change.

## Common Commands

Resolve and compile the OceanBase datasource-related module:

```bash
mvn -pl sdk/extensions/extensions-datasource -am \
  -DskipTests -Dmaven.test.skip=true -Dmaven.antrun.skip=true \
  test-compile
```

Build the frontend:

```bash
cd core/core-frontend
npm ci
npm run build:base
```

Build the backend package:

```bash
mvn clean install -DskipTests -Dmaven.test.skip=true
mvn -f core/pom.xml clean package -Pstandalone -DskipTests -Dmaven.test.skip=true
```

Build the Docker image locally after backend packaging:

```bash
docker build -t dataease-2.10.22-ob:local .
```

## OceanBase Oracle Notes

The fork adds datasource type `obOracle` using `com.oceanbase.jdbc.Driver`.

Supported username formats:

- Direct OBServer: `username@tenant`
- OBProxy/ODP: `username@tenant#cluster`

When schema is left empty, the implementation defaults to the account name uppercased, matching Oracle-style schema behavior.

## Workspace Hygiene

Do not commit local build or runtime output:

- `node_modules`
- Maven `target` directories
- `.flattened-pom.xml`
- `runtime`
- generated logs, pids, archives, and IDE files

The root `.gitignore` and `.dockerignore` are configured for this. If a local workspace gets large, remove ignored files with:

```bash
git clean -fdX
```

Review the output first with:

```bash
git clean -fdXn
```

## Contribution Rules

- Keep changes scoped and reviewable.
- Follow existing DataEase patterns before adding new abstractions.
- Run focused Maven/frontend verification for the area touched.
- Update README or this guide when build, deployment, dependency, or OceanBase behavior changes.
