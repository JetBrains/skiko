# Skiko Versioning and Release Process

## Versioning Format

Skiko uses the following version format:

`0.<skia-milestone>.<patch>(-alpha<number>)`

### Version Components

- **`0`** — Fixed version prefix in the current versioning scheme, meaning we are not committed to binary compatibility.
- **`skia-milestone`** — Updated when Skiko adopts a new Skia milestone.
- **`patch`** — Represents the Skiko release within the corresponding Skia milestone. Incremented for subsequent releases without changing the Skia milestone. **Patch releases should not introduce breaking changes.**
- **`number`** — Sequential number identifying alpha releases for a given version.

## Alpha Releases

Alpha releases are the default development releases of Skiko.

- Alpha releases can be published at any time from the `master` branch.
- They follow the usual development and publishing workflow.
- Multiple alpha releases can be published between stable releases.
- Alpha releases may include new features, bug fixes, and other ongoing development changes.

Alpha releases make the latest changes available without waiting for the next stable release.

## Stable Releases

The first stable release for a Skia milestone is published before moving `master` to the next milestone. Further patch releases can be published whenever needed.

**Before merging a new Skia milestone into `master`, we publish a stable Skiko release based on the current state of `master`.**

The release order is:

1. Finish development on the current Skia milestone.
2. Make sure the next Skia milestone is ready to merge.
3. Publish a stable Skiko release from `master`.
4. Merge the next Skia milestone into `master`.
5. Continue development and publish alpha releases as needed.

### Patch Releases for Previous Milestones

If the patch targets a previous Skia milestone, create a `release/0.<skia-milestone>` branch from that milestone's stable release. Apply the changes to this branch and publish the release from the branch. Keep applicable fixes on `master` as well.

If the release needs a previous TeamCity build configuration, create a separate TeamCity branch with the same name as the GitHub release branch. TeamCity will then pick it up automatically.
