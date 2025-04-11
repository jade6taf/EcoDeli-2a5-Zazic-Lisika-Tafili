<script>
export default {
  name: 'HomeView',
  data() {
        return {
        carPosition: 0, // Position initiale de la voiture
        };
    }
}
</script>

<template>
    <div class="home">
        <section class="hero">
            <div class="hero-content">
                <div class="hero-text">
                    <h1>Une <span class="highlight">nouvelle</span> façon de penser la livraison</h1>
                    <p>Avec plateforme de <span class="highlight">crowdshipping</span>, un système collaboratif où des particuliers et des professionnels se connectent pour envoyer et recevoir des colis de manière simple, rapide et éco-responsable</p>
                    <p>nous réduisons l'impact carbone des livraisons, offrons une alternative économique, et favorisons l'entraide entre particuliers et professionnels</p>
                    <button class="cta-button">En savoir plus</button>
                </div>

                <div class="hero-image">
                    <img src="@/assets/accueil_screen.png" alt="Illustration_home" class="home_illustration" />
                </div>

            </div>
        </section>

        <section class="services">
            <h2>Nos services</h2>
            <p>Bien plus qu'un service de livraison ! Nous proposons :</p>
            <div class="services-container">
                <div class="service-card">
                    <img src="@/assets/livraison_colis.png" alt="Livraison de colis" class="service-icon" />
                    <h3>Livraison de colis</h3>
                    <p>Expédiez vos colis à moindre coût grâce à notre réseau de livreurs occasionnels.</p>
                    <button class="service-button">Livraison de colis</button>
                </div>

                <div class="service-card">
                    <img src="@/assets/cadi.png" alt="Lâcher vos chariots" class="service-icon" />
                    <h3>Lâcher vos chariots</h3>
                    <p>Faites vos courses chez nos commerçants partenaires et faites-vous livrer à domicile.</p>
                    <button class="service-button">Lâcher vos chariots</button>
                </div>

                <div class="service-card">
                    <img src="@/assets/covoiturage.png" alt="Transport de personnes" class="service-icon" />
                    <h3>Transport de personnes</h3>
                    <p>Un service de transport pour les personnes âgées, à mobilités réduites.</p>
                    <button class="service-button">Transport de personnes</button>
                </div>

                <div class="service-card">
                    <img src="@/assets/shopping.png" alt="Achats à l'étranger" class="service-icon" />
                    <h3>Achats à l'étranger</h3>
                    <p>Demandez à un voyageur de vous rapporter des produits introuvables en France.</p>
                    <button class="service-button">Achats à l'étranger</button>
                </div>

                <div class="service-card">
                    <img src="@/assets/dog.png" alt="Garde d'animaux" class="service-icon" />
                    <h3>Garde d'animaux</h3>
                    <p>Prise en charge de vos compagnons pendant vos absences.</p>
                    <button class="service-button">Garde d'animaux</button>
                </div>

                <div class="service-card">
                    <img src="@/assets/aide.png" alt="Aide à domicile" class="service-icon" />
                    <h3>Aide à domicile</h3>
                    <p>Petits travaux, jardinage et assistance ponctuelle.</p>
                    <button class="service-button">Aide à domicile</button>
                </div>
            </div>
            <p class="services-footer">
                Un service <strong>flexible</strong>, <strong>éthique</strong> et adapté à <strong>tous</strong> !
            </p>
        </section>
    </div>
</template>


<style scoped>
    .hero-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 2rem;
        background-color: #f1f5f4;
    }

    .hero-content h1 {
        font-size: 2rem;
        margin-bottom: 2rem;
        color: #205B05;
    }

    .hero-text {
        flex: 1;
        padding-right: 2rem;
    }

    .hero-image {
        flex: 1;
    }

    .home_illustration {
        max-width: 100%;
        height: auto;
    }

    .hero-content p {
        font-size: 1.2rem;
        margin-bottom: 2rem;
        color: #205B05;
    }

    .hero-content .highlight {
        background: linear-gradient(120deg, #A8D08D 0%, #A8D08D 100%);
        background-size: 100% 0.4em;
        background-repeat: no-repeat;
        background-position: 0 88%;
        padding: 0 2px;
        color: #205B05;
        font-weight: bold;
    }

    .cta-button {
        background-color: #4CAF50;
        color: white;
        border: none;
        padding: 0.8rem 1.5rem;
        font-size: 1rem;
        border-radius: 4px;
        cursor: pointer;
        transition: background-color 0.3s;
    }

    .cta-button:hover {
        background-color: #45a049;
    }

    .services {
        padding: 4rem 2rem;
        background-color: #f1f5f4;
    }

    .services h2 {
        font-size: 2.5rem;
        text-align: left;
        margin-left: 4rem;
        margin-bottom: 1rem;
        color: #205B05;
    }

    .services p {
        font-size: 1.2rem;
        color: #205B05;
        text-align: left;
        margin-left: 4rem;
        margin-bottom: 2rem;
    }

    .services-container {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 3rem 0rem;
        text-align: center;
        justify-items: center;
    }

    .service-card {
        background-color: #f1f5f4;
        border-radius: 30px;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        padding: 1.5rem;
        width: 500px;
        max-width: 100%;
    }

    .service-icon {
        width: 200px;
        height: 200px;
        margin-bottom: 1rem;
    }

    .service-card h3 {
        font-size: 1.5rem;
        margin-bottom: 1rem;
        color: #346a18;
    }

    .service-card p {
        font-size: 1rem;
        color: #6a831d;
        text-align: center;
        margin-bottom: 1.5rem;
    }

    .service-button {
        background-color: #b6ca2f;
        color: #f1f5f4;
        border: none;
        padding: 0.5rem 1rem;
        font-size: 1rem;
        border-radius: 10px;
        cursor: pointer;
        transition: background-color 0.3s;
        text-align: left;
    }

    .service-button:hover {
        background-color: #173f01;
    }

    .services-footer {
        margin-top: 2rem;
        font-size: 1.2rem;
        color: #333;
    }

.how-it-works {
    padding: 4rem 2rem;
    text-align: center;
    background-color: #205B05;;
}

.how-it-works h2 {
    font-size: 2.5rem;
    color: #f1f5f4;
    margin-bottom: 2rem;
}
</style>
