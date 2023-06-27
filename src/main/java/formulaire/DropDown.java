package formulaire;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class DropDown extends JComboBox<String> {
    
    DataListe[] data;

    public void setData(DataListe[] data) throws Exception {
        this.data = data;
    }

    public DataListe[] getData() {
        return data;
    }

    public String getSelectedValue() {
        return this.data[this.getSelectedIndex()].getValue();
    }

    public static DataListe[] convertToData(Object[] objects, String valueFunction, String nameFunction) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        DataListe[] data = new DataListe[objects.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = new DataListe();
            data[i].setName((String) objects[i].getClass().getDeclaredMethod(nameFunction).invoke(objects[i]));
            data[i].setValue((String) objects[i].getClass().getDeclaredMethod(valueFunction).invoke(objects[i]));
        }
        return data;
    }

}