package com.example.bio_backend.dto;

/**
 * Corps de la requete PUT /api/paiements/{id}/confirmer.
 *
 * Dans un vrai systeme, ce serait Orange Money qui rappellerait notre API
 * (webhook) pour dire si la transaction a reussi ou echoue. Ici, comme on
 * n'a pas de vrai compte marchand Orange Money, on SIMULE ce retour : c'est
 * l'appelant (nous, via Postman) qui indique "reussi=true/false" a la place
 * de l'operateur.
 */
public class ConfirmerPaiementRequest {

    private boolean reussi;

    public boolean isReussi() {
        return reussi;
    }

    public void setReussi(boolean reussi) {
        this.reussi = reussi;
    }
}
