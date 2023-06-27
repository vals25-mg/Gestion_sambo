package formulaire;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Vector;
// import connection.BddObject;

public class Formulaire extends JPanel {
    
    Object object;
    String title = "";
    Champ[] listeChamp;
    String error = "";
    String action = "";
    Vector<Button> buttons = new Vector<Button>();


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setError(String error) {
        this.error = (error == null) ? "" : error;
    }

    public String getError() {
        return error;
    }

    public Champ[] getListeChamp() {
        return listeChamp;
    }

    public Object getObject() {
        return object;
    }

    public void setListeChamp(Champ[] listeChamp) {
        this.listeChamp = listeChamp;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void setButtons(Vector<Button> buttons) {
        this.buttons = buttons;
    }

    public void addButtons(Button[] buttons) {
        for(Button button : buttons)
            this.buttons.add(button);
    }

    public void addButton(Button button) {
        this.buttons.add(button);
    }
    
    protected Formulaire(Champ[] liste, Object object) {
        super(null);
        setListeChamp(liste);
        setObject(object);
        setBackground(new Color(92, 133, 214));
    }

    /**
     * <p>Function for creating Formulaire of this obj<p>
     * <p>Arguments are fields of this Formulaire<p>
     * @param obj : object to create formulaire
     * @return {@code Formulaire} an formulaire with fields of obj attributes
     * @throws Exception
     */
    public static Formulaire createFormulaire(Object obj) throws Exception {
        Field[] fields = obj.getClass().getDeclaredFields();
        Champ[] champs = new Champ[fields.length];
        if (fields.length == 0) throw new Exception("No argument in obj");
        for (int i = 0; i < fields.length; i++) {
            champs[i] = new Champ(new JTextField(), fields[i]);
        }
        return new Formulaire(champs, obj);
    }
    
    // public static Formulaire createFormulaire(BddObject<?> obj) throws Exception {
    //     Field[] fields = obj.getClass().getDeclaredFields();
    //     Champ[] champs = new Champ[fields.length];
    //     if (fields.length == 0) throw new Exception("No argument in obj");
    //     for (int i = 0; i < fields.length; i++) {
    //         champs[i] = new Champ(new JTextField(), fields[i]);
    //     }
    //     return new Formulaire(champs, obj);
    // }


    /**
     * <p>Validation of our update about champ of this formulaire<p>
     * <p>And make champs and buttons into this formulaire<p>
     */
    public void setPosition(JFrame frame) throws Exception {
        int p = 0;
        int dx = 0;
        for(Champ champ : this.listeChamp) {
            if(champ.isVisible()) {
                champ.getLabel().setBounds(20, 20 + p * 40, 400, 40);
                champ.getChamp().setBounds(160, 20 + p * 40, 300, 40);
                this.add(champ.getLabel());
                this.add(champ.getChamp());
                p++;
            }
        }
        if (buttons.size() == 0) throw new Exception("Any button is added in this formulaire");
        for (Button button : buttons) {
            button.setBounds(160 + dx, 20 + p * 40, button.getWidth(), button.getHeight());
            add(button);
            dx += 100;
        }
        initFrame(frame);
    }

    /**
     * <p>Set this formulaire into frame<p>
     * <p>This is useful to use after {@code setPosition()}<p>
     * @param frame
     */
    public void initFrame(JFrame frame) {
        frame.add(this);
        frame.setSize(600, getListeChamp().length * 80);
        frame.setResizable(false);
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }


    //Check this function
    public String getHTMLString() {
        String html = "<form method=\"post\" action=\"" + this.getAction() + "\" class=\"container w-50 shadow p-5 rounded-3\">";
        html += (!this.getTitle().isEmpty()) ? "<h1 class=\"text-center mb-4\">" + this.getTitle() + "</h1>" : "";
        for (Champ champ : this.getListeChamp()) {
            if (champ.isVisible()) {
                html += "<div class=\"row mt-3\">";
                html += "<h4 class=\"mb-2\">" + champ.getLabel().getText() + "</h4>";
                if(champ.getChamp() instanceof JTextField) {
                    html += "<input type=\"" + champ.getType() + "\" name=\"" + champ.getAttribut().getName() + "\" class=\"form-control\" value=\"" + champ.getDefaultValue() + "\">";
                } else if(champ.getChamp() instanceof DropDown) {
                    html += "<select name=\"" + champ.getAttribut().getName() + "\" class=\"form-select\">\n";
                    DropDown down = (DropDown) champ.getChamp();
                    for (DataListe data : down.getData()) {
                        html += "<option value=\"" + data.getValue() + "\">" + data.getName() + "</option>\n";
                    }
                    html += "</select>\n";
                }
                html += "</div>";
            }
        }
        html += "<input type=\"hidden\" value=\"" + this.getObject().getClass().getName() + "\" name=\"mapping\">";
        html += "<div class=\"row mt-3\">";
        html += "<input class=\"btn btn-success\" type=\"submit\" value=\"Valider\">";
        html += "</div>";
        html += "<h3 class=\"mt-4 text-danger\">" + this.getError() + "</h3>";
        html += "</form>";
        return html;
    }

    /**
     * <p>Create an order into fields formulaire<p>
     * @param orders
     */
    public void setOrdre(String[] orders) {
        Champ[] liste = new Champ[orders.length];
        for (int i = 0; i < orders.length; i++) {
            for (Champ champ : this.listeChamp) {
                if (orders[i].compareTo(champ.getAttribut().getName()) == 0) liste[i] = champ;
            }
        }
        this.listeChamp = liste;
    }

    /**
     * <p>Reset order to origin arguments location<p>
     * @return {@code Champ[]}
     */
    public Champ[] resetOrder() {
        Field[] fields = this.object.getClass().getDeclaredFields();
        String[] order = new String[fields.length];
        for(int i = 0; i < fields.length; i++)
            order[i] = fields[i].getName();
        Formulaire form = new Formulaire(this.listeChamp, this.object);
        form.setOrdre(order);
        return form.getListeChamp();
    }
    
    /**
     * <p>Reset to null all fields (JTextField)<p>
     * 
     */
    public void resetform() {
        Vector<Champ> fields = getTextField();
        for(Champ field : fields) {
            if(field.isVisible()) field.setDefault("");
        }
    }
    
    /**
     * Check if one of this fields is Empty (JTextField) or not
     * @return true if empty else false
     */
    public boolean isEmpty() {
        Vector<Champ> fields = getTextField();
        for(Champ field : fields) {
            JTextField text = (JTextField) field.getChamp();
            if(field.isVisible() && text.getText().isEmpty()) return true;
        }
        return false;
    }

    /**
     * Get text of champs (JTextField or Dropdown)
     * @return value of formulaire
     */
    public String[] getText() {
        Champ[] champs = this.resetOrder();
        String[] fieldsStrings = new String[champs.length];
        for(int i = 0; i < champs.length; i++) {
            if(champs[i].getChamp() instanceof JTextField) {
                JTextField field = (JTextField) champs[i].getChamp();
                fieldsStrings[i] = field.getText();
            } else if(champs[i].getChamp() instanceof DropDown) {
                DropDown box = (DropDown) champs[i].getChamp();
                fieldsStrings[i] = box.getSelectedValue();
            }
        }
        return fieldsStrings;
    }

    /**
     * Get Components that are JTextField 
     * @return Champ with JTextField for Component
     */
    public Vector<Champ> getTextField() {
        Champ[] champs = this.resetOrder();
        Vector<Champ> fields = new Vector<Champ>();
        for(Champ champ : champs) {
            if(champ.getChamp() instanceof JTextField)
                fields.add(champ);
        }
        return fields;
    }
}