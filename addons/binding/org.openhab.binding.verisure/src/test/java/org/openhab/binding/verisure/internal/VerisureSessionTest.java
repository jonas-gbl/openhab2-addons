package org.openhab.binding.verisure.internal;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openhab.binding.verisure.ArmStatus;
import org.openhab.binding.verisure.LockStatus;
import org.openhab.binding.verisure.internal.json.Installation;
import org.openhab.binding.verisure.internal.json.InstallationOverview;

@RunWith(JUnit4.class)
public class VerisureSessionTest {

    private static final String GIID = "112233445566";
    private static final String PIN = "1234";
    private static final String USERNAME = "someone@somewhere.net";
    private static final String PASSWORD = "p@$$w0rd";
    private static final String SMARTPLUG = "SMRT PLG1";
    private static final String DOOR_LOCK = "D00R L0CK";
    private static final String BASE_URL = "http://localhost:9090";
    private static final String COOKIE_NAME = "vid";
    private static final String COOKIE = "@-c00k1e";
    private static final ArmStatus ARM_STATUS = ArmStatus.ARMED_HOME;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9090);
    private VerisureSession session;

    @Before
    public void setUp() {
        setupWireMock();
        setupVerisureSession();
    }

    @After
    public void disposeVerisureSession() throws IOException {
        session.logout();
    }

    @Test
    public void testLogin() throws IOException {
        session.login();
        assertTrue(session.isLoggedIn());
    }

    @Test
    public void testLoginNoCookieFound() throws IOException {
        stubFor(post(urlEqualTo("/cookie"))
                        .withBasicAuth("CPE/" + USERNAME, PASSWORD)
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("{\"pasta\": \"spaghetti\"}")));
        try {
            session.login();
            fail();
        } catch (VerisureResponseException vre) {
            assertFalse(session.isLoggedIn());
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void testLogout() throws IOException {
        session.login();
        assertTrue(session.isLoggedIn());
        boolean loggedOutOnServer = session.logout();
        assertTrue(loggedOutOnServer);
        assertFalse(session.isLoggedIn());
    }

    @Test
    public void testAuthorizationErrorShouldLogout() {

        try {
            session.login();
            assertTrue(session.isLoggedIn());

            stubFor(get(urlEqualTo("/installation/" + GIID + "/overview"))
                            .withCookie(COOKIE_NAME, equalTo(COOKIE))
                            .willReturn(aResponse()
                                                .withStatus(401)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\n" +
                                                                  "    \"errorGroup\": \"UNAUTHORIZED\",\n" +
                                                                  "    \"errorCode\": \"AUT_00041\",\n" +
                                                                  "    \"errorMessage\": \"Cannot use both basic authentication and cookie " +
                                                                  "authentication.\"\n" +
                                                                  "}")));

            session.retrieveInstallationOverview(GIID);
            fail();
        } catch (VerisureResponseException vre) {
            assertFalse(session.isLoggedIn());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSetArmState() {
        try {
            session.login();
            session.setArmState(GIID, PIN, ArmStatus.ARMED_HOME);
            stubFor(put(urlEqualTo("/installation/" + GIID + "/armstate/code"))
                            .withCookie(COOKIE_NAME, equalTo(COOKIE))
                            .withRequestBody(equalToJson("{\"state\": \"" + ARM_STATUS.id + "\", \"code\": \"" + PIN + "\"}", true, true))
                            .willReturn(aResponse()
                                                .withStatus(400)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\n" +
                                                                  "\t\"errorCode\": \"VAL_00818\",\n" +
                                                                  "\t\"errorGroup\": \"BAD_REQUEST\",\n" +
                                                                  "\t\"errorMessage\": \"The requested arm state is not possible to apply due " +
                                                                  "to state already set\"\n" +
                                                                  "}")));
            session.setArmState(GIID, PIN, ArmStatus.ARMED_HOME);
        } catch (VerisureResponseException vre) {
            assertFalse(session.isLoggedIn());
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = VerisureResponseException.class)
    public void testSetArmState_NonRecoverableError() throws IOException {
        session.login();

        stubFor(put(urlEqualTo("/installation/" + GIID + "/armstate/code"))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .withRequestBody(equalToJson("{\"state\": \"" + ARM_STATUS.id + "\", \"code\": \"" + PIN + "\"}", true, true))
                        .willReturn(aResponse()
                                            .withStatus(400)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("{\n" +
                                                              "\t\"errorCode\": \"ERROR123\",\n" +
                                                              "\t\"errorGroup\": \"BAD_REQUEST\",\n" +
                                                              "\t\"errorMessage\": \"Oops\"\n" +
                                                              "}")));
        session.setArmState(GIID, PIN, ArmStatus.ARMED_HOME);
    }

    @Test(expected = VerisureResponseException.class)
    public void testUnexpectedResponse() throws IOException {
        stubFor(get(urlEqualTo("/installation/" + GIID + "/overview"))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .willReturn(aResponse()
                                            .withStatus(500)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("<whatever/>")));

        session.login();
        session.retrieveInstallationOverview(GIID);
    }

    @Test
    public void testRetrieveInstallationOverview() throws IOException {

        session.login();
        InstallationOverview installationOverview = session.retrieveInstallationOverview(GIID);
        assertNotNull(installationOverview);
        assertThat(installationOverview.getDoorLockStatusList(), hasSize(1));
    }

    @Test
    public void testRetrieveInstallations() throws IOException {
        session.login();

        List<Installation> installations = session.retrieveInstallations();
        assertThat(installations, hasSize(1));
    }

    @Test
    public void testRetrieveInstallationsNoInstallationsFound() throws IOException {
        stubFor(get(urlEqualTo("/installation/search?email=" + USERNAME))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("[]")));
        session.login();

        List<Installation> installations = session.retrieveInstallations();
        assertThat(installations, is(empty()));
    }

    @Test(expected = IOException.class)
    public void testRetrieveInstallationsNoGiidFound() throws IOException {
        stubFor(get(urlEqualTo("/installation/search?email=" + USERNAME))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("[\n" +
                                                              "    {\n" +
                                                              "        \"alias\": \"Somewherevej\",\n" +
                                                              "        \"shard\": 24,\n" +
                                                              "        \"routingGroup\": \"DK\",\n" +
                                                              "        \"street\": \"Somewherevej 33\",\n" +
                                                              "        \"streetNo2\": \"\",\n" +
                                                              "        \"firmwareVersion\": 26650\n" +
                                                              "    }\n" +
                                                              "]")));

        session.login();
        session.retrieveInstallations();
    }

    @Test(expected = IOException.class)
    public void testRetrieveInstallationsNonJsonResponse() throws IOException {
        stubFor(get(urlEqualTo("/installation/search?email=" + USERNAME))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("<response>\n" +
                                                              "    <installation>\n" +
                                                              "        <giid>11942690918</giid>\n" +
                                                              "        <firmwareVersion>26650</firmwareVersion>\n" +
                                                              "        <routingGroup>DK</routingGroup>\n" +
                                                              "        <shard>24</shard>\n" +
                                                              "        <locale>da_DK</locale>\n" +
                                                              "        <signalFilterId>1</signalFilterId>\n" +
                                                              "        <deleted>false</deleted>\n" +
                                                              "        <cid>11111111</cid>\n" +
                                                              "        <street>somewherevej 33</street>\n" +
                                                              "        <streetNo1></streetNo1>\n" +
                                                              "        <streetNo2></streetNo2>\n" +
                                                              "        <alias>Somewherevej</alias>\n" +
                                                              "    </installation>\n" +
                                                              "</response>")));

        session.login();
        session.retrieveInstallations();
    }


    @Test
    public void testSetSmartPlug() {
        try {
            session.login();
            session.setSmartPlug(GIID, SMARTPLUG, true);
            session.logout();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSetDoorLock() {
        try {
            session.login();
            session.setDoorLock(GIID, DOOR_LOCK, PIN, LockStatus.LOCKED);
            stubFor(put(urlEqualTo("/installation/" + GIID + "/device/" + DOOR_LOCK.replace(" ", "%20") + "/lock"))
                            .withRequestBody(equalToJson("{\"code\": \"" + PIN + "\"}", true, true))
                            .willReturn(aResponse()
                                                .withStatus(400)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\n" +
                                                                  "\t\"errorCode\": \"VAL_00819\",\n" +
                                                                  "\t\"errorGroup\": \"BAD_REQUEST\",\n" +
                                                                  "\t\"errorMessage\": \"The requested doorlock state is not possible to apply due " +
                                                                  "to state already set\"\n" +
                                                                  "}")));
            session.setDoorLock(GIID, DOOR_LOCK, PIN, LockStatus.LOCKED);

            session.setDoorLock(GIID, DOOR_LOCK, PIN, LockStatus.UNLOCKED);
            stubFor(put(urlEqualTo("/installation/" + GIID + "/device/" + DOOR_LOCK.replace(" ", "%20") + "/unlock"))
                            .withRequestBody(equalToJson("{\"code\": \"" + PIN + "\"}", true, true))
                            .willReturn(aResponse()
                                                .withStatus(400)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\n" +
                                                                  "\t\"errorCode\": \"VAL_00819\",\n" +
                                                                  "\t\"errorGroup\": \"BAD_REQUEST\",\n" +
                                                                  "\t\"errorMessage\": \"The requested doorlock state is not possible to apply due " +
                                                                  "to state already set\"\n" +
                                                                  "}")));
            session.setDoorLock(GIID, DOOR_LOCK, PIN, LockStatus.LOCKED);
            session.logout();
        } catch (Exception e) {
            fail();
        }
    }

    private void setupWireMock() {
        stubFor(post(urlEqualTo("/cookie"))
                        .withBasicAuth("CPE/" + USERNAME, PASSWORD)
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("{\"cookie\": \"" + COOKIE + "\"}")));

        stubFor(delete(urlEqualTo("/cookie"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("{\"cookie\": \"" + COOKIE + "\"}")));

        stubFor(get(urlEqualTo("/installation/" + GIID + "/overview"))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBodyFile("installation-overview.json")));

        stubFor(put(urlEqualTo("/installation/" + GIID + "/armstate/code"))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .withRequestBody(equalToJson("{\"state\": \"" + ARM_STATUS.id + "\", \"code\": \"" + PIN + "\"}", true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("{\"armStateChangeTransactionId\": \"11111111\"}")));

        stubFor(post(urlEqualTo("/installation/" + GIID + "/smartplug/state"))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .withRequestBody(equalToJson("[{\"deviceLabel\": \"SMRT PLG1\",\"state\": true}]", true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)));

        stubFor(put(urlEqualTo("/installation/" + GIID + "/device/" + DOOR_LOCK.replace(" ", "%20") + "/lock"))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .withRequestBody(equalToJson("{\"code\": \"" + PIN + "\"}", true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)));

        stubFor(put(urlEqualTo("/installation/" + GIID + "/device/" + DOOR_LOCK.replace(" ", "%20") + "/unlock"))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .withRequestBody(equalToJson("{\"code\": \"" + PIN + "\"}", true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)));


        stubFor(get(urlEqualTo("/installation/search?email=" + USERNAME))
                        .withCookie(COOKIE_NAME, equalTo(COOKIE))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBodyFile("installations.json")));
    }

    private void setupVerisureSession() {
        session = new VerisureSession(VerisureUrls.withBaseUrl(BASE_URL), USERNAME, PASSWORD);
    }
}