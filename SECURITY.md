# Security Policy

## Supported versions

Security fixes are applied to the actively developed `main` branch unless a release-specific policy is published.

## Reporting a vulnerability

Please do **not** disclose security vulnerabilities in public GitHub issues.

Use GitHub's private vulnerability reporting/security advisory mechanism for this repository when available. Include:

- a clear description of the issue;
- affected file, component, or version;
- reproducible steps or a minimal proof of concept;
- expected and actual behavior;
- security impact;
- any safe mitigation you know.

Do not include passwords, private keys, tokens, personal data, or other secrets in a report.

## Secrets

Never commit API keys, access tokens, passwords, signing keys, keystores, export credentials, or other secrets. If a secret is accidentally committed, revoke/rotate it immediately and remove it from the repository history using an appropriate GitHub-supported procedure.
