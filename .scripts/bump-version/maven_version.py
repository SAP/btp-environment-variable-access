import os
from argparse import ArgumentParser
from pathlib import Path
from typing import Any, Dict

from maven.xml_maven_module import XmlMavenModule
from maven.xml_maven_module_reader import XmlMavenModuleReader
from maven.xml_maven_project import XmlMavenProject


def bump(
        project_root_poms: list[Path],
        bump_type: XmlMavenProject.VersionBumpType,
        custom_version: str = "",
        assert_uniform_version: bool = True,
        github_actions_output: bool = True,
) -> None:
    if any(filter(lambda x: x.name != "pom.xml", project_root_poms)):
        raise AssertionError(
            f"Currently, only Maven projects ('pom.xml') are supported by this operation. Sorry."
        )

    if custom_version and not XmlMavenProject.SEMANTIC_VERSION.match(custom_version):
        raise AssertionError(f"Invalid custom version '{bump_type}'.")

    project: XmlMavenProject = XmlMavenProject()
    module_reader: XmlMavenModuleReader = XmlMavenModuleReader()

    for project_root_pom in project_root_poms:
        project.add_modules(*module_reader.read_recursive(project_root_pom))

    if github_actions_output:
        versions: Dict[XmlMavenModule, str] = project.get_module_versions()
        unique_versions: list[str] = list(set(versions.values()))

        if len(unique_versions) == 1:
            __write_to_github_actions_output("old_version", unique_versions[0])

        else:
            __write_to_github_actions_output("old_version", "undefined")

    project.bump_version(
        bump_type, custom_version, assert_uniform_version=assert_uniform_version, write_modules=True
    )

    if github_actions_output:
        versions: Dict[XmlMavenModule, str] = project.get_module_versions()
        unique_versions: list[str] = list(set(versions.values()))

        if len(unique_versions) == 1:
            __write_to_github_actions_output("new_version", unique_versions[0])

        else:
            __write_to_github_actions_output("new_version", "undefined")


def __write_to_github_actions_output(key: str, value: str) -> None:
    if "GITHUB_OUTPUT" not in os.environ:
        print("UNABLE TO WRITE TO GITHUB_OUTPUT. '$GITHUB_OUTPUT' IS NOT DEFINED.")
        return

    output_path: str = os.environ["GITHUB_OUTPUT"]
    if not output_path:
        print("UNABLE TO WRITE TO GITHUB_OUTPUT. '$GITHUB_OUTPUT' IS NOT DEFINED.")
        return

    with open(output_path, "a+") as output_file:
        print(f"{key}={value}", file=output_file)


def main() -> None:
    argument_parser: ArgumentParser = ArgumentParser(
        "Script to manage Maven project versions"
    )

    sub_parsers: Any = argument_parser.add_subparsers(dest="subparser")

    # region bump command

    bump_parser: ArgumentParser = sub_parsers.add_parser(
        "bump", help="Bumps a Maven project version."
    )
    bump_parser.add_argument(
        "--bump-type",
        type=str,
        required=True,
        help=f"Available values: '{XmlMavenProject.VersionBumpType.MAJOR}', "
             f"'{XmlMavenProject.VersionBumpType.MINOR}', "
             f"and '{XmlMavenProject.VersionBumpType.PATCH}'",
    )
    bump_parser.add_argument(
        "--custom-version",
        type=str,
        required=False,
        help="Allows to update maven modules with a custom version. "
             "The input value should be semver compatible version string (X.Y.Z). "
             "Note: This option will override the bump-type option.",
    )
    bump_parser.add_argument(
        "--accept-non-uniform-versions",
        action="store_false",
        required=False,
        help="Indicates whether the version bump should even be performed if the project contains "
             "different versions. "
             "Note: ALL module versions will be increased if this option is enabled.",
    )
    bump_parser.add_argument(
        "--no-github-action-outputs",
        action="store_true",
        required=False,
        help="Indicates whether the script should NOT set the GitHub action outputs.",
    )
    bump_parser.add_argument("pom", type=Path, nargs="+")

    # endregion

    parsed_args: Any = argument_parser.parse_args()
    if parsed_args.subparser == "bump":
        bump(
            parsed_args.pom,
            XmlMavenProject.VersionBumpType[parsed_args.bump_type.upper()],
            parsed_args.custom_version,
            not parsed_args.accept_non_uniform_versions,
            not parsed_args.no_github_action_outputs,
        )


if __name__ == "__main__":
    main()
