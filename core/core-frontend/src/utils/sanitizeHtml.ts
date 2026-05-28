import xss from 'xss'

const safeHtmlFilter = new xss.FilterXSS({
  css: false,
  whiteList: {
    b: [],
    br: [],
    em: [],
    i: [],
    p: [],
    span: [],
    strong: [],
    u: []
  },
  stripIgnoreTag: true,
  stripIgnoreTagBody: ['script', 'style', 'iframe', 'object', 'embed']
})

export const sanitizeHtml = (value?: string | null) => safeHtmlFilter.process(value || '')
