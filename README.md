# Projet Mobile UQAC
Le projet de dev mobile, à base de GPS et de QR Code

#### Liste des fonctionnalités à implémenter
* ~~Récupérer la position via GPS.~~
* Récupérer la position via Wi-fi.
* ~~Scanner un QR Code.~~
* Récupération des informations via internet ou base de données interne.
* Base de données hors-ligne pour accéder .
* Gestion de base de données pré remplies (Ajouter/Supprimer des bases de données en fonction de sa position).
* Positionner un point d’intérêt privé. (Sauvegarde en local)
* Positionner un point d’intérêt public. (Sauvegarde en ligne)
* Partager un point d’intérêt (email/réseaux sociaux).
* Génération de chemins en fonctions des points d’intérêt ou de la liste des points d’intérêt en fonction du chemin via maps.


#### Quelques idées intéressantes (Notamment avec ce que nous a dit Bob)
* Un service tourne en fond, permettant d'avertir l'utilisateur au moment opportun.
* Avec le GPS, on peut connaitre la vitesse de l'utilisateur, donc il faut le prévenir un peu avant de rencontrer un point d'interet (1 minute à pied, 3 en voiture ?)
* Quand l'utilisateur est en voiture (on peut le savoir avec la vitesse de l'ordre de 20->130km/h), si l'utilisateur a activé cette fonctionnalité, on peut mettre des alertes vocales. Comme ça, il n'a pas a sortir son téléphone.
* Quand l'utilisateur est a pied, ou s'il prefere (par exemple en transport en commun), on utilisera plutot des notifications.
* Possibilité de catégoriser les points d'interet. (Avec par exemple : Accepte un détour de XX minutes pour voir ça, Veut être prévenu s'il est sur la route uniquement, ne veut pas être alerté...)
* Utiliser la reconnaissance vocale pour ajouter des points d'interet (utilisation de mots clés).

#### Autres notes 
* L'écran principal doit être la carte Google Maps, avec un menu déroulant.
* Si pas de réseau, lui mettre un écran blanc (plutot qu'une erreur GMaps), et le lui dire.
