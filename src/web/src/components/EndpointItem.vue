<script>
export default {
  data() {
    return {
      endpoints: []
    }
  },
  methods: {
    async getEndpoints() {
      await fetch(import.meta.env.VITE_SPACENOW_API_BASE_URL + '/endpoint', {
        method: 'GET',
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(response => {
        return response.json().then(json => {
          return response.ok ? json : Promise.reject(json)
        })
      }).then(data => {
        this.endpoints = data
      }).catch(error => {
        console.log(error)
      })
    },
    async deleteEndpoint(event) {
      const deleteId = event.target.attributes['data-id'].value
      await fetch(import.meta.env.VITE_SPACENOW_API_BASE_URL + `/endpoint/${deleteId}`, {
        method: 'DELETE',
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(() => {
        this.getEndpoints()
      })
    }
  },
  mounted() {
    this.getEndpoints()
  }
}
</script>

<template>
  <div class="container">
    <div class="card mb-4" v-for="endpoint in endpoints">
      <div class="card-header clearfix">
        <div class="float-start" v-text="endpoint.webhookUrl"></div>
        <div class="float-end">
          <button type="button" class="btn-close" :data-id="endpoint.id" @click="deleteEndpoint" aria-label="削除"></button>
        </div>
      </div>
      <div class="card-body">
        <div>監視対象ユーザ名一覧</div>
        <ul class="card-text">
          <li v-for="username in endpoint.usernames" v-text="username"></li>
        </ul>
      </div>
    </div>
  </div>
</template>

