<script>
import { addressAutocompleteService } from '@/services/mapsServices'

export default {
  name: 'MapComponent',
  props: {
    center: {
      type: Object,
      default: () => ({ lat: 48.8566, lng: 2.3522 }) // Paris par défaut
    },
    zoom: {
      type: Number,
      default: 10
    },
    mapHeight: {
      type: String,
      default: '400px'
    },
    markers: {
      type: Array,
      default: () => []
    },
    showRoute: {
      type: Boolean,
      default: false
    },
    routeWaypoints: {
      type: Array,
      default: () => []
    },
    showControls: {
      type: Boolean,
      default: true
    },
    interactive: {
      type: Boolean,
      default: true
    },
    theme: {
      type: String,
      default: 'default'
    }
  },
  data() {
    return {
      map: null,
      googleMaps: null,
      markersOnMap: [],
      routeRenderer: null,
      directionsService: null,
      loading: true,
      routeInfo: null,
      userLocationMarker: null
    }
  },
  async mounted() {
    await this.initializeMap()
  },
  watch: {
    markers: {
      handler: 'updateMarkers',
      deep: true
    },
    center: {
      handler: 'updateCenter',
      deep: true
    },
    showRoute: 'updateRoute'
  },
  methods: {
    async initializeMap() {
      try {
        const loaded = await addressAutocompleteService.loadGoogleMapsAPI()

        if (!loaded || !window.google || !window.google.maps) {
          throw new Error('Google Maps API non disponible')
        }

        this.googleMaps = window.google.maps

        const mapOptions = {
          center: this.center,
          zoom: this.zoom,
          disableDefaultUI: !this.interactive,
          gestureHandling: this.interactive ? 'auto' : 'none',
          styles: this.getMapStyles()
        }

        this.map = new window.google.maps.Map(this.$refs.map, mapOptions)
        this.directionsService = new window.google.maps.DirectionsService()
        this.routeRenderer = new window.google.maps.DirectionsRenderer({
          suppressMarkers: false,
          draggable: false
        })
        this.routeRenderer.setMap(this.map)

        this.loading = false
        this.updateMarkers()

        if (this.showRoute) {
          this.updateRoute()
        }

        console.log('Carte Google Maps initialisée avec succès')
        this.$emit('mapReady', this.map)
      } catch (error) {
        console.error('Erreur lors du chargement de Google Maps:', error)
        this.loading = false
        this.$emit('mapError', error)
      }
    },

    updateMarkers() {
      this.markersOnMap.forEach(marker => marker.setMap(null))
      this.markersOnMap = []

      if (window.google && window.google.maps) {
        this.markers.forEach(markerData => {
          const marker = new window.google.maps.Marker({
            position: { lat: markerData.lat, lng: markerData.lng },
            map: this.map,
            title: markerData.title || '',
            icon: markerData.icon || this.getDefaultIcon(markerData.type)
          })

          if (markerData.infoWindow) {
            const infoWindow = new window.google.maps.InfoWindow({
              content: markerData.infoWindow
            })

            marker.addListener('click', () => {
              infoWindow.open(this.map, marker)
            })
          }

          this.markersOnMap.push(marker)
        })
      }

      if (this.markers.length > 1) {
        this.fitBounds()
      }
    },

    updateCenter() {
      if (this.map) {
        this.map.setCenter(this.center)
      }
    },

    async updateRoute() {
      if (!this.showRoute || this.markers.length < 2) return

      const waypoints = this.routeWaypoints.map(point => ({
        location: { lat: point.lat, lng: point.lng },
        stopover: true
      }))

      const request = {
        origin: { lat: this.markers[0].lat, lng: this.markers[0].lng },
        destination: { lat: this.markers[this.markers.length - 1].lat, lng: this.markers[this.markers.length - 1].lng },
        waypoints: waypoints,
        travelMode: window.google.maps.TravelMode.DRIVING,
        unitSystem: window.google.maps.UnitSystem.METRIC
      }

      try {
        const result = await new Promise((resolve, reject) => {
          this.directionsService.route(request, (result, status) => {
            if (status === 'OK') {
              resolve(result)
            } else {
              reject(new Error('Erreur de calcul d\'itinéraire: ' + status))
            }
          })
        })

        this.routeRenderer.setDirections(result)

        const route = result.routes[0]
        if (route) {
          const leg = route.legs[0]
          this.routeInfo = {
            distance: (leg.distance.value / 1000).toFixed(1),
            duration: Math.round(leg.duration.value / 60)
          }
          this.$emit('routeCalculated', {
            distance: leg.distance.value / 1000,
            duration: leg.duration.value / 60,
            route: result
          })
        }
      } catch (error) {
        console.error('Erreur lors du calcul de l\'itinéraire:', error)
        this.$emit('routeError', error)
      }
    },

    getDefaultIcon(type) {
      const icons = {
        origin: 'https://maps.google.com/mapfiles/ms/icons/green-dot.png',
        destination: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',
        warehouse: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png',
        driver: 'https://maps.google.com/mapfiles/ms/icons/truck.png'
      }
      return icons[type] || 'https://maps.google.com/mapfiles/ms/icons/red-dot.png'
    },

    getMapStyles() {
      if (this.theme === 'dark') {
        return [
          { elementType: 'geometry', stylers: [{ color: '#242f3e' }] },
          { elementType: 'labels.text.stroke', stylers: [{ color: '#242f3e' }] },
          { elementType: 'labels.text.fill', stylers: [{ color: '#746855' }] }
        ]
      }
      return []
    },

    async centerOnUserLocation() {
      if (!navigator.geolocation) return

      try {
        const position = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject)
        })

        const userLocation = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        }

        this.map.setCenter(userLocation)
        this.map.setZoom(15)

        if (this.userLocationMarker) {
          this.userLocationMarker.setMap(null)
        }

        this.userLocationMarker = new window.google.maps.Marker({
          position: userLocation,
          map: this.map,
          icon: {
            url: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png'
          },
          title: 'Ma position'
        })

        this.$emit('userLocationFound', userLocation)
      } catch (error) {
        console.error('Erreur de géolocalisation:', error)
        this.$emit('locationError', error)
      }
    },

    fitBounds() {
      if (this.markersOnMap.length === 0 || !window.google || !window.google.maps) return

      const bounds = new window.google.maps.LatLngBounds()
      this.markersOnMap.forEach(marker => {
        bounds.extend(marker.getPosition())
      })
      this.map.fitBounds(bounds)
    },

    addMarker(markerData) {
      this.markers.push(markerData)
    },

    removeMarker(index) {
      if (this.markersOnMap[index]) {
        this.markersOnMap[index].setMap(null)
        this.markersOnMap.splice(index, 1)
        this.markers.splice(index, 1)
      }
    },

    setRoutePrice(price) {
      if (this.routeInfo) {
        this.routeInfo.price = price
      }
    }
  }
}
</script>

