-- Création de la base de données
CREATE DATABASE IF NOT EXISTS quickride_db;
USE quickride_db;

-- Table des véhicules
CREATE TABLE IF NOT EXISTS vehicules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,               -- ex: Taxi, Bateau
    localisation VARCHAR(100) NOT NULL,      -- ex: Paris, Lyon
    statut VARCHAR(20) DEFAULT 'disponible'  -- disponible, occupe, hors_service
);

CREATE TABLE IF NOT EXISTS clients (
	id INT AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(50) NOT NULL,
	telephone TEXT NOT NULL,
	pwd VARCHAR(255) NOT NULL
);

-- Table des requêtes clients
CREATE TABLE IF NOT EXISTS requetes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    localisation VARCHAR(100) NOT NULL,
    statut VARCHAR(50) DEFAULT 'en_attente',
    heure_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des attributions de véhicules aux requêtes
CREATE TABLE IF NOT EXISTS attributions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_requete INT NOT NULL,
    id_vehicule INT NOT NULL,
    heure_attrib TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_requete) REFERENCES requetes(id),
    FOREIGN KEY (id_vehicule) REFERENCES vehicules(id)
);

-- Quelques véhicules initiaux
INSERT INTO vehicules (type, localisation, statut) VALUES
('Taxi', 'Paris', 'disponible'),
('Taxi', 'Lyon', 'disponible'),
('Bateau', 'Marseille', 'disponible');
