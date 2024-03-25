from typing import Optional
from unittest import TestCase
from xml.etree.ElementTree import Element

from utility.xml.e_tree_xml_node import ETreeXmlNode
from utility.xml.xml_node import XmlNode


class TestETreeXmlNode(TestCase):
    def test_get_name(self) -> None:
        element: Element = Element("{namespace}name")
        sut: ETreeXmlNode = ETreeXmlNode(element)

        self.assertEqual(sut.name, "name")

    def test_get_namespace(self) -> None:
        element: Element = Element("{namespace}name")
        sut: ETreeXmlNode = ETreeXmlNode(element)

        self.assertEqual(sut.namespace, "namespace")

    def test_get_text(self) -> None:
        element: Element = Element("{namespace}name")
        element.text = "Hello, World!"
        sut: ETreeXmlNode = ETreeXmlNode(element)

        self.assertEqual(sut.text, "Hello, World!")

    def test_set_text(self) -> None:
        element: Element = Element("{namespace}name")
        element.text = "Hello, World!"
        sut: ETreeXmlNode = ETreeXmlNode(element)

        sut.text = "foo"

        self.assertEqual(element.text, "foo")
        self.assertEqual(sut.text, "foo")

    def test_find_first_node_without_path(self) -> None:
        element: Element = Element("{namespace}name")
        sut: ETreeXmlNode = ETreeXmlNode(element)

        self.assertEqual(sut, sut.find_first_node())

    def test_find_first_nested_node(self) -> None:
        element: Element = Element("{namespace}name")
        sub_element: Element = Element("{sub-namespace}sub-name")
        sub_element.text = "sub-text"

        element.append(sub_element)

        sut: ETreeXmlNode = ETreeXmlNode(element)

        sub_node: Optional[ETreeXmlNode] = sut.find_first_node("sub-name")

        self.assertIsNotNone(sub_node)
        self.assertEqual(sub_node.name, "sub-name")
        self.assertEqual(sub_node.namespace, "sub-namespace")
        self.assertEqual(sub_node.text, "sub-text")

    def test_find_first_nested_node_with_changed_element(self) -> None:
        element: Element = Element("{namespace}name")
        sub_element: Element = Element("{sub-namespace}sub-name")
        sub_element.text = "sub-text"

        element.append(sub_element)

        sut: ETreeXmlNode = ETreeXmlNode(element)

        sub_node: Optional[ETreeXmlNode] = sut.find_first_node("sub-name")

        # sanity check: sub node is found
        self.assertIsNotNone(sub_node)

        element.remove(sub_element)

        sub_node = sut.find_first_node("sub-name")

        self.assertIsNone(sub_node)

    def test_find_all_nodes_without_path(self) -> None:
        element: Element = Element("{namespace}name")
        sut: ETreeXmlNode = ETreeXmlNode(element)

        self.assertListEqual(sut.find_all_nodes(), [sut])

    def test_find_all_nested_nodes(self) -> None:
        element: Element = Element("{namespace}name")
        first_sub_element: Element = Element("{sub-namespace-1}sub-name")
        first_sub_element.text = "sub-text-1"
        second_sub_element: Element = Element("{sub-namespace-2}sub-name")
        second_sub_element.text = "sub-text-2"

        element.append(first_sub_element)
        element.append(second_sub_element)

        sut: ETreeXmlNode = ETreeXmlNode(element)

        sub_nodes: list[XmlNode] = sut.find_all_nodes("sub-name")

        self.assertEqual(len(sub_nodes), 2)

        first_sub_node: XmlNode = sub_nodes[0]
        self.assertEqual(first_sub_node.name, "sub-name")
        self.assertEqual(first_sub_node.namespace, "sub-namespace-1")
        self.assertEqual(first_sub_node.text, "sub-text-1")

        second_sub_node: XmlNode = sub_nodes[1]
        self.assertEqual(second_sub_node.name, "sub-name")
        self.assertEqual(second_sub_node.namespace, "sub-namespace-2")
        self.assertEqual(second_sub_node.text, "sub-text-2")

    def test_find_all_nested_nodes_with_changed_element(self) -> None:
        element: Element = Element("{namespace}name")
        first_sub_element: Element = Element("{sub-namespace-1}sub-name")
        first_sub_element.text = "sub-text-1"
        second_sub_element: Element = Element("{sub-namespace-2}sub-name")
        second_sub_element.text = "sub-text-2"

        element.append(first_sub_element)
        element.append(second_sub_element)

        sut: ETreeXmlNode = ETreeXmlNode(element)

        sub_nodes: list[XmlNode] = sut.find_all_nodes("sub-name")

        # sanity check: make sure all sub nodes are found
        self.assertEqual(len(sub_nodes), 2)

        element.remove(first_sub_element)

        sub_nodes = sut.find_all_nodes("sub-name")

        self.assertEqual(len(sub_nodes), 1)
