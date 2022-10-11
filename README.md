# java-aci-api-ng

A Java API for communicating with Micro Focus ACI servers.

[![Build Status](https://travis-ci.org/microfocus-idol/java-aci-api-ng.svg?branch=master)](https://travis-ci.org/microfocus-idol/java-aci-api-ng)

## About
The Micro Focus Content Infrastructure (ACI) is a protocol for communicating with Micro Focus servers using XML over HTTP.
This Java API provides an interface for constructing and executing ACI requests and for consuming ACI responses. Previous
versions were available as jar files from Micro Focus customer support.


## Usage
java-aci-api-ng is available from the central Maven repository.

    <dependency>
        <groupId>com.hp.autonomy.aci.client</groupId>
        <artifactId>aci-api</artifactId>
        <version>12.13.0</version>
    </dependency>

For more documentation, see the project homepage [here](http://microfocus-idol.github.io/java-aci-api-ng).

## Migration to FOSS Version
Previous versions of this library included a taglib for use in JSPs and support for legacy OEM encryption. Builds with
these features are available from Micro Focus customer support.

## Contributing
We welcome pull requests. These must be licensed under the MIT license. Please submit pull requests to the develop
branch - the master branch is for stable code only.

## Is it any good?
Yes.

## License

(c) Copyright 2006-2020 Micro Focus or one of its affiliates.

Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.
