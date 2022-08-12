## Module: `java-access-api`

* **Breaking Changes:**
  * Removed the default constructor of `com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException`.

## Module: `java-consumption-api`

* `TypedMapView#getEntries` now also returns all entries that are subtypes (`Class#isAssignableFrom`) of the queried entry type.
* `TypedListView#getItems` now also returns all items that are subtypes (`Class#isAssignableFrom`) of the queried item type.
* **Breaking Changes:**
  * Removed the default constructor of `com.sap.cloud.environment.servicebinding.api.exception.ValueCastException`.
  * Removed the default constructor of `com.sap.cloud.environment.servicebinding.api.exception.KeyNotFoundException`.

## Module: `java-sap-service-operator`

* **Breaking Changes:**
  * Following classes have been removed from the public API:
    * `com.sap.cloud.environment.servicebinding.metadata.BindingMetadata`
    * `com.sap.cloud.environment.servicebinding.metadata.BindingMetadataFactory`
    * `com.sap.cloud.environment.servicebinding.metadata.BindingProperty`
    * `com.sap.cloud.environment.servicebinding.metadata.PropertyFormat`
