<script setup>
import AddEndpoint from './components/AddEndpoint.vue'
import EndpointItem from './components/EndpointItem.vue'
</script>
<script>
export default {
  emits: ['needReload'],
  methods: {
    addedEndpoint() {
      this.$refs.endpointItem.getEndpoints()
    }
  }
}
</script>

<template>
  <div class="container">
    <header class="d-flex flex-wrap align-items-center py-3 mb-4 border-bottom">
      <span class="fs-4">Space Now</span>
    </header>
  </div>
  <AddEndpoint @add-event="addedEndpoint" />
  <EndpointItem ref="endpointItem" />
</template>

