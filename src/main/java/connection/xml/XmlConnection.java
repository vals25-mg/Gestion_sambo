package connection.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XmlConnection {
    
    String host;
    String port;
    String database;
    String user;
    String password;

    public void setHost(String host) throws Exception {
        if (host == null) throw new Exception("Host est null");
        if (host.isEmpty()) throw new Exception("Host est vide");
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(String port) throws Exception {
        if (port == null) throw new Exception("Port est null");
        if (port.isEmpty()) throw new Exception("Port est vide");
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setDatabase(String database) throws Exception {
        if (database == null) throw new Exception("Database name est null");
        if (database.isEmpty()) throw new Exception("Database name est vide");
        this.database = database;
    }

    public String getDatabase() {
        return database;
    }

    public void setPassword(String password) throws Exception {
        if (password == null) throw new Exception("Database name est null");
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUser(String user) throws Exception {
        if (user == null) throw new Exception("Database name est null");
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public XmlConnection(String host, String port, String database, String user, String password) throws Exception {
        this.setHost(host);
        this.setPort(port);
        this.setDatabase(database);
        this.setUser(user);
        this.setPassword(password);
    }

    public static XmlConnection createConnection(String product) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new File("./config.xml")); // Fichier de configuration .xml
        document.getDocumentElement().normalize(); // Pour normaliser les textes des elements
        NodeList list = document.getElementsByTagName("connection"); // root du fichier .xml
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            Element element = (Element) node;
            String name = element.getAttribute("name"); // Nom de la base de donnee
            if (name.equals(product)) {
                String host = element.getElementsByTagName("host").item(0).getTextContent();
                String port = element.getElementsByTagName("port").item(0).getTextContent();
                String database = element.getElementsByTagName("database").item(0).getTextContent();
                String user = element.getElementsByTagName("user").item(0).getTextContent();
                String password = element.getElementsByTagName("password").item(0).getTextContent();
                return new XmlConnection(host, port, database, user, password);
            }
        }
        throw new Exception("Configuration de Base de donnee introuvable");
    }

}
