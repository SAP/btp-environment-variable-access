from pathlib import Path
from unittest import TestCase

from maven.maven_module_identifier import MavenModuleIdentifier
from maven.maven_property import MavenProperty
from maven.xml_maven_module import XmlMavenModule
from maven.xml_maven_module_reader import XmlMavenModuleReader
from maven.xml_maven_project import XmlMavenProject


class TestXmlMavenProject(TestCase):
    RESOURCES: Path = Path(Path(__file__).parent, "resources", Path(__file__).stem)

    def test_bump_version_with_single_module(self) -> None:
        module: XmlMavenModule = XmlMavenModuleReader().read(
            Path(self.RESOURCES, "single_module", "pom.xml")
        )

        sut: XmlMavenProject = XmlMavenProject()
        sut.add_modules(module)

        # sanity check
        self.assertEqual(module.identifier.version, "${this.version}")

        self.assertEqual(len(module.properties), 2)
        self.assertPropertyValue(module.properties, "this.version", "13.3.7")
        self.assertPropertyValue(module.properties, "dependency.version", "42.9.9")

        self.assertIsNotNone(module.parent_identifier)
        self.assertEqual(module.parent_identifier.version, "1.1.1")

        self.assertEqual(len(module.dependencies), 3)
        self.assertDependencyVersion(
            module.dependencies, "com.example", "dpm-with-version", "0.42.0"
        )
        self.assertDependencyVersion(
            module.dependencies,
            "com.example",
            "dpm-with-property-version",
            "${dependency.version}",
        )
        self.assertDependencyVersion(
            module.dependencies, "com.example", "dp-with-version", "0.1.0"
        )

        # bump version
        sut.bump_version(XmlMavenProject.VersionBumpType.PATCH, write_modules=False)

        self.assertEqual(module.identifier.version, "${this.version}")

        self.assertEqual(len(module.properties), 2)
        self.assertPropertyValue(module.properties, "this.version", "13.3.8")
        self.assertPropertyValue(module.properties, "dependency.version", "42.9.9")

        self.assertIsNotNone(module.parent_identifier)
        self.assertEqual(module.parent_identifier.version, "1.1.1")

        self.assertEqual(len(module.dependencies), 3)
        self.assertDependencyVersion(
            module.dependencies, "com.example", "dpm-with-version", "0.42.0"
        )
        self.assertDependencyVersion(
            module.dependencies,
            "com.example",
            "dpm-with-property-version",
            "${dependency.version}",
        )
        self.assertDependencyVersion(
            module.dependencies, "com.example", "dp-with-version", "0.1.0"
        )

        # set custom version
        sut.bump_version(XmlMavenProject.VersionBumpType.MINOR, "1.2.3", write_modules=False)

        self.assertEqual(module.identifier.version, "${this.version}")

        self.assertEqual(len(module.properties), 2)
        self.assertPropertyValue(module.properties, "this.version", "1.2.3")
        self.assertPropertyValue(module.properties, "dependency.version", "42.9.9")

        self.assertIsNotNone(module.parent_identifier)
        self.assertEqual(module.parent_identifier.version, "1.1.1")

        self.assertEqual(len(module.dependencies), 3)
        self.assertDependencyVersion(
            module.dependencies, "com.example", "dpm-with-version", "0.42.0"
        )
        self.assertDependencyVersion(
            module.dependencies,
            "com.example",
            "dpm-with-property-version",
            "${dependency.version}",
        )
        self.assertDependencyVersion(
            module.dependencies, "com.example", "dp-with-version", "0.1.0"
        )

    def test_bump_version_with_multi_module(self) -> None:
        modules: list[XmlMavenModule] = XmlMavenModuleReader().read_recursive(
            Path(self.RESOURCES, "multi_module", "pom.xml")
        )

        sut: XmlMavenProject = XmlMavenProject()
        sut.add_modules(*modules)

        # sanity check
        parent_module: XmlMavenModule = [
            module
            for module in modules
            if module.identifier.artifact_id == "application"
        ][0]

        self.assertEqual(parent_module.identifier.version, "13.3.7")
        self.assertIsNotNone(parent_module.parent_identifier)
        self.assertEqual(parent_module.parent_identifier.version, "1.1.1")

        child_module: XmlMavenModule = [
            module
            for module in modules
            if module.identifier.artifact_id == "sub-module"
        ][0]

        self.assertEqual(child_module.identifier.version, "13.3.7")
        self.assertIsNotNone(child_module.parent_identifier)
        self.assertEqual(child_module.parent_identifier.version, "13.3.7")

        # bump version
        sut.bump_version(XmlMavenProject.VersionBumpType.PATCH, write_modules=False)

        self.assertEqual(parent_module.identifier.version, "13.3.8")
        self.assertIsNotNone(parent_module.parent_identifier)
        self.assertEqual(parent_module.parent_identifier.version, "1.1.1")

        self.assertEqual(child_module.identifier.version, "13.3.8")
        self.assertIsNotNone(child_module.parent_identifier)
        self.assertEqual(child_module.parent_identifier.version, "13.3.8")

        # set custom version
        sut.bump_version(XmlMavenProject.VersionBumpType.MAJOR, "1.2.3", write_modules=False)

        self.assertEqual(parent_module.identifier.version, "1.2.3")
        self.assertIsNotNone(parent_module.parent_identifier)
        self.assertEqual(parent_module.parent_identifier.version, "1.1.1")

        self.assertEqual(child_module.identifier.version, "1.2.3")
        self.assertIsNotNone(child_module.parent_identifier)
        self.assertEqual(child_module.parent_identifier.version, "1.2.3")

    def assertPropertyValue(
            self, properties: list[MavenProperty], name: str, expected_value: str
    ) -> None:
        for p in properties:
            if p.name == name:
                self.assertEqual(p.value, expected_value)
                return

        self.fail(f"Property '{name}' not found")

    def assertDependencyVersion(
            self,
            dependencies: list[MavenModuleIdentifier],
            group_id: str,
            artifact_id: str,
            expected_version: str,
    ) -> None:
        for d in dependencies:
            if d.group_id == group_id and d.artifact_id == artifact_id:
                self.assertEqual(d.version, expected_version)
                return

        self.fail(f"Dependency '{group_id}:{artifact_id}' not found")
