<script>
export default {
  name: 'CategorySelector',
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue', 'category-selected'],
  data() {
    return {
      categories: [
        {
          code: 'TRANSPORT_LIVRAISON',
          icon: '🛻',
          libelle: 'TRANSPORT & LIVRAISON',
          description: 'Transport de personnes, livraison de colis, déménagement, courses urgentes'
        },
        {
          code: 'SERVICES_DOMICILE',
          icon: '🏠',
          libelle: 'SERVICES À DOMICILE',
          description: 'Ménage, garde d\'enfants/animaux, jardinage, assistance aux personnes âgées'
        },
        {
          code: 'TRAVAUX_REPARATIONS',
          icon: '🔧',
          libelle: 'TRAVAUX & RÉPARATIONS',
          description: 'Bricolage, plomberie, électricité, rénovation, assemblage de meubles'
        },
        {
          code: 'COURSES_ACHATS',
          icon: '🛒',
          libelle: 'COURSES & ACHATS',
          description: 'Courses alimentaires, achats divers, recherche de produits spécifiques'
        },
        {
          code: 'SERVICES_PERSONNELS',
          icon: '👥',
          libelle: 'SERVICES PERSONNELS',
          description: 'Assistance administrative, organisation d\'événements, secrétariat, conciergerie'
        },
        {
          code: 'EDUCATION_FORMATION',
          icon: '🎓',
          libelle: 'ÉDUCATION & FORMATION',
          description: 'Cours particuliers, formation professionnelle, coaching, soutien scolaire'
        }
      ]
    }
  },
  computed: {
    selectedCategory() {
      return this.modelValue
    }
  },
  methods: {
    selectCategory(categoryCode) {
      this.$emit('update:modelValue', categoryCode)
      this.$emit('category-selected', categoryCode)
    },
    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code)
      return category ? category.libelle : ''
    }
  }
}
</script>

<template>
  <div class="category-selector">
    <h2>Étape 1: Choisissez votre catégorie de service</h2>

    <div class="categories-grid">
      <div
        v-for="category in categories"
        :key="category.code"
        class="category-card"
        :class="{ 'selected': selectedCategory === category.code }"
        @click="selectCategory(category.code)"
      >
        <div class="category-icon">{{ category.icon }}</div>
        <h3>{{ category.libelle }}</h3>
        <p>{{ category.description }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.category-selector h2 {
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.category-card {
  padding: 1.5rem;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  background-color: var(--card-bg);
  position: relative;
  overflow: hidden;
}

.category-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.category-card:hover::before {
  left: 100%;
}

.category-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.category-card.selected {
  border-color: var(--primary-color);
  background: linear-gradient(135deg, var(--primary-color-light, #f8f9ff) 0%, var(--card-bg) 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
}

.category-card.selected::after {
  content: '✓';
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: var(--primary-color);
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: bold;
}

.category-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  filter: grayscale(0.3);
  transition: filter 0.3s ease;
}

.category-card:hover .category-icon,
.category-card.selected .category-icon {
  filter: grayscale(0);
}

.category-card h3 {
  color: var(--text-primary);
  margin: 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  transition: color 0.3s ease;
}

.category-card.selected h3 {
  color: var(--primary-color);
}

.category-card p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.category-card:hover p,
.category-card.selected p {
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .categories-grid {
    grid-template-columns: 1fr;
    gap: 0.8rem;
  }
  
  .category-card {
    padding: 1.2rem;
  }
  
  .category-icon {
    font-size: 2rem;
  }
  
  .category-card h3 {
    font-size: 1rem;
  }
  
  .category-card p {
    font-size: 0.85rem;
  }
}
</style>
