from typing import Optional
from unittest import TestCase
from xml.etree.ElementTree import Element, ElementTree

from utility.xml.e_tree_xml_document import ETreeXmlDocument
from utility.xml.xml_node import XmlNode


class TestETreeXmlDocument(TestCase):
    def test_find_root_node(self) -> None:
        root_element: Element = Element("{root-namespace}root-node")
        tree: ElementTree = ElementTree(root_element)

        sut: ETreeXmlDocument = ETreeXmlDocument(tree)

        root_node: Optional[XmlNode] = sut.find_first_node("root-node")

        self.assertIsNotNone(root_node)
        self.assertEqual(root_node.name, "root-node")
        self.assertEqual(root_node.namespace, "root-namespace")
        self.assertIsNone(root_node.text)

    def test_find_nested_node(self) -> None:
        root_element: Element = Element("{root-namespace}root-node")
        root_element.text = "root-text"
        sub_element: Element = Element("{sub-namespace}sub-node")
        sub_element.text = "sub-text"
        root_element.append(sub_element)

        tree: ElementTree = ElementTree(root_element)

        sut: ETreeXmlDocument = ETreeXmlDocument(tree)

        sub_node: Optional[XmlNode] = sut.find_first_node("root-node", "sub-node")

        self.assertIsNotNone(sub_node)
        self.assertEqual(sub_node.name, "sub-node")
        self.assertEqual(sub_node.namespace, "sub-namespace")
        self.assertEqual(sub_node.text, "sub-text")
