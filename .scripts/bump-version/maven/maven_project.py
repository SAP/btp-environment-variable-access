from abc import ABC, abstractmethod

from maven.maven_module import MavenModule


class MavenProject(ABC):
    @property
    def modules(self) -> list[MavenModule]:
        return self._get_modules()

    @abstractmethod
    def _get_modules(self) -> list[MavenModule]:
        raise NotImplementedError

    @abstractmethod
    def add_module(self, module: MavenModule) -> None:
        raise NotImplementedError
