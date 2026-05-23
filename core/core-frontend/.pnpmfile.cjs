function readPackage(pkg) {
  if (pkg.name === 'exceljs' && pkg.version === '4.4.0') {
    pkg.dependencies = pkg.dependencies || {}
    pkg.dependencies.uuid = '^11.1.1'
  }
  if (pkg.dependencies?.['@antv/color-util']) {
    pkg.dependencies['@antv/color-util'] = 'link:./vendor/antv-color-util'
  }
  if (pkg.dependencies?.['@antv/adjust']) {
    pkg.dependencies['@antv/adjust'] = 'link:./vendor/antv-adjust'
  }
  if (pkg.dependencies?.['size-sensor']) {
    pkg.dependencies['size-sensor'] = 'link:./vendor/size-sensor'
  }
  if (pkg.name === 'fmin' && pkg.version === '0.0.2') {
    pkg.dependencies = {}
  }
  return pkg
}

module.exports = {
  hooks: {
    readPackage
  }
}
