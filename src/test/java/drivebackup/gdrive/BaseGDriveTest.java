package drivebackup.gdrive;

import com.google.api.client.auth.oauth2.Credential;
import org.junit.BeforeClass;

public class BaseGDriveTest {
  @BeforeClass
  public static void authenticate() {
    if (System.getenv("DRIVEBACKUP_CREDENTIAL_REFRESH_TOKEN") == null) {
      Credential credential = DriveServiceFactory.authorize();
      System.out.println(
          String.format(
              "export DRIVEBACKUP_CREDENTIAL_REFRESH_TOKEN='%s'", credential.getRefreshToken()));
    }
  }
}
