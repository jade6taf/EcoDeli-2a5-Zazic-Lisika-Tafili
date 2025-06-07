<script>
export default {
  name: 'AddressAutocomplete',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: 'Saisissez une adresse...'
    },
    required: {
      type: Boolean,
      default: false
    },
    inputId: {
      type: String,
      default: () => `address-input-${Math.random().toString(36).substr(2, 9)}`
    }
  },
  emits: ['update:modelValue', 'address-selected'],
  data() {
    return {
      inputValue: '',
      suggestions: [],
      showSuggestions: false,
      isLoading: false,
      highlightedIndex: -1,
      debounceTimeout: null,
      abortController: null,
      isFocused: false
    }
  },
  watch: {
    modelValue: {
      immediate: true,
      handler(newValue) {
        this.inputValue = newValue || '';
      }
    }
  },
  methods: {
    onInput() {
      this.$emit('update:modelValue', this.inputValue);
      this.searchAddresses();
    },

    onFocus() {
      this.isFocused = true;
      if (this.inputValue.length >= 3 && this.suggestions.length > 0) {
        this.showSuggestions = true;
      }
    },

    onBlur() {
      setTimeout(() => {
        this.isFocused = false;
        this.showSuggestions = false;
        this.highlightedIndex = -1;
      }, 200);
    },

    onKeydown(event) {
      if (!this.showSuggestions || this.suggestions.length === 0) return;

      switch (event.key) {
        case 'ArrowDown':
          event.preventDefault();
          this.highlightedIndex = Math.min(
            this.highlightedIndex + 1,
            this.suggestions.length - 1
          );
          break;
        case 'ArrowUp':
          event.preventDefault();
          this.highlightedIndex = Math.max(this.highlightedIndex - 1, -1);
          break;
        case 'Enter':
          event.preventDefault();
          if (this.highlightedIndex >= 0) {
            this.selectSuggestion(this.suggestions[this.highlightedIndex]);
          }
          break;
        case 'Escape':
          this.showSuggestions = false;
          this.highlightedIndex = -1;
          break;
      }
    },

    searchAddresses() {
      if (this.debounceTimeout) {
        clearTimeout(this.debounceTimeout);
      }
      if (this.abortController) {
        this.abortController.abort();
      }

      const query = this.inputValue.trim();

      if (query.length < 3) {
        this.suggestions = [];
        this.showSuggestions = false;
        this.isLoading = false;
        return;
      }

      this.debounceTimeout = setTimeout(() => {
        this.performSearch(query);
      }, 300);
    },

    async performSearch(query) {
      this.isLoading = true;
      this.abortController = new AbortController();

      try {
        const response = await fetch(
          `/api/addresses/search?q=${encodeURIComponent(query)}`,
          {
            signal: this.abortController.signal,
            headers: {
              'Content-Type': 'application/json'
            }
          }
        );

        if (response.ok) {
          const data = await response.json();
          this.suggestions = data || [];
          this.showSuggestions = this.isFocused && this.suggestions.length > 0;
          this.highlightedIndex = -1;
        }
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error('Erreur lors de la recherche d\'adresses:', error);
          this.suggestions = [];
        }
      } finally {
        this.isLoading = false;
      }
    },

    selectSuggestion(suggestion) {
      this.inputValue = this.formatMainAddress(suggestion);
      this.$emit('update:modelValue', this.inputValue);
      this.$emit('address-selected', {
        address: this.inputValue,
        fullSuggestion: suggestion
      });

      this.showSuggestions = false;
      this.highlightedIndex = -1;
      this.suggestions = [];
    },

    formatMainAddress(suggestion) {
      const components = suggestion.address_components;
      if (components) {
        const parts = [];
        if (components.house_number)
          parts.push(components.house_number);
        if (components.road)
          parts.push(components.road);
        if (components.postcode)
          parts.push(components.postcode);
        if (components.city)
          parts.push(components.city);

        if (parts.length > 0) {
          return parts.join(' ');
        }
      }

      const displayName = suggestion.display_name;
      const parts = displayName.split(',').slice(0, 3);
      return parts.join(',').trim();
    }
  },

  beforeUnmount() {
    if (this.debounceTimeout) {
      clearTimeout(this.debounceTimeout);
    }
    if (this.abortController) {
      this.abortController.abort();
    }
  }
}
</script>

