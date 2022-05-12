package edu.mns.locmns.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Materiel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMateriel;         // Integer car c'est un objet il peut contenir la valeur Null contrairement a int


    private String numeroSerie;

    public Integer getIdMateriel() {
        return idMateriel;
    }

    public void setIdMateriel(Integer idMateriel) {
        this.idMateriel = idMateriel;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public LieuStockage getLieuStockage() {
        return lieuStockage;
    }

    public void setLieuStockage(LieuStockage lieuStockage) {
        this.lieuStockage = lieuStockage;
    }

    public Modele getModele() {
        return modele;
    }

    public void setModele(Modele modele) {
        this.modele = modele;
    }

    public Etat getEtat() {
        return etat;
    }

    public void setEtat(Etat etat) {
        this.etat = etat;
    }

    public List<Document> getListdocument() {
        return listdocument;
    }

    public void setListdocument(List<Document> listdocument) {
        this.listdocument = listdocument;
    }

    public List<Emprunt> getEmprunt() {
        return emprunt;
    }

    public void setEmprunt(List<Emprunt> emprunt) {
        this.emprunt = emprunt;
    }

    public List<Dysfonctionnement> getDysfonctionnement() {
        return dysfonctionnement;
    }

    public void setDysfonctionnement(List<Dysfonctionnement> dysfonctionnement) {
        this.dysfonctionnement = dysfonctionnement;
    }

    @ManyToOne
    @JoinColumn(name="id_lieu_stockage")
    private LieuStockage lieuStockage;

    @ManyToOne
    @JoinColumn(name="id_modele")
    private Modele modele;

    @ManyToOne
    @JoinColumn(name="id_etat")
    private Etat etat;

    @ManyToMany
    @JoinTable(
            name="fournit",
            joinColumns = @JoinColumn(name="id_materiel"),
            inverseJoinColumns = @JoinColumn(name="id_document")

    )
    private List<Document> listdocument = new ArrayList<>();

    @OneToMany(mappedBy = "materiel")
    private List<Emprunt> emprunt = new ArrayList<>();

    @OneToMany(mappedBy = "materiel")
    private List<Dysfonctionnement> dysfonctionnement = new ArrayList<>();



}
