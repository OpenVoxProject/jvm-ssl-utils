# Changelog

All notable changes to this project will be documented in this file.

## [3.7.2](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.7.2) (2026-11-19)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.7.1...3.7.2)

**Merged pull requests:**

- Sign CSRs with SHA-256 and run the FIPS tests in approved-only mode [\#71](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/71) ([nmburgan](https://github.com/nmburgan))
- Add the Prepare Release workflow [\#64](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/64) ([nmburgan](https://github.com/nmburgan))
- Move the FIPS test profile to BouncyCastle FIPS 2.1 [\#63](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/63) ([nmburgan](https://github.com/nmburgan))
- add script to regenerate x509 test fixtures [\#57](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/57) ([jcharaoui](https://github.com/jcharaoui))
- Update minimum Leiningen version to 2.12.0 [\#54](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/54) ([corporate-gadfly](https://github.com/corporate-gadfly))

## [3.7.1](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.7.1) (2026-08-11)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.7.0...3.7.1)

**Merged pull requests:**

- fix clj-kondo lint error/warning [\#53](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/53) ([corporate-gadfly](https://github.com/corporate-gadfly))

## [3.7.0](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.7.0) (2026-07-20)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.6.4...3.7.0)

**Implemented enhancements:**

- Upgrade BouncyCastle 1.84-\>1.85 [\#47](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/47) ([Sharpie](https://github.com/Sharpie))

**Merged pull requests:**

- Update test matrix to include FIPS [\#46](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/46) ([Sharpie](https://github.com/Sharpie))
- regenerate CRL validation PEM files by using test harness [\#36](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/36) ([corporate-gadfly](https://github.com/corporate-gadfly))

## [3.6.4](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.6.4) (2026-04-24)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.6.3...3.6.4)

**Implemented enhancements:**

- CI: Add Java 25 support [\#34](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/34) ([bastelfreak](https://github.com/bastelfreak))

## [3.6.3](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.6.3) (2026-04-12)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.6.2...3.6.3)

**Merged pull requests:**

- Potential fix for 1 code quality finding [\#30](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/30) ([corporate-gadfly](https://github.com/corporate-gadfly))
- CodeQL fixes [\#29](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/29) ([corporate-gadfly](https://github.com/corporate-gadfly))
- replace clj-commons/fs with clojure.java.io for file handling [\#28](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/28) ([corporate-gadfly](https://github.com/corporate-gadfly))
- replace Joda-Time with Java Time API for date handling [\#27](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/27) ([corporate-gadfly](https://github.com/corporate-gadfly))
- remove commons-codec dependency [\#26](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/26) ([corporate-gadfly](https://github.com/corporate-gadfly))
- Move version pins [\#24](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/24) ([nmburgan](https://github.com/nmburgan))

## [3.6.2](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.6.2) (2026-01-09)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.6.1...3.6.2)

**Merged pull requests:**

- CI: Add Vox Pupuli best practices [\#12](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/12) ([bastelfreak](https://github.com/bastelfreak))
- CI: Add Vox Pupuli best practices [\#11](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/11) ([bastelfreak](https://github.com/bastelfreak))
- Remove clj-parent [\#10](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/10) ([nmburgan](https://github.com/nmburgan))

## [3.6.1](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.6.1) (2026-01-03)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.6.0...3.6.1)

**Merged pull requests:**

- Bump clj-parent to 7.6.3 and i18n [\#9](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/9) ([nmburgan](https://github.com/nmburgan))

## [3.6.0](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.6.0) (2026-01-03)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.5.5...3.6.0)

**Merged pull requests:**

- Update dependencies and go back to BC FIPS 1.x  [\#8](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/8) ([nmburgan](https://github.com/nmburgan))

## [3.5.5](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.5.5) (2025-12-31)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.5.4...3.5.5)

**Merged pull requests:**

- Bump clj-parent and i18n [\#7](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/7) ([nmburgan](https://github.com/nmburgan))
- Fix unsupported Java version message [\#6](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/6) ([nmburgan](https://github.com/nmburgan))
- Remove extra download of clj-kondo [\#5](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/5) ([nmburgan](https://github.com/nmburgan))

## [3.5.4](https://github.com/OpenVoxProject/jvm-ssl-utils/tree/3.5.4) (2025-12-31)

[Full Changelog](https://github.com/OpenVoxProject/jvm-ssl-utils/compare/3.5.3...3.5.4)

**Merged pull requests:**

- Update workflow name and add license [\#4](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/4) ([nmburgan](https://github.com/nmburgan))
- CI and version: Changes for OpenVox namespacing [\#1](https://github.com/OpenVoxProject/jvm-ssl-utils/pull/1) ([nmburgan](https://github.com/nmburgan))

## [unreleased]

# [3.5.3]
* add functions to SSLUtil to alter and retrieve CRL expiration time
* update clj-parent to 5.6.4
* update issue submission instructions in README.

# [3.5.2]
* remove infinite recursion from ASN1 conversion.
* update clj-parent to 5.6.1 which includes bouncy castle 1.74

# [3.5.1]
* add support for creating csrs with Attributes
* remove use of deprecated routines in ExtensionUtils 
* add support for reading attributes from Certificate Signing Requests

# [3.5.0]


\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
