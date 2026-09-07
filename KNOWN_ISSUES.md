# Known issues

This is a work in progress, not a passing Grimhollow delivery.

- The verified latest release is v3.3.8, commit 7b8b845a76fe76c6b7c031ae9e570852411f56db. It contains six upstream heroes, whereas the specification assumes five. Preserving existing heroes requires nine total after the three additions; test 5's count of eight conflicts with that requirement.
- Upstream already has the Huntress WARDEN subclass. The Enchanter's requested Warden needs a distinct internal enum key while retaining its localized display name.
- The bundled Git could not clone: `git: 'remote-https' is not a git command.` The full installation at `C:\Program Files\Git\cmd\git.exe` works.
- Sandboxed network requests failed with `An attempt was made to access a socket in a way forbidden by its access permissions.` Git reported `Failed to connect to github.com:443`. The environment required escalation; escalated release lookup and cloning succeeded.
- The environment protects `.git` even inside the workspace: `Unable to create ... grimhollow.lock: Permission denied`. Branch, author, and remote setup succeeded with escalation.
- Acceptance tests 1-23 have not yet run.
