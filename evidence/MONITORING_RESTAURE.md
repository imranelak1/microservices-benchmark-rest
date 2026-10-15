# Provenance des vues Grafana

Les captures proviennent de la [campagne complète du 15 octobre 2026](https://github.com/imranelak1/microservices-benchmark-rest/actions/runs/38670648889), commit `d99b9c0`. J’ai restauré les sauvegardes InfluxDB et les snapshots Prometheus de chaque runner dans des conteneurs séparés, puis ouvert le dashboard dans un vrai navigateur. Les fenêtres affichées correspondent aux débuts et fins enregistrés par le driver distant, en UTC.

| Variante | Fenêtre UTC | Requêtes restaurées | Capture |
| --- | --- | ---: | --- |
| A — Jersey | 02:24:23.592–03:48:01.246 | 3 924 015 | [results.png](../screenshots/results.png) |
| C — MVC | 02:24:56.515–03:48:36.044 | 4 707 607 | [grafana-c.png](../screenshots/grafana-c.png) |
| D — Data REST | 02:24:36.837–03:47:58.639 | 2 368 437 | [grafana-d.png](../screenshots/grafana-d.png) |

Les compteurs InfluxDB correspondent exactement aux CSV de synthèse pour les quatre scénarios de chaque variante : **11 000 059 requêtes, zéro erreur**. Les séries restaurées sont conservées dans [A](monitoring/restored-a.csv), [C](monitoring/restored-c.csv) et [D](monitoring/restored-d.csv).

Le débit est le compteur de chaque intervalle divisé par sa durée réelle. Chaque variante fournit 988 compteurs et 984 valeurs de débit : le premier point de chacun des quatre scénarios est exclu, car son intervalle précédent est inconnu. Les 2 952 valeurs calculées par Flux ont été recoupées avec les horodatages bruts. Les neuf panneaux ont répondu correctement pour chaque capture.

La restauration utilise InfluxDB 2.7.12, Prometheus 2.55.1 et Grafana 11.5.2. Les identifiants des artefacts, empreintes des sauvegardes, images Docker, fenêtres temporelles et SHA-256 des trois PNG figurent dans [la provenance détaillée](monitoring-restoration.json). Les viewers ont été arrêtés après les captures.
