/**
 * ECharts 按需引入封装：只注册折线图、柱状图、饼图与必要组件，
 * 避免整包全局注册（T03 决策：报表/工作台按需 import）。
 */
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'

echarts.use([
  LineChart, BarChart, PieChart,
  GridComponent, TooltipComponent, LegendComponent, TitleComponent,
  CanvasRenderer
])

export { echarts }
export type { EChartsOption }
