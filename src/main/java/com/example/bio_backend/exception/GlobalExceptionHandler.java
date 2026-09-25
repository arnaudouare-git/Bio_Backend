package com.example.bio_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Intercepte les exceptions metier pour renvoyer une reponse JSON propre
 * (au lieu de la page d'erreur blanche par defaut de Spring Boot).
 *
 * Point de depart volontairement minimal : chaque nouveau module
 * (Produits/Commandes, Serres/IoT) pourra ajouter ses propres
 * @ExceptionHandler ici au fur et a mesure.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailDejaUtiliseException.class)
    public ResponseEntity<Object> handleEmailDejaUtilise(EmailDejaUtiliseException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<Object> handleIdentifiantsInvalides(IdentifiantsInvalidesException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<Object> handleRessourceIntrouvable(RessourceIntrouvableException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CompteNonEligibleActivationException.class)
    public ResponseEntity<Object> handleCompteNonEligibleActivation(CompteNonEligibleActivationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CompteNonVerifieException.class)
    public ResponseEntity<Object> handleCompteNonVerifie(CompteNonVerifieException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(StockInsuffisantException.class)
    public ResponseEntity<Object> handleStockInsuffisant(StockInsuffisantException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PaiementDejaTraiteException.class)
    public ResponseEntity<Object> handlePaiementDejaTraite(PaiementDejaTraiteException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ProduitNonProprietaireException.class)
    public ResponseEntity<Object> handleProduitNonProprietaire(ProduitNonProprietaireException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(CommandeAccesRefuseException.class)
    public ResponseEntity<Object> handleCommandeAccesRefuse(CommandeAccesRefuseException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(CompteSuspenduException.class)
    public ResponseEntity<Object> handleCompteSuspendu(CompteSuspenduException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(AvisDejaDonneException.class)
    public ResponseEntity<Object> handleAvisDejaDonne(AvisDejaDonneException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LitigeDejaOuvertException.class)
    public ResponseEntity<Object> handleLitigeDejaOuvert(LitigeDejaOuvertException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message
        );
        return ResponseEntity.status(status).body(body);
    }
}
