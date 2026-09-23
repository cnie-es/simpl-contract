package eu.europa.ec.simpl.contracts.utils;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebServerUtils {

    public static MockWebServer createMockWebServer(Dispatcher dispatcher) throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start(8082);
        mockWebServer.setDispatcher(dispatcher);
        return mockWebServer;
    }

    public static String getFileContent(String resourcePath) {
        ClassLoader classLoader = WebServerUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
        StringBuilder response = new StringBuilder();
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    response.append("\n");
                }
            } catch (IOException e) {
                return "";
            }
        }
        return response.toString();
    }
}
