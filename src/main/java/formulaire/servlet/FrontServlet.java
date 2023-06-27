package formulaire.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;

public class FrontServlet extends HttpServlet {
    
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        String link = request.getHeader("referer").split("[?]")[0];
        try {
            Class<?> cls = Class.forName(request.getParameter("mapping")); // Prendre le nom de la classe mapping
            Object obj = cls.getConstructor().newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                String value = request.getParameter(field.getName());
                if (value != null) {
                    Class<?> type = field.getType();
                    // Prendre les setters de cette classe
                    Method setter = cls.getMethod("set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1), String.class);
                    setter.invoke(obj, value);
                }
            }
            Method insert = Class.forName("connection.BddObject").getMethod("insert", Connection.class);
            insert.invoke(obj, (Connection) null);
            response.sendRedirect(link);
        } catch (Exception e) {
            response.sendRedirect(link + "?error=" + e.getCause().getMessage());
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        processRequest(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        processRequest(request, response);
    } 

}
