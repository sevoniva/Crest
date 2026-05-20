type Listener = (...args: any[]) => void

const noop = () => undefined

class StubMap {
  deMapProvider?: string
  deMapAutoFit?: boolean
  deMapAutoZoom?: number
  deMapAutoLng?: number
  deMapAutoLat?: number
  showLabel?: boolean
  dragPan = { disable: noop }
  scrollZoom = { disable: noop }
  doubleClickZoom = { disable: noop }
  dragRotate = { disable: noop }
  touchPitch = { disable: noop }
  touchZoomRotate = { disable: noop }

  constructor(options?: Record<string, any>) {
    Object.assign(this, options)
  }

  on(_event: string, listener?: Listener) {
    listener?.()
  }

  getCenter() {
    return {
      lng: 0,
      lat: 0,
      getLng: () => 0,
      getLat: () => 0
    }
  }

  getZoom() {
    return 0
  }

  setStatus() {}
  setBaseMap() {}
  setDraggable() {}
  setScrollable() {}
  setDoubleClickZoom() {}
  setTouchZoomable() {}
  setPitchable() {}
  setRotatable() {}
  checkResize() {}
  removeStyle() {}
}

export class Scene {
  map: any
  loaded = true

  constructor(options?: Record<string, any>) {
    this.map = options?.map ?? new StubMap()
  }

  once(_event: string, listener?: Listener) {
    listener?.()
  }

  on(_event: string, listener?: Listener) {
    listener?.()
  }

  addControl() {}
  removeControl() {}
  getControlByName() {
    return null
  }
  getZoom() {
    return 0
  }
  getCenter() {
    return [0, 0]
  }
  setZoomAndCenter() {}
  setPitch() {}
  setMapStyle() {}
  async removeAllLayer() {}
  getLayers() {
    return []
  }
  getServiceContainer() {
    return {
      sceneService: {
        getSceneContainer: () => null
      }
    }
  }
  addLayer() {}
  destroy() {}
  exportMap() {
    return Promise.resolve('')
  }
  exportPng() {
    return Promise.resolve('')
  }
}

export class Zoom {
  controlOption: Record<string, any>
  mapsService = {
    map: new StubMap(),
    fitBounds: noop,
    setZoomAndCenter: noop
  }
  zoomIn = noop
  zoomOut = noop

  constructor(option?: Record<string, any>) {
    this.controlOption = option ?? {}
  }

  createButton(text = '', title = '', className = '', container?: HTMLElement, handler?: Listener) {
    const button = document.createElement('button')
    button.innerHTML = text
    button.title = title
    button.className = className
    handler && button.addEventListener('click', handler)
    container?.appendChild(button)
    return button
  }

  updateDisabled() {}
  getDefault(option: Record<string, any>) {
    return option
  }
}

export class Control {}
export class Marker {}
export class Dot {}
export class Choropleth {}
export class TextLayer {}
export class Plot {}

export const CONTAINER_TPL = '<div><div class="l7plot-legend__category-list"></div></div>'
export const ITEM_TPL = '<div class="l7plot-legend__category-item"></div>'
export const LIST_CLASS = 'l7plot-legend__category-list'

export class ExportImage {
  private readonly onExport?: (base64: string) => void

  constructor(options?: { onExport?: (base64: string) => void }) {
    this.onExport = options?.onExport
  }

  hide() {}
  getImage() {
    this.onExport?.('')
    return Promise.resolve('')
  }
}

export const DOM = {
  clearChildren(container?: HTMLElement) {
    if (container) {
      container.textContent = ''
    }
  }
}

export const PositionType = {
  BOTTOMRIGHT: 'bottomright'
}

export class GaodeMap extends StubMap {}
export class TMap extends StubMap {}
export class TencentMap extends StubMap {}

export function centroid() {
  return {
    geometry: {
      coordinates: [0, 0]
    }
  }
}
