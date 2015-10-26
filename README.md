# docker-integration
Docker and Springboot based integration testing library.

* [Why](#why)
* [Setup](#setup)
* [Usage](#usage)

## Why?
You can use this framework to perform integration testing of docker containers.
 
The library uses spring features to support dependencies of other containers. and
making sure all dependencies have been meet before running the tests.

## Setup
Add library to your project:

	<dependency>
		<groupId>dk.sublife.docker-integration</groupId>
		<artifactId>docker-integration</artifactId>
		<version>1.0.2-SNAPSHOT</version>
	</dependency>

## Usage

	-