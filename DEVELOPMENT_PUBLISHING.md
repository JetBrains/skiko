# Skiko Versioning and Release Process

## Versioning Format

Skiko uses the following version format:

`0.<skia-milestone>.<patch>(-alpha<number>)`

### Version Components

- **`0`** — Fixed version prefix in the current versioning scheme, meaning we are not committed to binary compatibility.
- **`skia-milestone`** — Updated when Skiko adopts a new Skia milestone.
- **`patch`** — Represents the Skiko release within the corresponding Skia milestone. Incremented for subsequent releases without changing the Skia milestone. **Patch releases should not introduce breaking changes.**
- **`number`** — Sequential number identifying alpha releases for a given version.

Alpha releases can be published at any time from the `master` branch.

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

For a patch to a previous Skia milestone, first merge the fix into `master` (if applicable). Create a `release/0.<skia-milestone>` branch from that milestone's stable release, then cherry-pick the fix from `master` onto the release branch. Increment the patch number and publish the resulting version from `release/0.<skia-milestone>`.

If the release needs a previous TeamCity build configuration, create a separate TeamCity branch with the same name as the GitHub release branch. TeamCity will then pick it up automatically.
