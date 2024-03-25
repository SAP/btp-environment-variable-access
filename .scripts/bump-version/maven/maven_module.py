from abc import ABC, abstractmethod
from typing import Optional

from maven.maven_module_identifier import MavenModuleIdentifier
from maven.maven_property import MavenProperty


class MavenModule(ABC):
    @property
    def identifier(self) -> MavenModuleIdentifier:
        return self._get_identifier()

    @abstractmethod
    def _get_identifier(self) -> MavenModuleIdentifier:
        raise NotImplementedError

    @property
    def parent_identifier(self) -> Optional[MavenModuleIdentifier]:
        return self._get_parent_identifier()

    @abstractmethod
    def _get_parent_identifier(self) -> Optional[MavenModuleIdentifier]:
        raise NotImplementedError

    @property
    def properties(self) -> list[MavenProperty]:
        return self._get_properties()

    @abstractmethod
    def _get_properties(self) -> list[MavenProperty]:
        raise NotImplementedError

    @property
    def dependencies(self) -> list[MavenModuleIdentifier]:
        return self._get_dependencies()

    @abstractmethod
    def _get_dependencies(self) -> list[MavenModuleIdentifier]:
        raise NotImplementedError

    @property
    def plugins(self) -> list[MavenModuleIdentifier]:
        return self._get_plugins()

    @abstractmethod
    def _get_plugins(self) -> list[MavenModuleIdentifier]:
        raise NotImplementedError

    def __str__(self) -> str:
        return str(self.identifier)
