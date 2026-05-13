interface AreaNode {
  id: string
  name: string
  level: string
  pid: string
  children: AreaNode[]
}

interface CustomGeoArea {
  id: string
  name: string
}

type CustomGeoSubArea = CustomGeoArea & {
  geoAreaId: string
  scope: string
  scopeArr?: string[]
}
