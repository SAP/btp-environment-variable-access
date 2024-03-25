from typing import Optional
from unittest import TestCase

from utility.xml.xml_node import XmlNode


class TestXmlNode(TestCase):
    def test_abstract_property(self) -> None:
        class MockXmlNode(XmlNode):
            def __init__(self, name: str, text: str):
                self._name: str = name
                self._text: str = text

            @property
            def name(self) -> str:
                return self._name

            def _get_text(self) -> str:
                return self._text

            def _set_text(self, text: str) -> None:
                self._text = text

            def _get_nodes(self) -> list["XmlNode"]:
                return []

            def find_first_node(self, *path_segments: str) -> Optional["XmlNode"]:
                return None

            def find_all_nodes(self, *path_segments: str) -> list["XmlNode"]:
                return []

        sut: XmlNode = MockXmlNode("foo", "bar")

        self.assertEqual(sut.name, "foo")
        self.assertEqual(sut.text, "bar")

        sut.text = "baz"

        self.assertEqual(sut.name, "foo")
        self.assertEqual(sut.text, "baz")
