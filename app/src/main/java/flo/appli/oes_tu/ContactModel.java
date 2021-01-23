/**
 * Application de géolocalisation
 * @author Florian VERNIERES
 * @version 1.0
 */
package flo.appli.oes_tu;

/**
 *
 */
public class ContactModel {
    /** Nom et numéro de téléphone de d'une personne du répertoire */
    private String name, number;

    /**
     *  getter sur le nom de la personne
     * @return name, le nom de la personne
     */
    public String getName() {
        return name;
    }

    /**
     * setter sur le nom de la personne (change son nom)
     * @param name le nom de la personne du répertoire
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter sur le nom de la personne
     * @return number, le numero de la personne
     */
    public  String getNumber() {
        return number;
    }

    /**
     * setter sur le numéro d'une personne (change le numero)
     * @param number le numero de la personne que l'on souhaite changer
     */
    public void setNumber(String number) {
        this.number = number;
    }
}
