package connection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("unchecked")
public abstract class BddObject<T extends BddObject<?>> {

/// Field
    String prefix; // Prefix de L'ID de cette Object
    String functionPK; // fonction PlSQL pour prendre la sequence
    String table; // table de cette objec
    int countPK; // nombre de caractere de l'ID
    String connection;
    List<Column> columns;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

/// Getters
    public String getPrefix() { 
        if (prefix == null) setPrefix("");
        return prefix;
    }

    public Connection getConnection() throws Exception {
        Connection c = null;
        switch (this.connection) {
            case "PostgreSQL":
                c = getPostgreSQL();
                break;
            case "Oracle":
                c = getOracle();
                break;
        }
        return c;
    }

    public String getTable() throws NullPointerException {
        if (this.table == null) throw new NullPointerException("Pas de table pour l'object de type " + this.getClass().getSimpleName());
        return table;
    }

    public String getFunctionPK() throws NullPointerException {
        if (functionPK == null) throw new NullPointerException("Pas de fonction de sequence pour l'object de type " + this.getClass().getSimpleName());
        return functionPK;
    }
    
    public int getCountPK() { return countPK; }
    
/// Setters
    public void setConnection(String connection) throws Exception {
        this.connection = connection;
    }
    public void setTable(String table) { this.table = table; }
    public void setCountPK(int countPK) throws IllegalArgumentException {
        if (countPK < 0) throw new IllegalArgumentException("Count ne doit pas etre négative de type " + this.getClass().getSimpleName());
        this.countPK = countPK;
    }
    public void setFunctionPK(String function) { this.functionPK = function; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

/// Constructor
    public BddObject() {
        this.initColumns();
    }
/// Functions

    public void initColumns() {
        Vector<Column> columns = new Vector<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            columns.add(new Column(field));
        }
        this.setColumns(columns);
    }

/// Fonction pour prendre un connexion en Oracle
    // ! Configuration de la base de donnee dans un fichier xml
    public static Connection getOracle() throws SQLException, ClassNotFoundException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.88.21:1521:orcl", "scott", "tiger");
        connection.setAutoCommit(false);
        return connection;
    }

/// Fonction pour prendre un connexion en PostgreSQL
    public static Connection getPostgreSQL() throws Exception {
        Class.forName("org.postgresql.Driver");
//        XmlConnection config = XmlConnection.createConnection("PostgreSQL");
        String configuration = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", "localhost", "5432", "gestion_port", "valisoa", "Valis0a!w");
        Connection connection = DriverManager.getConnection(configuration);
        connection.setAutoCommit(false);
        return connection;
    }

/// Prendre des données dans la base de données avec "SELECT"
    public T[] getData(Connection connection, String order) throws Exception {
        String sql = "SELECT * FROM " + this.getTable() + predicat(); // Requete SQL avec les pedicats si nécessaire
        if (order != null) sql += " ORDER BY " + order;
        return this.getData(sql, connection);
    }

/// Tous requete peut etre en input sur cette fonction
    public T[] getData(String query, Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        String[] liste = listColumn(query, connection); // get liste of column length
        T[] employees = convertToObject(result, liste.length, connection);
        result.close();
        statement.close();
        return employees;
    }

/// Fonction pour prendre les listes de colonnes dans un requete
    public static String[] listColumn(String query, Connection connection) throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData rsMetaData = rs.getMetaData(); // Classe avec des données plus détaillé de la requete
        int count = rsMetaData.getColumnCount();
        String[] colonnes = new String[count];
        int increment = 0;
        for(int i = 1; i <= count; i++) {
            colonnes[increment] = rsMetaData.getColumnName(i);
            increment++;
        }
        return colonnes;
    }

