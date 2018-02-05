package drivebackup.gdrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DriveServiceFactory {
  private static final String APPLICATION_NAME = "drivebackup";

  /** Directory to store user credentials for this application. */
  private static final java.io.File DATA_STORE_DIR =
      new java.io.File(System.getProperty("user.home"), ".credentials/drivebackup");

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  private static HttpTransport HTTP_TRANSPORT;

  private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

  private static Drive drive;

  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  public static Credential authorize() {
    return authorize(DATA_STORE_DIR.getAbsolutePath());
  }

  public static Credential authorize(String pathToDataStoreDir) {
    try {
      java.io.File acutalDataStoreDir = null;
      if (pathToDataStoreDir == null) {
        acutalDataStoreDir = DATA_STORE_DIR;
      } else {
        acutalDataStoreDir = new java.io.File(pathToDataStoreDir);
      }
      DataStoreFactory dataStoreFactory = new FileDataStoreFactory(acutalDataStoreDir);
      // Build flow and trigger user authorization request.
      GoogleAuthorizationCodeFlow flow =
          new GoogleAuthorizationCodeFlow.Builder(
                  HTTP_TRANSPORT, JSON_FACTORY, getClientSecrets(), SCOPES)
              .setDataStoreFactory(dataStoreFactory)
              .setAccessType("offline")
              .build();
      Credential credential =
          new AuthorizationCodeInstalledApp(
                  flow,
                  new LocalServerReceiver.Builder().setPort(9001).setHost("localhost").build())
              .authorize("user");
      return credential;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Build and return an authorized Drive client service.
   *
   * @return an authorized Drive client service
   * @throws IOException
   */
  public static Drive getDriveService(String pathToDataStoreDir) throws IOException {
    if (drive == null) {
      Credential credential = credentialFromEnvVar().orElseGet(() -> authorize(pathToDataStoreDir));
      drive =
          new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
              .setApplicationName(APPLICATION_NAME)
              .build();
    }
    return drive;
  }

  public static Drive getDriveService() throws IOException {
    return getDriveService(null);
  }

  private static GoogleClientSecrets getClientSecrets() {
    try {
      // Load client secrets.
      String clientSecretsString = System.getenv("DRIVEBACKUP_CLIENT_SECRETS");
      if (clientSecretsString != null) {
        return GoogleClientSecrets.load(JSON_FACTORY, new StringReader(clientSecretsString));
      } else {
        InputStream in = DriveServiceFactory.class.getResourceAsStream("/client_secret.json");
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Optional<Credential> credentialFromEnvVar() throws IOException {
    return Optional.ofNullable(System.getenv("DRIVEBACKUP_CREDENTIAL_REFRESH_TOKEN"))
        .map(
            (refreshToken) ->
                new GoogleCredential.Builder()
                    .setTransport(HTTP_TRANSPORT)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(getClientSecrets())
                    .build()
                    .setFromTokenResponse(new GoogleTokenResponse().setRefreshToken(refreshToken)));
  }
}
