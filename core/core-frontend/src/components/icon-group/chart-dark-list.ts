import richTextDark from '@/assets/svg/rich-text-dark.svg'
import { defineAsyncComponent } from 'vue'
import { iconChartMapEmpty } from './chart-dark-list-empty'
const svgs = import.meta.glob('@/assets/svg/chart-dark/*.svg')

const iconChartDarkMap = {
  'rich-text-dark': richTextDark
}
Object.keys(svgs).forEach(path => {
  const name = path.match(/\/assets\/svg\/chart-dark\/([^/]+)\.svg$/)[1]
  const chartName = iconChartMapEmpty[name]
  iconChartDarkMap[chartName] = defineAsyncComponent(svgs[path])
  if (chartName === 'line-dark') {
    iconChartDarkMap['chart-mix-dual-line-dark'] = iconChartDarkMap[chartName]
  }
})

export { iconChartDarkMap }
