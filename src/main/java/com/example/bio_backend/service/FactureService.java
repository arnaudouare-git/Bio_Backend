package com.example.bio_backend.service;

import com.example.bio_backend.entity.Commande;
import com.example.bio_backend.entity.LigneCommande;
import com.example.bio_backend.entity.Paiement;
import com.example.bio_backend.entity.Utilisateur;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Generation de la facture PDF d'une commande payee (module Tier 2, ajoute
 * le 2026-09-24, via Apache PDFBox). Volontairement un simple document
 * texte tabulaire (pas de mise en page graphique avancee) : le but est
 * d'avoir une piece justificative telechargeable, pas un rendu marketing.
 * Appele uniquement depuis PaiementService.genererFacturePdf(), qui fait
 * deja le controle de propriete (acheteur ou admin) et verifie que le
 * paiement est bien REUSSI avant d'arriver ici.
 */
@Service
public class FactureService {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float MARGE_GAUCHE = 50f;
    private static final float LARGEUR_PAGE = PDRectangle.A4.getWidth();

    public byte[] genererFacturePdf(Commande commande, Paiement paiement) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font police = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font policeGras = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font policeTable = new PDType1Font(Standard14Fonts.FontName.COURIER);
            PDType1Font policeTableGras = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);

            try (PDPageContentStream flux = new PDPageContentStream(document, page)) {
                float y = 780f;

                y = ecrireLigne(flux, policeGras, 18, y, "BioConversion - Facture");
                y -= 10f;
                y = ecrireLigne(flux, police, 10, y, "Commande n." + commande.getId()
                        + " du " + commande.getDateCommande().format(FORMAT_DATE));
                y = ecrireLigne(flux, police, 10, y, "Paiement n." + paiement.getId()
                        + " - " + paiement.getMethode()
                        + (paiement.getReferenceTransaction() != null
                                ? " (ref. " + paiement.getReferenceTransaction() + ")" : "")
                        + " - confirme le " + paiement.getDatePaiement().format(FORMAT_DATE));

                Utilisateur acheteur = commande.getAcheteur();
                y -= 10f;
                y = ecrireLigne(flux, policeGras, 12, y, "Facture a :");
                y = ecrireLigne(flux, police, 10, y, acheteur.getNom() + " " + acheteur.getPrenom());
                y = ecrireLigne(flux, police, 10, y, acheteur.getEmail());
                if (commande.getAdresseLivraison() != null) {
                    y = ecrireLigne(flux, police, 10, y, "Livraison : " + commande.getAdresseLivraison());
                }

                y -= 20f;
                y = ecrireLigne(flux, policeTableGras, 10, y,
                        pad("Produit", 30) + pad("Qte", 6) + pad("PU (FCFA)", 14) + "Sous-total (FCFA)");
                y -= 4f;
                y = ligneHorizontale(flux, y);

                for (LigneCommande ligne : commande.getLignes()) {
                    BigDecimal sousTotal = ligne.getPrixUnitaire()
                            .multiply(BigDecimal.valueOf(ligne.getQuantite()));
                    String etiquette = ligne.getProduit().getNom()
                            + ("REFUSEE".equals(ligne.getStatut()) ? " (refusee)" : "");
                    y = ecrireLigne(flux, policeTable, 10, y,
                            pad(etiquette, 30) + pad(String.valueOf(ligne.getQuantite()), 6)
                                    + pad(ligne.getPrixUnitaire().toPlainString(), 14)
                                    + sousTotal.toPlainString());
                }

                y -= 6f;
                y = ligneHorizontale(flux, y);
                y -= 10f;
                y = ecrireLigne(flux, policeGras, 12, y,
                        "TOTAL : " + commande.getMontantTotal().toPlainString() + " FCFA");

                y -= 30f;
                ecrireLigne(flux, police, 8, y,
                        "Document genere automatiquement par la plateforme BioConversion.");
            }

            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            document.save(sortie);
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de la generation de la facture PDF.", e);
        }
    }

    private float ecrireLigne(PDPageContentStream flux, PDType1Font police, float taille, float y, String texte)
            throws IOException {
        flux.beginText();
        flux.setFont(police, taille);
        flux.newLineAtOffset(MARGE_GAUCHE, y);
        flux.showText(texte);
        flux.endText();
        return y - (taille + 6f);
    }

    private float ligneHorizontale(PDPageContentStream flux, float y) throws IOException {
        flux.moveTo(MARGE_GAUCHE, y);
        flux.lineTo(LARGEUR_PAGE - MARGE_GAUCHE, y);
        flux.stroke();
        return y;
    }

    /** Alignement en colonnes fixes -- s'appuie sur Courier (police a chasse fixe) pour que le padding soit exact. */
    private String pad(String texte, int longueur) {
        if (texte.length() >= longueur) {
            return texte.substring(0, longueur - 1) + " ";
        }
        return texte + " ".repeat(longueur - texte.length());
    }
}
