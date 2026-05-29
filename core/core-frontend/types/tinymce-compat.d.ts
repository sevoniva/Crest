import 'tinymce'
import 'tinymce/tinymce'

declare module 'tinymce' {
  interface Editor {
    settings?: Record<string, any>
  }

  interface TinyMCE {
    editors?: Record<string, Editor>
  }
}

declare module 'tinymce/tinymce' {
  interface TinyMCE {
    editors?: Record<string, import('tinymce').Editor>
  }
}
