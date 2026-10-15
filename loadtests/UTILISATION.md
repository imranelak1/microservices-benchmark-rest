# Exécution JMeter

Le workflow manuel `Campagne REST isolée` lance A, C et D sur trois runners Ubuntu publics distincts. Chaque runner dispose de 4 CPU ; les plafonds des cinq conteneurs totalisent 8 Gio : 5,5 Gio pour l'application et le générateur, 1 Gio pour PostgreSQL, 1 Gio pour InfluxDB, 256 Mio pour Prometheus et 256 Mio pour Grafana. Les heaps restent identiques : application 160 Mio, JMeter 192 Mio. `smoke=true` vérifie les quatre plans avec deux utilisateurs ; ces données ne servent pas à la comparaison finale.

La campagne complète conserve les paliers et durées des plans ci-dessous, pendant environ 82 minutes. Les artefacts contiennent les JTL bruts compressés, les statistiques par palier et endpoint, les métriques JVM relevées toutes les cinq secondes, les versions, les ressources réellement visibles et le contrôle initial des 2 000 catégories/100 000 produits. Leur rétention GitHub est d'un jour : télécharger les archives après l'exécution. Les premiers essais sur l'ordinateur partagé sont distincts des campagnes isolées retenues.

Depuis la racine du projet :

```bash
jmeter -n -t loadtests/READ-heavy.jmx -l results/A-READ-heavy.jtl -Jvariant=A -Jport=19401
jmeter -n -t loadtests/JOIN-filter.jmx -l results/A-JOIN-filter.jtl -Jvariant=A -Jport=19401
jmeter -n -t loadtests/MIXED.jmx -l results/A-MIXED.jtl -Jvariant=A -Jport=19401
jmeter -n -t loadtests/HEAVY-body.jmx -l results/A-HEAVY-body.jtl -Jvariant=A -Jport=19401
```

Refaire avec `-Jvariant=C -Jport=19402`, puis `D / 19403`. Les paliers s’exécutent successivement dans chaque plan ; la durée comprend la montée de 60 secondes. Une vérification courte utilise `-Jduration=15 -JrampUp=2 -Jthreads1=5 -Jthreads2=5 -Jthreads3=5`, sans confondre ses mesures avec la campagne complète.

Les trois schémas de base partent du même jeu de données. MIXED utilise un cycle de dix opérations par thread : quatre GET, deux POST Item, un PUT Item, un DELETE Item, un POST Category et un PUT Category. Les POST précèdent le DELETE, qui vise uniquement un identifiant créé par ce thread. Si les deux POST échouent, le DELETE vise -1 et son erreur reste visible ; aucun identifiant du dataset de lecture n’est supprimé.

Les corps Category font 768 octets UTF-8, complétés par des espaces JSON de transport après l’objet : les modèles métier et les valeurs persistées restent identiques dans A/C/D. Aucun listener graphique lourd n’est activé. Le listener asynchrone envoie les percentiles et erreurs vers le bucket InfluxDB `jmeter`, organisation `perf`.

Le panneau `JMeter débit (req/s)` affiche des requêtes par seconde : `count / durée réelle de l’intervalle`. Le [listener JMeter](https://github.com/apache/jmeter/blob/rel/v5.6.3/src/components/src/main/java/org/apache/jmeter/visualizers/backend/influxdb/InfluxdbBackendListenerClient.java) remet `count` à zéro après chaque envoi. [Flux elapsed](https://docs.influxdata.com/flux/v0/stdlib/universe/elapsed/) mesure l’écart entre horodatages avant la division, ce qui tient compte des intervalles irréguliers. Le premier point sans durée connue est exclu. Le total des requêtes et les RPS des tableaux restent calculés depuis les JTL bruts.
