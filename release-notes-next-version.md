## Module: `java-consumption-api`

* `TypedMapView#getEntries` now also returns all entries that are subtypes (`Class#isAssignableFrom`) of the queried entry type.
* `TypedListView#getItems` now also returns all items that are subtypes (`Class#isAssignableFrom`) of the queried item type.

## Module: `java-sap-service-operator`

* **Breaking Changes:**
  * Following classes have been removed from the public API:
    * `com.sap.cloud.environment.servicebinding.metadata.BindingMetadata`
    * `com.sap.cloud.environment.servicebinding.metadata.BindingMetadataFactory`
    * `com.sap.cloud.environment.servicebinding.metadata.BindingProperty`
    * `com.sap.cloud.environment.servicebinding.metadata.PropertyFormat`
