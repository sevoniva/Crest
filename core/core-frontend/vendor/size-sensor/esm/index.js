const sensors = new WeakMap()

export const ver = '1.0.3'

export function bind(element, cb) {
  if (!element || typeof ResizeObserver === 'undefined') {
    return () => {}
  }
  let callbacks = sensors.get(element)
  if (!callbacks) {
    const listeners = new Set()
    const observer = new ResizeObserver(() => {
      listeners.forEach(listener => listener())
    })
    observer.observe(element)
    callbacks = { listeners, observer }
    sensors.set(element, callbacks)
  }
  callbacks.listeners.add(cb)
  return () => {
    callbacks.listeners.delete(cb)
    if (!callbacks.listeners.size) {
      callbacks.observer.disconnect()
      sensors.delete(element)
    }
  }
}

export function clear(element) {
  const callbacks = sensors.get(element)
  if (!callbacks) {
    return
  }
  callbacks.observer.disconnect()
  sensors.delete(element)
}
