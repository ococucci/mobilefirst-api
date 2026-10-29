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

Préfixe : `/api/v1`. Les erreurs sortent au format ProblemDetail (RFC 7807) :

- **400 Saisie invalide** : la propriété `errors` liste chaque champ en erreur (`field` en chemin JSON, `message`
  identique à celui affiché par le front), pour placer le message sous le bon champ ;
- **404 Introuvable** : client, ligne, commande ou contrat inconnu ;
- **422 Opération impossible** : règle de gestion bloquante (score rouge, client entreprise en acquisition,
  ligne avec commande en cours, ligne Card re-scorée orange ou rouge…).

| Méthode et chemin | Usage | API du SI |
| --- | --- | --- |
| `GET /customers?msisdn=` | Identification par MSISDN (404 si inconnu) | Party |
| `GET /customers/{id}` | Client par identifiant | Party |
| `POST /customers/existence-checks` | Recherche d'existence avant création d'un client sans numéro | Party |
| `POST /customers` | Contrôle Préventel puis création (pas de création si score rouge) | api-scoring, Party |
| `PUT /customers/{id}` | Étape Client : mise à jour, Préventel relancé si nom, prénom ou date de naissance changent | api-scoring, Party |
| `GET /customers/{id}/pending-lines` | Lignes avec une commande en cours | api-order |
| `POST /customers/{id}/lines/{msisdn}/rescoring` | Re-scoring d'une ligne Card avant changement de forfait | api-scoring |
| `GET /catalog/offers`, `GET /catalog/options` | Forfaits et options mobile | Catalogue Castor |
| `GET /catalog/promotions` | Remises (pastilles) | api-promo advice |
| `GET /catalog/sim-reasons`, `GET /catalog/donor-operators` | Listes de référence | — |
| `GET /numbers/available` | Numéros de mobile disponibles | api ressource |
| `POST /open-ml/sponsor-checks` | Validation du N° sponsor Open ML | api RHOC |
| `GET /hardware/terminals` | Terminaux | Enabler si-financier |
| `POST /hardware/iccid-checks` | Contrôle de l'ICCID (`ok`, `format`, `inconnu`) | Enabler si-financier |
| `POST /hardware/sim-scans` | Lecture simulée du code-barres d'une SIM | Enabler si-financier |
| `POST /sepa-mandates` | Contrôle et stockage du mandat SEPA (signé hors outil) | api IML |
| `GET /addresses?q=` | Complétion d'adresse | Api-ORAS |
| `POST /contracts` | Génère le contrat de la commande et le stocke (sauf modification d'options) | api EPC, Api-document |
| `GET /contracts/{documentId}` | Téléchargement du contrat (PDF) | Api-document |
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
