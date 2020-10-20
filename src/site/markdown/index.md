# java-aci-api-ng

This is a Java API is for communicating with Micro Focus ACI servers.

## About
The Autonomy Content Infrastructure (ACI) is a protocol for communicating with Micro Focus servers using XML over HTTP.
This Java API provides an interface for constructing and executing ACI requests and for consuming ACI responses. Previous
versions were available as jar files from customer support.

## Usage
java-aci-api-ng is available from the central Maven repository.

    <dependency>
        <groupId>com.hp.autonomy.aci.client</groupId>
        <artifactId>aci-api</artifactId>
        <version>5.0.0</version>
    </dependency>

The AciService's executeAction methods are responsible for executing ACI actions. An AciServiceImpl can either be created
with the host and port for an ACI server, or the executeAction method must be called with a host and port each time.

    AciService aciService = new AciServiceImpl(
        new AciHttpClientImpl(new DefaultHttpClient()),
        new AciServerDetails(host, port)
    );

The AciParameters class can be used to construct a request:

    AciParameters parameters = new AciParameters("QUERY");
    parameters.add("text", "*");
    parameters.add("totalResults", true);

To execute this request, the AciService executeAction method should be called with the AciParameters and a Processor to
consume the response. In the following example, the included DocumentProcessor is used to convert the XML response into a DOM
document which can then be inspected either directly or using XPath. 

    Document response = aciService.executeAction(parameters, new DocumentProcessor());
    XPath xpath = XPathFactory.newInstance().newXPath();
    Number totalResults = (Number) xpath.evaluate("/autnresponse/responsedata/autn:numhits", response, XPathConstants.NUMBER); 

Preferably, the [ACI Annotations Processor Factory](http://microfocus-idol.github.io/java-aci-annotations-processor) can be used to
easily convert the response to an instance of a Java class defined by you.

## Migration to FOSS Version
Previous versions of this library included a taglib for use in JSPs and support for legacy OEM encryption. Builds with
these features are available from customer support.

## Contributing
We welcome pull requests. These must be licensed under the MIT license. Please submit pull requests to the develop
branch - the master branch is for stable code only.

## Is it any good?
Yes.

## License
Copyright 2006-2018 Micro Focus International plc.

Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.
