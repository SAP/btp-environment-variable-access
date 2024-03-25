from typing import Optional
from unittest import TestCase

from utility.type_utility import get_or_else, get_or_raise, if_not_none, without_nones


class TestTypeUtility(TestCase):
    NONE: Optional[str] = None

    def test_get_or_else(self) -> None:
        self.assertEqual(get_or_else("Foo", "Bar"), "Foo")
        self.assertEqual(get_or_else(self.NONE, "Bar"), "Bar")
        self.assertEqual(get_or_else(self.NONE, lambda: "Bar"), "Bar")

    def test_get_or_raise(self) -> None:
        self.assertEqual(get_or_raise("Foo", Exception("Assertion Error")), "Foo")

        with self.assertRaises(AssertionError):
            get_or_raise(None)

        with self.assertRaises(Exception):
            get_or_raise(None, Exception())

        with self.assertRaises(Exception):
            get_or_raise(None, lambda: Exception())

    def test_if_not_none(self) -> None:
        sut: list[str] = []

        if_not_none("Hello, World!", sut.append)
        self.assertListEqual(sut, ["Hello, World!"])

        if_not_none(self.NONE, sut.append)
        self.assertListEqual(sut, ["Hello, World!"])

    def test_without_nones(self) -> None:
        sut: list[str] = ["foo", None, "bar", None, None, "baz"]

        self.assertListEqual(without_nones(sut), ["foo", "bar", "baz"])
