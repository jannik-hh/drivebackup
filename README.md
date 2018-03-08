# Drivebackup [![CircleCI](https://circleci.com/gh/jannik-hh/drivebackup.svg?style=svg)](https://circleci.com/gh/jannik-hh/drivebackup) [![Maintainability](https://api.codeclimate.com/v1/badges/df110004d0ebe06536b9/maintainability)](https://codeclimate.com/github/jannik-hh/drivebackup/maintainability) [![Test Coverage](https://api.codeclimate.com/v1/badges/df110004d0ebe06536b9/test_coverage)](https://codeclimate.com/github/jannik-hh/drivebackup/test_coverage)
Drivebackup is a CLI tool to backup data with google drive.

## Prerequisites
Maven, Java JDK 8 and a Google api project is needed.

### Create a new Google api project
Set up a new Google api project (https://console.developers.google.com) and create an
OAuth client ID (https://support.google.com/googleapi/answer/6158849?hl=en&ref_topic=7013279).
Export the ENV var DRIVEBACKUP_CLIENT_SECRETS with the json data as content.

## Getting started
Make sure DRIVEBACKUP_CLIENT_SECRETS is exported.
### Build project
`mvn package`
### Backup
`java -jar target/drivebackup-0.7.0-jar-with-dependencies.jar backup -encrypt -encryptNames -source src -target backup_drivebackup_sources`
### Recover files from backup
* Download the `backup_drivebackup_sources` dir from google drive to the current dir.
* Run
`java -jar target/drivebackup-0.7.0-jar-with-dependencies.jar decrypt -decryptNames -source backup_drivebackup_sources -target recovered -secretKey drivebackup_encryption.key`
to decrypt the content.

## Run the tests with docker-compose
1) Create a docker-compose.override.yml and add DRIVEBACKUP_CLIENT_SECRETS with your client secret to the test env vars.
2) Run `docker-compose test` to execute the tests
3) Because some tests need access to a real google drive account, you will be ask to grant access by visiting an address
   in your browser. The callback will not work directly if you are using docker-compose. You have to call the callback URL within the docker container
   `docker compose exec test curl http://localhost:30001/Callback?code=4/AACNYYuSnvC3IcF5TYZYmCu33l8Z23gxuY64wmS_AIawSxaDyPCpHIijm_A5owEoccUXXAJ2uNSw1RjrH7X3YoE#`
