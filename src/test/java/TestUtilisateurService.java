import com.recruitx.hrone.entities.Utilisateur;
import com.recruitx.hrone.services.UtilisateurCRUD;
import org.junit.jupiter.api.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUtilisateurService {

    static UtilisateurCRUD uc;
    static int utilisateurId;          // store created utilisateur ID
    static int entrepriseIdForUser = 24; // pick an existing entreprise ID from your table
    static int profilIdForUser = 1;       // adjust to an existing profil ID

    @BeforeAll
    static void setup() {
        uc = new UtilisateurCRUD();
    }

    @Test
    @Order(1)
    void testCreateUtilisateur() {
        Utilisateur u = new Utilisateur();
        u.setIdEntreprise(entrepriseIdForUser);
        u.setIdProfil(profilIdForUser);
        u.setNomUtilisateur("John Doe");
        u.setMotPasse("password123");
        u.setEmail("john.doe@example.com");
        u.setAdresse("123 Main St");
        u.setNumTel("12345678");
        u.setCin("AA123456");
        u.setNumOrdreSignIn(1);
        u.setDateNaissance(new Date());
        u.setGender('M');
        u.setFirstLogin(0); // <-- explicitly set firstLogin to 0

        uc.create(u);
        assertTrue(u.getIdUtilisateur() > 0, "Utilisateur ID should be generated");

        utilisateurId = u.getIdUtilisateur();
    }

    @Test
    @Order(2)
    void testGetById() {
        Utilisateur u = uc.getById(utilisateurId);
        assertNotNull(u, "Utilisateur should not be null");
        assertEquals("John Doe", u.getNomUtilisateur());
        assertEquals(0, u.getFirstLogin(), "firstLogin should be 0 initially");
    }

    @Test
    @Order(3)
    void testUpdateUtilisateur() {
        Utilisateur u = new Utilisateur(
                utilisateurId,
                entrepriseIdForUser,
                profilIdForUser,
                "John Updated",
                "newpass123",
                "john.updated@example.com",
                "456 New St",
                "87654321",
                "BB654321",
                2,
                new Date(),
                'M',
                1 // <-- set firstLogin to 1 to simulate first login done
        );

        uc.update(u);

        Utilisateur updated = uc.getById(utilisateurId);
        assertNotNull(updated);
        assertEquals("John Updated", updated.getNomUtilisateur());
        assertEquals("newpass123", updated.getMotPasse());
        assertEquals("john.updated@example.com", updated.getEmail());
        assertEquals(1, updated.getFirstLogin(), "firstLogin should be updated to 1");
    }

    @Test
    @Order(4)
    void testDeleteUtilisateur() {
        uc.delete(utilisateurId);
        Utilisateur u = uc.getById(utilisateurId);
        assertNull(u, "Utilisateur should be deleted");
    }
}
