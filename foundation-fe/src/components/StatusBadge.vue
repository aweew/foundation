<template>
  <span v-if="label" class="status-badge" :class="`status-badge--${badgeType}`">
    <span class="status-badge__dot" aria-hidden="true" />
    <span class="status-badge__label">{{ label }}</span>
  </span>
  <span v-else>-</span>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useEnumDictionary } from '@/composables/useEnumDictionary'
import type { EnumOption } from '@/api/system/types'

type BadgeType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

const props = withDefaults(defineProps<{
  enumName?: string;
  value: unknown;
  type?: BadgeType;
}>(), { enumName: 'StatusEnum', type: undefined })

const { enumDictionary, loadEnums } = useEnumDictionary()

const enumOption = computed<EnumOption | undefined>(() => {
  const options = enumDictionary[props.enumName] ?? []
  return options.find(option => option.name === props.value || String(option.value) === String(props.value))
})

const label = computed(() => enumOption.value?.label ?? (props.value === null || props.value === undefined ? '' : String(props.value)))

const badgeType = computed<BadgeType>(() => {
  if (props.type) return props.type
  const name = enumOption.value?.name?.toUpperCase() ?? ''
  if (/(ENABLE|ACTIVE|SUCCESS|NORMAL|PASS|OPEN)/.test(name)) return 'success'
  if (/(DISABLE|INACTIVE|ERROR|FAIL|CLOSED|LOCK)/.test(name)) return 'danger'
  if (/(WAIT|PENDING|PROCESS)/.test(name)) return 'warning'
  return 'info'
})

const ensureEnumsLoaded = async () => {
  if (!enumDictionary[props.enumName]) await loadEnums(props.enumName)
}

onMounted(() => ensureEnumsLoaded().catch(() => undefined))
watch(() => props.enumName, () => ensureEnumsLoaded().catch(() => undefined))
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 24px;
  padding: 3px 9px 3px 7px;
  border: 1px solid;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  line-height: 16px;
  white-space: nowrap;
}

.status-badge__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  box-shadow: 0 0 0 3px currentColor;
  opacity: .22;
}

.status-badge__label {
  color: inherit;
}

.status-badge--success {
  color: #15803d;
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.status-badge--success .status-badge__dot {
  background: #16a34a;
}

.status-badge--danger {
  color: #b42318;
  border-color: #fecaca;
  background: #fff5f5;
}

.status-badge--danger .status-badge__dot {
  background: #dc2626;
}

.status-badge--warning {
  color: #a15c07;
  border-color: #fed7aa;
  background: #fffaf0;
}

.status-badge--warning .status-badge__dot {
  background: #f59e0b;
}

.status-badge--primary {
  color: #1d4ed8;
  border-color: #bfdbfe;
  background: #eff6ff;
}

.status-badge--primary .status-badge__dot {
  background: #2563eb;
}

.status-badge--info {
  color: #475569;
  border-color: #cbd5e1;
  background: #f8fafc;
}

.status-badge--info .status-badge__dot {
  background: #64748b;
}
</style>
