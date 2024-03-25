from unittest import TestCase

from maven.maven_module_identifier import MavenModuleIdentifier


class TestMavenModuleIdentifier(TestCase):
    def test_implement_abstract_properties(self) -> None:
        class MockMavenModuleIdentifier(MavenModuleIdentifier):
            def __init__(self, group_id: str, artifact_id: str, version: str):
                self._group_id: str = group_id
                self._artifact_id: str = artifact_id
                self._version: str = version

            @property
            def group_id(self) -> str:
                return self._group_id

            @property
            def artifact_id(self) -> str:
                return self._artifact_id

            def _get_version(self) -> str:
                return self._version

            def _set_version(self, version: str) -> None:
                self._version = version

        sut: MavenModuleIdentifier = MockMavenModuleIdentifier(
            "com.example", "application", "0.1.0"
        )

        self.assertEqual(sut.group_id, "com.example")
        self.assertEqual(sut.artifact_id, "application")
        self.assertEqual(sut.version, "0.1.0")

        sut.version = "1.0.0"

        self.assertEqual(sut.group_id, "com.example")
        self.assertEqual(sut.artifact_id, "application")
        self.assertEqual(sut.version, "1.0.0")
