# Bundled SVG visualization plugin

This example builds one uploadable JAR containing a library, Bar and Line chart
generators, a shared browser runtime, styles, an icon and reusable customization
metadata. Its SVG renderer uses browser APIs and makes no network requests.

From the template repository root:

```sh
./mvnw -f examples/bundled-svg/pom.xml clean package
```

Upload `target/openlap-bundled-svg-example-1.0.0.jar` from this example directory
through OpenLAP's visualization-library administration page. Review the library,
both chart types, runtime and declared assets before activation. Use an OpenLAP
build that supports manifest version 1.

Both charts accept a categorical **Categories** input and a numerical **Values**
input. Map them to an analysis result with one finite value per category. Chart
title, value labels, label position, series color, height and grid lines are
provided by the manifest. The label-position control is visible when values are
shown. Selecting either chart resolves the same `svg-example` runtime version
`1.0.0`; the generated snippet calls `window.OpenLAPExample.render()` after
OpenLAP loads that runtime.

The Java classes compile against the existing published template 1.1.0 and
dataset 1.0.0 ABI. The POM marks these dependencies `provided` and does not shade
them into the plugin. A Jackson BOM aligns the historical dataset's compile/test
dependencies, whose published POM otherwise mixes version ranges and an older
annotations version. The uploadable JAR contains only example classes and its
own resources, not Jackson or OpenLAP classes.

After editing `src/main/resources/web/` assets, inspect the changes and update
their recorded checksums from the repository root:

```sh
python3 scripts/update-example-manifest.py --write
./mvnw -f examples/bundled-svg/pom.xml clean package
```

Without `--write`, the helper only checks hashes. The Maven tests fail if an
asset's bytes no longer match the manifest. They also qualify inline-data
escaping, distinct chart container IDs, finite numeric data and invalid options.

To adapt the example, implement your own data transformer and concrete chart
classes, replace the bundled runtime assets with the exact dependency versions
you support, and update the manifest references. Preserve required third-party
license notices in your distribution. Use `textContent` for DOM labels and
context-appropriate escaping for generated JavaScript; never concatenate raw
dataset labels into executable code.

The runtime includes SVG titles, descriptions and focusable chart marks, and its
view box follows the container width. For very large datasets, provide a chart
designed for that density rather than extending this introductory example.
