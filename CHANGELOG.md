# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

[Tags on this repository](https://github.com/appulse-projects/encon-java/tags)

## [Unreleased]

- Speed-up serialization/deserialization.
- Provide more custom exceptions.
- Review debug logs.
- Add `monitor` functionality.
- Add tests.
- Rething caches in terms and modules.
- Add lazy deserialization for terms.
- Add `UDP`/`SCTP` protocols support.
- Rethink handshake implementation design.
- Add handshake error handling (exceptions? error logs?).
- Turn on checkstyle JavaDocs module.
- Add updates to the protocol, like new `ControlMessage`.
- Do we really need `ErlangInteger.cache` methods?
- Add atom's table equivalent.

## [1.6.9](https://github.com/appulse-projects/encon-java/releases/tag/1.6.9) - 2019-04-07

### Changed

- Updated dependencies and plugins versions.

## [1.6.7](https://github.com/appulse-projects/encon-java/releases/tag/1.6.7) - 2018-10-07

### Changed

- Quick fix for [GitHub issue](https://github.com/appulse-projects/encon-java/issues/13), removed `ErlangPid` and `EralngAtom` caches.

## [1.6.6](https://github.com/appulse-projects/encon-java/releases/tag/1.6.6) - 2018-10-02

### Changed

- Quick fix for [GitHub issue](https://github.com/appulse-projects/encon-java/issues/11), refactored `ErlangInteger` cache;
- Correctly setup `checkstyle`/`epmd`/`findbugs` validation phases.

## [1.6.5](https://github.com/appulse-projects/encon-java/releases/tag/1.6.5) - 2018-09-28

### Changed

- Quick fix for [GitHub issue](https://github.com/appulse-projects/encon-java/issues/10), added `ATOM` and `SMALL_ATOM` serialization.

## [1.6.4](https://github.com/appulse-projects/encon-java/releases/tag/1.6.4) - 2018-09-14

### Added

- `Mailbox.call()` and `Mailbox.receiveRemoteProcedureResult()`, a remote procedure call functionality;
- `Mailbox.exit()` without arguments, for normal mailbox exiting;
- Benchmark's `README.md` section about how to run single/group of becnhmark(s).

### Changed

- Tuned server and client `Netty`'s settings, especially its `WRITE_BUFFER_WATER_MARK` option;
- Now, use `channel.eventLoop().execute(Runnable)` for server's responses, instead of creating composite buffer and single `writeAndFlush` call;
- Benchmark's results;
- Fix samples `pom.xml` files.

## [1.6.3](https://github.com/appulse-projects/encon-java/releases/tag/1.6.3) - 2018-09-11

### Changed

- Removed multi-handlers `netty` pipeline, use single one instead;
- Cache `atom` and `pid` values;
- Improve benchmarks.

## [1.6.2](https://github.com/appulse-projects/encon-java/releases/tag/1.6.2) - 2018-09-06

### Added

- Many new example projects.

### Changed

- Names of some properties in `Defaults` and `NodeConfig` configurations, for better compatibility with `Spring` and its way of config instanctiation;
- Completed readme files.

## [1.6.1](https://github.com/appulse-projects/encon-java/releases/tag/1.6.1) - 2018-09-05

### Added

- Simple example project.

### Changed

- Node creation respects config's `shortName` flag now.


## [1.6.0](https://github.com/appulse-projects/encon-java/releases/tag/1.6.0) - 2018-08-21

### Added

- Spring Boot support.
- Benchmarks.
- Example projects.

## [1.5.0](https://github.com/appulse-projects/encon-java/releases/tag/1.5.0) - 2018-07-07

Remove non blocking queues, because it doesn't guarantee the order.

### Changed

- Replaced non-blocking queue to blocking.
- Edit docs.

### Removed

- Non-blocking queues.

## [1.4.0](https://github.com/appulse-projects/encon-java/releases/tag/1.4.0) - 2018-07-07

Add `encon-handler`.

### Changed

- Add `asText` implementation for `ErlangBinary`.
- Update dependencies.

## [1.3.1](https://github.com/appulse-projects/encon-java/releases/tag/1.3.1) - 2018-06-28

Fix short node name host detection.

## [1.3.0](https://github.com/appulse-projects/encon-java/releases/tag/1.3.0) - 2018-06-28

Add correct node naming.

### Changed

- Configs from `encon-config` are now cloneable via constructor.
- `NodeDescriptor` works as in Erlang's docs said.
- Several minor fixes.

## [1.2.0](https://github.com/appulse-projects/encon-java/releases/tag/1.2.0) - 2018-06-25

Refactoring and cleaning.

### Added

- `bstring` methods to `Erlang` term helper class.

### Changed

- Refactored external and internal APIs.

## [1.1.0](https://github.com/appulse-projects/encon-java/releases/tag/1.1.0) - 2018-06-19

Small refactoring and cleaning.

### Added

- Make receive/handler approaches in `mailbox` mutually exclusive.
- JavaDocs.

### Changed

- Small refactoring.

## [1.0.0](https://github.com/appulse-projects/encon-java/releases/tag/1.0.0) - 2018-05-01

Initial release.
