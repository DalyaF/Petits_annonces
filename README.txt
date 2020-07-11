Prérequis:
Il faut générer 3 fichiers avant de pouvoir tester ce projet, un fichier .jsk pour le serveur, et un fichier .jsk pour le client généré à partir du certificat donné par le serveur (.crt)

Pour générer le serveur.jsk :
keytool -genkey -keystore server.jsk -alias server keyalg RSA

Pour générer le serveur.crt :
keytool -export -keystore server.jsk -alias server -file server.crt

Pour générer le client.crt :
keytool -import -alias server -file server.crt -keystore client.jsk

Les mots de passe demandé sont "123456"

Lancer un Serveur avec la commande java Serveur
Lancer un Client avec la commande java ClientTCP <adresseIP Serveur>

J'ai déposé mes fichiers .jsk et .crt pour tester sans en générer de nouveaux.