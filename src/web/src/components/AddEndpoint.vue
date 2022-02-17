<script>
import { Modal } from 'bootstrap'

export default {
  emits: ['addEvent'],
  data() {
    return {
      formData: {
        webhookUrl: '',
        creatorUsernames: ''
      },
      formValidation: {
        webhookUrl: {
          isValid: false,
          isInvalid: false,
          message: ''
        },
        creatorUsernames: {
          isValid: false,
          isInvalid: false,
          message: ''
        }
      },
      addModal: null,
      addModalInstance: null,
      endpointForm: null
    }
  },
  methods: {
    async saveEndpoint() {
      await fetch(import.meta.env.VITE_SPACENOW_API_BASE_URL + '/endpoint', {
        method: 'POST',
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          webhookUrl: this.formData.webhookUrl,
          creatorUsernames: this.formData.creatorUsernames.split("\n").map(s => s.trim()).filter(s => s.length > 0)
        })
      }).then(response => {
        return response.json().then(json => {
          return response.ok ? json : Promise.reject(json)
        })
      }).then(() => {
        this.addModalInstance.toggle()
        this.resetForm()
        this.$emit('addEvent')
      }).catch(error => {
        this.resetValidation(true, false)
        error.errors.forEach(e => {
          const fieldKey = e.field.startsWith('creatorUsernames') ? 'creatorUsernames' : e.field
          this.formValidation[fieldKey] = {
            isValid: false,
            isInvalid: true,
            message: e.defaultMessage
          }
        })
      })
    },
    resetForm() {
      this.endpointForm.reset()
      this.resetValidation(false, false)
      Object.keys(this.formData).forEach(k => this.formData[k] = '')
    },
    resetValidation(valid, invalid) {
      Object.keys(this.formValidation).forEach(k => {
        this.formValidation[k] = {
          isValid: valid,
          isInvalid: invalid
        }
      })
    }
  },
  mounted() {
    this.addModalInstance = new Modal(this.addModal);
  }
}
</script>

<template>
  <div class="container d-flex align-items-center mb-4">
    <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addModal">
      <i class="bi-plus-lg"></i>エンドポイントを追加
    </button>
  </div>

  <!-- Modal -->
  <div class="modal fade" id="addModal" :ref="(el) => addModal = el" tabindex="-1" aria-labelledby="addModalLabel" aria-hidden="true">
    <div class="modal-dialog">
      <div class="modal-content">
        <form id="endpointForm" :ref="(el) => endpointForm = el">
          <div class="modal-header">
            <h5 class="modal-title" id="addModalLabel">新しいエンドポイント</h5>
            <button type="button" class="btn-close" @click="resetForm" data-bs-dismiss="modal" aria-label="閉じる"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="webhookUrl" class="form-label">Webhook URL</label>
              <input type="text" class="form-control" :class="{ 'is-valid': formValidation.webhookUrl.isValid, 'is-invalid': formValidation.webhookUrl.isInvalid }" id="webhookUrl" v-model="formData.webhookUrl">
              <div class="invalid-feedback" v-text="formValidation.webhookUrl.message"></div>
            </div>
            <div class="mb-3">
              <label for="creatorUsernames" class="form-label">監視対象 Twitter ユーザ名</label>
              <textarea class="form-control" id="creatorUsernames" :class="{ 'is-valid': formValidation.creatorUsernames.isValid, 'is-invalid': formValidation.creatorUsernames.isInvalid }" v-model="formData.creatorUsernames" row="5"></textarea>
              <div class="invalid-feedback" v-text="formValidation.creatorUsernames.message"></div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="resetForm" data-bs-dismiss="modal">キャンセル</button>
            <button type="button" class="btn btn-primary" @click="saveEndpoint">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

