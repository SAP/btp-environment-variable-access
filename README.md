# BTP Environment for Java
Utility for easily reading application configurations for bound services and certificates in the SAP Cloud Platform Cloud Foundry and Kubernetes (K8S) environment.

## Cloud Foundry Specifics
Cloud Foundry provides application configurations via environment variables.
The properties of the bound services are in [VCAP_SERVICES](http://docs.cloudfoundry.org/devguide/deploy-apps/environment-variable.html#VCAP-SERVICES) environment variable.

### Service Binding
In Cloud Foundry you bind a service instance to your application either via a deployment descriptor or with a command like this:
```sh
cf bind-service <app-name> <service-name>
```

## Kubernetes Specifics
Kubernetes offers several ways of handling application configurations for bound services and certificates. btp-env-java library expects that such configurations are handled as Kubernetes Secrets and mounted as files to the pod at a specific path. This path can be provided by the application developer, but the default is `/etc/secrets/sapbtp`. From there, btp-env-java library assumes that the directory structure is the following `/etc/secrets/sapcp/<service-name>/<instance-name>`. Here `<service-name>` and `<instance-name>` are both directories and the latter contains the credentials/configurations for the service instance as files. [SAP BTP service operator](https://github.com/SAP/sap-btp-service-operator) supports several ways on how files are organized.

For example, the following folder structure:
```sh

/etc/
    /secrets/
            /sapbtp/
                 /xsuaa/
                 |    /application/
                 |    |          /clientid
                 |    |          /certificate
                 |    /broker/
                 |    |          /clientid
                 |    |          /certificate
                 /servicemanager/
                       /sm-instance/
                                  /credentials
```
resembles two instances of service `xsuaa` - `application` and `broker` each with their own configurations and one instance of service `servicemanager` called `sm-instance` with its configurations.

### Service Binding
In Kubernetes you can create and bind to a service instance using the SAP BTP Service Operator as described [here](https://github.com/SAP/sap-btp-service-operator#using-the-sap-btp-service-operator).

Upon creation of the binding, the Service Catalog will create a Kubernetes secret (by default with the same name as the binding) containing credentials, configurations and certificates. This secret can then be mounted to the pod as a volume.

The following *deployment.yml* file would generate the file structure above, assuming we have bindings `hanaBind1`, `hanaBind2` and `xsuaaBind` for service instances `hanaInst1`, `hanaInst2` and `xsuaaInst` created with Service Catalog:
```sh
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

Of course, you can also create Kubernetes secrets directly with `kubectl` and  mount them to the pod. As long as the mount path follows the `<root-path>/<service-name>/<instance-name>` pattern, btp-env-java library will be able to discover the bound services configurations.

**Note**: The library attempts to parse property values which represent valid JSON objects.<br>
** TODO any limitations? Can we parse arrays properly?

The following service credentials:

```
/etc/
    /secrets/
            /sapcp/
                 /some-service/
                       /some-instance/
                                  /url   - containing https://some-service
                                  /uaa   - containing { "url": "https://uaa", "clientid": "client", "clientsecret": "secret" }
                                  /other - containing [1, "two"]
```

Will be available to the application as:

```
TODO
```


## Usage

### Service Lookup via Tag/Label
Here is how you can get this service configuration in your Java application if you don't know the instance name in advance:
```java

```

You can look up services based on their metadata:
```java

```
This example finds a service binding with `xyz` in the tags or label in case of Kubernetes.

> **K8s Hint**
> If you have mounted your secrets to a different path, ...

### User-Provided Service Instances
While this package can look up any kind of bound service instances, you should be aware that [User-Provided Service Instances](https://docs.cloudfoundry.org/devguide/services/user-provided.html) have less properties than managed service instances and no tags.


## Local Usage

TODO

## API

TODO

## Contributors
- SAP Cloud SDK (Johannes)
  - **johannes.schneider03@sap.com**
  - christoph.schubert@sap.com
  - ? alexander.duemont@sap.com
  - ? matthias.kuhr@sap.com
  - ? artem.kovalov@sap.com

- SAP Cloud Security
  - liga.ozolina@sap.com
  - nena.raab@sap.com

## Stakeholders
- marc.becker@sap.com
- matthia.braun@sap.com
- frank.stephan@sap.com

## Specifications
- https://github.com/openservicebrokerapi/servicebroker/blob/master/spec.md#body-11

## API / Java Doc
