<script lang="ts">
import { h } from 'vue'
import icon_expandDown_filled from '@/assets/svg/icon_expand-down_filled.svg'
import { ElMenuItem, ElSubMenu } from 'element-plus-secondary'

const title = props => {
  const { title } = props?.menu?.meta || {}
  return [h('span', null, { default: () => title })]
}

const HeaderMenuItem = props => {
  if (!props) return null
  const { children = [], hidden, path } = props?.menu || {}
  if (hidden) {
    return null
  }

  if (children?.length) {
    return h(
      ElSubMenu,
      {
        index: path,
        'popper-class': 'popper-class-menu',
        showTimeout: 1,
        expandCloseIcon: icon_expandDown_filled,
        expandOpenIcon: icon_expandDown_filled
      },
      {
        title: () => title(props),
        default: () => children.map(ele => h(HeaderMenuItem, { menu: ele, index: path }))
      }
    )
  }

  return h(
    ElMenuItem,
    { index: props.index ? `${props.index}/${path}` : path },
    {
      title: () => title(props)
    }
  )
}
export default HeaderMenuItem
</script>

<style lang="less">
.popper-class-menu {
  --active-color: #0f172a;
  min-width: 0 !important;
  padding: 0 !important;
  background: transparent !important;
  border: 0 !important;
  box-shadow: none !important;

  &.is-light {
    margin-top: -2px;
    background: transparent !important;
    border: 0 !important;
  }

  .ed-popper__arrow {
    display: none;
  }

  .ed-menu--popup {
    min-width: 132px !important;
    padding: 8px !important;
    background: #ffffff !important;
    border: 1px solid #e2e8f0 !important;
    border-radius: 12px !important;
    box-shadow: 0 12px 24px rgba(15, 23, 42, 0.1) !important;
  }

  .ed-menu-item,
  .ed-sub-menu__title {
    height: 34px;
    margin: 2px 0;
    padding: 0 12px !important;
    border-radius: 8px;
    color: #334155;
    font-family: var(--crest-font-sans);
    font-size: 14px;
    font-weight: 500;
    line-height: 34px;
    background: transparent;
  }

  .ed-menu-item:hover,
  .ed-sub-menu__title:hover {
    color: #0f172a;
    background: #f8fafc;
  }

  .ed-menu-item.is-active,
  .ed-menu-item.is-active:not(:hover) {
    color: var(--ed-color-primary);
    background: #eff6ff;

    &::after {
      display: none;
    }
  }

  .ed-sub-menu__icon-arrow {
    right: 10px;
    color: #94a3b8;
  }
}
</style>
