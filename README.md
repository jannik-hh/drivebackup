# drivebackup

## Getting started

1) Set up a new Google api project (https://console.developers.google.com) and create an 
   OAuth client ID (https://support.google.com/googleapi/answer/6158849?hl=en&ref_topic=7013279).
   Save the app credentials under `src/main/resources/client_secret.json` or export the 
   ENV var DRIVEBACKUP_CLIENT_SECRETS with the json data as content.

2) Authorize app to access your google drive account.
   