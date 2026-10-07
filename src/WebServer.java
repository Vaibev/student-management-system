import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class WebServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Route: Serve frontend HTML
        server.createContext("/", new StaticFileHandler());

        // Route: Get all students (JSON)
        server.createContext("/api/students", new GetStudentsHandler());

        // Route: Add student
        server.createContext("/api/add", new AddStudentHandler());

        // Route: Delete student
        server.createContext("/api/delete", new DeleteStudentHandler());

        server.setExecutor(null);
        System.out.println("==================================================");
        System.out.println("🚀 Web Application running at: http://localhost:8080");
        System.out.println("==================================================");
        server.start();
    }

    // Serves the index.html page
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File htmlFile = new File("web/index.html");
            if (!htmlFile.exists()) {
                String error = "index.html not found in 'web' folder.";
                exchange.sendResponseHeaders(404, error.length());
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
                return;
            }

            byte[] bytes = Files.readAllBytes(Paths.get("web/index.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // GET /api/students -> Returns JSON array from MySQL
    static class GetStudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            String sql = "SELECT * FROM students";

            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format("{\"id\":%d,\"reg\":\"%s\",\"name\":\"%s\",\"dept\":\"%s\"}",
                            rs.getInt("student_id"),
                            rs.getString("reg_number"),
                            rs.getString("full_name"),
                            rs.getString("department")));
                    first = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            json.append("]");

            byte[] responseBytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    // POST /api/add -> Inserts student into MySQL
    static class AddStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange.getRequestBody());
                String sql = "INSERT INTO students (reg_number, full_name, department) VALUES (?, ?, ?)";

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, params.get("reg"));
                    pstmt.setString(2, params.get("name"));
                    pstmt.setString(3, params.get("dept"));
                    pstmt.executeUpdate();

                    exchange.sendResponseHeaders(200, -1);
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(500, -1);
                }
            }
        }
    }

    // POST /api/delete -> Deletes student from MySQL
    static class DeleteStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange.getRequestBody());
                String sql = "DELETE FROM students WHERE reg_number = ?";

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, params.get("reg"));
                    pstmt.executeUpdate();

                    exchange.sendResponseHeaders(200, -1);
                } catch (SQLException e) {
                    exchange.sendResponseHeaders(500, -1);
                }
            }
        }
    }

    // Helper method to parse URL encoded form data
    private static Map<String, String> parseFormData(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line = reader.readLine();
        Map<String, String> map = new HashMap<>();
        if (line != null) {
            String[] pairs = line.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
        }
        return map;
    }
}