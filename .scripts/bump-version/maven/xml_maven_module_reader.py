from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional
from xml.etree import ElementTree
from xml.etree.ElementTree import TreeBuilder, XMLParser

from maven.maven_module_reader import MavenModuleReader
from maven.xml_maven_module import XmlMavenModule
from maven.xml_maven_module_identifier import XmlMavenModuleIdentifier
from maven.xml_maven_property import XmlMavenProperty
from utility.type_utility import all_defined, get_or_raise
from utility.xml.e_tree_xml_document import ETreeXmlDocument
from utility.xml.xml_document import XmlDocument
from utility.xml.xml_node import XmlNode


class XmlMavenModuleReader(MavenModuleReader):
    @dataclass
    class Context:
        pom: Path
        xml_document: XmlDocument
        properties: list[XmlMavenProperty] = field(default_factory=list)
        parent_identifier: Optional[XmlMavenModuleIdentifier] = None
        identifier: Optional[XmlMavenModuleIdentifier] = None
        dependencies: list[XmlMavenModuleIdentifier] = field(default_factory=list)
        plugins: list[XmlMavenModuleIdentifier] = field(default_factory=list)

    def read(self, pom: Path) -> XmlMavenModule:
        context: XmlMavenModuleReader.Context = self._create_context(pom)
        return self._read(context)

    def _read(self, context: "XmlMavenModuleReader.Context") -> XmlMavenModule:
        self._read_properties(context)
        self._read_parent_identifier(context)
        self._read_identifier(context)
        self._read_dependencies(context)
        self._read_plugins(context)

        return XmlMavenModule(
            context.xml_document,
            context.pom,
            get_or_raise(context.identifier),
            context.parent_identifier,
            context.properties,
            context.dependencies,
            context.plugins,
        )

    def read_recursive(self, pom: Path) -> list[XmlMavenModule]:
        context: XmlMavenModuleReader.Context = self._create_context(pom)
        return self._read_recursive(context)

    def _read_recursive(
        self, context: "XmlMavenModuleReader.Context"
    ) -> list[XmlMavenModule]:
        result: list[XmlMavenModule] = [self._read(context)]

        for pom_path in self._read_modules(context):
            new_context: "XmlMavenModuleReader.Context" = self._create_context(pom_path)
            result.extend(self._read_recursive(new_context))

        return result

    def _create_context(self, pom: Path) -> "XmlMavenModuleReader.Context":
        parser: XMLParser = XMLParser(target=TreeBuilder(insert_comments=True))
        xml_document: XmlDocument = ETreeXmlDocument(ElementTree.parse(pom, parser))

        return XmlMavenModuleReader.Context(pom, xml_document)

    def _read_properties(self, context: "XmlMavenModuleReader.Context") -> None:
        root: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "properties"
        )
        if root is None:
            return

        context.properties = [XmlMavenProperty(node) for node in root.nodes]

    def _read_parent_identifier(self, context: "XmlMavenModuleReader.Context") -> None:
        root: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "parent"
        )
        if root is None:
            return

        g: Optional[XmlNode] = root.find_first_node("groupId")
        a: Optional[XmlNode] = root.find_first_node("artifactId")
        v: Optional[XmlNode] = root.find_first_node("version")

        if not all_defined(g, a, v):
            raise AssertionError(
                f"Unable to determine parent module from '{context.pom.resolve().absolute()}'."
            )

        context.parent_identifier = XmlMavenModuleIdentifier(g, a, v)

    def _read_identifier(self, context: "XmlMavenModuleReader.Context") -> None:
        g: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "groupId"
        )
        a: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "artifactId"
        )
        v: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "version"
        )

        if g is None:
            if context.parent_identifier is None:
                raise AssertionError(
                    f"Unable to determine Maven Group Id from '{context.pom.resolve().absolute()}'."
                )

            g = context.parent_identifier.group_id_node

        if v is None:
            if context.parent_identifier is None:
                raise AssertionError(
                    f"Unable to determine Maven Version from '{context.pom.resolve().absolute()}'."
                )

            v = context.parent_identifier.version_node

        if not all_defined(g, a, v):
            raise AssertionError(
                f"Unable to determine Maven identifier (GAV) from '{context.pom.resolve().absolute()}'."
            )

        context.identifier = XmlMavenModuleIdentifier(g, a, v)

    def _read_dependencies(self, context: "XmlMavenModuleReader.Context") -> None:
        dp_root: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "dependencies"
        )
        if dp_root is not None:
            for dependency_root in dp_root.nodes:
                self._read_dependency(context, dependency_root)

        dpm_root: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "dependencyManagement", "dependencies"
        )
        if dpm_root is not None:
            for dependency_root in dpm_root.nodes:
                self._read_dependency(context, dependency_root)

    def _read_dependency(
        self, context: "XmlMavenModuleReader.Context", root: XmlNode
    ) -> None:
        g: Optional[XmlNode] = root.find_first_node("groupId")
        a: Optional[XmlNode] = root.find_first_node("artifactId")
        v: Optional[XmlNode] = root.find_first_node("version")

        if not all_defined(g, a, v):
            return

        context.dependencies.append(XmlMavenModuleIdentifier(g, a, v))

    def _read_modules(self, context: "XmlMavenModuleReader.Context") -> list[Path]:
        root: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "modules"
        )
        if root is None:
            return []

        return [Path(context.pom.parent, node.text, "pom.xml") for node in root.nodes]

    def _read_plugins(self, context: "XmlMavenModuleReader.Context") -> None:
        build_root: Optional[XmlNode] = context.xml_document.find_first_node(
            "project", "build"
        )
        if build_root is None:
            return

        pl_root: Optional[XmlNode] = build_root.find_first_node("plugins")
        if pl_root is not None:
            for plugin_root in pl_root.nodes:
                self._read_plugin(context, plugin_root)

        plm_root: Optional[XmlNode] = build_root.find_first_node(
            "pluginManagement", "plugins"
        )
        if plm_root is not None:
            for plugin_root in plm_root.nodes:
                self._read_plugin(context, plugin_root)

    def _read_plugin(
        self, context: "XmlMavenModuleReader.Context", root: XmlNode
    ) -> None:
        g: Optional[XmlNode] = root.find_first_node("groupId")
        a: Optional[XmlNode] = root.find_first_node("artifactId")
        v: Optional[XmlNode] = root.find_first_node("version")

        if not all_defined(g, a, v):
            return

        context.plugins.append(XmlMavenModuleIdentifier(g, a, v))
