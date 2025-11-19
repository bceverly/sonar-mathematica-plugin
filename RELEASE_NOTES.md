# Release Notes

## Version 1.1.0

- Updated workflow for release to marketplace
- Added link to SonarCloud status in pull request for a new version
- Fixed several false positives and added more unit tests
- Fixed slash operator issues with Mathematica parser
- Enhanced parser to better understand locally scoped module variables
- Fixed SonarPlugins deploy step
- Fixed git clone in release workflow
- Fixed issues with deploy workflow and release notes generation


**Commits since v1.0.3**


## Version 1.0.3

- Switched to simplified release notes
- Used heredocs to generate PR release notes
- Switched to use of environment variables for release notes file
- Fixed workflow for auto-deploy
- Hopefully fixed a release notes construction issue
- Fixed YAML syntax error
- Fixed multi-line escaping issue
- Fixed some rules that were missing builtins and added deploy workflow
- Cleaned up some overaly strict rules


**Commits since v1.0.2**

## Version 1.0.1

- Change plugin key from 'mathematica' to 'wolfralyze' per SonarSource requirements
- Updated severity of rule violations
- Added screenshots

**Full Changelog**: https://github.com/bceverly/wolfralyze/compare/v1.0.0...v1.0.1

## Version 1.0.0

- Fixed release workflow for rebranding
- Relicensed under AGPLv3
- Updated for rebranding and new website
- Rebranded SonarQube project name
- Updated for rebranding of test project
- Added unit tests for new coverage analysis
- Updated documentation for code coverage analysis
- Added code coverage analysis
- Added more badges to README
- Added new consolidated ci workflow
- Removed deprecated methods from previous AST refactoring
- Fixed SonarQube reported issues and added more unit tests
- Added SBOM
- Downgraded version of gradle to work with github actions
- Fixed java version
- Added NCLOC metric and some documentation
- Updated docs for publishing plugin
- Clean bill of health on lint
- Added IDE integration
- Upgraded gradle to Java 17
- Enhanced infrastructure for build and deploy
- Moved save() calls to queued thread to help performance
- Added parallelization to speed up scan
- Addressed shortcoming of trying to over-optimize and skipping files
- Added parser and more enhancements
- Expanded scope of large file analysis and improved ability of commented code detection to work better
- Fixed username in build.gradle
- Fixed license in build.gradle
- Added license
- Initial commit: Mathematica SonarQube plugin with CPD support

**Initial Release**
