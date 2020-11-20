ds3_java_cli
============

[![Build
Status](https://travis-ci.com/SpectraLogic/ds3_java_cli.svg?branch=master)](https://travis-ci.com/SpectraLogic/ds3_java_cli) [![Apache V2 License](http://img.shields.io/badge/license-Apache%20V2-blue.svg)](https://github.com/SpectraLogic/ds3_java_cli/blob/master/LICENSE.md)

## Contact Us

Join us at our [Google Groups](https://groups.google.com/d/forum/spectralogicds3-sdks) forum to ask questions, or see frequently asked questions.

## Install

To install the latest ds3_java_cli download the latest release(either a zip or tar) from the [Releases](../../releases) page.  Inside of the release download there is a `bin` directory and a `lib` directory.  The `bin` directory contains all the executable files for both Linux and Windows.  The `lib` directory contains all the jar files that are needed for the cli.  There should be no need to modify anything inside of the `lib` directory.  The only external dependency is the latest Java 8 JRE. We recommend [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html).

## Usage

After the release bundle has been extracted you can then execute `bin/ds3_java_cli -h` on Linux or `bin/ds3_java_cli.bat -h` on Windows to get the help menu.

**Note:** By default the CLI will attempt to verify the SSL/TLS certificates to make sure they are correctly signed.  This has the side affect that any connections to Black Pearl will fail.  You must either use `--http` or `--insecure` in order to connect to a Black Pearl system.

### Linux Configuration

On Linux you can create a RC or resource file that you use to store common configurations without having to specify those arguments from the command line each time the cli is executed.  The following is an example of a resource file in linux:

```bash
export DS3_ACCESS_KEY="access_key"
export DS3_SECRET_KEY="secret_key"
export DS3_ENDPOINT="hostname:8080"

```

To use the rc file use `source my_rc_file.rc` which will export all of the environment variables into the current bash shell and will be picked up by the CLI.  The help menu describes all the arguments that can be specified from the command line.

### Windows Configuration

On Windows you can create a resource `bat` file to do the same thing.  Here is the same example from above, but as a `bat` file:

```bat
set DS3_ACCESS_KEY=access_key
set DS3_SECRET_KEY=secret_key
set DS3_ENDPOINT=hostname:8080
```

To use the `bat` file just run it from the Windows CLI and the `ds3_java_cli.bat` script will be able to use the values. **Note:** `ds3_java_cli.bat` has to be ran from the same Windows CLI that the resource `bat` file was ran in.

## Proxy Support

The cli supports connecting to DS3 via a HTTP Proxy.  To automatically connect to the proxy from the cli set `http_proxy` as an environment variable and the `ds3_java_cli` will pick up the configuration.  The proxy setting it not required to be set, but if you work in an environment where a proxy is present and the `http_proxy` environment variable is already set, you should not have to do anything.

## Contributing
If you would like to contribute to the source code, sign the [Contributors Agreement](https://developer.spectralogic.com/contributors-agreement/) and make sure that your source conforms to our [Java Style Guide](https://github.com/SpectraLogic/spectralogic.github.com/wiki/Java-Style-Guide).  For an overview of how we use Github, please review our [Github Workflow](https://github.com/SpectraLogic/spectralogic.github.com/wiki/Github-Workflow).

## Build

To build the CLI, you need to make sure that the [ds3_java_sdk](https://github.com/SpectraLogic/ds3_java_sdk) is installed and accessible via maven (see [Installing ds3_java_sdk](https://github.com/SpectraLogic/ds3_java_sdk#install)).  You must also have cloned the latest version of the CLI source.  Then run `gradlew` from the root of the project:

    ./gradlew clean distTar
    
Or:

    ./gradlew clean distZip
    
Both `distZip` and `distTar` will build the bundle and place it in `build/distributions` in the root of the project directory.  To build both versions at the same time run:

    ./gradlew clean distTar distZip

`distZip` will create a zip version of the bundle, and `distTar` will create a tar version of the bundle.  The bundles contain the same exact binaries.
