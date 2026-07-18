# AGENTS.md

## Objective

Your task is to analyze the Android APK file located in this directory and produce comprehensive documentation describing its implementation, behavior, architecture, and user interface.

The final output should consist of one or more well-organized Markdown documents that are sufficiently detailed for another engineer to understand how the application works without performing the reverse engineering themselves.

---

# Primary Goals

Perform a thorough reverse engineering pass over the APK.

Your documentation should include, where applicable:

- Application overview
- Package structure
- Activities
- Fragments
- Services
- Broadcast receivers
- Content providers
- Permissions
- Manifest analysis
- Navigation flow
- Application lifecycle
- Data storage
- SharedPreferences
- Databases
- Network communication
- APIs
- Authentication
- Encryption
- Native libraries
- Assets
- Resources
- Strings
- Configuration files
- Dependency analysis
- Third-party SDKs
- Security mechanisms
- Background work
- Notifications
- Intent usage
- IPC mechanisms
- Build information
- Obfuscation observations
- Anti-tampering or anti-debugging features (if present)

Document all significant findings.

---

# UI Documentation

Produce a complete walkthrough of the application's UI.

For every screen you can identify:

- Screen name
- Purpose
- Layout hierarchy
- Controls
- Buttons
- Menus
- Navigation
- Dialogs
- User flows
- Inputs
- Outputs
- State transitions

Include inferred behavior when supported by code analysis, and clearly distinguish between observed behavior and inferred behavior.

---

# Architecture Documentation

Document:

- Overall architecture
- Major modules
- Component relationships
- Data flow
- Event flow
- Initialization sequence
- Startup behavior
- Long-running tasks
- Dependency injection (if present)
- Design patterns used

---

# Reverse Engineering

Use any appropriate reverse engineering tools, including but not limited to:

- jadx
- apktool
- aapt
- dex2jar
- CFR
- Bytecode viewers
- Android SDK tools
- jadx-gui (if useful)
- Other open-source Android reverse engineering utilities

Cross-reference results between tools when appropriate.

---

# Deliverables

Produce clear Markdown documentation.

Suggested organization:

```
docs/
    overview.md
    architecture.md
    ui.md
    manifest.md
    networking.md
    storage.md
    security.md
    findings.md
```

Use headings, code blocks, tables, and diagrams (Mermaid is encouraged) where they improve clarity.

---

# Working Practices

- Be systematic.
- Verify findings whenever possible.
- Clearly identify assumptions.
- Distinguish observed facts from inferred behavior.
- Prefer primary evidence from the APK over speculation.
- Keep notes as you progress.

---

# Environment

You do **not** have sudo access.

If you need additional tools, you may use **Nix** to install packages into your environment.

Prefer reproducible tooling through Nix instead of assuming packages are preinstalled.

---

# If Tools Fail

If you encounter a situation where:

- a required tool cannot be installed,
- Nix installation fails,
- a package is unavailable,
- a tool crashes repeatedly,
- or your environment prevents meaningful progress,

**Stop immediately** and ask the user to resolve the environment issue.

Do **not** spend excessive effort attempting alternative installation methods or workarounds once it becomes clear the environment is the blocker.

---

# Completion Criteria

The task is complete only when:

- the APK has been thoroughly analyzed,
- all major components have been documented,
- the UI has been comprehensively described,
- the application's architecture has been documented,
- significant implementation details have been explained,
- and the resulting Markdown documentation is organized, readable, and suitable as technical reference material.
