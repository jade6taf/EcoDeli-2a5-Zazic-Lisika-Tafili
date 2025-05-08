<script setup>
import { ref, watch, onMounted, nextTick } from 'vue';
import { LMap, LTileLayer, LMarker, LTooltip } from "@vue-leaflet/vue-leaflet";

import 'leaflet/dist/leaflet.css';

const props = defineProps({
  startLatitude: Number,
  startLongitude: Number,
  endLatitude: Number,
  endLongitude: Number,
});

const map = ref(null);
const zoom = ref(6);
const defaultCenter = ref([46.603354, 1.888334]);

const center = ref(
  props.startLatitude && props.startLongitude
    ? [props.startLatitude, props.startLongitude]
    : defaultCenter.value
);

const startCoords = ref(null);
const endCoords = ref(null);

function updateMarkersAndBounds() {
  const newStartCoords = props.startLatitude && props.startLongitude ? [props.startLatitude, props.startLongitude] : null;
  const newEndCoords = props.endLatitude && props.endLongitude ? [props.endLatitude, props.endLongitude] : null;

  startCoords.value = newStartCoords;
  endCoords.value = newEndCoords;

  nextTick(() => {
    if (map.value) {
      const leafletMap = map.value;
      const validCoords = [];
      if (newStartCoords) validCoords.push(newStartCoords);
      if (newEndCoords) validCoords.push(newEndCoords);

      if (validCoords.length > 1) {
        leafletMap.fitBounds([newStartCoords, newEndCoords], { padding: [50, 50], maxZoom: 15 });
      } else if (newStartCoords) {
        leafletMap.setView(newStartCoords, 13);
      } else if (newEndCoords) {
        leafletMap.setView(newEndCoords, 13);
      } else {
        leafletMap.setView(defaultCenter.value, zoom.value);
      }
    }
  });
}

watch(() => [props.startLatitude, props.startLongitude, props.endLatitude, props.endLongitude], () => {
  updateMarkersAndBounds();
}, { immediate: true });

onMounted(() => {
  updateMarkersAndBounds();
});
</script>

<template>
  <div style="height: 400px; width: 100%;">
    <l-map
      ref="map"
      :zoom="zoom"
      :center="center"
      :use-global-leaflet="false"
    >
      <l-tile-layer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        layer-type="base"
        name="OpenStreetMap"
      ></l-tile-layer>
      <l-marker v-if="startCoords" :lat-lng="startCoords">
        <l-tooltip>Point de départ</l-tooltip>
      </l-marker>
      <l-marker v-if="endCoords" :lat-lng="endCoords">
        <l-tooltip>Point d'arrivée</l-tooltip>
      </l-marker>
    </l-map>
  </div>
</template>

<style>
@import 'leaflet/dist/leaflet.css';
</style>