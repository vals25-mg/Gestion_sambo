package formulaire;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;

public class Table extends JPanel {
    Object[] objects;
    
    public Table(Object[] objects) throws Exception {
        this.objects = objects;
        this.add(new JScrollPane(new JTable(new DefaultTableModel(getdata(), createChamp()))));
    }

    public void initFrame(JFrame frame) {
        frame.add(this);
        frame.setSize(800, 500);
        frame.setResizable(false);
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }

    public Object[][] getdata() throws Exception {
        Field[] fields = this.objects[0].getClass().getDeclaredFields();
        Object[][] field = new Object[objects.length][fields.length];
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < fields.length; j++)
                field[i][j] = this.objects[0].getClass().getMethod("get" + Champ.toUpperCasefisrtLetter(fields[j].getName())).invoke(objects[i]);
        }
        return field;
    }

    public String[] createChamp() throws Exception {
        Field[] fields = this.objects[0].getClass().getDeclaredFields();
        String[] champ = new String[fields.length];
        for (int i = 0; i < champ.length; i++)
            champ[i] = fields[i].getName();
        return champ;
    }

    public String getHTMLString() throws Exception {
        Object[][] data = getdata();
        String[] champ = createChamp();
        String html = "<table class=\"table\">\n";
        html += "<tr>\n";
        for (int i = 0; i < champ.length; i++)
            html += "<th>" + champ[i] + "</th>\n";
        html += "</tr>\n";
        for (int i = 0; i < data.length; i++) {
            html += "<tr>\n";
            for (int j=0; j < data[i].length; j++)
                html += "<td>" + data[i][j].toString() + "</td>\n";
            html += "</tr>\n";
        }
        html += "</table>";
        return html;
    }
}