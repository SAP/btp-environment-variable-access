from pathlib import Path
from typing import Optional

from maven.maven_module import MavenModule
from maven.maven_module_identifier import MavenModuleIdentifier
from maven.maven_property import MavenProperty
from maven.xml_maven_module_identifier import XmlMavenModuleIdentifier
from maven.xml_maven_property import XmlMavenProperty
from utility.type_utility import get_or_else
from utility.xml.xml_document import XmlDocument


class XmlMavenModule(MavenModule):
    def __init__(
        self,
        xml_document: XmlDocument,
        pom_file: Path,
        identifier: XmlMavenModuleIdentifier,
        parent_identifier: Optional[XmlMavenModuleIdentifier] = None,
        properties: Optional[list[XmlMavenProperty]] = None,
        dependencies: Optional[list[XmlMavenModuleIdentifier]] = None,
        plugins: Optional[list[XmlMavenModuleIdentifier]] = None,
    ):
        self._xml_document: XmlDocument = xml_document
        self._pom_file: Path = pom_file
        self._identifier: XmlMavenModuleIdentifier = identifier
        self._parent_identifier: Optional[XmlMavenModuleIdentifier] = parent_identifier
        self._properties: list[XmlMavenProperty] = get_or_else(properties, list)
        self._dependencies: list[XmlMavenModuleIdentifier] = get_or_else(
            dependencies, list
        )
        self._plugins: list[XmlMavenModuleIdentifier] = get_or_else(plugins, list)

    @property
    def xml_document(self) -> XmlDocument:
        return self._xml_document

    @property
    def pom_file(self) -> Path:
        return self._pom_file

    def _get_identifier(self) -> MavenModuleIdentifier:
        return self._identifier

    def _get_parent_identifier(self) -> Optional[MavenModuleIdentifier]:
        return self._parent_identifier

    def _get_properties(self) -> list[MavenProperty]:
        return self._properties

    def _get_dependencies(self) -> list[MavenModuleIdentifier]:
        return self._dependencies

    def _get_plugins(self) -> list[MavenModuleIdentifier]:
        return self._plugins
