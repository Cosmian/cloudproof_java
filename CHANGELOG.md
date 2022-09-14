# Changelog

All notable changes to this project will be documented in this file.

---
## [0.8.2] - 2022-09-14
### Added
### Changed
### Fixed
- Findex->search: retry FFI call with adjusted allocated size
### Removed
---


---
## [0.8.1] - 2022-09-08
### Added
- Publish to Maven Central on tags creation

---
## [0.8.0] - 2022-09-02
### Changed
- Rename Findex native shared library to `cosmian_findex`
- Update `abe_gpsw` shared library to 2.0.1
- Update `cover_crypt` shared library to 6.0.1
- Update Policy according to new Format
  * `maxAttributeValue` becomes `maxAttributesCreations`
  * `store` becomes `axes`
- Update to `kms` 2.2.0

---
## [0.7.8] - 2022-08-22
### Fixed
- Findex Master keys deserialization (K* deduplicated)

---
## [0.7.7] - 2022-07-28
### Added
### Changed
### Fixed
- KMIP KeyValue structure has changed in KMS from version 2.0.5
### Removed
---
