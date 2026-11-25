git commit -m "Initial commit"# Local Git Setup and Commit Guide (macOS)

This guide explains how to configure Git on your Mac, connect to GitHub
using SSH, and commit and push a local project.

## 1. Check Git installation

Open Terminal and run:

``` bash
git --version
```

If Git is not installed, install Apple's Command Line Tools:

``` bash
xcode-select --install
```

## 2. Configure your Git identity

Use the name and email you want associated with your commits. The email
should ideally be verified on your GitHub account.

``` bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Verify the configuration:

``` bash
git config --global --list
```

## 3. Create an SSH key for GitHub

First check whether you already have an SSH key:

``` bash
ls -al ~/.ssh
```

If you already have a suitable `id_ed25519` key, you may be able to
reuse it. Otherwise, generate a new key:

``` bash
ssh-keygen -t ed25519 -C "you@example.com"
```

When asked where to save the key, press **Enter** to accept the default
path:

``` text
/Users/your-mac-username/.ssh/id_ed25519
```

Set a passphrase when prompted for better protection, or press Enter to
leave it empty.

The generated files are:

-   `~/.ssh/id_ed25519` --- private key. **Never share or upload this
    file.**
-   `~/.ssh/id_ed25519.pub` --- public key. This is the key you add to
    GitHub.

## 4. Add the key to the macOS SSH agent

Start the SSH agent:

``` bash
eval "$(ssh-agent -s)"
```

For a key protected by a passphrase, add it to the macOS Keychain:

``` bash
ssh-add --apple-use-keychain ~/.ssh/id_ed25519
```

If your macOS/OpenSSH version does not support `--apple-use-keychain`,
use:

``` bash
ssh-add ~/.ssh/id_ed25519
```

## 5. Add the public key to GitHub

Copy the public key to your clipboard:

``` bash
pbcopy < ~/.ssh/id_ed25519.pub
```

Then:

1.  Open <https://github.com/settings/keys>.
2.  Select **New SSH key**.
3.  Enter a title, such as `My Mac`.
4.  Choose **Authentication Key** as the key type.
5.  Paste the public key and select **Add SSH key**.

Only add the `.pub` public key. Never paste the private key into GitHub,
a chat, or a repository.

## 6. Test SSH authentication

Run:

``` bash
ssh -T git@github.com
```

The first connection may ask you to confirm GitHub's host fingerprint.
Verify it against GitHub's published SSH key fingerprints before
accepting.

A successful authentication message looks similar to:

``` text
Hi YOUR_USERNAME! You've successfully authenticated, but GitHub does not provide shell access.
```

## 7. Create an empty GitHub repository

1.  Open <https://github.com/new>.
2.  Enter a repository name, for example `DemoRDSwithSpringBoot`.
3.  Choose **Private** or **Public**.
4.  If your local project already has files, leave **Add a README**,
    **.gitignore**, and **license** unchecked to avoid an initial
    history conflict.
5.  Create the repository.

Copy the repository's SSH URL. It has this format:

``` text
git@github.com:YOUR_USERNAME/REPOSITORY_NAME.git
```

## 8. Prepare your local project

Move to the project directory. Example:

``` bash
cd ~/Downloads/DemoRDSwithSpringBoot
```

Before staging files, create or review `.gitignore` in the project root.
A basic Java/Spring Boot example:

``` gitignore
# Maven build output
target/

# IntelliJ IDEA
.idea/
*.iml

# Eclipse
.classpath
.project
.settings/

# macOS
.DS_Store

# Local environment and secrets
.env
.env.*
application-local.properties
application-secret.properties
```

**Important:** Review your configuration files before committing. Do not
commit AWS access keys, secret keys, database passwords, tokens, private
certificates, or other credentials. Keep secrets in environment
variables or local files excluded by `.gitignore`. If a secret has
already been committed or pushed, removing it in a later commit is not
enough; rotate/revoke that secret.

If you need to keep an example configuration in the repository, use
placeholders such as `${DB_USERNAME}` and `${DB_PASSWORD}`, not real
values.

## 9. Initialize Git and make the first commit

Run these commands from the project root:

``` bash
git init
git branch -M main
git status
git add .
git status
git commit -m "Initial commit"
```

Review the second `git status` output before committing. Make sure no
secrets or unwanted files are staged.

## 10. Connect the local project to GitHub and push

Replace the username and repository name with your own:

``` bash
git remote add origin git@github.com:YOUR_USERNAME/REPOSITORY_NAME.git
git remote -v
git push -u origin main
```

Refresh your GitHub repository page to confirm that the files are
present.

If `origin` already exists, inspect it:

``` bash
git remote -v
```

To correct its URL:

``` bash
git remote set-url origin git@github.com:YOUR_USERNAME/REPOSITORY_NAME.git
```

Then push:

``` bash
git push -u origin main
```

## 11. Commit and push future changes

Whenever you modify your project:

``` bash
git status
git add .
git status
git commit -m "Describe your changes"
git push
```

Use a clear commit message, for example:

``` bash
git commit -m "Add employee CRUD APIs"
```

## 12. Useful Git commands

  -------------------------------------------------------------------------
  Command                               Purpose
  ------------------------------------- -----------------------------------
  `git status`                          Show changed, staged, and untracked
                                        files

  `git add .`                           Stage changes in the current
                                        directory

  `git add path/to/file`                Stage one specific file

  `git diff`                            Show unstaged changes

  `git diff --staged`                   Show staged changes

  `git log --oneline`                   Show concise commit history

  `git remote -v`                       Show configured remote repositories

  `git branch`                          Show local branches

  `git pull`                            Fetch and integrate remote changes

  `git push`                            Push commits to the remote
                                        repository

  `git restore path/to/file`            Discard unstaged changes to a file
                                        (use carefully)

  `git restore --staged path/to/file`   Unstage a file without deleting its
                                        changes
  -------------------------------------------------------------------------

## Troubleshooting

### `Permission denied (publickey)`

-   Confirm the public key was added to the correct GitHub account.

-   Check that the SSH agent has the key loaded:

    ``` bash
    ssh-add -l
    ```

-   Test again:

    ``` bash
    ssh -T git@github.com
    ```

### `remote origin already exists`

Inspect the remote and update it if needed:

``` bash
git remote -v
git remote set-url origin git@github.com:YOUR_USERNAME/REPOSITORY_NAME.git
```

### Push rejected because the remote contains work

This can happen if you initialized the GitHub repository with a README
or other file. First inspect the remote and local history. Do not
force-push blindly. If the remote contains only an initial README
commit, you can usually integrate it with:

``` bash
git pull --rebase origin main
git push -u origin main
```

Resolve any reported conflicts before continuing.

------------------------------------------------------------------------

## Recommended workflow

For each new local project:

1.  Create an empty repository on GitHub.
2.  Add a suitable `.gitignore`.
3.  Run `git init`, `git add`, and `git commit`.
4.  Add the GitHub SSH remote.
5.  Run `git push -u origin main`.

For later updates, use `git add`, `git commit`, and `git push`.
