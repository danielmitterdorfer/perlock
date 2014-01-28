perlock
========

Perlock (short for Path Sherlock) provides a simple and sane path watching API on top of the JDK 7 WatchService API.

# Motivation

Have you ever tried to find out whether some file or directory has been changed during the lifetime of a Java program? Then perlock is for you. Perlock is especially suited for the following tasks:

* Check whether a removable drive was mounted and performing some custom action, for example starting a backup script.
* Watching a file for external changes in a text editor.
* Implementing a file based synchronization system for your application

# Philosophy

Current solutions have either a very cumbersome and error-prone API (JDK 7 WatchService) or are based on polling (Apache Commons VFS). Perlock avoids both problems and tries very hard to get out of your way:

* **No hidden magic:** The perlock API openly communicates that path watching requires background threads instead of silently creating them in the background. Perlock is also just a wrapper on top of the WatchService API. As such, it shares all capabilities and restrictions of WatchService, such as efficient path watching as a capability but also lack of support for watching network drives as a restriction.
* **Minimalistic:** perlock itself is very lightweight both in its implementation and in its dependencies. The only required runtime dependency is the slf4j-api.
* **Easy to use:** The standard use-case (watching paths for creation, modification and deletion) just requires you to implement a single callback interface.
* **Robust and efficient:** Perlock builds on top of the standard WatchService API of the JDK. On many platforms the WatchService implementation uses kernel facilities (such as kqueue or inotify) for file system watching which is very fast and efficient.

# Getting Started

## Installation

Perlock is currently not available as a binary. To use perlock, follow these steps:

1. Clone the repo: `git clone https://github.com/danielmitterdorfer/perlock.git`
2. Build perlock: `gradle install`

Perlock is then installed to your local repo. You can use the library as follows in your Gradle build scripts:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    compile group: 'name.mitterdorfer.perlock', name: 'perlock-core', version: '0.1.0-SNAPSHOT'
}
```

or alternatively via Maven.

```xml
<dependency>
    <groupId>name.mitterdorfer.perlock</groupId>
    <artifactId>perlock-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage

The API allows to create recursive or non-recursive path watchers.

Clients have to implement the callback interface `PathChangeListener` or extend the convenience base class `AbstractPathChangeListener`, for example:

```java
public class SimplePathChangeListener extends AbstractPathChangeListener {
    @Override
    public void onPathCreated(Path path) {
        System.out.println(path + " has been created");
    }
}
```

This listener has to be registered with a so-called `PathWatcher`. Creating a new recursive one for watching `someRootPath` is as easy as it can possibly get:

```java
ExecutorService executorService = Executors.newFixedThreadPool(1);
PathWatcherFactory pathWatchers = new PathWatcherFactory(executorService);
PathWatcher watcher = pathWatchers.createRecursiveWatcher(someRootPath, new SimplePathChangeListener());
watcher.start();
```

The `examples` directory contains a very small sample application that demonstrates recursive path watching in the class `PathWatcherDemo`.

# Prerequisites

Perlock is extremely lightweight and just relies on slf4j-api as runtime dependency. You might want to provide a suitable slf4j implementation but that is up to you. Additionally, JRE 7 is required.

# Building Perlock

Perlock requires Gradle to build. To create all necessary artifacts just clone the repository and type `gradle`.

# Caveats

There are a few caveats you should be aware of when using perlock:

* The JDK 7 WatchService API is platform dependent. Therefore, path watching might be slower on platforms where WatchService falls back to polling (for example JDK 7 in Mac OS X). For a more detailed description of `WatchService` please see its [Javadoc](http://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html).
* The root path that has to exist when the corresponding `PathWatcher` is started.
* If the root path is deleted the `PathWatcher` will be closed without any notification of the client.

# Alternatives

There are a few alternatives to perlock:

* [JDK 7 WatchService API](http://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html): This is the raw API perlock is built on.
* [jpathwatch](http://jpathwatch.sourceforge.net/): It closely resembles the JDK 7 watch service API but requires just JDK 5.
* [jnotify](http://jnotify.sourceforge.net/): It has quite a simple API but relies on a native library.
* [Apache Commons VFS](http://commons.apache.org/proper/commons-vfs/): Commons VFS is a file system abstraction and as such heavyweight if all you need is path watching. Commons VFS has a DefaultFileMonitor which performs path watching via polling.

# Changes

Relevant changes are documented in the [changelog](CHANGELOG.md). Perlock adheres to the [semantic versioning](http://semver.org/) scheme.

# Roadmap

The library is considered pretty much complete. However, there are still some topics that may need to be addressed:

* Reporting exceptions that occurred in the watcher: This could be done with a custom callback interface but details are not settled yet.
* Although all important NIO classes provide interfaces it is still very hard to implement in-memory filesystem tests. The project [memoryfilesystem](https://github.com/marschall/memoryfilesystem) implements an in-memory filesystem on top of JSR-203 but lacks an implementation of `WatchService`. Perlock might use memoryfilesystem in the future for in-memory filesystem tests.

# License

Perlock is distributed under the terms of the [Apache Software Foundation license, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).