package edu.mns.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import edu.mns.locmns.dao.EmpruntDao;
import edu.mns.locmns.dao.MaterielDao;
import edu.mns.locmns.dao.UtilisateurDao;
import edu.mns.locmns.model.Emprunt;
import edu.mns.locmns.model.Gestionnaire;
import edu.mns.locmns.model.Materiel;
import edu.mns.locmns.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
public class EmpruntController {

    private EmpruntDao empruntDao;
    private MaterielDao materielDao;
    private UtilisateurDao utilisateurDao;

    @Autowired
    public EmpruntController(EmpruntDao empruntDao, MaterielDao materielDao, UtilisateurDao utilisateurDao) {
        this.empruntDao = empruntDao;
        this.materielDao = materielDao;
        this.utilisateurDao = utilisateurDao;
    }

    @GetMapping("/gestionnaire/liste-emprunts")
    public List<Emprunt> listeEmprunts(){
        return this.empruntDao.findAll();
    }

    @GetMapping("gestionnaire/emprunt/{idUtilisateur}/{idMateriel}/{dateEmprunt}")
    public Emprunt emprunt(@PathVariable Integer idUtilisateur,
                           @PathVariable Integer idMateriel,
                           @PathVariable String dateEmprunt)
            throws ParseException {
        Date nouvelleDateEmprunt = new SimpleDateFormat("yyyy-MM-dd").parse(dateEmprunt);

        return this.empruntDao.findByUtilisateurIdAndMaterielIdMaterielAndDateEmprunt(idUtilisateur, idMateriel, nouvelleDateEmprunt).orElse(null);
    }

    @DeleteMapping("/gestionnaire/reservation/{idUtilisateur}/{idMateriel}/{dateEmprunt})")
    public String deleteReservation(@PathVariable Integer idUtilisateur, @PathVariable Integer idMateriel, @PathVariable Date dateEmprunt) {
        this.empruntDao.deleteByUtilisateurIdAndMaterielIdMaterielAndDateEmprunt(idUtilisateur, idMateriel, dateEmprunt);
        return "Le mat??riel a bien ??t?? supprim??";
    }

    @PostMapping("/demande-emprunt")
    public String demandeEmprunt (@RequestBody Emprunt emprunt){

        //R??cup??re l'id d'un nouveau mat??riel qui correspond au mod??le souhait??
        Integer idMaterielNouveau = this.materielDao.RechercheNouveauxMaterielDemandeEmprunt(emprunt.getMateriel().getModele().getIdModele());

        //R??cup??re l'id d'un mat??riel d??j?? emprunt??
        Integer idMaterielAncien = this.materielDao.RechercheMaterielDemandeEmprunt(emprunt.getMateriel().getModele().getIdModele(), emprunt.getDateEmprunt());

        //Enregistre la date du jour pour la date de demande
        emprunt.setDateDemandeEmprunt(LocalDateTime.now());

        if(idMaterielNouveau != null){
            emprunt.getMateriel().setIdMateriel(idMaterielNouveau);
            this.empruntDao.save(emprunt);
            return "La demande d'emprunt a ??t?? envoy??e";
        }else if(idMaterielAncien != null){
            emprunt.getMateriel().setIdMateriel(idMaterielAncien);
            this.empruntDao.save(emprunt);
            return "La demande d'emprunt a ??t?? envoy??e";
        }
        return "Il n'y a pas de mat??riel disponible pour ce mod??le";

    }

    @PostMapping("/demande-prolongation")
    public String demandeProlongationEmprunt(@RequestBody Emprunt emprunt){
        Emprunt empruntBdd = empruntDao.findByUtilisateurIdAndMaterielIdMateriel(emprunt.getUtilisateur().getId(), emprunt.getMateriel().getIdMateriel()); //Recup??re les infos post et effectue une recherche pour retrouver l'emprunt

        if(empruntBdd.getDateProlongation() == null) { //V??rifie qu'une demande de prolongation ne soit pas en cours
            empruntBdd.setDateProlongation(emprunt.getDateProlongation());
            this.empruntDao.save(empruntBdd);
            return "La demande de prolongation a ??t?? envoy??e";
        }else{
            return "Une demande de prolongation est d??j?? en cours";
        }
    }

