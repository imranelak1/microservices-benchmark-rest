# Tableaux T0 à T7 — campagne REST

Campagne retenue : [run 38670648889](https://github.com/imranelak1/microservices-benchmark-rest/actions/runs/38670648889), commit `d99b9c0bd5b9031dc0f0ba4ea1885721b2913540`. Trois runners indépendants, un par variante. Les 27 paliers sont entiers : neuf par variante et quatre remises à zéro de 2 000 catégories/100 000 items chacune, suivies de 50 GET de préchauffage. **11 000 059 tentatives et aucun échec** sont conservés. Les anciens essais locaux, smokes et runs antérieurs sont exclus.

## T0 — Matériel et logiciels

| Élément | Valeur |
| --- | --- |
| CPU / RAM hôte visible | A: AMD EPYC 7763 64-Core Processor, 4 vCPU, 15.61 Gio; C: INTEL(R) XEON(R) PLATINUM 8573C, 4 vCPU, 15.61 Gio; D: AMD EPYC 9V74 80-Core Processor, 4 vCPU, 15.61 Gio |
| OS / kernel | A/C/D : runner Ubuntu 24.04, noyau Linux 6.17.0-1022-azure, build #22-Ubuntu SMP du 27 juillet 2026, x86_64 ; worker Jammy, glibc 2.35. |
| Java serveur et JMeter | A/C/D : OpenJDK Temurin 17.0.16+8, version du 15 juillet 2025 ; compilation Temurin 17.0.20.1+1. |
| Docker / Compose | A/C/D : Docker Engine 28.0.4, client/serveur API 1.48 ; Docker Compose 2.38.2. |
| Runtime des conteneurs | A/C : containerd 2.4.1, runc 1.5.2 ; D : containerd 2.3.6, runc 1.5.1. |
| Images des services | PostgreSQL 16.15 ; Prometheus 2.55.1 ; Grafana 11.5.2 ; InfluxDB 2.7.12 (tags du compose versionné). |
| JMeter | 5.6.3, archive officielle vérifiée SHA-512 ; empreinte dans chaque environment.json. |
| JVM API / JMeter | API: -Xms64m -Xmx160m -XX:+UseG1GC ; JMeter: -Xmx192m -Xss512k. |
| HikariCP | minimum 5 / maximum 20 / connectionTimeout 10 000 ms ; mêmes paramètres dans A/C/D. |
| Budget | 8 Gio cumulés: worker 5,5 Gio, PostgreSQL 1 Gio, InfluxDB 1 Gio, Prometheus 256 Mio, Grafana 256 Mio. Tous partagent les 4 vCPU du runner. |

Java 17 a été conservé pour les trois applications et le générateur, avec les mêmes options. Les fichiers natifs de versions sont archivés dans `runs/{A,C,D}/`. Le budget de 8 Gio porte sur toute la pile de mesure ; le client, PostgreSQL et les collecteurs consomment aussi les 4 vCPU. **Les processeurs diffèrent : AMD EPYC 7763 pour A, Intel Xeon Platinum 8573C pour C et AMD EPYC 9V74 pour D.** Les budgets sont égaux, les capacités matérielles et certains composants du runtime ne sont donc pas identiques. Un seul passage par variante ne permet ni de séparer l’effet du processeur de celui du framework, ni de quantifier la variabilité entre machines.

## T1 — Scénarios

| Scénario | Mix | Utilisateurs | Rampe(s) | Durée/palier(s) | Corps UTF-8 |
| --- | --- | --- | --- | --- | --- |
| READ-heavy | 50% GET items,20% filtre,20% relation,10% GET catégories | 50/100/200 | 60 | 600 | — |
| JOIN-filter | 70% filtre,30% GET item/id | 60/120 | 60 | 480 | — |
| MIXED | 40% GET items,20% POST items,10% PUT items,10% DELETE items,10% POST catégories,10% PUT catégories | 50/100 | 60 | 600 | Item 1 024 o,Category 768 o |
| HEAVY-body | 50% POST items,50% PUT items | 30/60 | 60 | 480 | Item 5 120 o |

La rampe fait partie de la durée du palier. MIXED suit un cycle de dix opérations par thread; une fin de palier peut tronquer le dernier cycle. Les tailles sont celles du corps JSON, complété par des espaces de transport pour Category. Les JTL mesurent aussi les octets HTTP transférés, distincts de cette taille métier. Chaque scénario repart du même volume initial; pendant les écritures, les variantes les plus rapides créent davantage de lignes sur cette durée fixe. Les données ne restent donc pas identiques à chaque instant de MIXED et HEAVY-body.

## T2 — Latence, débit et erreurs

| Scénario | Mesure | A Jersey | C MVC | D Data REST |
| --- | --- | --- | --- | --- |
| READ-heavy | Tentatives | 347706 | 402172 | 345509 |
| READ-heavy | Échecs | 0 | 0 | 0 |
| READ-heavy | Tentatives/s | 193.08 | 223.30 | 191.81 |
| READ-heavy | Réussites/s | 193.08 | 223.30 | 191.81 |
| READ-heavy | p50 (ms) | 452.00 | 391.00 | 452.00 |
| READ-heavy | p95 (ms) | 1494.00 | 1279.00 | 1235.00 |
| READ-heavy | p99 (ms) | 2007.00 | 1729.00 | 2008.00 |
| READ-heavy | Erreurs (%) | 0.00 | 0.00 | 0.00 |
| JOIN-filter | Tentatives | 1495744 | 1842039 | 812517 |
| JOIN-filter | Échecs | 0 | 0 | 0 |
| JOIN-filter | Tentatives/s | 1559.26 | 1920.24 | 846.86 |
| JOIN-filter | Réussites/s | 1559.26 | 1920.24 | 846.86 |
| JOIN-filter | p50 (ms) | 43.00 | 35.00 | 80.00 |
| JOIN-filter | p95 (ms) | 129.00 | 102.00 | 258.00 |
| JOIN-filter | p99 (ms) | 180.00 | 143.00 | 352.00 |
| JOIN-filter | Erreurs (%) | 0.00 | 0.00 | 0.00 |
| MIXED | Tentatives | 247461 | 284988 | 260758 |
| MIXED | Échecs | 0 | 0 | 0 |
| MIXED | Tentatives/s | 206.26 | 237.51 | 217.28 |
| MIXED | Réussites/s | 206.26 | 237.51 | 217.28 |
| MIXED | p50 (ms) | 345.00 | 300.00 | 310.00 |
| MIXED | p95 (ms) | 730.00 | 637.00 | 731.00 |
| MIXED | p99 (ms) | 892.00 | 762.00 | 939.00 |
| MIXED | Erreurs (%) | 0.00 | 0.00 | 0.00 |
| HEAVY-body | Tentatives | 1833104 | 2178408 | 949653 |
| HEAVY-body | Échecs | 0 | 0 | 0 |
| HEAVY-body | Tentatives/s | 1911.41 | 2271.17 | 989.94 |
| HEAVY-body | Réussites/s | 1911.41 | 2271.17 | 989.94 |
| HEAVY-body | p50 (ms) | 16.00 | 13.00 | 26.00 |
| HEAVY-body | p95 (ms) | 49.00 | 41.00 | 145.00 |
| HEAVY-body | p99 (ms) | 75.00 | 65.00 | 242.00 |
| HEAVY-body | Erreurs (%) | 0.00 | 0.00 | 0.00 |

Les percentiles sont exacts par rang supérieur sur les échantillons regroupés, jamais des moyennes de percentiles. Ils incluent les tentatives échouées; les colonnes supplémentaires de `T2-scenarios.csv` et`T2-paliers.csv` donnent les latences des seuls succès. RPS=N/(fin du dernier appel−début du premier), rampe comprise; le débit utile exclut les échecs au numérateur. Les fenêtres et 27 paliers sont archivés pour éviter de masquer la concurrence derrière les moyennes globales.

## T3 — Ressources JVM pendant les charges

| Variante | CPU moy/pic(%) | Heap moy/pic(Mio) | GC moy/pic(ms/s) | Threads moy/pic | Hikari moy/pic/max |
| --- | --- | --- | --- | --- | --- |
| A | 21.31/75.00 | 101.05/138.53 | 15.01/71.89 | 198.03/231.00 | 16.88/20.00/20.00 |
| C | 21.80/75.00 | 94.01/138.93 | 10.31/35.94 | 197.99/231.00 | 17.31/20.00/20.00 |
| D | 31.81/92.50 | 96.05/137.82 | 44.03/114.15 | 198.10/231.00 | 18.78/20.00/20.00 |

Relevés Micrometer toutes les cinq secondes, limités aux fenêtres de charge JTL, avec reset/préchauffage/agrégation exclus. CPU = `process_cpu_usage`×100, fraction de la capacité visible de la JVM, pas un pourcentage par cœur. Heap désigne la mémoire Java utilisée, pas le RSS complet. Le temps GC est la différence du compteur cumulatif divisée par le temps entre deux relevés entièrement dans la même charge; les pics sont ces débits par intervalle. Les valeurs Hikari sont des échantillons, susceptibles de manquer de très courts pics. Le détail par scénario figure dans `T3-jvm.csv`.

## T4 — Relations et filtre

| Endpoint | Variante | N | Réussites/s | p95 (ms) | Erreurs (%) |
| --- | --- | --- | --- | --- | --- |
| GET /items?categoryId | A | 1046837 | 1091.29 | 132.00 | 0.00 |
| GET /items?categoryId | C | 1288378 | 1343.08 | 105.00 | 0.00 |
| GET /items?categoryId | D | 567932 | 591.94 | 268.00 | 0.00 |
| GET /categories/{id}/items | A | 0 | 0 | non mesuré | non mesuré |
| GET /categories/{id}/items | C | 0 | 0 | non mesuré | non mesuré |
| GET /categories/{id}/items | D | 0 | 0 | non mesuré | non mesuré |

Le scénario JOIN-filter prescrit 70 % de filtre et 30 % de lecture par identifiant; il ne contient aucun appel de la relation inverse demandée dans le tableau T4 du sujet. Cette ligne est donc non mesurée dans JOIN-filter. Voici les véritables appels de cette relation dans READ-heavy:

| Endpoint | Variante | N | Réussites/s | p95 (ms) | Erreurs (%) |
| --- | --- | --- | --- | --- | --- |
| GET /categories/{id}/items | A | 69630 | 38.67 | 1585.00 | 0.00 |
| GET /categories/{id}/items | C | 80457 | 44.67 | 1379.00 | 0.00 |
| GET /categories/{id}/items | D | 69103 | 38.36 | 1053.00 | 0.00 |

Le repository commun charge Category via`@EntityGraph` pour les listes et filtres; ce choix évite une lecture paresseuse par produit dans ces parcours, sans prétendre compter les requêtes SQL à partir des temps HTTP. Les trois variantes trient par id. Un test contractuel de 65 items vérifie plusieurs pages, totaux et 404; D conserve les CRUD/HAL automatiques, avec un adaptateur explicite pour rendre la relation paginée au niveau SQL. Les réponses HAL de D incluent liens et métadonnées: les octets reçus par endpoint figurent dans `endpoints.csv`, ce qui limite l’interprétation en coût pur des annotations.

## T5 — Écritures MIXED

| Endpoint | Variante | N | Réussites/s | p95 (ms) | Erreurs (%) |
| --- | --- | --- | --- | --- | --- |
| GET /items | A | 99166 | 82.66 | 804.00 | 0.00 |
| GET /items | C | 114195 | 95.17 | 693.00 | 0.00 |
| GET /items | D | 104487 | 87.07 | 803.00 | 0.00 |
| POST /items | A | 49469 | 41.23 | 497.00 | 0.00 |
| POST /items | C | 56986 | 47.49 | 438.00 | 0.00 |
| POST /items | D | 52125 | 43.43 | 528.00 | 0.00 |
| PUT /items/{id} | A | 24727 | 20.61 | 496.00 | 0.00 |
| PUT /items/{id} | C | 28466 | 23.72 | 443.00 | 0.00 |
| PUT /items/{id} | D | 26049 | 21.71 | 536.00 | 0.00 |
| DELETE /items/{id} | A | 24711 | 20.60 | 497.00 | 0.00 |
| DELETE /items/{id} | C | 28456 | 23.72 | 439.00 | 0.00 |
| DELETE /items/{id} | D | 26040 | 21.70 | 530.00 | 0.00 |
| GET /categories | A | 0 | 0 | non mesuré | non mesuré |
| GET /categories | C | 0 | 0 | non mesuré | non mesuré |
| GET /categories | D | 0 | 0 | non mesuré | non mesuré |
| POST /categories | A | 24700 | 20.59 | 494.00 | 0.00 |
| POST /categories | C | 28447 | 23.71 | 437.00 | 0.00 |
| POST /categories | D | 26035 | 21.69 | 529.00 | 0.00 |
| PUT /categories/{id} | A | 24688 | 20.58 | 495.00 | 0.00 |
| PUT /categories/{id} | C | 28438 | 23.70 | 438.00 | 0.00 |
| PUT /categories/{id} | D | 26022 | 21.68 | 529.00 | 0.00 |

GET /categories est absent du mix demandé: N=0 et latence non mesurée. PUT /categories, réellement présent à 10 %, est ajouté au tableau. Les débits par endpoint utilisent tous la même fenêtre du scénario pour rester additionnables. Chaque DELETE vise un Item créé par le même thread; les lectures initiales restent donc valides. Les erreurs éventuelles de création et de suppression restent dans les JTL.

## T6 — Incidents

Aucune erreur JMeter dans les trois campagnes complètes. Cela établit la stabilité sur ces données et paliers, sans prouver l’absence d’erreur à une autre charge. Les JTL bruts, journaux et CSV d’incidents vide restent archivés.

## T7 — Synthèse

Les meilleurs chiffres ci-dessous sont ceux des environnements observés. Avec trois modèles de processeur différents et une seule campagne par variante, ils ne permettent aucune attribution causale au seul framework, ni aucun classement universel de Jersey, MVC et Data REST.

| Critère | Meilleur observé selon ce critère | A | C | D |
| --- | --- | --- | --- | --- |
| Débit utile global | C | 797.75 réussites/s | 956.96 réussites/s | 481.38 réussites/s |
| p95 global regroupé | C | 529.00 ms | 447.00 ms | 750.00 ms |
| Taux d’erreur global | A/C/D | 0.00 % | 0.00 % | 0.00 % |
| CPU moyenne échantillonnée | A | 21.31 % capacité visible | 21.80 % capacité visible | 31.81 % capacité visible |
| Heap utilisé moyen | C | 101.05 Mio | 94.01 Mio | 96.05 Mio |

Le débit global utilise la somme des durées effectives des quatre scénarios; son p95 regroupe leurs observations. Comme les débits diffèrent, ce regroupement ne donne pas le même poids à chaque scénario. Le détail ci-dessous reste donc nécessaire:

| Scénario | Meilleur débit utile | Écart au suivant | Meilleur p95 |
| --- | --- | --- | --- |
| READ-heavy | C | 15.65% devant A | D |
| JOIN-filter | C | 23.15% devant A | C |
| MIXED | C | 9.31% devant D | C |
| HEAVY-body | C | 18.82% devant A | C |

Les critères doivent être lus ensemble : un débit élevé ne compense pas des appels échoués. CPU et heap sont comparés dans T3 avec leurs limites, et les 27 paliers permettent d’examiner la saturation plutôt que d’extrapoler à une capacité absolue. Le générateur et la base partagent le runner ; un plateau de débit ne prouve donc pas à lui seul que le serveur API est l’unique goulot. Jersey et MVC demandent les routes CRUD explicites ; Data REST réduit ce travail, mais le filtre et la pagination relationnelle de ce contrat requièrent ici un adaptateur. Pour un catalogue relationnel, garder les pages, l’ordre et le chargement JPA explicites ; pour des écritures soutenues, vérifier d’abord le pool et la base ; pour exposer rapidement un CRUD, HAL peut être utile si son contrat convient aux clients.

## Traçabilité

`raw/` contient les 12 JTL natifs compressés. `runs/{A,C,D}/` conserve les versions, validations HTTP, resets, séries JVM, journaux JMeter/application et rapports JUnit du runner. `provenance.json` identifie le commit, les trois artefacts GitHub et leurs empreintes SHA-256. Les instantanés InfluxDB/Prometheus ont servi aux captures Grafana réelles ; aucune capture ne sert de source aux calculs.
