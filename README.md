# Docker and Spring boot based integration testing library.
You can use this framework to perform integration testing of docker containers.
 
The library uses spring features to support dependencies of other containers. and
making sure all dependencies have been meet before running the tests.

* [Why](#why)
* [Features](#features)
	* [Big Bang testing](#big-bank-testing)
	* [Top-down and Bottom-up](#top-down-and-bottom-up)
		* [Bottom-up testing](#bottom-up-testing)
		* [Top-down testing](#top-down-testing)
		* [Sandwich testing](#sandwich-testing)
	* [Docker support](#docker-support)
	* [Dependency Awareness](#dependency-awareness)
* [Getting Started](#getting-started)
	* [Setting up your project](#setting-up-your-project)
		* [Creating the container](#creating-the-container)
		* [Setup the Spring Boot Application Context](#setup-the-spring-boot-application-context)
		* [Create Spring Boot Starter](#create-spring-boot-starter)
		* [First run](#first-run)
* [Creating your first tests](#creating-your-first-tests)
* [Using Dependencies](#using-dependencies) 

## Why?
Let's say you want to do integration testing of you software. You have most of
your code covered by unit tests, however this does not test your software running
in the target environment, working with all the components.

This project tries to solve this problem, a problem which some times can be quite
time consuming, we do this by leveraging Spring boot and Docker.

## Features
 - Top-down and Bottom-up integration testing (including Sandwich Testing)
 - Big Bang integration testing
 - Docker support
 - Dependency awareness

### Big Bang testing
The big bang method is the fastest, however not the best way. The way this works
is basically start the entire software stack and try to perform some actions. 
This means that if a error occurs somewhere below the top level, you will have a
hard time figuring out where the error occurred. Therefore it is recommended to
apply the Top-down and Bottom-up approaches.
 
### Top-down and Bottom-up
#### Bottom-up testing
Bottom-up testing is a bit like unit testing, however the tests is run on your
software. An example could be to test when your application receives a certain
message it returns the correct response. The bottom-up tests are just like unit
tests, shared with other components.

#### Top-down testing
Top-down testing allows you to test your software and find if any components in 
your software stack is missing.

#### Sandwich testing
Sandwich testing is an approach which includes both top-down and bottom-up.

### Docker support
All components of your software stack must be run via docker, this makes it much
easier and faster to setup each part of your software stack. And you can use the
same docker images in your production environment.

### Dependency Awareness
When you are doing integration testing you might not always know which database
and which tables to create before you are able to use some underlying piece of
the software stack. By using dependency awareness, you no longer need to know
the entire stack, all you need to know is that you want to talk to component X
and include that in the test. Component X is responsible for knowing about its
dependencies.

## Getting Started
Before you get started it is important to mention that it is always a very good
idea to keep you integration tests separated from your main source branch. This
is because all your software must be built before running your integration tests.

For ease of use the Archetype for a simple docker integration test module
can be used using maven:

	mvn archetype:generate -DarchetypeArtifactId=docker-integration-archetype -DarchetypeGroupId=dk.sublife.docker-integration

### Setting up your project
First start by creating a new maven project, and add the docker-integration 
dependency:

	<dependency>
		<groupId>dk.sublife.docker-integration</groupId>
		<artifactId>docker-integration</artifactId>
		<version>1.0.3</version>
	</dependency>

Now you must create a basic Spring boot application along with some basic classes
required to run integration tests and make other parts of your software stack aware
of your tests and software.

#### Creating the container
The container is a class which implements the `dk.sublife.docker.integration.Container`
This is the class that will describe how to start the docker container you want to
test, and how to test if the running container can be considered as "up and running"

#### Setup the Spring Boot Application Context
Next you need to setup a simple spring application. this application is used when
running you tests, the application created all the beans necessary to run the tests.
the Spring boot application is need to make your integration test dependency aware
of any dependencies you have.

#### Create Spring Boot Starter
To test and make other parts of the stack aware of your software you must create
a Spring Boot starter `Configuration` class. This is a simple Spring class which
is annotated with `@Configuration` and it should as a minimum expose the `Container`
class as a bean.
Next create a file in `resources/META-INF/spring.factories` and add the following
line (replacing the `package.StarterClass` with the package and name of your class):

	org.springframework.boot.autoconfigure.EnableAutoConfiguration=package.StarterClass

For more information about the spring.factories file please consult the [Spring
boot documentation: Developing auto-configuration and using conditions]
(http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-auto-configuration.html)

#### First run
To test if everything is working as intended, run the Spring boot Application, check
the logs and see if the container is being started.

For inspiration take a look in the example module, where you can find a simple 
integration test for two versions of a MySQL server running in a docker container.

## Creating your first tests
To create your first test we must first have a test class, since this is a 
integration test, it is recommended to use the `maven-failsafe-plugin` which
runs test classes containing `IT` in its name. When the test class have been
created, add the following annotations to the class:
 
	@RunWith(SpringJUnit4ClassRunner.class)
	@SpringApplicationConfiguration(classes = { MyExampleApplicationClass.class })
	@DirtiesContext

The first two annotation `@RunWith` and `SpringApplicationConfiguration` tells 
junit that we want to run the tests using Spring, and load the Application class
into that spring context.

The last `@DirtiesContext` tells Spring, that when all tests in the class have 
been executed, shutdown the spring context. when shutting down the Spring context
we can guarantee that all docker containers are shutdown and removed from the system

## Using dependencies
Great we have a couple of integration test modules, a couple of [Bottom-up tests]
(#bottom-up-testing) etc. Now it is time to combine it into [Top-down tests]
(#top-down-testing).

When all tests are passing, and the individual components are installed and or
deployed to a maven repository, all you need is to add the integration test
modules, you would like to combine, to your dependency list. Then you can create
new tests where you can access and combine each component. To access the container
information for retrieving addresses and such, simply just use Spring's `@Autowire`
annotation, and you can access all utility methods of the container.
