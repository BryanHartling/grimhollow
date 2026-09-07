# Known issues

- Stage 1 remainder is complete; stages 2-6 are not yet implemented. The complete game is not delivered at this checkpoint.
- Full art acceptance remains red: `Validated 45 generated specifications; 5 failures`; 103 legacy sheets lack sources, three new hero sheets are absent, and complete style/frame coverage remains deferred to stage 3.
- Lighting now falls off radially without a visibility-boundary boost; transient effects and lighting of walls rendered after characters remain deferred to stage 3.
- Tests 5-16 are not passing for the full deliverable because the new classes/content are absent; tests 18-19 and Android device gameplay have not run.
- CI retains the full art and three-class gates; the workflow remains red for missing later-stage content. No passing-state tag exists.
- GitHub Actions dispatch/settings requests return HTTP 403 with the supplied token; push-triggered workflows work but may appear after a delay.
- Sandbox fetch/.git writes require the documented escalation route; both continuation checks passed there. Builds must use an elevated fresh Gradle process (`--no-daemon`), because a cached sandbox daemon produced AccessDeniedException for cached dependency JARs.
