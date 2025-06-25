import apiServices from './apiServices'

export const mapsServices = {
  async calculatePrice(priceRequest) {
    try {
      const requestData = {
        origin: priceRequest.origin,
        destination: priceRequest.destination,
        warehouse: priceRequest.warehouse || null,
        partialDelivery: priceRequest.partialDelivery || false,
        urgent: priceRequest.urgent || false,
        colis: {
          poids: priceRequest.colis?.poids || 1,
          longueur: priceRequest.colis?.longueur || 10,
          largeur: priceRequest.colis?.largeur || 10,
          hauteur: priceRequest.colis?.hauteur || 10,
          fragile: priceRequest.colis?.fragile || false,
          description: priceRequest.colis?.description || ''
        }
      }

      const response = await apiServices.post('/maps/calculate-price', requestData)
      return response.data
    } catch (error) {
      console.error('Erreur lors du calcul du prix:', error)
      throw error
    }
  },

  async getOptimalWarehouse(origin, destination) {
    try {
      const response = await apiServices.get('/maps/optimal-warehouse', {
        params: { origin, destination }
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la recherche d\'entrepôt optimal:', error)
      throw error
    }
  },

  async compareDeliveryOptions(comparisonRequest) {
    try {
      const requestData = {
        origin: comparisonRequest.origin,
        destination: comparisonRequest.destination,
        urgent: comparisonRequest.urgent || false,
        colis: {
          poids: comparisonRequest.colis?.poids || 1,
          longueur: comparisonRequest.colis?.longueur || 10,
          largeur: comparisonRequest.colis?.largeur || 10,
          hauteur: comparisonRequest.colis?.hauteur || 10,
          fragile: comparisonRequest.colis?.fragile || false,
          description: comparisonRequest.colis?.description || ''
        }
      }

      const response = await apiServices.post('/maps/compare-delivery-options', requestData)
      return response.data
    } catch (error) {
      console.error('Erreur lors de la comparaison des options:', error)
      throw error
    }
  },

  async geocodeAddress(address) {
    try {
      return await apiServices.geocodeAddress(address)
    } catch (error) {
      console.error('Erreur lors du géocodage:', error)
      throw error
    }
  },

  async calculateDistance(originCoords, destinationCoords) {
    try {
      if (!window.google || !window.google.maps) {
        const distance = this.calculateHaversineDistance(originCoords, destinationCoords)
        return {
          distance: distance,
          duration: Math.round((distance / 50) * 60) // Estimation: 50 km/h moyenne
        }
      }

      const service = new window.google.maps.DistanceMatrixService()
      return new Promise((resolve, reject) => {
        service.getDistanceMatrix({
          origins: [new window.google.maps.LatLng(originCoords.lat, originCoords.lng)],
          destinations: [new window.google.maps.LatLng(destinationCoords.lat, destinationCoords.lng)],
          travelMode: window.google.maps.TravelMode.DRIVING,
          unitSystem: window.google.maps.UnitSystem.METRIC
        }, (response, status) => {
          if (status === window.google.maps.DistanceMatrixStatus.OK) {
            const element = response.rows[0].elements[0]
            if (element.status === 'OK') {
              resolve({
                distance: element.distance.value / 1000, // Convertir en km
                duration: element.duration.value / 60    // Convertir en minutes
              })
            } else {
              const distance = this.calculateHaversineDistance(originCoords, destinationCoords)
              resolve({
                distance: distance,
                duration: Math.round((distance / 50) * 60)
              })
            }
          } else {
            reject(new Error('Erreur Google Maps Distance Matrix'))
          }
        })
      })
    } catch (error) {
      console.error('Erreur lors du calcul de distance:', error)
      const distance = this.calculateHaversineDistance(originCoords, destinationCoords)
      return {
        distance: distance,
        duration: Math.round((distance / 50) * 60)
      }
    }
  },

  async getAvailableWarehouses() {
    try {
      const response = await apiServices.get('/maps/warehouses')
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des entrepôts:', error)
      throw error
    }
  },

  async getOptimalWarehouses(origin, destination) {
    try {
      const response = await apiServices.get('/maps/optimal-warehouses', {
        params: { origin, destination }
      })
      return response.data
    } catch (error) {
      console.error('Erreur lors de la récupération des entrepôts optimaux:', error)
      throw error
    }
  },

  async calculateRealTimePrice(origin, destination, colis = null, isPartial = false, warehouse = null) {
    const priceRequest = {
      origin,
      destination,
      partialDelivery: isPartial,
      urgent: false,
      colis,
      warehouse
    }

    return await this.calculatePrice(priceRequest)
  },

  async getWarehouseSuggestions(origin, destination) {
    try {
      const warehouses = await this.getAvailableWarehouses()
      const optimal = await this.getOptimalWarehouse(origin, destination)

      return warehouses.map(warehouse => ({
        ...warehouse,
        isOptimal: warehouse.name === optimal.warehouse
      }))
    } catch (error) {
      console.error('Erreur lors de la récupération des suggestions d\'entrepôts:', error)
      return []
    }
  },

  async validateAndImproveAddress(address) {
    try {
      const result = await this.geocodeAddress(address)
      return {
        isValid: true,
        improvedAddress: result.address,
        coordinates: {
          lat: result.latitude,
          lng: result.longitude
        }
      }
    } catch (error) {
      return {
        isValid: false,
        error: error.message
      }
    }
  },

  prepareMapData(annonceData, includeWarehouse = false) {
    const markers = []

    if (annonceData.adresseDepart) {
      markers.push({
        type: 'origin',
        title: 'Adresse de départ',
        address: annonceData.adresseDepart,
        infoWindow: `<div><strong>Départ</strong><br>${annonceData.adresseDepart}</div>`
      })
    }

    if (includeWarehouse && annonceData.entrepotIntermediaire) {
      markers.push({
        type: 'warehouse',
        title: 'Entrepôt intermédiaire',
        address: `Entrepôt ${annonceData.entrepotIntermediaire}`,
        infoWindow: `<div><strong>Entrepôt</strong><br>${annonceData.entrepotIntermediaire}</div>`
      })
    }

    if (annonceData.adresseFin) {
      markers.push({
        type: 'destination',
        title: 'Adresse de livraison',
        address: annonceData.adresseFin,
        infoWindow: `<div><strong>Destination</strong><br>${annonceData.adresseFin}</div>`
      })
    }

    return {
      markers,
      showRoute: markers.length > 1,
      routeWaypoints: includeWarehouse && annonceData.entrepotIntermediaire ? 
        [{ address: `Entrepôt ${annonceData.entrepotIntermediaire}` }] : []
    }
  },

  formatPriceInfo(priceResult) {
    if (priceResult.type === 'DIRECTE') {
      return {
        type: 'Livraison directe',
        totalPrice: priceResult.totalPrice,
        distance: `${priceResult.distance} km`,
        duration: `${priceResult.duration} min`,
        segments: []
      }
    } else if (priceResult.type === 'PARTIELLE') {
      const savings = priceResult.savings || 0
      return {
        type: 'Livraison partielle',
        totalPrice: priceResult.totalPrice,
        distance: `${priceResult.totalDistance} km`,
        duration: `${priceResult.totalDuration} min`,
        warehouse: priceResult.warehouse,
        segments: [
          {
            number: 1,
            price: priceResult.segment1Price,
            distance: `${priceResult.segment1Distance} km`,
            duration: `${priceResult.segment1Duration} min`,
            route: 'Départ → Entrepôt'
          },
          {
            number: 2,
            price: priceResult.segment2Price,
            distance: `${priceResult.segment2Distance} km`,
            duration: `${priceResult.segment2Duration} min`,
            route: 'Entrepôt → Destination'
          }
        ],
        comparison: {
          directPrice: priceResult.directPrice,
          savings: savings,
          isMoreExpensive: priceResult.isMoreExpensive,
          recommendation: savings > 0 ? 'Économie réalisée' : 'Plus cher que livraison directe'
        }
      }
    }

    return {
      type: 'Prix calculé',
      totalPrice: priceResult.totalPrice || priceResult.price || 0,
      distance: `${priceResult.distance || 0} km`,
      duration: `${priceResult.duration || 0} min`,
      segments: []
    }
  },

  estimateETA(currentPosition, destination, trafficFactor = 1.2) {
    // Calcul approximatif - dans une vraie implémentation, utiliser l'API Google Maps
    const distance = this.calculateHaversineDistance(currentPosition, destination)
    const baseTimeMinutes = (distance / 50) * 60 // 50 km/h en moyenne en ville
    return Math.round(baseTimeMinutes * trafficFactor)
  },

  calculateHaversineDistance(pos1, pos2) {
    const R = 6371 // Rayon de la Terre en km
    const dLat = this.toRad(pos2.lat - pos1.lat)
    const dLon = this.toRad(pos2.lng - pos1.lng)
    const lat1 = this.toRad(pos1.lat)
    const lat2 = this.toRad(pos2.lat)

    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    return R * c
  },

  toRad(value) {
    return value * Math.PI / 180
  }
}

import { Loader } from '@googlemaps/js-api-loader'

export const addressAutocompleteService = {
  autocompleteService: null,
  placesService: null,
  googleMapsLoaded: false,
  loadingPromise: null,

  async loadGoogleMapsAPI() {
    if (this.googleMapsLoaded && window.google && window.google.maps) {
      return true
    }

    if (this.loadingPromise) {
      return this.loadingPromise
    }

    this.loadingPromise = new Promise(async (resolve, reject) => {
      try {
        const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY
        if (!apiKey) {
          console.warn('Clé API Google Maps non configurée')
          resolve(false)
          return
        }

        const loader = new Loader({
          apiKey: apiKey,
          version: 'weekly',
          libraries: ['places', 'geometry']
        })

        await loader.load()
        this.googleMapsLoaded = true
        console.log('Google Maps API chargée avec succès')
        resolve(true)
      } catch (error) {
        console.error('Erreur lors du chargement de Google Maps API:', error)
        resolve(false)
      }
    })

    return this.loadingPromise
  },

  async initialize() {
    const loaded = await this.loadGoogleMapsAPI()
    if (loaded && window.google && window.google.maps) {
      try {
        this.autocompleteService = new window.google.maps.places.AutocompleteService()
        return true
      } catch (error) {
        console.error('Erreur lors de l\'initialisation du service d\'autocomplétion:', error)
        return false
      }
    }
    return false
  },

  async getSuggestions(input) {
    try {
      if (!this.autocompleteService) {
        const initialized = await this.initialize()
        if (!initialized) {
          console.warn('Impossible d\'initialiser le service d\'autocomplétion')
          return []
        }
      }

      return new Promise((resolve, reject) => {
        if (!this.autocompleteService) {
          reject(new Error('Service d\'autocomplétion non disponible'))
          return
        }

        this.autocompleteService.getPlacePredictions({
          input: input,
          componentRestrictions: { country: 'fr' },
          types: ['address']
        }, (predictions, status) => {
          if (status === window.google.maps.places.PlacesServiceStatus.OK) {
            resolve(predictions.map(prediction => ({
              id: prediction.place_id,
              description: prediction.description,
              mainText: prediction.structured_formatting.main_text,
              secondaryText: prediction.structured_formatting.secondary_text
            })))
          } else if (status === window.google.maps.places.PlacesServiceStatus.ZERO_RESULTS) {
            resolve([])
          } else {
            console.warn('Statut autocomplétion Google Maps:', status)
            resolve([])
          }
        })
      })
    } catch (error) {
      console.error('Erreur dans getSuggestions:', error)
      return []
    }
  },

  async getPlaceDetails(placeId) {
    try {
      if (!this.googleMapsLoaded) {
        const loaded = await this.loadGoogleMapsAPI()
        if (!loaded) {
          throw new Error('Google Maps API non disponible')
        }
      }

      return new Promise((resolve, reject) => {
        if (!this.placesService) {
          try {
            const div = document.createElement('div')
            this.placesService = new window.google.maps.places.PlacesService(div)
          } catch (error) {
            reject(new Error('Impossible de créer le service Places'))
            return
          }
        }

        this.placesService.getDetails({
          placeId: placeId,
          fields: ['formatted_address', 'geometry', 'name']
        }, (place, status) => {
          if (status === window.google.maps.places.PlacesServiceStatus.OK) {
            try {
              resolve({
                address: place.formatted_address,
                name: place.name || '',
                coordinates: {
                  lat: place.geometry.location.lat(),
                  lng: place.geometry.location.lng()
                }
              })
            } catch (error) {
              reject(new Error('Erreur lors du traitement des détails du lieu'))
            }
          } else {
            console.warn('Statut Google Places:', status)
            reject(new Error('Impossible de récupérer les détails du lieu'))
          }
        })
      })
    } catch (error) {
      console.error('Erreur dans getPlaceDetails:', error)
      throw error
    }
  },

  isGoogleMapsLoaded() {
    return this.googleMapsLoaded && window.google && window.google.maps
  },

  reset() {
    this.autocompleteService = null
    this.placesService = null
    this.googleMapsLoaded = false
    this.loadingPromise = null
  }
}