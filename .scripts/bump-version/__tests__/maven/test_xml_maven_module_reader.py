from pathlib import Path
from typing import cast
from unittest import TestCase

from maven.xml_maven_module import XmlMavenModule
from maven.xml_maven_module_identifier import XmlMavenModuleIdentifier
from maven.xml_maven_module_reader import XmlMavenModuleReader


class TestXmlMavenModuleReader(TestCase):
    RESOURCES: Path = Path(Path(__file__).parent, "resources", Path(__file__).stem)

    def test_read_single_module(self) -> None:
        sut: XmlMavenModuleReader = XmlMavenModuleReader()

        module: XmlMavenModule = sut.read(
            Path(self.RESOURCES, "single_module", "pom.xml")
        )

        self.assertEqual(module.identifier.group_id, "com.example")
        self.assertEqual(module.identifier.artifact_id, "application")
        self.assertEqual(module.identifier.version, "13.3.7")
        self.assertEqual(len(module.properties), 1)
        self.assertEqual(len(module.dependencies), 2)
        self.assertEqual(len(module.plugins), 2)
        self.assertIsNotNone(module.parent_identifier)
        self.assertEqual(module.parent_identifier.group_id, "com.example")
        self.assertEqual(module.parent_identifier.artifact_id, "parent")
        self.assertEqual(module.parent_identifier.version, "1.1.1")

    def test_read_multi_module_recursively(self) -> None:
        sut: XmlMavenModuleReader = XmlMavenModuleReader()

        modules: list[XmlMavenModule] = sut.read_recursive(
            Path(self.RESOURCES, "multi_module", "pom.xml")
        )

        self.assertEqual(len(modules), 2)

        parent: XmlMavenModule = modules[0]
        self.assertEqual(parent.identifier.group_id, "com.example")
        self.assertEqual(parent.identifier.artifact_id, "application")
        self.assertEqual(parent.identifier.version, "13.3.7")
        self.assertEqual(len(parent.properties), 1)
        self.assertEqual(len(parent.dependencies), 2)
        self.assertIsNotNone(parent.parent_identifier)
        self.assertEqual(parent.parent_identifier.group_id, "com.example")
        self.assertEqual(parent.parent_identifier.artifact_id, "parent")
        self.assertEqual(parent.parent_identifier.version, "1.1.1")

        child: XmlMavenModule = modules[1]
        self.assertEqual(child.identifier.group_id, "com.example")
        self.assertEqual(child.identifier.artifact_id, "sub-module")
        self.assertEqual(child.identifier.version, "1.33.7")
        self.assertEqual(len(child.properties), 1)
        self.assertEqual(len(child.dependencies), 1)
        self.assertIsNotNone(child.parent_identifier)
        self.assertEqual(child.parent_identifier.group_id, "com.example")
        self.assertEqual(child.parent_identifier.artifact_id, "application")
        self.assertEqual(child.parent_identifier.version, "1.33.7")

        self.assertIsInstance(child.identifier, XmlMavenModuleIdentifier)
        self.assertIsInstance(child.parent_identifier, XmlMavenModuleIdentifier)
        self.assertEqual(
            cast(XmlMavenModuleIdentifier, child.identifier).version_node,
            cast(XmlMavenModuleIdentifier, child.parent_identifier).version_node,
        )
