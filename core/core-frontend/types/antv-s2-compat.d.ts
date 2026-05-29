import '@antv/s2'

declare module '@antv/s2' {
  export type Style = S2Style
  export type ColumnNode = Node

  interface S2Style {
    rowExpandDepth?: number
    hierarchyCollapse?: boolean
    treeRowsWidth?: number
    colCfg?: Record<string, any>
    rowCfg?: Record<string, any>
    cellCfg?: Record<string, any>
    frozenColCount?: number
    frozenRowCount?: number
    [key: string]: any
  }

  interface S2Theme {
    colCellAlignConfig?: Record<string, any>
    dataCellAlignConfig?: Record<string, any>
    [key: string]: any
  }

  interface LayoutResult {
    getCellMeta?: (...args: any[]) => any
    [key: string]: any
  }

  interface CustomSVGIcon {
    svg?: string
  }

  interface HeaderActionIcon {
    iconNames?: string[]
  }

  interface TotalStatus {
    isRowTotal?: boolean
    isColTotal?: boolean
  }
}

declare global {
  interface CSSStyleDeclaration {
    MozTransform?: string
    msTransform?: string
    OTransform?: string
  }

  interface Element {
    style: CSSStyleDeclaration
  }
}
