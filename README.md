# BTP Environment for Java

Utility for easily reading application configurations for bound services in the SAP Business Technology Platform Cloud Foundry and Kubernetes (K8S) environment.


## Cloud Foundry Specifics

Cloud Foundry provides application configurations via environment variables. In the Cloud Foundry environment, [VCAP_SERVICES](http://docs.cloudfoundry.org/devguide/deploy-apps/environment-variable.html#VCAP-SERVICES) environment variable holds the configuration properties of the bound services.

#### Service Binding

In Cloud Foundry you bind a service instance to your application either via a deployment descriptor or with a command like this:

```sh
cf bind-service <app-name> <service-name>
```

## Kubernetes Specifics

Kubernetes offers several ways of handling application configurations for bound services and certificates. BTP Environment client library for Java expects that such configuration is handled as Kubernetes Secrets and mounted as files to the pod at a specific
path. This path can be provided by the application developer, but the default is `/etc/secrets/sapbtp`. From there, BTP Environment client library for Java  assumes that the directory structure is the
following `/etc/secrets/sapbtp/<service-name>/<instance-name>`. Here `<service-name>` and `<instance-name>` are both
directories and the latter contains the credentials/configurations for the service instance as
files. [SAP BTP service operator](https://github.com/SAP/sap-btp-service-operator) supports several ways 
on how secret files are organized.

For example, the below folder structure resembles two instances of service `xsuaa`, `application` and `broker`, each with their own configurations and one instance of service `servicemanager` called `my-instance` with its configuration.

```sh
/etc/
    /secrets/
            /sapbtp/
                 /xsuaa/
                 |    /application/
                 |    /broker/
                 /servicemanager/
                       /my-instance/
```

#### Service Binding

In Kubernetes you can create and bind to a service instance using the SAP BTP Service Operator as
described [here](https://github.com/SAP/sap-btp-service-operator#using-the-sap-btp-service-operator).

Upon creation of the binding, the Service Catalog will create a Kubernetes secret (by default with the same name as the binding) containing credentials, configurations and certificates. This secret can then be mounted to the pod as a volume.

The following *deployment.yml* file shows how a `xsuaa` service instance and binding `xsuaa-service-binding` 
is created with Service Catalog:

```yml
...
   spec:
     containers:
       - name: app
         image: app-image:1.0.0
         ports:
           - name: http
             containerPort: 8080
         volumeMounts:
           - name: xsuaa
             mountPath: "/etc/secrets/sapbtp/xsuaa/application"
             readOnly: true
     volumes:
       - name: xsuaa
         secret:
           secretName: xsuaa-service-binding
```

Of course, you can also create Kubernetes secrets directly with `kubectl` and mount them to the pod. As long as the mount path follows the `<root-path>/<service-name>/<instance-name>` pattern, btp-env-java library will be able to discover the bound services configurations.

**Note**: The library attempts to parse property values which are either stored flat, i.e. metadata and credential properties are located next to each other (DataParsingStrategy):
```
/etc/
    /secrets/
            /sapbtp/
                 /some-service/
                       /some-instance/
                                  /clientid
                                  /plan
                                  /tags
                                  /url
```

Or, alternatively metadata is stored flat, but the credential properties are stored in one json file (SecretKeyParsingStrategy):

```
/etc/
    /secrets/
            /sapbtp/
                 /some-service/
                       /some-instance/
                                  /credentials.json
                                  /plan
                                  /tags
```

Or, similar to Cloud Foundry ```VCAP_SERVICES``` metadata as well as the credential properties are stored in one json file (SecretRootKeyParsingStrategy):
```
/etc/
    /secrets/
            /sapbtp/
                 /some-service/
                       /some-instance/
                                  /binding.json
```

In all above cases, the service credentials are accessible to the application like that:

```java
ServiceBindingAccessor accessor = DefaultServiceBindingAccessor.getInstance();
ServiceBinding binding = getServiceBindings()
                            .stream()
                            .filter(b -> "some-instance".equals(b.getName().orElse(null)))
                            .collect(Collectors.toList()).get(0); // assumes there is one service binding found
String plan = binding.getServicePlan().orElse(null);
Map<String, Object> credentials = binding.getCredentials();
```


## Requirements and Setup

### Requirements
- Java 8 or 11
- Maven 3.8.1 or later

### Maven Dependencies

Include the following dependency in your ``pom.xml`` to consume service bindings created by the [SAP BTP Service Operator](https://github.com/SAP/sap-btp-service-operator) (K8s):

```xml
<dependency>
    <groupId>com.sap.cloud.environment.servicebinding</groupId>
    <artifactId>java-sap-service-operator</artifactId>
    <version>${sap.cloud.env.servicebinding.version}</version>
</dependency>
<dependency> <!-- optional if using TypedMapView etc. -->
    <groupId>com.sap.cloud.environment.api</groupId>
    <artifactId>java-consumption-api</artifactId>
    <version>${sap.cloud.env.servicebinding.version}</version>
</dependency>
```

### Service Lookup via Name

```java
ServiceBinding bindings = DefaultServiceBindingAccessor.getInstance().getServiceBindings().stream()
                                .filter(b -> "some-instance".equals(b.getName().orElse(null)))
                                .collect(Collectors.toList()).get(0); // assumes there is one service binding found 
```

### Service Lookup via Service Name and Plan

```java
ServiceBindingAccessor accessor = DefaultServiceBindingAccessor.getInstance();

List<ServiceBinding> bindings = accessor.getServiceBindings().stream()
                .filter(b -> "identity".equalsIgnoreCase(b.getServiceName().orElse(null))
                       && "application".equalsIgnoreCase(b.getServicePlan().orElse(null)))
                .collect(Collectors.toList()); // There should never be two

if (bindings.isEmpty()) {
    throw new IllegalStateException("There is no binding of service 'identity' and plan 'application'");
} else if (bindings.size() > 1) {
    throw new IllegalStateException("Found multiple bindings of service 'identity' and plan 'application'");
}
```

### Service Lookup via Tag / Label

Here is how you can get this service configuration in your Java application if you don't know the instance name in advance:

```java
ServiceBindingAccessor accessor = DefaultServiceBindingAccessor.getInstance();

List<ServiceBinding> bindings = accessor.getServiceBindings().stream()
                .filter(b -> b.getTags().contains("myTag"))
                .collect(Collectors.toList());

bindings = accessor.getServiceBindings().stream()
                .filter(b -> "myLabel".equalsIgnoreCase(b.get("label").orElse(null)))
                .collect(Collectors.toList());
```

This example finds a service binding with `myTag` in the tags or with `myLabel` label.

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
that [User-Provided Service Instances](https://docs.cloudfoundry.org/devguide/services/user-provided.html) have less
properties than managed service instances and no tags.

## Support, Feedback, Contributing

This project is open to feature requests/suggestions, bug reports etc. via [GitHub issues](https://github.com/SAP/btp-environment-variable-access/issues). Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](CONTRIBUTING.md).

## Code of Conduct

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone. By participating in this project, you agree to abide by its [Code of Conduct](CODE_OF_CONDUCT.md) at all times.

## Licensing

Copyright 2022 SAP SE or an SAP affiliate company and BTP Environment for Java contributors. Please see our [LICENSE](LICENSE) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available [via the REUSE tool](https://api.reuse.software/info/github.com/SAP/btp-environment-variable-access).

## Further References

- Specification<br>
  https://github.com/openservicebrokerapi/servicebroker/blob/master/spec.md#body-11
- API / Java Doc<br>
  TODO

