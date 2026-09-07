package com.VISION.continent.service;

import com.VISION.continent.entity.*;
import com.VISION.continent.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)//Dit à JUnit "active Mockito pour cette classe de test" — c'est ce qui permet aux annotations @Mock de fonctionner.
class PositionServiceTest {

    @Mock private PositionRepository positionRepository;//Crée un faux PositionRepository — un objet qui a la même forme (les mêmes méthodes) que le vrai, mais qui ne fait rien tout seul. Tu dois lui dire explicitement quoi répondre (voir when(...) plus bas). Aucune vraie base de données n'est touchée.
    @Mock private MarcheRepository marcheRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private NotificationService notificationService;
    @Mock private WalletService walletService;
    @Mock private EvenementRepository evenementRepository;

    @InjectMocks
    private PositionService positionService;//Crée une vraie instance de PositionService, mais en lui injectant automatiquement tous les mocks déclarés au-dessus (à la place des vraies dépendances Spring). C'est comme construire ton service normalement, mais avec des doublures à la place des vraies pièces.

    private Marche marche;
    private Evenement evenement;
    private Position positionA1000;
    private Position positionA2000;
    private Position positionB500;
    private User userA;
    private User userB;

    @BeforeEach//Cette méthode s'exécute avant chaque test de la classe — elle prépare des données réutilisables (ici, un marché avec 3 positions actives) pour éviter de tout recréer dans chaque test.
    void setUp() {
        userA = new User();
        userA.setId(1L);

        userB = new User();
        userB.setId(10L);

        evenement = Evenement.builder()
                .statut(Evenement.Statut.OUVERT)
                .build();

        marche = Marche.builder()
                .statut(Marche.Statut.OUVERT)
                .evenement(evenement)
                .poolOuiFcfa(new BigDecimal("3000"))
                .poolNonFcfa(new BigDecimal("500"))
                .question("Le Cameroun est-il qualifié ?")
                .build();

        positionA1000 = Position.builder()
                .user(userA).marche(marche)
                .choix(Position.Choix.OUI)
                .montantMiseFcfa(new BigDecimal("1000"))
                .statut(Position.Statut.ACTIVE)
                .build();

        positionA2000 = Position.builder()
                .user(userA).marche(marche)
                .choix(Position.Choix.OUI)
                .montantMiseFcfa(new BigDecimal("2000"))
                .statut(Position.Statut.ACTIVE)
                .build();

        positionB500 = Position.builder()
                .user(userB).marche(marche)
                .choix(Position.Choix.NON)
                .montantMiseFcfa(new BigDecimal("500"))
                .statut(Position.Statut.ACTIVE)
                .build();
    }

    @Test
    void resoudreMarche_devraitCrediterLesGagnantsProportionnellement() {
        // ARRANGE — on prépare les données et le comportement simulé des dépendances
        java.util.UUID marcheId = java.util.UUID.randomUUID();
        when(marcheRepository.findById(marcheId)).thenReturn(java.util.Optional.of(marche));
        when(positionRepository.findByMarcheId(marcheId))
                .thenReturn(List.of(positionA1000, positionA2000, positionB500));

        Wallet walletA = Wallet.builder().soldeFcfa(BigDecimal.ZERO).user(userA).build();
        when(walletService.getOrCreateWallet(userA)).thenReturn(walletA);

        // ACT — on exécute la méthode qu'on veut tester
        positionService.resoudreMarche(marcheId, "OUI");

        // ASSERT — on vérifie que le comportement est correct

        // 1. Le marché doit passer à RESOLU avec le bon outcome
        assertThat(marche.getStatut()).isEqualTo(Marche.Statut.RESOLU);
        assertThat(marche.getOutcomeGagnant()).isEqualTo("OUI");

        // 2. Les positions gagnantes doivent passer à GAGNEE
        assertThat(positionA1000.getStatut()).isEqualTo(Position.Statut.GAGNEE);
        assertThat(positionA2000.getStatut()).isEqualTo(Position.Statut.GAGNEE);

        // 3. La position perdante doit passer à PERDUE
        assertThat(positionB500.getStatut()).isEqualTo(Position.Statut.PERDUE);

        // 4. Vérifie que crediterDirect a bien été appelé avec les bons montants
        //    Pool total = 3500, position 1000/3000 => gain = 1166.67
        verify(walletService).crediterDirect(
                eq(walletA),
                eq(new BigDecimal("1166.67")),
                eq(WalletTransaction.Categorie.GAIN_RESOLUTION),
                anyString(),
                eq(positionA1000)
        );

        //    Position 2000/3000 => gain = 2333.33
        verify(walletService).crediterDirect(
                eq(walletA),
                eq(new BigDecimal("2333.33")),
                eq(WalletTransaction.Categorie.GAIN_RESOLUTION),
                anyString(),
                eq(positionA2000)
        );

        // 5. Le perdant ne doit JAMAIS recevoir de crédit
        verify(walletService, never()).crediterDirect(
                any(), any(), eq(WalletTransaction.Categorie.GAIN_RESOLUTION), anyString(), eq(positionB500)
        );
    }
}