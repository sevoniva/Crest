import type { ScatterOptions } from '@antv/g2plot/esm/plots/scatter'
import { Datum } from '@antv/g2plot/esm/types/common'
import { parseJson } from '@/views/chart/components/js/util'
import { MultiScatter } from './scatter-multi'

const numericValues = (data: Record<string, unknown>[], field: string) => {
  return data.map(item => Number(item[field])).filter(value => Number.isFinite(value))
}

const middleValue = (values: number[]) => {
  if (!values.length) {
    return 0
  }
  const sorted = [...values].sort((left, right) => left - right)
  const mid = Math.floor(sorted.length / 2)
  return sorted.length % 2 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2
}

const resolveReferenceValue = (configured: unknown, values: number[]) => {
  const value = Number(configured)
  if (Number.isFinite(value)) {
    return value
  }
  return middleValue(values)
}

const rangeMidpoint = (start: number, end: number) => start + (end - start) / 2

const resolveSizeRange = (configured: unknown): [number, number] | undefined => {
  if (!Array.isArray(configured) || configured.length < 2) {
    return undefined
  }
  const min = Number(configured[0])
  const max = Number(configured[1])
  if (!Number.isFinite(min) || !Number.isFinite(max)) {
    return undefined
  }
  return min <= max ? [min, max] : [max, min]
}

/**
 * 指标矩阵
 */
export class MetricMatrix extends MultiScatter {
  protected configColor(chart: Chart, options: ScatterOptions): ScatterOptions {
    const nextOptions = super.configColor(chart, options)
    const matrix = (parseJson(chart.customAttr).basicStyle as any)?.metricMatrix || {}
    const colorMapping = matrix.colorMapping
    if (!colorMapping || typeof colorMapping !== 'object') {
      return nextOptions
    }

    return {
      ...nextOptions,
      color: datum => {
        const key = String(datum?.color ?? datum?.category ?? datum?.field ?? '')
        return colorMapping[key] || matrix.defaultColor || key
      }
    }
  }

  protected configLabel(chart: Chart, options: ScatterOptions): ScatterOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const labelAttr = customAttr.label
    if (!labelAttr?.show) {
      return {
        ...options,
        label: false
      }
    }

    return {
      ...options,
      label: {
        fields: ['color'],
        layout: [{ type: 'hide-overlap' }, { type: 'limit-in-plot' }],
        offsetY: -10,
        style: {
          fill: labelAttr.color,
          fontSize: labelAttr.fontSize,
          fontFamily: chart.fontFamily
        },
        formatter: (datum: Datum) => {
          const source = datum as Record<string, any>
          const labelValue = source.dynamicLabelValue?.[0]
          return String(labelValue?.stringValue ?? labelValue?.value ?? source.color ?? '')
        }
      }
    }
  }

  protected setupOptions(chart: Chart, options: ScatterOptions): ScatterOptions {
    const nextOptions = super.setupOptions(chart, options)
    const matrix = (parseJson(chart.customAttr).basicStyle as any)?.metricMatrix || {}
    const referenceLines = Array.isArray(matrix.referenceLines) ? matrix.referenceLines : []
    const quadrants = Array.isArray(matrix.quadrants) ? matrix.quadrants : []
    const data = (nextOptions.data || []) as Record<string, unknown>[]
    const xValues = numericValues(data, String(nextOptions.xField || 'x'))
    const yValues = numericValues(data, String(nextOptions.yField || 'y'))
    if (!xValues.length || !yValues.length) {
      return nextOptions
    }

    const xMin = Math.min(0, ...xValues)
    const xMax = Math.max(...xValues) * 1.12
    const yMin = Math.min(0, ...yValues)
    const yMax = Math.max(...yValues) * 1.18
    const xReference = resolveReferenceValue(matrix.xReference, xValues)
    const yReference = resolveReferenceValue(matrix.yReference, yValues)
    const referenceLineColor = matrix.referenceLineColor || '#bfdbfe'
    const referenceTextColor = matrix.referenceTextColor || '#64748b'
    const pointSizeRange = resolveSizeRange(matrix.pointSizeRange)
    const annotations: any[] = []

    const quadrantRange = {
      'top-left': [xMin, yReference, xReference, yMax],
      'top-right': [xReference, yReference, xMax, yMax],
      'bottom-left': [xMin, yMin, xReference, yReference],
      'bottom-right': [xReference, yMin, xMax, yReference]
    }
    quadrants.forEach(quadrant => {
      const range = quadrantRange[quadrant.position]
      if (!range) {
        return
      }
      const [startX, startY, endX, endY] = range
      annotations.push({
        type: 'region',
        start: [startX, startY],
        end: [endX, endY],
        style: {
          fill: quadrant.color || '#eff6ff',
          fillOpacity: quadrant.opacity ?? 0.72
        }
      })
      if (quadrant.label) {
        annotations.push({
          type: 'text',
          position: [rangeMidpoint(startX, endX), rangeMidpoint(startY, endY)],
          content: quadrant.label,
          style: {
            fill: quadrant.textColor || referenceTextColor,
            fontSize: quadrant.fontSize || 12,
            fontWeight: 600,
            opacity: 0.72,
            textAlign: 'center'
          }
        })
      }
    })

    annotations.push(
      {
        type: 'line',
        start: [xReference, yMin],
        end: [xReference, yMax],
        style: {
          stroke: referenceLineColor,
          lineDash: [5, 5],
          lineWidth: 1
        },
        text: matrix.xReferenceLabel
          ? {
              content: matrix.xReferenceLabel,
              position: 'start',
              style: {
                fill: referenceTextColor,
                fontSize: 11
              }
            }
          : undefined
      },
      {
        type: 'line',
        start: [xMin, yReference],
        end: [xMax, yReference],
        style: {
          stroke: referenceLineColor,
          lineDash: [5, 5],
          lineWidth: 1
        },
        text: matrix.yReferenceLabel
          ? {
              content: matrix.yReferenceLabel,
              position: 'end',
              style: {
                fill: referenceTextColor,
                fontSize: 11
              }
            }
          : undefined
      }
    )

    referenceLines.forEach(line => {
      const slope = Number(line.value)
      if (!Number.isFinite(slope)) {
        return
      }
      annotations.push({
        type: 'line',
        start: [xMin, xMin * slope],
        end: [xMax, xMax * slope],
        style: {
          stroke: line.color || '#94a3b8',
          lineDash: [4, 4],
          lineWidth: 1
        },
        text: {
          content: line.label,
          position: 'end',
          style: {
            fill: line.color || '#94a3b8',
            fontSize: 12
          }
        }
      })
    })

    return {
      ...nextOptions,
      annotations: [...((nextOptions.annotations as any[]) || []), ...annotations],
      size: pointSizeRange || nextOptions.size || [8, 20],
      pointStyle: (datum: Record<string, unknown>) => {
        const lightness = Number(datum.lightness)
        const opacity = Number.isFinite(lightness) ? Math.max(0.68, Math.min(1, lightness / 100)) : 0.92
        return {
          stroke: matrix.pointStroke || '#ffffff',
          lineWidth: matrix.pointLineWidth ?? 2,
          shadowBlur: matrix.pointShadowBlur ?? 16,
          shadowColor: matrix.pointShadowColor || 'rgba(59,130,246,.22)',
          fillOpacity: opacity
        }
      },
      xAxis: {
        ...nextOptions.xAxis,
        min: xMin,
        max: xMax
      },
      yAxis: {
        ...nextOptions.yAxis,
        min: yMin,
        max: yMax
      }
    }
  }

  constructor() {
    super('metric-matrix')
  }
}
