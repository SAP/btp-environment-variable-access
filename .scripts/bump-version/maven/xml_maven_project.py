import re
from enum import Enum
from re import Match
from typing import ClassVar, Pattern, Union, cast

from maven.maven_module import MavenModule
from maven.maven_module_identifier import MavenModuleIdentifier
from maven.xml_maven_module import XmlMavenModule
from maven.xml_maven_module_identifier import XmlMavenModuleIdentifier
from maven.xml_maven_property import XmlMavenProperty
from utility.type_utility import get_or_else
from utility.xml.xml_node import XmlNode


class XmlMavenProject:
    SEMANTIC_VERSION: ClassVar[Pattern] = re.compile(
        r"^(?P<prefix>\D+)?(?P<major>\d+)\.(?P<minor>\d+)\.(?P<patch>\d+)(?P<suffix>\D.+)?$"
    )
    PROPERTY: ClassVar[Pattern] = re.compile(r"^\$\{(?P<name>[^}]+)}$")

    class VersionBumpType(Enum):
        MAJOR = "major"
        MINOR = "minor"
        PATCH = "patch"

    def __init__(self):
        self._modules: dict[str, XmlMavenModule] = {}

    def add_modules(self, *modules: XmlMavenModule) -> None:
        for module in modules:
            module_id: str = self._module_id(module)
            if module_id not in self._modules:
                self._modules[module_id] = module

    def get_module_versions(self) -> dict[XmlMavenModule, str]:
        return {
            module: self._resolve_version_property_node(module, module.identifier).text
            for module in self._modules.values()
        }

    def bump_version(
        self,
        bump_type: "XmlMavenProject.VersionBumpType",
        custom_version: str = "",
        assert_uniform_version: bool = True,
        write_modules: bool = True,
    ) -> None:
        current_versions: dict[str, str] = {
            self._module_id(module): version
            for module, version in self.get_module_versions().items()
        }

        if assert_uniform_version:
            versions: set[str] = set(current_versions.values())
            if len(versions) != 1:
                raise AssertionError(
                    f"The Maven project is expected to have a uniform version, but multiple versions were found."
                )

        updated_versions: dict[str, str] = self._bump_versions(
            current_versions, bump_type, custom_version
        )

        for module in self._modules.values():
            self._set_version(module, updated_versions, write_modules)

    def _collect_current_versions(self) -> dict[str, str]:
        return {
            self._module_id(module): self._resolve_version_property_node(
                module, module.identifier
            ).text
            for module in self._modules.values()
        }

    def _module_id(self, module: Union[MavenModule, MavenModuleIdentifier]) -> str:
        module_id: MavenModuleIdentifier
        if isinstance(module, MavenModule):
            module_id = cast(MavenModule, module).identifier

        elif isinstance(module, MavenModuleIdentifier):
            module_id = cast(MavenModuleIdentifier, module)

        else:
            raise AssertionError(f"Unable to determine module id of '{module}'.")

        return f"{module_id.group_id}:{module_id.artifact_id}"

    def _bump_versions(
        self, versions: dict[str, str], bump_type: "XmlMavenProject.VersionBumpType", custom_version: str = "",
    ) -> dict[str, str]:
        result: dict[str, str] = {}
        for key, value in versions.items():
            match: Match = XmlMavenProject.SEMANTIC_VERSION.match(value)
            if match is None:
                raise AssertionError(
                    f"Version of module '{key}' ('{value}') is not a valid semantic version."
                )

            major: int = int(match.group("major"))
            minor: int = int(match.group("minor"))
            patch: int = int(match.group("patch"))

            prefix: str = get_or_else(match.group("prefix"), "")
            suffix: str = get_or_else(match.group("suffix"), "")

            if custom_version:
                match: Match = XmlMavenProject.SEMANTIC_VERSION.match(custom_version)
                major, minor, patch = match.group("major"), match.group("minor"), match.group("patch")

            elif bump_type == XmlMavenProject.VersionBumpType.MAJOR:
                major += 1
                minor = 0
                patch = 0

            elif bump_type == XmlMavenProject.VersionBumpType.MINOR:
                minor += 1
                patch = 0

            elif bump_type == XmlMavenProject.VersionBumpType.PATCH:
                patch += 1

            else:
                raise AssertionError(f"Invalid bump type '{bump_type}'. and custom version '{custom_version}'.")

            result[key] = f"{prefix}{major}.{minor}.{patch}{suffix}"

        return result

    def _set_version(
        self,
        module: XmlMavenModule,
        updated_versions: dict[str, str],
        write_modules: bool,
    ) -> None:
        if module.parent_identifier is not None:
            parent_id: str = self._module_id(module.parent_identifier)
            if parent_id in updated_versions:
                self._resolve_version_property_node(
                    module, module.parent_identifier
                ).text = updated_versions[parent_id]

        module_id: str = self._module_id(module.identifier)
        if module_id in updated_versions:
            self._resolve_version_property_node(
                module, module.identifier
            ).text = updated_versions[module_id]

        for dependency in module.dependencies:
            dependency_id: str = self._module_id(dependency)
            if dependency_id not in updated_versions:
                continue

            self._resolve_version_property_node(
                module, dependency
            ).text = updated_versions[dependency_id]

        if write_modules:
            module.xml_document.save(module.pom_file)

    def _resolve_version_property_node(
        self, module: MavenModule, module_id: MavenModuleIdentifier
    ) -> XmlNode:
        if not isinstance(module_id, XmlMavenModuleIdentifier):
            raise AssertionError(
                f"Unable to determine version XML node for module '{module.identifier}'."
            )

        return self._resolve_property_node(
            module, cast(XmlMavenModuleIdentifier, module_id).version_node
        )

    def _resolve_property_node(self, module: MavenModule, node: XmlNode) -> XmlNode:
        match: Match = XmlMavenProject.PROPERTY.match(node.text)
        if match is None:
            return node

        return self._find_property_node(module, match.group("name"))

    def _find_property_node(self, module: MavenModule, property_name: str) -> XmlNode:
        for p in module.properties:
            if p.name == property_name and isinstance(p, XmlMavenProperty):
                return cast(XmlMavenProperty, p).node

        if module.parent_identifier is None:
            raise AssertionError(
                f"Unable to resolve property '{property_name}' in {module.identifier}."
            )

        parent_id: str = self._module_id(module.parent_identifier)
        parent_modules: list[XmlMavenModule] = list(
            filter(
                lambda x: self._module_id(x.identifier) == parent_id,
                self._modules.values(),
            )
        )

        if len(parent_modules) != 1:
            raise AssertionError(
                f"Unable to determine parent module '{parent_id}' for module {module.identifier}."
            )

        return self._find_property_node(parent_modules[0], property_name)
