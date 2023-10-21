# Quality-Annotate

Program to annotate quality issues on your pull request.

The implementation tries to be variable within the two dimensions of:

* Code-Hoster (bitbucket, gitlab, github)
* Quality-Tool (Sonarqube, Klocwork)

|           | Bitbucket | Gitlab   | Github   |
|-----------|-----------|----------|----------|
| Sonarqube | &#10060;  | &#10060; | &#9989;  |
| ???       | &#10060;  | &#10060; | &#10060; |

Since it is a commandline application it can also be integrated in various ci-systems. Please refer to the sample ci
configs:

* &#10060; Jenkins
* &#10060; Gitlab-CI
* &#10060; Github-Actions

## Installing

Download the program from releases. Put the application.yml from the release page
into `$HOME/.config/qualityannotate.yml`.
Then run ./qualityannotate sonarqube github

## Developing

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev --quarkus-args="sonarqube github"
```

If you want to use intellij, there is a bug with the quarkus-args, so use the jvm-args
`-Dquarkus.args="sonarqube github"` for the `quarkusDev` job.

## Codestyle

Codestyle is setup using `./gradlew spotlessApply`.
[Intellij requires the eclipse code-formatter plugin](https://plugins.jetbrains.com/plugin/6546-eclipse-code-formatter/versions).
Configs are `./config/code-formatter/eclipse.importorder` and `./config/code-formatter/eclipse.xml`.

## Pre-Commit hook

```sh
pip install pre-commit
pre-commit install
pre-commit install --hook-type commit-msg
```

## Creating a native executable / uber jar

```sh
# create a native executable
./gradlew build -Dquarkus.package.type=native
# create a native executable using a docker container
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
# create an uber jar
./gradlew build -Dquarkus.package.type=uber-jar
```