<template>
  <div class="map-component">
    <div ref="map" class="google-map" :style="{ height: mapHeight }"></div>

    <div v-if="showControls" class="map-controls">
      <button @click="centerOnUserLocation" class="control-btn" :disabled="loading">
        <i class="fas fa-location-arrow"></i>
        Ma position
      </button>
      <button @click="fitBounds" class="control-btn" v-if="markers.length > 1">
        <i class="fas fa-expand-arrows-alt"></i>
        Tout voir
      </button>
    </div>

    <div v-if="routeInfo" class="route-info">
      <div class="route-stats">
        <div class="stat-item">
          <i class="fas fa-route"></i>
          <span>{{ routeInfo.distance }} km</span>
        </div>
        <div class="stat-item">
          <i class="fas fa-clock"></i>
          <span>{{ routeInfo.duration }} min</span>
        </div>
        <div v-if="routeInfo.price" class="stat-item price">
          <i class="fas fa-euro-sign"></i>
          <span>{{ routeInfo.price }} €</span>
        </div>
      </div>
    </div>

    <div v-if="loading" class="map-loader">
      <i class="fas fa-spinner fa-spin"></i>
      <span>Chargement de la carte...</span>
    </div>
  </div>
</template>

<style scoped>
.map-component {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.google-map {
  width: 100%;
  min-height: 300px;
}

.map-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 10;
}

.control-btn {
  background: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.control-btn:hover:not(:disabled) {
  background: #f0f0f0;
  transform: translateY(-1px);
}

.control-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.route-info {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: white;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  z-index: 10;
}

.route-stats {
  display: flex;
  gap: 16px;
  align-items: center;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.stat-item i {
  width: 16px;
  text-align: center;
}

.stat-item.price {
  color: #2196f3;
  font-weight: 600;
}

.map-loader {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 100;
}

.map-loader i {
  font-size: 20px;
  color: #2196f3;
}

@media (max-width: 768px) {
  .route-stats {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .map-controls {
    flex-direction: row;
    top: auto;
    bottom: 10px;
    right: 10px;
  }
  
  .control-btn {
    padding: 6px 10px;
    font-size: 11px;
  }
}
</style>