package prevision;

import bateau.Bateau;
import connection.BddObject;
import facture.Facture;
import port.Quai;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Escale extends Proposition {

    Prestation[] prestations;
    Prestation[] listePrestation;
    Quai[] quais;
    Double cours;
    String idDebut;
    Facture[] factures;

    public void setFactures(Facture[] factures) {
        this.factures = factures;
    }

    public Facture[] getFactures() {
        return factures;
    }

    public String getIdDebut() {
        return idDebut;
    }

    public void setIdDebut(String idDebut) {
        this.idDebut = idDebut;
    }

    public void setPrestations(Prestation[] prestations) {
        this.prestations = prestations;
    }

    public Prestation[] getPrestations() {
        return prestations;
    }

    public Quai[] getQuais() {
        return quais;
    }
    public void setQuais(Quai[] quais) {
        this.quais = quais;
    }

    public void setListePrestation(Prestation[] prestations) {
        this.listePrestation = prestations;
    }

    public Prestation[] getListePrestation() {
        return listePrestation;
    }

    public Escale() throws Exception {}

    public Escale(Bateau bateau, Timestamp arrive, Timestamp depart, String reference) throws Exception {
        this.setBateau(bateau);
        this.setArrive(arrive);
        this.setDepart(depart);
        setReference(reference);
    }

    public boolean enCours() {
        return this.getDepart() == null;
    }

    public void ajouterPrestation(Prestation prestation) throws Exception {
        Connection connection = null;
        try {
            if (this.contains(prestation)) throw new Exception("Prestation deja ajouter");
            connection = BddObject.getPostgreSQL();
            prestation.setDebut(this.getArrive());
            prestation.setFin(this.getDepart());
            prestation.setEscale(this);
            prestation.setEtat(1);
            prestation.setPrix(prestation.getPrix(connection));
            prestation.insert(connection);
            connection.commit();
        }  catch (Exception e) {
            if (connection != null) connection.rollback();
            throw e;
        } finally {
            if (connection != null) connection.close();
        }
    }

    public boolean contains(Prestation prestation) {
        for (Prestation p : this.getListePrestation()) {
            if (p.getIdPrestation().equals(prestation.getIdPrestation())) return true;
        }
        return false;
    }

    public void setCours(double value) throws Exception{
        if( value < 0 ){
            throw new Exception("The value of cours can't be null");
        }
        this.cours = value;
    }

    public void setCours(String value) throws Exception {
        this.setCours(Double.valueOf(value));
    }

    public double getCours() {
        return this.cours;
    }

    public Escale(String reference, Timestamp debut, Timestamp fin, double cours) throws Exception {
        this.setReference(reference);
        this.setArrive(debut);
        this.setDepart(fin);
        this.setCours(cours);
    }

    public Escale[] findAll(Connection connection, String order) throws Exception {
        String sql = "SELECT * FROM v_escale";
        ArrayList<Escale> escales = new ArrayList<Escale>();
        Statement st = connection.createStatement();
        ResultSet set = st.executeQuery( sql );
        while( set.next() ) {
            Escale escale = new Escale(set.getString("reference"), set.getTimestamp("debut"), set.getTimestamp("fin"), set.getDouble("cours"));
            escale.setBateau( set.getString("idBateau") , connection);
            escales.add(escale);
        }
        st.close();
        return escales.toArray( new Escale[ escales.size() ] );
    }

    public boolean contains(Connection connection) throws Exception {
        for (Escale escale : this.findAll(connection, null)) {
            if (escale.getReference().equals(this.getReference()))
                return true;
        }
        return false;
    }

    public void debuter() throws Exception {
        DebutEscale debutEscale = new DebutEscale(this.getReference(), this.getArrive());
        Connection connection = null;
        try {
            connection = BddObject.getPostgreSQL();
            if (this.contains(connection)) throw new Exception("Vous avez deja debuter cette escale"); 
            debutEscale.setIdDebut(debutEscale.buildPrimaryKey(connection));
            debutEscale.insert(connection);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }
    }

    public static Escale getByReference( Connection connection, String reference ) throws Exception{
        String sql = "SELECT * FROM v_escale WHERE reference='%s'";
        Statement st = connection.createStatement();
        ResultSet set = st.executeQuery( String.format(sql, reference) );
        set.next();
        Escale escale = new Escale(set.getString("reference"), set.getTimestamp("debut"), set.getTimestamp("fin"), set.getDouble("cours"));
        escale.setBateau( set.getString("idBateau") , connection);
        escale.setIdDebut(set.getString("id_debut"));
        return escale;
    }

    public static Escale createEscale(String idQuai, String reference) throws Exception {
        Escale escale = null;
        try (Connection connection = BddObject.getPostgreSQL()) {
            escale = Escale.getByReference(connection, reference);
            escale.setPrestations(new Prestation().findAll(connection, null));
            escale.setQuais(new Quai().findAll(connection, null));
            escale.setListePrestation(escale.getPrestations(connection, idQuai));
            escale.setFactures(escale.getFactures(connection));
            escale.setQuai(idQuai);
        }
        return escale;
    }
    
    public static Escale createEscale(String reference) throws Exception {
        Escale escale = null;
        try (Connection connection = BddObject.getPostgreSQL()) {
            escale = Escale.getByReference(connection, reference);
            escale.setPrestations(new Prestation().findAll(connection, null));
            escale.setQuais(new Quai().findAll(connection, null));
            escale.setListePrestation(escale.getPrestations(connection));
            escale.setFactures(escale.getFactures(connection));
        }
        return escale;
    }

    public boolean containsFacture() {
        for (Facture facture : factures) {
            if (facture.getEtat() >= 10) return true;
        }
        return false;
    }

    public Facture facturer() throws Exception {
        if (this.enCours()) throw new Exception("Escale est encore en cours");
        if (this.containsFacture()) throw new Exception("Il y a deja une facture deja validee pour cette escale");
        Facture facture = new Facture();
        facture.setEscale(this);
        ArrayList<Facture> factures = new ArrayList<>();
        for (Prestation prestation : this.getListePrestation()) {
            if (prestation.getEtat() >= 10) {
                prestation.setPrix(prestation.getPrix() * this.getCours());
                factures.add(new Facture(prestation));
            }
        }
        facture.setFactures(factures);
        return facture;
    }

    public Prestation[] getPrestations(Connection connection, String quai) throws Exception {
        String sql = "SELECT * FROM v_escale_prestation WHERE id_quai='%s' AND reference='%s'";
        ArrayList<Prestation> prestations = new ArrayList<>();
        Statement st = connection.createStatement();
        ResultSet set = st.executeQuery( String.format(sql, quai, this.getReference()) );
        while( set.next() ) {
            Prestation prestation = new Prestation(set.getString("id_prestation"), set.getString("nom"), set.getTimestamp("debut"), set.getTimestamp("fin"), set.getDouble("prix"), set.getInt("etat"), this);
            prestation.setId(set.getString("id_escale_prestation"));
            prestations.add(prestation);
        }
        st.close();
        return prestations.toArray( new Prestation[ prestations.size() ] );
    }
    
    public Prestation[] getPrestations(Connection connection) throws Exception {
        String sql = "SELECT * FROM v_escale_prestation WHERE reference='%s'";
        ArrayList<Prestation> prestations = new ArrayList<>();
        Statement st = connection.createStatement();
        ResultSet set = st.executeQuery( String.format(sql, this.getReference()) );
        while( set.next() ) {
            Escale escale = new Escale();
            escale.setReference(set.getString("reference"));
            escale.setQuai(set.getString("id_quai"), connection);
            Prestation prestation = new Prestation(set.getString("id_prestation"), set.getString("nom"), set.getTimestamp("debut"), set.getTimestamp("fin"), set.getDouble("prix"), set.getInt("etat"), escale);
            prestation.setId(set.getString("id_escale_prestation"));
            prestations.add(prestation);
        }
        st.close();
        return prestations.toArray( new Prestation[ prestations.size() ] );
    }
    
    public Prestation getById(Connection connection, String id) throws Exception {
        boolean open = false;
        if ( connection == null ) { connection = BddObject.getPostgreSQL(); open = true; }
        Statement st = connection.createStatement();
        ResultSet set = st.executeQuery(String.format("SELECT * FROM v_escale_prestation WHERE id_escale_prestation='%s'", id));
        set.next();
        Prestation prestation = new Prestation(set.getString("id_prestation"), set.getString("nom"), set.getTimestamp("debut"), set.getTimestamp("fin"), set.getDouble("prix"), set.getInt("etat"), this);
        prestation.setId(set.getString("id_escale_prestation"));    
        st.close();
        set.close();
        if ( open ) { connection.commit(); connection.close(); }
        return prestation;
    }

    public void finir(String end) throws Exception {
        Connection connection = null;
        try {
            connection = BddObject.getPostgreSQL();
            FinEscale fin = new FinEscale(this.getIdDebut(), Prevision.toDate(end), this.getCours());
            fin.setIdFin(buildPrimaryKey(connection));
            fin.insert(connection);
            connection.commit();
        } catch(Exception e) {
            if (connection != null) connection.commit();
            throw e;
        } finally {
            if (connection != null) connection.close();
        }
    }

    public Facture[] getFactures(Connection connection) throws Exception {
        String sql = "SELECT * FROM facture WHERE reference='%s'";
        ArrayList<Facture> factures = new ArrayList<>();
        Statement st = connection.createStatement();
        ResultSet set = st.executeQuery( String.format(sql, this.getReference()) );
        while( set.next() ) {
            factures.add(new Facture(set.getString("id_facture"), set.getString("reference"), set.getTimestamp("date"), set.getInt("etat")));
        }
        st.close();
        return factures.toArray(new Facture[factures.size()]);
    }

    public void now() throws Exception {
        Timestamp zao=new Timestamp(System.currentTimeMillis()+6000);
        System.out.println(zao.toString());
        setArrive(zao);
        setDepart(zao);
    }
}