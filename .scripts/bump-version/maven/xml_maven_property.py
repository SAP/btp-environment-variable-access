from maven.maven_property import MavenProperty
from utility.xml.xml_node import XmlNode


class XmlMavenProperty(MavenProperty):
    def __init__(self, node: XmlNode):
        self._delegate: XmlNode = node

    @property
    def node(self) -> XmlNode:
        return self._delegate

    def _get_name(self) -> str:
        return self._delegate.name

    def _get_value(self) -> str:
        return self._delegate.text

    def _set_value(self, value: str) -> None:
        self._delegate.text = value
