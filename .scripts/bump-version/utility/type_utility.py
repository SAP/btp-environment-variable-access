from typing import Any, Callable, Optional, TypeVar, Union

T = TypeVar("T")


def get_or_else(maybe_value: Optional[T], fallback: Union[T, Callable[[], T]]) -> T:
    if maybe_value is not None:
        return maybe_value

    if isinstance(fallback, Callable):
        return fallback()

    return fallback


def get_or_raise(
    maybe_value: Optional[T],
    error: Optional[Union[Exception, Callable[[], Exception]]] = None,
) -> T:
    if maybe_value is not None:
        return maybe_value

    if error is None:
        raise AssertionError("Value must not be None")

    if isinstance(error, Exception):
        raise error

    raise error()


def if_not_none(maybe_value: Optional[T], action: Callable[[T], None]) -> None:
    if maybe_value is None:
        return

    action(maybe_value)


def without_nones(values: list[Optional[T]]) -> list[T]:
    return [value for value in values if value is not None]


def all_defined(*values: Any) -> bool:
    for v in values:
        if v is None:
            return False

    return True
