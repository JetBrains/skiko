# Skiko Versioning and Release Process

## Versioning Format

Skiko uses the following version formats:

- **Alpha:** `0.<major>.<minor>-alpha<number>`
- **Stable:** `0.<major>.<minor>`

For example:
- `0.154.0-alpha01`
- `0.154.0-alpha02`
- `0.154.0`
- `0.154.1`

### Version Components

- **`0`** — Fixed version prefix in the current versioning scheme.
- **`major`** — Follows the Skia milestone and is updated when Skiko adopts a new Skia milestone.
- **`minor`** — Represents the Skiko release within the corresponding Skia milestone. Incremented for subsequent releases without changing the Skia milestone. **Patch releases should not introduce breaking changes.**
- **`alpha-number`** — Sequential number identifying alpha releases for a given version.

## Alpha Releases

Alpha releases are the default development releases of Skiko.

- Alpha releases can be published at any time from the `master` branch.
- They follow the usual development and publishing workflow.
- Multiple alpha releases can be published between stable releases.
- Alpha releases may include new features, bug fixes, and other ongoing development changes.

Alpha releases make the latest changes available without waiting for the next stable release.

## Stable Releases

Stable releases are tied to Skia milestone transitions.

**Before merging a new Skia milestone into `master`, we publish a stable Skiko release based on the current state of `master`.**

The release order is:

1. Finish development on the current Skia milestone.
2. Publish a stable Skiko release from `master`.
3. Merge the next Skia milestone into `master`.
4. Continue development and publish alpha releases as needed.

### Exceptional Stable Releases

Additional stable releases may be published when required by Compose Multiplatform.

These releases are exceptions to the regular milestone-driven process and should remain rare.
