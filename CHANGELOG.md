# Changelog

## v1.2.0 - 2026-05-27

This is the first Crest public release baseline.

### Added

- Field-level data lineage across datasources, physical tables, fields, datasets, chart fields, charts, dashboards, and dataV screens.
- OceanBase Oracle mode datasource support based on OceanBase Connector/J.
- Built-in retail operation demo with a datasource, modeled datasets, charts, and a dataV showcase screen.
- Kubernetes deployment manifests for internal MySQL and external MySQL modes.
- GHCR image publishing workflow and a slim runtime image based on a JDK 21 Alpine build stage.
- Runtime API documentation grouped by current Crest modules.

### Changed

- Default administrator password is `admin`.
- Product branding, navigation, login, and API documentation use Crest naming.
- Removed non-current product entries from the visible navigation and documentation.
- Demo datasource connection is synchronized from the active metadata database settings at startup, so fresh installs work without host-specific SQL.

### Fixed

- DataV chart detail dialogs now fetch chart data when no local cache is available.
- Custom SQL dataset editing no longer hits a missing `/sysVariable/query` route.
- Large lineage graphs use a canvas renderer and steadier sizing to reduce blur and layout jitter.
- Workbench shortcut and favorite panels keep consistent spacing while loading.
- Initialization SQL can be replayed on a clean database without SQL warnings.

### Upgrade Notes

- Back up the metadata database and runtime directory before upgrading.
- Flyway `V1.2__demo_retail_dashboard.sql` creates or rebuilds the `crest_demo_retail` demo schema.
- The default image tag for this release is `ghcr.io/sevoniva/crest:v1.2.0`.
- Crest remains a GPLv3 project derived from DataEase 2.10.22. Keep the upstream copyright and license notices when redistributing.
