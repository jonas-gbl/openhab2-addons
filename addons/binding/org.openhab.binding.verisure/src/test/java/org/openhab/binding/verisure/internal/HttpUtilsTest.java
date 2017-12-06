package org.openhab.binding.verisure.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openhab.binding.verisure.internal.http.HttpResponse;
import org.openhab.binding.verisure.internal.http.HttpUtils;

@RunWith(JUnit4.class)
public class HttpUtilsTest {

    private static final String EMPTY = "";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9090);
    private String simpleJsonRequest = "{\"searching\": \"something\", \"urgency\": \"now\"}";
    private String simpleJsonPayload = "{\"something\": \"pipotron\", \"quantity\": 1, \"hazardous\": true}";
    private String someHeaderName = "some-header-name";
    private String someHeaderValue = "some-header-value";
    private String someUrlPath = "/some/url/path";
    private URL someUrl;


    @Before
    public void setUp() throws MalformedURLException {
        someUrl = new URL("http", "localhost", 9090, someUrlPath);
    }

    @Test
    public void testGet() throws IOException {

        stubFor(get(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);

        HttpResponse response = HttpUtils.get(someUrl, headers);
        assertEquals(200, response.getStatus());
        assertEquals(simpleJsonPayload, response.getBody());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_NullUrl() throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        HttpUtils.get(null, headers);
    }

    @Test
    public void testGet_EmptyResponse() throws IOException {

        stubFor(get(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .willReturn(aResponse()
                                            .withStatus(200)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);

        HttpResponse response = HttpUtils.get(new URL(someUrl, someUrlPath), headers);
        assertEquals(200, response.getStatus());
        assertEquals(EMPTY, response.getBody());
    }

    @Test
    public void testGet_ErrorResponse() throws IOException {

        stubFor(get(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .willReturn(aResponse()
                                            .withStatus(400)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);

        HttpResponse response = HttpUtils.get(someUrl, headers);
        assertEquals(400, response.getStatus());
        assertEquals(simpleJsonPayload, response.getBody());
    }

    @Test
    public void testPost() throws IOException {

        stubFor(post(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .withRequestBody(equalToJson(simpleJsonRequest, true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        headers.put("Content-Type", "application/json");

        HttpResponse response = HttpUtils.post(someUrl, headers, simpleJsonRequest);
        assertEquals(200, response.getStatus());
        assertEquals(simpleJsonPayload, response.getBody());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPost_NullUrl() throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        HttpUtils.post(null, headers, simpleJsonRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPost_NoContentType() throws IOException {

        stubFor(post(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .withRequestBody(equalToJson(simpleJsonRequest, true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);

        HttpUtils.post(someUrl, headers, simpleJsonRequest);
    }

    @Test
    public void testPost_ErrorResponse() throws IOException {

        stubFor(post(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .withRequestBody(equalToJson(simpleJsonRequest, true, true))
                        .willReturn(aResponse()
                                            .withStatus(400)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        headers.put("Content-Type", "application/json");

        HttpResponse response = HttpUtils.post(someUrl, headers, simpleJsonRequest);
        assertEquals(400, response.getStatus());
        assertEquals(simpleJsonPayload, response.getBody());
    }

    @Test
    public void testPost_EmptyResponse() throws IOException {

        stubFor(post(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .withRequestBody(equalToJson(simpleJsonRequest, true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        headers.put("Content-Type", "application/json");

        HttpResponse response = HttpUtils.post(someUrl, headers, simpleJsonRequest);
        assertEquals(200, response.getStatus());
        assertEquals(EMPTY, response.getBody());
    }

    @Test
    public void testPut() throws IOException {

        stubFor(put(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .withRequestBody(equalToJson(simpleJsonRequest, true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        headers.put("Content-Type", "application/json");

        HttpResponse response = HttpUtils.put(someUrl, headers, simpleJsonRequest);
        assertEquals(200, response.getStatus());
        assertEquals(simpleJsonPayload, response.getBody());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut_NullUrl() throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        HttpUtils.put(null, headers, simpleJsonRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut_NoContentType() throws IOException {

        stubFor(put(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .withRequestBody(equalToJson(simpleJsonRequest, true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);

        HttpUtils.put(someUrl, headers, simpleJsonRequest);
    }

    @Test
    public void testPut_ErrorResponse() throws IOException {

        stubFor(put(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .withRequestBody(equalToJson(simpleJsonRequest, true, true))
                        .willReturn(aResponse()
                                            .withStatus(400)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        headers.put("Content-Type", "application/json");

        HttpResponse response = HttpUtils.put(someUrl, headers, simpleJsonRequest);
        assertEquals(400, response.getStatus());
        assertEquals(simpleJsonPayload, response.getBody());
    }

    @Test
    public void testPut_EmptyResponse() throws IOException {

        stubFor(put(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .withRequestBody(equalToJson(simpleJsonRequest, true, true))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        headers.put("Content-Type", "application/json");

        HttpResponse response = HttpUtils.put(someUrl, headers, simpleJsonRequest);
        assertEquals(200, response.getStatus());
        assertEquals(EMPTY, response.getBody());
    }

    @Test
    public void testDelete() throws IOException {

        stubFor(delete(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);

        HttpResponse response = HttpUtils.delete(someUrl, headers);
        assertEquals(200, response.getStatus());
        assertEquals(simpleJsonPayload, response.getBody());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelete_NullUrl() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);
        HttpUtils.delete(null, headers);
    }

    @Test
    public void testDelete_EmptyResponse() throws IOException {

        stubFor(delete(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .willReturn(aResponse()
                                            .withStatus(200)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);

        HttpResponse response = HttpUtils.delete(someUrl, headers);
        assertEquals(200, response.getStatus());
        assertEquals(EMPTY, response.getBody());
    }

    @Test
    public void testDelete_ErrorResponse() throws IOException {

        stubFor(delete(urlEqualTo(someUrlPath))
                        .withHeader(someHeaderName, equalTo(someHeaderValue))
                        .willReturn(aResponse()
                                            .withStatus(400)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody(simpleJsonPayload)));


        Map<String, String> headers = new HashMap<>();
        headers.put(someHeaderName, someHeaderValue);

        HttpResponse response = HttpUtils.delete(someUrl, headers);
        assertEquals(400, response.getStatus());
        assertEquals(simpleJsonPayload, response.getBody());
    }
}
