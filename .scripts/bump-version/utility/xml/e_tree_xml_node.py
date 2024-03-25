import re
from re import Match, Pattern
from typing import ClassVar, Optional
from xml.etree.ElementTree import Element

from utility.type_utility import without_nones
from utility.xml.xml_node import XmlNode


class ETreeXmlNode(XmlNode):
    NAME_PATTERN: ClassVar[Pattern] = re.compile(
        r"^(?:\{(?P<namespace>[^}]+)})?(?P<tag>.+)$"
    )

    @staticmethod
    def try_create(delegate: Optional[Element]) -> Optional["ETreeXmlNode"]:
        if delegate is None:
            return None

        if not isinstance(delegate.tag, str):
            return None

        return ETreeXmlNode(delegate)

    def __init__(self, delegate: Element):
        self._delegate: Element = delegate
        self._name: str = self._delegate.tag
        self._namespace: str = ""

        match: Match = ETreeXmlNode.NAME_PATTERN.match(self._name)
        if match is None:
            return

        self._name = match.group("tag")
        if "namespace" in match.groupdict():
            self._namespace = match.group("namespace")

    @property
    def name(self) -> str:
        return self._name

    @property
    def namespace(self) -> str:
        return self._namespace

    def _get_text(self) -> Optional[str]:
        return self._delegate.text

    def _set_text(self, text: str) -> None:
        self._delegate.text = text

    def _get_nodes(self) -> list["XmlNode"]:
        return without_nones([ETreeXmlNode.try_create(node) for node in self._delegate])

    def find_first_node(self, *path_segments: str) -> Optional["XmlNode"]:
        if len(path_segments) < 1:
            return self

        for matching_node in filter(lambda x: x.name == path_segments[0], self.nodes):
            return matching_node.find_first_node(*path_segments[1:])

        return None

    def find_all_nodes(self, *path_segments: str) -> list["XmlNode"]:
        if len(path_segments) < 1:
            return [self]

        result: list[XmlNode] = []
        for matching_node in filter(lambda x: x.name == path_segments[0], self.nodes):
            result.extend(matching_node.find_all_nodes(*path_segments[1:]))

        return result
