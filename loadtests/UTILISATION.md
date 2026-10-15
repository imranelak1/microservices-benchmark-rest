# Exécution JMeter

Le workflow manuel `Campagne REST isolée` lance A, C et D sur trois runners Ubuntu publics distincts. Chaque runner dispose de 4 CPU ; les plafonds des trois conteneurs totalisent 8 Gio : 5,5 Gio pour l'application et le générateur, 1 Gio pour PostgreSQL, 1 Gio pour InfluxDB, 256 Mio pour Prometheus et 256 Mio pour Grafana. Les heaps restent identiques : application 160 Mio, JMeter 192 Mio. `smoke=true` vérifie les quatre plans avec deux utilisateurs ; ces données ne servent pas à la comparaison finale.

La campagne complète conserve les paliers et durées des plans ci-dessous, pendant environ 82 minutes. Les artefacts contiennent les JTL bruts compressés, les statistiques par palier et endpoint, les métriques JVM relevées toutes les cinq secondes, les versions, les ressources réellement visibles et le contrôle initial des 2 000 catégories/100 000 produits. Leur rétention GitHub est d'un jour : télécharger les archives après l'exécution. Les premiers essais sur l'ordinateur partagé sont distincts des campagnes isolées retenues.

    Depuis la racine du projet :

    ```bash
    jmeter -n -t loadtests/READ-heavy.jmx -l results/A-READ-heavy.jtl -Jvariant=A -Jport=19401
    jmeter -n -t loadtests/JOIN-filter.jmx -l results/A-JOIN-filter.jtl -Jvariant=A -Jport=19401
    jmeter -n -t loadtests/MIXED.jmx -l results/A-MIXED.jtl -Jvariant=A -Jport=19401
    jmeter -n -t loadtests/HEAVY-body.jmx -l results/A-HEAVY-body.jtl -Jvariant=A -Jport=19401
    ```

    Refaire avec `-Jvariant=C -Jport=19402`, puis `D / 19403`. Les paliers s’exécutent successivement dans chaque plan ; la durée comprend la montée de 60 secondes. Une vérification courte utilise `-Jduration=15 -JrampUp=2 -Jthreads1=5 -Jthreads2=5 -Jthreads3=5`, sans confondre ses mesures avec la campagne complète.

    Les trois schémas de base partent du même jeu de données. Les DELETE visent uniquement des produits créés par le même thread : les identifiants de lecture ne disparaissent pas pendant le test. Le premier DELETE sans produit disponible devient un POST ; ce petit écart de démarrage est visible dans les compteurs par endpoint. Aucun listener graphique lourd n’est activé. Le listener asynchrone envoie les percentiles et erreurs vers le bucket InfluxDB `jmeter`, organisation `perf`.
