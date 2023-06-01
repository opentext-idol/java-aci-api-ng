# java-aci-api-ng

A Java API for communicating with OpenText ACI servers.

## About
The OpenText Content Infrastructure (ACI) is a protocol for communicating with OpenText servers using XML over HTTP.
This Java API provides an interface for constructing and executing ACI requests and for consuming ACI responses. Previous
versions were available as jar files from OpenText customer support.


## Usage
java-aci-api-ng is available from the central Maven repository.

    <dependency>
        <groupId>com.hp.autonomy.aci.client</groupId>
        <artifactId>aci-api</artifactId>
        <version>23.3.0</version>
    </dependency>

For more documentation, see the project homepage [here](http://opentext-idol.github.io/java-aci-api-ng).

## Migration to FOSS Version
Previous versions of this library included a taglib for use in JSPs and support for legacy OEM encryption. Builds with
these features are available from OpenText customer support.

## Contributing
We welcome pull requests. These must be licensed under the MIT license. Please submit pull requests to the develop
branch - the master branch is for stable code only.

## Is it any good?
Yes.

## License

Copyright 2006-2020 OpenText or one of its affiliates.

Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.
