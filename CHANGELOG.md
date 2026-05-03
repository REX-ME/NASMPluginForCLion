# Changelog

## [Unreleased] 
## INFO: "Unreleased" is just for the Github Workflow, it's released
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template) **15. March 2026**
- Syntax Highlighting for much NASM x86 Opcodes **26. April 2026**
- (Still Bad, it will be better) Error Analysis **26. April 2026**
- Highlighting NASM Macros (%include is actual bad) **26. April 2026**
- GoTo Definition (Labels) **26. April 2026**
- Missing: Refactor (It will make an IDE-Error, so you don't must report it) **26. April 2026**
- **Bugfixes:**
- - **3. May 2026**:
- - - Escape-Fix, in ' ' and " " NASM don't escape
- - - extern/common labels are now LABEL_DEF, so there is autocomplete for them
