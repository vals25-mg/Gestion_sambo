package connection;

import connection.annotation.ColumnName;
import connection.annotation.ForeignKey;
import connection.annotation.NotNull;
import connection.annotation.PrimaryKey;

import java.lang.reflect.Field;

public class Column {
    
    String name;
    Field field;
    boolean primaryKey = false;
    boolean foreignKey = false;
    boolean notNull = false;

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setForeignKey(boolean foreignKey) {
        this.foreignKey = foreignKey;
    }

    public boolean isForeignKey() {
        return foreignKey;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public Column(Field field) {
        this.setField(field);
        this.setName(field.getName());
        if (field.isAnnotationPresent(PrimaryKey.class)) {
            this.setPrimaryKey(true);
            this.setNotNull(true);
        }
        if (field.isAnnotationPresent(ForeignKey.class)) {
            this.setForeignKey(true);
            this.setNotNull(true);
        }
        if (field.isAnnotationPresent(NotNull.class)) this.setNotNull(true);
        if (field.isAnnotationPresent(ColumnName.class)) {
            this.setName(field.getAnnotation(ColumnName.class).value());
        }
    }

}
