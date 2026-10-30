# Orange Order First · API

Backend Spring Boot de la prise de commande mobile en boutique. Il expose l'API attendue par le front
Angular [`ococucci/mobilefirst`](https://github.com/ococucci/mobilefirst) et orchestre les API du SI listées dans la
cinématique (section 9). Tant que `orderfirst.integrations=stub` (valeur par défaut), ces API du SI sont
bouchonnées avec le même jeu de démonstration que le front.

## Démarrer

Prérequis : Java 21.

```bash
./mvnw spring-boot:run      # http://localhost:8080
./mvnw test                 # tests unitaires et parcours de bout en bout
./mvnw package              # jar exécutable dans target/
```

- Documentation OpenAPI : http://localhost:8080/swagger-ui.html (JSON : `/v3/api-docs`)
- Santé : http://localhost:8080/actuator/health
- CORS : `orderfirst.cors.allowed-origins` (par défaut `http://localhost:4200`, le front en développement)

## Endpoints

Préfixe : `/api/v1`. Une recherche sans résultat répond **204 No Content** (corps vide) : numéro inconnu,
aucun client existant, client ou contrat inconnu en lecture. Ce n'est pas une erreur.

Les erreurs sortent au format ProblemDetail (RFC 7807) :

- **400 Saisie invalide** : la propriété `errors` liste chaque champ en erreur (`field` en chemin JSON, `message`
  identique à celui affiché par le front), pour placer le message sous le bon champ ;
- **404 Introuvable** : une action porte sur un élément inconnu (commande ou client inconnus, ligne qui
  n'appartient pas au client…) ;
- **422 Opération impossible** : règle de gestion bloquante (score rouge, client entreprise en acquisition,
  ligne avec commande en cours, ligne Card re-scorée orange ou rouge…).

| Méthode et chemin | Usage | API du SI |
| --- | --- | --- |
| `GET /customers?msisdn=` | Identification par MSISDN (204 si le numéro est inconnu) | Party |
| `GET /customers/{id}` | Client par identifiant (204 si inconnu) | Party |
| `POST /customers/existence-checks` | Recherche d'existence avant création d'un client sans numéro : le client existant, ou 204 | Party |
| `POST /customers` | Contrôle Préventel puis création (pas de création si score rouge) | api-scoring, Party |
| `PUT /customers/{id}` | Étape Client : mise à jour, Préventel relancé si nom, prénom ou date de naissance changent | api-scoring, Party |
| `GET /customers/{id}/pending-lines` | Lignes avec une commande en cours | api-order |
| `POST /customers/{id}/lines/{msisdn}/rescoring` | Re-scoring d'une ligne Card avant changement de forfait | api-scoring |
| `GET /catalog/offers` | Forfaits, avec pour chacun ses options, sa remise et l'éligibilité Open ML | Catalogue Castor, api-promo advice |
| `GET /catalog/sim-reasons`, `GET /catalog/donor-operators` | Listes de référence | — |
| `GET /numbers/available` | Numéros de mobile disponibles | api ressource |
| `POST /open-ml/sponsor-checks` | Validation du N° sponsor Open ML | api RHOC |
| `GET /hardware/terminals` | Terminaux | Enabler si-financier |
| `POST /hardware/iccid-checks` | Contrôle de l'ICCID (`ok`, `format`, `inconnu`) | Enabler si-financier |
| `POST /hardware/sim-scans` | Lecture simulée du code-barres d'une SIM | Enabler si-financier |
| `POST /sepa-mandates` | Contrôle et stockage du mandat SEPA (signé hors outil) | api IML |
| `GET /addresses?q=` | Complétion d'adresse | Api-ORAS |
| `POST /contracts` | Génère le contrat de la commande et le stocke (sauf modification d'options) | api EPC, Api-document |
| `GET /contracts/{documentId}` | Téléchargement du contrat (PDF), 204 si inconnu | Api-document |
| `POST /orders` | Valide et crée la commande, puis la publie dans le bus PubSub | api-order, PubSub |
| `POST /orders/{orderNumber}/cash-payments` | Enregistre le paiement effectué en caisse | Enabler managePayment |

### Commande (`POST /contracts` et `POST /orders`)

Le même corps sert à générer le contrat puis à valider la commande. Les champs dépendent de `flow` :

| `flow` | Champs |
| --- | --- |
| `acquisition` | `holder`, `billingAddress`, `number` (`nouveau` + `msisdn`, ou `portabilite` + `rio`, `portedMsisdn`, `operator`, `portingDate`), `offerId`, `options`, `openMlSponsor`, `sim` (`type`, `iccid`), `terminal` (`id`, `imei`), `sepaMandateRef` |
| `forfait` | `lineMsisdn`, `offerId`, `options`, `openMlSponsor` |
| `options` | `lineMsisdn`, `options` (liste complète souhaitée : le serveur calcule ajouts et retraits) |
| `sim` | `lineMsisdn`, `simReason`, `sim` |

Pour valider : `contractRef` (renvoyé par `POST /contracts`) et `contractSigned: true` (sauf `options`),
`cgvAccepted: true` et `privacyAccepted: true`.

Toutes les règles de gestion sont revérifiées côté serveur (le front n'est pas une source de confiance) et le
montant à régler en caisse est recalculé : puce Carte Prépayée à 10 €, terminal, frais de 10 € pour une SIM
perdue ou volée. À la validation, la SIM sort du stock, le numéro choisi est réservé, la ligne passe en
« commande en cours » et le message `orange-order-first.mobile.order-validated` est publié.

### Offres

Chaque offre de `GET /catalog/offers` porte ce qui dépend d'elle :

- `openMl` : éligibilité et montant de la remise Open ML (`monthlyDiscount` : 5 € pour une offre d'entrée de
  gamme, 10 € sinon ; non éligible pour la Carte Prépayée) ;
- `options` : options proposées avec cette offre, à un prix propre à l'offre ; une option `included` est gratuite
  et toujours active ;
- `promo` : remise api-promo advice, appliquée automatiquement (`monthlyDiscount`, `durationMonths`) ; une seule
  par offre (les remises ne se cumulent pas), `null` si l'offre n'en a pas.

Une commande ne peut porter que des options proposées avec l'offre choisie (ou, pour une modification d'options,
avec l'offre actuelle de la ligne).

### Message publié à la validation

En plus de l'en-tête (`orderNumber`, `createdAt`, `flow`, `customerId`, `msisdn`, `cashDue`) et de la commande
saisie (`order`), le message recopie ce qui a été vendu, avec libellés et prix du jour de la vente :

- acquisition et changement de forfait : `offer` (offre, options actives dont les incluses, remise, Open ML
  appliqué, `monthlyPrice` et `monthlyPriceDuringPromo`, mensualité pendant la durée de la remise) ;
- modification d'options : `optionsChange` (options ajoutées et retirées, `monthlyDelta`).

```json
"offer": {
  "id": "bl-60", "label": "Forfait bloqué 60 Go", "price": 22.99,
  "options": [ { "id": "europe", "label": "Option Europe 10 Go", "price": 5.0, "included": false } ],
  "promo": { "id": "promo-10e-6m", "label": "−10 € / 6 mois", "monthlyDiscount": 10.0, "durationMonths": 6 },
  "openMl": { "sponsor": "0690654321", "monthlyDiscount": 10.0 },
  "monthlyPrice": 17.99,
  "monthlyPriceDuringPromo": 7.99
}
```

## Organisation du code

```
src/main/java/com/orderfirst/api/
  domain/    modèle, règles de gestion et messages de validation (alignés sur le front)
  port/      une interface par API du SI (Party, api-scoring, Castor, api-order, PubSub…)
  stub/      implémentations bouchonnées de ces interfaces (orderfirst.integrations=stub)
  service/   identification du client, contrat et commande (contrôles et orchestration)
  web/       contrôleurs REST
  config/    CORS, OpenAPI, format des erreurs, horloge
```

Pour brancher le SI réel : implémenter les interfaces de `port/` (clients HTTP, publication PubSub…) dans des
beans activés par une autre valeur de `orderfirst.integrations`. Les services et contrôleurs ne changent pas.

## Jeu de démonstration

Identique au front : `06 90 12 34 56` (Mme Marie Dupont, score vert, 4 lignes dont une Card et une avec
commande en cours), `06 96 77 88 99` (score orange, ligne Card), `06 94 99 90 00` (score rouge),
`06 90 40 05 00` (client entreprise). Préventel renvoie rouge ou orange si le nom contient ce mot. Sponsor Open ML
`0690654321`, ICCID en stock `8933010000000000001` à `…043`, IBAN `FR76 3000 6000 0112 3456 7890 189`,
IMEI `490154203237518`.
