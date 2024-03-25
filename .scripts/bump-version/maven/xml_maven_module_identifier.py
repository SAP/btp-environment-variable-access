from maven.maven_module_identifier import MavenModuleIdentifier
from utility.xml.xml_node import XmlNode


class XmlMavenModuleIdentifier(MavenModuleIdentifier):
    def __init__(
        self, group_id_node: XmlNode, artifact_id_node: XmlNode, version_node: XmlNode
    ):
        self._group_id_node: XmlNode = group_id_node
        self._artifact_id_node: XmlNode = artifact_id_node
        self._version_node: XmlNode = version_node

    @property
    def group_id(self) -> str:
        return self._group_id_node.text

    @property
    def group_id_node(self) -> XmlNode:
        return self._group_id_node

    @group_id_node.setter
    def group_id_node(self, node: XmlNode) -> None:
        self._group_id_node = node

    @property
    def artifact_id(self) -> str:
        return self._artifact_id_node.text

    @property
    def artifact_id_node(self) -> XmlNode:
        return self._artifact_id_node

    def _get_version(self) -> str:
        return self._version_node.text

    def _set_version(self, version: str) -> None:
        self._version_node.text = version

    @property
    def version_node(self) -> XmlNode:
        return self._version_node

    @version_node.setter
    def version_node(self, node: XmlNode) -> None:
        self._version_node = node
