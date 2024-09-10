[![Maven Central](https://img.shields.io/badge/Maven_Central-yellow.svg)](https://search.maven.org/search?q=g:com.sap.cloud.environment.servicebinding%2BAND%2Ba:java-bom)
[![REUSE status](https://api.reuse.software/badge/github.com/SAP/btp-environment-variable-access)](https://api.reuse.software/info/github.com/SAP/btp-environment-variable-access)
[![Java CI with Maven](https://github.com/SAP/btp-environment-variable-access/actions/workflows/ci-build.yml/badge.svg)](https://github.com/SAP/btp-environment-variable-access/actions/workflows/ci-build.yml)
[![Fosstars security rating](https://raw.githubusercontent.com/SAP/btp-environment-variable-access/fosstars-report/fosstars_badge.svg)](https://github.com/SAP/btp-environment-variable-access/blob/fosstars-report/fosstars_report.md)

# BTP Environment Service Binding Access for Java

Utility for easily reading application configurations for bound services in the SAP Business Technology Platform Cloud
Foundry and Kubernetes (K8S) environment.

## Requirements

- Java `≥ 8`
- Maven `≥ 3.8.1`

### Building the Project

Building the project is as simple as running the following command:

```sh
mvn clean install -Dgpg.skip -DskipCveCheck
```

The command above skips both signing the artifacts and performing vulnerability checks.
If you like to run the vulnerability checks (which might take a while, as it downloads the CVE database), you may use the following command instead:

```sh
mvn clean install -Dgpg.skip -DupdateCveDatabase
```

## Usage

### Parent POM

Add one of the following dependencies to the `<dependencyManagement>` section of your _parent_ `pom.xml`:

```xml
<dependency>
    <groupId>com.sap.cloud.environment.servicebinding</groupId>
    <artifactId>java-bom</artifactId>
    <version>LATEST-VERSION</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

<details>
<summary>Alternative: Minimum Version Management</summary>

```xml
<dependency>
    <groupId>com.sap.cloud.environment.servicebinding</groupId>
    <artifactId>java-modules-bom</artifactId>
    <version>LATEST-VERSION</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

</details>

### Application POM

Add following dependencies to the `<dependenices>` section of your _application_ `pom.xml`:

```xml
<!-- Include following dependency if your application uses the VCAP_SERVICES environment variable as a source for service bindings -->
<dependency>
    <groupId>com.sap.cloud.environment.servicebinding</groupId>
    <artifactId>java-sap-vcap-services</artifactId>
</dependency>

<!-- Include following dependency if your application uses mounted service bindings created by the SAP BTP Service Operator -->
<dependency>
    <groupId>com.sap.cloud.environment.servicebinding</groupId>
    <artifactId>java-sap-service-operator</artifactId>
</dependency>
```

### Quick Start

You can use following code to conveniently access and filter all available service bindings:

```java
// read all service bindings
List<ServiceBinding> allServiceBindings = DefaultServiceBindingAccessor.getInstance().getServiceBindings();

// filter for a specific binding
ServiceBinding xsuaaBinding = allServiceBindings.stream()
        .filter(binding -> "xsuaa".equalsIgnoreCase(binding.getServiceName().orElse(null)))
        .filter(binding -> "lite".equalsIgnoreCase(binding.getServicePlan().orElse(null)))
        .filter(binding -> binding.getTags().contains("tag"))
        .findFirst()
        .get();
```

## Cloud Foundry Specifics

Cloud Foundry provides application configurations via environment variables. In the Cloud Foundry
environment, [VCAP_SERVICES](http://docs.cloudfoundry.org/devguide/deploy-apps/environment-variable.html#VCAP-SERVICES)
environment variable holds the configuration properties of the bound services.

#### Service Binding

In Cloud Foundry you bind a service instance to your application either via a deployment descriptor or with a command
like this:

```sh
cf bind-service <app-name> <service-name>
```

## Kubernetes Specifics

Kubernetes offers several ways of handling application configurations for bound services and certificates. The BTP
Service Binding client library for Java is capable of handling service bindings that conform to
the [servicebinding.io](https://servicebinding.io/spec/core/1.0.0/) specification (version `1.0.0`) with
the [SAP metadata extension](https://blogs.sap.com/2022/07/12/the-new-way-to-consume-service-bindings-on-kyma-runtime/).
To conform to the specification, following requirements _must_ be met:

1. There must be an environment variable named `SERVICE_BINDING_ROOT` that points to a local directory, which must be
   accessible for the application.
2. Each service binding must be contained within its own directory. These directories must be contained in
   the `SERVICE_BINDING_ROOT`.
3. Each service binding must define a `.metadata` file, which provides structural information about the properties of
   the binding.

<details>
<summary>Service Binding Example</summary>

For example, a valid binding could look like below:

```sh
/$SERVICE_BINDING_ROOT/
  my-funny-binding/
    .metadata: /* see below */
    type: funny-service
    tags: ["funny", "somewhat", "useful"]
    user: me
    a_number: 3
    a_boolean: true
    an_object: { "property1": "value 1", "property2": true }
```

With the `.metadata` file containing following content:

```json
{
  "metaDataProperties": [
    {
      "name": "type",
      "format": "text"
    },
    {
      "name": "tags",
      "format": "json"
    }
  ],
  "credentialProperties": [
    {
      "name": "user",
      "format": "text"
    },
    {
      "name": "a_number",
      "format": "json"
    },
    {
      "name": "a_boolean",
      "format": "json"
    },
    {
      "name": "an_object",
      "format": "json"
    }
  ]
}
```

</details>

Such bindings can be created using the [SAP BTP service operator](https://github.com/SAP/sap-btp-service-operator).

#### Mounting Service Bindings

In Kubernetes you can create and bind to a service instance using the SAP BTP Service Operator as
described [here](https://github.com/SAP/sap-btp-service-operator#using-the-sap-btp-service-operator).

Upon creation of the binding, a Kubernetes secret (by default with the same name as the binding) is created containing
credentials, configurations and certificates. This secret can then be mounted to the pod as a volume.

The following extract from _deployment.yml_ file shows how the secret of a `xsuaa` service instance
binding `xsuaa-service-binding` is mounted as volume to an application container:

```yml
...
spec:
  containers:
    - name: app
      image: app-image:1.0.0
      env:
        - name: SERVICE_BINDING_ROOT
          value: "/bindings/"
      ports:
        - name: http
          containerPort: 8080
      volumeMounts:
        - name: authn
          mountPath: "/bindings/auth"
          readOnly: true
  volumes:
    - name: authn
      secret:
        secretName: xsuaa-service-binding
```

<details>
<summary>Legacy Bindings</summary>

This library also supports older versions of the SAP BTP Service operator (version `< 0.2.3`).
Bindings created by the older operator do not have the `.metadata` file and are **required** to be mounted to a specific
path within the pod: `/etc/secrets/sapbtp/<service-name>/<instance-name>`.

For example, the below folder structure resembles two instances of service `xsuaa`: One named `application` and the
other named `broker`, each with their own configurations. Additionally, there is one instance of
service `servicemanager` called `my-instance` with its configuration.

```sh
/etc/secrets/sapbtp
  /xsuaa
    /application/
    /broker/
  /servicemanager
    /my-instance/
```

Bindings within these directories may have different structures. This library is capable of parsing the following
structures:

1. "Flat Properties"

```sh
/etc/secrets/sapbtp
  /some-service
    /some-instance
      /clientid
      /plan
      /tags
      /url
```

2. "Flat Metadata and JSON Credentials"

```sh
/etc/secrets/sapbtp
  /some-service
    /some-instance
      /credentials.json
      /plan
      /tags
```

3. "Full JSON Binding"

```sh
/etc/secrets/sapbtp
  /some-service
    /some-instance
      /binding.json
```

</details>

### Read Service Binding Properties

Given that ``credentials.json`` consists of

```json
{
  "url": "https://mydomain.com",
  "domains": [
    "mydomain-1",
    "mydomain-2"
  ]
}
```

You can access the ``url`` property as follows:

```java
String url = (String)credentials.get("url");
```

Or, alternatively:

```java
TypedMapView credentialsTyped = TypedMapView.ofCredentials(binding);
String url = credentialsTyped.getString("url");
```

The access of ``domains`` looks like:

```java
List<String> domainsList = credentialsTyped.getListView("domains").getItems(String.class);
```

### User-Provided Service Instances

While this package can look up any kind of bound service instances, you should be aware
that [User-Provided Service Instances](https://docs.cloudfoundry.org/devguide/services/user-provided.html) have fewer
properties than managed service instances and no tags.

## Support, Feedback, Contributing

This project is open to feature requests/suggestions, bug reports etc.
via [GitHub issues](https://github.com/SAP/btp-environment-variable-access/issues). Contribution and feedback are
encouraged and always welcome. For more information about how to contribute, the project structure, as well as
additional contribution information, see our [Contribution Guidelines](CONTRIBUTING.md).

## Code of Conduct

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for
everyone. By participating in this project, you agree to abide by its [Code of Conduct](CODE_OF_CONDUCT.md) at all
times.

## Licensing

Copyright 2022 SAP SE or an SAP affiliate company and BTP Environment for Java contributors. Please see
our [LICENSE](LICENSE) for copyright and license information. Detailed information including third-party components and
their licensing/copyright information is
available [via the REUSE tool](https://api.reuse.software/info/github.com/SAP/btp-environment-variable-access).

## Further References

- Maven Central<br>
  https://search.maven.org/search?q=g:com.sap.cloud.environment.servicebinding
- Specification<br>
  https://github.com/openservicebrokerapi/servicebroker/blob/master/spec.md#body-11

