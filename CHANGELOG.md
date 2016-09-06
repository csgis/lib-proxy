# Change Log
This project adheres to [semantic versioning](http://semver.org/).

## [Unreleased]

## [1.0.2] [2016-09-06]
### Fixed
- Fixed SSL support in ConfigurableProxyServlet [2016-09-06]

## [1.0.1] [2016-08-10]
### Changed
- Java 7 instead of 8.

## 1.0.0 [2016-08-09]
### Added
- `ConfigurableHttpServletRequest`: a `HttpServletRequest` where params and headers can be added.
- `ConfigurableProxyServlet`: a (jetty) `ProxySevlet` where destination URI can be configured.

[Unreleased]: https://github.com/csgis/lib-json/compare/1.0.2...HEAD
[1.0.2]: https://github.com/csgis/lib-json/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/csgis/lib-json/compare/1.0.0...1.0.1
