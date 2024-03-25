from abc import ABC, abstractmethod


class MavenProperty(ABC):
    @property
    def name(self) -> str:
        return self._get_name()

    @abstractmethod
    def _get_name(self) -> str:
        raise NotImplementedError

    @property
    def value(self) -> str:
        return self._get_value()

    @abstractmethod
    def _get_value(self) -> str:
        raise NotImplementedError

    @value.setter
    def value(self, value: str) -> None:
        self._set_value(value)

    @abstractmethod
    def _set_value(self, value: str) -> None:
        raise NotImplementedError

    def __str__(self) -> str:
        return f"{self.name} = '{self.value}'"