/// Convertir les réponse SQL en Object (T[])
    public T[] convertToObject(ResultSet result, int attribut, Connection connection) throws Exception {
        Vector<T> objects = new Vector<T>(); // Initialisation du vector pour sauver les donnees
        Class<T> typeClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        while (result.next()) {
            T object = typeClass.getConstructor().newInstance(); // Nouveau instance de l'object qui hérite ce BddObject
            for (int i = 0; i < attribut; i++) {
                Object value = null;
                if (columns.get(i).isForeignKey()) {
                    BddObject<?> fkObject = (BddObject<?>) columns.get(i).getField().getType().getConstructor().newInstance();
                    Column primaryKey = fkObject.getFieldPrimaryKey();
                    Method setter = fkObject.getClass().getDeclaredMethod("set" + toUpperCase(primaryKey.getField().getName()), primaryKey.getField().getType());
                    Object dbValue = ResultSet.class.getMethod("get" + toUpperCase(primaryKey.getField().getType().getSimpleName()), String.class).invoke(result, primaryKey.getName());
                    setter.invoke(fkObject, dbValue);
                    value = fkObject.getById(connection);
                } else {
                    value = ResultSet.class.getMethod("get" + toUpperCase(columns.get(i).getField().getType().getSimpleName()), int.class).invoke(result, i + 1);
                }
                this.getClass().getMethod("set" + toUpperCase(columns.get(i).getField().getName()), columns.get(i).getField().getType()).invoke(object, value);
            }
            objects.add(object);
        }
        return objects.toArray((T[]) Array.newInstance(typeClass, objects.size())); // Fonction pour creer un tableau avec le generic
    }

    public String predicat() throws Exception {
        String sql = " WHERE "; // Condition with AND clause
        for (Column column : this.getColumns()) {
            String predicat = column.getName();
            Object value = this.getClass().getMethod("get" + toUpperCase(column.getField().getName())).invoke(this);
            if (value != null) {
                if (value instanceof BddObject) {
                    predicat = ((BddObject<?>) value).getFieldPrimaryKey().getName();
                    value = value.getClass().getMethod("get" + toUpperCase(((BddObject<?>) value).getFieldPrimaryKey().getField().getName())).invoke(value);
                }
                sql += predicat + "=" + convertToLegal(value) + " AND ";
            }
        }
        return sql.substring(0, sql.length() - 5); // Delete last " AND " in sql
    }
    
    public void insert(Connection connection) throws Exception {
        boolean connect = false;
        if (connection == null) {connection = getConnection(); connect = true;}
        String[] liste = listColumn("SELECT * FROM " + this.getTable(), connection);
        String sql = "INSERT INTO " + this.getTable() + " " + createColumn() + " VALUES ("; // Insert with all column
        for (int i = 0; i < liste.length; i++) {
            Object value = this.getClass().getMethod("get" + toUpperCase(columns.get(i).getField().getName())).invoke(this);
            if (value == null && columns.get(i).isNotNull()) throw new Exception(columns.get(i).getField().getName() + "is null");
            if (columns.get(i).isForeignKey())
                value = value.getClass().getMethod("get" + toUpperCase(((BddObject<?>) value).getFieldPrimaryKey().getField().getName())).invoke(value);
            sql += convertToLegal(value) + ",";
        }
        sql = sql.substring(0, sql.length() - 1) + ")";
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
        if (connect) {connection.commit(); connection.close();}
    }
    
    public String createColumn() throws Exception {
        String result = "(";
        for (Column colonne : this.getColumns()) {
            String name = colonne.getName();
            if (colonne.isForeignKey()) {
                BddObject<?> fk = (BddObject<?>) colonne.getField().getType().getConstructor().newInstance();
                name = fk.getFieldPrimaryKey().getName();
            }
            result += name  + ",";
        }
        result = result.substring(0, result.length() - 1) + ")";
        return result;
    }

    @Deprecated
    public void update(String[] column, Object[] value, String ID, Connection connection) throws Exception {
        if (value.length != column.length) throw new Exception("Value and column must be equals");
        boolean connect = false;
        if (connection == null) {connection = getConnection(); connect = true;}
        Statement statement = connection.createStatement();
        String sql = "UPDATE " + this.getTable() + " \nSET ";
        for (int i = 0; i < column.length; i++)
            sql += column[i] + " = " + convertToLegal(value[i]) + ",\n";
        sql = sql.substring(0, sql.length() - 2);
        sql += " WHERE " + ID + " = " + convertToLegal(this.getClass().getMethod("get" + toUpperCase(ID)).invoke(this));
        statement.executeUpdate(sql);
        statement.close();
        if (connect) {connection.commit(); connection.close();}
    }

    public String convertToLegal(Object args) {
        return (args == null) ? "null"
        : (args.getClass() == java.util.Date.class) ? "TO_TIMESTAMP('"+ new Timestamp(((java.util.Date) args).getTime()) +"', 'YYYY-MM-DD HH24:MI:SS.FF')"
        : (args.getClass() == Date.class) ? "TO_DATE('" + args + "', 'YYYY-MM-DD')"
        : (args.getClass() == Timestamp.class) ? "TO_TIMESTAMP('"+ args +"', 'YYYY-MM-DD HH24:MI:SS.FF')"
        : ((args.getClass() == String.class) || (args.getClass() == Time.class)) ? "'"+ args +"'"
        : args.toString();
    }

    public static String toUpperCase(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String buildPrimaryKey(Connection connection) throws SQLException {
        return (getPrefix() == null) ? getSequence(connection) : this.getPrefix() + completeZero(getSequence(connection), this.getCountPK() - this.getPrefix().length());
    }

    public String getSequence(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        String sql = (connection.getMetaData().getDatabaseProductName().equals("PostgreSQL")) ? "SELECT " + this.getFunctionPK() : "SELECT " + this.getFunctionPK() + " FROM DUAL";
        ResultSet result = statement.executeQuery(sql);
        result.next();
        String sequence = result.getString(1);
        statement.close();
        result.close();
        return sequence;
    }
    
    public static String completeZero(String seq, int count) {
        int length = count - seq.length();
        String zero = "";
        for (int i = 0; i < length; i++)
            zero += "0";
        return zero + seq;
    }

    public Column getFieldPrimaryKey() throws Exception {
        for (Column field : this.getColumns()) {
            if (field.isPrimaryKey()) return field;
        }
        throw new Exception("Il n'y pas de cle primaire dans la classe " + this.getClass().getSimpleName());
    }

    public T[] findAll(Connection connection, String order) throws Exception {
        return this.getData(connection, order);
    }

    public T[] findAll(String order) throws Exception {
        T[] objects = null;
        try (Connection connection = getConnection()) {
            objects = this.findAll(connection, order);
        }
        return objects;
    }

    public T getById(Connection connection) throws Exception {
        Column primaryKey = this.getFieldPrimaryKey();
        if (primaryKey == null) throw new Exception("Pas de cles primaire dans cette classe " + this.getClass().getSimpleName());
        T[] objects = this.getData(connection, null);
        return (objects.length > 0) ? objects[0] : null;
    }

    public T getById() throws Exception {
        T object = null;
        try (Connection connection = getConnection()) {
            object = this.getById(connection);
        }
        return object;
    }
    
}