    @PostMapping("/demande-retour")
    public String demandeRetourEmprunt(@RequestBody Emprunt emprunt){
        Emprunt empruntBdd = empruntDao.findByUtilisateurIdAndMaterielIdMateriel(emprunt.getUtilisateur().getId(), emprunt.getMateriel().getIdMateriel()); //Recup??re les infos post et effectue une recherche pour retrouver l'emprunt

        if(empruntBdd.getdateDemandeRetour() == null) { //V??rifie qu'une demande de retour ne soit pas en cours
            empruntBdd.setdateDemandeRetour(emprunt.getdateDemandeRetour());
            this.empruntDao.save(empruntBdd);
            return "La demande de retour a ??t?? envoy??e";
        }else{
            return "Une demande de retour est d??j?? en cours";
        }
    }

    @GetMapping("gestionnaire/listeDemandesEmprunt")
    @JsonView(View.ListeDemandesEmprunt.class)
    public List listeDemandesEmprunt(){

        List listeDemandesEmprunt = this.empruntDao.findAllByDateDemandeEmpruntIsNotNull();

        return listeDemandesEmprunt;
    }

    @PostMapping("gestionnaire/valider-demande-emprunt/{idGestionnaire}")
    public String validationDemandeEmprunt(@RequestBody Emprunt emprunt, @PathVariable Integer idGestionnaire){
        Gestionnaire gestionnaire = new Gestionnaire();
        gestionnaire.setId(idGestionnaire);
        emprunt = empruntDao.findById(emprunt.getIdEmprunt()).orElse(null);
        emprunt.setGestionnaire(gestionnaire);
        emprunt.setDateValidationEmprunt(LocalDateTime.now()); //Enregistre la date du jour pour la date de validation
        emprunt.setDateDemandeEmprunt(null); //Met la date de demande ?? null la demande n'existe plus
        empruntDao.save(emprunt);
        return "La demande d'emprunt est valid??e";
    }

    @DeleteMapping("gestionnaire/supprimer-demande-emprunt/{idEmprunt}")
    public String supprimerDemandeEmprunt(@PathVariable Integer idEmprunt){
        this.empruntDao.deleteById(idEmprunt);
        return "La demande a bien ??t?? supprim??e";
    }

    @GetMapping("gestionnaire/listeRetoursEmprunt")
    @JsonView(View.ListeDemandesEmprunt.class)
    public List listeRetoursEmprunt(){
        return this.empruntDao.findAllByDateDemandeRetourIsNotNull();
    }

    @PutMapping("gestionnaire/valider-retour-emprunt/{idGestionnaire}")
    public String validationRetourEmprunt(@RequestBody Emprunt emprunt, @PathVariable Integer idGestionnaire){
        Gestionnaire gestionnaire = new Gestionnaire();
        gestionnaire.setId(idGestionnaire);
        emprunt = empruntDao.findById(emprunt.getIdEmprunt()).orElse(null);
        emprunt.setGestionnaire(gestionnaire);
        emprunt.setDateValidationRetour(LocalDateTime.now()); //Enregistre la date du jour pour la date de validation
        emprunt.setdateDemandeRetour(null); //Met la date de demande retour ?? null la demande de retour n'existe plus
        empruntDao.save(emprunt);
        return "La demande de retour est valid??e";
    }

