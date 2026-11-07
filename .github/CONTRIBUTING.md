# Welcome to the Shops contributing guidelines

Thank you for investing your time in contributing to this project! Any contribution you make will be reflected in the [Shops contributors](https://github.com/BrendonButler/Shops/graphs/contributors).

Read our [Code of Conduct](CODE_OF_CONDUCT.md) to keep our community approachable and respectable.

In this guide you will get an overview of the contribution workflow from forking the repository, creating a PR, reviewing, and merging the PR.

To get an overview of the project, read the [README](../README.md).

## Getting started

### Pre-requisites

1. Have [Git](https://github.com/git-guides/install-git) installed and [configured](https://git-scm.com/book/en/v2/Getting-Started-First-Time-Git-Setup) on your machine
2. [Fork the Shops repository](https://github.com/BrendonButler/Shops/fork) into your own account
3. Optional: Utilize the Project codestyle for reformatting changes <br>_found here: `Shops/.idea/codeStyles/Project.xml`_

### Issues/Features

#### Create a new issue/feature request

If you identify an issue or want to make a suggestion, [search if an issue already exists](https://docs.github.com/en/github/searching-for-information-on-github/searching-on-github/searching-issues-and-pull-requests#search-by-the-title-body-or-comments). If a related issue doesn't exist, you can open a new issue using a relevant [issue form](https://github.com/BrendonButler/Shops/issues/new).

#### Solve an issue or implement a feature

If an issue doesn't exist for a feature you want to implement, please create an issue first for pre-review. Once it's determined that the feature should be implemented, and you get feedback from owners on the repo, feel free to work on the issue and create a PR.

Scan through the [existing issues](https://github.com/BrendonButler/Shops/issues) to find one that interests you. You can narrow down the search using `labels` as filters. See [Labels](/contributing/how-to-use-labels.md) for more information. As a general rule, we don’t assign issues to anyone. If you find an issue to work on, you are welcome to open a PR with a fix.

#### Make changes locally

1. Fork the repository.
    - Using GitHub Desktop:
      - [Getting started with GitHub Desktop](https://docs.github.com/en/desktop/installing-and-configuring-github-desktop/getting-started-with-github-desktop) will guide you through setting up Desktop.
      - Once Desktop is set up, you can use it to [fork the repo](https://docs.github.com/en/desktop/contributing-and-collaborating-using-github-desktop/cloning-and-forking-repositories-from-github-desktop)!

    - Using the command line:
      - [Fork the repo](https://docs.github.com/en/github/getting-started-with-github/fork-a-repo#fork-an-example-repository) so that you can make your changes without affecting the original project until you're ready to merge them.

2. Create a working branch from `main` and start with your changes!
    - Name the branch `<type>/<issue>-<short-description>`, for example `fix/35-mockbukkit-tests` or `feat/32-permissions`. Leave out the issue number if there isn't one.
    - Use one of these types:
      - `feat` - a new feature
      - `fix` - a bug fix
      - `docs` - documentation only
      - `refactor` - code changes that don't change behavior
      - `test` - adding or fixing tests
      - `ci` - build and pipeline changes
      - `chore` - anything else, such as dependency updates or cleanup
    - Keep branches short-lived and focused on one issue, so they're quick to review and merge.

### Build environment

Shops builds with JDK 25 and Maven, the same as CI. Either install both locally, or open the repository in its dev
container (`.devcontainer/`) with VS Code, IntelliJ IDEA or GitHub Codespaces, which has JDK 25, Maven and Docker ready.

```bash
mvn verify                                # build and run the tests
mvn -Pmc26 verify                         # build and test against the 26.x API
mvn -Pspigot-api -pl spigot -am compile   # catch Paper-only API calls
```

### Testing on a local server

`compose.yaml` runs Minecraft test servers in Docker with the plugin you just built, plus Vault and EssentialsX as the
economy, so buying and selling work. Server, Vault and EssentialsX versions are pinned, so everyone tests the same thing.
You need [Docker](https://docs.docker.com/get-started/get-docker/) with Compose. Starting a server accepts the
[Minecraft EULA](https://aka.ms/MinecraftEULA). On Windows, run the commands and scripts below from WSL or Git Bash:
`docker compose` works in any shell, but `docker/reload.sh` and the IntelliJ IDEA shell run configurations need a Unix
shell.

| Profile      | Server                                               | Address           | Debugger port |
|--------------|------------------------------------------------------|-------------------|---------------|
| `paper-1.21` | Paper 1.21.11, the version the plugin is built for   | `localhost:25565` | `5005`        |
| `paper-26`   | Paper 26.3, the newest version CI tests against      | `localhost:25566` | `5006`        |
| `spigot`     | Spigot 1.21.11, to catch Paper-only behavior         | `localhost:25567` | `5007`        |

1. Copy the example settings. `.env` picks which server `docker compose` starts by default. In `docker/local.env`, add
   your Minecraft name to `OPS` so you can use every `/shop` command. Both copies are git-ignored:
   ```bash
   cp .env.example .env
   cp docker/local.env.example docker/local.env
   ```
2. Build the plugin JAR into `spigot/target`:
   ```bash
   mvn package -DskipTests
   ```
3. Start the default server, Paper 1.21.11. The first start downloads the server and takes a minute or two:
   ```bash
   docker compose up -d
   docker compose logs -f
   ```
4. Join `localhost:25565` from Minecraft 1.21.11.
5. After changing code, rebuild and load the new JAR. See [Trying out changes](#trying-out-changes) for faster options:
   ```bash
   mvn package -DskipTests && docker compose restart
   ```
6. Open the server console to run commands such as `eco give <player> 1000`, and detach with `Ctrl+P` then `Ctrl+Q`:
   ```bash
   docker compose attach paper-1.21
   ```
7. Stop the server when you're done. World and plugin data are kept for next time:
   ```bash
   docker compose down
   ```

To test on another server, pick its profile. Each server has its own port and data, so they can also run side by side:
```bash
COMPOSE_PROFILES=paper-26 docker compose up -d
```
or change `COMPOSE_PROFILES` in your `.env`. Use the same `COMPOSE_PROFILES` for `logs`, `restart` and `down`, or
name the server directly, such as `docker compose restart paper-26`. The Spigot server is built from source with
Spigot's BuildTools, so its first start takes several minutes, and again after a version change or a reset. Later starts
are as fast as Paper's.

To reset a server's world and plugin data (stores, balances, configs), remove its volume:
```bash
docker compose down -v                    # every server in the selected profiles
docker compose rm -sf paper-26 && docker volume rm shops_paper-26-data   # one server
```

#### Trying out changes

From slowest and most reliable to fastest:

- **Restart** (about 15 seconds): `docker compose restart` installs the new JAR and starts the server fresh. Use it
  whenever you're unsure, and always before reporting a bug.
- **Reload** (about 1 second): `docker/reload.sh` installs the new JAR into every running test server and runs
  `/bukkit:reload confirm`, which disables and re-enables all plugins. Name servers to limit it, such as `docker/reload.sh paper-26`.
  Paper and Spigot don't support `/reload`, Paper plans to remove it, and EssentialsX logs an error each time, so
  restart if anything behaves strangely.
- **Hot swap** (instant): every test server listens for a Java debugger on its debugger port, on localhost only. Attach
  your IDE's remote debugger, and changes inside method bodies are swapped into the running server when you recompile,
  with no reload or restart. The JVM can't hot swap new or removed methods, fields or classes, or changes to
  `plugin.yml`, so reload or restart for those.

#### In IntelliJ IDEA

Shared run configurations in `.run/` appear under *Test servers* in the run configuration list:

- **Paper 1.21.11**, **Paper 26.3** and **Spigot 1.21.11** build the plugin, then start that server with its log in
  the Run window. Stopping the run configuration stops the server.
- **Reload Shops** builds the plugin, then runs `docker/reload.sh` on every running test server.
- **Debug Paper 1.21.11**, **Debug Paper 26.3** and **Debug Spigot 1.21.11** build the plugin, start that server in
  the background if it isn't running yet, wait until it's ready, and attach the debugger. Stopping the debug session
  only detaches, and the server keeps running.
- **Start Paper 1.21.11 (background)** and the other *Start* configurations do the build-and-start part on their own.
  The *Debug* configurations run them first.

To work on a change with hot swap:

1. Click **Debug Paper 1.21.11** (or another server), with the debug button rather than the run button. Hot swap only
   reaches servers with a debug session attached, so a server started any other way keeps running the old code until
   you attach. To hot swap into several servers at once, start the *Debug* configuration for each one.
2. Change code inside a method, then build with *Build > Build Project*. IntelliJ IDEA asks to reload the changed
   classes, or does it automatically with *Reload classes after compilation* set to *Always* under *Settings > Build,
   Execution, Deployment > Debugger > HotSwap*. The next command you run in game uses the new code.
3. If IntelliJ IDEA reports that hot swap failed, for example after adding a method or field, run **Reload Shops**. The
   debugger stays attached.

The shared compiler settings turn off IntelliJ IDEA's *Add runtime assertions for notnull-annotated methods and
parameters* option. That option adds a hidden method to classes that Maven's build doesn't have, so every hot swap of
those classes would fail with "add method not implemented".

To see the server log while debugging, also run **Paper 1.21.11** (it attaches to the same server), or open the
container in the *Services* tool window.

They use the Shell Script plugin, which is bundled with IntelliJ IDEA, and need `docker` on the `PATH`. On Windows, set
the Shell Script interpreter to WSL or Git Bash.

#### Updating the pinned versions

Everything the test servers run is pinned in `compose.yaml`, so updates are deliberate and everyone moves together:

| What                    | Setting in `compose.yaml`               | Where to find new versions                                                      |
|-------------------------|-----------------------------------------|---------------------------------------------------------------------------------|
| Server image            | `image:` tag, for example `2026.10.1-java25` | [Docker Hub](https://hub.docker.com/r/itzg/minecraft-server/tags)          |
| Paper version and build | `VERSION` and `PAPER_BUILD`             | [PaperMC downloads](https://papermc.io/downloads/paper)                         |
| Spigot version          | `VERSION` on the `spigot` service (built with BuildTools) | [SpigotMC](https://www.spigotmc.org/wiki/buildtools/)         |
| Vault and EssentialsX   | the download links in `PLUGINS`         | [Vault](https://github.com/MilkBowl/Vault/releases), [EssentialsX](https://github.com/EssentialsX/Essentials/releases) |

- Keep the servers in step with the API versions in `pom.xml`: `paper-1.21` runs the version in `papermc-api.version`,
  and `paper-26` runs the version and build in the `mc26` profile (`26.3.build.243-stable` is `VERSION: "26.3"` with
  `PAPER_BUILD: "243"`). When a new 26.x comes out, update the profile and the `paper-26` service together.
- Keep the image's Java version (the `-java25` suffix) at or above what the newest server needs. The `spigot` service
  sets its own `image:`, the `-jdk` variant of the same release, because BuildTools needs a JDK. Update both tags
  together.
- After changing a version, run `docker compose up -d` to recreate the server. Plugin JARs dropped from `PLUGINS` are
  removed automatically.
- Minecraft worlds can be upgraded but not downgraded. When moving a server to an older version, reset its volume.

### Branches and releases

Shops uses [GitHub flow](https://docs.github.com/en/get-started/using-github/github-flow):
- `main` is the only long-lived branch. Every change reaches it through a pull request, and CI must pass first.
- `main` should always be releasable, because any commit on it can become the next release.
- Releases, including beta builds, are tags on `main` (for example `0.4.0-beta.1`). Maintainers create them.
- A `release/<major>.<minor>.x` branch is only created when an older version needs a fix after a newer one is out.

### Commit your update

When creating commits, please use this format and if there are multiple updates that don't make sense to be added to the specific issue ID, you can create a new line for another issue:
- `GH-123 add CreateCommand to create stores`
- `GH-9999 fix command sender validation in CreateCommand`
- `update README links`

This will ensure fantastic traceability on issues and how the commits relate. For updates such as README enhancements, you can omit the issue tag. These tags will be clickable in the commit history to quickly bring up the issue.

### Pull Request

When you're finished with the changes, create a pull request, also known as a PR.
- Open the PR against `main`.
- Don't forget to [link PR to issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue) if you are solving one.
- Enable the checkbox to [allow maintainer edits](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/allowing-changes-to-a-pull-request-branch-created-from-a-fork) so the branch can be updated for a merge.
  Once you submit your PR, a project admin will review your proposal. We may ask questions or request additional information.
- We may ask for changes to be made before a PR can be merged, either using [suggested changes](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/incorporating-feedback-in-your-pull-request) or pull request comments. You can apply suggested changes directly through the UI. You can make any other changes in your fork, then commit them to your branch.
- As you update your PR and apply changes, mark each conversation as [resolved](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/commenting-on-a-pull-request#resolving-conversations).
- If you run into any merge issues, checkout this [git tutorial](https://github.com/skills/resolve-merge-conflicts) to help you resolve merge conflicts and other issues.