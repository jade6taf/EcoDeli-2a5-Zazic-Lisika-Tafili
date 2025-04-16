<template>
    <div class="home">

        <section class="hero">
            <h1 class="hero-title">Réinventons la livraison ensemble – écoresponsable, solidaire et locale</h1>
            <div class="hero-content">
                <div class="hero-text">
                    <p>Chez EcoDeli, chacun peut devenir acteur de la mobilité durable.<br>
                    En livrant un colis ou en rendant un service, vous participez à un réseau<br>
                    collaboratif qui crée du lien et réduit l'impact écologique.</p>
                </div>
                <div class="hero-image">
                    <img src="@/assets/accueil_screen.png" alt="Accueil" />
                </div>
            </div>
        </section>

        <section class="services">
            <h2 class="services-title">Nos services</h2>
            <p class="services-description">
                <strong>EcoDeli</strong>, c'est bien plus qu'un service de livraison ! Nous proposons :
            </p>
            <div class="services-grid">
                <div class="service-card" v-for="(service, index) in services" :key="index" @mouseover="hoverService(index)" @mouseleave="hoverService(-1)">
                    <div class="service-content">
                        <h3 class="service-title">{{ service.title }}</h3>
                        <p class="service-description">{{ service.description }}</p>
                    </div>
                </div>
            </div>
            <p class="services-footer">
                Un service <strong>flexible</strong>, <strong>éthique</strong> et adapté à <strong>tous</strong> !
            </p>
        </section>
    </div>
</template>


<script>
export default {
    data() {
        return {
            hoveredService: -1,
            hoveredStep: 0,
            services: [
                { title: "Livraison de colis", description: "Expédiez vos colis à moindre coût grâce à notre réseau de livreurs occasionnels." },
                { title: "Lâcher vos chariots", description: "Faites vos courses chez nos commerçants partenaires et faites-vous livrer à domicile." },
                { title: "Transport de personnes", description: "Un service de transport pour les personnes âgées, à mobilités réduites." },
                { title: "Achats à l'étranger", description: "Demandez à un voyageur de vous rapporter des produits introuvables en France." },
                { title: "Garde d'animaux", description: "Prise en charge de vos compagnons pendant vos absences." },
                { title: "Aide à domicile", description: "Petits travaux, jardinage et assistance ponctuelle." }
            ]
        };
    },
    methods: {
        hoverService(index) {
            this.hoveredService = index;
        },
        hoverStep(step) {
            this.hoveredStep = step;
        }
    }
};
</script>


<style scoped>
.home {
    background-color: #f1f5f4;
    min-height: 100vh;
}

.hero {
    padding: 2rem;
    max-width: 1400px;
    margin: 0 auto;
}

.hero-title {
    color: #A5C53D;
    font-size: 2.5rem;
    line-height: 1.2;
    max-width: 100%;
    margin: 2rem 0 4rem -4rem;
    text-align: left;
    white-space: nowrap;
}

.hero-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 6rem;
    margin-top: 2rem;
}

.hero-image {
    flex: 1.4;
    display: flex;
    justify-content: flex-start;
    margin-left: -2rem;
}

.hero-image img {
    width: 100%;
    height: auto;
    max-width: 1200px;
    object-fit: cover;
    border-radius: 10px;
}

.hero-text {
    flex: 0.6;
    text-align: left;
    padding-right: 2rem;
}

.hero-content p {
    color: #205B01;
    font-size: 1.3rem;
    line-height: 1.8;
    margin-right: 2rem;
    white-space: pr;
}


.services {
    padding: 4rem 2rem;
    text-align: left;
    margin-left: 4rem;
    background-color: #f1f5f4;
}

.services-title {
    font-size: 2rem;
    color: #95B728;
    margin-bottom: 1rem;
}

.services-description {
    font-size: 1.2rem;
    color: #84A470;
    margin-bottom: 2rem;
}

.services-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;
}

.service-card {
    background-color: #ffffff;
    border-radius: 10px;
    padding: 2rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s, box-shadow 0.3s;
    cursor: pointer;
}

.service-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 6px 10px rgba(0, 0, 0, 0.15);
    background-color: #F5FCDD;
}

.service-content {
    text-align: center;
}

.service-title {
    font-size: 1.5rem;
    color: #CEDF95;
    margin-bottom: 1rem;
}

.service-description {
    font-size: 1rem;
    color: #346A18;
    line-height: 1.5;
}

.services-footer {
    margin-top: 2rem;
    font-size: 1.2rem;
    color: #84A470;
}

</style>
