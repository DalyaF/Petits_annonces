Pr�requis:
Il faut g�n�rer 3 fichiers avant de pouvoir tester ce projet, un fichier .jsk pour le serveur, et un fichier .jsk pour le client g�n�r� � partir du certificat donn� par le serveur (.crt)

Pour g�n�rer le serveur.jsk :
keytool -genkey -keystore server.jsk -alias server keyalg RSA

Pour g�n�rer le serveur.crt :
keytool -export -keystore server.jsk -alias server -file server.crt

Pour g�n�rer le client.crt :
keytool -import -alias server -file server.crt -keystore client.jsk

Les mots de passe demand� sont "123456"

Lancer un Serveur avec la commande java Serveur
Lancer un Client avec la commande java ClientTCP <adresseIP Serveur>

J'ai d�pos� mes fichiers .jsk et .crt pour tester sans en g�n�rer de nouveaux.