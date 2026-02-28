package com.recruitx.hrone.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Rectangle;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CertificatGenerator {

    // ✅ Ajout paramètre niveau
    public static String generer(String nomCandidat, String titreFormation,
                                 String niveau, String logoPath) {
        String fileName = "certificat_" + nomCandidat.replaceAll(" ", "_")
                + "_" + System.currentTimeMillis() + ".pdf";
        String outputPath = "docs/certificats/" + fileName;
        new java.io.File("docs/certificats").mkdirs();

        try {
            Rectangle pageSize = new Rectangle(842, 595); // A4 paysage
            Document document = new Document(pageSize);
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream(outputPath));
            document.open();

            PdfContentByte canvas = writer.getDirectContentUnder();

            // ── Fond bleu clair ──
            canvas.setColorFill(new Color(230, 240, 255));
            canvas.rectangle(0, 0, 842, 595);
            canvas.fill();

            // ── Bordure dorée ──
            canvas.setColorStroke(new Color(212, 175, 55));
            canvas.setLineWidth(6f);
            canvas.rectangle(20, 20, 802, 555);
            canvas.stroke();

            // ── Titre principal ──
            Font fontTitre = FontFactory.getFont(FontFactory.TIMES_BOLD, 36,
                    new Color(30, 60, 120));
            Paragraph titre = new Paragraph("CERTIFICAT DE PARTICIPATION", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingBefore(60f);
            document.add(titre);

            // ── Ligne dorée ──
            Chunk linebreak = new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(
                    2f, 80f, new Color(212, 175, 55), Element.ALIGN_CENTER, -2));
            document.add(new Paragraph(linebreak));

            // ── Nous certifions que ──
            Font fontNormal = FontFactory.getFont(FontFactory.TIMES_ITALIC, 18,
                    Color.DARK_GRAY);
            Paragraph certifie = new Paragraph("\nNous certifions que\n", fontNormal);
            certifie.setAlignment(Element.ALIGN_CENTER);
            document.add(certifie);

            // ── Nom candidat ──
            Font fontNom = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 30,
                    new Color(30, 60, 120));
            Paragraph nom = new Paragraph(nomCandidat, fontNom);
            nom.setAlignment(Element.ALIGN_CENTER);
            document.add(nom);

            // ── A participé à ──
            Paragraph aParticipe = new Paragraph(
                    "\na participé avec succès à la formation\n", fontNormal);
            aParticipe.setAlignment(Element.ALIGN_CENTER);
            document.add(aParticipe);

            // ── Titre formation ──
            Font fontFormation = FontFactory.getFont(FontFactory.TIMES_BOLD, 24,
                    new Color(212, 175, 55));
            Paragraph formation = new Paragraph("« " + titreFormation + " »",
                    fontFormation);
            formation.setAlignment(Element.ALIGN_CENTER);
            document.add(formation);

            // ✅ Niveau de formation
            if (niveau != null && !niveau.isEmpty()) {
                String niveauIcon = switch (niveau.toLowerCase()) {
                    case "débutant", "debutant"       -> "🟢";
                    case "intermédiaire", "intermediaire" -> "🟡";
                    case "avancé", "avance"            -> "🔴";
                    default -> "⭐";
                };
                Font fontNiveau = FontFactory.getFont(FontFactory.HELVETICA_BOLD,
                        14, new Color(80, 80, 80));
                Paragraph niveauPara = new Paragraph(
                        "\nNiveau : " + niveauIcon + " " + niveau, fontNiveau);
                niveauPara.setAlignment(Element.ALIGN_CENTER);
                document.add(niveauPara);
            }

            // ── Date ──
            String dateEmission = LocalDate.now().format(
                    DateTimeFormatter.ofPattern("dd MMMM yyyy",
                            java.util.Locale.FRENCH));
            Font fontDate = FontFactory.getFont(FontFactory.HELVETICA, 13,
                    Color.GRAY);
            Paragraph date = new Paragraph("\nDélivré le " + dateEmission,
                    fontDate);
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);

            // ── Signature ──
            Font fontSign = FontFactory.getFont(FontFactory.TIMES_ITALIC, 14,
                    Color.DARK_GRAY);
            Paragraph signature = new Paragraph(
                    "\n\n\n_______________________\nL'Agent RH", fontSign);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setIndentationRight(60f);
            document.add(signature);

            document.close();
            System.out.println("✅ Certificat généré : " + outputPath);
            return outputPath;

        } catch (DocumentException | IOException e) {
            System.err.println("❌ Erreur génération certificat: " + e.getMessage());
            return null;
        }
    }
}