    @PutMapping("gestionnaire/supprimer-retour-emprunt") //On ne supprime pas tout l'emprunt mais on enl??ve uniquement date demande retour
    public String supprimerRetourEmprunt(@RequestBody Emprunt emprunt){
        emprunt = empruntDao.findById(emprunt.getIdEmprunt()).orElse(null);
        emprunt.setdateDemandeRetour(null); //Met la date de demande retour ?? null la demande est supprim??e
        empruntDao.save(emprunt);
        return "La demande de retour a bien ??t?? supprim??e";
    }

    @GetMapping("gestionnaire/nombre-demandes-emprunt") //Recherche nomnre de demandes d'emprunt
    public Integer NombreDemandesEmprunt(){
        return this.empruntDao.RechercherNombreDemandesEmprunt();
    }

    @GetMapping("gestionnaire/nombre-retours-emprunt") //Recherche nombre de retours d'emprunt
    public Integer NombreRetoursEmprunt(){
        return this.empruntDao.RechercherNombreDemandesRetour();
    }

    @GetMapping("gestionnaire/nombre-prolongation-emprunt") //Recherche nombre de retours d'emprunt
    public Integer RechercherNombreDemandesProlongation(){
        return this.empruntDao.RechercherNombreDemandesProlongation();
    }

    @GetMapping("gestionnaire/listeProlongationEmprunt")
    @JsonView(View.ListeDemandesEmprunt.class)
    public List listeProlongationEmprunt(){
        return this.empruntDao.findAllByDateProlongationIsNotNull();
    }

    @PutMapping("gestionnaire/valider-prolongation-emprunt/{idGestionnaire}")
    public String validationProlongationEmprunt(@RequestBody Emprunt emprunt, @PathVariable Integer idGestionnaire){
        Gestionnaire gestionnaire = new Gestionnaire();
        gestionnaire.setId(idGestionnaire);
        emprunt = empruntDao.findById(emprunt.getIdEmprunt()).orElse(null);
        emprunt.setGestionnaire(gestionnaire);
        emprunt.setDateValidationProlongation(LocalDateTime.now()); //Enregistre la date du jour pour la date de validation
        emprunt.setDateRetour(emprunt.getDateProlongation());
        emprunt.setDateProlongation(null); //Met la date de demande prolongation ?? null la demande de route n'existe plus
        empruntDao.save(emprunt);
        return "La demande de prolongation est valid??e";
    }

    @PutMapping("gestionnaire/supprimer-prolongation-emprunt") //On ne supprime pas tout l'emprunt mais on enl??ve uniquement date demande retour
    public String supprimerProlongationEmprunt(@RequestBody Emprunt emprunt){
        emprunt = empruntDao.findById(emprunt.getIdEmprunt()).orElse(null);
        emprunt.setDateProlongation(null); //Met la date de demande retour ?? null la demande est supprim??e
        empruntDao.save(emprunt);
        return "La demande de prolongation a bien ??t?? supprim??e";
    }

    @PostMapping("gestionnaire/demande-reservation")
    public String enregistrerReservation(@RequestBody Emprunt emprunt){
        emprunt.setDateValidationEmprunt(LocalDateTime.now()); //Validation automatique de la demande de r??servation faite par le gestionnaire
        this.empruntDao.save(emprunt);
        return "La demande de r??servation est enregistr??e";
    }

    @GetMapping("gestionnaire/historique-materiels")
    @JsonView(View.listeHistoriqueMateriels.class)
    public List listeHistoriqueMateriels(){
        return this.empruntDao.findAllByDateValidationRetourIsNotNull();
    }

    @PutMapping("gestionnaire/modification-demande-emprunt")
    public String ModificationNumeroSerieDemandeEmprunt(@RequestBody Emprunt emprunt){
        Materiel materiel = new Materiel();
        materiel.setIdMateriel(emprunt.getMateriel().getIdMateriel());
        emprunt = this.empruntDao.findById(emprunt.getIdEmprunt()).orElse(null);
        emprunt.setMateriel(materiel);
        this.empruntDao.save(emprunt);
        return "La modification du num??ro de s??rie est bien effectu??e";
    }

}