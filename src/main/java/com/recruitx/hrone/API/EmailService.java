package com.recruitx.hrone.API;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "mohamedkooli588@gmail.com";
    private static final String FROM_PASSWORD = "asyopjcqbvnjnvaw"; // ✅ sans espaces
    private static final String EMPLOYEE_FROM_EMAIL =
        System.getenv().getOrDefault("HRONE_EMPLOYEE_FROM_EMAIL", FROM_EMAIL);
    private static final String EMPLOYEE_FROM_PASSWORD =
        System.getenv().getOrDefault("HRONE_EMPLOYEE_FROM_PASSWORD", FROM_PASSWORD);

    private static Properties getSmtpProperties(String email) {
        Properties props = new Properties();

        if (email.endsWith("@gmail.com")) {
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        } else if (email.endsWith("@outlook.com")
                || email.endsWith("@hotmail.com")
                || email.endsWith("@live.com")
                || email.endsWith("@esprit.tn")) {
            props.put("mail.smtp.host", "smtp.office365.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", "smtp.office365.com");

        } else {
            // Fallback Gmail
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        }

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        return props;
    }

    public static void sendTicketEmail(
            String toEmail,
            String candidatNom,
            String formationTitre,
            String modeFormation,
            String dateDebut,
            String dateFin,
            String niveau,
            byte[] qrCodeBytes) {

        // ✅ TOUJOURS Gmail — peu importe l'email destinataire
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));
            message.setSubject("🎓 Confirmation d'inscription — " + formationTitre);

            MimeMultipart multipart = new MimeMultipart("related");

            // Part 1 — HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            String html = buildEmailHtml(
                    candidatNom, formationTitre, modeFormation,
                    dateDebut, dateFin, niveau);
            htmlPart.setContent(html, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // Part 2 — QR Code
            if (qrCodeBytes != null) {
                MimeBodyPart qrPart = new MimeBodyPart();
                qrPart.setDataHandler(new jakarta.activation.DataHandler(
                        new ByteArrayDataSource(qrCodeBytes, "image/png")));
                qrPart.setHeader("Content-ID", "<qrcode>");
                qrPart.setDisposition(MimeBodyPart.INLINE);
                multipart.addBodyPart(qrPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("✅ Email envoyé à : " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendEmployeeCredentialsEmail(
            String toEmail,
            String employeeName,
            String username,
            String plainPassword) {

        Properties props = getSmtpProperties(EMPLOYEE_FROM_EMAIL);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMPLOYEE_FROM_EMAIL, EMPLOYEE_FROM_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMPLOYEE_FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("👋 Bienvenue sur HROne — Vos identifiants de connexion");
            message.setContent(buildEmployeeCredentialsHtml(employeeName, username, plainPassword),
                    "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("✅ Email identifiants employé envoyé à : " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur email identifiants employé: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void sendEventApplicationEmail(
            String toEmail,
            String participantName,
            String eventTitle,
            String eventDate,
            String eventLocation) {

        Properties props = getSmtpProperties(FROM_EMAIL);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));
            message.setSubject("📅 Confirmation d'inscription — " + eventTitle);

            message.setContent(
                    buildEventApplicationHtml(
                            participantName,
                            eventTitle,
                            eventDate,
                            eventLocation
                    ),
                    "text/html; charset=UTF-8"
            );

            Transport.send(message);
            System.out.println("✅ Email confirmation événement envoyé à : " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur email événement: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void sendJobApprovalEmail(
            String toEmail,
            String candidateName,
            String offerTitle) {

        Properties props = getSmtpProperties(FROM_EMAIL);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("✅ Candidature approuvée — " + offerTitle);
            message.setContent(buildJobApprovalHtml(candidateName, offerTitle),
                    "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("✅ Email approbation candidature envoyé à : " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur email approbation candidature: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String buildEventApplicationHtml(
            String name,
            String eventTitle,
            String eventDate,
            String eventLocation) {

        String safeName = name != null ? name : "Participant";
        String safeTitle = eventTitle != null ? eventTitle : "Événement";
        String safeDate = eventDate != null ? eventDate : "";
        String safeLocation = eventLocation != null ? eventLocation : "";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
                "<div style='font-family:Arial,sans-serif;max-width:620px;margin:auto;background:#f4f6f8;padding:20px'>" +
                "<div style='background:linear-gradient(135deg,#2c3e50,#8e44ad);color:white;padding:28px;border-radius:12px 12px 0 0;text-align:center'>" +
                "<h2 style='margin:0'>📅 Confirmation d'inscription</h2>" +
                "<p style='margin:6px 0 0;opacity:0.9'>Votre participation a été enregistrée</p>" +
                "</div>" +
                "<div style='background:white;padding:26px;border-radius:0 0 12px 12px'>" +
                "<p style='margin:0 0 14px;color:#2c3e50'>Bonjour <strong>" + safeName + "</strong>,</p>" +
                "<p style='margin:0 0 18px;color:#555'>Vous êtes inscrit(e) à l'événement suivant :</p>" +
                "<div style='border:1px solid #e2e8f0;border-radius:10px;padding:14px;background:#f8fafc'>" +
                "<p style='margin:0 0 8px'><strong>📌 Événement :</strong> " + safeTitle + "</p>" +
                "<p style='margin:0 0 8px'><strong>📅 Date :</strong> " + safeDate + "</p>" +
                "<p style='margin:0'><strong>📍 Lieu :</strong> " + safeLocation + "</p>" +
                "</div>" +
                "<p style='margin:18px 0 0;color:#555'>Nous vous attendons le jour de l'événement.</p>" +
                "<p style='margin:24px 0 0;color:#888;font-size:12px'>HROne — Système de gestion RH</p>" +
                "</div></div></body></html>";
    }

    private static String buildEmailHtml(
            String nom, String formation,
            String mode, String dateDebut,
            String dateFin, String niveau) {

        String modeIcon = "En ligne".equals(mode) ? "🌐" : "🏢";
        String modeCouleur = "En ligne".equals(mode) ? "#2980b9" : "#27ae60";

        String niveauCouleur = switch (niveau != null ? niveau : "Débutant") {
            case "Débutant"      -> "#27ae60";
            case "Intermédiaire" -> "#e67e22";
            case "Avancé"        -> "#e74c3c";
            default              -> "#95a5a6";
        };
        String niveauIcon = switch (niveau != null ? niveau : "Débutant") {
            case "Débutant"      -> "🟢";
            case "Intermédiaire" -> "🟡";
            case "Avancé"        -> "🔴";
            default              -> "⚪";
        };

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}" +
                ".ticket{background:white;border-radius:12px;max-width:600px;margin:auto;" +
                "box-shadow:0 4px 20px rgba(0,0,0,0.1)}" +
                ".header{background:linear-gradient(135deg,#2c3e50,#3498db);" +
                "color:white;padding:30px;text-align:center;border-radius:12px 12px 0 0}" +
                ".header h1{margin:0;font-size:24px}" +
                ".header p{margin:5px 0 0;opacity:0.8}" +
                ".body{padding:30px}" +
                ".info-row{display:flex;align-items:center;padding:12px 0;" +
                "border-bottom:1px solid #eee}" +
                ".info-label{font-weight:bold;color:#666;width:140px;font-size:13px}" +
                ".info-value{color:#2c3e50;font-size:14px}" +
                ".badge{display:inline-block;color:white;" +
                "padding:4px 12px;border-radius:20px;font-size:12px}" +
                ".qr-section{text-align:center;padding:25px;" +
                "background:#f8f9fa;border-top:2px dashed #dee2e6}" +
                ".qr-section img{width:180px;height:180px}" +
                ".qr-section p{color:#666;font-size:12px;margin-top:10px}" +
                ".footer{background:#2c3e50;color:white;text-align:center;" +
                "padding:15px;font-size:12px;border-radius:0 0 12px 12px}" +
                "</style></head><body>" +
                "<div class='ticket'>" +
                "<div class='header'>" +
                "<h1>🎓 Ticket de Formation</h1>" +
                "<p>Confirmation d'inscription — HROne</p>" +
                "</div>" +
                "<div class='body'>" +
                "<div class='info-row'><span class='info-label'>👤 Candidat</span>" +
                "<span class='info-value'>" + nom + "</span></div>" +
                "<div class='info-row'><span class='info-label'>📚 Formation</span>" +
                "<span class='info-value'>" + formation + "</span></div>" +
                "<div class='info-row'><span class='info-label'>" + modeIcon + " Mode</span>" +
                "<span class='info-value'><span class='badge' style='background:" + modeCouleur + "'>" +
                mode + "</span></span></div>" +
                "<div class='info-row'><span class='info-label'>📅 Date début</span>" +
                "<span class='info-value'>" + dateDebut + "</span></div>" +
                "<div class='info-row'><span class='info-label'>📅 Date fin</span>" +
                "<span class='info-value'>" + dateFin + "</span></div>" +
                "<div class='info-row'><span class='info-label'>🎯 Niveau</span>" +
                "<span class='info-value'><span class='badge' style='background:" + niveauCouleur + "'>" +
                niveauIcon + " " + (niveau != null ? niveau : "Débutant") +
                "</span></span></div>" +
                "</div>" +
                "<div class='qr-section'>" +
                "<img src='cid:qrcode' alt='QR Code'/>" +
                "<p>📱 Présentez ce QR code lors de votre formation</p>" +
                "</div>" +
                "<div class='footer'>" +
                "HROne — Système de gestion RH | Ne pas répondre à cet email" +
                "</div></div></body></html>";
    }

    private static String buildEmployeeCredentialsHtml(
            String employeeName,
            String username,
            String plainPassword) {

        String safeName = employeeName != null ? employeeName : "Employé";
        String safeUsername = username != null ? username : "";
        String safePassword = "MDP";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
                "<div style='font-family:Arial,sans-serif;max-width:620px;margin:auto;background:#f4f6f8;padding:20px'>" +
                "<div style='background:linear-gradient(135deg,#2c3e50,#3498db);color:white;padding:28px;border-radius:12px 12px 0 0;text-align:center'>" +
                "<h2 style='margin:0'>Bienvenue sur HROne</h2>" +
                "<p style='margin:6px 0 0;opacity:0.9'>Votre compte employé a été créé avec succès</p>" +
                "</div>" +
                "<div style='background:white;padding:26px;border-radius:0 0 12px 12px'>" +
                "<p style='margin:0 0 14px;color:#2c3e50'>Bonjour <strong>" + safeName + "</strong>,</p>" +
                "<p style='margin:0 0 18px;color:#555'>Voici vos identifiants de connexion :</p>" +
                "<div style='border:1px solid #e2e8f0;border-radius:10px;padding:14px;background:#f8fafc'>" +
                "<p style='margin:0 0 8px'><strong>Nom d'utilisateur :</strong> " + safeUsername + "</p>" +
                "<p style='margin:0'><strong>Mot de passe :</strong> " + safePassword + "</p>" +
                "</div>" +
                "<p style='margin:18px 0 0;color:#555'>Pour des raisons de sécurité, changez votre mot de passe dès votre première connexion.</p>" +
                "<p style='margin:24px 0 0;color:#888;font-size:12px'>HROne — Système de gestion RH</p>" +
                "</div></div></body></html>";
    }

    private static String buildJobApprovalHtml(
            String candidateName,
            String offerTitle) {

        String safeName = candidateName != null ? candidateName : "Candidat";
        String safeOffer = offerTitle != null ? offerTitle : "Offre";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
                "<div style='font-family:Arial,sans-serif;max-width:620px;margin:auto;background:#f4f6f8;padding:20px'>" +
                "<div style='background:linear-gradient(135deg,#16a085,#27ae60);color:white;padding:28px;border-radius:12px 12px 0 0;text-align:center'>" +
                "<h2 style='margin:0'>Félicitations 🎉</h2>" +
                "<p style='margin:6px 0 0;opacity:0.9'>Votre candidature a été approuvée</p>" +
                "</div>" +
                "<div style='background:white;padding:26px;border-radius:0 0 12px 12px'>" +
                "<p style='margin:0 0 14px;color:#2c3e50'>Bonjour <strong>" + safeName + "</strong>,</p>" +
                "<p style='margin:0;color:#555'>Nous sommes heureux de vous informer que votre candidature pour le poste</p>" +
                "<p style='margin:12px 0 16px;color:#1f2937;font-size:18px'><strong>" + safeOffer + "</strong></p>" +
                "<p style='margin:0;color:#555'>a été <strong>acceptée</strong>. Notre équipe RH vous contactera prochainement pour la suite.</p>" +
                "<p style='margin:24px 0 0;color:#888;font-size:12px'>HROne — Système de gestion RH</p>" +
                "</div></div></body></html>";
    }
    // ✅ Envoyer certificat PDF par email
    public static void sendCertificatEmail(
            String toEmail,
            String candidatNom,
            String formationTitre,
            String niveau,
            String certificatPath) {

        Properties props = getSmtpProperties(FROM_EMAIL);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));
            message.setSubject("🎓 Votre certificat de participation — " + formationTitre);

            MimeMultipart multipart = new MimeMultipart();

            // ── Part 1 — HTML ──
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(buildCertificatHtml(
                            candidatNom, formationTitre, niveau),
                    "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // ── Part 2 — PDF en pièce jointe ──
            if (certificatPath != null) {
                MimeBodyPart pdfPart = new MimeBodyPart();
                pdfPart.attachFile(certificatPath);
                pdfPart.setFileName("Certificat_" +
                        candidatNom.replaceAll(" ", "_") + ".pdf");
                multipart.addBodyPart(pdfPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("✅ Certificat envoyé à : " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi certificat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── HTML email certificat ──
    private static String buildCertificatHtml(
            String nom, String formation, String niveau) {

        String niveauCouleur = switch (niveau != null ? niveau.toLowerCase() : "") {
            case "débutant", "debutant"           -> "#27ae60";
            case "intermédiaire", "intermediaire" -> "#f39c12";
            case "avancé", "avance"               -> "#e74c3c";
            default -> "#95a5a6";
        };

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>" +
                "<div style='font-family:Arial;max-width:600px;margin:auto;" +
                "background:#f4f4f4;padding:20px'>" +
                "<div style='background:linear-gradient(135deg,#1e3c78,#2a5298);" +
                "color:white;padding:30px;text-align:center;border-radius:12px 12px 0 0'>" +
                "<h1 style='margin:0'>🎓 Félicitations !</h1>" +
                "<p style='margin:5px 0 0;opacity:0.8'>Certificat de participation</p>" +
                "</div>" +
                "<div style='background:white;padding:30px;border-radius:0 0 12px 12px'>" +
                "<p style='font-size:16px;color:#2c3e50'>Bonjour <strong>" + nom + "</strong>,</p>" +
                "<p style='color:#555'>Vous avez complété avec succès la formation :</p>" +
                "<div style='background:#f8f9fa;padding:15px;border-radius:8px;" +
                "border-left:4px solid #d4af37;margin:15px 0'>" +
                "<strong style='font-size:18px;color:#1e3c78'>« " + formation + " »</strong><br/>" +
                "<span style='display:inline-block;margin-top:8px;background:" +
                niveauCouleur + ";color:white;padding:4px 12px;" +
                "border-radius:20px;font-size:12px'>Niveau : " + niveau + "</span>" +
                "</div>" +
                "<p style='color:#555'>Votre certificat PDF est joint à cet email.</p>" +
                "<p style='color:#888;font-size:12px;margin-top:30px'>" +
                "HROne — Système de gestion RH</p>" +
                "</div></div></body></html>";
    }

    static class ByteArrayDataSource implements jakarta.activation.DataSource {
        private final byte[] data;
        private final String type;

        public ByteArrayDataSource(byte[] data, String type) {
            this.data = data;
            this.type = type;
        }

        @Override public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(data);
        }
        @Override public java.io.OutputStream getOutputStream() { return null; }
        @Override public String getContentType() { return type; }
        @Override public String getName() { return "qrcode.png"; }
    }
}