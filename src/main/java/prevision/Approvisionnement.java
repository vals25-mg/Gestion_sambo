package prevision;

import java.sql.Connection;

public class Approvisionnement extends Prestation{
    public Approvisionnement() throws Exception {
    }
    public Approvisionnement(double litre) throws Exception {
        setLitre(litre);
    }


    double litre;

    public double getLitre() {
        return litre;
    }

    public void setLitre(double litre) {
        this.litre = litre;
    }

    public void setLitre(String litre) throws Exception {
        this.setLitre(Double.valueOf(litre));
    }

    @Override
    public Double getPrix(Connection connection) throws Exception {
//        if(getLitre()==)
        this.setTarifs(this.getTarifs(connection));
        return this.getTarifs()[0].getPrixTotal()*this.getLitre();
    }

    @Override
    public double getDuree() {
        return 0.0;
    }
}
