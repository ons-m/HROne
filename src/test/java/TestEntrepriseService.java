import com.recruitx.hrone.entities.Entreprise;
import com.recruitx.hrone.services.EntrepriseCRUD;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestEntrepriseService {

    static EntrepriseCRUD ec;
    static int entrepriseId; // pour stocker l’ID entre les tests

    @BeforeAll
    static void setup() {
        ec = new EntrepriseCRUD();
    }

    @Test
    @Order(1)
    void testCreateEntreprise() {
        Entreprise e = new Entreprise("OpenAI Tunisia", "REF-001");
        ec.create(e);

        // Use the ID set during creation
        assertTrue(e.getIdEntreprise() > 0, "ID should be set after creation");
        entrepriseId = e.getIdEntreprise();

        // Optional: verify it exists in DB
        Entreprise fromDb = ec.getById(entrepriseId);
        assertNotNull(fromDb);
        assertEquals("OpenAI Tunisia", fromDb.getNomEntreprise());
        assertEquals("REF-001", fromDb.getReference());
    }

    @Test
    @Order(2)
    void testGetById() {
        Entreprise e = ec.getById(entrepriseId);
        assertNotNull(e);
        assertEquals("OpenAI Tunisia", e.getNomEntreprise());
    }

    @Test
    @Order(3)
    void testUpdateEntreprise() {
        Entreprise e = new Entreprise(
                entrepriseId,
                "OpenAI Tunisia Updated",
                "REF-002"
        );

        ec.update(e);

        Entreprise updated = ec.getById(entrepriseId);
        assertNotNull(updated);
        assertEquals("OpenAI Tunisia Updated", updated.getNomEntreprise());
        assertEquals("REF-002", updated.getReference());
    }

    @Test
    @Order(4)
    void testDeleteEntreprise() {
        ec.delete(entrepriseId);

        Entreprise e = ec.getById(entrepriseId);
        assertNull(e);
    }
}