<template>
  <div class="address-autocomplete">
    <div class="input-container">
      <input
        :id="inputId"
        v-model="inputValue"
        type="text"
        :placeholder="placeholder"
        :required="required"
        @input="onInput"
        @focus="onFocus"
        @blur="onBlur"
        @keydown="onKeydown"
        :class="{ 'has-suggestions': showSuggestions && suggestions.length > 0 }"
      />
      <div v-if="isLoading" class="loading-indicator">
        <i class="fas fa-spinner fa-spin"></i>
      </div>
    </div>

    <div v-if="showSuggestions" class="suggestions-dropdown">
      <div
        v-for="(suggestion, index) in suggestions"
        :key="index"
        :class="[
          'suggestion-item',
          { 'highlighted': index === highlightedIndex }
        ]"
        @click="selectSuggestion(suggestion)"
        @mouseenter="highlightedIndex = index"
      >
        <div class="suggestion-content">
          <i class="fas fa-map-marker-alt suggestion-icon"></i>
          <div class="suggestion-text">
            <div class="main-address">{{ formatMainAddress(suggestion) }}</div>
            <div class="sub-address">{{ suggestion.display_name }}</div>
          </div>
        </div>
      </div>

      <div v-if="suggestions.length === 0 && !isLoading && inputValue.length >= 3" class="no-results">
        <i class="fas fa-search"></i>
        <span>Aucune adresse trouvée</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.address-autocomplete {
  position: relative;
  width: 100%;
}

.input-container {
  position: relative;
  width: 100%;
}

.input-container input {
  width: 100%;
  padding: 0.75rem;
  padding-right: 2.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.input-container input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-container input.has-suggestions {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

.loading-indicator {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 2px solid #3b82f6;
  border-top: none;
  border-radius: 0 0 8px 8px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.suggestion-item {
  padding: 0.75rem;
  cursor: pointer;
  border-bottom: 1px solid #f3f4f6;
  transition: background-color 0.2s;
}

.suggestion-item:hover,
.suggestion-item.highlighted {
  background-color: #f0f9ff;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-content {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.suggestion-icon {
  color: #3b82f6;
  margin-top: 0.25rem;
  flex-shrink: 0;
}

.suggestion-text {
  flex: 1;
  min-width: 0;
}

.main-address {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub-address {
  font-size: 0.875rem;
  color: #6b7280;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.no-results {
  padding: 1rem;
  text-align: center;
  color: #6b7280;
  font-style: italic;
}

.no-results i {
  margin-right: 0.5rem;
}

/* Responsive */
@media (max-width: 768px) {
  .suggestions-dropdown {
    max-height: 150px;
  }

  .suggestion-item {
    padding: 0.5rem;
  }

  .main-address {
    font-size: 0.875rem;
  }

  .sub-address {
    font-size: 0.75rem;
  }
}

/* Dark theme support */
@media (prefers-color-scheme: dark) {
  .suggestions-dropdown {
    background: #1f2937;
    border-color: #60a5fa;
  }

  .suggestion-item {
    border-bottom-color: #374151;
  }

  .suggestion-item:hover,
  .suggestion-item.highlighted {
    background-color: #374151;
  }

  .main-address {
    color: #f9fafb;
  }

  .sub-address {
    color: #d1d5db;
  }

  .suggestion-icon {
    color: #60a5fa;
  }

  .no-results {
    color: #9ca3af;
  }

  .loading-indicator {
    color: #9ca3af;
  }
}
</style>
