package org.openhab.binding.verisure.internal;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VerisureUrlsTest {

    @Test
    public void retrieveUrl_OK() throws MalformedURLException {
        VerisureUrls verisureUrls = VerisureUrls.withBaseUrl("https://e-api02.verisure.com/xbn/2");
        URL expectedUrl = new URL("https://e-api02.verisure.com/xbn/2/cookie");
        assertEquals(expectedUrl, verisureUrls.login());

        verisureUrls = VerisureUrls.withBaseUrl("http://localhost:9090");
        expectedUrl = new URL("http://localhost:9090/cookie");
        assertEquals(expectedUrl, verisureUrls.login());
    }

    @Test
    public void retrieveUrl_BaseUrlContainsTrailingSlash() throws MalformedURLException {
        VerisureUrls verisureUrls = VerisureUrls.withBaseUrl("https://e-api02.verisure.com/xbn/2");
        URL expectedUrl = new URL("https://e-api02.verisure.com/xbn/2/cookie");
        assertEquals(expectedUrl, verisureUrls.login());
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrieveUrl_BaseUrlContainsQueryParameters() throws MalformedURLException {
        VerisureUrls.withBaseUrl("https://e-api02.verisure.com/xbn/2?something=strange");
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrieveUrl_BaseUrlContainsReference() throws MalformedURLException {
        VerisureUrls.withBaseUrl("https://e-api02.verisure.com/xbn/2#somewhere");
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrieveUrl_NoProtocol() throws MalformedURLException {
        VerisureUrls.withBaseUrl("e-api02.verisure.com/xbn/2?something=strange");
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrieveUrl_WrongProtocol() throws MalformedURLException {
        VerisureUrls.withBaseUrl("ftp://e-api02.verisure.com/xbn/2?something=strange");
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrieveUrl_Nonsense() throws MalformedURLException {
        VerisureUrls.withBaseUrl("somewhere@here");
    }
}
