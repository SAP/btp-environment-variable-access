from abc import ABC, abstractmethod
from pathlib import Path

from maven.maven_module import MavenModule


class MavenModuleReader(ABC):
    @abstractmethod
    def read(self, pom: Path) -> MavenModule:
        raise NotImplementedError

    @abstractmethod
    def read_recursive(self, pom: Path) -> list[MavenModule]:
        raise NotImplementedError
