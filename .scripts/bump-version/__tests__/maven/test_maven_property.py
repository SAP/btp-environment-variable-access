from unittest import TestCase

from maven.maven_property import MavenProperty


class TestMavenProperty(TestCase):
    def test_implement_abstract_properties(self) -> None:
        class MockMavenProperty(MavenProperty):
            def __init__(self, name: str, value: str):
                self._name: str = name
                self._value: str = value

            def _get_name(self) -> str:
                return self._name

            def _get_value(self) -> str:
                return self._value

            def _set_value(self, value: str) -> None:
                self._value = value

        sut: MavenProperty = MockMavenProperty("foo", "bar")

        self.assertEqual(sut.name, "foo")
        self.assertEqual(sut.value, "bar")

        sut.value = "baz"

        self.assertEqual(sut.name, "foo")
        self.assertEqual(sut.value, "baz")
