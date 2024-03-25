from abc import ABC, abstractmethod


class MavenModuleIdentifier(ABC):
    @property
    def group_id(self) -> str:
        raise NotImplementedError

    @property
    def artifact_id(self) -> str:
        raise NotImplementedError

    @property
    def version(self) -> str:
        return self._get_version()

    @abstractmethod
    def _get_version(self) -> str:
        raise NotImplementedError

    @version.setter
    def version(self, version: str) -> None:
        self._set_version(version)

    @abstractmethod
    def _set_version(self, version: str) -> None:
        raise NotImplementedError

    def __str__(self) -> str:
        return f"{self.group_id}:{self.artifact_id}:{self.version}"
