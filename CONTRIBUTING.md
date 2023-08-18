# Welcome to the Shops contributing guidelines

Thank you for investing your time in contributing to this project! Any contribution you make will be reflected in the [Shops contributors](https://github.com/BrendonButler/Shops/graphs/contributors).

Read our [Code of Conduct](CODE_OF_CONDUCT.md) to keep our community approachable and respectable.

In this guide you will get an overview of the contribution workflow from forking the repository, creating a PR, reviewing, and merging the PR.

To get an overview of the project, read the [README](README.md).

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

2. Create a working branch and start with your changes!
    - Create a branch with the appropriate prefix for the issue type:
      - `feature/GH-{ISSUE_NUM}` - use the "feature/GH-##" prefix to create the new feature branch
      - `fix/GH-{ISSUE_NUM}` - use the "fix/GH-##" prefix to create a fix branch for bugs and other issues

### Commit your update

When creating commits, please use this format and if there are multiple updates that don't make sense to be added to the specific issue ID, you can create a new line for another issue:
- `GH-123 add CreateCommand to create stores`
- `GH-9999 fix command sender validation in CreateCommand`
- `update README links`

This will ensure fantastic traceability on issues and how the commits relate. For updates such as README enhancements, you can omit the issue tag. These tags will be clickable in the commit history to quickly bring up the issue.

### Pull Request

When you're finished with the changes, create a pull request, also known as a PR.
- Don't forget to [link PR to issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue) if you are solving one.
- Enable the checkbox to [allow maintainer edits](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/allowing-changes-to-a-pull-request-branch-created-from-a-fork) so the branch can be updated for a merge.
  Once you submit your PR, a project admin will review your proposal. We may ask questions or request additional information.
- We may ask for changes to be made before a PR can be merged, either using [suggested changes](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/incorporating-feedback-in-your-pull-request) or pull request comments. You can apply suggested changes directly through the UI. You can make any other changes in your fork, then commit them to your branch.
- As you update your PR and apply changes, mark each conversation as [resolved](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/commenting-on-a-pull-request#resolving-conversations).
- If you run into any merge issues, checkout this [git tutorial](https://github.com/skills/resolve-merge-conflicts) to help you resolve merge conflicts and other issues.