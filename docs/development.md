# DataEase 2.10.22 OB Oracle Fork Development Guide

This repository is a fork of `https://github.com/dataease/dataease.git` based on DataEase 2.10.22. The fork keeps the upstream GPLv3 license and adds OceanBase Oracle mode datasource support.

## Repository Layout

- `core/core-backend`: Spring Boot backend and packaged application.
- `core/core-frontend`: Vue frontend. Use the checked-in `package-lock.json`.
- `sdk/extensions/extensions-datasource`: datasource extension interfaces and JDBC datasource definitions.
- `drivers`: fork-managed JDBC driver jars. Only `oceanbase-client-2.4.17.jar` is intentionally tracked here.
- `third-party/maven`: small static Maven repository for required artifacts that are not available from public Maven mirrors.
- `installer`: deployment templates.
- `docs/data-lineage.md`: product and design notes for the field-level data lineage page.
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

Publish GHCR images manually from GitHub Actions:

1. Open `Actions` in GitHub.
2. Select `Build and Publish Docker Images`.
3. Run the workflow and optionally override `image_tag`.

## OceanBase Oracle Notes

The fork adds datasource type `obOracle` using `com.oceanbase.jdbc.Driver`.

Supported username formats:

- Direct OBServer: `username@tenant`
- OBProxy/ODP: `username@tenant#cluster`

When schema is left empty, the implementation defaults to the account name uppercased, matching Oracle-style schema behavior.

Minimum regression coverage for OB Oracle changes:

- direct OBServer connection, for example `test@obora` on port `2881`;
- OBProxy/ODP connection, for example `test@obora#obdemo` on port `2883`;
- table and field metadata, including column comments;
- dataset preview, full cache sync, incremental cache sync, and dashboard reads.

## Internal Lite Boundary

This fork is maintained as an internal BI baseline. Keep the main BI path simple:

- keep datasource, dataset, dataset cache sync, field-level data lineage, chart editor, dashboards, screens, share pages, export, login, users, roles, system parameters, and OB Oracle support;
- do not reintroduce SQLBot, template market, toolbox, message center, standalone mobile pages, map chart creation, map runtime dependencies, map APIs, or demo template resources unless there is a clear product decision;
- preserve compatibility for existing dashboards where practical, but do not add new entry points for removed features;
- when removing another feature, remove the visible entry, router/page build input, active API calls, large static assets, and documentation together.

The share module intentionally keeps historical names such as `xpack_share`, `XpackShare*`, and `io.dataease.api.xpack.share`. These names are compatibility boundaries for existing tables, routes, mapper statements, and old share links. Do not rename them without a migration and link-compatibility plan.

## Data Lineage Notes

The lineage page is part of the community-code fork, not an xpack module. Keep it that way unless there is an explicit product decision to move it.

Main backend files:

- `core/core-backend/src/main/java/io/dataease/relation/server/RelationServer.java`
- `core/core-backend/src/main/java/io/dataease/relation/manage/RelationManage.java`
- `core/core-backend/src/main/java/io/dataease/relation/dto/*`

Main frontend files:

- `core/core-frontend/src/api/relation/index.ts`
- `core/core-frontend/src/views/visualized/data/lineage/index.vue`
- `core/core-frontend/src/components/relation-chart/GraphView.vue`

Menu migration:

- `core/core-backend/src/main/resources/db/migration/V2.10.22.5__data_lineage_menu.sql`
- `core/core-backend/src/main/resources/db/desktop/V2.10.22.5__data_lineage_menu.sql`

The graph is built from DataEase metadata only. It should not connect to the business datasource when a user opens the lineage page. The current model is:

```text
datasource -> table -> table_field -> dataset_field -> dataset -> chart_field -> chart -> dv
```

Use the same node type names in backend, frontend, and documentation:

- `datasource`
- `table`
- `table_field`
- `dataset_field`
- `dataset`
- `chart_field`
- `chart`
- `dv`

`RelationManage` also implements `RelationApi` so existing delete checks can use the same graph logic:

- `getDsResource(id)` protects datasources with downstream usage;
- `getDatasetResource(id)` protects datasets used by charts or dashboards.

When changing this module, verify at least:

- global overview loads;
- datasource-level field lineage loads;
- dataset-level field lineage loads;
- dashboard/screen lineage loads;
- table selector lists all physical tables in the selected graph;
- field selector follows the selected table;
- selecting a field narrows the graph to upstream and downstream nodes;
- keyword search still works for fields and resources;
- deleting a used datasource or dataset still reports downstream impact;
- dashboard and screen pages still open, because they share dynamic chunks with chart editing code.

The user-facing document is `docs/data-lineage.md`; update it whenever graph semantics, endpoints, node types, or known limits change.

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
- Update README, `docs/data-lineage.md`, or this guide when build, deployment, dependency, OceanBase behavior, or lineage behavior changes.
