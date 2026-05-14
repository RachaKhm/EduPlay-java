# 🖥️ EduPlay - Application Desktop (JavaFX)

![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-20-blue?style=for-the-badge&logo=java)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql)

> **EduPlay Java** est la version desktop de l'écosystème EduPlay. Conçue pour une performance maximale et une expérience utilisateur fluide sur ordinateur, elle intègre des fonctionnalités avancées comme la reconnaissance faciale, la gestion de webcam et l'intégration profonde avec les services Google.

---

## 📌 Sommaire
1. [Aperçu du Projet](#-aperçu-du-projet)
2. [Fonctionnalités Détaillées](#-fonctionnalités-détaillées)
3. [Technologies & Bibliothèques](#-technologies--bibliothèques)
4. [Structure du Projet](#-structure-du-projet)
5. [Installation & Exécution](#-installation--exécution)
6. [Topics & Mots Clés](#-topics--mots-clés)

---

## 🔍 Aperçu du Projet
L'application EduPlay Java complète la plateforme Web en offrant un environnement desktop robuste. Elle est idéale pour une utilisation en milieu scolaire (tablettes Windows, ordinateurs de classe) et permet une interaction directe avec le matériel (caméra, fichiers locaux).

## ✨ Fonctionnalités Détaillées

### 🖥️ Interface & Expérience Utilisateur
- **JavaFX Modernisé** : Utilisation de FXML pour des interfaces modulaires et réactives.
- **Tableau de Bord Administratif** : Gestion complète des utilisateurs et des ressources éducatives en mode natif.
- **Notifications Interactives** : Système d'alertes visuelles intégré.

### 📸 Multimédia & Matériel
- **Capture Webcam** : Intégration de `webcam-capture` pour les photos de profil ou les activités interactives.
- **Lecteur PDF** : Extraction et affichage de contenu via Apache PDFBox.
- **QR Codes** : Génération et lecture de codes via ZXing (Zebra Crossing).

### 🔒 Sécurité & Data
- **Authentification Sécurisée** : Chiffrement des mots de passe avec BCrypt.
- **Migrations de Base de Données** : Utilisation de Flyway pour synchroniser automatiquement le schéma SQL.
- **Intégration Cloud** : Upload direct de médias vers Cloudinary via API HTTP.

### 📅 Services & Productivité
- **Google Calendar** : Synchronisation des emplois du temps et des événements.
- **Envoi d'Emails** : Support de Jakarta Mail pour les confirmations et notifications.
- **Stripe Desktop** : Intégration du SDK Java pour gérer les paiements depuis l'application.

---

## 🛠️ Technologies & Bibliothèques

- **Langage** : Java 21+
- **Interface** : JavaFX 20, FXML
- **Build Tool** : Maven
- **Base de données** : MySQL, Flyway
- **Bibliothèques Clés** :
  - **sarxos/webcam-capture** (Caméra)
  - **google/google-api-client** (Calendar, Auth)
  - **apache/pdfbox** (Traitement PDF)
  - **stripe/stripe-java** (Paiements)
  - **zxing** (QR Code)
  - **jackson** (Parsing JSON)

---

## 📂 Structure du Projet

```bash
EduPlay-Java/
├── src/main/java/      # Code source (Controllers, Services, Models)
├── src/main/resources/ # Fichiers FXML, Styles CSS, Images
├── sql/                # Scripts de base de données
├── config/             # Fichiers de configuration
└── pom.xml             # Gestion des dépendances Maven
```

---

## ⚙️ Installation & Exécution

1. **Prérequis** : JDK 21+ et Maven installés.
2. **Installation** :
   ```bash
   mvn install
   ```
3. **Configuration** : Créez un fichier `.env` à la racine avec vos identifiants SQL et clés API.
4. **Exécution** :
   ```bash
   mvn javafx:run
   ```

---

## 🏷️ Topics & Mots Clés

### **Topics (GitHub Style)**
`#java` `#javafx` `#desktop-app` `#maven` `#mysql` `#webcam` `#google-calendar` `#stripe` `#pdf-processing` `#flyway`

### **Mots Clés**
- **Secteur** : EdTech, Desktop Software, Éducation Interactive.
- **Technique** : JavaFX Framework, Maven Dependencies, SQL Database, FXML UI.
- **Fonctionnel** : Gestion Webcam, QR Code, Intégration API, Sécurité BCrypt.

---
⭐ *EduPlay Java - La puissance du natif au service de l'apprentissage.*
