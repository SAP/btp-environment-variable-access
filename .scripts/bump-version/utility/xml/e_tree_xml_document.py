from pathlib import Path
from typing import Optional
from xml.etree.ElementTree import ElementTree

from utility.xml.e_tree_xml_node import ETreeXmlNode
from utility.xml.xml_document import XmlDocument
from utility.xml.xml_node import XmlNode


class ETreeXmlDocument(XmlDocument):
    def __init__(self, element_tree: ElementTree):
        self._delegate: ElementTree = element_tree

    def find_first_node(self, *path_segments: str) -> Optional[XmlNode]:
        maybe_root: Optional[XmlNode] = self._try_get_root_node()
        if maybe_root is None:
            return None

        if len(path_segments) < 1:
            return None

        if maybe_root.name != path_segments[0]:
            return None

        return maybe_root.find_first_node(*path_segments[1:])

    def find_all_nodes(self, *path_segments: str) -> list[XmlNode]:
        maybe_root: Optional[XmlNode] = self._try_get_root_node()
        if maybe_root is None:
            return []

        if len(path_segments) < 1:
            return []

        if maybe_root.name != path_segments[0]:
            return []

        return maybe_root.find_all_nodes(*path_segments)

    def save(self, file: Path) -> None:
        namespace: str = self._get_default_namespace()
        self._delegate.write(
            file, encoding="UTF-8", xml_declaration=True, default_namespace=namespace
        )

        self._replace_quotes_with_double_quotes(file)

    def _get_default_namespace(self) -> str:
        maybe_root: Optional[XmlNode] = self._try_get_root_node()
        if maybe_root is None:
            return ""

        return maybe_root.namespace

    def _try_get_root_node(self) -> Optional[XmlNode]:
        return ETreeXmlNode.try_create(self._delegate.getroot())

    def _replace_quotes_with_double_quotes(self, file: Path) -> None:
        content: str = ""
        with file.open("r") as r:
            for line in r:
                if line.strip().startswith("<!--"):
                    content += line

                else:
                    content += line.replace("'", '"')

        with file.open("w") as w:
            w.write(content)
