# it.bz.noi.community.android
NOI Community App for Android

> TODO Describe what this App provides

*Table of Contents*
- [it.bz.noi.community.android](#itbznoicommunityandroid)
	- [Getting started](#getting-started)
		- [Prerequisites](#prerequisites)
		- [Source code](#source-code)
		- [Configure the project](#configure-the-project)
	- [Running tests](#running-tests)
	- [Deployment](#deployment)
	- [Information](#information)
		- [Support](#support)
		- [Contributing](#contributing)
		- [Documentation](#documentation)
		- [License](#license)

## Getting started

These instructions will get you a copy of the project up and running
on your local machine for development and testing purposes.

### Prerequisites

To build the project, the following prerequisites must be met:

1. [Android Studio](https://developer.android.com/studio) is strongly recommended,
   even if any other IDE that supports Android projects should be fine


### Source code

Get a copy of the repository:

```bash
git clone git@github.com:noi-techpark/it.bz.noi.community.android.git
```

### Configure the project

No configuration is needed.

## Running tests

The unit tests can be executed with the following command launched from the project folder:

```bash
./gradlew clean
./gradlew test
```

*More information about Android tests at https://developer.android.com/studio/test/command-line*

## Deployment

We deployment the application with Github Actions to the Internal Test Track, if
someone pushes to the `development` branch, and prefixes the commit message with
`[SNAPSHOT]`. See [.github/workflows] for details...

A detailed description on how to release the application to the store, will
follow when we do that the first time.

## Information

### Support

For support, please contact [help@opendatahub.bz.it](mailto:help@opendatahub.bz.it).

### Contributing

If you'd like to contribute, please follow the following instructions:

- Fork the repository.

- Checkout a topic branch from the `development` branch.

- Make sure the tests are passing.

- Create a pull request against the `development` branch.

### Documentation

More documentation can be found at

> TODO

### License

The code in this project is licensed under the GNU GENERAL PUBLIC LICENSE 3.0 or later license.
See the LICENSE file for more information.
