package com.borekkst.armshands;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ApiManager {
    private final ArmsHands plugin;
    private HttpServer server;
    public ApiManager(ArmsHands plugin) { this.plugin = plugin; }
    public void startApi() {
        if (!plugin.getConfig().getBoolean("settings.enable-internal-api", true)) return;
        int port = plugin.getConfig().getInt("api-port", 8080);
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/status", new StatusHandler(plugin));
            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("HTTP API запущен на порту " + port);
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка запуска API: " + e.getMessage());
        }
    }
    public void stopApi() { if (server != null) server.stop(0); }
    static class StatusHandler implements HttpHandler {
        private final ArmsHands plugin;
        public StatusHandler(ArmsHands plugin) { this.plugin = plugin; }
        public void handle(HttpExchange exchange) throws IOException {
            String resp = "{\"status\":\"online\",\"plugin\":\"ArmsHands\",\"players\":" + plugin.getServer().getOnlinePlayers().size() + "}";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, resp.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }
    }
}
