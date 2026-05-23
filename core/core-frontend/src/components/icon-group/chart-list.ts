import outerParams from '@/assets/svg/icon_params_setting.svg'
import filter from '@/assets/svg/filter.svg'
import pictureGroup from '@/assets/svg/picture-group.svg'
import richText from '@/assets/svg/rich-text.svg'
import { defineAsyncComponent } from 'vue'
import { iconChartMapEmpty } from './chart-list-empty'
const svgs = import.meta.glob('@/assets/svg/chart-light/*.svg')

const iconChartMap = {
  'rich-text': richText,
  'picture-group': pictureGroup,
  filter: filter,
  outerParams: outerParams
}
Object.keys(svgs).forEach(path => {
  const name = path.match(/\/assets\/svg\/chart-light\/([^/]+)\.svg$/)[1]
  const chartName = iconChartMapEmpty[name]
  iconChartMap[chartName] = defineAsyncComponent(svgs[path])
  if (chartName === 'line') {
    iconChartMap['chart-mix-dual-line'] = iconChartMap[chartName]
  }
})
export { iconChartMap }
